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
import com.kenai.jffi.Platform;
import jnr.ffi.*;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.*;
import jnr.ffi.provider.IdentityFunctionMapper;
import jnr.ffi.provider.NullTypeMapper;
import jnr.ffi.Struct;
import jnr.ffi.util.EnumMapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.NumberUtil.*;
import static org.objectweb.asm.Opcodes.*;

public class AsmLibraryLoader extends LibraryLoader {
    public final static boolean DEBUG = false || Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);

    private final AtomicLong nextIvarID = new AtomicLong(0);
    private final AtomicLong nextMethodID = new AtomicLong(0);
    private final NativeClosureManager closureManager = NativeRuntime.getInstance().getClosureManager();

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
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        String className = p(interfaceClass) + "$jaffl$" + nextClassID.getAndIncrement();
        AsmBuilder builder = new AsmBuilder(className, cv);

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, p(AbstractAsmLibraryInterface.class),
                new String[] { p(interfaceClass) });

        // Create the constructor to set the 'library' & functions fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC, "<init>",
                sig(void.class, NativeLibrary.class, Function[].class, FromNativeConverter[].class, ToNativeConverter[].class),
                null, null));
        init.start();
        // Invokes the super class constructor as super(Library)

        init.aload(0);
        init.aload(1);

        init.invokespecial(p(AbstractAsmLibraryInterface.class), "<init>", sig(void.class, NativeLibrary.class));
        
        final Method[] methods = interfaceClass.getMethods();
        Function[] functions = new Function[methods.length];
        FromNativeConverter[] resultConverters = new FromNativeConverter[methods.length];
        ToNativeConverter[][] parameterConverters = new ToNativeConverter[methods.length][0];
        
        FunctionMapper functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();

        TypeMapper typeMapper = libraryOptions.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) libraryOptions.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;
        com.kenai.jffi.CallingConvention libraryCallingConvention = getCallingConvention(interfaceClass, libraryOptions);

        BufferMethodGenerator bufgen = new BufferMethodGenerator();
        StubCompiler compiler = StubCompiler.newCompiler();

        final MethodGenerator[] generators = {
                new X86MethodGenerator(compiler, bufgen),
                new FastIntMethodGenerator(bufgen),
                new FastLongMethodGenerator(bufgen),
                new FastNumericMethodGenerator(bufgen),
                bufgen
        };

        for (int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            final Class returnType = m.getReturnType();
            final Class[] parameterTypes = m.getParameterTypes();
            Class nativeReturnType = returnType;
            Class[] nativeParameterTypes = new Class[parameterTypes.length];
            final Annotation[] resultAnnotations = m.getAnnotations();
            final Annotation[][] parameterAnnotations = m.getParameterAnnotations();

            boolean conversionRequired = false;

            resultConverters[i] = getResultConverter(m, typeMapper);
            if (resultConverters[i] != null) {

                nativeReturnType = resultConverters[i].nativeType();
                conversionRequired = true;
            }

            parameterConverters[i] = new ToNativeConverter[parameterTypes.length];
            for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
                ToNativeConverter converter = getParameterConverter(m, pidx, typeMapper);
                if (converter != null) {
                    nativeParameterTypes[pidx] = converter.nativeType();
                    
                    parameterConverters[i][pidx] = new ParameterConverter(converter,
                            new MethodParameterContext(m, pidx));
                    conversionRequired = true;
                
                } else if (isLong32(parameterTypes[pidx], parameterAnnotations[pidx])) {
                    nativeParameterTypes[pidx] = parameterTypes[pidx] == long.class ? int.class : Integer.class;
                
                } else {
                    nativeParameterTypes[pidx] = parameterTypes[pidx];
                }
            }

            // Stash the name of the function in a static field
            String functionName = functionMapper.mapFunctionName(m.getName(), null);
            cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "name_" + i, ci(String.class), null, functionName);

            // Allow individual methods to set the calling convention to stdcall
            CallingConvention callingConvention = m.getAnnotation(StdCall.class) != null
                    ? CallingConvention.STDCALL : libraryCallingConvention;
            try {
                functions[i] = getFunction(library.findSymbolAddress(functionName),
                    nativeReturnType, resultAnnotations, nativeParameterTypes, parameterAnnotations, InvokerUtil.requiresErrno(m),
                    callingConvention);
            } catch (SymbolNotFoundError ex) {
                cv.visitField(ACC_PRIVATE | ACC_FINAL | ACC_STATIC, "error_" + i, ci(String.class), null, ex.getMessage());
                generateFunctionNotFound(cv, className, i, functionName, returnType, parameterTypes);
                continue;
            }

            String functionFieldName = "function_" + i;
            builder.addFunctionField(functions[i], functionFieldName);

            cv.visitField(ACC_PRIVATE | ACC_FINAL, functionFieldName, ci(Function.class), null, null);

            Signature signature = new Signature(nativeReturnType, nativeParameterTypes, resultAnnotations, parameterAnnotations,
                    callingConvention, !InvokerUtil.requiresErrno(m));
            String rawMethodName = m.getName() + (conversionRequired ? "$raw" + nextMethodID.incrementAndGet() : "");
            for (MethodGenerator g : generators) {
                if (g.isSupported(signature)) {
                    g.generate(builder, rawMethodName, functions[i], signature);
                    break;
                }
            }

            if (conversionRequired) {
                generateConversionMethod(builder, m.getName(), rawMethodName, i, returnType, parameterTypes, nativeReturnType,
                        nativeParameterTypes, resultConverters[i], parameterConverters[i]);
            }

            // The Function[] array is passed in as the second param, so generate
            // the constructor code to store each function in a field
            init.aload(0);
            init.aload(2);
            init.pushInt(i);
            init.aaload();
            init.putfield(className, functionFieldName, ci(Function.class));
        }

        FromNativeConverter[] fromNativeConverters = builder.getFromNativeConverterArray();
        for (int i = 0; i < fromNativeConverters.length; i++) {
            String fieldName = builder.getResultConverterName(fromNativeConverters[i]);
            cv.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(FromNativeConverter.class), null, null);
            init.aload(0);
            init.aload(3);
            init.pushInt(i);
            init.aaload();

            init.putfield(className, fieldName, ci(FromNativeConverter.class));
        }

        ToNativeConverter[] toNativeConverters = builder.getToNativeConverterArray();
        for (int i = 0; i < toNativeConverters.length; i++) {
            String fieldName = builder.getParameterConverterName(toNativeConverters[i]);
            cv.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(ToNativeConverter.class), null, null);
            init.aload(0);
            init.aload(4);
            init.pushInt(i);
            init.aaload();
            init.putfield(className, fieldName, ci(ToNativeConverter.class));
        }


        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        cv.visitEnd();

        try {
            byte[] bytes = cw.toByteArray();
            if (DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }

            Class implClass = new AsmClassLoader(interfaceClass.getClassLoader()).defineClass(className.replace("/", "."), bytes);
            Constructor<T> cons = implClass.getDeclaredConstructor(NativeLibrary.class, Function[].class,
                    FromNativeConverter[].class, ToNativeConverter[].class);
            T result = cons.newInstance(library, functions, fromNativeConverters, toNativeConverters);

            // Attach any native method stubs - we have to delay this until the
            // implementation class is loaded for it to work.
            compiler.attach(implClass);

            return result;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private final ToNativeConverter getParameterConverter(Method m, int parameterIndex, TypeMapper typeMapper) {
        Class parameterType = m.getParameterTypes()[parameterIndex];
        ToNativeConverter conv = typeMapper.getToNativeConverter(parameterType);
        if (conv != null) {
            return new ParameterConverter(conv, new MethodParameterContext(m, parameterIndex));

        } else if (Enum.class.isAssignableFrom(parameterType)) {
            return EnumMapper.getInstance(parameterType.asSubclass(Enum.class));

        } else if (isDelegate(parameterType)) {

            return closureManager.getClosureFactory(parameterType);
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

    private final void generateConversionMethod(AsmBuilder builder, String functionName, String rawFunctionName, int idx,
            Class returnType, Class[] parameterTypes, Class nativeReturnType, Class[] nativeParameterTypes,
            FromNativeConverter resultConverter, ToNativeConverter[] parameterConverters) {

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(builder.getClassVisitor().visitMethod(ACC_PUBLIC | ACC_FINAL,
                functionName,
                sig(returnType, parameterTypes), null, null));
        mv.start();

        // If there is a result converter, retrieve it and put on the stack
        if (!returnType.equals(nativeReturnType)) {
            mv.aload(0);
            mv.getfield(builder.getClassNamePath(), builder.getResultConverterName(resultConverter), ci(FromNativeConverter.class));
        }

        
        mv.aload(0);

        // Load and convert the parameters
        int lvar = 1;
        for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
            final boolean convertParameter = !parameterTypes[pidx].equals(nativeParameterTypes[pidx]);
            final boolean convertLong = long.class == parameterTypes[pidx] && int.class == nativeParameterTypes[pidx];

            if (convertParameter && !convertLong) {
                mv.aload(0);
                mv.getfield(builder.getClassNamePath(), builder.    getParameterConverterName(parameterConverters[pidx]), ci(ToNativeConverter.class));
            }

            lvar = loadParameter(mv, parameterTypes[pidx], lvar);

            if (convertParameter && convertLong) {
                mv.l2i();
            } else if (convertParameter) {
                if (parameterTypes[pidx].isPrimitive()) {
                    boxValue(mv, getBoxedClass(parameterTypes[pidx]), parameterTypes[pidx]);
                }
                mv.aconst_null();
                mv.invokeinterface(ToNativeConverter.class, "toNative",
                        Object.class, Object.class, ToNativeContext.class);
                mv.checkcast(p(nativeParameterTypes[pidx]));
            }
        }

        // Invoke the real native method
        mv.invokevirtual(builder.getClassNamePath(), rawFunctionName, sig(nativeReturnType, nativeParameterTypes));
        if (!returnType.equals(nativeReturnType)) {
            if (nativeReturnType.isPrimitive()) {
                boxValue(mv, getBoxedClass(nativeReturnType), nativeReturnType);
            }
            mv.aconst_null();
            mv.invokeinterface(FromNativeConverter.class, "fromNative",
                    Object.class, Object.class, FromNativeContext.class);
            mv.checkcast(p(returnType));
        }
        emitReturnOp(mv, returnType);
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    static final Label emitDirectCheck(SkinnyMethodAdapter mv, Class[] parameterTypes) {

        // Iterate through any parameters that might require a HeapInvocationBuffer
        Label bufferInvocationLabel = new Label();
        boolean needBufferInvocation = false;
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Pointer.class);
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;
            
            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Struct.class);
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else if (Buffer.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);


                if (Platform.getPlatform().getJavaMajorVersion() < 6 && Buffer.class == parameterTypes[i]) {
                    // Buffer#isDirect() is only available in java 6 or later, so need to use a slower test
                    // that checks / casts to the appropriate buffer subclass
                    mv.invokestatic(AsmRuntime.class, "isDirect5", boolean.class, Buffer.class);

                } else {
                    mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, parameterTypes[i]);
                }
                mv.iffalse(bufferInvocationLabel);
                needBufferInvocation = true;

            } else {
                lvar += calculateLocalVariableSpace(parameterTypes[i]);
            }
        }

        return needBufferInvocation ? bufferInvocationLabel : null;
    }

    static final void emitReturn(SkinnyMethodAdapter mv, Class returnType, Class nativeIntType) {
        if (returnType.isPrimitive()) {
            if (long.class == returnType) {
                widen(mv, nativeIntType, returnType);
                mv.lreturn();
            
            } else if (float.class == returnType) {
                mv.freturn();
            
            } else if (double.class == returnType) {
                mv.dreturn();
            
            } else if (void.class == returnType) {
                mv.voidreturn();
            
            } else {
                narrow(mv, nativeIntType, returnType);
                mv.ireturn();
            }

        } else {
            boxValue(mv, returnType, nativeIntType);
            mv.areturn();
        }
    }

    static int loadParameter(SkinnyMethodAdapter mv, Class parameterType, int lvar) {
        if (!parameterType.isPrimitive()) {
            mv.aload(lvar++);
        
        } else if (long.class == parameterType) {
            mv.lload(lvar);
            lvar += 2;
        
        } else if (float.class == parameterType) {
            mv.fload(lvar++);
        
        } else if (double.class == parameterType) {
            mv.dload(lvar);
            lvar += 2;
        
        } else {
            mv.iload(lvar++);
        }
        
        return lvar;
    }

    private static final Function getFunction(long address, Class resultType, final Annotation[] resultAnnotations,
            Class[] parameterTypes, final Annotation[][] parameterAnnotations,
            boolean requiresErrno, CallingConvention convention) {
        
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[parameterTypes.length];
        
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = InvokerUtil.getNativeParameterType(parameterTypes[i], parameterAnnotations[i]);
        }

        return new Function(address, InvokerUtil.getNativeReturnType(resultType, resultAnnotations),
                nativeParamTypes, convention, requiresErrno);

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
