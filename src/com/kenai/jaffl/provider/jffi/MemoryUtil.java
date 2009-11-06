
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.provider.NullMemoryIO;

public final class MemoryUtil {

    private MemoryUtil() {}

    static final com.kenai.jaffl.Pointer newPointer(long ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : null;
    }

    static final com.kenai.jaffl.MemoryIO newMemoryIO(long ptr) {
        return ptr != 0 ? new DirectMemoryIO(ptr) : NullMemoryIO.INSTANCE;
    }

    static final com.kenai.jaffl.MemoryIO newMemoryIO(long ptr, long size) {
        return ptr != 0 ? new BoundedDirectMemoryIO(new DirectMemoryIO(ptr), 0, size) : NullMemoryIO.INSTANCE;
    }

    static final long getAddress(MemoryIO ptr) {
        if (ptr == null) {
            return 0L;
        } else if (ptr instanceof JFFIPointer) {
            return ((JFFIPointer) ptr).address;
        } else if (ptr instanceof DirectMemoryIO) {
            return ((DirectMemoryIO) ptr).address;
        }

        throw new IllegalArgumentException("attempted to get address of non-direct memory. " + ptr.getClass());
    }
}
