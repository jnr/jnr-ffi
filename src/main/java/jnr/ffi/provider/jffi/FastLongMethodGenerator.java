package jnr.ffi.provider.jffi;

import com.kenai.jffi.*;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;

/**
 *
 */
public class FastLongMethodGenerator extends AbstractFastNumericMethodGenerator {
    private static final boolean ENABLED = getBooleanProperty("jnr.ffi.fast-long.enabled", true);
    private static final int MAX_PARAMETERS = getMaximumFastLongParameters();
    private static final String[] signatures;

    private static final String[] methodNames = {
        "invokeL0", "invokeL1", "invokeL2", "invokeL3", "invokeL4", "invokeL5", "invokeL6"
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

    public FastLongMethodGenerator(BufferMethodGenerator bufgen) {
        super(bufgen);
    }

    @Override
    String getInvokerMethodName(ResultType resultType, ParameterType[] parameterTypes, boolean ignoreErrno) {
        final int parameterCount = parameterTypes.length;

        if (parameterCount <= MAX_PARAMETERS && parameterCount <= methodNames.length) {
            return methodNames[parameterCount];

        } else {
            throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
        }
    }

    @Override
    String getInvokerSignature(int parameterCount, Class nativeIntType) {

        if (parameterCount <= MAX_PARAMETERS && parameterCount <= signatures.length) {
            return signatures[parameterCount];

        } else {
            throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
        }
    }

    @Override
    Class getInvokerType() {
        return long.class;
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
        // Only supported on amd64 arches
        if (platform.getCPU() != Platform.CPU.X86_64) {
            return false;
        }

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }


        for (ParameterType parameterType : parameterTypes) {
            if (!isFastLongParameter(platform, parameterType)) {
                return false;
            }
        }

        return isFastLongResult(platform, resultType);
    }

    static int getMaximumFastLongParameters() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeLLLLLLrL", Function.class,
                    long.class, long.class, long.class, long.class, long.class, long.class);
            return 6;
        } catch (Throwable t) {
            return 0;
        }
    }

    private static boolean isLongType(Platform platform, SigType type) {
        if (FastIntMethodGenerator.isFastIntType(platform, type)) {
            return true;
        }

        Type jffiType = type.jffiType;
        if (Type.SLONG == jffiType || Type.ULONG == jffiType
            || Type.SLONG_LONG == jffiType || Type.ULONG_LONG == jffiType
            || Type.SINT64 == jffiType || Type.UINT64 == jffiType) {
            return true;
        }

        return false;
    }

    static boolean isFastLongResult(Platform platform, ResultType type) {
        return isLongType(platform, type)
                || Type.VOID == type.jffiType
                || (Type.POINTER == type.jffiType && Type.POINTER.size() == 8)
                ;
    }

    static boolean isFastLongParameter(Platform platform, ParameterType type) {
        return isLongType(platform, type) || (isDelegate(type.javaType) && Type.POINTER.size() == 8);
    }
}
