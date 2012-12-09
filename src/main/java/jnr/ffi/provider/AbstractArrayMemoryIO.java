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
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class AbstractArrayMemoryIO extends AbstractMemoryIO {
    private final ArrayIO io;
    protected final byte[] buffer;
    protected final int offset, length;

    protected AbstractArrayMemoryIO(Runtime runtime, byte[] buffer, int offset, int length) {
        super(runtime, 0, false);
        this.io = ArrayIO.getArrayIO(runtime);
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    protected AbstractArrayMemoryIO(Runtime runtime, byte[] buffer) {
        this(runtime, buffer, 0, buffer.length);
    }

    protected AbstractArrayMemoryIO(Runtime runtime, int size) {
        this(runtime, new byte[size], 0, size);
    }

    protected final ArrayIO getArrayIO() {
        return io;
    }
    
    public final byte[] array() {
        return buffer;
    }
    public final int offset() {
        return offset;
    }
    public final int length() {
        return length;
    }

    @Override
    public final int arrayLength() {
        return length;
    }

    @Override
    public final int arrayOffset() {
        return offset;
    }

    @Override
    public final boolean hasArray() {
        return true;
    }

    public final long size() {
        return this.length;
    }
    
    protected final int index(long off) {
        return this.offset + (int) off;
    }

    protected final int remaining(long offset) {
        return length - (int) offset;
    }

    public final boolean isNull() {
        return false;
    }

    public String getString(long offset) {
        return BufferUtil.getString(ByteBuffer.wrap(buffer, index(offset), length - (int) offset), Charset.defaultCharset());
    }

    public String getString(long offset, int maxLength, Charset cs) {
        return BufferUtil.getString(ByteBuffer.wrap(buffer, index(offset), Math.min(length - (int) offset, maxLength)), cs);
    }
    
    public void putString(long offset, String string, int maxLength, Charset cs) {
        ByteBuffer buf = cs.encode(string);
        int len = Math.min(maxLength - 1, Math.min(buf.remaining(), remaining(offset)));
        buf.get(buffer, index(offset), len);
        buffer[index(offset) + len] = (byte) 0;
    }

    public void putZeroTerminatedByteArray(long offset, byte[] src, int off, int len) {
        System.arraycopy(src, off, buffer, index(offset), length - (int) offset);
        buffer[index(offset) + len] = (byte) 0;
    }

    public final byte getByte(long offset) {
        return (byte) (buffer[index(offset)] & 0xff);
    }

    public final short getShort(long offset) {
        return io.getInt16(buffer, index(offset));
    }

    public final int getInt(long offset) {
        return io.getInt32(buffer, index(offset));
    }

    public final long getLongLong(long offset) {
        return io.getInt64(buffer, index(offset));
    }

    @Override
    public final long getAddress(long offset) {
        return io.getAddress(buffer, index(offset));
    }

    public final float getFloat(long offset) {
        return io.getFloat32(buffer, index(offset));
    }

    public final double getDouble(long offset) {
        return io.getFloat64(buffer, index(offset));
    }
    
    public final void putByte(long offset, byte value) {
        buffer[index(offset)] = value;
    }

    public final void putShort(long offset, short value) {
        io.putInt16(buffer, index(offset), value);
    }

    public final void putInt(long offset, int value) {
        io.putInt32(buffer, index(offset), value);
    }
    
    public final void putLongLong(long offset, long value) {
        io.putInt64(buffer, index(offset), value);
    }

    @Override
    public final void putAddress(long offset, long value) {
        io.putAddress(buffer, index(offset), value);
    }

    public final void putFloat(long offset, float value) {
        io.putFloat32(buffer, index(offset), value);
    }

    public final void putDouble(long offset, double value) {
        io.putFloat64(buffer, index(offset), value);
    }

    public final void get(long offset, byte[] dst, int off, int len) {
        System.arraycopy(buffer, index(offset), dst, off, len);
    }

    public final void put(long offset, byte[] src, int off, int len) {
        System.arraycopy(src, off, buffer, index(offset), len);
    }

    public final void get(long offset, short[] dst, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            dst[off + i] = io.getInt16(buffer, begin + (i << 1));
        }
    }

    public final void put(long offset, short[] src, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            io.putInt16(buffer, begin + (i << 1), src[off + i]);
        }
    }

    public final void get(long offset, int[] dst, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            dst[off + i] = io.getInt32(buffer, begin + (i << 2));
        }
    }

    public final void put(long offset, int[] src, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            io.putInt32(buffer, begin + (i << 2), src[off + i]);
        }
    }

    public final void get(long offset, long[] dst, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            dst[off + i] = io.getInt64(buffer, begin + (i << 3));
        }
    }

    public final void put(long offset, long[] src, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            io.putInt64(buffer, begin + (i << 3), src[off + i]);
        }
    }

    public final void get(long offset, float[] dst, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            dst[off + i] = io.getFloat32(buffer, begin + (i << 2));
        }
    }

    public final void put(long offset, float[] src, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            io.putFloat32(buffer, begin + (i << 2), src[off + i]);
        }
    }

    public final void get(long offset, double[] dst, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            dst[off + i] = io.getFloat64(buffer, begin + (i << 3));
        }
    }

    public final void put(long offset, double[] src, int off, int len) {
        int begin = index(offset);
        for (int i = 0; i < len; ++i) {
            io.putFloat64(buffer, begin + (i << 3), src[off + i]);
        }
    }

    @Override
    public final int indexOf(long offset, byte value) {
        int off = index(offset);
        for (int i = 0; i < length; ++i) {
            if (buffer[off + i] == value) {
                return i;
            }
        }
        return -1;
    }

    public final int indexOf(long offset, byte value, int maxlen) {
        int off = index(offset);
        for (int i = 0; i < Math.min(length, maxlen); ++i) {
            if (buffer[off + i] == value) {
                return i;
            }
        }
        return -1;
    }

    public final void setMemory(long offset, long size, byte value) {
        Arrays.fill(buffer, index(offset), (int) size, value);
    }

    public final void clear() {
        Arrays.fill(buffer, offset, length, (byte) 0);
    }

    protected static abstract class ArrayIO {

        public static ArrayIO getArrayIO(Runtime runtime) {
            if (runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)) {
                return runtime.addressSize() == 8
                        ? BE64ArrayIO.INSTANCE : BE32ArrayIO.INSTANCE;
            } else {
                return runtime.addressSize() == 8
                        ? LE64ArrayIO.INSTANCE : LE32ArrayIO.INSTANCE;
            }
        }

        public abstract short getInt16(byte[] buffer, int offset);
        public abstract int getInt32(byte[] buffer, int offset);
        public abstract long getInt64(byte[] buffer, int offset);
        public abstract long getAddress(byte[] buffer, int offset);
        
        public abstract void putInt16(byte[] buffer, int offset, int value);
        public abstract void putInt32(byte[] buffer, int offset, int value);
        public abstract void putInt64(byte[] buffer, int offset, long value);
        public abstract void putAddress(byte[] buffer, int offset, long value);



        public final float getFloat32(byte[] buffer, int offset) {
            return Float.intBitsToFloat(getInt32(buffer, offset));
        }
        public final void putFloat32(byte[] buffer, int offset, float value) {
            putInt32(buffer, offset, Float.floatToRawIntBits(value));
        }
        public final double getFloat64(byte[] buffer, int offset) {
            return Double.longBitsToDouble(getInt64(buffer, offset));
        }
        public final void putFloat64(byte[] buffer, int offset, double value) {
            putInt64(buffer, offset, Double.doubleToRawLongBits(value));
        }
    }

    private static abstract class LittleEndianArrayIO extends ArrayIO {
        public final short getInt16(byte[] array, int offset) {
            return (short) ((array[offset] & 0xff) | ((array[offset + 1] & 0xff) << 8));
        }
        public final int getInt32(byte[] array, int offset) {
            return    ((array[offset + 0] & 0xff) << 0)
                    | ((array[offset + 1] & 0xff) << 8)
                    | ((array[offset + 2] & 0xff) << 16)
                    | ((array[offset + 3] & 0xff) << 24);
        }
        public final long getInt64(byte[] array, int offset) {
            return    (((long)array[offset + 0] & 0xff) << 0)
                    | (((long)array[offset + 1] & 0xff) << 8)
                    | (((long)array[offset + 2] & 0xff) << 16)
                    | (((long)array[offset + 3] & 0xff) << 24)
                    | (((long)array[offset + 4] & 0xff) << 32)
                    | (((long)array[offset + 5] & 0xff) << 40)
                    | (((long)array[offset + 6] & 0xff) << 48)
                    | (((long)array[offset + 7] & 0xff) << 56);
        }
        public final void putInt16(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 0);
            buffer[offset + 1] = (byte) (value >> 8);
        }
        public final void putInt32(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 0);
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
        }
        public final void putInt64(byte[] buffer, int offset, long value) {
            buffer[offset + 0] = (byte) (value >> 0);
            buffer[offset + 1] = (byte) (value >> 8);
            buffer[offset + 2] = (byte) (value >> 16);
            buffer[offset + 3] = (byte) (value >> 24);
            buffer[offset + 4] = (byte) (value >> 32);
            buffer[offset + 5] = (byte) (value >> 40);
            buffer[offset + 6] = (byte) (value >> 48);
            buffer[offset + 7] = (byte) (value >> 56);
        }
    }

    private static abstract class BigEndianArrayIO extends ArrayIO {
        public short getInt16(byte[] array, int offset) {
            return (short) (((array[offset + 0] & 0xff) << 8)
                    | (array[offset + 1] & 0xff));
        }
        public int getInt32(byte[] array, int offset) {
            return    ((array[offset + 0] & 0xff) << 24)
                    | ((array[offset + 1] & 0xff) << 16)
                    | ((array[offset + 2] & 0xff) << 8)
                    | ((array[offset + 3] & 0xff) << 0);
        }
        public long getInt64(byte[] array, int offset) {
            return    (((long)array[offset + 0] & 0xff) << 56)
                    | (((long)array[offset + 1] & 0xff) << 48)
                    | (((long)array[offset + 2] & 0xff) << 40)
                    | (((long)array[offset + 3] & 0xff) << 32)
                    | (((long)array[offset + 4] & 0xff) << 24)
                    | (((long)array[offset + 5] & 0xff) << 16)
                    | (((long)array[offset + 6] & 0xff) << 8)
                    | (((long)array[offset + 7] & 0xff) << 0);
        }
        public final void putInt16(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 8);
            buffer[offset + 1] = (byte) (value >> 0);
        }
        public final void putInt32(byte[] buffer, int offset, int value) {
            buffer[offset + 0] = (byte) (value >> 24);
            buffer[offset + 1] = (byte) (value >> 16);
            buffer[offset + 2] = (byte) (value >> 8);
            buffer[offset + 3] = (byte) (value >> 0);
        }
        public final void putInt64(byte[] buffer, int offset, long value) {
            buffer[offset + 0] = (byte) (value >> 56);
            buffer[offset + 1] = (byte) (value >> 48);
            buffer[offset + 2] = (byte) (value >> 40);
            buffer[offset + 3] = (byte) (value >> 32);
            buffer[offset + 4] = (byte) (value >> 24);
            buffer[offset + 5] = (byte) (value >> 16);
            buffer[offset + 6] = (byte) (value >> 8);
            buffer[offset + 7] = (byte) (value >> 0);
        }
    }

    private static final class LE32ArrayIO extends LittleEndianArrayIO {
        public static final ArrayIO INSTANCE = new LE32ArrayIO();

        public final long getAddress(byte[] buffer, int offset) {
            return (long) getInt32(buffer, offset) & 0xffffffffL;
        }
        public final void putAddress(byte[] buffer, int offset, long value) {
            putInt32(buffer, offset, (int) value);
        }
    }

    private static final class LE64ArrayIO extends LittleEndianArrayIO {
        public static final ArrayIO INSTANCE = new LE64ArrayIO();

        public final long getAddress(byte[] buffer, int offset) {
            return getInt64(buffer, offset);
        }
        public final void putAddress(byte[] buffer, int offset, long value) {
            putInt64(buffer, offset, value);
        }
    }
    
    private static final class BE32ArrayIO extends BigEndianArrayIO {
        public static final ArrayIO INSTANCE = new BE32ArrayIO();

        public final long getAddress(byte[] buffer, int offset) {
            return (long) getInt32(buffer, offset) & 0xffffffffL;
        }
        public final void putAddress(byte[] buffer, int offset, long value) {
            putInt32(buffer, offset, (int) value);
        }
    }
    
    private static final class BE64ArrayIO extends BigEndianArrayIO {
        public static final ArrayIO INSTANCE = new BE64ArrayIO();

        public final long getAddress(byte[] buffer, int offset) {
            return getInt64(buffer, offset);
        }
        public final void putAddress(byte[] buffer, int offset, long value) {
            putInt64(buffer, offset, value);
        }
    }
}
