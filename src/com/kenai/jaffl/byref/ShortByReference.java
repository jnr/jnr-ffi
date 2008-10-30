
package com.kenai.jaffl.byref;

import java.nio.ByteBuffer;

/**
 *
 */
public class ShortByReference extends AbstractPrimitiveReference<Short> {
    
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
    public void marshal(ByteBuffer buffer) {
        buffer.putShort(0, value);
    }

    /**
     * Copies the short value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(ByteBuffer buffer) {
        value = buffer.getShort(0);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize() {
        return Short.SIZE / 8;
    }
}
