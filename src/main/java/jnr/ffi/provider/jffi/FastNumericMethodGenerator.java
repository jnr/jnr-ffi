package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;

import java.nio.*;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.FastIntMethodGenerator.isFastIntType;
import static jnr.ffi.provider.jffi.Util.getBooleanProperty;

/**
 *
 */
class FastNumericMethodGenerator extends AbstractFastNumericMethodGenerator {
    private static final boolean ENABLED = getBooleanProperty("jnr.ffi.fast-numeric.enabled", true);
    private static final int MAX_PARAMETERS = getMaximumParameters();
    private static final String[] signatures;

    private static final String[] methodNames = {
        "invokeN0", "invokeN1", "invokeN2", "invokeN3", "invokeN4", "invokeN5", "invokeN6"
    };

    static {
        signatures = new String[MAX_PARAMETERS + 1];
        for (int i = 0; i <= MAX_PARAMETERS; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(ci(CallContext.class)).append(ci(long.class));
            for (int n = 0; n < i; n++) {
                sb.append('J');
            }
            signatures[i] = sb.append(")J").toString();
        }
    }

    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        final int parameterCount = parameterTypes.length;

        if (!ENABLED) {
            return false;
        }

        if (callingConvention != CallingConvention.DEFAULT || parameterCount > MAX_PARAMETERS) {
            return false;
        }
        final Platform platform = Platform.getPlatform();

        // Only supported on i386 and amd64 arches
        if (platform.getCPU() != Platform.CPU.I386 && platform.getCPU() != Platform.CPU.X86_64) {
            return false;
        }

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }

        for (ParameterType parameterType : parameterTypes) {
            if (!isFastNumericParameter(platform, parameterType)) {
                return false;
            }
        }

        return isFastNumericResult(platform, resultType);
    }


    @Override
    String getInvokerMethodName(ResultType resultType, ParameterType[] parameterTypes, boolean ignoreErrno) {
        final int parameterCount = parameterTypes.length;

        if (parameterCount <= MAX_PARAMETERS && parameterCount <= methodNames.length) {
            return methodNames[parameterCount];

        } else {
            throw new IllegalArgumentException("invalid fast-numeric parameter count: " + parameterCount);
        }
    }

    @Override
    String getInvokerSignature(int parameterCount, Class nativeIntType) {

        if (parameterCount <= MAX_PARAMETERS && parameterCount <= signatures.length) {
            return signatures[parameterCount];

        } else {
            throw new IllegalArgumentException("invalid fast-numeric parameter count: " + parameterCount);
        }
    }

    @Override
    Class getInvokerType() {
        return long.class;
    }

    private static boolean isNumericType(Platform platform, SigType type) {
        return isFastIntType(platform, type)
                || type.nativeType == NativeType.SLONG || type.nativeType == NativeType.ULONG
                || type.nativeType == NativeType.SLONGLONG || type.nativeType == NativeType.ULONGLONG
                || type.nativeType == NativeType.FLOAT || type.nativeType == NativeType.DOUBLE
                ;
    }

    static boolean isFastNumericResult(Platform platform, ResultType type) {
        return isNumericType(platform, type)
                || NativeType.VOID == type.nativeType
                || NativeType.ADDRESS == type.nativeType
                ;
    }

    static boolean isFastNumericParameter(Platform platform, ParameterType parameterType) {
        return isNumericType(platform, parameterType) || isDelegate(parameterType.getDeclaredType())
            || (parameterType.nativeType == NativeType.ADDRESS && isSupportedPointerParameterType(parameterType.effectiveJavaType()));
    }

    private static boolean isSupportedPointerParameterType(Class javaParameterType) {
        return Pointer.class.isAssignableFrom(javaParameterType)
                || ByteBuffer.class.isAssignableFrom(javaParameterType)
                || ShortBuffer.class.isAssignableFrom(javaParameterType)
                || IntBuffer.class.isAssignableFrom(javaParameterType)
                || (LongBuffer.class.isAssignableFrom(javaParameterType) && Type.SLONG.size() == 8)
                || FloatBuffer.class.isAssignableFrom(javaParameterType)
                || DoubleBuffer.class.isAssignableFrom(javaParameterType)
                || byte[].class == javaParameterType
                || short[].class == javaParameterType
                || int[].class == javaParameterType
                || (long[].class == javaParameterType && Type.SLONG.size() == 8)
                || float[].class == javaParameterType
                || double[].class == javaParameterType
                || boolean[].class == javaParameterType
                ;
    }


    static int getMaximumParameters() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeN6", CallContext.class, long.class,
                    long.class, long.class, long.class, long.class, long.class, long.class);
            return 6;
        } catch (Throwable t) {
            return 0;
        }
    }
}
