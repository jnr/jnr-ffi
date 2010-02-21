
package com.kenai.jaffl.provider;

import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.util.BufferUtil;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 */
abstract public class AbstractBufferMemoryIO extends AbstractMemoryIO {

    protected final ByteBuffer buffer;

    public AbstractBufferMemoryIO(Runtime runtime, ByteBuffer buffer) {
        super(runtime);
        this.buffer = buffer;
    }

    public final boolean isDirect() {
        return buffer.isDirect();
    }

    @Override
    public long address() {
        throw new UnsupportedOperationException("Not a direct memory object.");
    }

    public final ByteBuffer getByteBuffer() {
        return buffer;
    }
    public byte getByte(long offset) {
        return buffer.get((int) offset);
    }

    public short getShort(long offset) {
        return buffer.getShort((int) offset);
    }

    public int getInt(long offset) {
        return buffer.getInt((int) offset);
    }

    public long getLong(long offset) {
        return buffer.getLong((int) offset);
    }

    public float getFloat(long offset) {
        return buffer.getFloat((int) offset);
    }

    public double getDouble(long offset) {
        return buffer.getDouble((int) offset);
    }

    public void putByte(long offset, byte value) {
        buffer.put((int) offset, value);
    }

    public void putShort(long offset, short value) {
        buffer.putShort((int) offset, value);
    }

    public void putInt(long offset, int value) {
        buffer.putInt((int) offset, value);
    }

    public void putLong(long offset, long value) {
        buffer.putLong((int) offset, value);
    }

    public void putFloat(long offset, float value) {
        buffer.putFloat((int) offset, value);
    }

    public void putDouble(long offset, double value) {
        buffer.putDouble((int) offset, value);
    }

    public String getString(long offset, int size) {
        return BufferUtil.getString(BufferUtil.slice(buffer, (int) offset), Charset.defaultCharset());
    }

    public void putString(long offset, String string) {
        BufferUtil.putString(BufferUtil.slice(buffer, (int) offset), Charset.defaultCharset(), string);
    }

    @Override
    public void get(long offset, byte[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len).get(dst, off, len);
    }

    @Override
    public void get(long offset, short[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Short.SIZE / 8).asShortBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, int[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Integer.SIZE / 8).asIntBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Long.SIZE / 8).asLongBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Float.SIZE / 8).asFloatBuffer().get(dst, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Double.SIZE / 8).asDoubleBuffer().get(dst, off, len);
    }

    @Override
    public void put(long offset, byte[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len).put(dst, off, len);
    }

    @Override
    public void put(long offset, short[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Short.SIZE / 8).asShortBuffer().put(dst, off, len);
    }

    @Override
    public void put(long offset, int[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Integer.SIZE / 8).asIntBuffer().put(dst, off, len);
    }

    @Override
    public void put(long offset, long[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Long.SIZE / 8).asLongBuffer().put(dst, off, len);
    }

    @Override
    public void put(long offset, float[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Float.SIZE / 8).asFloatBuffer().put(dst, off, len);
    }

    @Override
    public void put(long offset, double[] dst, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Double.SIZE / 8).asDoubleBuffer().put(dst, off, len);
    }

    @Override
    public String getString(long offset) {
        return BufferUtil.getString(BufferUtil.slice(buffer, (int) offset), Charset.defaultCharset());
    }


    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        return BufferUtil.getString(BufferUtil.slice(buffer, (int) offset, maxLength),
                cs);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        BufferUtil.putString(BufferUtil.slice(buffer, (int) offset, maxLength), cs, string);
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        for (; offset > -1; ++offset) {
            if (buffer.get((int) offset) == value) {
                return (int) offset;
            }
        }
        return -1;
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        for (int i = 0; i < size; ++i) {
            buffer.put((int) offset + i, value);
        }
    }

    
}
