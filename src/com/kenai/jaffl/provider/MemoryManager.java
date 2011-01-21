
package com.kenai.jaffl.provider;

import com.kenai.jaffl.Pointer;
import java.nio.ByteBuffer;

/**
 * Manages access to various types of java and native memory.
 */
public interface MemoryManager {
    public abstract Pointer allocate(int size);
    public abstract Pointer allocateDirect(int size);
    public abstract Pointer allocateDirect(int size, boolean clear);
    public abstract Pointer wrap(ByteBuffer buffer);
    public abstract Pointer wrap(long address);
    public abstract Pointer wrap(long address, long size);
}
