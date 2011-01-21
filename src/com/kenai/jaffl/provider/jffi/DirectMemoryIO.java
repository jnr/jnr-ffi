
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.provider.AbstractMemoryIO;
import com.kenai.jaffl.provider.StringIO;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

class DirectMemoryIO extends AbstractMemoryIO {
    static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
    protected final long address;

    DirectMemoryIO(Runtime runtime, long address) {
        super(runtime);
        this.address = address;
    }

    DirectMemoryIO(Runtime runtime, int address) {
        super(runtime);
        this.address = (long) address & 0xffffffffL;
    }

    DirectMemoryIO(long address) {
        this(NativeRuntime.getInstance(), address);
    }

    DirectMemoryIO(int address) {
        this(NativeRuntime.getInstance(), address);
    }

    public final long address() {
        return address;
    }

    public long size() {
        return Long.MAX_VALUE;
    }



    @Override
    public int hashCode() {
        return (int) ((address << 32L) ^ address);
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pointer && ((Pointer) obj).address() == address && ((Pointer) obj).getRuntime().equals(getRuntime());
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
    
    public final long getLongLong(long offset) {
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
    
    public final void putLongLong(long offset, long value) {
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

    @Override
    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(IO.getAddress(address + offset));
    }
    
    public Pointer getPointer(long offset, long size) {
        return MemoryUtil.newPointer(IO.getAddress(this.address + offset), size);
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        IO.putAddress(address + offset, value.address());
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

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        ByteBuffer buf = StringIO.getStringIO().toNative(string, 0, true);
        IO.putByteArray(address + offset, buf.array(), buf.arrayOffset() + buf.position(), buf.remaining());
    }


    public int indexOf(long offset, byte value, int maxlen) {
        return (int) IO.indexOf(address + offset, value, maxlen);
    }

    public final boolean isDirect() {
        return true;
    }

    public final void setMemory(long offset, long size, byte value) {
        IO.setMemory(this.address + offset, size, value);
    }

}
