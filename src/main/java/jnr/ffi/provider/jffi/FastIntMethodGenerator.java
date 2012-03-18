package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import com.kenai.jffi.Type;
import jnr.ffi.Address;
import jnr.ffi.Pointer;
import jnr.ffi.Struct;

import java.lang.annotation.Annotation;
import java.nio.Buffer;

import static jnr.ffi.provider.jffi.AsmUtil.isDelegate;
import static jnr.ffi.provider.jffi.CodegenUtils.ci;

/**
 *
 */
final class FastIntMethodGenerator extends AbstractFastNumericMethodGenerator {
    private static final boolean ENABLED = getBooleanProperty("jnr.ffi.fast-int.enabled", true);
    private static final int MAX_FASTINT_PARAMETERS = getMaximumFastIntParameters();

    private static final String[] signatures;

    private static final String[] methodNames = {
        "invokeVrI", "invokeIrI", "invokeIIrI", "invokeIIIrI", "invokeIIIIrI", "invokeIIIIIrI", "invokeIIIIIIrI"
    };

    private static final String[] noErrnoMethodNames = {
        "invokeNoErrnoVrI", "invokeNoErrnoIrI", "invokeNoErrnoIIrI", "invokeNoErrnoIIIrI"
    };

    static {
        signatures = new String[MAX_FASTINT_PARAMETERS + 1];
        for (int i = 0; i <= MAX_FASTINT_PARAMETERS; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(ci(Function.class));
            for (int n = 0; n < i; n++) {
                sb.append('I');
            }
            signatures[i] = sb.append(")I").toString();
        }
    }

    FastIntMethodGenerator(BufferMethodGenerator bufgen) {
        super(bufgen);
    }

    @Override
    String getInvokerMethodName(ResultType resultType, ParameterType[] parameterTypes, boolean ignoreErrno) {
        final int parameterCount = parameterTypes.length;

        if (ignoreErrno && parameterCount <= MAX_FASTINT_PARAMETERS && parameterCount <= noErrnoMethodNames.length) {
            return noErrnoMethodNames[parameterTypes.length];

        } else if (parameterCount <= MAX_FASTINT_PARAMETERS && parameterCount <= methodNames.length) {
            return methodNames[parameterCount];

        } else {
            throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
        }
    }

    @Override
    String getInvokerSignature(int parameterCount, Class nativeIntType) {
        if (parameterCount <= MAX_FASTINT_PARAMETERS && parameterCount <= signatures.length) {
            return signatures[parameterCount];
        }
        throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
    }

    final Class getInvokerType() {
        return int.class;
    }

    public boolean isSupported(ResultType resultType, ParameterType[] parameterTypes, CallingConvention callingConvention) {
        final int parameterCount = parameterTypes.length;

        if (!ENABLED) {
            return false;
        }

        if (!callingConvention.equals(CallingConvention.DEFAULT) || parameterCount > MAX_FASTINT_PARAMETERS) {
            return false;
        }

        final Platform platform = Platform.getPlatform();

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }

        if (!platform.getCPU().equals(Platform.CPU.I386) && !platform.getCPU().equals(Platform.CPU.X86_64)) {
            return false;
        }

        for (ParameterType parameterType : parameterTypes) {
            if (!isFastIntParameter(platform, parameterType)) {
                return false;
            }
        }

        return isFastIntResult(platform, resultType);
    }


    static int getMaximumFastIntParameters() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeIIIIIIrI", Function.class,
                    int.class, int.class, int.class, int.class, int.class, int.class);
            return 6;
        } catch (NoSuchMethodException nex) {
            try {
                com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeIIIrI", Function.class,
                        int.class, int.class, int.class);
                return 3;
            } catch (NoSuchMethodException nex2) {
                return 0;
            }
        } catch (Throwable t) {
            return 0;
        }
    }

    static boolean isFastIntType(Platform platform, SigType type) {
        if (Type.SCHAR == type.jffiType || Type.SINT8 == type.jffiType
                || Type.UCHAR == type.jffiType || Type.UINT8 == type.jffiType
                || Type.SSHORT == type.jffiType || Type.SINT16 == type.jffiType
                || Type.USHORT == type.jffiType || Type.UINT16 == type.jffiType
                || Type.SINT == type.jffiType || Type.SINT32 == type.jffiType
                || Type.UINT == type.jffiType || Type.UINT32 == type.jffiType
                || ((Type.SLONG == type.jffiType || Type.ULONG == type.jffiType) && type.jffiType.size() == 4)) {
            return true;
        }

        if (Type.POINTER == type.jffiType && Type.POINTER.size() == 4) {
            Class javaType = type.javaType;
            if (isDelegate(javaType)) {
                return true;
            }
            return  false && (Address.class == javaType
                    || Pointer.class.isAssignableFrom(javaType)
                    || Struct.class.isAssignableFrom(javaType)
                    || Buffer.class.isAssignableFrom(javaType));
        }

        return false;
    }

    static boolean isFastIntResult(Platform platform, ResultType parameterType) {
        return isFastIntType(platform, parameterType)
                || Type.VOID == parameterType.jffiType
                || (Type.POINTER == parameterType.jffiType && Type.POINTER.size() == 4)
                ;
    }


    static boolean isFastIntParameter(Platform platform, ParameterType parameterType) {
        return isFastIntType(platform, parameterType);
    }
}
