package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import jnr.ffi.*;
import jnr.ffi.Struct;

import java.lang.annotation.Annotation;
import java.nio.Buffer;

import static jnr.ffi.provider.jffi.CodegenUtils.ci;

/**
 *
 */
public class FastLongMethodGenerator extends AbstractFastNumericMethodGenerator {
    private static final int MAX_PARAMETERS = getMaximumFastLongParameters();
    private static final String[] signatures;

    private static final String[] methodNames = {
        "invokeVrL", "invokeLrL", "invokeLLrL", "invokeLLLrL", "invokeLLLLrL", "invokeLLLLLrL", "invokeLLLLLLrL"
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

    public FastLongMethodGenerator(BufferMethodGenerator bufgen) {
        super(bufgen);
    }

    @Override
    String getInvokerMethodName(Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes,
            Annotation[][] parameterAnnotations, boolean ignoreErrno) {
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

    public boolean isSupported(Signature signature) {
        final int parameterCount = signature.parameterTypes.length;

        if (signature.callingConvention != CallingConvention.DEFAULT || parameterCount > MAX_PARAMETERS) {
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


        for (int i = 0; i < parameterCount; i++) {
            if (!isFastLongParameter(platform, signature.parameterTypes[i], signature.parameterAnnotations[i])) {
                return false;
            }
        }

        return isFastLongResult(platform, signature.resultType, signature.resultAnnotations);
    }

    final static int getMaximumFastLongParameters() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeLLLLLLrL", Function.class,
                    long.class, long.class, long.class, long.class, long.class, long.class);
            return 6;
        } catch (Throwable t) {
            return 0;
        }
    }

    private static boolean isLongType(Platform platform, Class type, Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
            || Byte.class.isAssignableFrom(type) || byte.class == type
            || Short.class.isAssignableFrom(type) || short.class == type
            || Integer.class.isAssignableFrom(type) || int.class == type
            || Long.class == type || long.class == type
            || NativeLong.class == type
            || Pointer.class.isAssignableFrom(type)
            || Struct.class.isAssignableFrom(type)
            || Address.class == type
            ;
    }

    static boolean isFastLongResult(Platform platform, Class type, Annotation[] annotations) {
        return isLongType(platform, type, annotations)
            || Void.class.isAssignableFrom(type) || void.class == type
            || String.class == type
            ;

    }

    static boolean isFastLongParameter(Platform platform, Class type, Annotation[] annotations) {
        return isLongType(platform, type, annotations)
            || (Buffer.class.isAssignableFrom(type) && platform.addressSize() == 64)
            || (Callable.class.isAssignableFrom(type) && platform.addressSize() == 64)
            ;
    }
}
