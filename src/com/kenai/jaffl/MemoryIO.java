
package com.kenai.jaffl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;


/**
 * Interface to reading/writing various types of memory
 */
public abstract class MemoryIO {
    /**
     * Allocates a new block of java heap memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    public static final MemoryIO allocate(int size) {
        return FFIProvider.getProvider().getMemoryManager().allocate(size);
    }
    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    public static final MemoryIO allocateDirect(int size) {
        return FFIProvider.getProvider().getMemoryManager().allocateDirect(size);
    }
    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     * @param clear Whether the memory contents should be cleared, or left as
     * random data.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    public static final MemoryIO allocateDirect(int size, boolean clear) {
        return FFIProvider.getProvider().getMemoryManager().allocateDirect(size, clear);
    }
    public static final MemoryIO wrap(Pointer ptr) {
        return FFIProvider.getProvider().getMemoryManager().wrap(ptr);
    }
    public static final MemoryIO wrap(Pointer ptr, int size) {
        return FFIProvider.getProvider().getMemoryManager().wrap(ptr, size);
    }
    public static final MemoryIO wrap(ByteBuffer buffer) {
        return FFIProvider.getProvider().getMemoryManager().wrap(buffer);
    }

    /**
     * Reads a 8 bit integer at the given offset.
     *
     * @param offset The offset from which the integer will be read.
     * @return The byte value at the offset.
     */
    abstract public byte getByte(long offset);

    /**
     * Reads a 16 bit integer at the given offset.
     *
     * @param offset The offset from which the integer will be read.
     * @return The short value at the offset.
     */
    abstract public short getShort(long offset);

    /**
     * Reads a 32 bit integer at the given offset.
     *
     * @param offset The offset from which the integer will be read.
     * @return The int value at the offset.
     */
    abstract public int getInt(long offset);

    /**
     * Reads a 64 bit integer at the given offset.
     *
     * @param offset The offset from which the integer will be read.
     * @return The long value at the offset.
     */
    abstract public long getLong(long offset);

    /**
     * Reads a 32 bit floating point value at the given offset.
     *
     * @param offset The offset from which the integer will be read.
     * @return The float value at the offset.
     */
    abstract public float getFloat(long offset);

    /**
     * Reads a 64 bit floating point value at the given offset.
     *
     * @param offset The offset from which the integer will be read.
     * @return The double value at the offset.
     */
    abstract public double getDouble(long offset);

    /**
     * Writes an 8 bit integer value at the given offset.
     *
     * @param offset The offset at which the value will be written.
     * @param value The value to be written.
     */
    abstract public void putByte(long offset, byte value);

    /**
     * Writes a 16 bit integer value at the given offset.
     *
     * @param offset The offset at which the value will be written.
     * @param value The value to be written.
     */
    abstract public void putShort(long offset, short value);

    /**
     * Writes a 32 bit integer value at the given offset.
     *
     * @param offset The offset at which the value will be written.
     * @param value The value to be written.
     */
    abstract public void putInt(long offset, int value);

    /**
     * Writes a 64 bit integer value at the given offset.
     *
     * @param offset The offset at which the value will be written.
     * @param value The value to be written.
     */
    abstract public void putLong(long offset, long value);

    /**
     * Writes a 32 bit floating point value at the given offset.
     *
     * @param offset The offset at which the value will be written.
     * @param value The value to be written.
     */
    abstract public void putFloat(long offset, float value);

    /**
     * Writes a 64 bit floating point value at the given offset.
     *
     * @param offset The offset at which the value will be written.
     * @param value The value to be written.
     */
    abstract public void putDouble(long offset, double value);

    /**
     * Bulk byte get method.
     *
     * This method reads an array of bytes at the given offset into the given
     * destination array.
     *
     * @param offset The offset at which the values will be read.
     * @param dst The array into which values are to be written.
     * @param idx The index within the destination array of the first value to be written.
     * @param len The number of values to be written to the destination array.
     */
    abstract public void get(long offset, byte[] dst, int idx, int len);

    /**
     * Bulk byte put method.
     *
     * This method writes an array of bytes at the given offset from the given
     * source array.
     *
     * @param offset The offset at which the values will be written.
     * @param src The source array from which values are to be read.
     * @param idx The index within the destination array of the first value to be read.
     * @param len The number of values to be read from the source array.
     */
    abstract public void put(long offset, byte[] src, int idx, int len);

    /**
     * Bulk short get method.
     *
     * This method reads an array of 16 bit integers at the given offset into the given
     * destination array.
     *
     * @param offset The offset at which the values will be read.
     * @param dst The array into which values are to be written.
     * @param idx The index within the destination array of the first value to be written.
     * @param len The number of values to be written to the destination array.
     */
    abstract public void get(long offset, short[] dst, int idx, int len);

