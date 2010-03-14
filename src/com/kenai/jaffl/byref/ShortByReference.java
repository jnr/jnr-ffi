
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * ShortByReference is used when the address of a primitive short value must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <p><blockquote><pre>
 * {@code
 * extern void get_a(short * ap);
 *
 * short foo(void) {
 *     short a;
 *     // pass a reference to 'a' so get_a() can fill it out
 *     get_a(&a);
 *
 *     return a;
 * }
 * }
 * </pre></blockquote>
 * <p>Would be declared in java as
 * <p><blockquote><pre>
 * {@code
 * interface Lib {
 *     void get_a(@Out ShortByReference ap);
 * }
 * }
 * </pre></blockquote>
 * <p>and used like this
 * <p><blockquote><pre>
 * ShortByReference ap = new ShortByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.getValue());
 * </pre></blockquote>
 */
public final class ShortByReference extends AbstractPrimitiveReference<Short> {
    
    /**
     * Creates a new reference to a short value initialized to zero.
     */
    public ShortByReference() {
        super(Short.valueOf((short) 0));
    }

    /**
     * Creates a new reference to a short value.
     * 
     * @param value the initial native value
     */
    public ShortByReference(Short value) {
        super(value);
    }
    
    /**
     * Copies the short value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(Pointer buffer, long offset) {
        buffer.putShort(offset, value);
    }

    /**
     * Copies the short value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(Pointer buffer, long offset) {
        this.value = buffer.getShort(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return 2;
    }
}
