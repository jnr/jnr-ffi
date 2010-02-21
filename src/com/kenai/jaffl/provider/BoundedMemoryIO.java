
package com.kenai.jaffl.provider;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import java.nio.charset.Charset;


public class BoundedMemoryIO extends AbstractMemoryIO implements DelegatingMemoryIO {

    private final long base,  size;
    private final MemoryIO io;

    public BoundedMemoryIO(MemoryIO parent, long offset, long size) {
        super(parent.getRuntime());
        this.io = parent;
        this.base = offset;
        this.size = size;
    }

    public boolean isDirect() {
        return getDelegatedMemoryIO().isDirect();
    }
    
    public long address() {
        return getDelegatedMemoryIO().address();
    }

    public MemoryIO getDelegatedMemoryIO() {
        return io;
    }

    @Override
    public byte getByte(long offset) {
        checkBounds(size, offset, 1);
        return io.getByte(base + offset);
    }

    @Override
    public short getShort(long offset) {
        checkBounds(size, offset, 2);
        return io.getShort(base + offset);
    }

    @Override
    public int getInt(long offset) {
        checkBounds(size, offset, 4);
        return io.getInt(base + offset);
    }

    @Override
    public long getLong(long offset) {
        checkBounds(size, offset, 8);
        return io.getLong(base + offset);
    }

    @Override
    public float getFloat(long offset) {
        checkBounds(size, offset, 4);
        return io.getFloat(base + offset);
    }

    @Override
    public double getDouble(long offset) {
        checkBounds(size, offset, 8);
        return io.getDouble(base + offset);
    }
    public Pointer getPointer(long offset) {
        checkBounds(size, offset, Address.SIZE / 8);
        return io.getPointer(base + offset);
    }

    public MemoryIO getMemoryIO(long offset) {
        checkBounds(this.size, base + offset, Address.SIZE / 8);
        return io.getMemoryIO(base + offset);
    }

    public MemoryIO getMemoryIO(long offset, long size) {
        checkBounds(this.size, base + offset, Address.SIZE / 8);
        return io.getMemoryIO(base + offset, size);
    }
    @Override
    public void putByte(long offset, byte value) {
        checkBounds(size, offset, 1);
        io.putByte(base + offset, value);
    }

    @Override
    public void putShort(long offset, short value) {
        checkBounds(size, offset, 2);
        io.putShort(base + offset, value);
    }

    @Override
    public void putInt(long offset, int value) {
        checkBounds(size, offset, 4);
        io.putInt(base + offset, value);
    }

    @Override
    public void putLong(long offset, long value) {
        checkBounds(size, offset, 8);
        io.putLong(base + offset, value);
    }

    @Override
    public void putFloat(long offset, float value) {
        checkBounds(size, offset, 4);
        io.putFloat(base + offset, value);
    }

    @Override
    public void putDouble(long offset, double value) {
        checkBounds(size, offset, 8);
        io.putDouble(base + offset, value);
    }
    public void putPointer(long offset, Pointer value) {
        checkBounds(size, offset, Address.SIZE / 8);
        io.putPointer(base + offset, value);
    }
    @Override
    public void get(long offset, byte[] dst, int off, int len) {
        checkBounds(size, offset, len);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, byte[] dst, int off, int len) {
        checkBounds(size, offset, len);
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, short[] dst, int off, int len) {
        checkBounds(size, offset, len * Short.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, short[] dst, int off, int len) {
        checkBounds(size, offset, len * Short.SIZE / 8);
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, int[] dst, int off, int len) {
        checkBounds(size, offset, len * Integer.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, int[] dst, int off, int len) {
        checkBounds(size, offset, len * Integer.SIZE / 8);
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        checkBounds(size, offset, len * Long.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, long[] dst, int off, int len) {
        checkBounds(size, offset, len * Long.SIZE / 8);
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        checkBounds(size, offset, len * Float.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, float[] dst, int off, int len) {
        checkBounds(size, offset, len * Float.SIZE / 8);
        io.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        checkBounds(size, offset, len * Double.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, double[] dst, int off, int len) {
        checkBounds(size, offset, len * Double.SIZE / 8);
        io.put(base + offset, dst, off, len);
    }

    @Override
    public long getAddress(long offset) {
        checkBounds(size, offset, Address.SIZE >> 3);
        return io.getAddress(base + offset);
    }

    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        checkBounds(size, offset, maxLength);
        return io.getString(base + offset, maxLength, cs);
    }

    @Override
    public String getString(long offset) {
        return io.getString(base + offset, (int) size, Charset.defaultCharset());
    }

    @Override
    public void putAddress(long offset, long value) {
        checkBounds(size, offset, Address.SIZE >> 3);
        io.putAddress(base + offset, value);
    }

    @Override
    public void putAddress(long offset, Address value) {
        checkBounds(size, offset, Address.SIZE >> 3);
        io.putAddress(base + offset, value);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        checkBounds(size, offset, maxLength);
        io.putString(base + offset, string, maxLength, cs);
    }

    @Override
    public int indexOf(long offset, byte value) {
        return io.indexOf(base + offset, value, (int) size);
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        checkBounds(size, offset, maxlen);
        return io.indexOf(base + offset, value, maxlen);
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        checkBounds(this.size, base + offset, size);
        io.setMemory(base + offset, size, value);
    }
}
