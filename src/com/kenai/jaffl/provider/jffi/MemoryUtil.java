
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.provider.BoundedMemoryIO;
import com.kenai.jaffl.provider.NullMemoryIO;

public final class MemoryUtil {
    static final NullMemoryIO NULL = new NullMemoryIO(NativeRuntime.getInstance());

    private MemoryUtil() {}

    static final com.kenai.jaffl.Pointer newPointer(long ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }

    static final com.kenai.jaffl.Pointer newPointer(int ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }
    
    static final com.kenai.jaffl.Pointer newPointer(long ptr, long size) {
        return ptr != 0 ? new BoundedMemoryIO(new DirectMemoryIO(ptr), 0, size) : null;
    }
}
