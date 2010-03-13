
package com.kenai.jaffl.byref;


import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * A class to pass a reference (or address of) an Integer to a method.
 */
public final class IntByReference extends AbstractPrimitiveReference<Integer> {
    /**
     * Creates a new reference to a integer value
     * 
     * @param value the initial native value
     */
    public IntByReference(Integer value) {
        super(value);
    }
    
    /**
     * Copies the integer value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(Pointer buffer, long offset) {
        buffer.putInt(offset, value);
    }

    /**
     * Copies the integer value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(Pointer buffer, long offset) {
        value = buffer.getInt(offset);
    }
    
    /**
     * Gets the native size of type of reference
     * 
     * @return Integer.SIZE
     */
    public int nativeSize(Runtime runtime) {
        return Integer.SIZE / 8;
    }
    
    /**
     * Gets the native type of the reference
     * 
     * @return Integer.class
     */
    @Override
    public Class nativeType() {
        return Integer.class;
    }
}
