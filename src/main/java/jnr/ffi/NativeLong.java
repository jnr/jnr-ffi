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
 * Represents a C long.
 * <p>
 * In C, a <tt>long</tt> can be either 32 bits or 64bits, depending on the platform.
 * </p>
 * Replace any function parameters which are <tt>long</tt> in the C definition with 
 * a NativeLong.
 */
public final class NativeLong extends Number implements Comparable<NativeLong> {

    private static final NativeLong ZERO = new NativeLong(0);
    private static final NativeLong ONE = new NativeLong(1);
    private static final NativeLong MINUS_ONE = new NativeLong(-1);

    private final long value;
    
    /**
     * Creates a new <tt>NativeLong</tt> instance with the supplied value.
     * 
     * @param value a long or integer.
     */
    public NativeLong(long value) {
        this.value = value;
    }

    /**
     * Creates a new <tt>NativeLong</tt> instance with the supplied value.
     *
     * @param value an integer.
     */
    public NativeLong(int value) {
        this.value = value;
    }
    
    /**
     * Returns an integer representation of this <tt>NativeLong</tt>.
     * 
     * @return an integer value for this <tt>NativeLong</tt>.
     */
    @Override
    public final int intValue() {
        return (int) value;
    }
    
    /**
     * Returns an {@code long} representation of this <tt>NativeLong</tt>.
     * 
     * @return an {@code long} value for this <tt>NativeLong</tt>.
     */
    @Override
    public final long longValue() {
        return value;
    }
    
    /**
     * Returns an {@code float} representation of this <tt>NativeLong</tt>.
     * 
     * @return an {@code float} value for this <tt>NativeLong</tt>.
     */
    @Override
    public final float floatValue() {
        return (float) value;
    }
    
    /**
     * Returns an {@code double} representation of this <tt>NativeLong</tt>.
     * 
     * @return an {@code double} value for this <tt>NativeLong</tt>.
     */
    @Override
    public final double doubleValue() {
        return (double) value;
    }
    
    /**
     * Gets a hash code for this {@code NativeLong}.
     * 
     * @return a hash code for this {@code NativeLong}.
     */
    @Override
    public final int hashCode() {
         return (int)(value ^ (value >>> 32));
    }

    /**
     * Compares this <tt>NativeLong</tt> to another <tt>NativeLong</tt>.
     * 
     * @param obj the other <tt>NativeLong</tt> to compare to.
     * @return {@code true} if this <tt>NativeLong</tt> is equal to the other 
     * <tt>NativeLong</tt>, else false.
     */
    @Override
    public final boolean equals(Object obj) {
        return ((obj instanceof NativeLong) && value == ((NativeLong) obj).value);
    }
    
    /**
     * Returns a string representation of this <tt>NativeLong</tt>.
     *
     * @return a string representation of this <tt>NativeLong</tt>.
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
    
    /**
     * Compares two {@code NativeLong} instances numerically.
     * 
     * @param other the other NativeLong to compare to.
     * 
     * @return {@code 0} if {@code other} is equal to this instance, -1 if this
     * instance is numerically less than {@code other} or 1 if this instance is
     * numerically greater than {@code other}.
     */
    public final int compareTo(NativeLong other) {
        return value < other.value ? -1 : value > other.value ? 1 : 0;
    }

    /**
     * Internal cache of common native long values
     */
    private static final class Cache {
        private Cache() {}

        static final NativeLong[] cache = new NativeLong[256];

        static {
            for (int i = 0; i < cache.length; ++i) {
                cache[i] = new NativeLong(i - 128);
            }
            cache[128 + 0] = ZERO;
            cache[128 + 1] = ONE;
            cache[128 - 1] = MINUS_ONE;
        }

    }

    private static NativeLong _valueOf(final long value) {
        return value >= -128 && value <= 127
            ? Cache.cache[128 + (int) value] : new NativeLong(value);
    }

    private static NativeLong _valueOf(final int value) {
        return value >= -128 && value <= 127
            ? Cache.cache[128 + value] : new NativeLong(value);
    }

    /**
     * Returns a NativeLong instance representing the specified long value
     *
     * @param value a long value
     * @return a <tt>NativeLong</tt> instance representing <tt>value</tt>
     */
    public static NativeLong valueOf(final long value) {
        return value == 0 ? ZERO : value == 1 ? ONE : value == -1 ? MINUS_ONE : _valueOf(value);
    }

    /**
     * Returns a NativeLong instance representing the specified int value
     *
     * @param value a 32bit integer value
     * @return a <tt>NativeLong</tt> instance representing <tt>value</tt>
     */
    public static NativeLong valueOf(final int value) {
        return value == 0 ? ZERO : value == 1 ? ONE : value == -1 ? MINUS_ONE : _valueOf(value);
    }
}
