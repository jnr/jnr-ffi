package jnr.ffi;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

public final class TstUtil {
    private TstUtil() {}
    private static FFIProvider provider;
    private static String libname = "test";

    public static void setProvider(FFIProvider provider) {
        TstUtil.provider = provider;
    }

    public static void setPath(String path) {
        TstUtil.libname = path;
    }
    
    public static interface HelperLib {
        Pointer ptr_from_buffer(ByteBuffer buf);
    }

    public static <T> T loadTestLib(Class<T> interfaceClass) {
        final Map<LibraryOption, ?> options = Collections.emptyMap();
        return loadTestLib(interfaceClass, options);
    }
    public static <T> T loadTestLib(Class<T> interfaceClass, Map<LibraryOption, ?> options) {
        if (provider != null) {
            return provider.loadLibrary(libname, interfaceClass, options);
        } else {
            return Library.loadLibrary(libname, interfaceClass, options);
        }
    }

    public static Pointer getDirectBufferPointer(ByteBuffer buf) {
        return TstUtil.loadTestLib(HelperLib.class).ptr_from_buffer(buf);
    }
}
