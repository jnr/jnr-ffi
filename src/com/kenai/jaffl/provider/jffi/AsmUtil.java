
package com.kenai.jaffl.provider.jffi;

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
}
