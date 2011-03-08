/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi;

/**
 * The {@code Address} class wraps a native address in an object.  Both 32 bit and 64
 * bit native address values are wrapped in a singular {@code Address} type.
 *
 * <p>This class extends {@link java.lang.Number} and implements all {@code Number} methods for
 * converting to primitive integer types.
 *
 * <p>An {@code Address} instance is lighter weight than most {@link Pointer}
 * instances, and may be used when a native address needs to be stored in java,
 * but no other operations (such as reading/writing values) need be performed on
 * the native memory.  For most cases, a {@link Pointer} offers more flexibility
 * and should be preferred instead.
 */
public final class Address extends Number implements Comparable<Address> {
    /** A global instance of this class representing the C NULL value */
    private static final Address NULL = new Address(0L);

    /** The native numeric value of this {@code Address} */
    private final long address;


    /**
     * Creates a new address representation.
     * 
     * @param address the native address.
     */
    private Address(long address) {
        this.address = address;
    }
    
    /**
     * Creates a new address representation.
     * 
     * @param address the native address.
     */
    public Address(final Address address) {
        this.address = address.address;
    }

    /**
     * Gets the native memory address represented by this {@code Address} as a {@code long} integer.
     *
     * @return the native memory address
     */
    public final long address() {
        return address;
    }

    /**
     * Returns the value of this {@code Address} as an {@code int}.
     * On 64bit systems, this will result in the upper 32bits of the address being truncated.
     * 
     * @return the numeric value of this {@code Address} after conversion to an {@code int}.
     */
    @Override
    public final int intValue() {
        return (int) address;
    }
    
    /**
     * Returns the value of this {@code Address} as a {@code long}.
     * 
     * @return the numeric value of this {@code Address} after conversion to a {@code long}.
     */
    @Override
    public final long longValue() {
        return address;
    }
    
    /**
     * Returns the value of this {@code Address} as a {@code float}.
     *
     * This method is not particularly useful, and is here to fulfill the {@link java.lang.Number} interface contract.
     * 
     * @return the numeric value of this {@code Address} after conversion to a {@code float}.
     */
    @Override
    public final float floatValue() {
        return (float) address;
    }
    
    /**
     * Returns the value of this {@code Address} as a {@code double}.
     *
     * This method is not particularly useful, and is here to fulfill the {@link java.lang.Number} interface contract.
     *
     * @return the numeric value of this {@code Address} after conversion to a {@code double}.
     */
    @Override
    public final double doubleValue() {
        return (double) address;
    }
    
    /**
     * Returns the native value of this address.
     * 
     * @return an {@code long} value representing the native value of this address.
     */
    public final long nativeAddress() {
        return address;
    }

    /**
     * Returns a hash code for this {@code Address}.
     * 
     * @return a hash code for this {@code Address}.
     */
    @Override
    public final int hashCode() {
         return (int)(address ^ (address >>> 32));
    }

    /**
     * Compares this address to another address.
     * 
     * @param obj the other address to compare to.
     * @return {@code true} if this Address is equal to the other address, else false.
     */
    @Override
    public final boolean equals(Object obj) {
        return ((obj instanceof Address) && address == ((Address) obj).address)
                || (obj == null && address == 0);
    }
    
    /**
     * Returns a {@code String} object representing this {@code Address} as a decimal value.
     *
     * @return a string representation of this {@code Address}.
     */
    @Override
    public final String toString() {
        return Long.toString(address, 10);
    }

    /**
     * Returns a {@code String} object representing this {@code Address} as a hex value.
     *
     * @return a string representation of this {@code Address}.
     */
    public final String toHexString() {
        return Long.toString(address, 16);
    }
    
    /**
     * Compares two {@code Address} instances numerically.
     * 
     * @param other the other Address to compare to.
     * @return {@code 0} if {@code other} is equal to this instance, -1 if this
     * instance is numerically less than {@code other} or 1 if this instance is
     * numerically greater than {@code other}.
     */
    public final int compareTo(Address other) {
        return address < other.address ? -1 : address > other.address ? 1 : 0;
    }

    /**
     * Tests if this <tt>Address</tt> is equivalent to C NULL
     * 
     * @return true if the address is 0
     */
    public final boolean isNull() {
        return address == 0;
    }

    /**
     * Returns a Address instance representing the specified {@code long} value.
     *
     * @param address a {@code long} value
     * @return an {@code Address} instance representing {@code address}
     */
    public static Address valueOf(long address) {
        return address == 0 ? NULL : new Address(address);
    }

    /**
     * Returns a Address instance representing the specified {@code int} value.
     *
     * @param address an {@code int} value
     * @return an {@code Address} instance representing {@code address}
     */
    public static Address valueOf(int address) {
        return address == 0 ? NULL : new Address((long) address & 0xffffffffL);
    }
}
