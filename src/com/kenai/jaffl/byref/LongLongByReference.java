
package com.kenai.jaffl.byref;


import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 *
 */
public final class LongLongByReference extends AbstractPrimitiveReference<Long> {
    
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
     * @param memory the native memory buffer
     */
    public void marshal(Pointer memory, long offset) {
        memory.putLong(offset, value);
    }

    /**
     * Copies the Byte value from native memory
     * 
     * @param memory the native memory buffer.
     */
    public void unmarshal(Pointer memory, long offset) {
        value = memory.getLong(offset);
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
