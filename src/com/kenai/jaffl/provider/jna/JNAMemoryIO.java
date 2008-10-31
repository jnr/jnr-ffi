
package com.kenai.jaffl.provider.jna;

/**
 * JNA implementation of memory I/O operations.
 */
abstract class JNAMemoryIO extends AbstractMemoryIO {
    /**
     * The native memory pointer
     */
    final Object memory;
    
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
    
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JNAMemoryIO) && ((JNAMemoryIO) obj).memory.equals(memory);
    }

    @Override
    public int hashCode() {
        return memory.hashCode();
    }
}
