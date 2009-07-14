
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.AbstractMemoryIO;
import com.kenai.jaffl.provider.NullMemoryIO;
import com.kenai.jaffl.provider.StringIO;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class DirectMemoryIO extends AbstractMemoryIO {
    static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
    protected final long address;

    DirectMemoryIO(long address) {
        this.address = address;
    }
    public final long getAddress() {
        return address;
    }
    public final byte getByte(long offset) {
        return IO.getByte(address + offset);
    }

    public final short getShort(long offset) {
        return IO.getShort(address + offset);
    }

    public final int getInt(long offset) {
        return IO.getInt(address + offset);
    }

    public final long getLong(long offset) {
        return IO.getLong(address + offset);
    }

    public final float getFloat(long offset) {
        return IO.getFloat(address + offset);
    }

    public final double getDouble(long offset) {
        return IO.getDouble(address + offset);
    }

    public final void putByte(long offset, byte value) {
        IO.putByte(address + offset, value);
    }

    public final void putShort(long offset, short value) {
        IO.putShort(address + offset, value);
    }

    public final void putInt(long offset, int value) {
        IO.putInt(address + offset, value);
    }

    public final void putLong(long offset, long value) {
        IO.putLong(address + offset, value);
    }

    public final void putFloat(long offset, float value) {
        IO.putFloat(address + offset, value);
    }

    public final void putDouble(long offset, double value) {
        IO.putDouble(address + offset, value);
    }

    public final void get(long offset, byte[] dst, int off, int len) {
        IO.getByteArray(address + offset, dst, off, len);
    }

    public final void put(long offset, byte[] src, int off, int len) {
        IO.putByteArray(address + offset, src, off, len);
    }

    public final void get(long offset, short[] dst, int off, int len) {
        IO.getShortArray(address + offset, dst, off, len);
    }

    public final void put(long offset, short[] src, int off, int len) {
        IO.putShortArray(address + offset, src, off, len);
    }

    public final void get(long offset, int[] dst, int off, int len) {
        IO.getIntArray(address + offset, dst, off, len);
    }

    public final void put(long offset, int[] src, int off, int len) {
        IO.putIntArray(address + offset, src, off, len);
    }

    public final void get(long offset, long[] dst, int off, int len) {
        IO.getLongArray(address + offset, dst, off, len);
    }

    public final void put(long offset, long[] src, int off, int len) {
        IO.putLongArray(address + offset, src, off, len);
    }

    public final void get(long offset, float[] dst, int off, int len) {
        IO.getFloatArray(address + offset, dst, off, len);
    }

    public final void put(long offset, float[] src, int off, int len) {
        IO.putFloatArray(address + offset, src, off, len);
    }

    public final void get(long offset, double[] dst, int off, int len) {
        IO.getDoubleArray(address + offset, dst, off, len);
    }

    public final void put(long offset, double[] src, int off, int len) {
        IO.putDoubleArray(address + offset, src, off, len);
    }

    public MemoryIO getMemoryIO(long offset) {
        final long ptr = IO.getAddress(this.address + offset);
        return ptr != 0 ? new DirectMemoryIO(ptr) : new NullMemoryIO();
    }

    public MemoryIO getMemoryIO(long offset, long size) {
        final long ptr = getAddress(offset);
        return ptr != 0 ? new BoundedDirectMemoryIO(new DirectMemoryIO(ptr), 0, size) : new NullMemoryIO();
    }

    @Override
    public Pointer getPointer(long offset) {
        long ptr = IO.getAddress(address + offset);
        return ptr != 0 ? new JFFIPointer(ptr) : null;
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        if (value == null) {
            IO.putAddress(address + offset, 0L);
        } else if (value instanceof JFFIPointer) {
            IO.putAddress(address + offset, ((JFFIPointer) value).address);
        }
        throw new IllegalArgumentException("Invalid Pointer");
    }

    @Override
    public String getString(long offset) {
        final byte[] bytes = IO.getZeroTerminatedByteArray(address + offset);
        
        return StringIO.getStringIO().fromNative(ByteBuffer.wrap(bytes)).toString();
    }


    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        byte[] bytes = IO.getZeroTerminatedByteArray(address + offset, maxLength);
        final ByteBuffer buf = ByteBuffer.wrap(bytes);

        return StringIO.getStringIO().fromNative(buf).toString();
    }

    public int indexOf(long offset, byte value, int maxlen) {
        return (int) IO.indexOf(address, value, maxlen);
    }

    public final boolean isDirect() {
        return true;
    }

    public final void setMemory(long offset, long size, byte value) {
        IO.setMemory(this.address + offset, size, value);
    }

}
