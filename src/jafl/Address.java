/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
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

package jafl;

/**
 * Represents a native memory address.
 */
public class Address extends Number implements Comparable<Address> {
    public static final int SIZE = Platform.getPlatform().addressSize();
    public static final int SHIFT = SIZE == 32 ? 2 : 3;
    public static final long MASK = SIZE == 32 ? 0xffffffffL : 0xffffffffffffffffL;

    protected final long address;
    
    /**
     * Creates a new address representation.
     * 
     * @param address the native address.
     */
    public Address(long address) {
        this.address = address & MASK;
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
     * Returns an integer representation of this address.
     * 
     * @return an integer value for this address.
     */
    @Override
    public final int intValue() {
        return (int) address;
    }
    
    /**
     * Returns an {@code long} representation of this address.
     * 
     * @return an {@code long} value for this address.
     */
    @Override
    public final long longValue() {
        return address;
    }
    
    /**
     * Returns an {@code float} representation of this address.
     * 
     * @return an {@code float} value for this address.
     */
    @Override
    public final float floatValue() {
        return (float) address;
    }
    
    /**
     * Returns an {@code double} representation of this address.
     * 
     * @return an {@code double} value for this address.
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
     * Gets a hash code for this {@code Address}.
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
     * Returns a string representation of this <code>Address</code>.
     *
     * @return a string representation of this <code>Address</code>.
     */
    @Override
    public String toString() {
        return getClass().getName() + String.format("[address=%x]", address);
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
}
