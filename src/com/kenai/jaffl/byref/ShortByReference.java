
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 *
 */
public final class ShortByReference extends AbstractPrimitiveReference<Short> {
    
    /**
     * Creates a new reference to a short value
     * 
     * @param value the initial native value
     */
    public ShortByReference(Short value) {
        super(value);
    }
    
    /**
     * Copies the short value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(Pointer buffer, long offset) {
        buffer.putShort(offset, value);
    }

    /**
     * Copies the short value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(Pointer buffer, long offset) {
        value = buffer.getShort(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return Short.SIZE / 8;
    }
}
