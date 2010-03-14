
package com.kenai.jaffl.byref;


import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 *
 */
public final class FloatByReference extends AbstractPrimitiveReference<Float> {
    private static final Float DEFAULT = Float.valueOf(0f);

    /**
     * Creates a new reference to a short value initialized to zero.
     */
    public FloatByReference() {
        super(DEFAULT);
    }

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
    public void marshal(Pointer buffer, long offset) {
        buffer.putFloat(offset, value);
    }

    /**
     * Copies the float value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(Pointer buffer, long offset) {
        this.value = buffer.getFloat(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return 4;
    }
}