    /**
     * Bulk short put method.
     *
     * This method writes an array of 16 bit integers at the given offset from 
     * the given array.
     *
     * @param offset The offset at which the values will be written.
     * @param src The source array from which values are to be read.
     * @param idx The index within the destination array of the first value to be read.
     * @param len The number of values to be read from the source array.
     */
    abstract public void put(long offset, short[] src, int idx, int len);

    /**
     * Bulk int get method.
     *
     * This method reads an array of 32 bit integers at the given offset into the given
     * destination array.
     *
     * @param offset The offset at which the values will be read.
     * @param dst The array into which values are to be written.
     * @param idx The index within the destination array of the first value to be written.
     * @param len The number of values to be written to the destination array.
     */
    abstract public void get(long offset, int[] dst, int idx, int len);

    /**
     * Bulk int put method.
     *
     * This method writes an array of 32 bit integers at the given offset from
     * the given array.
     *
     * @param offset The offset at which the values will be written.
     * @param src The source array from which values are to be read.
     * @param idx The index within the destination array of the first value to be read.
     * @param len The number of values to be read from the source array.
     */
    abstract public void put(long offset, int[] src, int idx, int len);

    /**
     * Bulk long get method.
     *
     * This method reads an array of 64 bit integers at the given offset into the given
     * destination array.
     *
     * @param offset The offset at which the values will be read.
     * @param dst The array into which values are to be written.
     * @param idx The index within the destination array of the first value to be written.
     * @param len The number of values to be written to the destination array.
     */
    abstract public void get(long offset, long[] dst, int idx, int len);

    /**
     * Bulk long put method.
     *
     * This method writes an array of 64 bit integers at the given offset from
     * the given array.
     *
     * @param offset The offset at which the values will be written.
     * @param src The source array from which values are to be read.
     * @param idx The index within the destination array of the first value to be read.
     * @param len The number of values to be read from the source array.
     */
    abstract public void put(long offset, long[] src, int idx, int len);

    /**
     * Bulk float get method.
     *
     * This method reads an array of 32 bit floats at the given offset into the given
     * destination array.
     *
     * @param offset The offset at which the values will be read.
     * @param dst The array into which values are to be written.
     * @param idx The index within the destination array of the first value to be written.
     * @param len The number of values to be written to the destination array.
     */
    abstract public void get(long offset, float[] dst, int idx, int len);

    /**
     * Bulk float put method.
     *
     * This method writes an array of 32 bit floats at the given offset from
     * the given array.
     *
     * @param offset The offset at which the values will be written.
     * @param src The source array from which values are to be read.
     * @param idx The index within the destination array of the first value to be read.
     * @param len The number of values to be read from the source array.
     */
    abstract public void put(long offset, float[] src, int idx, int len);

    /**
     * Bulk double get method.
     *
     * This method reads an array of 64 bit floats at the given offset into the given
     * destination array.
     *
     * @param offset The offset at which the values will be read.
     * @param dst The array into which values are to be written.
     * @param idx The index within the destination array of the first value to be written.
     * @param len The number of values to be written to the destination array.
     */
    abstract public void get(long offset, double[] dst, int idx, int len);

    /**
     * Bulk double put method.
     *
     * This method writes an array of 64 bit floats at the given offset from
     * the given array.
     *
     * @param offset The offset at which the values will be written.
     * @param src The source array from which values are to be read.
     * @param idx The index within the destination array of the first value to be read.
     * @param len The number of values to be read from the source array.
     */
    abstract public void put(long offset, double[] src, int idx, int len);


    abstract public MemoryIO getMemoryIO(long offset);
    abstract public MemoryIO getMemoryIO(long offset, long size);
    abstract public Pointer getPointer(long offset);
    abstract public void putPointer(long offset, Pointer value);
    abstract public int indexOf(long offset, byte value);    
    abstract public int indexOf(long offset, byte value, int maxlen);
    abstract public boolean isDirect();
    abstract public long getAddress(long offset);
    
    abstract public void putAddress(long offset, long value);
    abstract public void putAddress(long offset, Address value);
    abstract public long getNativeLong(long offset);
    abstract public void putNativeLong(long offset, long value);
    abstract public String getString(long offset, int maxLength, Charset cs);
    abstract public void putString(long offset, String string, int maxLength, Charset cs);

    abstract public MemoryIO slice(long offset);
    abstract public MemoryIO slice(long offset, long size);

    abstract public void transferTo(long offset, MemoryIO other, long otherOffset, long count);
    abstract public void setMemory(long offset, long size, byte value);
}
