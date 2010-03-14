
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 *
 */
public final class DoubleByReference extends AbstractNumberReference<Double> {
    private static final Double DEFAULT = Double.valueOf(0d);

    /**
     * Creates a new reference to a double value initialized to zero.
     */
    public DoubleByReference() {
        super(DEFAULT);
    }

    /**
     * Creates a new reference to a double value
     * 
     * @param value the initial native value
     */
    public DoubleByReference(Double value) {
        super(checkNull(value));
    }

    /**
     * Creates a new reference to a double value
     *
     * @param value the initial native value
     */
    public DoubleByReference(double value) {
        super(value);
    }
    
    /**
     * Copies the double value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(Pointer buffer, long offset) {
        buffer.putDouble(offset, value);
    }

    /**
     * Copies the double value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(Pointer buffer, long offset) {
        this.value = buffer.getDouble(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return 8;
    }
}
