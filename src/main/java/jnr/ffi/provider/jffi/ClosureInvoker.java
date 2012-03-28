package jnr.ffi.provider.jffi;

import jnr.ffi.NativeLong;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.NumberUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 */
abstract public class ClosureInvoker {
    protected final NativeRuntime runtime;

    protected ClosureInvoker(NativeRuntime runtime) {
        this.runtime = runtime;
    }


    abstract public void invoke(com.kenai.jffi.Closure.Buffer buffer, Object callable);

    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private static final AtomicLong nextClassID = new AtomicLong(0);

    static ClosureInvoker newClosureInvoker(NativeRuntime runtime, Method callMethod,
                                            ToNativeType resultType, FromNativeType[] parameterTypes) {

        final String closureInvokerClassName = p(ClosureInvoker.class) + "$$impl$$" + nextClassID.getAndIncrement();
        final ClassWriter closureClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor closureClassVisitor = DEBUG ? AsmUtil.newCheckClassAdapter(closureClassWriter) : closureClassWriter;
        AsmBuilder builder = new AsmBuilder(closureInvokerClassName, closureClassVisitor);

        closureClassVisitor.visit(V1_5, ACC_PUBLIC | ACC_FINAL, closureInvokerClassName, null, p(ClosureInvoker.class),
                new String[]{ });

        SkinnyMethodAdapter closureInvoke = new SkinnyMethodAdapter(closureClassVisitor.visitMethod(ACC_PUBLIC | ACC_FINAL, "invoke",
                sig(void.class, com.kenai.jffi.Closure.Buffer.class, Object.class),
                null, null));
        closureInvoke.start();

        // Cast the Callable instance to the Callable subclass
        closureInvoke.aload(2);
        closureInvoke.checkcast(p(callMethod.getDeclaringClass()));

        // Construct callback method
        LocalVariableAllocator localVariableAllocator = new LocalVariableAllocator(com.kenai.jffi.Closure.Buffer.class, Object.class);

        for (int i = 0; i < parameterTypes.length; ++i) {
            FromNativeType parameterType = parameterTypes[i];
            Class parameterClass = parameterType.effectiveJavaType();

            if (!isParameterTypeSupported(parameterClass)) {
                throw new IllegalArgumentException("unsupported closure parameter type " + parameterTypes[i].getDeclaredType());
            }

            Class nativeParameterClass;
            switch (parameterType.nativeType) {
                case SCHAR:
                case UCHAR:
                    loadParameter(closureInvoke, "getByte", nativeParameterClass = byte.class, i);
                    break;

                case SSHORT:
                case USHORT:
                    loadParameter(closureInvoke, "getShort", nativeParameterClass = short.class, i);
                    break;

                case SINT:
                case UINT:
                    loadParameter(closureInvoke, "getInt", nativeParameterClass = int.class, i);
                    break;

                case SLONG:
                case ULONG:
                case ADDRESS:
                    if (sizeof(parameterType.nativeType) == 4) {
                        loadParameter(closureInvoke, "getInt", nativeParameterClass = int.class, i);
                    } else {
                        loadParameter(closureInvoke, "getLong", nativeParameterClass = long.class, i);
                    }
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    loadParameter(closureInvoke, "getLong", nativeParameterClass = long.class, i);
                    break;

                case FLOAT:
                    loadParameter(closureInvoke, "getFloat", nativeParameterClass = float.class, i);
                    break;

                case DOUBLE:
                    loadParameter(closureInvoke, "getDouble", nativeParameterClass = float.class, i);
                    break;

                default:
                    throw new IllegalArgumentException("unsupported closure parameter type " + parameterType.getDeclaredType());
            }

            Class unboxedType = unboxedType(parameterClass);
            convertPrimitive(closureInvoke, nativeParameterClass, unboxedType, parameterType.nativeType);
            if (!parameterClass.isPrimitive()) {
                boxValue(closureInvoke, parameterClass, unboxedType);
            }

            // If there is a parameter converter, retrieve it and put on the stack
            FromNativeConverter fromNativeConverter = parameterTypes[i].fromNativeConverter;
            if (fromNativeConverter != null) {
                closureInvoke.aload(0);
                closureInvoke.getfield(builder.getClassNamePath(), builder.getFromNativeConverterName(fromNativeConverter), ci(FromNativeConverter.class));
                closureInvoke.swap();
                closureInvoke.aconst_null();
                closureInvoke.invokeinterface(FromNativeConverter.class, "fromNative",
                        Object.class, Object.class, FromNativeContext.class);
                if (parameterType.getDeclaredType().isPrimitive()) {
                    Class boxedType = getBoxedClass(parameterType.getDeclaredType());
                    closureInvoke.checkcast(p(boxedType));
                    unboxNumber(closureInvoke, boxedType, parameterType.getDeclaredType(), parameterType.nativeType);
                } else {
                    closureInvoke.checkcast(p(parameterTypes[i].getDeclaredType()));
                }
            }
        }

        // dispatch to java method
        if (callMethod.getDeclaringClass().isInterface()) {
            closureInvoke.invokeinterface(p(callMethod.getDeclaringClass()), callMethod.getName(), sig(callMethod.getReturnType(), callMethod.getParameterTypes()));
        } else {
            closureInvoke.invokevirtual(p(callMethod.getDeclaringClass()), callMethod.getName(), sig(callMethod.getReturnType(), callMethod.getParameterTypes()));
        }

        if (!isReturnTypeSupported(resultType.effectiveJavaType())) {
            throw new IllegalArgumentException("unsupported closure return type " + resultType.getDeclaredType());
        }

        ToNativeConverter toNativeConverter = resultType.toNativeConverter;
        if (toNativeConverter != null) {

            closureInvoke.aload(0);
            closureInvoke.getfield(builder.getClassNamePath(), builder.getToNativeConverterName(toNativeConverter), ci(ToNativeConverter.class));
            closureInvoke.swap();
            if (resultType.getDeclaredType().isPrimitive()) {
                boxValue(closureInvoke, getBoxedClass(resultType.getDeclaredType()), resultType.getDeclaredType());
            }
            closureInvoke.aconst_null();
            closureInvoke.invokeinterface(ToNativeConverter.class, "toNative",
                        Object.class, Object.class, ToNativeContext.class);
            closureInvoke.checkcast(p(toNativeConverter.nativeType()));
        }


        Class nativeResultClass;

        switch (resultType.nativeType) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
                nativeResultClass = int.class;
                break;

            case SLONG:
            case ULONG:
            case ADDRESS:
                nativeResultClass = sizeof(resultType.nativeType) == 4 ? int.class : long.class;
                break;

            case SLONGLONG:
            case ULONGLONG:
                nativeResultClass = long.class;
                break;

            case FLOAT:
                nativeResultClass = float.class;
                break;

            case DOUBLE:
                nativeResultClass = double.class;
                break;

            case VOID:
                nativeResultClass = void.class;
                break;

            default:
                throw new IllegalArgumentException("unsupported result type " + resultType.getDeclaredType());
        }

