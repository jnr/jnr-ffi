package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import jnr.ffi.*;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.InvocationSession;
import jnr.ffi.provider.ParameterFlags;

import java.nio.Buffer;

import static jnr.ffi.provider.jffi.AsmUtil.boxValue;
import static jnr.ffi.provider.jffi.AsmUtil.calculateLocalVariableSpace;
import static jnr.ffi.provider.jffi.AsmUtil.unboxNumber;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;
import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.ffi.provider.jffi.NumberUtil.*;

/**
 *
 */
final class BufferMethodGenerator extends BaseMethodGenerator {

    @Override
    void generate(AsmBuilder builder, SkinnyMethodAdapter mv, ResultType resultType, ParameterType[] parameterTypes, boolean ignoreError) {
        generateBufferInvocation(builder, mv, resultType, parameterTypes);
    }

    public boolean isSupported(Signature signature) {
        // Buffer invocation supports everything
        return true;
    }

    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        // Buffer invocation supports everything
        return true;
    }

    static void emitInvocationBufferNumericParameter(final SkinnyMethodAdapter mv, ParameterType parameterType,
                                                     Class javaParameterType) {
        String paramMethod;
        Class nativeParamType = int.class;
        Type to = parameterType.jffiType;

        if (Type.SCHAR == to || Type.UCHAR == to || Type.SINT8 == to || Type.UINT8 == to) {
            paramMethod = "putByte";

        } else if (Type.SSHORT == to || Type.USHORT == to || Type.SINT16 == to || Type.UINT16 == to) {
            paramMethod = "putShort";

        } else if (Type.SINT == to || Type.UINT == to || Type.SINT32 == to || Type.UINT32 == to) {
            paramMethod = "putInt";

        } else if (Type.SLONG == to || Type.ULONG == to) {
            if (to.size() == 4) {
                paramMethod = "putInt";
                nativeParamType = int.class;

            } else {
                paramMethod = "putLong";
                nativeParamType = long.class;
            }

        } else if (Type.SLONG_LONG == to || Type.ULONG_LONG == to || Type.SINT64 == to|| Type.UINT64 == to) {
            paramMethod = "putLong";
            nativeParamType = long.class;

        } else if (Type.FLOAT == to) {
            paramMethod = "putFloat";
            nativeParamType = float.class;

        } else if (Type.DOUBLE == to) {
            paramMethod = "putDouble";
            nativeParamType = double.class;

        } else {
            throw new IllegalArgumentException("unsupported parameter type " + parameterType);
        }

        if (!parameterType.javaType.isPrimitive()) {
            unboxNumber(mv, javaParameterType, nativeParamType, parameterType.jffiType);
        } else {
            NumberUtil.narrow(mv, javaParameterType, nativeParamType);
        }


        mv.invokevirtual(HeapInvocationBuffer.class, paramMethod, void.class, nativeParamType);
    }

    static boolean isSessionRequired(ParameterType parameterType) {
        Class javaType = parameterType.toNativeConverter != null
            ? parameterType.toNativeConverter.nativeType()
            : parameterType.javaType;
        return StringBuilder.class.isAssignableFrom(javaType)
                || StringBuffer.class.isAssignableFrom(javaType)
                || ByReference.class.isAssignableFrom(javaType)
                || (javaType.isArray() && Pointer.class.isAssignableFrom(javaType.getComponentType()))
                || (javaType.isArray() && CharSequence.class.isAssignableFrom(javaType.getComponentType()))
                || (javaType.isArray() && NativeLong.class.isAssignableFrom(javaType.getComponentType()))
                || (javaType.isArray() && isLong32(javaType.getComponentType(), parameterType.annotations))
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
        mv.invokestatic(p(AsmRuntime.class), "marshal", sig(void.class, ci(InvocationBuffer.class), parameterTypes));
    }

    static void sessionmarshal(SkinnyMethodAdapter mv, Class... parameterTypes) {
        mv.invokestatic(p(AsmRuntime.class), "marshal",
                sig(void.class, ci(InvocationBuffer.class) + ci(InvocationSession.class), parameterTypes));
    }

    void generateBufferInvocation(AsmBuilder builder, SkinnyMethodAdapter mv, ResultType resultType, ParameterType[] parameterTypes) {
        // [ stack contains: Invoker, Function ]
        final boolean sessionRequired = isSessionRequired(parameterTypes);
        final int lvarSession = sessionRequired ? calculateLocalVariableSpace(parameterTypes) + 1 : -1;
        if (sessionRequired) {
            mv.newobj(p(InvocationSession.class));
            mv.dup();
            mv.invokespecial(InvocationSession.class, "<init>", void.class);
            mv.astore(lvarSession);
        }

        // [ stack contains: Invoker, Function, Function ]
        mv.dup();
        mv.invokestatic(AsmRuntime.class, "newHeapInvocationBuffer", HeapInvocationBuffer.class, Function.class);
        // [ stack contains: Invoker, Function, HeapInvocationBuffer ]

        int lvar = 1;
        for (int i = 0; i < parameterTypes.length; ++i) {
            mv.dup(); // dup ref to HeapInvocationBuffer

            if (isSessionRequired(parameterTypes[i])) {
                mv.aload(lvarSession);
            }
            lvar = loadAndConvertParameter(builder, mv, lvar, parameterTypes[i]);

            final int parameterFlags = AsmUtil.getParameterFlags(parameterTypes[i].annotations);
            final int nativeArrayFlags = AsmUtil.getNativeArrayFlags(parameterFlags)
                        | ((parameterFlags & ParameterFlags.IN) != 0 ? ArrayFlags.NULTERMINATE : 0);

            ToNativeConverter parameterConverter = parameterTypes[i].toNativeConverter;
            final Class javaParameterType = parameterConverter != null
                    ? parameterConverter.nativeType() : parameterTypes[i].javaType;

            if (javaParameterType.isArray() && javaParameterType.getComponentType().isPrimitive()) {
                mv.pushInt(nativeArrayFlags);

                if (isLong32(javaParameterType.getComponentType(), parameterTypes[i].annotations)) {
                    mv.invokestatic(p(AsmRuntime.class), "marshal32",
                        sig(void.class, ci(InvocationBuffer.class) + ci(InvocationSession.class),
                                javaParameterType, int.class));
                } else {
                    marshal(mv, javaParameterType, int.class);
                }

            } else if (Pointer.class.isAssignableFrom(javaParameterType)) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, Pointer.class, int.class);

            } else if (Address.class.isAssignableFrom(javaParameterType)) {
                marshal(mv, Address.class);

            } else if (Buffer.class.isAssignableFrom(javaParameterType)) {
                mv.pushInt(nativeArrayFlags);
                marshal(mv, javaParameterType, int.class);

            } else if (ByReference.class.isAssignableFrom(javaParameterType)) {
                mv.pushInt(nativeArrayFlags);
                // stack should be: [ session, buffer, ref, flags ]
                sessionmarshal(mv, ByReference.class, int.class);

            } else if (StringBuilder.class.isAssignableFrom(javaParameterType) || StringBuffer.class.isAssignableFrom(javaParameterType)) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                // stack should be: [ session, buffer, ref, flags ]
                sessionmarshal(mv, javaParameterType, int.class, int.class);

            } else if (CharSequence.class.isAssignableFrom(javaParameterType)) {
                // stack should be: [ Buffer, array, flags ]
                marshal(mv, CharSequence.class);

            } else if (javaParameterType.isArray() && CharSequence.class.isAssignableFrom(javaParameterType.getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, CharSequence[].class, int.class, int.class);

            } else if (jnr.ffi.Struct.class.isAssignableFrom(javaParameterType)) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                marshal(mv, jnr.ffi.Struct.class, int.class, int.class);

            } else if (javaParameterType.isArray() && jnr.ffi.Struct.class.isAssignableFrom(javaParameterType.getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                marshal(mv, jnr.ffi.Struct[].class, int.class, int.class);

            } else if (javaParameterType.isArray() && Pointer.class.isAssignableFrom(javaParameterType.getComponentType())) {
                mv.pushInt(parameterFlags);
                mv.pushInt(nativeArrayFlags);
                sessionmarshal(mv, Pointer[].class, int.class, int.class);

            } else if (javaParameterType.isPrimitive() || Number.class.isAssignableFrom(javaParameterType)
                    || Boolean.class == javaParameterType) {
                emitInvocationBufferNumericParameter(mv, parameterTypes[i], javaParameterType);

            } else {
                throw new IllegalArgumentException("unsupported parameter type " + parameterTypes[i]);
            }
        }

        String invokeMethod ;
        Class nativeReturnType;
        FromNativeConverter resultConverter = resultType.fromNativeConverter;
        Class javaReturnType = resultConverter != null
                ? resultConverter.nativeType() : resultType.javaType;

        if (Type.SCHAR == resultType.jffiType || Type.UCHAR == resultType.jffiType
                || Type.SINT8 == resultType.jffiType || Type.UINT8 == resultType.jffiType
                || Type.SSHORT == resultType.jffiType || Type.USHORT == resultType.jffiType
                || Type.SINT16 == resultType.jffiType || Type.UINT16 == resultType.jffiType
                || Type.SINT == resultType.jffiType || Type.UINT == resultType.jffiType
                || Type.SINT32 == resultType.jffiType || Type.UINT32 == resultType.jffiType
                || Type.VOID == resultType.jffiType) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;

        } else if ((Type.SLONG == resultType.jffiType || Type.ULONG == resultType.jffiType) && resultType.jffiType.size() == 4) {
            invokeMethod = "invokeInt";
            nativeReturnType = int.class;

        } else if ((Type.SLONG == resultType.jffiType || Type.ULONG == resultType.jffiType) && resultType.jffiType.size() == 8) {
            invokeMethod = "invokeLong";
            nativeReturnType = long.class;

        } else if (Type.SLONG_LONG == resultType.jffiType || Type.ULONG_LONG == resultType.jffiType
            || Type.SINT64 == resultType.jffiType || Type.UINT64 == resultType.jffiType) {
            invokeMethod = "invokeLong";
            nativeReturnType = long.class;

        } else if (Pointer.class == javaReturnType || Address.class == javaReturnType
            || jnr.ffi.Struct.class.isAssignableFrom(javaReturnType) || String.class.isAssignableFrom(javaReturnType)) {
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
                nativeReturnType, Function.class, HeapInvocationBuffer.class);

        if (sessionRequired) {
            mv.aload(lvarSession);
            mv.invokevirtual(p(InvocationSession.class), "finish", "()V");
        }

        convertAndReturnResult(builder, mv, resultType, nativeReturnType);
    }
}
