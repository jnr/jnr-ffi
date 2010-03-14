
package com.kenai.jaffl.byref;

/**
 * An abstract class for common PrimitiveReference functionality
 */
abstract public class AbstractNumberReference<T extends Number> extends Number implements ByReference<T> {
    T value;
    
    protected AbstractNumberReference(T value) {
        this.value = value;
    }

    protected static <T extends Number> T checkNull(T value) {
        if (value == null) {
            throw new NullPointerException("reference value cannot be null");
        }

        return value;
    }

    /**
     * Gets the current value the reference points to.
     *
     * @return the current value.
     */
    public T getValue() {
        return value;
    }
    
    @Override
    public final byte byteValue() {
        return value.byteValue();
    }
    
    @Override
    public final short shortValue() {
        return value.byteValue();
    }

    public final int intValue() {
        return value.intValue();
    }

    @Override
    public final long longValue() {
        return value.longValue();
    }

    @Override
    public final float floatValue() {
        return value.floatValue();
    }

    @Override
    public final double doubleValue() {
        return value.doubleValue();
    }
}
