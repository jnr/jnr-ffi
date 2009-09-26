
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Pointer;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import com.kenai.jffi.Platform;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

class AsmLibraryLoader extends LibraryLoader implements Opcodes {
    private static final LibraryLoader INSTANCE = new AsmLibraryLoader();
    private final AtomicLong nextClassID = new AtomicLong(0);
    private final AtomicLong nextIvarID = new AtomicLong(0);

    static final LibraryLoader getInstance() {
        return INSTANCE;
    }

    boolean isInterfaceSupported(Class interfaceClass) {
        return true;
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
        MethodVisitor init = cv.visitMethod(ACC_PUBLIC, "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Library.class), Type.getType(Function[].class) }),
                null, null);
        init.visitCode();
        // Invokes the super class constructor as super(Library)

        init.visitVarInsn(ALOAD, 0);
        init.visitVarInsn(ALOAD, 1);

        init.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(AbstractNativeInterface.class), "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Library.class) }));
        
        final Method[] methods = interfaceClass.getDeclaredMethods();
        Function[] functions = new Function[methods.length];
        init.visitVarInsn(ALOAD, 0);
        init.visitVarInsn(ALOAD, 2);

        for (int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            functions[i] = getFunction(m, library.findSymbolAddress(m.getName()));
            String functionFieldName = "function_" + i;

            cv.visitField(ACC_PRIVATE | ACC_FINAL, functionFieldName, Type.getDescriptor(Function.class), null, null);

            generateMethod(cv, className, functionFieldName, m);

            // The Function[] array is passed in as the second param, so generate
            // the constructor code to store each function in a field
            init.visitInsn(DUP2);
            init.visitIntInsn(SIPUSH, i);
            init.visitInsn(AALOAD);
            init.visitFieldInsn(PUTFIELD, className, functionFieldName, Type.getDescriptor(Function.class));
        }

        init.visitInsn(RETURN);
        init.visitMaxs(10, 10);
        init.visitEnd();

        cv.visitEnd();

        try {
            Class implClass = AsmLoader.INSTANCE.defineClass(className, cw.toByteArray());
            Constructor<T> cons = implClass.getDeclaredConstructor(Library.class, Function[].class);
            System.out.println("constructor=" + cons);
            return cons.newInstance(library, functions);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private final void generateMethod(ClassVisitor cv, String className, String functionFieldName, Method m) {

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_FINAL, m.getName(), Type.getMethodDescriptor(m), null, null);
        mv.visitCode();

        final Class returnType = m.getReturnType();
        final Class[] parameterTypes = m.getParameterTypes();

        // Retrieve the static 'ffi' Invoker instance
        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(AbstractNativeInterface.class), "ffi", Type.getDescriptor(com.kenai.jffi.Invoker.class));

        // retrieve this.function
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, className, functionFieldName, Type.getDescriptor(Function.class));

        if (isFastIntMethod(m)) {
            generateFastIntInvocation(mv, returnType, parameterTypes);
        } else {
            generateBufferInvocation(mv, returnType, parameterTypes);
        }
        