        if (!resultType.effectiveJavaType().isPrimitive()) {
            if (Number.class.isAssignableFrom(resultType.effectiveJavaType())) {
                AsmUtil.unboxNumber(closureInvoke, resultType.effectiveJavaType(), nativeResultClass, resultType.nativeType);

            } else if (Boolean.class.isAssignableFrom(resultType.effectiveJavaType())) {
                AsmUtil.unboxBoolean(closureInvoke, nativeResultClass);

            } else if (Pointer.class.isAssignableFrom(resultType.effectiveJavaType())) {
                AsmUtil.unboxPointer(closureInvoke, nativeResultClass);

            }
        }

        // Load the Callable.Buffer for the result set call
        if (NativeType.VOID != resultType.nativeType) {
            LocalVariable result = localVariableAllocator.allocate(nativeResultClass);
            store(closureInvoke, nativeResultClass, result);

            // If the Callable returns a value, push the Callable.Buffer on the stack
            // for the call to Callable.Buffer#set<Foo>Return()
            closureInvoke.aload(1);
            load(closureInvoke, nativeResultClass, result);
        }

        switch (resultType.nativeType) {
            case SCHAR:
            case UCHAR:
                setResult(closureInvoke, "Byte", byte.class);
                break;

            case SSHORT:
            case USHORT:
                setResult(closureInvoke, "Short", short.class);
                break;

            case SINT:
            case UINT:
                setResult(closureInvoke, "Int", int.class);
                break;

            case SLONG:
            case ULONG:
            case ADDRESS:
                if (int.class == nativeResultClass) {
                    setResult(closureInvoke, "Int", int.class);
                } else {
                    setResult(closureInvoke, "Long", long.class);
                }
                break;

            case SLONGLONG:
            case ULONGLONG:
                setResult(closureInvoke, "Long", long.class);
                break;

            case FLOAT:
                setResult(closureInvoke, "Float", float.class);
                break;

            case DOUBLE:
                setResult(closureInvoke, "Double", double.class);
                break;
        }


