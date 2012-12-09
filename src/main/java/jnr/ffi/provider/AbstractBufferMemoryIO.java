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

package jnr.ffi.provider;

import jnr.ffi.Runtime;
import jnr.ffi.util.BufferUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 */
abstract public class AbstractBufferMemoryIO extends AbstractMemoryIO {

    protected final ByteBuffer buffer;

    public AbstractBufferMemoryIO(Runtime runtime, ByteBuffer buffer, long address) {
        super(runtime, address, buffer.isDirect());
        this.buffer = buffer;
    }


    public long size() {
        return buffer.remaining();
    }

    public final ByteBuffer getByteBuffer() {
        return buffer;
    }

    @Override
    public int arrayLength() {
        return getByteBuffer().remaining();
    }

    @Override
    public int arrayOffset() {
        return getByteBuffer().arrayOffset();
    }

    @Override
    public Object array() {
        return getByteBuffer().array();
    }

    @Override
    public boolean hasArray() {
        return getByteBuffer().hasArray();
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

    public long getLongLong(long offset) {
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
    
    public void putLongLong(long offset, long value) {
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
    public void put(long offset, int[] src, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Integer.SIZE / 8).asIntBuffer().put(src, off, len);
    }

    @Override
    public void put(long offset, long[] src, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Long.SIZE / 8).asLongBuffer().put(src, off, len);
    }

    @Override
    public void put(long offset, float[] src, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Float.SIZE / 8).asFloatBuffer().put(src, off, len);
    }

    @Override
    public void put(long offset, double[] src, int off, int len) {
        BufferUtil.slice(buffer, (int) offset, len * Double.SIZE / 8).asDoubleBuffer().put(src, off, len);
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
