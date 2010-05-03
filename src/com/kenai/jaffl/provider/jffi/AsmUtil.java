
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.struct.Struct;
import com.kenai.jffi.Platform;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import static com.kenai.jaffl.provider.jffi.NumberUtil.*;
import static com.kenai.jaffl.provider.jffi.CodegenUtils.*;

class AsmUtil {
    private AsmUtil() {}
    
    public static final MethodVisitor newTraceMethodVisitor(MethodVisitor mv) {
        try {
            Class<? extends MethodVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceMethodVisitor").asSubclass(MethodVisitor.class);
            Constructor<? extends MethodVisitor> c = tmvClass.getDeclaredConstructor(MethodVisitor.class);
            return c.newInstance(mv);
        } catch (Throwable t) {
            return mv;
        }
    }

    public static final ClassVisitor newTraceClassVisitor(ClassVisitor cv, OutputStream out) {
        return newTraceClassVisitor(cv, new PrintWriter(out, true));
    }

    public static final ClassVisitor newTraceClassVisitor(ClassVisitor cv, PrintWriter out) {
        try {

            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class, PrintWriter.class);
            return c.newInstance(cv, out);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static final ClassVisitor newCheckClassAdapter(ClassVisitor cv) {
        try {
            Class<? extends ClassVisitor> tmvClass = Class.forName("org.objectweb.asm.util.CheckClassAdapter").asSubclass(ClassVisitor.class);
            Constructor<? extends ClassVisitor> c = tmvClass.getDeclaredConstructor(ClassVisitor.class);
            return c.newInstance(cv);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static final Class unboxedReturnType(Class type) {
        if (Pointer.class.isAssignableFrom(type)
            || Struct.class.isAssignableFrom(type)
            || String.class.isAssignableFrom(type)) {
            return Platform.getPlatform().longSize() == 32 ? int.class : long.class;
        }
        
        return unboxedType(type);
    }

    public static final Class unboxedType(Class boxedType) {
        if (boxedType == Byte.class) {
            return byte.class;
        } else if (boxedType == Short.class) {
            return short.class;
        } else if (boxedType == Integer.class) {
            return int.class;
        } else if (boxedType == Long.class) {
            return long.class;
        } else if (boxedType == Float.class) {
            return float.class;
        } else if (boxedType == Double.class) {
            return double.class;
        } else if (boxedType == Boolean.class) {
            return boolean.class;
        } else if (boxedType == NativeLong.class) {
            return Platform.getPlatform().longSize() == 32 ? int.class : long.class;
        } else if (Pointer.class.isAssignableFrom(boxedType) || Struct.class.isAssignableFrom(boxedType)) {
            return Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else if (Enum.class.isAssignableFrom(boxedType)) {
            return int.class;
        
        } else {
            return boxedType;
        }
    }

    public static final Class boxedType(Class type) {
        if (type == byte.class) {
            return Byte.class;
        } else if (type == short.class) {
            return Short.class;
        } else if (type == int.class) {
            return Integer.class;
        } else if (type == long.class) {
            return Long.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == boolean.class) {
            return Boolean.class;
        } else {
            return type;
        }
    }

    
    static final void emitReturnOp(SkinnyMethodAdapter mv, Class returnType) {
        if (!returnType.isPrimitive()) {
            mv.areturn();
        } else if (long.class == returnType) {
            mv.lreturn();
        } else if (float.class == returnType) {
            mv.freturn();
        } else if (double.class == returnType) {
            mv.dreturn();
        } else if (void.class == returnType) {
            mv.voidreturn();
        } else {
            mv.ireturn();
        }
    }

    /**
     * Calculates the size of a local variable
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static final int calculateLocalVariableSpace(Class type) {
        return long.class == type || double.class == type ? 2 : 1;
    }

    /**
     * Calculates the size of a list of types in the local variable area.
     *
     * @param type The type of parameter
     * @return The size in parameter units
     */
    static final int calculateLocalVariableSpace(Class... types) {
        int size = 0;

        for (int i = 0; i < types.length; ++i) {
            size += calculateLocalVariableSpace(types[i]);
        }

        return size;
    }

    private static final void unboxPointerOrStruct(final SkinnyMethodAdapter mv, final Class type, final Class nativeType) {
        mv.invokestatic(p(AsmRuntime.class), long.class == nativeType ? "longValue" : "intValue",
            sig(nativeType, type));
    }

    static final void unboxPointer(final SkinnyMethodAdapter mv, final Class nativeType) {
        unboxPointerOrStruct(mv, Pointer.class, nativeType);
    }

    static final void unboxStruct(final SkinnyMethodAdapter mv, final Class nativeType) {
        unboxPointerOrStruct(mv, Struct.class, nativeType);
    }

    static final void unboxEnum(final SkinnyMethodAdapter mv, final Class nativeType) {
        mv.invokestatic(p(AsmRuntime.class), long.class == nativeType ? "longValue" : "intValue",
            sig(nativeType, Enum.class));
    }

    static final void unboxNumber(final SkinnyMethodAdapter mv, final Class boxedType, final Class nativeType) {
        String intValueMethod = long.class == nativeType ? "longValue" : "intValue";
        String intValueSignature = long.class == nativeType ? "()J" : "()I";

        if (Byte.class == boxedType || Short.class == boxedType || Integer.class == boxedType) {
            mv.invokevirtual(p(boxedType), intValueMethod, intValueSignature);

        } else if (Long.class == boxedType) {
            mv.invokevirtual(p(boxedType), "longValue", "()J");

        } else if (Float.class == boxedType) {
            mv.invokevirtual(p(boxedType), "floatValue", "()F");

        } else if (Double.class == boxedType) {
            mv.invokevirtual(p(boxedType), "doubleValue", "()D");

        } else if (NativeLong.class.isAssignableFrom(boxedType)) {
            mv.invokevirtual(p(boxedType), intValueMethod, intValueSignature);
        
        } else if (Boolean.class.isAssignableFrom(boxedType)) {
            mv.invokevirtual(p(boxedType), "booleanValue", "()Z");
            widen(mv, boolean.class, nativeType);

        } else if (Enum.class.isAssignableFrom(boxedType)) {
            unboxEnum(mv, nativeType);

        } else {
            throw new IllegalArgumentException("unsupported Number subclass: " + boxedType);
        }
    }
}
