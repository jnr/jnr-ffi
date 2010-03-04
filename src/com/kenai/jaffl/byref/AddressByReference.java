
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Runtime;

/**
 *
 */
public final class AddressByReference extends AbstractPrimitiveReference<Address> {
    /**
     * Creates a new reference to an address value
     * 
     * @param value the initial native value
     */
    public AddressByReference(Address value) {
        super(value);
    }
    
    /**
     * Copies the address value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(MemoryIO memory, long offset) {
        memory.putAddress(offset, value.nativeAddress());
    }

    /**
     * Copies the address value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(MemoryIO memory, long offset) {
        value = Address.valueOf(memory.getAddress(offset));
    }
    
    /**
     * Gets the native size of type of reference
     * 
     * @return The size of the native type this reference points to
     */
    public int nativeSize(Runtime runtime) {
        return runtime.addressSize();
    }
}
