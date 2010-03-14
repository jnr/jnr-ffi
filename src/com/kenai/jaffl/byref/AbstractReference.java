
package com.kenai.jaffl.byref;

/**
 * An abstract class for common PrimitiveReference functionality
 */
abstract public class AbstractReference<T> implements ByReference<T> {
    T value;
    
    protected AbstractReference(T value) {
        this.value = value;
    }

    protected static <T> T checkNull(T value) {
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
}
