
package com.kenai.jaffl.provider.jna;

import com.sun.jna.Pointer;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.provider.AbstractMemoryIO;
import java.nio.charset.Charset;

/**
 * A <tt>MemoryIO</tt> accessor that wraps a native pointer.
 */
final class PointerMemoryIO extends AbstractMemoryIO {

    /**
     * The native pointer.
     */
    private final Pointer ptr;

    PointerMemoryIO() {
        this(Pointer.NULL);
    }

    PointerMemoryIO(Pointer ptr) {
        this.ptr = ptr;
    }
    /*
    public Pointer getAddress() {
    return ptr;
    }
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PointerMemoryIO)) {
            return false;
        }
        PointerMemoryIO io = (PointerMemoryIO) obj;
        return ptr != null ? ptr.equals(io.ptr) : ptr == io.ptr;
    }

    @Override
    public int hashCode() {
        return ptr != null ? ptr.hashCode() : 0;
    }
    /**
     * Gets the underlying memory object this <tt>MemoryIO</tt> is wrapping.
     *
     * @return The native pointer or ByteBuffer.
     */
    Object getNativeMemory() {
        return ptr;
    }
    public boolean isNull() {
        return ptr == null;
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
        return new PointerMemoryIO(ptr.getPointer(offset));
    }

    public MemoryIO getMemoryIO(long offset, long size) {
        return new PointerMemoryIO(ptr.getPointer(offset));
    }

    public boolean isDirect() {
        return true;
    }

    @Override
    public long getAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public void setMemory(long offset, long size, byte value) {
        ptr.setMemory(offset, size, value);
    }

}
