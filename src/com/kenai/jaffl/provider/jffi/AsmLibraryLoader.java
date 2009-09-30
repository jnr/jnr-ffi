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
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.ParameterFlags;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.FunctionMapper;
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
import java.io.PrintWriter;
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
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import static com.kenai.jaffl.provider.jffi.CodegenUtils.*;

public class AsmLibraryLoader extends LibraryLoader implements Opcodes {
    private static final LibraryLoader INSTANCE = new AsmLibraryLoader();
    private final AtomicLong nextClassID = new AtomicLong(0);
    private final AtomicLong nextIvarID = new AtomicLong(0);

    static final LibraryLoader getInstance() {
        return INSTANCE;
    }

    boolean isInterfaceSupported(Class interfaceClass, Map<LibraryOption, ?> options) {
        TypeMapper typeMapper = options.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) options.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;

        for (Method m : interfaceClass.getDeclaredMethods()) {
            if (!isReturnTypeSupported(m.getReturnType()) && getResultConverter(m.getReturnType(), typeMapper) == null) {
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
        ClassVisitor cv = new CheckClassAdapter(new TraceClassVisitor(cw, new PrintWriter(System.out)));
//        ClassVisitor cv = new CheckClassAdapter(cw);
//        ClassVisitor cv = cw;

        String className = p(AsmLibraryLoader.class).replace("/", "_")
                + "_" + interfaceClass.getSimpleName() + "_" + nextClassID.incrementAndGet();

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
        
        final Method[] methods = interfaceClass.getDeclaredMethods();
        Function[] functions = new Function[methods.length];
        FromNativeConverter[] resultConverters = new FromNativeConverter[methods.length];
        ToNativeConverter[][] parameterConverters = new ToNativeConverter[methods.length][0];
        
        FunctionMapper functionMapper = libraryOptions.containsKey(LibraryOption.FunctionMapper)
                ? (FunctionMapper) libraryOptions.get(LibraryOption.FunctionMapper) : IdentityFunctionMapper.INSTANCE;

        TypeMapper typeMapper = libraryOptions.containsKey(LibraryOption.TypeMapper)
                ? (TypeMapper) libraryOptions.get(LibraryOption.TypeMapper) : NullTypeMapper.INSTANCE;
        
        for (int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            final Class returnType = m.getReturnType();
            final Class[] parameterTypes = m.getParameterTypes();
            Class nativeReturnType = returnType;
            Class[] nativeParameterTypes = new Class[parameterTypes.length];

            boolean conversionRequired = false;

            resultConverters[i] = getResultConverter(returnType, typeMapper);
            if (resultConverters[i] != null) {
                cv.visitField(ACC_PRIVATE | ACC_FINAL, getResultConverterFieldName(i), ci(FromNativeConverter.class), null, null);
                nativeReturnType = resultConverters[i].nativeType();
                conversionRequired = true;
            }

            parameterConverters[i] = new ToNativeConverter[parameterTypes.length];
            for (int pidx = 0; pidx < parameterTypes.length; ++pidx) {
                parameterConverters[i][pidx] = typeMapper.getToNativeConverter(parameterTypes[pidx]);
                if (parameterConverters[i][pidx] != null) {
                    cv.visitField(ACC_PRIVATE | ACC_FINAL, getParameterConverterFieldName(i, pidx),
                            ci(ToNativeConverter.class), null, null);
                    nativeParameterTypes[pidx] = parameterConverters[i][pidx].nativeType();
                    conversionRequired = true;
                } else {
                    nativeParameterTypes[pidx] = parameterTypes[pidx];
                }
            }

            try {
                functions[i] = getFunction(library.findSymbolAddress(functionMapper.mapFunctionName(m.getName(), null)),
                    nativeReturnType, nativeParameterTypes, InvokerUtil.requiresErrno(m));
            } catch (SymbolNotFoundError ex) {
                generateFunctionNotFound(cv, className, m.getName(), returnType, parameterTypes);
                continue;
            }

            String functionFieldName = "function_" + i;

            cv.visitField(ACC_PRIVATE | ACC_FINAL, functionFieldName, ci(Function.class), null, null);
            final boolean ignoreErrno = !InvokerUtil.requiresErrno(m);

            generateMethod(cv, className, m.getName() + (conversionRequired ? "$raw" : ""), functionFieldName, m, nativeReturnType, nativeParameterTypes,
                    m.getParameterAnnotations(), ignoreErrno);

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
            Class implClass = AsmLoader.INSTANCE.defineClass(className, cw.toByteArray());
            Constructor<T> cons = implClass.getDeclaredConstructor(Library.class, Function[].class, FromNativeConverter[].class, ToNativeConverter[][].class);
            return cons.newInstance(library, functions, resultConverters, parameterConverters);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private final FromNativeConverter getResultConverter(Class returnType, TypeMapper typeMapper) {
        FromNativeConverter conv = typeMapper.getFromNativeConverter(returnType);
        if (conv != null) {
            return conv;
        } else if (Enum.class.isAssignableFrom(returnType)) {
            return new EnumResultConverter(returnType);
        } else {
            return null;
        }
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

    private final void generateFunctionNotFound(ClassVisitor cv, String className, String functionName,
            Class returnType, Class[] parameterTypes) {
        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, functionName,
                sig(returnType, parameterTypes), null, null));
        mv.start();
        mv.newobj(p(UnsatisfiedLinkError.class));
        mv.dup();
        mv.invokespecial(p(UnsatisfiedLinkError.class), "<init>", "()V");
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

            if (parameterTypes[pidx].isPrimitive()) {
                if (float.class == parameterTypes[pidx]) {
                    mv.fload(lvar++);
                } else if (double.class == parameterTypes[pidx]) {
                    mv.dload(lvar);
                    lvar += 2;
                } else if (long.class == parameterTypes[pidx]) {
                    mv.lload(lvar);
                    lvar += 2;
                } else {
                    mv.iload(lvar++);
                }
            } else {
                mv.aload(lvar++);
            }

            if (convertParameter) {
                if (parameterTypes[pidx].isPrimitive()) {
                    boxPrimitive(mv, parameterTypes[pidx]);
                }
                mv.aconst_null();
                mv.invokeinterface(p(ToNativeConverter.class), "toNative",
                        sig(Object.class, Object.class, ToNativeContext.class));
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
            mv.invokeinterface(p(FromNativeConverter.class), "fromNative",
                    sig(Object.class, Object.class, FromNativeContext.class));
            mv.checkcast(p(returnType));
        }
        emitReturnOp(mv, returnType);
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    private final void generateMethod(ClassVisitor cv, String className, String functionName, String functionFieldName, Method m,
            Class returnType, Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreErrno) {

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, functionName, 
                sig(returnType, parameterTypes), null, null));
        mv.start();
        
        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(p(AbstractNativeInterface.class), "ffi", ci(com.kenai.jffi.Invoker.class));

        // retrieve this.function
        mv.aload(0);
        mv.getfield(className, functionFieldName, ci(Function.class));

        if (isFastIntMethod(returnType, parameterTypes)) {
            generateFastIntInvocation(mv, returnType, parameterTypes, ignoreErrno);
        } else {
            generateBufferInvocation(mv, returnType, parameterTypes, parameterAnnotations);
        }
        
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    private final void generateBufferInvocation(SkinnyMethodAdapter mv, Class returnType, Class[] parameterTypes, Annotation[][] annotations) {
        // [ stack contains: Invoker, Function ]
        final boolean sessionRequired = isSessionRequired(parameterTypes);
        final int lvarSession = sessionRequired ? getTotalParameterSize(parameterTypes) + 1 : -1;
        if (sessionRequired) {
            mv.newobj(p(InvocationSession.class));
            mv.dup();
            mv.invokespecial(p(InvocationSession.class), "<init>",sig(void.class));
            mv.astore(lvarSession);
        }

        // new HeapInvocationBuffer(function)
        mv.newobj(p(HeapInvocationBuffer.class));
        
        // [ ..., Function, Buffer ] => [ ..., Function, Buffer, Function, Buffer ]
        mv.dup2();
        // [ ..., Function, Buffer, Function, Buffer ] => [ ..., Function, Buffer, Buffer, Function ]
        mv.swap();
        mv.invokespecial(p(HeapInvocationBuffer.class), "<init>", sig(void.class, Function.class));

        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.dup(); // dup ref to HeapInvocationBuffer
            if (isSessionRequired(parameterTypes[i])) {
                mv.aload(lvarSession);
                mv.swap();
            }
            if (!parameterTypes[i].isPrimitive()) {
                mv.aload(lvar++);
            } else if (long.class == parameterTypes[i]) {
                mv.lload(lvar);
                lvar += 2;
            } else if (float.class == parameterTypes[i]) {
                mv.fload(lvar++);
            } else if (double.class == parameterTypes[i]) {
                mv.dload(lvar);
                lvar += 2;
            } else {
                mv.iload(lvar++);
            }
            
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
        
        if (isPrimitiveInt(returnType) || void.class == returnType || boolean.class == returnType
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
        
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class), invokeMethod,
                sig(nativeReturnType, Function.class, HeapInvocationBuffer.class));

        if (sessionRequired) {
            mv.aload(lvarSession);
            mv.invokevirtual(p(InvocationSession.class), "finish", "()V");
        }

        if (Struct.class.isAssignableFrom(returnType)) {
            boxStructReturnValue(mv, returnType);
        } else if (String.class == returnType) {
            mv.invokestatic(p(MarshalUtil.class), "returnString", sig(String.class, long.class));
            mv.areturn();
        } else if (!returnType.isPrimitive()) {
            boxIntReturnValue(mv, returnType, nativeReturnType);
            mv.areturn();
        } else {
            emitReturnOp(mv, returnType);
        }
    }
    
    private final void generateFastIntInvocation(SkinnyMethodAdapter mv, Class returnType, Class[] parameterTypes, boolean ignoreErrno) {
        // [ stack contains: Invoker, Function ]

        Class nativeIntType = getNativeIntType(returnType, parameterTypes);

        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (isPrimitiveInt(parameterTypes[i]) || boolean.class == parameterTypes[i]) {
                mv.iload(lvar++);
                if (nativeIntType == long.class) {
                    mv.i2l(); // sign extend to long
                }
            } else if (long.class == parameterTypes[i]) {
                mv.lload(lvar); lvar += 2;
            } else if (Number.class.isAssignableFrom(parameterTypes[i])) {
                mv.aload(lvar++);
                unboxIntParameter(mv, parameterTypes[i], nativeIntType);
            }
        }

        // stack now contains [ IntInvoker, Function, int args ]
        mv.invokevirtual(p(com.kenai.jffi.Invoker.class),
                getFastIntInvokerMethodName(parameterTypes.length, ignoreErrno, nativeIntType),
                sig(nativeIntType, ci(Function.class), params(nativeIntType, parameterTypes.length)));

        if (isPrimitiveInt(returnType) || boolean.class == returnType) {
            if (int.class != nativeIntType) mv.l2i();
            mv.ireturn();
        } else if (long.class == returnType) {
            if (long.class != nativeIntType) mv.i2l();
            mv.lreturn();
        } else if (void.class == returnType) {
            mv.voidreturn();
        } else {
            boxIntReturnValue(mv, returnType, nativeIntType);
            mv.areturn();
        }
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
    
    private final void boxStructReturnValue(SkinnyMethodAdapter mv, Class returnType) {
        mv.dup2();
        Label retnull = new Label();
        mv.lconst_0();
        mv.lcmp();
        mv.ifeq(retnull);

        // Create an instance of the struct subclass
        mv.newobj(p(returnType));
        mv.dup();
        mv.invokespecial(p(returnType), "<init>", "()V");
        mv.dup_x2();

        // associate the memory with the struct and return the struct
        invokestatic(mv, MarshalUtil.class, "useMemory", void.class, long.class, Struct.class);
        mv.areturn();

        mv.label(retnull);
        mv.aconst_null();
        mv.areturn();
    }

    private final void boxPrimitive(SkinnyMethodAdapter mv, Class primitiveType) {
        Class objClass = getBoxedClass(primitiveType);
        invokestatic(mv, objClass, "valueOf", objClass, primitiveType);
    }

    private final void boxIntReturnValue(SkinnyMethodAdapter mv, Class returnType, Class nativeReturnType) {
        Class primitiveClass, objClass = returnType;
        if (Byte.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.l2i();
            mv.i2b();
            primitiveClass = byte.class;
            
        } else if (Short.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.l2i();
            mv.i2s();
            primitiveClass = short.class;
            
        } else if (Integer.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.l2i();
            primitiveClass = int.class;
            

        } else if (Long.class.isAssignableFrom(returnType)) {
            if (long.class != nativeReturnType) mv.i2l();
            primitiveClass = long.class;
            
        } else if (NativeLong.class.isAssignableFrom(returnType)) {
            if (long.class != nativeReturnType) mv.i2l();
            primitiveClass = long.class;

        } else if (Float.class.isAssignableFrom(returnType)) {
            primitiveClass = float.class;

        } else if (Double.class.isAssignableFrom(returnType)) {
            primitiveClass = double.class;

        } else if (Boolean.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.l2i();
            primitiveClass = boolean.class;
            
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            invokestatic(mv, MarshalUtil.class, "returnPointer", Pointer.class, nativeReturnType);
            return;

        } else if (Address.class == returnType) {
            if (long.class != nativeReturnType) mv.i2l();
            primitiveClass = long.class;
        } else {
            throw new IllegalArgumentException("invalid return type");
        }

        invokestatic(mv, objClass, "valueOf", objClass, primitiveClass);
    }

    
    private final void unboxIntParameter(final SkinnyMethodAdapter mv, final Class parameterType, final Class nativeIntType) {
        String intValueMethod = long.class == nativeIntType ? "longValue" : "intValue";
        String intValueSignature = long.class == nativeIntType ? "()J" : "()I";

        if (Byte.class == parameterType || Short.class == parameterType || Integer.class == parameterType) {
            mv.invokevirtual(p(parameterType), intValueMethod, intValueSignature);

        } else if (Long.class == parameterType) {
            mv.invokevirtual(p(parameterType), "longValue", "()J");

        } else if (Float.class == parameterType) {
            mv.invokevirtual(p(parameterType), "floatValue", "()F");

        } else if (Double.class == parameterType) {
            mv.invokevirtual(p(parameterType), "doubleValue", "()D");

        } else if (NativeLong.class.isAssignableFrom(parameterType) && Platform.getPlatform().longSize() == 64) {
            mv.invokevirtual(p(parameterType), "longValue", "()J");

        } else if (NativeLong.class.isAssignableFrom(parameterType)) {
            mv.invokevirtual(p(parameterType), intValueMethod, intValueSignature);

        } else if (Boolean.class.isAssignableFrom(parameterType)) {
            mv.invokevirtual(p(parameterType), "booleanValue", "()Z");

        } else {
            throw new IllegalArgumentException("unsupported Number subclass");
        }
    }
    
    private final void emitReturnOp(SkinnyMethodAdapter mv, Class returnType) {
        if (isPrimitiveInt(returnType) || boolean.class == returnType) {
            mv.ireturn();
        } else if (long.class == returnType) {
            mv.lreturn();
        } else if (float.class == returnType) {
            mv.freturn();
        } else if (double.class == returnType) {
            mv.dreturn();
        } else if (void.class == returnType) {
            mv.voidreturn();
        } else {
            mv.areturn();
        }
    }

    private final void emitInvocationBufferIntParameter(final SkinnyMethodAdapter mv, final Class parameterType) {
        String paramMethod = null;
        Class paramClass = int.class;
        
        if (!parameterType.isPrimitive()) {
            unboxIntParameter(mv, parameterType, null);
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
        mv.invokevirtual(p(HeapInvocationBuffer.class), paramMethod,
                sig(void.class, paramClass));
    }

    private final void invokestatic(SkinnyMethodAdapter mv, Class recv, String methodName, Class returnType, Class... parameterTypes) {
        mv.invokestatic(p(recv), methodName, sig(returnType, parameterTypes));
    }

    private final void marshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(MarshalUtil.class), "marshal", sig(void.class, ci(InvocationBuffer.class), parameterTypes));
    }

