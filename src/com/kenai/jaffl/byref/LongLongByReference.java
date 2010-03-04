
package com.kenai.jaffl.byref;


import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Runtime;

/**
 *
 */
public class LongLongByReference extends AbstractPrimitiveReference<Long> {
    
    /**
     * Creates a new reference to a native longlong value
     * 
     * @param value the initial native value
     */
    public LongLongByReference(Long value) {
        super(value);
    }
    
    /**
     * Copies the Byte value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(MemoryIO buffer, long offset) {
        buffer.putLong(offset, value);
    }

    /**
     * Copies the Byte value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(MemoryIO buffer, long offset) {
        value = buffer.getLong(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return Long.SIZE / 8;
    }
}
