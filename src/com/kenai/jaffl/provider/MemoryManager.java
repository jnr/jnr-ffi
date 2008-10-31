
package com.kenai.jaffl.provider;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import java.nio.Buffer;

/**
 * Manages access to various types of java and native memory.
 */
public interface MemoryManager {
    public abstract MemoryIO allocate(int size);
    public abstract MemoryIO allocateDirect(int size);
    public abstract MemoryIO allocateDirect(int size, boolean clear);
    public abstract MemoryIO wrap(Pointer address);
    public abstract MemoryIO wrap(Pointer address, int size);
    public abstract Pointer getBufferPointer(Buffer buffer);
}
