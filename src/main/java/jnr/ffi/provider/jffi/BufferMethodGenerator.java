package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import jnr.ffi.Address;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.InvocationSession;
import jnr.ffi.provider.ParameterFlags;

import java.nio.*;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static jnr.ffi.provider.jffi.AbstractFastNumericMethodGenerator.emitPointerParameterStrategyLookup;
import static jnr.ffi.provider.jffi.AbstractFastNumericMethodGenerator.hasPointerParameterStrategy;
import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.NumberUtil.*;

/**
 *
 */
final class BufferMethodGenerator extends BaseMethodGenerator {
    private static final class MarshalOp {
        private final String methodName;
        private final Class primitiveClass;

        private MarshalOp(String methodName, Class primitiveClass) {
            this.methodName = "put" + methodName;
            this.primitiveClass = primitiveClass;
        }
    }

    static final Map<NativeType, MarshalOp> marshalOps;
    static {
        Map<NativeType, MarshalOp> ops = new EnumMap<NativeType, MarshalOp>(NativeType.class);
        ops.put(NativeType.SCHAR, new MarshalOp("Byte", int.class));
        ops.put(NativeType.UCHAR, new MarshalOp("Byte", int.class));
        ops.put(NativeType.SSHORT, new MarshalOp("Short", int.class));
        ops.put(NativeType.USHORT, new MarshalOp("Short", int.class));
        ops.put(NativeType.SINT, new MarshalOp("Int", int.class));
        ops.put(NativeType.UINT, new MarshalOp("Int", int.class));
        ops.put(NativeType.SLONGLONG, new MarshalOp("Long", long.class));
        ops.put(NativeType.ULONGLONG, new MarshalOp("Long", long.class));
        ops.put(NativeType.FLOAT, new MarshalOp("Float", float.class));
        ops.put(NativeType.DOUBLE, new MarshalOp("Double", double.class));
        ops.put(NativeType.ADDRESS, new MarshalOp("Address", long.class));
        if (sizeof(NativeType.SLONG) == 4) {
            ops.put(NativeType.SLONG, new MarshalOp("Int", int.class));
            ops.put(NativeType.ULONG, new MarshalOp("Int", int.class));
        } else {
            ops.put(NativeType.SLONG, new MarshalOp("Long", long.class));
            ops.put(NativeType.ULONG, new MarshalOp("Long", long.class));
        }

        marshalOps = Collections.unmodifiableMap(ops);
    }

    @Override
    void generate(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, Function function, ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {
        generateBufferInvocation(builder, mv, localVariableAllocator, function, resultType, parameterTypes);
    }

    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        // Buffer invocation supports everything
        return true;
    }

    private static void emitPrimitiveOp(final SkinnyMethodAdapter mv, ParameterType parameterType, ToNativeOp op) {
        MarshalOp marshalOp = marshalOps.get(parameterType.nativeType);
        if (marshalOp == null) {
            throw new IllegalArgumentException("unsupported parameter type " + parameterType);
        }

        op.emitPrimitive(mv, marshalOp.primitiveClass, parameterType.nativeType);
        mv.invokevirtual(HeapInvocationBuffer.class, marshalOp.methodName, void.class, marshalOp.primitiveClass);
    }

    static boolean isSessionRequired(ParameterType parameterType) {
        return false;
    }


    static boolean isSessionRequired(ParameterType[] parameterTypes) {
        for (ParameterType parameterType : parameterTypes) {
            if (isSessionRequired(parameterType)) {
                return true;
            }
        }

        return false;
    }