    private final void sessionmarshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(MarshalUtil.class), "marshal",
                sig(void.class, ci(InvocationSession.class) + ci(InvocationBuffer.class), parameterTypes));
    }
    
    private static final Function getFunction(long address, Class returnType, Class[] paramTypes, boolean requiresErrno) {
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[paramTypes.length];
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = InvokerUtil.getNativeParameterType(paramTypes[i]);
        }

        return new Function(address, InvokerUtil.getNativeReturnType(returnType),
                nativeParamTypes, CallingConvention.DEFAULT, requiresErrno);

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

    private static int getTotalParameterSize(Class[] parameterTypes) {
        int size = 0;
        for (int i = 0; i < parameterTypes.length; ++i) {
            size++;
            if (long.class == parameterTypes[i] || double.class == parameterTypes[i])
                size++;
        }

        return size;
    }
    
    private static boolean isPrimitiveInt(Class c) {
        return byte.class == c || short.class == c || int.class == c;
    }

    final static boolean isFastIntMethod(Class returnType, Class[] parameterTypes) {
        if (parameterTypes.length > 3) {
            return false;
        }

        if (!isFastIntResult(returnType)) {
            return false;
        }
        
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!isFastIntParam(parameterTypes[i])) {
                return false;
            }
        }
        return com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.I386
                || com.kenai.jffi.Platform.getPlatform().getCPU() == com.kenai.jffi.Platform.CPU.X86_64;
    }

    private static final Class getBoxedClass(Class c) {
        if (!c.isPrimitive()) {
            return c;
        }

        if (void.class == c) {
            return Void.class;
        } else if (byte.class == c) {
            return Byte.class;
        } else if (char.class == c) {
            return Character.class;
        } else if (short.class == c) {
            return Short.class;
        } else if (int.class == c) {
            return Integer.class;
        } else if (long.class == c) {
            return Long.class;
        } else if (float.class == c) {
            return Float.class;
        } else if (double.class == c) {
            return Double.class;
        } else if (boolean.class == c) {
            return Boolean.class;
        } else {
            throw new IllegalArgumentException("unknown primitive class");
        }
    }

    private static final Class getPrimitiveClass(Class c) {
        if (!Number.class.isAssignableFrom(c)) {
            return c;
        }
        if (Void.class == c) {
            return void.class;
        } else if (Byte.class == c) {
            return byte.class;
        } else if (Character.class == c) {
            return char.class;
        } else if (Short.class == c) {
            return short.class;
        } else if (Integer.class == c) {
            return int.class;
        } else if (Long.class == c) {
            return long.class;
        } else if (Float.class == c) {
            return float.class;
        } else if (Double.class == c) {
            return double.class;
        } else if (Boolean.class == c) {
            return boolean.class;
        } else {
            throw new IllegalArgumentException("unknown number class");
        }
    }

    final static boolean isFastIntResult(Class type) {
        return Void.class.isAssignableFrom(type) || void.class == type
                || Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || Long.class.isAssignableFrom(type) || long.class == type
                || NativeLong.class.isAssignableFrom(type)
                || Pointer.class.isAssignableFrom(type)
                ;
    }
    final static boolean isFastIntParam(Class type) {
        return Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || Long.class.isAssignableFrom(type) || long.class == type
                || NativeLong.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type) || boolean.class == type
                ;
    }

    private final static boolean requiresLong(Class type) {
        return Long.class.isAssignableFrom(type) || long.class == type
                || (NativeLong.class.isAssignableFrom(type) && Platform.getPlatform().longSize() == 64)
                || (Pointer.class.isAssignableFrom(type) && Platform.getPlatform().addressSize() == 64)
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

    static final int marshall(byte[] array) {
        if (array == null) {
            return 1;
        } else {
            return array[0];
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
        public static final class s8 extends Struct {
            public final Signed8 s8 = new Signed8();
        }
        public Long add_int32_t(long i1, int i2);
        public byte add_int8_t(byte i1, byte i2);
        byte ptr_ret_int8_t(s8[] s, int index);
    }


    public static void main(String[] args) {
        final Map<LibraryOption, Object> options = new HashMap<LibraryOption, Object>();
        options.put(LibraryOption.TypeMapper, new TypeMapper() {

            public FromNativeConverter getFromNativeConverter(Class type) {
                if (Long.class.isAssignableFrom(type)) {
                    return new IntToLong();
                }
                return null;
            }

            public ToNativeConverter getToNativeConverter(Class type) {
                if (Long.class.isAssignableFrom(type) || long.class == type) {
                    return new IntToLong();
                }
                return null;
            }
        });

        TestLib lib = AsmLibraryLoader.getInstance().loadLibrary(new Library("test"), TestLib.class, options);
        Number result = lib.add_int32_t(1L, 2);
        System.err.println("result=" + result);
    }
}
