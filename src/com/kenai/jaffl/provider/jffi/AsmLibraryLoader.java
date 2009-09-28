
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.ParameterFlags;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.byref.ByteByReference;
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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

public class AsmLibraryLoader extends LibraryLoader implements Opcodes {
    private static final LibraryLoader INSTANCE = new AsmLibraryLoader();
    private final AtomicLong nextClassID = new AtomicLong(0);
    private final AtomicLong nextIvarID = new AtomicLong(0);

    static final LibraryLoader getInstance() {
        return INSTANCE;
    }

    boolean isInterfaceSupported(Class interfaceClass) {
        for (Method m : interfaceClass.getDeclaredMethods()) {
            if (!isReturnTypeSupported(m.getReturnType())) {
                return false;
            }
            for (Class c: m.getParameterTypes()) {
                if (!isParameterTypeSupported(c)) {
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
                || CharSequence.class.isAssignableFrom(type)
                || ByReference.class.isAssignableFrom(type)
                || StringBuilder.class.isAssignableFrom(type)
                || StringBuffer.class.isAssignableFrom(type);
    }
    
    @Override
    <T> T loadLibrary(Library library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return generateInterfaceImpl(library, interfaceClass, libraryOptions);
    }

    private final <T> T generateInterfaceImpl(Library library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new CheckClassAdapter(new TraceClassVisitor(cw, new PrintWriter(System.err)));
//        ClassVisitor cv = cw;

        String className = Type.getInternalName(AsmLibraryLoader.class).replace("/", "_")
                + "_" + interfaceClass.getSimpleName() + "_" + nextClassID.incrementAndGet();

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, Type.getInternalName(AbstractNativeInterface.class),
                new String[] { Type.getInternalName(interfaceClass) });

        // Create the constructor to set the 'library' & functions fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC, "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Library.class), Type.getType(Function[].class) }),
                null, null));
        init.start();
        // Invokes the super class constructor as super(Library)

        init.aload(0);
        init.aload(1);

        init.invokespecial(Type.getInternalName(AbstractNativeInterface.class), "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Library.class) }));
        
        final Method[] methods = interfaceClass.getDeclaredMethods();
        Function[] functions = new Function[methods.length];

        init.aload(0);
        init.aload(2);

        for (int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            functions[i] = getFunction(m, library.findSymbolAddress(m.getName()));

            String functionFieldName = "function_" + i;

            cv.visitField(ACC_PRIVATE | ACC_FINAL, functionFieldName, Type.getDescriptor(Function.class), null, null);

            generateMethod(cv, className, functionFieldName, m);

            // The Function[] array is passed in as the second param, so generate
            // the constructor code to store each function in a field
            init.dup2();
            init.pushInt(i);
            init.aaload();
            init.putfield(className, functionFieldName, Type.getDescriptor(Function.class));
        }

        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        cv.visitEnd();

        try {
            Class implClass = AsmLoader.INSTANCE.defineClass(className, cw.toByteArray());
            Constructor<T> cons = implClass.getDeclaredConstructor(Library.class, Function[].class);
            return cons.newInstance(library, functions);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private final void generateMethod(ClassVisitor cv, String className, String functionFieldName, Method m) {

        SkinnyMethodAdapter mv = new SkinnyMethodAdapter(cv.visitMethod(ACC_PUBLIC | ACC_FINAL, m.getName(), Type.getMethodDescriptor(m), null, null));
        mv.start();

        final Class returnType = m.getReturnType();
        final Class[] parameterTypes = m.getParameterTypes();

        // Retrieve the static 'ffi' Invoker instance
        mv.getstatic(Type.getInternalName(AbstractNativeInterface.class), "ffi", Type.getDescriptor(com.kenai.jffi.Invoker.class));

        // retrieve this.function
        mv.aload(0);
        mv.getfield(className, functionFieldName, Type.getDescriptor(Function.class));

        if (isFastIntMethod(m)) {
            generateFastIntInvocation(mv, returnType, parameterTypes);
        } else {
            generateBufferInvocation(mv, returnType, parameterTypes, m.getParameterAnnotations());
        }
        
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    private final void generateBufferInvocation(SkinnyMethodAdapter mv, Class returnType, Class[] parameterTypes, Annotation[][] annotations) {
        // [ stack contains: Invoker, Function ]
        final boolean sessionRequired = isSessionRequired(parameterTypes);
        final int lvarSession = sessionRequired ? getTotalParameterSize(parameterTypes) + 1 : -1;
        if (sessionRequired) {
            mv.newobj(Type.getInternalName(InvocationSession.class));
            mv.dup();
            mv.invokespecial(Type.getInternalName(InvocationSession.class), "<init>",
                    Type.getMethodDescriptor(Type.getType(void.class), new Type[]{}));
            mv.astore(lvarSession);
        }

        // new HeapInvocationBuffer(function)
        mv.newobj(Type.getInternalName(HeapInvocationBuffer.class));
        
        // [ ..., Function, Buffer ] => [ ..., Function, Buffer, Function, Buffer ]
        mv.dup2();
        // [ ..., Function, Buffer, Function, Buffer ] => [ ..., Function, Buffer, Buffer, Function ]
        mv.swap();
        mv.invokespecial(Type.getInternalName(HeapInvocationBuffer.class), "<init>",
                Type.getMethodDescriptor(Type.getType(void.class), new Type[] { Type.getType(Function.class) }));

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
        
        mv.invokevirtual(Type.getInternalName(com.kenai.jffi.Invoker.class), invokeMethod,
                Type.getMethodDescriptor(Type.getType(nativeReturnType), new Type[] { Type.getType(Function.class), Type.getType(HeapInvocationBuffer.class) }));

        if (sessionRequired) {
            mv.aload(lvarSession);
            mv.invokevirtual(Type.getInternalName(InvocationSession.class), "finish", "()V");
        }

        if (Struct.class.isAssignableFrom(returnType)) {
            boxStructReturnValue(mv, returnType);
        } else if (String.class == returnType) {
            mv.invokevirtual(Type.getInternalName(MarshalUtil.class), "returnString",
                Type.getMethodDescriptor(Type.getType(String.class), new Type[] { Type.LONG_TYPE }));
            mv.areturn();
        } else if (!returnType.isPrimitive()) {
            boxIntReturnValue(mv, returnType, nativeReturnType);
            mv.areturn();
        } else {
            emitReturnOp(mv, returnType);
        }
    }
    
    private final void generateFastIntInvocation(SkinnyMethodAdapter mv, Class returnType, Class[] parameterTypes) {
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

        Type[] paramTypes = new Type[parameterTypes.length + 1];
        paramTypes[0] = Type.getType(Function.class);
        for (int i = 0; i < parameterTypes.length; ++i) {
            paramTypes[i + 1] = Type.getType(nativeIntType);
        }

        // stack now contains [ IntInvoker, Function, int args ]
        mv.invokevirtual(Type.getInternalName(com.kenai.jffi.Invoker.class),
                getFastIntInvokerMethodName(parameterTypes.length, false, nativeIntType),
                Type.getMethodDescriptor(Type.getType(nativeIntType), paramTypes));

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
        Label nonnull = new Label();
        mv.lconst_0();
        mv.lcmp();
        mv.ifne(nonnull);
        mv.aconst_null();
        mv.areturn();
        mv.label(nonnull);
        // Create an instance of the struct subclass
        mv.newobj(Type.getInternalName(returnType));
        mv.dup();
        mv.invokespecial(Type.getInternalName(returnType), "<init>",
                Type.getMethodDescriptor(Type.getType(void.class), new Type[]{}));
        mv.dup_x2();

        // associate the memory with the struct and return the struct
        invokestatic(mv, MarshalUtil.class, "useMemory", void.class, long.class, Struct.class);
        mv.areturn();
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
            primitiveClass = nativeReturnType;
            objClass = JFFIPointer.class;

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
            mv.invokevirtual(Type.getInternalName(parameterType), intValueMethod, intValueSignature);

        } else if (Long.class == parameterType) {
            mv.invokevirtual(Type.getInternalName(parameterType), "longValue", "()J");

        } else if (Float.class == parameterType) {
            mv.invokevirtual(Type.getInternalName(parameterType), "floatValue", "()F");

        } else if (Double.class == parameterType) {
            mv.invokevirtual(Type.getInternalName(parameterType), "doubleValue", "()D");

        } else if (NativeLong.class.isAssignableFrom(parameterType) && Platform.getPlatform().longSize() == 64) {
            mv.invokevirtual(Type.getInternalName(parameterType), "longValue", "()J");

        } else if (NativeLong.class.isAssignableFrom(parameterType)) {
            mv.invokevirtual(Type.getInternalName(parameterType), intValueMethod, intValueSignature);

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
        mv.invokevirtual(Type.getInternalName(HeapInvocationBuffer.class), paramMethod,
                Type.getMethodDescriptor(Type.getType(void.class), new Type[] { Type.getType(paramClass) }));
    }

    private final void invokestatic(SkinnyMethodAdapter mv, Class recv, String methodName, Class returnType, Class... parameterTypes) {
        Type[] types = new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            types[i] = Type.getType(parameterTypes[i]);
        }
        mv.invokestatic(Type.getInternalName(recv), methodName, Type.getMethodDescriptor(Type.getType(returnType), types));
    }

    private final void marshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        Type[] types = new Type[parameterTypes.length + 1];
        for (int i = 0; i < parameterTypes.length; ++i) {
            types[i + 1] = Type.getType(parameterTypes[i]);
        }
        types[0] = Type.getType(InvocationBuffer.class);
        mv.invokestatic(Type.getInternalName(MarshalUtil.class), "marshal", Type.getMethodDescriptor(Type.VOID_TYPE, types));
    }

    private final void sessionmarshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        Type[] types = new Type[parameterTypes.length + 2];
        for (int i = 0; i < parameterTypes.length; ++i) {
            types[i + 2] = Type.getType(parameterTypes[i]);
        }
        types[0] = Type.getType(InvocationSession.class);
        types[1] = Type.getType(InvocationBuffer.class);

        mv.invokestatic(Type.getInternalName(MarshalUtil.class), "marshal", Type.getMethodDescriptor(Type.VOID_TYPE, types));
    }
    
    private static final Function getFunction(Method method, long address) {
        Class[] paramTypes = method.getParameterTypes();
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[paramTypes.length];
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = InvokerUtil.getNativeParameterType(paramTypes[i]);
        }

        return new Function(address, InvokerUtil.getNativeReturnType(method),
                nativeParamTypes, CallingConvention.DEFAULT, InvokerUtil.requiresErrno(method));

    }

