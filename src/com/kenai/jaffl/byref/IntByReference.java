
package com.kenai.jaffl.byref;


import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * IntByReference is used when the address of a primitive int must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <p><blockquote><pre>
 * {@code
 * extern void get_a(int * ap);
 *
 * int foo(void) {
 *     int a;
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
 *     void get_a(@Out IntByReference ap);
 * }
 * }
 * </pre></blockquote>
 * <p>and used like this
 * <p><blockquote><pre>
 * IntByReference ap = new IntByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.getValue());
 * </pre></blockquote>
 */
public final class IntByReference extends AbstractPrimitiveReference<Integer> {

    /**
     * Creates a new reference to an integer value initialized to zero.
     */
    public IntByReference() {
        super(Integer.valueOf(0));
    }

    /**
     * Creates a new reference to an integer value
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
        this.value = buffer.getInt(offset);
    }
    
    /**
     * Gets the native size of type of reference
     * 
     * @return Integer.SIZE
     */
    public int nativeSize(Runtime runtime) {
        return 4;
    }
}
