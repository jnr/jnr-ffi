
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * AddressByReference is used when the address of a pointer must be passed
 * as a parameter to a function.
 *
 * <p>For example, the following C code,
 * <p><blockquote><pre>
 * {@code
 * extern void get_a(void** ap);
 *
 * void* foo(void) {
 *     void* a;
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
 *     void get_a(@Out PointerByReference ap);
 * }
 * }
 * </pre></blockquote>
 * <p>and used like this
 * <p><blockquote><pre>
 * PointerByReference ap = new PointerByReference();
 * lib.get_a(ap);
 * Pointer ptr = ap.getValue();
 * System.out.println("ptr from lib=" + a.getValue());
 * System.out.println("ptr contents=" + ptr.getInt(0));
 * </pre></blockquote>
 */
public final class PointerByReference extends AbstractPrimitiveReference<Pointer>{
    /**
     * Creates a new reference to a pointer value with a null default value.
     */
    public PointerByReference() {
        super(null, false);
    }

    /**
     * Creates a new reference to a pointer value
     *
     * @param value the initial pointer value
     */
    public PointerByReference(Pointer value) {
        super(value, false);
    }

    public final void marshal(Pointer memory, long offset) {
        memory.putPointer(offset, this.value);
    }

    public final void unmarshal(Pointer memory, long offset) {
        this.value = memory.getPointer(offset);
    }

    public final int nativeSize(Runtime runtime) {
        return runtime.addressSize();
    }
}
