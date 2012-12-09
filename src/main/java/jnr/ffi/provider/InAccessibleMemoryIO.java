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
import jnr.ffi.Runtime;

import java.nio.charset.Charset;

abstract public class InAccessibleMemoryIO extends AbstractMemoryIO {
    private static final String msg = "attempted access to inaccessible memory";

    protected InAccessibleMemoryIO(Runtime runtime, long address, boolean isDirect) {
        super(runtime, address, isDirect);
    }

    protected RuntimeException error() {
        return new IndexOutOfBoundsException(msg);
    }


    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public Object array() {
        return null;
    }

    @Override
    public int arrayOffset() {
        return 0;
    }

    @Override
    public int arrayLength() {
        return 0;
    }

    public final byte getByte(long offset) {
        throw error();
    }

    public final short getShort(long offset) {
        throw error();
    }

    public final int getInt(long offset) {
        throw error();
    }

    public final long getLong(long offset) {
        throw error();
    }
    
    public final long getLongLong(long offset) {
        throw error();
    }

    public final float getFloat(long offset) {
        throw error();
    }

    public final double getDouble(long offset) {
        throw error();
    }

    public final void putByte(long offset, byte value) {
        throw error();
    }

    public final void putShort(long offset, short value) {
        throw error();
    }

    public final void putInt(long offset, int value) {
        throw error();
    }

    public final void putLong(long offset, long value) {
        throw error();
    }
    
    public final void putLongLong(long offset, long value) {
        throw error();
    }

    public final void putFloat(long offset, float value) {
        throw error();
    }

    public final void putDouble(long offset, double value) {
        throw error();
    }

    public final void get(long offset, byte[] dst, int off, int len) {
        throw error();
    }

    public final void put(long offset, byte[] dst, int off, int len) {
        throw error();
    }

    public final void get(long offset, short[] dst, int off, int len) {
        throw error();
    }

    public final void put(long offset, short[] dst, int off, int len) {
        throw error();
    }

    public final void get(long offset, int[] dst, int off, int len) {
        throw error();
    }

    public final void put(long offset, int[] src, int off, int len) {
        throw error();
    }

    public final void get(long offset, long[] dst, int off, int len) {
        throw error();
    }

    public final void put(long offset, long[] src, int off, int len) {
        throw error();
    }

    public final void get(long offset, float[] dst, int off, int len) {
        throw error();
    }

    public final void put(long offset, float[] src, int off, int len) {
        throw error();
    }

    public final void get(long offset, double[] dst, int off, int len) {
        throw error();
    }

    public final void put(long offset, double[] src, int off, int len) {
        throw error();
    }

    public final Pointer getPointer(long offset, long size) {
        throw error();
    }

    public final Pointer getPointer(long offset) {
        throw error();
    }

    public final void putPointer(long offset, Pointer value) {
        throw error();
    }

    public String getString(long offset) {
        throw error();
    }

    public String getString(long offset, int maxLength, Charset cs) {
        throw error();
    }

    public void putString(long offset, String string, int maxLength, Charset cs) {
        throw error();
    }

    public final int indexOf(long offset, byte value, int maxlen) {
        throw error();
    }

    public final void setMemory(long offset, long size, byte value) {
        throw error();
    }
}
