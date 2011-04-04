package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Platform;
import jnr.ffi.NativeLong;
import jnr.ffi.Pointer;
import jnr.ffi.struct.Struct;

import java.lang.annotation.Annotation;

import static jnr.ffi.provider.jffi.CodegenUtils.ci;

/**
 *
 */
public class FastLongMethodGenerator extends AbstractFastNumericMethodGenerator {
    private static final int MAX_FASTLONG_PARAMETERS = getMaximumFastLongParameters();
    private static final String[] signatures;

    private static final String[] methodNames = {
        "invokeVrL", "invokeLrL", "invokeLLrL", "invokeLLLrL", "invokeLLLLrL", "invokeLLLLLrL", "invokeLLLLLLrL"
    };

    static {
        signatures = new String[MAX_FASTLONG_PARAMETERS + 1];
        for (int i = 0; i <= MAX_FASTLONG_PARAMETERS; i++) {
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

        if (parameterCount <= MAX_FASTLONG_PARAMETERS && parameterCount <= methodNames.length) {
            return methodNames[parameterCount];

        } else {
            throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
        }
    }

    @Override
    String getInvokerSignature(int parameterCount, Class nativeIntType) {

        if (parameterCount <= MAX_FASTLONG_PARAMETERS && parameterCount <= signatures.length) {
            return signatures[parameterCount];

        } else {
            throw new IllegalArgumentException("invalid fast-int parameter count: " + parameterCount);
        }
    }

    @Override
    Class getInvokerType() {
        return long.class;
    }

    public boolean isSupported(Class returnType, Annotation[] resultAnnotations, Class[] parameterTypes,
            Annotation[][] parameterAnnotations, CallingConvention convention) {
        final int parameterCount = parameterTypes.length;

        System.out.println("fast-long checking for supported. paramcount=" + parameterCount);
        System.out.println("longSize=" + Platform.getPlatform().longSize());
        System.out.println("data.model=" + Integer.getInteger("sun.arch.data.model", 0));

        if (parameterCount > MAX_FASTLONG_PARAMETERS) {
            return false;
        }
        final Platform platform = Platform.getPlatform();
        // Only supported on amd64 arches
        if (platform.getCPU() != Platform.CPU.X86_64) {
            return false;
        }

        for (int i = 0; i < parameterCount; i++) {
            if (!isFastLongParameter(platform, parameterTypes[i], parameterAnnotations[i])) {
                return false;
            }
        }

        boolean supported = isFastLongResult(platform, returnType, resultAnnotations);
        System.out.println("fast-long method supported=" + supported);
        return supported;
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
            ;
    }
}
