package jnr.ffi.provider.jffi;

import jnr.ffi.Closure;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.ffi.provider.jffi.NumberUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
public abstract class NativeClosureFactory<T extends Closure> {
    public final static boolean DEBUG = true || Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);

    protected final NativeRuntime runtime;
    protected final Class<? extends T> closureClass;
    protected NativeClosureFactory(NativeRuntime runtime, Class<? extends T> closureClass) {
        this.runtime = runtime;
        this.closureClass = closureClass;
    }

    abstract public T newClosure(T instance);

    static NativeClosureFactory newClosureFactory(NativeRuntime runtime, Class<? extends Closure> closureClass) {
        ClassWriter factoryClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor factoryClassVisitor = DEBUG ? AsmUtil.newCheckClassAdapter(factoryClassWriter) : factoryClassWriter;
        final long classIdx = nextClassID.getAndIncrement();

        String factoryClassName = p(NativeClosureFactory.class) + '$' + closureClass.getName().replace('.', '$')
                + '$' + classIdx;

        factoryClassVisitor.visit(V1_5, ACC_PUBLIC | ACC_FINAL, factoryClassName, null, p(NativeClosureFactory.class),
                new String[]{});

        final String closureInstanceClassName = factoryClassName + "$ClosureInstance";
        final ClassWriter closureClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor closureClassVisitor = DEBUG ? AsmUtil.newCheckClassAdapter(closureClassWriter) : closureClassWriter;

        closureClassVisitor.visit(V1_5, ACC_PUBLIC | ACC_FINAL, closureInstanceClassName, null, p(NativeClosure.class),
                        new String[]{ p(closureClass), p(com.kenai.jffi.Closure.class) });

        SkinnyMethodAdapter closureInit = new SkinnyMethodAdapter(closureClassVisitor.visitMethod(ACC_PUBLIC, "<init>",
               sig(void.class, NativeRuntime.class, closureClass),
               null, null));
        closureClassVisitor.visitField(ACC_PRIVATE | ACC_FINAL, "closure", ci(closureClass), null, null);
        closureInit.start();
        closureInit.aload(0);
        closureInit.aload(1);
        closureInit.aload(2);

        closureInit.invokespecial(p(NativeClosure.class), "<init>", sig(void.class, NativeRuntime.class, Closure.class));

        closureInit.aload(0);
        closureInit.aload(2);
        closureInit.putfield(closureInstanceClassName, "closure", ci(closureClass));
        closureInit.voidreturn();
        closureInit.visitMaxs(10, 10);
        closureInit.visitEnd();


        Method callMethod = null;
        for (Method m : closureClass.getDeclaredMethods()) {
            if (m.getName().equals("call")) {
                callMethod = m;
                break;
            }
        }
        if (callMethod == null) {
            throw new NoSuchMethodError("no call method defined in " + closureClass.getName());
        }

        SkinnyMethodAdapter closureInvoke = new SkinnyMethodAdapter(closureClassVisitor.visitMethod(ACC_PUBLIC, "invoke",
                       sig(void.class, com.kenai.jffi.Closure.Buffer.class, Closure.class),
                       null, null));
        closureInvoke.start();

        if (void.class != callMethod.getReturnType() && Void.class != callMethod.getReturnType()) {
            // If the Closure returns a value, push the Closure.Buffer on the stack
            // for the call to Closure.Buffer#set<Foo>Return()
            closureInvoke.aload(1);
        }

        // Cast the Closure instance to the Closure subclass
        closureInvoke.aload(2);
        closureInvoke.checkcast(p(closureClass));

        // Construct callback method
        Class[] parameterTypes = callMethod.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class parameterType = parameterTypes[i];
            if (!isParameterTypeSupported(parameterType)) {
                throw new IllegalArgumentException("unsupported closure parameter type " + parameterType);
            }

            // Load the Closure.Buffer for the parameter set call
            closureInvoke.aload(1);

            // Load the parameter index
            closureInvoke.pushInt(i);

            Class type = parameterType.isPrimitive() ? parameterType : AsmUtil.unboxedType(parameterType);

            if (byte.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getByte", sig(type, int.class));
            } else if (char.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getShort", sig(short.class, int.class));

            } else if (short.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getShort", sig(type, int.class));

            } else if (int.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getInt", sig(type, int.class));

            } else if (long.class == type) {
                if (AsmLibraryLoader.isLong32(parameterType, callMethod.getParameterAnnotations()[i])) {
                    closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getInt", sig(int.class, int.class));
                    widen(closureInvoke, int.class, long.class);
                } else {
                    closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getLong", sig(long.class, int.class));
                }
            } else if (float.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getFloat", sig(type, int.class));

            } else if (double.class == type) {
                closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "getDouble", sig(type, int.class));

            } else {
                throw new IllegalArgumentException("unsupported closure parameter type " + parameterType);
            }

            if (type != parameterType) {
                AsmUtil.boxValue(closureInvoke, parameterType, type);
            }
        }
        closureInvoke.invokeinterface(p(closureClass), callMethod.getName(), sig(callMethod.getReturnType(), callMethod.getParameterTypes()));

        Class returnType = callMethod.getReturnType();
        if (!isReturnTypeSupported(returnType)) {
            throw new IllegalArgumentException("unsupported closure return type " + returnType.getName());
        }
        Annotation[] returnAnnotations = callMethod.getAnnotations();
        Class nativeReturnType = AsmUtil.unboxedType(returnType);

        if (AsmLibraryLoader.isLong32(returnType, returnAnnotations)) {
            nativeReturnType = int.class;
        }

        if (Number.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxNumber(closureInvoke, returnType, nativeReturnType);

        } else if (Boolean.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxBoolean(closureInvoke, nativeReturnType);

        } else if (Pointer.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxPointer(closureInvoke, nativeReturnType);

        } else if (Enum.class.isAssignableFrom(returnType)) {
            AsmUtil.unboxEnum(closureInvoke, nativeReturnType);
        }

        if (void.class == nativeReturnType || Void.class == nativeReturnType) {
            // No return value to set, so no call to set<Foo>Return()

        } else if (byte.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setByteReturn", sig(void.class, byte.class));

        } else if (short.class == nativeReturnType || char.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setShortReturn", sig(void.class, short.class));

        } else if (int.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setIntReturn", sig(void.class, int.class));

        } else if (long.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setLongReturn", sig(void.class, long.class));

        } else if (float.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setFloatReturn", sig(void.class, float.class));

        } else if (double.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setDoubleReturn", sig(void.class, double.class));

        } else if (boolean.class == nativeReturnType) {
            closureInvoke.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "setIntReturn", sig(void.class, int.class));
        }

        closureInvoke.voidreturn();
        closureInvoke.visitMaxs(10, 10);
        closureInvoke.visitEnd();

        closureClassVisitor.visitEnd();

        // Create the constructor to set the 'library' & functions fields
        SkinnyMethodAdapter factoryInit = new SkinnyMethodAdapter(factoryClassVisitor.visitMethod(ACC_PUBLIC, "<init>",
               sig(void.class, NativeRuntime.class, Class.class),
               null, null));
        factoryInit.start();

        factoryInit.aload(0);
        factoryInit.aload(1);
        factoryInit.aload(2);

        factoryInit.invokespecial(p(NativeClosureFactory.class), "<init>", sig(void.class, NativeRuntime.class, Class.class));

        factoryInit.voidreturn();
        factoryInit.visitMaxs(10, 10);
        factoryInit.visitEnd();



        // Generate the 'newClosure' method
        SkinnyMethodAdapter newClosure = new SkinnyMethodAdapter(factoryClassVisitor.visitMethod(ACC_PUBLIC | ACC_FINAL, "newClosure",
                sig(Closure.class, Closure.class), null, null));
        newClosure.start();
        newClosure.newobj(p(closureInstanceClassName));
        newClosure.dup();
        newClosure.aload(0);
        newClosure.getfield(factoryClassName, "runtime", ci(NativeRuntime.class));
        newClosure.aload(1);
        newClosure.checkcast(p(closureClass));
        newClosure.invokespecial(p(closureInstanceClassName), "<init>", sig(void.class, NativeRuntime.class, closureClass));
        newClosure.areturn();
        newClosure.visitMaxs(10, 10);
        newClosure.visitEnd();

        factoryClassVisitor.visitEnd();

        try {
            byte[] factoryImpBytes = factoryClassWriter.toByteArray();
            byte[] closureImpBytes = closureClassWriter.toByteArray();
            System.out.println("debug=" + DEBUG);
            if (DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(closureImpBytes).accept(trace, 0);
                //trace.visitEnd();
                trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(factoryImpBytes).accept(trace, 0);
                //trace.visitEnd();
                System.err.flush();
            }
            ClassLoader cl = NativeClosureFactory.class.getClassLoader();
            if (cl == null) {
                cl = Thread.currentThread().getContextClassLoader();
            }
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            AsmClassLoader asm = new AsmClassLoader(cl);
            asm.defineClass(c(closureInstanceClassName), closureImpBytes);
            Class<? extends NativeClosureFactory> factoryImpClass = asm.defineClass(c(factoryClassName), factoryImpBytes);
            Constructor<? extends NativeClosureFactory> cons = factoryImpClass.getDeclaredConstructor(NativeRuntime.class, Class.class);
            NativeClosureFactory result = cons.newInstance(runtime, closureClass);

            return result;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isReturnTypeSupported(Class type) {
        return type.isPrimitive()
                || boolean.class == type || Boolean.class == type
                || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
                || Enum.class.isAssignableFrom(type)
                || Pointer.class == type
                ;
    }

    private static boolean isParameterTypeSupported(Class type) {
        return type.isPrimitive()
                || boolean.class == type || Boolean.class == type
                || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
                || Pointer.class == type
                || String.class == type
                /*
                || CharSequence.class == type
                || Buffer.class.isAssignableFrom(type)
                || Struct.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)
                || (type.isArray() && type.getComponentType().isPrimitive())
                || (type.isArray() && Struct.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && Pointer.class.isAssignableFrom(type.getComponentType()))
                || (type.isArray() && CharSequence.class.isAssignableFrom(type.getComponentType()))
                || ByReference.class.isAssignableFrom(type)
                */
                ;
    }
    public static interface MyClosure extends Closure {
        public void call(Byte b, char c, int i, Long l, Float f, Double d, Pointer p);
    }

    private static final class MyBuffer implements com.kenai.jffi.Closure.Buffer {
        public byte getByte(int i) {
            return (byte) 1;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public short getShort(int i) {
            return (short) 2;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int getInt(int i) {
            return 3;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public long getLong(int i) {
            return 4;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public float getFloat(int i) {
            return 5;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public double getDouble(int i) {
            return 6;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public long getAddress(int i) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public long getStruct(int i) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setByteReturn(byte b) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setShortReturn(short i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setIntReturn(int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setLongReturn(long l) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setFloatReturn(float v) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setDoubleReturn(double v) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setAddressReturn(long l) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setStructReturn(long l) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setStructReturn(byte[] bytes, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
    public static void main(String[] args) {
        NativeRuntime runtime = NativeRuntime.getInstance();
        NativeClosureFactory f = NativeClosureFactory.newClosureFactory(runtime, MyClosure.class);
        System.out.println("factory class=" + f.getClass());
        MyClosure closure = (MyClosure) f.newClosure(new MyClosure() {
            public void call(Byte b, char c, int i, Long l, Float f, Double d, Pointer p) {
                System.out.println("b=" + b + ", c=" + c + ", i=" + i + ", l=" + l + ", p=" + p);
            }
        });
        System.out.println("Closure instance=" + closure.getClass());
        NativeClosure ncl = (NativeClosure) closure;
        ncl.invoke(new MyBuffer());

    }
}
