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

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.ObjectParameterInfo;
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
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static org.objectweb.asm.Opcodes.*;

public class AsmLibraryLoader extends LibraryLoader {
    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);


    private final NativeRuntime runtime = NativeRuntime.getInstance();


    boolean isInterfaceSupported(Class interfaceClass, Map<LibraryOption, ?> options) {
        TypeMapper typeMapper = options.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) options.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;

        for (Method m : interfaceClass.getDeclaredMethods()) {
            if (!isReturnTypeSupported(m.getReturnType()) && getResultConverter(m, typeMapper) == null) {
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

        String className = p(interfaceClass) + "$jaffl$" + nextClassID.getAndIncrement();
        AsmBuilder builder = new AsmBuilder(className, cv);


        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, p(AbstractAsmLibraryInterface.class),
                new String[] { p(interfaceClass) });

        // Create the constructor to set the 'library' & functions fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC, "<init>",
                sig(void.class, NativeLibrary.class, Function[].class,
                        FromNativeConverter[].class, ToNativeConverter[].class, ObjectParameterInfo[].class),
                null, null));
        init.start();
        // Invokes the super class constructor as super(Library)

        init.aload(0);
        init.aload(1);

        init.invokespecial(p(AbstractAsmLibraryInterface.class), "<init>", sig(void.class, NativeLibrary.class));
        
        final Method[] methods = interfaceClass.getMethods();
        FromNativeConverter[] resultConverters = new FromNativeConverter[methods.length];
        ToNativeConverter[][] parameterConverters = new ToNativeConverter[methods.length][0];
        
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


        for (int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            final Class javaReturnType = m.getReturnType();
            final Class[] javaParameterTypes = m.getParameterTypes();
            final Annotation[] resultAnnotations = m.getAnnotations();
            final Annotation[][] parameterAnnotations = m.getParameterAnnotations();

            resultConverters[i] = getResultConverter(m, typeMapper);
            ResultType resultType = InvokerUtil.getResultType(runtime, m.getReturnType(),
                    resultAnnotations, resultConverters[i]);

            parameterConverters[i] = new ToNativeConverter[javaParameterTypes.length];
            ParameterType[] parameterTypes = new ParameterType[javaParameterTypes.length];

            for (int pidx = 0; pidx < javaParameterTypes.length; ++pidx) {
                parameterConverters[i][pidx] = getParameterConverter(m, pidx, typeMapper, closureManager);
                parameterTypes[pidx] = InvokerUtil.getParameterType(runtime, javaParameterTypes[pidx],
                        parameterAnnotations[pidx], parameterConverters[i][pidx]);
            }

            // Stash the name of the function in a static field
            String functionName = functionMapper.mapFunctionName(m.getName(), null);
            cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "name_" + i, ci(String.class), null, functionName);

            // Allow individual methods to set the calling convention to stdcall
            CallingConvention callingConvention = m.getAnnotation(StdCall.class) != null
                    ? CallingConvention.STDCALL : libraryCallingConvention;
            boolean saveErrno = InvokerUtil.requiresErrno(m);
            Function function;
            try {
                function = getFunction(library.findSymbolAddress(functionName),
                    resultType, parameterTypes, saveErrno, callingConvention);
            } catch (SymbolNotFoundError ex) {
                cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "error_" + i, ci(String.class), null, ex.getMessage());
                generateFunctionNotFound(cv, className, i, functionName, javaReturnType, javaParameterTypes);
                continue;
            }

            for (MethodGenerator g : generators) {
                if (g.isSupported(resultType, parameterTypes, callingConvention)) {
                    g.generate(builder, m.getName(), function, resultType, parameterTypes, !saveErrno);
                    break;
                }
            }
        }

        Function[] functions = builder.getFunctionArray();
        for (int i = 0; i < functions.length; i++) {
            Function function = functions[i];
            cv.visitField(ACC_PRIVATE | ACC_FINAL, builder.getFunctionFieldName(function), ci(Function.class), null, null);
            cv.visitField(ACC_PRIVATE | ACC_FINAL, builder.getCallContextFieldName(function), ci(CallContext.class), null, null);
            cv.visitField(ACC_PRIVATE | ACC_FINAL, builder.getFunctionAddressFieldName(function), ci(long.class), null, null);

            init.aload(2);
            init.pushInt(i);
            init.aaload();
            init.dup();
            init.dup();

            init.aload(0);
            init.swap();
            init.putfield(className, builder.getFunctionFieldName(function), ci(Function.class));


            init.aload(0);
            init.swap();
            init.invokevirtual(Function.class, "getCallContext", CallContext.class);
            init.putfield(className, builder.getCallContextFieldName(function), ci(CallContext.class));

            init.aload(0);
            init.swap();
            init.invokevirtual(Function.class, "getFunctionAddress", long.class);
            init.putfield(className, builder.getFunctionAddressFieldName(function), ci(long.class));
        }

        FromNativeConverter[] fromNativeConverters = builder.getFromNativeConverterArray();
        for (int i = 0; i < fromNativeConverters.length; i++) {
            String fieldName = builder.getFromNativeConverterName(fromNativeConverters[i]);
            cv.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(FromNativeConverter.class), null, null);
            init.aload(0);
            init.aload(3);
            init.pushInt(i);
            init.aaload();

            init.putfield(className, fieldName, ci(FromNativeConverter.class));
        }

        ToNativeConverter[] toNativeConverters = builder.getToNativeConverterArray();
        for (int i = 0; i < toNativeConverters.length; i++) {
            String fieldName = builder.getToNativeConverterName(toNativeConverters[i]);
            cv.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(ToNativeConverter.class), null, null);
            init.aload(0);
            init.aload(4);
            init.pushInt(i);
            init.aaload();
            init.putfield(className, fieldName, ci(ToNativeConverter.class));
        }

        ObjectParameterInfo[] objectParameterInfo = builder.getObjectParameterInfoArray();
        for (int i = 0; i < objectParameterInfo.length; i++) {
            String fieldName = builder.getObjectParameterInfoName(objectParameterInfo[i]);
            cv.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(ObjectParameterInfo.class), null, null);
            init.aload(0);
            init.aload(5);
            init.pushInt(i);
            init.aaload();
            init.putfield(className, fieldName, ci(ObjectParameterInfo.class));
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

            Class implClass = new AsmClassLoader(interfaceClass.getClassLoader()).defineClass(className.replace("/", "."), bytes);
            Constructor<T> cons = implClass.getDeclaredConstructor(NativeLibrary.class, Function[].class,
                    FromNativeConverter[].class, ToNativeConverter[].class, ObjectParameterInfo[].class);
            T result = cons.newInstance(library, functions, fromNativeConverters, toNativeConverters, objectParameterInfo);

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
        Class parameterType = m.getParameterTypes()[parameterIndex];
        ToNativeConverter conv = typeMapper.getToNativeConverter(parameterType);
        if (conv != null) {
            return new ParameterConverter(conv, new MethodParameterContext(m, parameterIndex));

        } else if (Enum.class.isAssignableFrom(parameterType)) {
            return EnumMapper.getInstance(parameterType.asSubclass(Enum.class));

        } else if (isDelegate(parameterType)) {
            return closureManager.getClosureFactory(parameterType);

        } else if (ByReference.class.isAssignableFrom(parameterType)) {
            return new ByReferenceParameterConverter(ParameterFlags.parse(m.getParameterAnnotations()[parameterIndex]));

        } else {
            return null;
        }
    }

    private final FromNativeConverter getResultConverter(Method m, TypeMapper typeMapper) {
        Class returnType = m.getReturnType();
        FromNativeConverter conv = typeMapper.getFromNativeConverter(returnType);
        if (conv != null) {
            return new ResultConverter(conv, new MethodResultContext(m));

        } else if (Enum.class.isAssignableFrom(returnType)) {
            return EnumMapper.getInstance(returnType.asSubclass(Enum.class));

        } else {
            return null;
        }
    }

    private static final com.kenai.jffi.CallingConvention getCallingConvention(Class interfaceClass, Map<LibraryOption, ?> options) {
        if (interfaceClass.getAnnotation(StdCall.class) != null) {
            return com.kenai.jffi.CallingConvention.STDCALL;
        }
        return InvokerUtil.getCallingConvention(options);
    }

    private final void generateFunctionNotFound(ClassVisitor cv, String className, int idx, String functionName,
            Class returnType, Class[] parameterTypes) {
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, functionName,
                sig(returnType, parameterTypes), null, null));
        mv.start();
        mv.getstatic(className, "error_" + idx, ci(String.class));
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
                || Struct.class.isAssignableFrom(type);

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
