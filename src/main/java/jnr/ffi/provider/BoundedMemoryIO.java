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

import jnr.ffi.Address;
import jnr.ffi.Pointer;

import java.nio.charset.Charset;


public final class BoundedMemoryIO extends AbstractMemoryIO implements DelegatingMemoryIO {

    private final long base,  size;
    private final Pointer io;

    public BoundedMemoryIO(Pointer parent, long offset, long size) {
        super(parent.getRuntime(), parent.address() != 0L ? parent.address() + offset : 0L, parent.isDirect());
        this.io = parent;
        this.base = offset;
        this.size = size;
    }

    public long size() {
        return this.size;
    }

    @Override
    public final boolean hasArray() {
        return io.hasArray();
    }

    @Override
    public final Object array() {
        return io.array();
    }

    @Override
    public final int arrayOffset() {
        return io.arrayOffset() + (int) base;
    }

    @Override
    public final int arrayLength() {
        return (int) size;
    }

    @Override
    public void checkBounds(long offset, long length) {
        checkBounds(this.size, offset, length);
        getDelegatedMemoryIO().checkBounds(base + offset, length);
    }

    public Pointer getDelegatedMemoryIO() {
        return io;
    }

    @Override
    public int hashCode() {
        return getDelegatedMemoryIO().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BoundedMemoryIO && io.equals(((BoundedMemoryIO) obj).io) &&
                ((BoundedMemoryIO) obj).base == base && ((BoundedMemoryIO) obj).size == size)
                || io.equals(obj);
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
    public long getLongLong(long offset) {
        checkBounds(size, offset, 8);
        return io.getLongLong(base + offset);
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
        checkBounds(size, offset, getRuntime().addressSize());
        return io.getPointer(base + offset);
    }
    
    public Pointer getPointer(long offset, long size) {
        checkBounds(this.size, base + offset, getRuntime().addressSize());
        return io.getPointer(base + offset, size);
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
    public void putLongLong(long offset, long value) {
        checkBounds(size, offset, 8);
        io.putLongLong(base + offset, value);
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
        checkBounds(size, offset, getRuntime().addressSize());
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
    public void put(long offset, int[] src, int off, int len) {
        checkBounds(size, offset, len * Integer.SIZE / 8);
        io.put(base + offset, src, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        checkBounds(size, offset, len * Long.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, long[] src, int off, int len) {
        checkBounds(size, offset, len * Long.SIZE / 8);
        io.put(base + offset, src, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        checkBounds(size, offset, len * Float.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, float[] src, int off, int len) {
        checkBounds(size, offset, len * Float.SIZE / 8);
        io.put(base + offset, src, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        checkBounds(size, offset, len * Double.SIZE / 8);
        io.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, double[] src, int off, int len) {
        checkBounds(size, offset, len * Double.SIZE / 8);
        io.put(base + offset, src, off, len);
    }

    @Override
    public long getAddress(long offset) {
        checkBounds(size, offset, getRuntime().addressSize());
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
        checkBounds(size, offset, getRuntime().addressSize());
        io.putAddress(base + offset, value);
    }

    @Override
    public void putAddress(long offset, Address value) {
        checkBounds(size, offset, getRuntime().addressSize());
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

    @Override
    public void transferFrom(long offset, Pointer other, long otherOffset, long count) {
        checkBounds(this.size, base + offset, count);
        getDelegatedMemoryIO().transferFrom(offset, other, otherOffset, count);
    }

    @Override
    public void transferTo(long offset, Pointer other, long otherOffset, long count) {
        checkBounds(this.size, base + offset, count);
        getDelegatedMemoryIO().transferTo(offset, other, otherOffset, count);
    }
}