        closureInvoke.voidreturn();
        closureInvoke.visitMaxs(10, 10 + localVariableAllocator.getSpaceUsed());
        closureInvoke.visitEnd();

        SkinnyMethodAdapter closureInit = new SkinnyMethodAdapter(closureClassVisitor.visitMethod(ACC_PUBLIC, "<init>",
                sig(void.class, NativeRuntime.class, Object[].class),
                null, null));
        closureInit.start();
        closureInit.aload(0);
        closureInit.aload(1);
        closureInit.invokespecial(p(ClosureInvoker.class), "<init>", sig(void.class, NativeRuntime.class));

        AsmBuilder.ObjectField[] fields = builder.getObjectFieldArray();
        Object[] fieldObjects = new Object[fields.length];
        for (int i = 0; i < fieldObjects.length; i++) {
            fieldObjects[i] = fields[i].value;
            String fieldName = fields[i].name;
            builder.getClassVisitor().visitField(ACC_PRIVATE | ACC_FINAL, fieldName, ci(fields[i].klass), null, null);
            closureInit.aload(0);
            closureInit.aload(2);
            closureInit.pushInt(i);
            closureInit.aaload();
            if (fields[i].klass.isPrimitive()) {
                Class unboxedType = unboxedType(fields[i].klass);
                closureInit.checkcast(unboxedType);
                unboxNumber(closureInit, unboxedType, fields[i].klass);
            } else {
                closureInit.checkcast(fields[i].klass);
            }
            closureInit.putfield(builder.getClassNamePath(), fieldName, ci(fields[i].klass));
        }

        closureInit.voidreturn();
        closureInit.visitMaxs(10, 10);
        closureInit.visitEnd();

        closureClassVisitor.visitEnd();

        try {
            byte[] closureImpBytes = closureClassWriter.toByteArray();
            if (DEBUG) {
                ClassVisitor trace = AsmUtil.newTraceClassVisitor(new PrintWriter(System.err));
                new ClassReader(closureImpBytes).accept(trace, 0);
            }
            ClassLoader cl = NativeClosureFactory.class.getClassLoader();
            if (cl == null) {
                cl = Thread.currentThread().getContextClassLoader();
            }
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            AsmClassLoader asm = new AsmClassLoader(cl);
            Class<? extends ClosureInvoker> nativeClosureClass = asm.defineClass(c(closureInvokerClassName), closureImpBytes);
            Constructor<? extends ClosureInvoker> closureInvokerConstructor
                    = nativeClosureClass.getConstructor(NativeRuntime.class, Object[].class);

            return closureInvokerConstructor.newInstance(runtime, fieldObjects);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void loadParameter(SkinnyMethodAdapter mv, String method, Class parameterClass, int idx) {
        // Load the Callable.Buffer for the parameter set call
        mv.aload(1);

        // Load the parameter index
        mv.pushInt(idx);
        mv.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), method, sig(parameterClass, int.class));
    }

    private static void setResult(SkinnyMethodAdapter mv, String typeName, Class resultClass) {
        mv.invokeinterface(p(com.kenai.jffi.Closure.Buffer.class), "set" + typeName + "Return", sig(void.class, resultClass));
    }


    private static boolean isReturnTypeSupported(Class type) {
        return type.isPrimitive()
                || boolean.class == type || Boolean.class == type
                || Byte.class == type
                || Short.class == type || Integer.class == type
                || Long.class == type || Float.class == type
                || Double.class == type || NativeLong.class == type
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

}
