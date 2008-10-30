
package com.kenai.jaffl.byref;

/**
 * An abstract class for common PrimitiveReference functionality
 */
abstract public class AbstractPrimitiveReference<T> implements ByReference<T> {
    protected T value;
    public AbstractPrimitiveReference(T value) {
        if (value == null) {
            throw new NullPointerException("Reference value cannot be null");
        }
        this.value = value;
    }
    public Class nativeType() {
        return value.getClass();
    }
    public void setValue(T value) {
        if (value == null) {
            throw new NullPointerException("Reference value cannot be null");
        }
        this.value = value;
    }
    public T getValue() {
        return value;
    }
}
