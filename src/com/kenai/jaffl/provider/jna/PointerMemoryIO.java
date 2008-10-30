
package com.kenai.jaffl.provider.jna;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.kenai.jaffl.MemoryIO;
import java.nio.charset.Charset;

/**
 * A <tt>MemoryIO</tt> accessor that wraps a native pointer.
 */
final class PointerMemoryIO extends JNAMemoryIO {

    /**
     * The native pointer.
     */
    private final Pointer ptr;

    /**
     * Wraps a <tt>MemoryIO</tt> accessor around an existing native memory area.
     *
     * @param ptr The native pointer to wrap.
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static PointerMemoryIO wrap(Pointer ptr) {
        return new PointerMemoryIO(ptr);
    }

    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO allocate(int size) {
        return new PointerMemoryIO(new Memory(size));
    }

    PointerMemoryIO() {
        this(Pointer.NULL);
    }

    PointerMemoryIO(Pointer ptr) {
        super(ptr);
        this.ptr = ptr;
    }
    /*
    public Pointer getAddress() {
    return ptr;
    }
     */

    public boolean isNull() {
        return Pointer.NULL.equals(ptr);
    }

    public byte getByte(long offset) {
        return ptr.getByte(offset);
    }

    public short getShort(long offset) {
        return ptr.getShort(offset);
    }

    public int getInt(long offset) {
        return ptr.getInt(offset);
    }

    public long getLong(long offset) {
        return ptr.getLong(offset);
    }

    public float getFloat(long offset) {
        return ptr.getFloat(offset);
    }

    public double getDouble(long offset) {
        return ptr.getDouble(offset);
    }

    public void putByte(long offset, byte value) {
        ptr.setByte(offset, value);
    }

    public void putShort(long offset, short value) {
        ptr.setShort(offset, value);
    }

    public void putInt(long offset, int value) {
        ptr.setInt(offset, value);
    }

    public void putLong(long offset, long value) {
        ptr.setLong(offset, value);
    }

    public void putFloat(long offset, float value) {
        ptr.setFloat(offset, value);
    }

    public void putDouble(long offset, double value) {
        ptr.setDouble(offset, value);
    }

    public void putPointer(long offset, Pointer value) {
        ptr.setPointer(offset, value);
    }

    public void get(long offset, byte[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, byte[] dst, int off, int len) {
        ptr.write(offset, dst, off, len);
    }

    public void get(long offset, short[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, short[] dst, int off, int len) {
        ptr.write(offset, dst, off, len);
    }

    public void get(long offset, int[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, int[] dst, int off, int len) {
        ptr.write(offset, dst, off, len);
    }

    public void get(long offset, long[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, long[] dst, int off, int len) {
        ptr.write(offset, dst, off, len);
    }

    public void get(long offset, float[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, float[] dst, int off, int len) {
        ptr.write(offset, dst, off, len);
    }

    public void get(long offset, double[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, double[] dst, int off, int len) {
        ptr.write(offset, dst, off, len);
    }

    @Override
    public int indexOf(long offset, byte value) {
        return (int) ptr.indexOf(offset, value);
    }

    public int indexOf(long offset, byte value, int maxlen) {
        return (int) ptr.indexOf(offset, value);
    }

    public PointerMemoryIO getMemoryIO(long offset) {
        return wrap(ptr.getPointer(offset));
    }

    public MemoryIO getMemoryIO(long offset, long size) {
        return wrap(ptr.getPointer(offset));
    }

    public boolean isDirect() {
        return true;
    }

    public com.kenai.jaffl.Pointer getPointer(long offset) {
        return new JNAPointer(ptr.getPointer(offset));
    }

    public void putPointer(long offset, com.kenai.jaffl.Pointer value) {
        ptr.setPointer(offset, ((JNAPointer) value).getNativePointer());
    }

    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        return ptr.getString(offset);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        ptr.setString(offset, string);
    }
}
