package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import jnr.ffi.Address;
import jnr.ffi.NativeLong;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.InvocationSession;
import jnr.ffi.provider.ParameterFlags;

import java.nio.Buffer;

import static jnr.ffi.provider.jffi.AsmUtil.*;
import static jnr.ffi.provider.jffi.CodegenUtils.*;
import static jnr.ffi.provider.jffi.NumberUtil.*;

/**
 *
 */
final class BufferMethodGenerator extends BaseMethodGenerator {

    @Override
    void generate(AsmBuilder builder, SkinnyMethodAdapter mv, LocalVariableAllocator localVariableAllocator, Function function, ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {
        generateBufferInvocation(builder, mv, localVariableAllocator, function, resultType, parameterTypes);
    }

    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        // Buffer invocation supports everything
        return true;
    }

    private static void emitPrimitiveOp(final SkinnyMethodAdapter mv, ParameterType parameterType, ToNativeOp op) {
        String paramMethod;
        Class nativeParamType = int.class;


        switch (parameterType.nativeType) {
            case SCHAR:
            case UCHAR:
                paramMethod = "putByte";
                break;

            case SSHORT:
            case USHORT:
                paramMethod = "putShort";
                break;

            case SINT:
            case UINT:
                paramMethod = "putInt";
                break;

            case SLONG:
            case ULONG:
                if (sizeof(parameterType) == 4) {
                    paramMethod = "putInt";
                    nativeParamType = int.class;

                } else {
                    paramMethod = "putLong";
                    nativeParamType = long.class;
                }
                break;

            case SLONGLONG:
            case ULONGLONG:
                paramMethod = "putLong";
                nativeParamType = long.class;
                break;

            case FLOAT:
                paramMethod = "putFloat";
                nativeParamType = float.class;
                break;

            case DOUBLE:
                paramMethod = "putDouble";
                nativeParamType = double.class;
                break;

            default:
                throw new IllegalArgumentException("unsupported parameter type " + parameterType);
        }

        op.emitPrimitive(mv, nativeParamType, parameterType.nativeType);
        mv.invokevirtual(HeapInvocationBuffer.class, paramMethod, void.class, nativeParamType);
    }

    static boolean isSessionRequired(ParameterType parameterType) {
        Class javaType = parameterType.effectiveJavaType();
        return (javaType.isArray() && Pointer.class.isAssignableFrom(javaType.getComponentType()))
                || (javaType.isArray() && CharSequence.class.isAssignableFrom(javaType.getComponentType()))
                || (javaType.isArray() && NativeLong.class.isAssignableFrom(javaType.getComponentType()))
                || (javaType.isArray() && isLong32(javaType.getComponentType(), parameterType.annotations()))
                ;
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


        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.dup(); // dup ref to HeapInvocationBuffer

            if (isSessionRequired(parameterTypes[i])) {
                mv.aload(session);
            }
            loadAndConvertParameter(builder, mv, parameters[i], parameterTypes[i]);
            if (parameterTypes[i].toNativeConverter instanceof ToNativeConverter.PostInvocation) {
                mv.dup();
                mv.astore(converted[i] = localVariableAllocator.allocate(Object.class));
            }

            final int parameterFlags = ParameterFlags.parse(parameterTypes[i].annotations());
            final int nativeArrayFlags = AsmUtil.getNativeArrayFlags(parameterFlags)
                        | ((parameterFlags & ParameterFlags.IN) != 0 ? ArrayFlags.NULTERMINATE : 0);

            final Class javaParameterType = parameterTypes[i].effectiveJavaType();
            ToNativeOp op = ToNativeOp.get(parameterTypes[i]);
            if (op != null && op.isPrimitive()) {
                emitPrimitiveOp(mv, parameterTypes[i], op);

            } else if (javaParameterType.isArray() && javaParameterType.getComponentType().isPrimitive()) {
                mv.pushInt(nativeArrayFlags);

                if (isLong32(javaParameterType.getComponentType(), parameterTypes[i].annotations())) {
                    mv.invokestatic(p(AsmRuntime.class), "marshal32",
                        sig(void.class, ci(HeapInvocationBuffer.class) + ci(InvocationSession.class),
                                javaParameterType, int.class));
                } else {
                    marshal(mv, javaParameterType, int.class);
                }

            } else if (Pointer.class.isAssignableFrom(javaParameterType)) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Pointer.class, int.class);

            } else if (Buffer.class.isAssignableFrom(javaParameterType)) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, javaParameterType, int.class);

            } else if (CharSequence.class.isAssignableFrom(javaParameterType)) {
                // stack should be: [ Buffer, array, flags ]
                marshal(mv, CharSequence.class);

            } else if (javaParameterType.isArray() && CharSequence.class.isAssignableFrom(javaParameterType.getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, CharSequence[].class, int.class, int.class);

            } else if (javaParameterType.isArray() && jnr.ffi.Struct.class.isAssignableFrom(javaParameterType.getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                marshal(mv, jnr.ffi.Struct[].class, int.class, int.class);

            } else if (javaParameterType.isArray() && Pointer.class.isAssignableFrom(javaParameterType.getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, Pointer[].class, int.class, int.class);

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
