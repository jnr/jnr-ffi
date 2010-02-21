
package com.kenai.jaffl.provider;

import com.kenai.jaffl.*;
import com.kenai.jaffl.Runtime;

/**
 * Base implementations of some MemoryIO operations.
 */
abstract public class AbstractMemoryIO extends MemoryIO {
    protected static final void checkBounds(long size, long off, long len) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected AbstractMemoryIO(Runtime runtime) {
        super(runtime);
    }

    public int indexOf(long offset, byte value) {
        return indexOf(offset, value, Integer.MAX_VALUE);
    }

    public long getAddress(long offset) {
        return getRuntime().addressSize() == 4 ? getInt(offset) : getLong(offset);
    }

    public void putAddress(long offset, long value) {
        if (getRuntime().addressSize() == 4) {
            putInt(offset, (int) value);
        } else {
            putLong(offset, value);
        }
    }

    public void putAddress(long offset, Address value) {
        if (getRuntime().addressSize() == 4) {
            putInt(offset, value.intValue());
        } else {
            putLong(offset, value.longValue());
        }
    }

    public final long getNativeLong(long offset) {
        return getRuntime().longSize() == 4 ? getInt(offset) : getLong(offset);
    }

    public MemoryIO slice(long offset) {
        return new ShareMemoryIO(this, offset);
    }

    public MemoryIO slice(long offset, long size) {
        return new BoundedMemoryIO(this, offset, size);
    }

    public void putNativeLong(long offset, long value) {
        if (getRuntime().longSize() == 4) {
            putInt(offset, (int) value);
        } else {
            putLong(offset, value);
        }
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
}
