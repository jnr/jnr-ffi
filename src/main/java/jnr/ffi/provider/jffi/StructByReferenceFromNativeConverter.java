package jnr.ffi.provider.jffi;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static org.objectweb.asm.Opcodes.*;

@FromNativeConverter.NoContext
abstract public class StructByReferenceFromNativeConverter implements FromNativeConverter<Struct, Pointer> {
    private final int flags;

    protected StructByReferenceFromNativeConverter(int flags) {
        this.flags = flags;
    }

    public final Class<Pointer> nativeType() {
        return Pointer.class;
    }

    // getRuntime() is called from generated code
    protected final jnr.ffi.Runtime getRuntime() {
        return NativeRuntime.getInstance();
    }

    static final Map<Class<? extends Struct>, Class<? extends StructByReferenceFromNativeConverter>> converterClasses
            = new ConcurrentHashMap<Class<? extends Struct>, Class<? extends StructByReferenceFromNativeConverter>>();
    static StructByReferenceFromNativeConverter newStructByReferenceConverter(Class<? extends Struct> structClass, int flags) {
        Class<? extends StructByReferenceFromNativeConverter> converterClass = converterClasses.get(structClass);
        if (converterClass == null) {
            synchronized (converterClasses) {
                if ((converterClass = converterClasses.get(structClass)) == null) {
                    converterClasses.put(structClass, converterClass = newStructByReferenceClass(structClass));
                }
            }
        }
        try {
            return converterClass.getConstructor(int.class).newInstance(flags);

        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (InstantiationException ie) {
            throw new RuntimeException(ie);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(ite);
        }
    }

    private static final AtomicLong nextClassID = new AtomicLong(0);

    static Class<? extends StructByReferenceFromNativeConverter> newStructByReferenceClass(Class<? extends Struct> structClass) {

        try {
            Constructor<? extends Struct> cons = structClass.asSubclass(Struct.class).getConstructor(jnr.ffi.Runtime.class);
            if (!Modifier.isPublic(cons.getModifiers())) {
                throw new RuntimeException(structClass.getName() + " constructor is not public");
            }
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("struct subclass " + structClass.getName() + " has no constructor that takes a "
                    + jnr.ffi.Runtime.class.getName(), ex);
        }


        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = AsmLibraryLoader.DEBUG ? AsmUtil.newCheckClassAdapter(cw) : cw;

        final String className = p(structClass) + "$jnr$fromNativeConverter$" + nextClassID.getAndIncrement();

        cv.visit(V1_5, ACC_PUBLIC | ACC_FINAL, className, null, p(StructByReferenceFromNativeConverter.class),
                new String[0]);

        cv.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);

        // Create the constructor to set the instance fields
        SkinnyMethodAdapter init = new SkinnyMethodAdapter(cv, ACC_PUBLIC, "<init>", sig(void.class, int.class), null, null);
        init.start();
        // Invoke the super class constructor as super(Library)
        init.aload(0);
        init.iload(1);
        init.invokespecial(p(StructByReferenceFromNativeConverter.class), "<init>", sig(void.class, int.class));
        init.voidreturn();
        init.visitMaxs(10, 10);
        init.visitEnd();

        SkinnyMethodAdapter fromNative = new SkinnyMethodAdapter(cv, ACC_PUBLIC | ACC_FINAL, "fromNative",
                sig(structClass, Pointer.class, FromNativeContext.class), null, null);

        fromNative.start();
        Label nullPointer = new Label();
        fromNative.aload(1);
        fromNative.ifnull(nullPointer);

        // Create an instance of the struct subclass
        fromNative.newobj(p(structClass));
        fromNative.dup();
        fromNative.aload(0);
        fromNative.invokevirtual(p(StructByReferenceFromNativeConverter.class), "getRuntime", sig(Runtime.class));
        fromNative.invokespecial(structClass, "<init>", void.class, jnr.ffi.Runtime.class);

        // associate the memory with the struct and return the struct
        fromNative.dup();
        fromNative.aload(1);
        fromNative.invokevirtual(structClass, "useMemory", void.class, Pointer.class);
        fromNative.areturn();

        fromNative.label(nullPointer);
        fromNative.aconst_null();
        fromNative.areturn();

        fromNative.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);
        fromNative.visitMaxs(10, 10);
        fromNative.visitEnd();

        fromNative = new SkinnyMethodAdapter(cv, ACC_PUBLIC | ACC_FINAL, "fromNative",
                sig(Object.class, Object.class, FromNativeContext.class), null, null);
        fromNative.start();
        fromNative.aload(0);
        fromNative.aload(1);
        fromNative.checkcast(Pointer.class);
        fromNative.aload(2);
        fromNative.invokevirtual(className, "fromNative", sig(structClass, Pointer.class, FromNativeContext.class));
        fromNative.areturn();
        fromNative.visitAnnotation(ci(FromNativeConverter.NoContext.class), true);
        fromNative.visitMaxs(10, 10);
        fromNative.visitEnd();
        cv.visitEnd();

        try {
            byte[] bytes = cw.toByteArray();
            if (AsmLibraryLoader.DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(bytes).accept(trace, 0);
            }

            return AsmLibraryLoader.getCurrentClassLoader().defineClass(className.replace("/", "."), bytes);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
