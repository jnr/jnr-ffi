
package com.kenai.jaffl.byref;


import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Runtime;

/**
 *
 */
public class FloatByReference extends AbstractPrimitiveReference<Float> {
    
    /**
     * Creates a new reference to a float value
     * 
     * @param value the initial native value
     */
    public FloatByReference(Float value) {
        super(value);
    }
    
    /**
     * Copies the float value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(MemoryIO buffer, long offset) {
        buffer.putFloat(offset, value);
    }

    /**
     * Copies the float value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(MemoryIO buffer, long offset) {
        value = buffer.getFloat(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return Float.SIZE / 8;
    }
}
