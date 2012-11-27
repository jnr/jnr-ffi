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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.Buffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.InvokerUtil.*;
import static org.objectweb.asm.Opcodes.*;

public class AsmLibraryLoader extends LibraryLoader {
    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);
    private static final AtomicLong uniqueId = new AtomicLong(0);
    private static final ThreadLocal<AsmClassLoader> classLoader = new ThreadLocal<AsmClassLoader>();

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
                if (!isParameterTypeSupported(c) && typeMapper.getToNativeConverter(c, null) == null) {
                    System.err.println("Unsupported parameter type: " + c);
                    return false;
                }
            }
        }

        return true;
    }

    static AsmClassLoader getCurrentClassLoader() {
        return classLoader.get();
    }

    @Override
    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        AsmClassLoader oldClassLoader = classLoader.get();
        classLoader.set(new AsmClassLoader(interfaceClass.getClassLoader()));
        try {
            return generateInterfaceImpl(library, interfaceClass, libraryOptions);
        } finally {
            classLoader.set(oldClassLoader);
        }
    }

    private <T> T generateInterfaceImpl(final NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {

        boolean debug = DEBUG && !interfaceClass.isAnnotationPresent(NoTrace.class);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = debug ? AsmUtil.newCheckClassAdapter(cw) : cw;

        AsmBuilder builder = new AsmBuilder(p(interfaceClass) + "$jaffl$" + nextClassID.getAndIncrement(), cv);

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, builder.getClassNamePath(), null, p(AbstractAsmLibraryInterface.class),
                new String[] { p(interfaceClass) });

        FunctionMapper functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();

        TypeMapper typeMapper = libraryOptions.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) libraryOptions.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;
        NativeClosureManager closureManager = new NativeClosureManager(runtime, typeMapper);
        com.kenai.jffi.CallingConvention libraryCallingConvention = getCallingConvention(interfaceClass, libraryOptions);

        BufferMethodGenerator bufgen = new BufferMethodGenerator();
        StubCompiler compiler = StubCompiler.newCompiler();

        final MethodGenerator[] generators = {
                !interfaceClass.isAnnotationPresent(NoX86.class)
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

            String functionName = functionMapper.mapFunctionName(m.getName(), null);

            // Allow individual methods to set the calling convention to stdcall
            CallingConvention callingConvention = m.isAnnotationPresent(StdCall.class)
                    ? CallingConvention.STDCALL : libraryCallingConvention;
            boolean saveErrno = InvokerUtil.requiresErrno(m);
            try {
                generateFunctionInvocation(runtime, builder, m, library.findSymbolAddress(functionName),
                        callingConvention, saveErrno, typeMapper, closureManager, generators);

            } catch (SymbolNotFoundError ex) {
                String errorFieldName = "error_" + uniqueId.incrementAndGet();
                cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, errorFieldName, ci(String.class), null, ex.getMessage());
                generateFunctionNotFound(cv, builder.getClassNamePath(), errorFieldName, functionName, m.getReturnType(), m.getParameterTypes());
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


        // Create the constructor to set the instance fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>",
                sig(void.class, NativeLibrary.class, Object[].class),
                null, null);
        init.start();
        // Invoke the super class constructor as super(Library)
        init.aload(0);
        init.aload(1);
        init.invokespecial(p(AbstractAsmLibraryInterface.class), "<init>", sig(void.class, NativeLibrary.class));

        builder.emitFieldInitialization(init, 2);

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

            Class<T> implClass = classLoader.get().defineClass(builder.getClassNamePath().replace("/", "."), bytes);
            Constructor<T> cons = implClass.getDeclaredConstructor(NativeLibrary.class, Object[].class);
            T result = cons.newInstance(library, builder.getObjectFieldValues());

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

    private static FromNativeConverter getResultConverter(Method m, TypeMapper typeMapper, NativeClosureManager closureManager) {
        return getFromNativeConverter(m.getReturnType(), typeMapper, new SimpleNativeContext(m.getAnnotations()), closureManager);
    }

    private static com.kenai.jffi.CallingConvention getCallingConvention(Class interfaceClass, Map<LibraryOption, ?> options) {
        if (interfaceClass.isAnnotationPresent(StdCall.class)) {
            return com.kenai.jffi.CallingConvention.STDCALL;
        }
        return InvokerUtil.getCallingConvention(options);
    }

    private void generateFunctionNotFound(ClassVisitor cv, String className, String errorFieldName, String functionName,
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


}
