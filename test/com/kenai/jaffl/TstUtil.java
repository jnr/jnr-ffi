
package com.kenai.jaffl;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

public final class TstUtil {
    private TstUtil() {}
    public static final String getTestLibraryName() {
        return "test";
    }
    public static interface HelperLib {
        Pointer ptr_from_buffer(ByteBuffer buf);
    }
    public static <T> T loadTestLib(Class<T> interfaceClass) {
        final Map<LibraryOption, ?> options = Collections.emptyMap();
        return Library.loadLibrary(getTestLibraryName(), interfaceClass, options);
    }
    public static Pointer getDirectBufferPointer(ByteBuffer buf) {
        return TstUtil.loadTestLib(HelperLib.class).ptr_from_buffer(buf);
    }
}
