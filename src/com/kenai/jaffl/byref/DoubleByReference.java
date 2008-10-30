
package com.kenai.jaffl.byref;

import java.nio.ByteBuffer;

/**
 *
 */
public class DoubleByReference extends AbstractPrimitiveReference<Double> {
    
    /**
     * Creates a new reference to a double value
     * 
     * @param value the initial native value
     */
    public DoubleByReference(Double value) {
        super(value);
    }
    
    /**
     * Copies the double value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(ByteBuffer buffer) {
        buffer.putDouble(0, value);
    }

    /**
     * Copies the double value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(ByteBuffer buffer) {
        value = buffer.getDouble(0);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize() {
        return Double.SIZE / 8;
    }
}
