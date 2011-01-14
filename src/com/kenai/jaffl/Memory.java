
package com.kenai.jaffl;

import java.nio.ByteBuffer;


/**
 * A utility for allocating memory that can be passed to native functions.
 */
public final class Memory {

    private Memory() {
    }
    
    /**
     * Allocates a new block of java memory and wraps it in a {@link Pointer}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return a {@code Pointer} instance that can access the memory.
     */
    public static final Pointer allocate(Runtime runtime, int size) {
        return runtime.getMemoryManager().allocate(size);
    }

    /**
     * Allocates a new block of native memory and wraps it in a {@link Pointer}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return a {@code Pointer} instance that can access the memory.
     */
    public static final Pointer allocateDirect(Runtime runtime, int size) {
        return runtime.getMemoryManager().allocateDirect(size);
    }

    /**
     * Allocates a new block of native memory and wraps it in a {@link Pointer}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     * @param clear Whether the memory contents should be cleared, or left as
     * random data.
     *
     * @return a {@code Pointer} instance that can access the memory.
     */
    public static final Pointer allocateDirect(Runtime runtime, int size, boolean clear) {
        return runtime.getMemoryManager().allocateDirect(size, clear);
    }

    /**
     * Wraps an existing ByteBuffer in a {@link Pointer} implementation so it can
     * be used as a parameter to native functions.
     *
     * <p>Wrapping a ByteBuffer is only neccessary if the native function parameter
     * was declared as a {@code Pointer}.  The if the method will always be used
     * with {@code ByteBuffer} parameters, then the parameter type can just be declared
     * as {@code ByteBuffer} and the conversion will be performed automatically.
     *
     * @param runtime the {@code Runtime} the wrapped {@code ByteBuffer} will
     * be used with.
     * @param buffer the {@code ByteBuffer} to wrap.
     *
     * @return a {@code Pointer} instance that will proxy all accesses to the ByteBuffer contents.
     */
    public static final Pointer wrap(Runtime runtime, ByteBuffer buffer) {
        return runtime.getMemoryManager().wrap(buffer);
    }
    
    public static Pointer newPointer(Runtime runtime, long address) {
        return runtime.getMemoryManager().wrap(address);
    }
    
    public static Pointer newPointer(Runtime runtime, long address, long size) {
        return runtime.getMemoryManager().wrap(address, size);
    }
}
