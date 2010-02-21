
package com.kenai.jaffl.byref;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Runtime;


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
    public void marshal(MemoryIO buffer) {
        buffer.putByte(0, value);
    }

    /**
     * Copies the Byte value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(MemoryIO buffer) {
        value = buffer.getByte(0);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return Byte.SIZE / 8;
    }
}
