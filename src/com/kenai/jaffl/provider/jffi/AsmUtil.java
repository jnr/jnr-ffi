
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Platform;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.struct.Struct;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

class AsmUtil {
    private AsmUtil() {}
    
    public static final MethodVisitor newTraceMethodVisitor(MethodVisitor mv) {
        try {
            Class tmvClass = Class.forName("org.objectweb.asm.util.TraceMethodVisitor");
            Constructor c = tmvClass.getDeclaredConstructor(MethodVisitor.class);
            return (MethodVisitor) c.newInstance(mv);
        } catch (Throwable t) {
            return mv;
        }
    }

    public static final ClassVisitor newTraceClassVisitor(ClassVisitor cv, OutputStream out) {
        return newTraceClassVisitor(cv, new PrintWriter(out, true));
    }

    public static final ClassVisitor newTraceClassVisitor(ClassVisitor cv, PrintWriter out) {
        try {

            Class tmvClass = Class.forName("org.objectweb.asm.util.TraceClassVisitor");
            Constructor c = tmvClass.getDeclaredConstructor(ClassVisitor.class, PrintWriter.class);
            return (ClassVisitor) c.newInstance(cv, out);
        } catch (Throwable t) {
            return cv;
        }
    }

    public static final ClassVisitor newCheckClassAdapter(ClassVisitor cv) {
        try {
            Class tmvClass = Class.forName("org.objectweb.asm.util.CheckClassAdapter");
            Constructor c = tmvClass.getDeclaredConstructor(ClassVisitor.class);
            return (ClassVisitor) c.newInstance(cv);
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
}
