
package com.kenai.jaffl.provider;

import com.kenai.jaffl.provider.jna.*;
import com.kenai.jaffl.*;

/**
 * Base implementations of some MemoryIO operations.
 */
abstract public class AbstractMemoryIO extends MemoryIO {
    protected static final void checkBounds(long size, long off, long len) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }
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

    public void transferTo(long offset, MemoryIO other, long otherOffset, long count) {
        for (long i = 0; i < count; ++i) {
            other.putByte(otherOffset + i, getByte(offset + i));
        }
    }

    public void transferFrom(long offset, MemoryIO other, long otherOffset, long count) {
        for (long i = 0; i < count; ++i) {
            putByte(offset + i, other.getByte(otherOffset + i));
        }
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
        public static class AddressIO64 implements AddressIO {
            public static final AddressIO _INSTANCE = new AddressIO64();
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
