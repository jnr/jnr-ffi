
package com.kenai.jaffl.byref;

import java.nio.ByteBuffer;

/**
 *
 */
public class ByteByReference extends AbstractPrimitiveReference<Byte> {
    
    /**
     * Creates a new reference to a byte value
     * 
     * @param value the initial native value
     */
    public ByteByReference(Byte value) {
        super(value);
    }
    
    /**
     * Copies the Byte value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(ByteBuffer buffer) {
        buffer.put(0, value);
    }

    /**
     * Copies the Byte value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(ByteBuffer buffer) {
        value = buffer.get(0);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize() {
        return Byte.SIZE / 8;
    }
}
