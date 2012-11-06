/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import jnr.ffi.*;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.IdentityFunctionMapper;
import jnr.ffi.provider.NullTypeMapper;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.util.EnumMapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.Buffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.InvokerUtil.getFromNativeConverter;
import static jnr.ffi.provider.jffi.InvokerUtil.getToNativeConverter;
import static org.objectweb.asm.Opcodes.*;

public class AsmLibraryLoader extends LibraryLoader {
    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);
    private static final AtomicLong uniqueId = new AtomicLong(0);


    private final NativeRuntime runtime = NativeRuntime.getInstance();


    boolean isInterfaceSupported(Class interfaceClass, Map<LibraryOption, ?> options) {
        TypeMapper typeMapper = options.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) options.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;
        NativeClosureManager closureManager = new NativeClosureManager(runtime, typeMapper);
        for (Method m : interfaceClass.getDeclaredMethods()) {
            if (!isReturnTypeSupported(m.getReturnType()) && getResultConverter(m, typeMapper, closureManager) == null) {
                System.err.println("Unsupported return type: " + m.getReturnType());
                return false;
            }
            for (Class c: m.getParameterTypes()) {
                if (!isParameterTypeSupported(c) && typeMapper.getToNativeConverter(c) == null) {
                    System.err.println("Unsupported parameter type: " + c);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return generateInterfaceImpl(library, interfaceClass, libraryOptions);
    }

    private final <T> T generateInterfaceImpl(final NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {

        boolean debug = DEBUG && interfaceClass.getAnnotation(NoTrace.class) == null;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = debug ? AsmUtil.newCheckClassAdapter(cw) : cw;

        AsmBuilder builder = new AsmBuilder(p(interfaceClass) + "$jaffl$" + nextClassID.getAndIncrement(), cv);

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, builder.getClassNamePath(), null, p(AbstractAsmLibraryInterface.class),
                new String[] { p(interfaceClass) });

        // Create the constructor to set the 'library' & functions fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>",
                sig(void.class, NativeLibrary.class, Object[].class),
                null, null);
        init.start();

        // Invoke the super class constructor as super(Library)
        init.aload(0);
        init.aload(1);
        init.invokespecial(p(AbstractAsmLibraryInterface.class), "<init>", sig(void.class, NativeLibrary.class));
        
        FunctionMapper functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();

        TypeMapper typeMapper = libraryOptions.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) libraryOptions.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;
        NativeClosureManager closureManager = new NativeClosureManager(runtime, typeMapper);
        com.kenai.jffi.CallingConvention libraryCallingConvention = getCallingConvention(interfaceClass, libraryOptions);

        BufferMethodGenerator bufgen = new BufferMethodGenerator();
        StubCompiler compiler = StubCompiler.newCompiler();

        final MethodGenerator[] generators = {
                interfaceClass.getAnnotation(NoX86.class) == null
                    ? new X86MethodGenerator(compiler, bufgen) : new NotImplMethodGenerator(),
                new FastIntMethodGenerator(bufgen),
                new FastLongMethodGenerator(bufgen),
                new FastNumericMethodGenerator(bufgen),
                bufgen
        };



        for (Method m : interfaceClass.getMethods()) {
            if (Variable.class.isAssignableFrom(m.getReturnType())) {
                continue;
            }
            final Class javaReturnType = m.getReturnType();
            final Class[] javaParameterTypes = m.getParameterTypes();
            final Annotation[] resultAnnotations = m.getAnnotations();
            final Annotation[][] parameterAnnotations = m.getParameterAnnotations();

            ResultType resultType = InvokerUtil.getResultType(runtime, m.getReturnType(),
                    resultAnnotations, getResultConverter(m, typeMapper, closureManager));

            ParameterType[] parameterTypes = new ParameterType[javaParameterTypes.length];

            for (int pidx = 0; pidx < javaParameterTypes.length; ++pidx) {
                parameterTypes[pidx] = InvokerUtil.getParameterType(runtime, javaParameterTypes[pidx],
                        parameterAnnotations[pidx], getParameterConverter(m, pidx, typeMapper, closureManager));
            }

            String functionName = functionMapper.mapFunctionName(m.getName(), null);

            // Allow individual methods to set the calling convention to stdcall
            CallingConvention callingConvention = m.getAnnotation(StdCall.class) != null
                    ? CallingConvention.STDCALL : libraryCallingConvention;
            boolean saveErrno = InvokerUtil.requiresErrno(m);
            try {
                Function function = getFunction(library.findSymbolAddress(functionName),
                    resultType, parameterTypes, saveErrno, callingConvention);

                for (MethodGenerator g : generators) {
                    if (g.isSupported(resultType, parameterTypes, callingConvention)) {
                        g.generate(builder, m.getName(), function, resultType, parameterTypes, !saveErrno);
                        break;
                    }
                }
            } catch (SymbolNotFoundError ex) {
                String errorFieldName = "error_" + uniqueId.incrementAndGet();
                cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, errorFieldName, ci(String.class), null, ex.getMessage());
                generateFunctionNotFound(cv, builder.getClassNamePath(), errorFieldName, functionName, javaReturnType, javaParameterTypes);
            }
        }

        // generate global variable accessors
        VariableAccessorGenerator variableAccessorGenerator = new VariableAccessorGenerator();
        for (Method m : interfaceClass.getMethods()) {
            if (Variable.class == m.getReturnType()) {
                java.lang.reflect.Type variableType = ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
                if (!(variableType instanceof Class)) {
                    throw new IllegalArgumentException("unsupported variable class: " + variableType);
                }
                String functionName = functionMapper.mapFunctionName(m.getName(), null);
                try {
                    variableAccessorGenerator.generate(builder, interfaceClass, m.getName(),
                            library.findSymbolAddress(functionName), (Class) variableType, m.getAnnotations(),
                            typeMapper, closureManager);

                } catch (SymbolNotFoundError ex) {
                    String errorFieldName = "error_" + uniqueId.incrementAndGet();
                    cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, errorFieldName, ci(String.class), null, ex.getMessage());
                    generateFunctionNotFound(cv, builder.getClassNamePath(), errorFieldName, functionName, m.getReturnType(), m.getParameterTypes());
                }
            }
        }



        AsmBuilder.ObjectField[] fields = builder.getObjectFieldArray();
        Object[] fieldObjects = new Object[fields.length];
        for (int i = 0; i < fieldObjects.length; i++) {
            fieldObjects[i] = fields[i].value;
            String fieldName = fields[i].name;
            cv.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(fields[i].klass), null, null);
            init.aload(0);
            init.aload(2);
            init.pushInt(i);
            init.aaload();

            if (fields[i].klass.isPrimitive()) {
                Class boxedType = boxedType(fields[i].klass);
                init.checkcast(boxedType);
                unboxNumber(init, boxedType, fields[i].klass);
            } else {
                init.checkcast(fields[i].klass);
            }
            init.putfield(builder.getClassNamePath(), fieldName, ci(fields[i].klass));
        }

        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        cv.visitEnd();

        try {
            byte[] bytes = cw.toByteArray();
            if (debug) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }

            Class implClass = new AsmClassLoader(interfaceClass.getClassLoader()).defineClass(builder.getClassNamePath().replace("/", "."), bytes);
            Constructor<T> cons = implClass.getDeclaredConstructor(NativeLibrary.class, Object[].class);
            T result = cons.newInstance(library, fieldObjects);

            // Attach any native method stubs - we have to delay this until the
            // implementation class is loaded for it to work.
            System.err.flush();
            System.out.flush();
            compiler.attach(implClass);

            return result;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private final ToNativeConverter getParameterConverter(Method m, int parameterIndex,
                                                          TypeMapper typeMapper, NativeClosureManager closureManager) {
        return getToNativeConverter(m.getParameterTypes()[parameterIndex], m.getParameterAnnotations()[parameterIndex],
                typeMapper, closureManager,
                new MethodParameterContext(m, parameterIndex));
    }


    private final FromNativeConverter getResultConverter(Method m, TypeMapper typeMapper, NativeClosureManager closureManager) {
        return getFromNativeConverter(m.getReturnType(), m.getAnnotations(), typeMapper, closureManager, new MethodResultContext(m));
    }

    private static final com.kenai.jffi.CallingConvention getCallingConvention(Class interfaceClass, Map<LibraryOption, ?> options) {
        if (interfaceClass.getAnnotation(StdCall.class) != null) {
            return com.kenai.jffi.CallingConvention.STDCALL;
        }
        return InvokerUtil.getCallingConvention(options);
    }

    private final void generateFunctionNotFound(ClassVisitor cv, String className, String errorFieldName, String functionName,
                                                Class returnType, Class[] parameterTypes) {
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv, ACC_PUBLIC | ACC_FINAL, functionName,
                sig(returnType, parameterTypes), null, null);
        mv.start();
        mv.getstatic(className, errorFieldName, ci(String.class));
        mv.invokestatic(AsmRuntime.class, "newUnsatisifiedLinkError", UnsatisfiedLinkError.class, String.class);
        mv.athrow();
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }


    private static Function getFunction(long address, ResultType resultType, ParameterType[] parameterTypes,
                                        boolean requiresErrno, CallingConvention convention) {
        return new Function(address, InvokerUtil.getCallContext(resultType, parameterTypes, convention, requiresErrno));
    }

    private static boolean isReturnTypeSupported(Class type) {
        return type.isPrimitive() || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
                || Enum.class.isAssignableFrom(type)
                || Pointer.class == type || Address.class == type
                || String.class == type
                || Struct.class.isAssignableFrom(type)
                || Variable.class == type
                ;

    }

    private static boolean isParameterTypeSupported(Class type) {
        return type.isPrimitive() || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
                || Pointer.class.isAssignableFrom(type) || Address.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)
                || Buffer.class.isAssignableFrom(type)
                || (type.isArray() && type.getComponentType().isPrimitive())
                || Struct.class.isAssignableFrom(type)
                || (type.isArray() && Struct.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && Pointer.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && CharSequence.class.isAssignableFrom(type.getComponentType()))
                || CharSequence.class.isAssignableFrom(type)
                || ByReference.class.isAssignableFrom(type)
                || StringBuilder.class.isAssignableFrom(type)
                || StringBuffer.class.isAssignableFrom(type)
                || isDelegate(type)
                ;
    }


}
