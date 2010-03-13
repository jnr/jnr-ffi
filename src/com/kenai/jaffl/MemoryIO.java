
package com.kenai.jaffl;

import java.nio.ByteBuffer;


/**
 * Interface to reading/writing various types of memory
 */
public abstract class MemoryIO implements Pointer {
    
    /**
     * Allocates a new block of java heap memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    public static final MemoryIO allocate(Runtime runtime, int size) {
        return runtime.getMemoryManager().allocate(size);
    }
    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    public static final MemoryIO allocateDirect(Runtime runtime, int size) {
        return runtime.getMemoryManager().allocateDirect(size);
    }

    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     * @param clear Whether the memory contents should be cleared, or left as
     * random data.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    public static final MemoryIO allocateDirect(Runtime runtime, int size, boolean clear) {
        return runtime.getMemoryManager().allocateDirect(size, clear);
    }
    
    public static final MemoryIO wrap(Runtime runtime, ByteBuffer buffer) {
        return runtime.getMemoryManager().wrap(buffer);
    }
}
