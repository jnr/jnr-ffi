
package com.kenai.jaffl.provider.jna;

import com.sun.jna.Pointer;
import com.kenai.jaffl.MemoryIO;

/**
 * JNA implementation of memory I/O operations.
 */
abstract class JNAMemoryIO extends AbstractMemoryIO {
    /**
     * The native memory pointer
     */
    final Object memory;
    
    /**
     * Allocates a new block of java heap memory and wraps it in a {@link MemoryIO}
     * accessor.
     * 
     * @param size The size in bytes of memory to allocate.
     * 
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO allocate(int size) {
        return allocateDirect(size);
    }

    /**
     * Allocates a new block of java heap memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO allocate(int size, boolean clear) {
        return allocateDirect(size, clear);
    }
    
    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     * 
     * @param size The size in bytes of memory to allocate.
     * 
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO allocateDirect(int size) {
        return PointerMemoryIO.wrap(new com.sun.jna.Memory(size));
    }

    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO allocateDirect(int size, boolean clear) {
        com.sun.jna.Memory m = new com.sun.jna.Memory(size);
        if (clear) m.clear();
        return PointerMemoryIO.wrap(m);
    }
    
    /**
     * Creates a new JNA <tt>MemoryIO</tt> instance.
     * 
     * @param memory The memory object to wrap.
     */
    JNAMemoryIO(Object memory) {
        this.memory = memory;
    }
    
    /**
     * Gets the underlying memory object this <tt>MemoryIO</tt> is wrapping.
     * 
     * @return The native pointer or ByteBuffer.
     */
    Object getMemory() {
        return memory;
    }
    
    /**
     * Wraps a <tt>MemoryIO</tt> accessor around an existing native memory area.
     * 
     * @param ptr The native pointer to wrap.
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO wrap(Pointer ptr) {
        return new PointerMemoryIO(ptr);
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JNAMemoryIO) && ((JNAMemoryIO) obj).memory.equals(memory);
    }

    @Override
    public int hashCode() {
        return memory.hashCode();
    }
}
