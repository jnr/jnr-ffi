/* 
 * Copyright (C) 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

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
