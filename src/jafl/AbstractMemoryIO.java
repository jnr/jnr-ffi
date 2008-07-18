/*
 * Copyright (C) 2007, 2008 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package jafl;

import java.nio.charset.Charset;

/**
 * Base implementations of some MemoryIO operations.
 */
abstract public class AbstractMemoryIO implements MemoryIO {
    public int indexOf(long offset, byte value) {
        return indexOf(offset, value, Integer.MAX_VALUE);
    }
    public long getAddress(long offset) {
        return AddressIO.INSTANCE.getAddress(this, offset);
    }
    public void putAddress(long offset, long value) {
        AddressIO.INSTANCE.putAddress(this, offset, value);
    }
    public void putAddress(long offset, Address value) {
        AddressIO.INSTANCE.putAddress(this, offset, value.longValue());
    }
    public final long getNativeLong(long offset) {
        return NativeLongIO.INSTANCE.getLong(this, offset);
    }
    public MemoryIO slice(long offset) {
        return MemoryUtil.slice(this, offset);
    }
    public MemoryIO slice(long offset, long size) {
        return MemoryUtil.slice(this, offset, size);
    }

    public void putNativeLong(long offset, long value) {
        NativeLongIO.INSTANCE.putLong(this, offset, value);
    }
    public String getString(long offset, int maxLength, Charset cs) {
        byte[] tmp = new byte[maxLength];
        get(offset, tmp, 0, tmp.length);
        return new java.lang.String(tmp);
    }
    public void putString(long offset, String string, int maxLength, Charset cs) {
        byte[] tmp = string.getBytes();
        put(offset, tmp, 0, Math.min(tmp.length, maxLength));
        putByte(offset + maxLength - 1, (byte) 0);
    }
    //
    // Optimize reading/writing pointers.
    //
    private static interface AddressIO {
        public long getAddress(MemoryIO io, long offset);
        public void putAddress(MemoryIO io, long offset, long address);
        public static class AddressIO32 implements AddressIO {
            public static final AddressIO _INSTANCE = new AddressIO32();
            public long getAddress(MemoryIO io, long offset) {
                return io.getInt(offset);
            }
            public void putAddress(MemoryIO io, long offset, long address) {
                io.putInt(offset, (int) address);
            }
        }
        public static class AddressIO64 {
            public static final AddressIO _INSTANCE = new AddressIO32();
            public long getAddress(MemoryIO io, long offset) {
                return io.getLong(offset);
            }
            public void putAddress(MemoryIO io, long offset, long address) {
                io.putLong(offset, address);
            }
        }
        public static final AddressIO INSTANCE = Platform.getPlatform().addressSize() == 32
                ? AddressIO32._INSTANCE : AddressIO64._INSTANCE;
    }
    //
    // Optimize reading/writing native long values.
    //
    private static interface NativeLongIO {
        public long getLong(MemoryIO io, long offset);
        public void putLong(MemoryIO io, long offset, long value);
        public static class LongIO32 implements NativeLongIO {
            public static final NativeLongIO _INSTANCE = new LongIO32();
            public long getLong(MemoryIO io, long offset) {
                return io.getInt(offset);
            }
            public void putLong(MemoryIO io, long offset, long value) {
                io.putInt(offset, (int) value);
            }
        }
        public static class LongIO64 implements NativeLongIO {
            public static final NativeLongIO _INSTANCE = new LongIO64();
            public long getLong(MemoryIO io, long offset) {
                return io.getLong(offset);
            }
            public void putLong(MemoryIO io, long offset, long value) {
                io.putLong(offset, value);
            }
        }
        public static final NativeLongIO INSTANCE = Platform.getPlatform().longSize() == 32
                ? LongIO32._INSTANCE : LongIO64._INSTANCE;
    }
}
