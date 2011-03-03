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


package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.annotations.LongLong;
import java.io.PrintWriter;
import com.kenai.jaffl.Address;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.provider.ParameterFlags;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.annotations.StdCall;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.FunctionMapper;
import com.kenai.jaffl.mapper.MethodParameterContext;
import com.kenai.jaffl.mapper.MethodResultContext;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;
import com.kenai.jaffl.provider.InvocationSession;
import com.kenai.jaffl.provider.LoadedLibrary;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.util.EnumMapper;
import com.kenai.jffi.ArrayFlags;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.InvocationBuffer;
import com.kenai.jffi.Platform;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import static com.kenai.jaffl.provider.jffi.CodegenUtils.*;
import static com.kenai.jaffl.provider.jffi.NumberUtil.*;
import static com.kenai.jaffl.provider.jffi.AsmUtil.*;
import static com.kenai.jaffl.provider.jffi.InvokerUtil.*;
import static org.objectweb.asm.Opcodes.*;

public class AsmLibraryLoader extends LibraryLoader {
    public final static boolean DEBUG = false || Boolean.getBoolean("jaffl.compile.dump");
    private static final class SingletonHolder {
        static final LibraryLoader INSTANCE = new AsmLibraryLoader();
    }
    private static final boolean FAST_NUMERIC_AVAILABLE = isFastNumericAvailable();
    private static final boolean FAST_LONG_AVAILABLE = isFastLongAvailable();
    private final AtomicLong nextClassID = new AtomicLong(0);
    private final AtomicLong nextIvarID = new AtomicLong(0);
    private final AtomicLong nextMethodID = new AtomicLong(0);

    static final LibraryLoader getInstance() {
        return SingletonHolder.INSTANCE;
    }

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

    private boolean isReturnTypeSupported(Class type) {
        return type.isPrimitive() || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
                || Enum.class.isAssignableFrom(type)
                || Pointer.class == type || Address.class == type
                || String.class == type
                || Struct.class.isAssignableFrom(type);
        
    }

    private boolean isParameterTypeSupported(Class type) {
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
                || StringBuffer.class.isAssignableFrom(type);
    }

