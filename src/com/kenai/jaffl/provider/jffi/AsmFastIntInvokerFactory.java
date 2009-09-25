package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Pointer;
import java.util.concurrent.atomic.AtomicInteger;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

public class AsmFastIntInvokerFactory implements InvokerFactory, Opcodes {
    private static final AtomicInteger nextClassID = new AtomicInteger(0);
    
    private final static class SingletonHolder {
        static InvokerFactory INSTANCE = new AsmFastIntInvokerFactory();
    }
    public static final InvokerFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static abstract class BaseInvoker {
        public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
        protected final Function function;

        public BaseInvoker(Function function) {
            this.function = function;
        }
    }

    public final boolean isMethodSupported(Method method) {
        try {
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
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    final static boolean isFastIntResult(Class type) {
        return Void.class.isAssignableFrom(type) || void.class == type
                || Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
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

    public com.kenai.jaffl.provider.Invoker createInvoker(Method method, com.kenai.jaffl.provider.Library library, Map<LibraryOption, ?> options) {
        final long address = ((Library) library).findSymbolAddress(method.getName());

        Class returnType = method.getReturnType();
        Class[] paramTypes = method.getParameterTypes();
        com.kenai.jffi.Type[] nativeParamTypes = new com.kenai.jffi.Type[paramTypes.length];
        for (int i = 0; i < nativeParamTypes.length; ++i) {
            nativeParamTypes[i] = InvokerUtil.getNativeParameterType(paramTypes[i]);
        }

        Function function = new Function(address, InvokerUtil.getNativeReturnType(method),
                nativeParamTypes, CallingConvention.DEFAULT, InvokerUtil.requiresErrno(method));

        Class invokerClass = generateIntInvokerClass(AsmLoader.INSTANCE, returnType, method.getParameterTypes(),
                !InvokerUtil.requiresErrno(method));
        try {
            Constructor cons = invokerClass.getDeclaredConstructor(Function.class);
            return (com.kenai.jaffl.provider.Invoker) cons.newInstance(function);
        } catch (Throwable t) {
            return null;
        }
    }

    
    private static Class generateIntInvokerClass(AsmLoader loader, Class returnType, Class[] parameterTypes, boolean noErrno) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//        ClassVisitor cv = new CheckClassAdapter(new TraceClassVisitor(cw, new PrintWriter(System.out)));
        ClassVisitor cv = cw;        
        
        String className = Type.getInternalName(AsmFastIntInvokerFactory.class).replace("/", "_") 
                + "_FastIntInvoker_" + nextClassID.incrementAndGet();

        cv.visit(V1_5, ACC_PUBLIC, className, null, Type.getInternalName(BaseInvoker.class), 
                new String[] { Type.getInternalName(com.kenai.jaffl.provider.Invoker.class) });
        cv.visitField(ACC_PRIVATE | ACC_FINAL, "function", Type.getDescriptor(Function.class), null, null);

        // Create the constructor to set the 'function' field
        MethodVisitor init = cv.visitMethod(ACC_PUBLIC, "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Function.class) }), null, null);
        init.visitCode();
        init.visitVarInsn(ALOAD, 0);
        init.visitVarInsn(ALOAD, 1);
        init.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(BaseInvoker.class), "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { Type.getType(Function.class) }));

        init.visitInsn(RETURN);
        init.visitMaxs(2, 2);
        init.visitEnd();

        Class<? extends Number> nativeIntType = getNativeIntType(returnType, parameterTypes);

        // Now generate the Object invoke(Object[]) method
        MethodVisitor invoke = cv.visitMethod(ACC_PUBLIC, "invoke",
                Type.getMethodDescriptor(Type.getType(Object.class), new Type[] { Type.getType(Object[].class) }),
                null, null);
        invoke.visitCode();
        invoke.visitFieldInsn(GETSTATIC, Type.getInternalName(BaseInvoker.class), "ffi", Type.getDescriptor(com.kenai.jffi.Invoker.class));
        invoke.visitVarInsn(ALOAD, 0);
        invoke.visitFieldInsn(GETFIELD, Type.getInternalName(BaseInvoker.class), "function", Type.getDescriptor(Function.class));

        for (int i = 0; i < parameterTypes.length; ++i) {
            // pushes the first arg (the arg array) onto the stack
            invoke.visitVarInsn(ALOAD, 1);
            invoke.visitLdcInsn(i);
            invoke.visitInsn(AALOAD);

            emitParameterConversion(invoke, parameterTypes[i], nativeIntType);
        }

        Type[] paramTypes = new Type[parameterTypes.length + 1];
        paramTypes[0] = Type.getType(Function.class);
        for (int i = 0; i < parameterTypes.length; ++i) {
            paramTypes[i + 1] = Type.getType(nativeIntType);
        }

        // stack now contains [ IntInvoker, Function, int args ]
        invoke.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(com.kenai.jffi.Invoker.class), 
                getInvokerMethodName(parameterTypes.length, noErrno, nativeIntType),
                Type.getMethodDescriptor(Type.getType(nativeIntType), paramTypes));
        
        emitResultConversion(invoke, returnType, nativeIntType);

        invoke.visitInsn(ARETURN);
        invoke.visitMaxs(10, 10);
        invoke.visitEnd();

        cv.visitEnd();

        return loader.defineClass(className, cw.toByteArray());
    }

    private static final String getInvokerMethodName(int parameterCount, boolean ignoreErrno, Class nativeParamType) {
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

    private static final Class<? extends Number> getNativeIntType(Class returnType, Class[] parameterTypes) {

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (requiresLong(parameterTypes[i])) {
                return long.class;
            }
        }

        return requiresLong(returnType) ? long.class : int.class;
    }

    private static final void emitParameterConversion(MethodVisitor invoke, Class parameterType, Class nativeIntType) {
        if (Boolean.class.isAssignableFrom(parameterType) || boolean.class == parameterType) {
            invoke.visitTypeInsn(CHECKCAST, Type.getInternalName(Boolean.class));
            invoke.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Boolean.class), "booleanValue", "()Z");
        } else {
            invoke.visitTypeInsn(CHECKCAST, Type.getInternalName(Number.class));
            if (int.class == nativeIntType) {
                invoke.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Number.class), "intValue", "()I");
            } else {
                invoke.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(Number.class), "longValue", "()L");
            }
        }
    }
    
    private static final void emitResultConversion(MethodVisitor mv, Class returnType, Class nativeParamType) {
        Class primitiveClass, objClass;
        if (Byte.class.isAssignableFrom(returnType) || byte.class == returnType) {
            if (long.class == nativeParamType) mv.visitInsn(L2I);
            mv.visitInsn(I2B);
            primitiveClass = byte.class;
            objClass = Byte.class;
        } else if (Short.class.isAssignableFrom(returnType) || short.class == returnType) {
            if (long.class == nativeParamType) mv.visitInsn(L2I);
            mv.visitInsn(I2S);
            primitiveClass = short.class;
            objClass = Short.class;
        } else if (Long.class.isAssignableFrom(returnType)  || long.class == returnType) {
            if (long.class != nativeParamType) mv.visitInsn(I2L);
            primitiveClass = long.class;
            objClass = Long.class;
        } else if (NativeLong.class.isAssignableFrom(returnType)) {
            if (long.class != nativeParamType) mv.visitInsn(I2L);
            primitiveClass = long.class;
            objClass = NativeLong.class;
        } else if (Integer.class.isAssignableFrom(returnType) || int.class == returnType) {
            if (long.class == nativeParamType) mv.visitInsn(L2I);
            primitiveClass = int.class;
            objClass = Integer.class;
        } else if (Boolean.class.isAssignableFrom(returnType) || boolean.class == returnType) {
            if (long.class == nativeParamType) mv.visitInsn(L2I);
            primitiveClass = boolean.class;
            objClass = Boolean.class;
        } else if (Pointer.class.isAssignableFrom(returnType)) {
            primitiveClass = nativeParamType;
            objClass = JFFIPointer.class;
        } else {
            throw new IllegalArgumentException("invalid return type");
        }

        mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(objClass), "valueOf",
                Type.getMethodDescriptor(Type.getType(objClass), new Type[] { Type.getType(primitiveClass) }));
    }

    static final class AsmLoader extends ClassLoader {
        static final AsmLoader INSTANCE = new AsmLoader();

        public Class defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
