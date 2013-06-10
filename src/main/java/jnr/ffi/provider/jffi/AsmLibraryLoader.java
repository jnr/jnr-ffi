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

import com.kenai.jffi.Function;
import jnr.ffi.*;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.InvokerUtil.*;
import static jnr.ffi.util.Annotations.sortedAnnotationCollection;
import static org.objectweb.asm.Opcodes.*;

public class AsmLibraryLoader extends LibraryLoader {
    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);
    private static final AtomicLong uniqueId = new AtomicLong(0);
    private static final ThreadLocal<AsmClassLoader> classLoader = new ThreadLocal<AsmClassLoader>();

    private final NativeRuntime runtime = NativeRuntime.getInstance();

    @Override
    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        AsmClassLoader oldClassLoader = classLoader.get();

        // Only create a new class loader if this was not a recursive call (i.e. loading a library as a result of loading another library)
        if (oldClassLoader == null) {
            classLoader.set(new AsmClassLoader(interfaceClass.getClassLoader()));
        }
        try {
            return generateInterfaceImpl(library, interfaceClass, libraryOptions, classLoader.get());
        } finally {
            if (oldClassLoader == null) classLoader.remove();
        }
    }

    private <T> T generateInterfaceImpl(final NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions,
                                        AsmClassLoader classLoader) {

        boolean debug = DEBUG && !interfaceClass.isAnnotationPresent(NoTrace.class);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = debug ? AsmUtil.newCheckClassAdapter(cw) : cw;

        AsmBuilder builder = new AsmBuilder(runtime, p(interfaceClass) + "$jnr$ffi$" + nextClassID.getAndIncrement(), cv, classLoader);

        cv.visit(V1_6, ACC_PUBLIC | ACC_FINAL, builder.getClassNamePath(), null, p(AbstractAsmLibraryInterface.class),
                new String[] { p(interfaceClass) });

        FunctionMapper functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();

        SignatureTypeMapper typeMapper;
        if (libraryOptions.containsKey(LibraryOption.TypeMapper)) {
            Object tm = libraryOptions.get(LibraryOption.TypeMapper);
            if (tm instanceof SignatureTypeMapper) {
                typeMapper = (SignatureTypeMapper) tm;
            } else if (tm instanceof TypeMapper) {
                typeMapper = new SignatureTypeMapperAdapter((TypeMapper) tm);
            } else {
                throw new IllegalArgumentException("TypeMapper option is not a valid TypeMapper instance");
            }
        } else {
            typeMapper = new NullTypeMapper();
        }

        typeMapper = new CompositeTypeMapper(typeMapper, 
                new CachingTypeMapper(new InvokerTypeMapper(new NativeClosureManager(runtime, typeMapper, classLoader), classLoader, NativeLibraryLoader.ASM_ENABLED)),
                new CachingTypeMapper(new AnnotationTypeMapper()));
        
        CallingConvention libraryCallingConvention = getCallingConvention(interfaceClass, libraryOptions);

        StubCompiler compiler = StubCompiler.newCompiler(runtime);

        final MethodGenerator[] generators = {
                !interfaceClass.isAnnotationPresent(NoX86.class)
                    ? new X86MethodGenerator(compiler) : new NotImplMethodGenerator(),
                new FastIntMethodGenerator(),
                new FastLongMethodGenerator(),
                new FastNumericMethodGenerator(),
                new BufferMethodGenerator()
        };
        
        InterfaceScanner scanner = new InterfaceScanner(interfaceClass, typeMapper, libraryCallingConvention);

        for (NativeFunction function : scanner.functions()) {
            String functionName = functionMapper.mapFunctionName(function.name(), new NativeFunctionMapperContext(library, function.annotations()));

            try {
                long functionAddress = library.findSymbolAddress(functionName);
                
                FromNativeContext resultContext = new MethodResultContext(runtime, function.getMethod());
                SignatureType signatureType = DefaultSignatureType.create(function.getMethod().getReturnType(), resultContext);
                ResultType resultType = getResultType(runtime, function.getMethod().getReturnType(),
                        resultContext.getAnnotations(), typeMapper.getFromNativeType(signatureType, resultContext),
                        resultContext);

                ParameterType[] parameterTypes = getParameterTypes(runtime, typeMapper, function.getMethod());

                Function jffiFunction = new Function(functionAddress, 
                        getCallContext(resultType, parameterTypes,function.convention(), function.isErrnoRequired())); 

                for (MethodGenerator g : generators) {
                    if (g.isSupported(resultType, parameterTypes, function.convention())) {
                        g.generate(builder, function.getMethod().getName(), jffiFunction, resultType, parameterTypes, !function.isErrnoRequired());
                        break;
                    }
                }

            } catch (SymbolNotFoundError ex) {
                String errorFieldName = "error_" + uniqueId.incrementAndGet();
                cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, errorFieldName, ci(String.class), null, ex.getMessage());
                generateFunctionNotFound(cv, builder.getClassNamePath(), errorFieldName, functionName, 
                        function.getMethod().getReturnType(), function.getMethod().getParameterTypes());
            }
        }

        // generate global variable accessors
        VariableAccessorGenerator variableAccessorGenerator = new VariableAccessorGenerator(runtime);
        for (NativeVariable v : scanner.variables()) {
            Method m = v.getMethod();
            java.lang.reflect.Type variableType = ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
            if (!(variableType instanceof Class)) {
                throw new IllegalArgumentException("unsupported variable class: " + variableType);
            }
            String functionName = functionMapper.mapFunctionName(m.getName(), null);
            try {
                variableAccessorGenerator.generate(builder, interfaceClass, m.getName(),
                        library.findSymbolAddress(functionName), (Class) variableType, sortedAnnotationCollection(m.getAnnotations()),
                        typeMapper, classLoader);

            } catch (SymbolNotFoundError ex) {
                String errorFieldName = "error_" + uniqueId.incrementAndGet();
                cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, errorFieldName, ci(String.class), null, ex.getMessage());
                generateFunctionNotFound(cv, builder.getClassNamePath(), errorFieldName, functionName, m.getReturnType(), m.getParameterTypes());
            }
        }

        // Create the constructor to set the instance fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>",
                sig(void.class, jnr.ffi.Runtime.class, NativeLibrary.class, Object[].class),
                null, null);
        init.start();
        // Invoke the super class constructor as super(Library)
        init.aload(0);
        init.aload(1);
        init.aload(2);
        init.invokespecial(p(AbstractAsmLibraryInterface.class), "<init>", sig(void.class, jnr.ffi.Runtime.class, NativeLibrary.class));

        builder.emitFieldInitialization(init, 3);

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

            Class<T> implClass = classLoader.defineClass(builder.getClassNamePath().replace("/", "."), bytes);
            Constructor<T> cons = implClass.getDeclaredConstructor(jnr.ffi.Runtime.class, NativeLibrary.class, Object[].class);
            T result = cons.newInstance(runtime, library, builder.getObjectFieldValues());

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