        mv.visitMaxs(10, 10);
        mv.visitEnd();
    }

    private final void generateBufferInvocation(MethodVisitor mv, Class returnType, Class[] parameterTypes) {
        // [ stack contains: Invoker, Function ]
        // new HeapInvocationBuffer(function)
//        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, Type.getInternalName(HeapInvocationBuffer.class));
        // [ Invoker, Function, Buffer ] => [ Invoker, Function, Buffer, Function, Buffer ]
        mv.visitInsn(DUP2);
        // [ Invoker, Function, Buffer, Function, Buffer ] => [ Invoker, Function, Buffer, Buffer, Function ]
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(HeapInvocationBuffer.class), "<init>",
                Type.getMethodDescriptor(Type.getType(void.class), new Type[] { Type.getType(Function.class) }));
        

        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.visitInsn(DUP); // dup ref to HeapInvocationBuffer
            if (isPrimitiveInt(parameterTypes[i]) || boolean.class == parameterTypes[i]) {
                mv.visitVarInsn(ILOAD, lvar++);
            } else if (long.class == parameterTypes[i]) {
                mv.visitVarInsn(LLOAD, lvar); lvar += 2;
            } else if (float.class == parameterTypes[i]) {
                mv.visitVarInsn(FLOAD, lvar++);
            } else if (double.class == parameterTypes[i]) {
                mv.visitVarInsn(DLOAD, lvar); lvar += 2;
            } else if (Number.class.isAssignableFrom(parameterTypes[i])) {
                mv.visitVarInsn(ALOAD, lvar++);
                unboxIntParameter(mv, parameterTypes[i], null);
            } else {
                throw new IllegalArgumentException("unsupported parameter type " + parameterTypes[i]);
            }

            emitInvocationBufferParameter(mv, parameterTypes[i]);
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
        } else if (Float.class == returnType || float.class == returnType) {
            invokeMethod = "invokeFloat";
            nativeReturnType = float.class;
        } else if (Double.class == returnType || double.class == returnType) {
            invokeMethod = "invokeDouble";
            nativeReturnType = double.class;
        } else {
            throw new IllegalArgumentException("unsupported return type " + returnType);
        }
        
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(com.kenai.jffi.Invoker.class), invokeMethod,
                Type.getMethodDescriptor(Type.getType(nativeReturnType), new Type[] { Type.getType(Function.class), Type.getType(HeapInvocationBuffer.class) }));

        if (!returnType.isPrimitive()) {
            boxIntReturnValue(mv, returnType, nativeReturnType);
            mv.visitInsn(ARETURN);
        } else {
            emitReturnOp(mv, returnType);
        }
    }

    private final void generateFastIntInvocation(MethodVisitor mv, Class returnType, Class[] parameterTypes) {
        // [ stack contains: Invoker, Function ]

        Class nativeIntType = getNativeIntType(returnType, parameterTypes);

        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (isPrimitiveInt(parameterTypes[i]) || boolean.class == parameterTypes[i]) {
                mv.visitVarInsn(ILOAD, lvar++);
                if (nativeIntType == long.class) {
                    mv.visitInsn(I2L); // sign extend to long
                }
            } else if (long.class == parameterTypes[i]) {
                mv.visitVarInsn(LLOAD, lvar); lvar += 2;
            } else if (Number.class.isAssignableFrom(parameterTypes[i])) {
                mv.visitVarInsn(ALOAD, lvar++);
                unboxIntParameter(mv, parameterTypes[i], nativeIntType);
            }
        }

        Type[] paramTypes = new Type[parameterTypes.length + 1];
        paramTypes[0] = Type.getType(Function.class);
        for (int i = 0; i < parameterTypes.length; ++i) {
            paramTypes[i + 1] = Type.getType(nativeIntType);
        }

        // stack now contains [ IntInvoker, Function, int args ]
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(com.kenai.jffi.Invoker.class),
                getFastIntInvokerMethodName(parameterTypes.length, false, nativeIntType),
                Type.getMethodDescriptor(Type.getType(nativeIntType), paramTypes));

        if (isPrimitiveInt(returnType) || boolean.class == returnType) {
            if (int.class != nativeIntType) mv.visitInsn(L2I);
            mv.visitInsn(IRETURN);
        } else if (long.class == returnType) {
            if (long.class != nativeIntType) mv.visitInsn(I2L);
            mv.visitInsn(LRETURN);
        } else if (void.class == returnType) {
            mv.visitInsn(RETURN);
        } else {
            boxIntReturnValue(mv, returnType, nativeIntType);
            mv.visitInsn(ARETURN);
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

    private final void boxIntReturnValue(MethodVisitor mv, Class returnType, Class nativeReturnType) {
        Class primitiveClass, objClass;
        if (Byte.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.visitInsn(L2I);
            mv.visitInsn(I2B);
            primitiveClass = byte.class;
            objClass = Byte.class;

        } else if (Short.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.visitInsn(L2I);
            mv.visitInsn(I2S);
            primitiveClass = short.class;
            objClass = Short.class;

        } else if (Integer.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.visitInsn(L2I);
            primitiveClass = int.class;
            objClass = Integer.class;


        } else if (Long.class.isAssignableFrom(returnType)) {
            if (long.class != nativeReturnType) mv.visitInsn(I2L);
            primitiveClass = long.class;
            objClass = Long.class;

        } else if (NativeLong.class.isAssignableFrom(returnType)) {
            if (long.class != nativeReturnType) mv.visitInsn(I2L);
            primitiveClass = long.class;
            objClass = NativeLong.class;

        } else if (Boolean.class.isAssignableFrom(returnType)) {
            if (long.class == nativeReturnType) mv.visitInsn(L2I);
            primitiveClass = boolean.class;
            objClass = Boolean.class;

        } else if (Pointer.class.isAssignableFrom(returnType)) {
            primitiveClass = nativeReturnType;
            objClass = JFFIPointer.class;
        } else {
            throw new IllegalArgumentException("invalid return type");
        }

        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(objClass), "valueOf",
                Type.getMethodDescriptor(Type.getType(objClass), new Type[] { Type.getType(primitiveClass) }));
    }

    private final void unboxIntParameter(final MethodVisitor mv, final Class parameterType, final Class nativeIntType) {
        String intValueMethod = long.class == nativeIntType ? "longValue" : "intValue";
        String intValueSignature = long.class == nativeIntType ? "()J" : "()I";

        if (Byte.class == parameterType || Short.class == parameterType || Integer.class == parameterType) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(parameterType), intValueMethod, intValueSignature);
        } else if (Long.class == parameterType) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(parameterType), "longValue", "()J");
        } else if (Float.class == parameterType) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(parameterType), "floatValue", "()F");
        } else if (Double.class == parameterType) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(parameterType), "doubleValue", "()D");
        } else if (NativeLong.class.isAssignableFrom(parameterType) && nativeIntType != null) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(parameterType), intValueMethod, intValueSignature);
        } else if (NativeLong.class.isAssignableFrom(parameterType) && Platform.getPlatform().longSize() == 32) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(parameterType), "intValue", "()I");
        } else if (NativeLong.class.isAssignableFrom(parameterType) && Platform.getPlatform().longSize() == 64) {
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(parameterType), "longValue", "()J");
        } else {
            throw new IllegalArgumentException("unsupported Number subclass");
        }
    }
    
    private final void emitReturnOp(MethodVisitor mv, Class returnType) {
        if (isPrimitiveInt(returnType) || boolean.class == returnType) {
            mv.visitInsn(IRETURN);
        } else if (long.class == returnType) {
            mv.visitInsn(LRETURN);
        } else if (float.class == returnType) {
            mv.visitInsn(FRETURN);
        } else if (double.class == returnType) {
            mv.visitInsn(DRETURN);
        } else if (void.class == returnType) {
            mv.visitInsn(RETURN);
        } else {
            mv.visitInsn(ARETURN);
        }
    }
    private final void emitNullReturn(MethodVisitor mv, Class returnType) {
        if (isPrimitiveInt(returnType) || boolean.class == returnType) {
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
        } else if (long.class == returnType) {
            mv.visitInsn(LCONST_0);
            mv.visitInsn(LRETURN);
        } else if (float.class == returnType) {
            mv.visitInsn(FCONST_0);
            mv.visitInsn(FRETURN);
        } else if (double.class == returnType) {
            mv.visitInsn(DCONST_0);
            mv.visitInsn(DRETURN);
        } else if (void.class == returnType) {
            mv.visitInsn(RETURN);
        } else {
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        }
    }

    private final void emitInvocationBufferParameter(final MethodVisitor mv, final Class parameterType) {
        String paramMethod = null;
        Class paramClass = int.class;
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
        mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(HeapInvocationBuffer.class), paramMethod,
                Type.getMethodDescriptor(Type.getType(void.class), new Type[] { Type.getType(paramClass) }));
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
    private static boolean isPrimitive(Class c) {
        return byte.class == c || short.class == c || int.class == c
                || long.class == c || float.class == c || double.class == c
                || boolean.class == c;
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

    public static interface TestLib {
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
        TestLib lib = AsmLibraryLoader.getInstance().loadLibrary(new Library("test"), TestLib.class, options);
        byte result = lib.add_int8_t((byte) 1, (byte) 2);
        System.err.println("result=" + result);
    }
}
