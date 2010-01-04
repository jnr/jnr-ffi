package com.kenai.jaffl.provider;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import java.nio.charset.Charset;

/**
 *
 */
public class ShareMemoryIO extends AbstractMemoryIO implements DelegatingMemoryIO {

    private final MemoryIO io;
    private final long base;

    public ShareMemoryIO(MemoryIO parent, long offset) {
        this.io = parent;
        this.base = offset;
    }

    public final boolean isDirect() {
        return io.isDirect();
    }

    @Override
    public long address() {
        return io.address() + base;
    }


    public final MemoryIO getDelegatedMemoryIO() {
        return io;
    }

    @Override
    public byte getByte(long offset) {
        return io.getByte(base + offset);
    }

    @Override
    public short getShort(long offset) {
        return io.getShort(base + offset);
    }

    @Override
    public int getInt(long offset) {
        return io.getInt(base + offset);
    }

    @Override
    public long getLong(long offset) {
        return io.getLong(base + offset);
    }

    @Override
    public float getFloat(long offset) {
        return io.getFloat(base + offset);
    }

    @Override
    public double getDouble(long offset) {
        return io.getDouble(base + offset);
    }

    public MemoryIO getMemoryIO(long offset) {
        return io.getMemoryIO(base + offset);
    }

    public MemoryIO getMemoryIO(long offset, long size) {
        return io.getMemoryIO(base + offset, size);
    }

    public Pointer getPointer(long offset) {
        return io.getPointer(base + offset);
    }

    @Override
    public String getString(long offset) {
        return io.getString(base + offset);
    }


    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        return io.getString(base + offset, maxLength, cs);
    }


    @Override
    public void putByte(long offset, byte value) {
        io.putByte(base + offset, value);
    }

    @Override
    public void putShort(long offset, short value) {
        io.putShort(base + offset, value);
    }

    @Override
    public void putInt(long offset, int value) {
        io.putInt(base + offset, value);
    }

    @Override
    public void putLong(long offset, long value) {
        io.putLong(base + offset, value);
    }

    @Override
    public void putFloat(long offset, float value) {
        io.putFloat(base + offset, value);
    }

    @Override
    public void putDouble(long offset, double value) {
        io.putDouble(base + offset, value);
    }

    public void putPointer(long offset, Pointer value) {
        io.putPointer(base + offset, value);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        io.putString(base + offset, string, maxLength, cs);
    }

    @Override
    public void get(long offset, byte[] dst, int off, int len) {
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, byte[] dst, int off, int len) {
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, short[] dst, int off, int len) {
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, short[] dst, int off, int len) {
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, int[] dst, int off, int len) {
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, int[] dst, int off, int len) {
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, long[] dst, int off, int len) {
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, float[] dst, int off, int len) {
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, double[] dst, int off, int len) {
        io.put(base + offset, dst, off, len);
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        return io.indexOf(base + offset, value, maxlen);
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        io.setMemory(base + offset, size, value);
    }
}
