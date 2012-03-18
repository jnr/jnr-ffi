package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;

import java.lang.annotation.Annotation;
import java.nio.Buffer;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;

/**
 *
 */
class FastNumericMethodGenerator extends AbstractFastNumericMethodGenerator {
    private static final boolean ENABLED = getBooleanProperty("jnr.ffi.fast-numeric.enabled", true);
    private static final int MAX_PARAMETERS = getMaximumParameters();
    private static final String[] signatures;

    private static final String[] methodNames = {
        "invokeVrN", "invokeNrN", "invokeNNrN", "invokeNNNrN", "invokeNNNNrN", "invokeNNNNNrN", "invokeNNNNNNrN"
    };

    static {
        signatures = new String[MAX_PARAMETERS + 1];
        for (int i = 0; i <= MAX_PARAMETERS; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(ci(Function.class));
            for (int n = 0; n < i; n++) {
                sb.append('J');
            }
            signatures[i] = sb.append(")J").toString();
        }
    }

    FastNumericMethodGenerator(BufferMethodGenerator bufgen) {
        super(bufgen);
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
        if (FastIntMethodGenerator.isFastIntType(platform, type)) {
            return true;
        }
        Type jffiType = type.jffiType;
        if (Type.SLONG == jffiType || Type.ULONG == jffiType
            || Type.SLONG_LONG == jffiType || Type.ULONG_LONG == jffiType
            || Type.SINT64 == jffiType || Type.UINT64 == jffiType
            || Type.FLOAT == jffiType || Type.DOUBLE == jffiType) {
            return true;
        }

        return false;
    }

    static boolean isFastNumericResult(Platform platform, ResultType type) {
        return isNumericType(platform, type)
                || Type.VOID == type.jffiType
                || Type.POINTER == type.jffiType
                ;
    }

    static boolean isFastNumericParameter(Platform platform, ParameterType parameterType) {
        return isNumericType(platform, parameterType) || isDelegate(parameterType.javaType);
    }

    static int getMaximumParameters() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeNNNNNNrN", Function.class,
                    long.class, long.class, long.class, long.class, long.class, long.class);
            return 6;
        } catch (Throwable t) {
            return 0;
        }
    }
}
