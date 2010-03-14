
package com.kenai.jaffl.byref;

/**
 * An abstract class for common PrimitiveReference functionality
 */
abstract public class AbstractPrimitiveReference<T> implements ByReference<T> {
    private final boolean performNullCheck;
    T value;

    public AbstractPrimitiveReference(T value, boolean performNullCheck) {
        this.performNullCheck = performNullCheck;

        if (performNullCheck && value == null) {
            throw new NullPointerException("Initial reference value cannot be null");
        }
        this.value = value;
    }

    /* Internal constructor to avoid null checks */
    AbstractPrimitiveReference(T value) {
        this.performNullCheck = false;
        this.value = value;
    }

    public Class nativeType() {
        return value.getClass();
    }

    protected void setValue(T value) {
        if (performNullCheck && value == null) {
            throw new NullPointerException("Reference value cannot be null");
        }

        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
