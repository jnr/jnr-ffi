package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;

/**
 *
 */
public class FastLongInvocationGenerator {
    static final boolean FAST_LONG_AVAILABLE = isFastLongAvailable();

    static boolean isFastLongAvailable() {
        try {
            com.kenai.jffi.Invoker.class.getDeclaredMethod("invokeLLLLLLrL", Function.class, long.class, long.class, long.class, long.class, long.class, long.class);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
