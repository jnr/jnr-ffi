
package com.kenai.jaffl.byref;


import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * LongLongByReference is used when the address of a native long long value must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <p><blockquote><pre>
 * {@code
 * extern void get_a(long long * ap);
 *
 * long long foo(void) {
 *     long long a;
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
 *     void get_a(@Out LongLongByReference ap);
 * }
 * }
 * </pre></blockquote>
 * <p>and used like this
 * <p><blockquote><pre>
 * LongLongByReference ap = new LongLongByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.getValue());
 * </pre></blockquote>
 */
public final class LongLongByReference extends AbstractPrimitiveReference<Long> {
    
    /**
     * Creates a new reference to a long long value initialized to zero.
     */
    public LongLongByReference() {
        super(Long.valueOf(0));
    }
    
    /**
     * Creates a new reference to a native longlong value
     * 
     * @param value the initial native value
     */
    public LongLongByReference(Long value) {
        super(value);
    }
    
    /**
     * Copies the value to native memory
     * 
     * @param memory the native memory buffer
     */
    public void marshal(Pointer memory, long offset) {
        memory.putLong(offset, value);
    }

    /**
     * Copies the value from native memory
     * 
     * @param memory the native memory buffer.
     */
    public void unmarshal(Pointer memory, long offset) {
        this.value = memory.getLong(offset);
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
