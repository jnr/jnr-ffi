/*
 * Copyright (C) 2009 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.ParameterFlags;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.annotations.StdCall;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.FunctionMapper;
import com.kenai.jaffl.mapper.MethodParameterContext;
import com.kenai.jaffl.mapper.MethodResultContext;
import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;
import com.kenai.jaffl.provider.InvocationSession;
import com.kenai.jaffl.struct.Struct;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import static com.kenai.jaffl.provider.jffi.CodegenUtils.*;
import static com.kenai.jaffl.provider.jffi.NumberUtil.*;
import static com.kenai.jaffl.provider.jffi.AsmUtil.*;

public class AsmLibraryLoader extends LibraryLoader implements Opcodes {
    public final static boolean DEBUG = false || Boolean.getBoolean("jaffl.compile.dump");
    private static final LibraryLoader INSTANCE = new AsmLibraryLoader();
    private static final boolean FAST_NUMERIC_AVAILABLE = isFastNumericAvailable();
    private static final boolean FAST_LONG_AVAILABLE = isFastLongAvailable();
    private final AtomicLong nextClassID = new AtomicLong(0);
    private final AtomicLong nextIvarID = new AtomicLong(0);
    private final AtomicLong nextMethodID = new AtomicLong(0);

    static final LibraryLoader getInstance() {
        return INSTANCE;
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
                || CharSequence.class.isAssignableFrom(type)
                || ByReference.class.isAssignableFrom(type)
                || StringBuilder.class.isAssignableFrom(type)
                || StringBuffer.class.isAssignableFrom(type);
    }

    @Override
    <T> T loadLibrary(Library library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return generateInterfaceImpl(library, interfaceClass, libraryOptions);
    }

    private final <T> T generateInterfaceImpl(final Library library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = DEBUG ? AsmUtil.newCheckClassAdapter(AsmUtil.newTraceClassVisitor(cw, System.err)) : cw;

        String className = p(interfaceClass) + "$jaffl$" + nextClassID.getAndIncrement();

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, p(AbstractNativeInterface.class),
                new String[] { p(interfaceClass) });

        // Create the constructor to set the 'library' & functions fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC, "<init>",
                sig(void.class, Library.class, Function[].class, FromNativeConverter[].class, ToNativeConverter[][].class),
                null, null));
        init.start();
        // Invokes the super class constructor as super(Library)

        init.aload(0);
        init.aload(1);

        init.invokespecial(p(AbstractNativeInterface.class), "<init>", sig(void.class, Library.class));
        
        final Method[] methods = interfaceClass.getMethods();
        Function[] functions = new Function[methods.length];
        FromNativeConverter[] resultConverters = new FromNativeConverter[methods.length];
        ToNativeConverter[][] parameterConverters = new ToNativeConverter[methods.length][0];
        
        FunctionMapper functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.INSTANCE;

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

            boolean conversionRequired = false;

            resultConverters[i] = getResultConverter(m, typeMapper);
            if (resultConverters[i] != null) {
                cv.visitField(ACC_PRIVATE | ACC_FINAL, getResultConverterFieldName(i), ci(FromNativeConverter.class), null, null);
                nativeReturnType = resultConverters[i].nativeType();
                conversionRequired = true;
            }

            parameterConverters[i] = new ToNativeConverter[parameterTypes.length];
            for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
                ToNativeConverter converter = typeMapper.getToNativeConverter(parameterTypes[pidx]);
                if (converter != null) {
                    cv.visitField(ACC_PRIVATE | ACC_FINAL, getParameterConverterFieldName(i, pidx),
                            ci(ToNativeConverter.class), null, null);
                    nativeParameterTypes[pidx] = converter.nativeType();
                    
                    parameterConverters[i][pidx] = new ToNativeProxy(converter,
                            new MethodParameterContext(m, pidx));
                    conversionRequired = true;
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
                    nativeReturnType, nativeParameterTypes, InvokerUtil.requiresErrno(m),
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
                        functionFieldName, nativeReturnType, nativeParameterTypes,
                        m.getParameterAnnotations(), callingConvention, ignoreErrno);
            } else {
                generateMethod(cv, className, m.getName() + (conversionRequired ? "$raw" : ""),
                        functionFieldName, nativeReturnType, nativeParameterTypes,
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
                init.putfield(className, getResultConverterFieldName(i), ci(FromNativeConverter.class));
            }

            for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
                if (parameterConverters[i][pidx] != null) {
                    init.aload(0);
                    init.aload(4);
                    init.pushInt(i);
                    init.aaload();
                    init.pushInt(pidx);
                    init.aaload();
                    init.putfield(className, getParameterConverterFieldName(i, pidx), ci(ToNativeConverter.class));
                }
            }
        }

        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        cv.visitEnd();

        try {
            Class implClass = new AsmClassLoader(interfaceClass.getClassLoader()).defineClass(className.replace("/", "."), cw.toByteArray());
            Constructor<T> cons = implClass.getDeclaredConstructor(Library.class, Function[].class, FromNativeConverter[].class, ToNativeConverter[][].class);
            T result = cons.newInstance(library, functions, resultConverters, parameterConverters);

            // Attach any native method stubs - we have to delay this until the
            // implementation class is loaded for it to work.
            compiler.attach(implClass);

            return result;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private final FromNativeConverter getResultConverter(Method m, TypeMapper typeMapper) {
        Class returnType = m.getReturnType();
        FromNativeConverter conv = typeMapper.getFromNativeConverter(returnType);
        if (conv != null) {
            return new FromNativeProxy(conv, new MethodResultContext(m));
        } else if (Enum.class.isAssignableFrom(returnType)) {
            return new EnumResultConverter(returnType);
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
            mv.getfield(className, getResultConverterFieldName(idx), ci(FromNativeConverter.class));
        }

        
        mv.aload(0);

        // Load and convert the parameters
        int lvar = 1;
        for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
            final boolean convertParameter = !parameterTypes[pidx].equals(nativeParameterTypes[pidx]);
            if (convertParameter) {
                mv.aload(0);
                mv.getfield(className, getParameterConverterFieldName(idx, pidx), ci(ToNativeConverter.class));
            }

            lvar = loadParameter(mv, parameterTypes[pidx], lvar);
            
            if (convertParameter) {
                if (parameterTypes[pidx].isPrimitive()) {
                    boxPrimitive(mv, parameterTypes[pidx]);
                }

                mv.aconst_null();
                mv.invokeinterface(ToNativeConverter.class, "toNative",
                        Object.class, Object.class, ToNativeContext.class);
                mv.checkcast(p(nativeParameterTypes[pidx]));
            }
        }

        // Invoke the real native method
        mv.invokevirtual(className, functionName + "$raw", sig(nativeReturnType, nativeParameterTypes));
        if (!returnType.equals(nativeReturnType)) {
            if (nativeReturnType.isPrimitive()) {
                boxPrimitive(mv, nativeReturnType);
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
            Class returnType, Class[] parameterTypes, Annotation[][] annotations,
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

                generateBufferInvocation(bi, returnType, parameterTypes, annotations);
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
            Class returnType, Class[] parameterTypes, Annotation[][] parameterAnnotations,
            CallingConvention convention, boolean ignoreErrno) {

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, functionName, 
                sig(returnType, parameterTypes), null, null));
        mv.start();
        
        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractNativeInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve this.function
        mv.aload(0);
        mv.getfield(className, functionFieldName, ci(Function.class));

        if (convention == CallingConvention.DEFAULT && isFastIntegerMethod(returnType, parameterTypes)) {
            generateFastIntegerInvocation(mv, returnType, parameterTypes, parameterAnnotations, ignoreErrno);
        } else if (convention == CallingConvention.DEFAULT && FAST_NUMERIC_AVAILABLE && isFastNumericMethod(returnType, parameterTypes)) {
            generateFastNumericInvocation(mv, returnType, parameterTypes, parameterAnnotations);
        } else {
            generateBufferInvocation(mv, returnType, parameterTypes, parameterAnnotations);
        }
        
        mv.visitMaxs(100, calculateLocalVariableSpace(parameterTypes) + 10);
        mv.visitEnd();
    }

    private final void generateBufferInvocation(SkinnyMethodAdapter mv, Class returnType, Class[] parameterTypes, Annotation[][] annotations) {
        // [ stack contains: Invoker, Function ]
        final boolean sessionRequired = isSessionRequired(parameterTypes);
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
            if (isSessionRequired(parameterTypes[i])) {
                mv.aload(lvarSession);
                mv.swap();
            }

            lvar = loadParameter(mv, parameterTypes[i], lvar);
            
            final int parameterFlags = DefaultInvokerFactory.getParameterFlags(annotations[i]);
            final int nativeArrayFlags = DefaultInvokerFactory.getNativeArrayFlags(parameterFlags)
                        | ((parameterFlags & ParameterFlags.IN) != 0 ? ArrayFlags.NULTERMINATE : 0);

            if (parameterTypes[i].isArray() && parameterTypes[i].getComponentType().isPrimitive()) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, parameterTypes[i], int.class);

            } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Pointer.class, int.class);

            } else if (Address.class.isAssignableFrom(parameterTypes[i])) {
                marshal(mv, Pointer.class);

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
                emitInvocationBufferIntParameter(mv, parameterTypes[i]);

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
        } else if (Long.class == returnType || long.class == returnType) {
            invokeMethod = "invokeLong";
            nativeReturnType = long.class;
        } else if (NativeLong.class == returnType) {
            invokeMethod = NativeLong.SIZE == 32 ? "invokeInt" : "invokeLong";
            nativeReturnType = NativeLong.SIZE == 32 ? int.class : long.class;
        } else if (Pointer.class == returnType || Address.class == returnType
            || Struct.class.isAssignableFrom(returnType) || String.class.isAssignableFrom(returnType)) {
            invokeMethod = "invokeAddress";
            nativeReturnType = long.class;
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
    
    private final void generateFastIntegerInvocation(SkinnyMethodAdapter mv, Class returnType,
            Class[] parameterTypes, Annotation[][] annotations, boolean ignoreErrno) {
        // [ stack contains: Invoker, Function ]

        Label bufferInvocationLabel = emitDirectCheck(mv, parameterTypes);

        Class nativeIntType = getNativeIntType(returnType, parameterTypes);
        // Emit fast-int invocation
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            lvar = loadParameter(mv, parameterTypes[i], lvar);

            if (parameterTypes[i].isPrimitive()) {
                // widen to long if needed
                widen(mv, parameterTypes[i], nativeIntType);

            } else if (Number.class.isAssignableFrom(parameterTypes[i])) {
                unboxNumber(mv, parameterTypes[i], nativeIntType);

            } else if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                unboxPointer(mv, nativeIntType);

            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                unboxStruct(mv, nativeIntType);

            }
        }

        // stack now contains [ IntInvoker, Function, int args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                getFastIntInvokerMethodName(parameterTypes.length, ignoreErrno, nativeIntType),
                getFastIntInvokerSignature(parameterTypes.length, nativeIntType));

        emitReturn(mv, returnType, nativeIntType);

        // Now emit the fall back code to handle heap-based memory
        if (bufferInvocationLabel != null) {
            mv.label(bufferInvocationLabel);
            generateBufferInvocation(mv, returnType, parameterTypes, annotations);
        }
    }

    private final void generateFastNumericInvocation(SkinnyMethodAdapter mv, Class returnType, 
            Class[] parameterTypes, Annotation[][] annotations) {
        // [ stack contains: Invoker, Function ]

        Label bufferInvocationLabel = emitDirectCheck(mv, parameterTypes);

        // Emit fast-numeric invocation
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            lvar = loadParameter(mv, parameterTypes[i], lvar);

            if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                unboxPointer(mv, long.class);
                
            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                unboxStruct(mv, long.class);

            } else {
                
                if (!parameterTypes[i].isPrimitive() && Number.class.isAssignableFrom(parameterTypes[i])) {
                    unboxNumber(mv, parameterTypes[i], long.class);
                }

                if (Float.class == parameterTypes[i] || float.class == parameterTypes[i]) {
                    mv.invokestatic(Float.class, "floatToRawIntBits", int.class, float.class);
                    mv.i2l();

                } else if (Double.class == parameterTypes[i] || double.class == parameterTypes[i]) {
                    mv.invokestatic(Double.class, "doubleToRawLongBits", long.class, double.class);

                } else {
                    // widen to long if needed
                    widen(mv, parameterTypes[i], long.class);

                }
            }
        }

        // stack now contains [ IntInvoker, Function, int args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class), 
                getFastNumericInvokerMethodName(parameterTypes.length, returnType),
                getFastNumericInvokerSignature(parameterTypes.length));

        // Convert the result from long to the correct return type
        if (Float.class == returnType || float.class == returnType) {
            mv.l2i();
            mv.invokestatic(Float.class, "intBitsToFloat", float.class, int.class);

        } else if (Double.class == returnType || double.class == returnType) {
            mv.invokestatic(Double.class, "longBitsToDouble", double.class, long.class);
        }

        emitReturn(mv, returnType, long.class);

        if (bufferInvocationLabel != null) {
            // Now emit the alternate path for any parameters that might require it
            mv.label(bufferInvocationLabel);
            generateBufferInvocation(mv, returnType, parameterTypes, annotations);
        }
    }

    private static final Label emitDirectCheck(SkinnyMethodAdapter mv, Class[] parameterTypes) {

        // Iterate through any parameters that might require a HeapInvocationBuffer
        Label notFastInt = new Label();
        boolean needBufferInvocation = false;
        for (int i = 0, lvar = 1; i < parameterTypes.length; ++i) {
            if (Pointer.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Pointer.class);
                mv.iffalse(notFastInt);
                needBufferInvocation = true;
            } else if (Struct.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);
                mv.invokestatic(AsmRuntime.class, "isDirect", boolean.class, Struct.class);
                mv.iffalse(notFastInt);
                needBufferInvocation = true;
            } else {
                lvar += calculateLocalVariableSpace(parameterTypes[i]);
            }
        }

        return needBufferInvocation ? notFastInt : null;
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

    private static final Class<? extends Number> getNativeIntType(Class returnType, Class[] parameterTypes) {

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (requiresLong(parameterTypes[i])) {
                return long.class;
            }
        }

        return requiresLong(returnType) ? long.class : int.class;
    }

    static final String getFastIntInvokerMethodName(int parameterCount, boolean ignoreErrno, Class nativeParamType) {
        StringBuilder sb = new StringBuilder("invoke");

        if (ignoreErrno && int.class == nativeParamType) {
            sb.append("NoErrno");
        }

        String t = int.class == nativeParamType ? "I" : "L";

        if (parameterCount < 1) {
            sb.append("V");
        } else for (int i = 0; i < parameterCount; ++i) {
            sb.append(t);
        }

        return sb.append("r").append(t).toString();
    }

    static final String getFastIntInvokerSignature(int parameterCount, Class nativeIntType) {
        final String t = int.class == nativeIntType ? "I" : "J";
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(ci(Function.class));
        for (int i = 0; i < parameterCount; ++i) {
            sb.append(t);
        }
        sb.append(")").append(t);
        return sb.toString();
    }

    static final String getFastNumericInvokerMethodName(int parameterCount, Class nativeParamType) {
        StringBuilder sb = new StringBuilder("invoke");

        if (parameterCount < 1) {
            sb.append("V");
        } else for (int i = 0; i < parameterCount; ++i) {
            sb.append("N");
        }

        return sb.append("r").append("N").toString();
    }

    static final String getFastNumericInvokerSignature(int parameterCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(ci(Function.class));
        for (int i = 0; i < parameterCount; ++i) {
            sb.append("J");
        }
        sb.append(")").append("J");

        return sb.toString();
    }


    
    private final void boxStruct(SkinnyMethodAdapter mv, Class returnType) {
        mv.dup2();
        Label nonnull = new Label();
        Label end = new Label();
        mv.lconst_0();
        mv.lcmp();
        mv.ifne(nonnull);
        mv.pop2();
        mv.aconst_null();
        mv.go_to(end);

        mv.label(nonnull);
        // Create an instance of the struct subclass
        mv.newobj(p(returnType));
        mv.dup();
        mv.invokespecial(returnType, "<init>", void.class);
        mv.dup_x2();

        // associate the memory with the struct and return the struct
        mv.invokestatic(AsmRuntime.class, "useMemory", void.class, long.class, Struct.class);
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
            widen(mv, nativeType, long.class);
            primitiveClass = long.class;

        } else if (Boolean.class.isAssignableFrom(type)) {
            narrow(mv, nativeType, int.class);

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
            narrow(mv, nativeReturnType, int.class);
            mv.invokestatic(Boolean.class, "valueOf", Boolean.class, boolean.class);
            
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            mv.invokestatic(AsmRuntime.class, "pointerValue", Pointer.class, nativeReturnType);

        } else if (Address.class == returnType) {
            widen(mv, nativeReturnType, long.class);
            mv.invokestatic(returnType, "valueOf", returnType, long.class);

        } else if (Struct.class.isAssignableFrom(returnType)) {
            widen(mv, nativeReturnType, long.class);
            boxStruct(mv, returnType);
         
        } else if (Number.class.isAssignableFrom(returnType)) {
            boxNumber(mv, returnType, nativeReturnType);
         
        } else if (String.class == returnType) {
            widen(mv, nativeReturnType, long.class);
            mv.invokestatic(AsmRuntime.class, "returnString", String.class, long.class);
         
        } else {
            throw new IllegalArgumentException("cannot box value of type " + nativeReturnType + " to " + returnType);
        }
    }

    private final void emitInvocationBufferIntParameter(final SkinnyMethodAdapter mv, final Class parameterType) {
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
                sig(void.class, ci(InvocationSession.class) + ci(InvocationBuffer.class), parameterTypes));
    }
    
    private static final Function getFunction(long address, Class returnType, Class[] paramTypes, 
            boolean requiresErrno, CallingConvention convention) {
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[paramTypes.length];
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = InvokerUtil.getNativeParameterType(paramTypes[i]);
        }

        return new Function(address, InvokerUtil.getNativeReturnType(returnType),
                nativeParamTypes, convention, requiresErrno);

    }

    private static boolean isSessionRequired(Class parameterType) {
        return StringBuilder.class.isAssignableFrom(parameterType)
                || StringBuffer.class.isAssignableFrom(parameterType)
                || ByReference.class.isAssignableFrom(parameterType)
                || (parameterType.isArray() && Pointer.class.isAssignableFrom(parameterType.getComponentType()));
    }

    private static boolean isSessionRequired(Class[] parameterTypes) {
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (isSessionRequired(parameterTypes[i])) {
                return true;
            }
        }

        return false;
    }    

    final static boolean isFastNumericMethod(Class returnType, Class[] parameterTypes) {
        if (!FAST_NUMERIC_AVAILABLE || parameterTypes.length > 6) {
            return false;
        }

        if (!isFastNumericResult(returnType)) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!isFastNumericParam(parameterTypes[i])) {
                return false;
            }
        }
        return com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.I386
                || com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.X86_64;
    }

    final static boolean isFastIntegerMethod(Class returnType, Class[] parameterTypes) {
        if (parameterTypes.length > 3) {
            return false;
        }

        if (!isFastIntegerResult(returnType)) {
            return false;
        }
        
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!isFastIntegerParam(parameterTypes[i])) {
                return false;
            }
        }
        return com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.I386
                || com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.X86_64;
    }

    final static boolean isInt32(Class type) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type;
    }

    final static boolean isInt32Result(Class type) {
        return isInt32(type) || Void.class.isAssignableFrom(type) || void.class == type;
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

    private final static boolean isFastIntegerResult(Class type) {
        if (isInt32Result(type)) {
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

    private final static boolean isFastIntegerParam(Class type) {
        if (isInt32(type)) {
            return true;
        }

        final boolean isPointer = isPointerParam(type);
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

    final static boolean isFastNumericResult(Class type) {
        return isFastIntegerResult(type)
                || Long.class.isAssignableFrom(type) || long.class == type
                || NativeLong.class.isAssignableFrom(type)
                || Pointer.class.isAssignableFrom(type)
                || Struct.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type)
                || float.class == type || Float.class == type
                || double.class == type || Double.class == type;
    }

    final static boolean isFastNumericParam(Class type) {
        return isFastIntegerParam(type)
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
    
    private final static boolean requiresLong(Class type) {
        return Long.class.isAssignableFrom(type) || long.class == type
                || (NativeLong.class.isAssignableFrom(type) && Platform.getPlatform().longSize() == 64)
                || (Pointer.class.isAssignableFrom(type) && Platform.getPlatform().addressSize() == 64)
                || (Struct.class.isAssignableFrom(type) && Platform.getPlatform().addressSize() == 64)
                || (String.class.isAssignableFrom(type) && Platform.getPlatform().addressSize() == 64)
                ;
    }

    
    public static abstract class AbstractNativeInterface {
        public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
        
        // Strong ref to keep the library alive
        protected final Library library;

        public AbstractNativeInterface(Library library) {
            this.library = library;
        }

        protected static final HeapInvocationBuffer newInvocationBuffer(Function f) {
            return new HeapInvocationBuffer(f);
        }

    }

    public static final class ToNativeProxy implements ToNativeConverter {
        private final ToNativeConverter converter;
        private final ToNativeContext ctx;

        public ToNativeProxy(ToNativeConverter converter, ToNativeContext ctx) {
            this.converter = converter;
            this.ctx = ctx;
        }

        public Object toNative(Object value, ToNativeContext unused) {
            return converter.toNative(value, ctx);
        }

        public Class nativeType() {
            return converter.nativeType();
        }
    }

    public static final class FromNativeProxy implements FromNativeConverter {
        private final FromNativeConverter converter;
        private final FromNativeContext ctx;

        public FromNativeProxy(FromNativeConverter converter, FromNativeContext ctx) {
            this.converter = converter;
            this.ctx = ctx;
        }

        public Object fromNative(Object value, FromNativeContext unused) {
            return converter.fromNative(value, ctx);
        }

        public Class nativeType() {
            return converter.nativeType();
        }
    }
    
    public static final class IntToLong implements FromNativeConverter, ToNativeConverter {

        public Object fromNative(Object nativeValue, FromNativeContext context) {
            return ((Number) nativeValue).longValue();
        }

        public Object toNative(Object value, ToNativeContext context) {
            return Integer.valueOf(((Number) value).intValue());
        }

        public Class nativeType() {
            return Integer.class;
        }
    };

    public static interface TestLib {
        static final class s8 extends Struct {
            public final Signed8 s8 = new Signed8();
        }
        public Integer add_int32_t(Integer i1, int i2);
        public Float add_float(float f1, float f2);
        public Double add_double(Double f1, double f2);
//        public byte add_int8_t(byte i1, byte i2);
        byte ptr_ret_int8_t(s8[] s, int index);
        Byte ptr_ret_int8_t(Pointer p, int index);
        byte ptr_ret_int8_t(s8 s, int index);
        void not_found_function();
    }


    public static void main(String[] args) {
        System.setProperty("jaffl.compile.dump", "true");
        System.out.println("cpu=" + Platform.getPlatform().getCPU());
        final Map<LibraryOption, Object> options = new HashMap<LibraryOption, Object>();
//        options.put(LibraryOption.TypeMapper, new TypeMapper() {
//
//            public FromNativeConverter getFromNativeConverter(Class type) {
//                if (Long.class.isAssignableFrom(type)) {
//                    return new IntToLong();
//                }
//                return null;
//            }
//
//            public ToNativeConverter getToNativeConverter(Class type) {
//                if (Long.class.isAssignableFrom(type) || long.class == type) {
//                    return new IntToLong();
//                }
//                return null;
//            }
//        });

        TestLib lib = AsmLibraryLoader.getInstance().loadLibrary(new Library("test"), TestLib.class, options);
        Number result = lib.add_int32_t(1, 2);
        System.err.println("result=" + result);
//        System.err.println("adding bytes =" + lib.add_int8_t((byte) 1, (byte) 3));
        System.err.println("adding floats=" + lib.add_float(1.0f, 2.0f));
        System.err.println("adding doubles=" + lib.add_double(1.0, 2.0));
        Pointer p = MemoryIO.allocateDirect(1024);
        lib.ptr_ret_int8_t(p, 0);
        lib.ptr_ret_int8_t(MemoryIO.allocate(1024), 0);
    }
}
