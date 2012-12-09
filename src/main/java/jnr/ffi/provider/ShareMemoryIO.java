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

import jnr.ffi.Pointer;

import java.nio.charset.Charset;

/**
 *
 */
public class ShareMemoryIO extends AbstractMemoryIO implements DelegatingMemoryIO {

    private final Pointer ptr;
    private final long base;

    public ShareMemoryIO(Pointer parent, long offset) {
        super(parent.getRuntime(), parent.address() != 0L ? parent.address() + offset : 0L, parent.isDirect());
        this.ptr = parent;
        this.base = offset;
    }

    public long size() {
        return ptr.size() - base;
    }

    @Override
    public final boolean hasArray() {
        return ptr.hasArray();
    }

    @Override
    public final Object array() {
        return ptr.array();
    }

    @Override
    public final int arrayOffset() {
        return ptr.arrayOffset() + (int) base;
    }

    @Override
    public final int arrayLength() {
        return ptr.arrayLength() - (int) base;
    }

    public final Pointer getDelegatedMemoryIO() {
        return ptr;
    }

    @Override
    public byte getByte(long offset) {
        return ptr.getByte(base + offset);
    }

    @Override
    public short getShort(long offset) {
        return ptr.getShort(base + offset);
    }

    @Override
    public int getInt(long offset) {
        return ptr.getInt(base + offset);
    }

    @Override
    public long getLong(long offset) {
        return ptr.getLong(base + offset);
    }
    
    @Override
    public long getLongLong(long offset) {
        return ptr.getLongLong(base + offset);
    }

    @Override
    public float getFloat(long offset) {
        return ptr.getFloat(base + offset);
    }

    @Override
    public double getDouble(long offset) {
        return ptr.getDouble(base + offset);
    }

    public Pointer getPointer(long offset) {
        return ptr.getPointer(base + offset);
    }

    public Pointer getPointer(long offset, long size) {
        return ptr.getPointer(base + offset, size);
    }

    @Override
    public String getString(long offset) {
        return ptr.getString(base + offset);
    }


    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        return ptr.getString(base + offset, maxLength, cs);
    }


    @Override
    public void putByte(long offset, byte value) {
        ptr.putByte(base + offset, value);
    }

    @Override
    public void putShort(long offset, short value) {
        ptr.putShort(base + offset, value);
    }

    @Override
    public void putInt(long offset, int value) {
        ptr.putInt(base + offset, value);
    }

    @Override
    public void putLong(long offset, long value) {
        ptr.putLong(base + offset, value);
    }
    
    @Override
    public void putLongLong(long offset, long value) {
        ptr.putLongLong(base + offset, value);
    }

    @Override
    public void putFloat(long offset, float value) {
        ptr.putFloat(base + offset, value);
    }

    @Override
    public void putDouble(long offset, double value) {
        ptr.putDouble(base + offset, value);
    }

    public void putPointer(long offset, Pointer value) {
        ptr.putPointer(base + offset, value);
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        ptr.putString(base + offset, string, maxLength, cs);
    }

    @Override
    public void get(long offset, byte[] dst, int off, int len) {
        ptr.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, byte[] dst, int off, int len) {
        ptr.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, short[] dst, int off, int len) {
        ptr.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, short[] dst, int off, int len) {
        ptr.put(base + offset, dst, off, len);
    }

    @Override
    public void get(long offset, int[] dst, int off, int len) {
        ptr.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, int[] src, int off, int len) {
        ptr.put(base + offset, src, off, len);
    }

    @Override
    public void get(long offset, long[] dst, int off, int len) {
        ptr.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, long[] src, int off, int len) {
        ptr.put(base + offset, src, off, len);
    }

    @Override
    public void get(long offset, float[] dst, int off, int len) {
        ptr.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, float[] src, int off, int len) {
        ptr.put(base + offset, src, off, len);
    }

    @Override
    public void get(long offset, double[] dst, int off, int len) {
        ptr.get(base + offset, dst, off, len);
    }

    @Override
    public void put(long offset, double[] src, int off, int len) {
        ptr.put(base + offset, src, off, len);
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        return ptr.indexOf(base + offset, value, maxlen);
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        ptr.setMemory(base + offset, size, value);
    }
}
