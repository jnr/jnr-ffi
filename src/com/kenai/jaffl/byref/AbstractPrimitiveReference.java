
package com.kenai.jaffl.byref;

/**
 * An abstract class for common PrimitiveReference functionality
 */
abstract public class AbstractPrimitiveReference<T> implements ByReference<T> {
    T value;

    public AbstractPrimitiveReference(T value, boolean performNullCheck) {
        
        if (performNullCheck && value == null) {
            throw new NullPointerException("Initial reference value cannot be null");
        }

        this.value = value;
    }

    /* Internal constructor to avoid null checks */
    AbstractPrimitiveReference(T value) {
        this.value = value;
    }

    /**
     * Gets the current value the reference points to.
     *
     * @return the current value.
     */
    public T getValue() {
        return value;
    }
}