    static void marshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(AsmRuntime.class), "marshal", sig(void.class, ci(HeapInvocationBuffer.class), parameterTypes));
    }

    static void sessionmarshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(AsmRuntime.class), "marshal",
                sig(void.class, ci(HeapInvocationBuffer.class) + ci(InvocationSession.class), parameterTypes));
    }

    void generateBufferInvocation(final AsmBuilder builder, final SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, Function function, final ResultType resultType, final ParameterType[] parameterTypes) {
        // [ stack contains: Invoker, Function ]
        final boolean sessionRequired = isSessionRequired(parameterTypes);
        final LocalVariable session = localVariableAllocator.allocate(InvocationSession.class);

        if (sessionRequired) {
            mv.newobj(p(InvocationSession.class));
            mv.dup();
            mv.invokespecial(InvocationSession.class, "<init>", void.class);
            mv.astore(session);
        }

        // Create a new InvocationBuffer
        mv.aload(0);
        mv.getfield(builder.getClassNamePath(), builder.getCallContextFieldName(function), ci(CallContext.class));
        mv.invokestatic(AsmRuntime.class, "newHeapInvocationBuffer", HeapInvocationBuffer.class, CallContext.class);
        // [ stack contains: Invoker, Function, HeapInvocationBuffer ]

        final LocalVariable[] parameters = AsmUtil.getParameterVariables(parameterTypes);
        final LocalVariable[] converted = new LocalVariable[parameterTypes.length];
        LocalVariable[] strategies = new LocalVariable[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.dup(); // dup ref to HeapInvocationBuffer

            if (isSessionRequired(parameterTypes[i])) {
                mv.aload(session);
            }
            loadAndConvertParameter(builder, mv, parameters[i], parameterTypes[i]);

            if (parameterTypes[i].toNativeConverter != null) {
                mv.astore(converted[i] = localVariableAllocator.allocate(parameterTypes[i].effectiveJavaType()));
                mv.aload(converted[i]);
            }

            final Class javaParameterType = parameterTypes[i].effectiveJavaType();
            ToNativeOp op = ToNativeOp.get(parameterTypes[i]);
            if (op != null && op.isPrimitive()) {
                emitPrimitiveOp(mv, parameterTypes[i], op);

            } else if (hasPointerParameterStrategy(javaParameterType)) {
                mv.dup();
                emitPointerParameterStrategyLookup(mv, javaParameterType, parameterTypes[i].annotations());
                mv.astore(strategies[i] = localVariableAllocator.allocate(PointerParameterStrategy.class));
                mv.aload(strategies[i]);

                mv.pushInt(AsmUtil.getNativeArrayFlags(parameterTypes[i].annotations()));
                mv.invokevirtual(HeapInvocationBuffer.class, "putObject", void.class, Object.class, ObjectParameterStrategy.class, int.class);

            } else {
                throw new IllegalArgumentException("unsupported parameter type " + parameterTypes[i]);
            }
        }

        String invokeMethod ;
        Class nativeReturnType;
        Class javaReturnType = resultType.effectiveJavaType();

        if (NativeType.SCHAR == resultType.nativeType || NativeType.UCHAR == resultType.nativeType
            || NativeType.SSHORT== resultType.nativeType || NativeType.USHORT == resultType.nativeType
            || NativeType.SINT == resultType.nativeType || NativeType.UINT == resultType.nativeType
            || NativeType.VOID == resultType.nativeType) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;

        } else if ((NativeType.SLONG == resultType.nativeType || NativeType.ULONG == resultType.nativeType) && sizeof(resultType) == 4) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;

        } else if ((NativeType.SLONG == resultType.nativeType || NativeType.ULONG == resultType.nativeType) && sizeof(resultType) == 8) {
            invokeMethod = "invokeLong";
            nativeReturnType = long.class;

        } else if (NativeType.SLONGLONG == resultType.nativeType || NativeType.ULONGLONG == resultType.nativeType) {
            invokeMethod = "invokeLong";
            nativeReturnType = long.class;

        } else if (Pointer.class == javaReturnType || Address.class == javaReturnType
            || String.class.isAssignableFrom(javaReturnType)) {
            invokeMethod = Platform.getPlatform().addressSize() == 32 ? "invokeInt" : "invokeLong";
            nativeReturnType = Platform.getPlatform().addressSize() == 32 ? int.class : long.class;

        } else if (Float.class == javaReturnType || float.class == javaReturnType) {
            invokeMethod = "invokeFloat";
            nativeReturnType = float.class;

        } else if (Double.class == javaReturnType || double.class == javaReturnType) {
            invokeMethod = "invokeDouble";
            nativeReturnType = double.class;

        } else {
            throw new IllegalArgumentException("unsupported return type " + javaReturnType);
        }

        mv.invokevirtual(Invoker.class, invokeMethod,
                nativeReturnType, CallContext.class, long.class, HeapInvocationBuffer.class);

        // box and/or narrow/widen the return value if needed
        final Class unboxedResultType = unboxedReturnType(javaReturnType);
        convertPrimitive(mv, nativeReturnType, unboxedResultType, resultType.nativeType);
        emitEpilogue(builder, mv, resultType, parameterTypes, parameters, converted, sessionRequired ? new Runnable() {
            public void run() {
                mv.aload(session);
                mv.invokevirtual(p(InvocationSession.class), "finish", "()V");
            }
        } : null);
    }
}