    private static boolean isSessionRequired(Class parameterType) {
        return StringBuilder.class.isAssignableFrom(parameterType)
                || StringBuffer.class.isAssignableFrom(parameterType)
                || ByReference.class.isAssignableFrom(parameterType);
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

    final static boolean isFastIntMethod(Method method) {
        if (!isFastIntResult(method.getReturnType())) {
            return false;
        }

        Class[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length > 3) {
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

    
    static final class AsmLoader extends ClassLoader {
        static final AsmLoader INSTANCE = new AsmLoader();

        public Class defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    static final int marshall(byte[] array) {
        if (array == null) {
            return 1;
        } else {
            return array[0];
        }
    }
    public static interface TestLib {
        byte ptr_ret_int8_t(ByteByReference p, int offset);
        byte ptr_ret_int8_t(@In byte[] p, int offset);
        public byte add_int8_t(byte i1, byte i2);
        public byte add_int8_t(Byte i1, byte i2);
        public Byte add_int8_t(byte i1, Byte i2);

        public short add_int16_t(short i1, short i2);

        public int add_int32_t(int i1, int i2);
        public long add_int64_t(long i1, long i2);
        public NativeLong add_long(NativeLong i1, NativeLong i2);
        public NativeLong sub_long(NativeLong i1, NativeLong i2);
        public NativeLong mul_long(NativeLong i1, NativeLong i2);
        public NativeLong div_long(NativeLong i1, NativeLong i2);
        public float add_float(float f1, float f2);
        public float sub_float(float f1, float f2);
        public float mul_float(float f1, float f2);
        public float div_float(float f1, float f2);
        public double add_double(double f1, double f2);
        public double sub_double(double f1, double f2);
        public double mul_double(double f1, double f2);
        public double div_double(double f1, double f2);
    }
    
    public static void main(String[] args) {
        final Map<LibraryOption, ?> options = Collections.emptyMap();
        byte[].class.cast(null);
        TestLib lib = AsmLibraryLoader.getInstance().loadLibrary(new Library("test"), TestLib.class, options);
        byte result = lib.add_int8_t((byte) 1, (byte) 2);
        System.err.println("result=" + result);
    }
}
