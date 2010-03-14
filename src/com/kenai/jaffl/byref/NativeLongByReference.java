
package com.kenai.jaffl.byref;


import com.kenai.jaffl.NativeLong;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * NativeLongByReference is used when the address of a primitive C long must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <p><blockquote><pre>
 * {@code
 * extern void get_a(long * ap);
 *
 * long foo(void) {
 *     long a;
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
 *     void get_a(@Out NativeLongByReference ap);
 * }
 * }
 * </pre></blockquote>
 * <p>and used like this
 * <p><blockquote><pre>
 * NativeLongByReference ap = new NativeLongByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.getValue());
 * </pre></blockquote>
 */
public final class NativeLongByReference extends AbstractPrimitiveReference<NativeLong> {
    
    /**
     * Creates a new reference to a native long value initialized to zero.
     */
    public NativeLongByReference() {
        super(NativeLong.valueOf(0));
    }
    
    /**
     * Creates a new reference to a native long value
     * 
     * @param value the initial native value
     */
    public NativeLongByReference(NativeLong value) {
        super(value);
    }

    /**
     * Creates a new reference to a native long value
     *
     * @param value the initial native value
     */
    public NativeLongByReference(long value) {
        super(NativeLong.valueOf(value));
    }
    
    /**
     * Copies the long value to native memory
     * 
     * @param memory the native memory buffer
     */
    public void marshal(Pointer memory, long offset) {
        memory.putNativeLong(offset, value.longValue());
    }

    /**
     * Copies the long value from native memory
     * 
     * @param memory the native memory buffer.
     */
    public void unmarshal(Pointer memory, long offset) {
        this.value = NativeLong.valueOf(memory.getNativeLong(offset));
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return runtime.longSize();
    }
}
