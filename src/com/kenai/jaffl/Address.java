
package com.kenai.jaffl;

/**
 * Represents a native memory address.
 */
public final class Address extends Number implements Comparable<Address> {
    private static final Address NULL = new Address(0L);

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
     * Gets the native memory address as a long integer
     *
     * @return the native memory address
     */
    public final long address() {
        return address;
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

    /**
     * Returns a Address instance representing the specified long value
     *
     * @param address a long value
     * @return a <tt>Address</tt> instance representing <tt>address</tt>
     */
    public static Address valueOf(long address) {
        return address == 0 ? NULL : new Address(address);
    }

    /**
     * Returns a Address instance representing the specified long value
     *
     * @param address a long value
     * @return a <tt>Address</tt> instance representing <tt>address</tt>
     */
    public static Address valueOf(int address) {
        return address == 0 ? NULL : new Address(address);
    }

}
