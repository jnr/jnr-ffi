package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import jnr.ffi.Callable;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.struct.Struct;

import java.lang.annotation.Annotation;
import java.nio.Buffer;

import static jnr.ffi.provider.jffi.CodegenUtils.ci;

/**
 *
 */
class FastNumericMethodGenerator extends AbstractFastNumericMethodGenerator {
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

    public boolean isSupported(Signature signature) {
        final int parameterCount = signature.parameterTypes.length;

        if (signature.callingConvention != CallingConvention.DEFAULT || parameterCount > MAX_PARAMETERS) {
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

        for (int i = 0; i < parameterCount; i++) {
            if (!isFastNumericParameter(platform, signature.parameterTypes[i], signature.parameterAnnotations[i])) {
                return false;
            }
        }

        return isFastNumericResult(platform, signature.resultType, signature.resultAnnotations);
    }

    @Override
    String getInvokerMethodName(Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes,
            Annotation[][] parameterAnnotations, boolean ignoreErrno) {
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

    private static boolean isNumericType(Platform platform, Class type, Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
            || Byte.class.isAssignableFrom(type) || byte.class == type
            || Short.class.isAssignableFrom(type) || short.class == type
            || Integer.class.isAssignableFrom(type) || int.class == type
            || Long.class == type || long.class == type
            || NativeLong.class == type
            || Pointer.class.isAssignableFrom(type)
            || Struct.class.isAssignableFrom(type)
            || float.class == type || Float.class == type
            || double.class == type || Double.class == type
            ;
    }

    final static boolean isFastNumericResult(Platform platform, Class type, Annotation[] annotations) {
        return isNumericType(platform, type, annotations)
            || String.class == type
            ;
    }

    final static boolean isFastNumericParameter(Platform platform, Class type, Annotation[] annotations) {
        return isNumericType(platform, type, annotations)
            || Buffer.class.isAssignableFrom(type)
            || Callable.class.isAssignableFrom(type)
            ;
    }

    final static int getMaximumParameters() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeNNNNNNrN", Function.class,
                    long.class, long.class, long.class, long.class, long.class, long.class);
            return 6;
        } catch (Throwable t) {
            return 0;
        }
    }
}
