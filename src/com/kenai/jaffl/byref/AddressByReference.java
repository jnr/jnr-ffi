
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.util.BufferUtil;
import java.nio.ByteBuffer;

/**
 *
 */
public class AddressByReference extends AbstractPrimitiveReference<Address> {
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
    public void marshal(ByteBuffer buffer) {
        BufferUtil.putAddress(buffer, 0, value.nativeAddress());
    }

    /**
     * Copies the address value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(ByteBuffer buffer) {
        value = new Address(BufferUtil.getAddress(buffer, 0));
    }
    
    /**
     * Gets the native size of type of reference
     * 
     * @return Integer.SIZE
     */
    public int nativeSize() {
        return Address.SIZE / 8;
    }
}