    @Override
    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return generateInterfaceImpl(library, interfaceClass, libraryOptions);
    }

    private final <T> T generateInterfaceImpl(final NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        String className = p(interfaceClass) + "$jaffl$" + nextClassID.getAndIncrement();

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, p(AbstractNativeInterface.class),
                new String[] { p(interfaceClass) });

        // Create the constructor to set the 'library' & functions fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC, "<init>",
                sig(void.class, NativeLibrary.class, Function[].class, ResultConverter[].class, ParameterConverter[][].class),
                null, null));
        init.start();
        // Invokes the super class constructor as super(Library)

        init.aload(0);
        init.aload(1);

        init.invokespecial(p(AbstractNativeInterface.class), "<init>", sig(void.class, NativeLibrary.class));
        
        final Method[] methods = interfaceClass.getMethods();
        Function[] functions = new Function[methods.length];
        ResultConverter[] resultConverters = new ResultConverter[methods.length];
        ParameterConverter[][] parameterConverters = new ParameterConverter[methods.length][0];
        
        FunctionMapper functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.getInstance();

        TypeMapper typeMapper = libraryOptions.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) libraryOptions.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;
        com.kenai.jffi.CallingConvention libraryCallingConvention = getCallingConvention(interfaceClass, libraryOptions);

        StubCompiler compiler = StubCompiler.newCompiler();

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
                cv.visitField(ACC_PRIVATE | ACC_FINAL, getResultConverterFieldName(i), ci(ResultConverter.class), null, null);
                nativeReturnType = resultConverters[i].nativeType();
                conversionRequired = true;
            }

            parameterConverters[i] = new ParameterConverter[parameterTypes.length];
            for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
                ToNativeConverter converter = Enum.class.isAssignableFrom(parameterTypes[pidx])
                        ? EnumMapper.getInstance(parameterTypes[pidx].asSubclass(Enum.class))
                        : typeMapper.getToNativeConverter(parameterTypes[pidx]);
                if (converter != null) {
                    cv.visitField(ACC_PRIVATE | ACC_FINAL, getParameterConverterFieldName(i, pidx),
                            ci(ParameterConverter.class), null, null);
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

            cv.visitField(ACC_PRIVATE | ACC_FINAL, functionFieldName, ci(Function.class), null, null);
            final boolean ignoreErrno = !InvokerUtil.requiresErrno(m);

            if (canCompile(compiler, nativeReturnType, nativeParameterTypes, callingConvention)) {
                // The method can be generated via a direct java -> jni mapping
                compile(compiler, functions[i], cv, className, m.getName() + (conversionRequired ? "$raw" : ""),
                        functionFieldName, nativeReturnType, m.getAnnotations(), 
                        nativeParameterTypes, m.getParameterAnnotations(), 
                        callingConvention, ignoreErrno);
            
            } else {
                generateMethod(cv, className, m.getName() + (conversionRequired ? "$raw" : ""),
                        functionFieldName, nativeReturnType, m.getAnnotations(), nativeParameterTypes,
                        m.getParameterAnnotations(), callingConvention, ignoreErrno);
            }

            if (conversionRequired) {
                generateConversionMethod(cv, className, m.getName(), i, returnType, parameterTypes, nativeReturnType, nativeParameterTypes);
            }

            // The Function[] array is passed in as the second param, so generate
            // the constructor code to store each function in a field
            init.aload(0);
            init.aload(2);
            init.pushInt(i);
            init.aaload();
            init.putfield(className, functionFieldName, ci(Function.class));

            // If there is a result converter for this function, put it in a field too
            if (resultConverters[i] != null) {
                
                init.aload(0);
                init.aload(3);
                init.pushInt(i);
                init.aaload();
                init.putfield(className, getResultConverterFieldName(i), ci(ResultConverter.class));
            }

            for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
                if (parameterConverters[i][pidx] != null) {
                    init.aload(0);
                    init.aload(4);
                    init.pushInt(i);
                    init.aaload();
                    init.pushInt(pidx);
                    init.aaload();
                    init.putfield(className, getParameterConverterFieldName(i, pidx), ci(ParameterConverter.class));
                }
            }
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
            Constructor<T> cons = implClass.getDeclaredConstructor(NativeLibrary.class, Function[].class, ResultConverter[].class, ParameterConverter[][].class);
            T result = cons.newInstance(library, functions, resultConverters, parameterConverters);

            // Attach any native method stubs - we have to delay this until the
            // implementation class is loaded for it to work.
            compiler.attach(implClass);

            return result;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private final ResultConverter getResultConverter(Method m, TypeMapper typeMapper) {
        Class returnType = m.getReturnType();
        FromNativeConverter conv = typeMapper.getFromNativeConverter(returnType);
        if (conv != null) {
            return new ResultConverter(conv, new MethodResultContext(m));

        } else if (Enum.class.isAssignableFrom(returnType)) {
            return new ResultConverter(EnumMapper.getInstance(returnType.asSubclass(Enum.class)), null);

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
    
    private final String getFunctionFieldName(int idx) {
        return "function_" + idx;
    }

    private final String getResultConverterFieldName(int idx) {
        return "resultConverter_" + idx;
    }

    private final String getParameterConverterFieldName(int idx, int paramIndex) {
        return "parameterConverter_" + idx + "_" + paramIndex;
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

    private final void generateConversionMethod(ClassVisitor cv, String className, String functionName, int idx,
            Class returnType, Class[] parameterTypes, Class nativeReturnType, Class[] nativeParameterTypes) {

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, functionName,
                sig(returnType, parameterTypes), null, null));
        mv.start();

        // If there is a result converter, retrieve it and put on the stack
        if (!returnType.equals(nativeReturnType)) {
            mv.aload(0);
            mv.getfield(className, getResultConverterFieldName(idx), ci(ResultConverter.class));
        }

        
        mv.aload(0);

        // Load and convert the parameters
        int lvar = 1;
        for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
            final boolean convertParameter = !parameterTypes[pidx].equals(nativeParameterTypes[pidx]);
            if (convertParameter) {
                mv.aload(0);
                mv.getfield(className, getParameterConverterFieldName(idx, pidx), ci(ParameterConverter.class));
            }

            lvar = loadParameter(mv, parameterTypes[pidx], lvar);
            
            if (convertParameter) {
                if (parameterTypes[pidx].isPrimitive()) {
                    boxPrimitive(mv, parameterTypes[pidx]);
                }

                mv.invokevirtual(ParameterConverter.class, "toNative",
                        Object.class, Object.class);
                mv.checkcast(p(nativeParameterTypes[pidx]));
            }
        }

        // Invoke the real native method
        mv.invokevirtual(className, functionName + "$raw", sig(nativeReturnType, nativeParameterTypes));
        if (!returnType.equals(nativeReturnType)) {
            if (nativeReturnType.isPrimitive()) {
                boxPrimitive(mv, nativeReturnType);
            }

            mv.invokevirtual(ResultConverter.class, "fromNative",
                    Object.class, Object.class);
            mv.checkcast(p(returnType));
        }
        emitReturnOp(mv, returnType);
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    private final boolean canCompile(StubCompiler compiler, Class returnType, Class[] parameterTypes,
            CallingConvention convention) {

        Class[] nativeParameterTypes = new Class[parameterTypes.length];
        for (int i = 0; i < nativeParameterTypes.length; ++i) {
            nativeParameterTypes[i] = AsmUtil.unboxedType(parameterTypes[i]);
        }

        return compiler.canCompile(AsmUtil.unboxedReturnType(returnType), nativeParameterTypes, convention);
    }

    private final void compile(StubCompiler compiler, Function function,
            ClassVisitor cv, String className, String functionName, String functionFieldName,
            Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
            CallingConvention convention, boolean ignoreErrno) {

        Class[] nativeParameterTypes = new Class[parameterTypes.length];
        boolean unboxing = false;
        boolean ptrCheck = false;
        for (int i = 0; i < nativeParameterTypes.length; ++i) {
            nativeParameterTypes[i] = AsmUtil.unboxedType(parameterTypes[i]);
            unboxing |= nativeParameterTypes[i] != parameterTypes[i];
            ptrCheck |= Pointer.class.isAssignableFrom(parameterTypes[i])
                    || Struct.class.isAssignableFrom(parameterTypes[i]);
        }
        
        Class nativeReturnType = AsmUtil.unboxedReturnType(returnType);
        unboxing |= nativeReturnType != returnType;

        String stubName = functionName + (unboxing || ptrCheck ? "$jni$" + nextMethodID.getAndIncrement() : "");
        
        // If unboxing of parameters is required, generate a wrapper
        if (unboxing || ptrCheck) {
            SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL,
                    functionName, sig(returnType, parameterTypes), null, null));
            mv.start();
            mv.aload(0);

            Label bufferInvocationLabel = emitDirectCheck(mv, parameterTypes);
            
            // Emit the unboxing wrapper
            for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
                lvar = loadParameter(mv, parameterTypes[i], lvar);
                if (parameterTypes[i] != nativeParameterTypes[i]) {

                    if (Number.class.isAssignableFrom(parameterTypes[i])) {
                        unboxNumber(mv, parameterTypes[i], nativeParameterTypes[i]);

                    } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                        unboxPointer(mv, nativeParameterTypes[i]);
                        
                    } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                        unboxStruct(mv, nativeParameterTypes[i]);
                        
                    } else if (Enum.class.isAssignableFrom(parameterTypes[i])) {
                        unboxEnum(mv, nativeParameterTypes[i]);
                    }
                }
            }

            // invoke the compiled stub
            mv.invokevirtual(className, stubName, sig(nativeReturnType, nativeParameterTypes));

            // emitReturn will box the return value if needed
            emitReturn(mv, returnType, nativeReturnType);

            String bufInvoke = null;
            if (bufferInvocationLabel != null) {
                // If there was a non-direct pointer in the parameters, need to
                // handle it via a call to the slow buffer invocation
                mv.label(bufferInvocationLabel);

                bufInvoke = functionName + "$buf$" + nextMethodID.getAndIncrement();
                // reload all the parameters
                for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
                    lvar = loadParameter(mv, parameterTypes[i], lvar);
                }
                mv.invokevirtual(className, bufInvoke, sig(returnType, parameterTypes));
                emitReturnOp(mv, returnType);
            }
            mv.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
            mv.visitEnd();

            if (bufInvoke != null) {
                SkinnyMethodAdapter bi = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL,
                        bufInvoke, sig(returnType, parameterTypes), null, null));
                bi.start();

                // Retrieve the static 'ffi' Invoker instance
                bi.getstatic(p(AbstractNativeInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

                // retrieve this.function
                bi.aload(0);
                bi.getfield(className, functionFieldName, ci(Function.class));

                generateBufferInvocation(bi, returnType, resultAnnotations, parameterTypes, parameterAnnotations);
                bi.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
                bi.visitEnd();
            }
        }

        cv.visitMethod(ACC_PUBLIC | ACC_FINAL | ACC_NATIVE,
                stubName, sig(nativeReturnType, nativeParameterTypes), null, null);
        

        compiler.compile(function, stubName, nativeReturnType, nativeParameterTypes,
                convention, !ignoreErrno);
    }

    private final void generateMethod(ClassVisitor cv, String className, String functionName,
            String functionFieldName,
            Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
            CallingConvention convention, boolean ignoreErrno) {

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, functionName, 
                sig(returnType, parameterTypes), null, null));
        mv.start();
        
        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractNativeInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve this.function
        mv.aload(0);
        mv.getfield(className, functionFieldName, ci(Function.class));

        if (convention == CallingConvention.DEFAULT && FAST_NUMERIC_AVAILABLE && 
                isFastNumericMethod(returnType, resultAnnotations, parameterTypes, parameterAnnotations)) {
            generateFastNumericInvocation(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations, ignoreErrno);
        } else {
            generateBufferInvocation(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations);
        }
        
        mv.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
        mv.visitEnd();
    }

    private final void generateBufferInvocation(SkinnyMethodAdapter mv, 
            Class returnType, Annotation[] resultAnnotations, 
            Class[] parameterTypes, Annotation[][] parameterAnnotations) {
        // [ stack contains: Invoker, Function ]
        final boolean sessionRequired = isSessionRequired(parameterTypes, parameterAnnotations);
        final int lvarSession = sessionRequired ? calculateLocalVariableSpace(parameterTypes) + 1 : -1;
        if (sessionRequired) {
            mv.newobj(p(InvocationSession.class));
            mv.dup();
            mv.invokespecial(InvocationSession.class, "<init>", void.class);
            mv.astore(lvarSession);
        }

        // [ stack contains: Invoker, Function, Function ]
        mv.dup();
        mv.invokestatic(AsmRuntime.class, "newHeapInvocationBuffer", HeapInvocationBuffer.class, Function.class);
        // [ stack contains: Invoker, Function, HeapInvocationBuffer ]
        
        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.dup(); // dup ref to HeapInvocationBuffer

            if (isSessionRequired(parameterTypes[i], parameterAnnotations[i])) {
                mv.aload(lvarSession);
            }

            lvar = loadParameter(mv, parameterTypes[i], lvar);
            
            final int parameterFlags = DefaultInvokerFactory.getParameterFlags(parameterAnnotations[i]);
            final int nativeArrayFlags = DefaultInvokerFactory.getNativeArrayFlags(parameterFlags)
                        | ((parameterFlags & ParameterFlags.IN) != 0 ? ArrayFlags.NULTERMINATE : 0);

            if (parameterTypes[i].isArray() && parameterTypes[i].getComponentType().isPrimitive()) {
                mv.pushInt(nativeArrayFlags);
                
                if (isLong32(parameterTypes[i].getComponentType(), parameterAnnotations[i])) {
                    mv.invokestatic(p(AsmRuntime.class), "marshal32",
                        sig(void.class, ci(InvocationBuffer.class) + ci(InvocationSession.class), parameterTypes[i], int.class));
                } else {
                    marshal(mv, parameterTypes[i], int.class);
                }

            } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Pointer.class, int.class);

            } else if (Address.class.isAssignableFrom(parameterTypes[i])) {
                marshal(mv, Address.class);

            } else if (Enum.class.isAssignableFrom(parameterTypes[i])) {
                marshal(mv, Enum.class);

            } else if (Buffer.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, parameterTypes[i], int.class);

            } else if (ByReference.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(nativeArrayFlags);
                // stack should be: [ session, buffer, ref, flags ]
                sessionmarshal(mv, ByReference.class, int.class);

            } else if (StringBuilder.class.isAssignableFrom(parameterTypes[i]) || StringBuffer.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                // stack should be: [ session, buffer, ref, flags ]
                sessionmarshal(mv, parameterTypes[i], int.class, int.class);

            } else if (CharSequence.class.isAssignableFrom(parameterTypes[i])) {
                // stack should be: [ Buffer, array, flags ]
                marshal(mv, CharSequence.class);

            } else if (parameterTypes[i].isArray() && CharSequence.class.isAssignableFrom(parameterTypes[i].getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, CharSequence[].class, int.class, int.class);

            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Struct.class, int.class, int.class);

            } else if (parameterTypes[i].isArray() && Struct.class.isAssignableFrom(parameterTypes[i].getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Struct[].class, int.class, int.class);
            
            } else if (parameterTypes[i].isArray() && Pointer.class.isAssignableFrom(parameterTypes[i].getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, Pointer[].class, int.class, int.class);

            } else if (parameterTypes[i].isPrimitive() || Number.class.isAssignableFrom(parameterTypes[i])) {
                emitInvocationBufferIntParameter(mv, parameterTypes[i], parameterAnnotations[i]);

            } else {
                throw new IllegalArgumentException("unsupported parameter type " + parameterTypes[i]);
            }
        }

        String invokeMethod = null;
        Class nativeReturnType = null;
        
        if (isPrimitiveInt(returnType) || void.class == returnType
                || Byte.class == returnType || Short.class == returnType || Integer.class == returnType) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;

        } else if (isLong32(returnType, resultAnnotations)) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;
            
        } else if (Long.class == returnType || long.class == returnType) {
            invokeMethod = "invokeLong";
            nativeReturnType = long.class;

        } else if (NativeLong.class == returnType) {
            invokeMethod = Platform.getPlatform().longSize() == 32 ? "invokeInt" : "invokeLong";
            nativeReturnType = Platform.getPlatform().longSize() == 32 ? int.class : long.class;

        } else if (Pointer.class == returnType || Address.class == returnType
            || Struct.class.isAssignableFrom(returnType) || String.class.isAssignableFrom(returnType)) {
            invokeMethod = Platform.getPlatform().addressSize() == 32 ? "invokeInt" : "invokeLong";
            nativeReturnType = Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else if (Float.class == returnType || float.class == returnType) {
            invokeMethod = "invokeFloat";
            nativeReturnType = float.class;

        } else if (Double.class == returnType || double.class == returnType) {
            invokeMethod = "invokeDouble";
            nativeReturnType = double.class;

        } else {
            throw new IllegalArgumentException("unsupported return type " + returnType);
        }
        
        mv.invokevirtual(com.kenai.jffi.Invoker.class, invokeMethod,
                nativeReturnType, Function.class, HeapInvocationBuffer.class);

        if (sessionRequired) {
            mv.aload(lvarSession);
            mv.invokevirtual(p(InvocationSession.class), "finish", "()V");
        }

        emitReturn(mv, returnType, nativeReturnType);
    }

    private final void generateFastNumericInvocation(SkinnyMethodAdapter mv, Class returnType, 
            Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreErrno) {
        // [ stack contains: Invoker, Function ]

        Label bufferInvocationLabel = emitDirectCheck(mv, parameterTypes);
        Class nativeIntType = getMinimumIntType(returnType, resultAnnotations, parameterTypes, parameterAnnotations);

        // Emit fast-numeric invocation
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            lvar = loadParameter(mv, parameterTypes[i], lvar);

            if (parameterTypes[i].isPrimitive()) {
                // widen to long if needed
                widen(mv, parameterTypes[i], nativeIntType);

            } else if (Number.class.isAssignableFrom(parameterTypes[i])) {
                unboxNumber(mv, parameterTypes[i], nativeIntType);

            } else if (Enum.class.isAssignableFrom(parameterTypes[i])) {
                unboxEnum(mv, nativeIntType);

            } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                unboxPointer(mv, nativeIntType);

            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                unboxStruct(mv, nativeIntType);

            }
            
            if (Float.class == parameterTypes[i] || float.class == parameterTypes[i]) {
                mv.invokestatic(Float.class, "floatToRawIntBits", int.class, float.class);
                mv.i2l();

            } else if (Double.class == parameterTypes[i] || double.class == parameterTypes[i]) {
                mv.invokestatic(Double.class, "doubleToRawLongBits", long.class, double.class);

            }
        }

        // stack now contains [ IntInvoker, Function, int args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class), 
                getFastNumericInvokerMethodName(returnType, resultAnnotations, 
                parameterTypes, parameterAnnotations, ignoreErrno),
                getFastNumericInvokerSignature(parameterTypes.length, nativeIntType));

        // Convert the result from long to the correct return type
        if (Float.class == returnType || float.class == returnType) {
            mv.l2i();
            mv.invokestatic(Float.class, "intBitsToFloat", float.class, int.class);

        } else if (Double.class == returnType || double.class == returnType) {
            mv.invokestatic(Double.class, "longBitsToDouble", double.class, long.class);
        }

        emitReturn(mv, returnType, nativeIntType);

        if (bufferInvocationLabel != null) {
            // Now emit the alternate path for any parameters that might require it
            mv.label(bufferInvocationLabel);
            generateBufferInvocation(mv, returnType, resultAnnotations, parameterTypes, parameterAnnotations);
        }
    }

    private static final Label emitDirectCheck(SkinnyMethodAdapter mv, Class[] parameterTypes) {

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
            
            } else {
                lvar += calculateLocalVariableSpace(parameterTypes[i]);
            }
        }

        return needBufferInvocation ? bufferInvocationLabel : null;
    }

    private final void emitReturn(SkinnyMethodAdapter mv, Class returnType, Class nativeIntType) {
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

    private final int loadParameter(SkinnyMethodAdapter mv, Class parameterType, int lvar) {
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

    private static final Class<? extends Number> getMinimumIntType(
            Class resultType, Annotation[] resultAnnotations,
            Class[] parameterTypes, Annotation[][] parameterAnnotations) {

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!isInt32Param(parameterTypes[i], parameterAnnotations[i])) {
                return long.class;
            }
        }

        return !isInt32Result(resultType, resultAnnotations) ? long.class : int.class;
    }
    
    static final String getFastNumericInvokerMethodName(Class returnType, 
            Annotation[] resultAnnotations, Class[] parameterTypes, Annotation[][] parameterAnnotations,
            boolean ignoreErrno) {
        
        StringBuilder sb = new StringBuilder("invoke");

        boolean p32 = false, p64 = false, n = false;
        for (int i = 0; i < parameterTypes.length; ++i) {
            final Class t = parameterTypes[i];
            if (isInt32Param(t, parameterAnnotations[i])) {
                p32 = true;

            } else if (long.class == t || Long.class == t || NativeLong.class == t
                    || (Platform.getPlatform().addressSize() == 64 && isPointerParam(t))) {
                p64 = true;

            } else {
                n = true;
                break;
            }
        }

        n |= !isFastIntegerResult(returnType, resultAnnotations);

        final boolean r32 = isInt32Result(returnType, resultAnnotations);
        char numericType;

        if (r32 && !p64 && !n) {
            // all 32 bit integer params with a 32bit integer result - use the int path
            numericType = 'I';

        } else if (!r32 && !n && (!p32 || Platform.getPlatform().addressSize() == 64)) {
            // A call that is 64bit result with all 64bit params, or it is a 64bit
            // machine where 32bit params will promote, then use the fast-long invoker
            numericType = 'L';

        } else {
            // Default to the numeric path, which is a bit slower, but can handle anything
            numericType = 'N';
        }

        if (ignoreErrno && numericType == 'I') {
            sb.append("NoErrno");
        }
        
        if (parameterTypes.length < 1) {
            sb.append("V");
        } else for (int i = 0; i < parameterTypes.length; ++i) {
            sb.append(numericType);
        }

        return sb.append("r").append(numericType).toString();
    }

    static final String getFastNumericInvokerSignature(int parameterCount, Class nativeIntType) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(ci(Function.class));
        final char iType = nativeIntType == int.class ? 'I' : 'J';
        for (int i = 0; i < parameterCount; ++i) {
            sb.append(iType);
        }
        sb.append(")").append(iType);

        return sb.toString();
    }


    
    private final void boxStruct(SkinnyMethodAdapter mv, Class returnType, Class nativeType) {
        Label nonnull = new Label();
        Label end = new Label();

        if (long.class == nativeType) {
            mv.dup2();
            mv.lconst_0();
            mv.lcmp();
            mv.ifne(nonnull);
            mv.pop2();

        } else {
            mv.dup();
            mv.ifne(nonnull);
            mv.pop();
        }
        
        mv.aconst_null();
        mv.go_to(end);

        mv.label(nonnull);
        
        // Create an instance of the struct subclass
        mv.newobj(p(returnType));
        mv.dup();
        mv.invokespecial(returnType, "<init>", void.class);
        if (long.class == nativeType) {
            mv.dup_x2();
        } else {
            mv.dup_x1();
        }

        // associate the memory with the struct and return the struct
        mv.invokestatic(AsmRuntime.class, "useMemory", void.class, nativeType, Struct.class);
        mv.label(end);
    }

    private final void boxPrimitive(SkinnyMethodAdapter mv, Class primitiveType) {
        Class objClass = getBoxedClass(primitiveType);
        mv.invokestatic(objClass, "valueOf", objClass, primitiveType);
    }

    private final void boxNumber(SkinnyMethodAdapter mv, Class type, Class nativeType) {
        Class primitiveClass = getPrimitiveClass(type);

        if (Byte.class.isAssignableFrom(type)) {
            narrow(mv, nativeType, byte.class);

        } else if (Character.class.isAssignableFrom(type)) {
            narrow(mv, nativeType, char.class);

        } else if (Short.class.isAssignableFrom(type)) {
            narrow(mv, nativeType, short.class);

        } else if (Integer.class.isAssignableFrom(type)) {
            narrow(mv, nativeType, int.class);

        } else if (Long.class.isAssignableFrom(type)) {
            widen(mv, nativeType, long.class);

        } else if (NativeLong.class.isAssignableFrom(type)) {
            if (Platform.getPlatform().longSize() == 64) {
                widen(mv, nativeType, long.class);
                primitiveClass = long.class;
            } else {
                primitiveClass = int.class;
            }

        } else if (Boolean.class.isAssignableFrom(type)) {
            narrow(mv, nativeType, boolean.class);

        } else if (Float.class == type || Double.class == type) {
            // nothing to do
        } else {
            throw new IllegalArgumentException("invalid Number subclass");
        }
        
        mv.invokestatic(type, "valueOf", type, primitiveClass);
    }

    private final void boxValue(SkinnyMethodAdapter mv, Class returnType, Class nativeReturnType) {
        if (returnType == nativeReturnType) {
            return;

        } else if (Boolean.class.isAssignableFrom(returnType)) {
            narrow(mv, nativeReturnType, boolean.class);
            mv.invokestatic(Boolean.class, "valueOf", Boolean.class, boolean.class);
            
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            mv.invokestatic(AsmRuntime.class, "pointerValue", Pointer.class, nativeReturnType);

        } else if (Address.class == returnType) {
            mv.invokestatic(returnType, "valueOf", returnType, nativeReturnType);

        } else if (Struct.class.isAssignableFrom(returnType)) {
            boxStruct(mv, returnType, nativeReturnType);
         
        } else if (Number.class.isAssignableFrom(returnType)) {
            boxNumber(mv, returnType, nativeReturnType);
         
        } else if (String.class == returnType) {
            mv.invokestatic(AsmRuntime.class, "stringValue", String.class, nativeReturnType);
         
        } else {
            throw new IllegalArgumentException("cannot box value of type " + nativeReturnType + " to " + returnType);
        }
    }

    private final void emitInvocationBufferIntParameter(final SkinnyMethodAdapter mv, 
            final Class parameterType, final Annotation[] parameterAnnotations) {
        String paramMethod = null;
        Class paramClass = int.class;
        
        if (!parameterType.isPrimitive()) {
            unboxNumber(mv, parameterType, null);
        }

        if (byte.class == parameterType || Byte.class == parameterType) {
            paramMethod = "putByte";
        } else if (short.class == parameterType || Short.class == parameterType) {
            paramMethod = "putShort";
        } else if (int.class == parameterType || Integer.class == parameterType || boolean.class == parameterType) {
            paramMethod = "putInt";
        
        } else if (isLong32(parameterType, parameterAnnotations)) {
            paramMethod = "putInt";
            paramClass = int.class;
            narrow(mv, parameterType, paramClass);
            
        } else if (long.class == parameterType || Long.class == parameterType) {
            paramMethod = "putLong";
            paramClass = long.class;
        
        } else if (float.class == parameterType || Float.class == parameterType) {
            paramMethod = "putFloat";
            paramClass = float.class;
        
        } else if (double.class == parameterType || Double.class == parameterType) {
            paramMethod = "putDouble";
            paramClass = double.class;
        
        } else if (NativeLong.class.isAssignableFrom(parameterType) && Platform.getPlatform().longSize() == 32) {
            paramMethod = "putInt";
            paramClass = int.class;
        } else if (NativeLong.class.isAssignableFrom(parameterType) && Platform.getPlatform().longSize() == 64) {
            paramMethod = "putLong";
            paramClass = long.class;
        } else {
            throw new IllegalArgumentException("unsupported parameter type " + parameterType);
        }
        
        mv.invokevirtual(HeapInvocationBuffer.class, paramMethod, void.class, paramClass);
    }

    private final void marshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(AsmRuntime.class), "marshal", sig(void.class, ci(InvocationBuffer.class), parameterTypes));
    }

    private final void sessionmarshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(AsmRuntime.class), "marshal",
                sig(void.class, ci(InvocationBuffer.class) + ci(InvocationSession.class), parameterTypes));
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

    private static boolean isSessionRequired(Class parameterType, Annotation[] annotations) {
        return StringBuilder.class.isAssignableFrom(parameterType)
                || StringBuffer.class.isAssignableFrom(parameterType)
                || ByReference.class.isAssignableFrom(parameterType)
                || (parameterType.isArray() && Pointer.class.isAssignableFrom(parameterType.getComponentType()))
                || (parameterType.isArray() && CharSequence.class.isAssignableFrom(parameterType.getComponentType()))
                || (parameterType.isArray() && NativeLong.class.isAssignableFrom(parameterType.getComponentType()))
                || (parameterType.isArray() && isLong32(parameterType.getComponentType(), annotations))
                ;
    }

    private static boolean isSessionRequired(Class[] parameterTypes, Annotation[][] annotations) {
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (isSessionRequired(parameterTypes[i], annotations[i])) {
                return true;
            }
        }

        return false;
    }    

    final static boolean isFastNumericMethod(Class returnType, Annotation[] resultAnnotations, 
            Class[] parameterTypes, Annotation[][] parameterAnnotations) {
        
        if (!FAST_NUMERIC_AVAILABLE || parameterTypes.length > 6) {
            return false;
        }

        if (!isFastNumericResult(returnType, resultAnnotations)) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!isFastNumericParam(parameterTypes[i], parameterAnnotations[i])) {
                return false;
            }
        }

        return com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.I386
                || com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.X86_64;
    }
    
    static boolean isLong32(Class type, Annotation[] annotations) {
        return (long.class == type || Long.class.isAssignableFrom(type))
                && Platform.getPlatform().longSize() == 32
                && !InvokerUtil.hasAnnotation(annotations, LongLong.class);
    }

    final static boolean isInt32(Class type, Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || isLong32(type, annotations)
                || (NativeLong.class.isAssignableFrom(type) && Platform.getPlatform().longSize() == 32)
                || Enum.class.isAssignableFrom(type)
                ;
    }

    final static boolean isInt32Result(Class type, Annotation[] annotations) {
        return isInt32(type, annotations) 
            || Void.class.isAssignableFrom(type) || void.class == type
            || (isPointerResult(type) && Platform.getPlatform().addressSize() == 32);
    }

    final static boolean isInt32Param(Class type, Annotation[] annotations) {
        return isInt32(type, annotations)
                || (isPointerParam(type) && Platform.getPlatform().addressSize() == 32)
                ;
    }

    final static boolean isPointerResult(Class type) {
        return Pointer.class.isAssignableFrom(type)
                || Struct.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type);
    }
    
    final static boolean isPointerParam(Class type) {
        return Pointer.class.isAssignableFrom(type)
                || Struct.class.isAssignableFrom(type);
    }

    private final static boolean isFastIntegerResult(Class type, Annotation[] annotations) {
        if (isInt32Result(type, annotations)) {
            return true;
        }

        final boolean isPointer = isPointerResult(type);
        if (isPointer && Platform.getPlatform().addressSize() == 32) {
            return true;
        }

        if (NativeLong.class.isAssignableFrom(type) && Platform.getPlatform().longSize() == 32) {
            return true;
        }

        // For x86_64, any args that promote up to 64bit can be accepted.
        final boolean isLong = Long.class == type || long.class == type;
        return Platform.getPlatform().addressSize() == 64 && FAST_LONG_AVAILABLE &&
                (isPointer || NativeLong.class.isAssignableFrom(type) || isLong);
    }

    private final static boolean isFastIntegerParam(Class type, Annotation[] annotations) {
        if (isInt32Param(type, annotations)) {
            return true;
        }

        final boolean isPointer = isPointerParam(type);
        if (isPointer && Platform.getPlatform().addressSize() == 32) {
            return true;
        }

        if (NativeLong.class.isAssignableFrom(type) && Platform.getPlatform().longSize() == 32) {
            return true;
        }

        if (Enum.class.isAssignableFrom(type)) {
            return true;
        }

        // For x86_64, any args that promote up to 64bit can be accepted.
        final boolean isLong = Long.class == type || long.class == type;
        return Platform.getPlatform().addressSize() == 64 && FAST_LONG_AVAILABLE &&
                (isPointer || NativeLong.class.isAssignableFrom(type) || isLong);
    }

    final static boolean isFastNumericResult(Class type, Annotation[] annotations) {
        return isFastIntegerResult(type, annotations)
                || Long.class.isAssignableFrom(type) || long.class == type
                || NativeLong.class.isAssignableFrom(type)
                || isPointerResult(type)
                || float.class == type || Float.class == type
                || double.class == type || Double.class == type;
    }

    final static boolean isFastNumericParam(Class type, Annotation[] annotations) {
        return isFastIntegerParam(type, annotations)
                || Long.class.isAssignableFrom(type) || long.class == type
                || NativeLong.class.isAssignableFrom(type)
                || Pointer.class.isAssignableFrom(type)
                || Struct.class.isAssignableFrom(type)
                || float.class == type || Float.class == type
                || double.class == type || Double.class == type;
    }

    final static boolean isFastNumericAvailable() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeNNNNNNrN", Function.class, long.class, long.class, long.class, long.class, long.class, long.class);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    final static boolean isFastLongAvailable() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeLLLLLLrL", Function.class, long.class, long.class, long.class, long.class, long.class, long.class);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    
    public static abstract class AbstractNativeInterface implements LoadedLibrary {
        public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
        
        // Strong ref to keep the library alive
        protected final NativeLibrary library;

        public AbstractNativeInterface(NativeLibrary library) {
            this.library = library;
        }

        protected static final HeapInvocationBuffer newInvocationBuffer(Function f) {
            return new HeapInvocationBuffer(f);
        }

        public final Runtime __jaffl_runtime__() {
            return NativeRuntime.getInstance();
        }
    }
}
