package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import jnr.ffi.Address;
import jnr.ffi.Callable;
import jnr.ffi.Pointer;
import jnr.ffi.struct.Struct;

import java.lang.annotation.Annotation;
import java.nio.Buffer;

import static jnr.ffi.provider.jffi.CodegenUtils.ci;

/**
 *
 */
final class FastIntMethodGenerator extends AbstractFastNumericMethodGenerator {
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
    String getInvokerMethodName(Class returnType, Annotation[] resultAnnotations,
            Class[] parameterTypes, Annotation[][] parameterAnnotations, boolean ignoreErrno) {

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

    public boolean isSupported(Signature signature) {
        final int parameterCount = signature.parameterTypes.length;

        if (!signature.callingConvention.equals(CallingConvention.DEFAULT) || parameterCount > MAX_FASTINT_PARAMETERS) {
            return false;
        }

        final Platform platform = Platform.getPlatform();

        if (platform.getOS().equals(Platform.OS.WINDOWS)) {
            return false;
        }

        if (!platform.getCPU().equals(Platform.CPU.I386) && !platform.getCPU().equals(Platform.CPU.X86_64)) {
            return false;
        }

        for (int i = 0; i < parameterCount; i++) {
            if (!isFastIntParameter(platform, signature.parameterTypes[i], signature.parameterAnnotations[i])) {
                return false;
            }
        }

        return isFastIntResult(platform, signature.resultType, signature.resultAnnotations);
    }


    final static int getMaximumFastIntParameters() {
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


    private static boolean isFastIntType(Platform platform, Class type, Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || NumberUtil.isLong32(platform, type, annotations)
                || (Address.class == type && platform.addressSize() == 32)
                || (Pointer.class.isAssignableFrom(type) && platform.addressSize() == 32)
                || (Struct.class.isAssignableFrom(type) && platform.addressSize() == 32)
                || (Buffer.class.isAssignableFrom(type) && platform.addressSize() == 32)

//                || ((float.class == type || Float.class == type) && platform.getCPU() == Platform.CPU.I386)
                ;
    }


    static boolean isFastIntResult(Platform platform, Class type, Annotation[] annotations) {
        return isFastIntType(platform, type, annotations)
            || Void.class.isAssignableFrom(type) || void.class == type
            || (platform.addressSize() == 32 && String.class.isAssignableFrom(type))
            ;
    }

    static boolean isFastIntParameter(Platform platform, Class type, Annotation[] annotations) {
        return isFastIntType(platform, type, annotations)
            || (Callable.class.isAssignableFrom(type) && platform.addressSize() == 32)
            ;
    }
}
