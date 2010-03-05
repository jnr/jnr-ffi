
package com.kenai.jaffl.byref;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * Represents a pointer-to-pointer parameter.
 */
public final class PointerByReference extends AbstractPrimitiveReference<Pointer>{
    /**
     * Creates a new reference to a pointer value
     *
     * @param value the initial pointer value
     */
    public PointerByReference(Pointer value) {
        super(value);
    }

    public final void marshal(MemoryIO memory, long offset) {
        memory.putPointer(offset, this.value);
    }

    public final void unmarshal(MemoryIO memory, long offset) {
        this.value = memory.getPointer(offset);
    }

    public final int nativeSize(Runtime runtime) {
        return runtime.addressSize();
    }
}
