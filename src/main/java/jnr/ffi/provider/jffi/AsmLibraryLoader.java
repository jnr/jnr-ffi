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

import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.ffi.provider.jffi.InvokerUtil.getCallContext;
import static jnr.ffi.provider.jffi.InvokerUtil.getCallingConvention;
import static jnr.ffi.provider.jffi.InvokerUtil.getParameterTypes;
import static jnr.ffi.provider.jffi.InvokerUtil.getResultType;
import static jnr.ffi.util.Annotations.sortedAnnotationCollection;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.V1_6;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.annotations.Synchronized;
import jnr.ffi.mapper.CachingTypeMapper;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.DefaultSignatureType;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.mapper.MethodResultContext;
import jnr.ffi.mapper.SignatureType;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapperAdapter;
import jnr.ffi.mapper.TypeMapper;
import jnr.ffi.provider.IdentityFunctionMapper;
import jnr.ffi.provider.InterfaceScanner;
import jnr.ffi.provider.Invoker;
import jnr.ffi.provider.NativeFunction;
import jnr.ffi.provider.NativeVariable;
import jnr.ffi.provider.NullTypeMapper;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.ffi.provider.jffi.AsmBuilder.ObjectField;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.kenai.jffi.Function;

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

        CompositeTypeMapper closureTypeMapper = new CompositeTypeMapper(typeMapper, 
                new CachingTypeMapper(new InvokerTypeMapper(null, classLoader, NativeLibraryLoader.ASM_ENABLED)),
                new CachingTypeMapper(new AnnotationTypeMapper()));
        
        typeMapper = new CompositeTypeMapper(typeMapper, 
                new CachingTypeMapper(new InvokerTypeMapper(new NativeClosureManager(runtime, closureTypeMapper, classLoader), classLoader, NativeLibraryLoader.ASM_ENABLED)),
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
        
        DefaultInvokerFactory invokerFactory = new DefaultInvokerFactory(runtime, library, typeMapper, functionMapper, libraryCallingConvention, libraryOptions, interfaceClass.isAnnotationPresent(Synchronized.class));
        InterfaceScanner scanner = new InterfaceScanner(interfaceClass, typeMapper, libraryCallingConvention);

        for (NativeFunction function : scanner.functions()) {
            if (function.getMethod().isVarArgs()) {
                ObjectField field = builder.getObjectField(invokerFactory.createInvoker(function.getMethod()), Invoker.class);
                generateVarargsInvocation(builder, function.getMethod(), field);
                continue;
            }

            String functionName = functionMapper.mapFunctionName(function.name(), new NativeFunctionMapperContext(library, function.annotations()));

            try {
                long functionAddress = library.findSymbolAddress(functionName);
                
                FromNativeContext resultContext = new MethodResultContext(runtime, function.getMethod());
                SignatureType signatureType = DefaultSignatureType.create(function.getMethod().getReturnType(), resultContext);
                ResultType resultType = getResultType(runtime, function.getMethod().getReturnType(),
                        resultContext.getAnnotations(), typeMapper.getFromNativeType(signatureType, resultContext),
                        resultContext);

                ParameterType[] parameterTypes = getParameterTypes(runtime, typeMapper, function.getMethod());

                boolean saveError = jnr.ffi.LibraryLoader.saveError(libraryOptions, function.hasSaveError(), function.hasIgnoreError());

                Function jffiFunction = new Function(functionAddress, 
                        getCallContext(resultType, parameterTypes,function.convention(), saveError));

                for (MethodGenerator g : generators) {
                    if (g.isSupported(resultType, parameterTypes, function.convention())) {
                        g.generate(builder, function.getMethod().getName(), jffiFunction, resultType, parameterTypes, !saveError);
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

    private void generateVarargsInvocation(AsmBuilder builder, Method m, ObjectField field) {
        Class[] parameterTypes = m.getParameterTypes();
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor(), ACC_PUBLIC | ACC_FINAL,
                m.getName(),
                sig(m.getReturnType(), parameterTypes), null, null);
        mv.start();

        // Retrieve the invoker
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), field.name, ci(Invoker.class));

        //Push ref to this
        mv.aload(0);
        
        //Construct the params array
        mv.pushInt(parameterTypes.length);
        mv.anewarray(p(Object.class));
        
        int slot = 1;
        for (int i = 0; i < parameterTypes.length; i++) {
            mv.dup();
            mv.pushInt(i);
            if (parameterTypes[i].equals(long.class)) {
                mv.lload(slot);
                mv.invokestatic(Long.class, "valueOf", Long.class, long.class);
                slot++;
            } else if (parameterTypes[i].equals(double.class)) {
                mv.dload(slot);
                mv.invokestatic(Double.class, "valueOf", Double.class, double.class);
                slot++;
            } else if (parameterTypes[i].equals(int.class)) {
                mv.iload(slot);
                mv.invokestatic(Integer.class, "valueOf", Integer.class, int.class);
            } else if (parameterTypes[i].equals(float.class)) {
                mv.fload(slot);
                mv.invokestatic(Float.class, "valueOf", Float.class, float.class);
            } else if (parameterTypes[i].equals(short.class)) {
                mv.iload(slot);
                mv.i2s();
                mv.invokestatic(Short.class, "valueOf", Short.class, short.class);
            } else if (parameterTypes[i].equals(char.class)) {
                mv.iload(slot);
                mv.i2c();
                mv.invokestatic(Character.class, "valueOf", Character.class, char.class);
            } else if (parameterTypes[i].equals(byte.class)) {
                mv.iload(slot);
                mv.i2b();
                mv.invokestatic(Byte.class, "valueOf", Byte.class, byte.class);
            } else if (parameterTypes[i].equals(boolean.class)) {
                mv.iload(slot);
                mv.i2b();
                mv.invokestatic(Boolean.class, "valueOf", Boolean.class, boolean.class);
            } else {
                mv.aload(slot);
            }
            mv.aastore();
            slot++;
        }
        
        // call invoker(this, parameters)
        mv.invokeinterface(jnr.ffi.provider.Invoker.class, "invoke", Object.class, Object.class, Object[].class);
        
        Class<?> returnType = m.getReturnType();
        if (returnType.equals(long.class)) {
            mv.checkcast(Long.class);
            mv.invokevirtual(Long.class, "longValue", long.class);
            mv.lreturn();
        } else if (returnType.equals(double.class)) {
            mv.checkcast(Double.class);
            mv.invokevirtual(Double.class, "doubleValue", double.class);
            mv.dreturn();
        } else if (returnType.equals(int.class)) {
            mv.checkcast(Integer.class);
            mv.invokevirtual(Integer.class, "intValue", int.class);
            mv.ireturn();
        } else if (returnType.equals(float.class)) {
            mv.checkcast(Float.class);
            mv.invokevirtual(Float.class, "floatValue", float.class);
            mv.freturn();
        } else if (returnType.equals(short.class)) {
            mv.checkcast(Short.class);
            mv.invokevirtual(Short.class, "shortValue", short.class);
            mv.ireturn();
        } else if (returnType.equals(char.class)) {
            mv.checkcast(Character.class);
            mv.invokevirtual(Character.class, "charValue", char.class);
            mv.ireturn();
        } else if (returnType.equals(byte.class)) {
            mv.checkcast(Byte.class);
            mv.invokevirtual(Byte.class, "byteValue", byte.class);
            mv.ireturn();
        } else if (returnType.equals(boolean.class)) {
            mv.checkcast(Boolean.class);
            mv.invokevirtual(Boolean.class, "booleanValue", boolean.class);
            mv.ireturn();
        } else if (void.class.isAssignableFrom(m.getReturnType())) {
           mv.voidreturn();
        } else {
            mv.checkcast(m.getReturnType());
            mv.areturn();
        }
        
        mv.visitMaxs(100, AsmUtil.calculateLocalVariableSpace(parameterTypes) + 1);
        mv.visitEnd();
    }
}
