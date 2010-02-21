
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.util.BufferUtil;
import java.nio.ByteBuffer;

/**
 *
 */
public class AddressByReference extends AbstractPrimitiveReference<Address> {
    private final Runtime runtime;

    /**
     * Creates a new reference to an address value
     *
     * @param runtime the <tt>Runtime</tt> to use for address size
     * @param value the initial native value
     */
    public AddressByReference(Runtime runtime, Address value) {
        super(value);
        this.runtime = runtime;
    }

    /**
     * Creates a new reference to an address value
     *
     * @param value the initial native value
     */
    public AddressByReference(Runtime runtime) {
        super(new Address(0));
        this.runtime = runtime;
    }

    /**
     * Creates a new reference to an address value
     * 
     * @param value the initial native value
     */
    public AddressByReference(Address value) {
        super(value);
        this.runtime = Runtime.getDefault();
    }
    
    /**
     * Copies the address value to native memory
     * 
     * @param buffer the native memory buffer
     */
    public void marshal(ByteBuffer buffer) {
        BufferUtil.putAddress(runtime, buffer, 0, value.nativeAddress());
    }

    /**
     * Copies the address value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    public void unmarshal(ByteBuffer buffer) {
        value = new Address(BufferUtil.getAddress(runtime, buffer, 0));
    }
    
    /**
     * Gets the native size of type of reference
     * 
     * @return The size of the native type this reference points to
     */
    public int nativeSize() {
        return runtime.addressSize();
    }
}
