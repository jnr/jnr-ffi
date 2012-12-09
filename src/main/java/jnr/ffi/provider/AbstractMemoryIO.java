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
import jnr.ffi.Runtime;
import jnr.ffi.Type;

import java.nio.ByteBuffer;

/**
 * Base implementations of some MemoryIO operations.
 */
abstract public class AbstractMemoryIO extends Pointer {

    protected static void checkBounds(long size, long off, long len) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected AbstractMemoryIO(Runtime runtime, long address, boolean isDirect) {
        super(runtime, address, isDirect);
    }

    public int indexOf(long offset, byte value) {
        return indexOf(offset, value, Integer.MAX_VALUE);
    }

    public long getAddress(long offset) {
        return getRuntime().addressSize() == 4 ? getInt(offset) : getLongLong(offset);
    }

    public void putAddress(long offset, long value) {
        if (getRuntime().addressSize() == 4) {
            putInt(offset, (int) value);
        } else {
            putLongLong(offset, value);
        }
    }

    public void checkBounds(long offset, long size) {
        // No bounds checking by default
    }

    public void putAddress(long offset, Address value) {
        if (getRuntime().addressSize() == 4) {
            putInt(offset, value.intValue());
        } else {
            putLongLong(offset, value.longValue());
        }
    }

    public final long getNativeLong(long offset) {
        return getRuntime().longSize() == 4 ? getInt(offset) : getLongLong(offset);
    }

    public void putNativeLong(long offset, long value) {
        if (getRuntime().longSize() == 4) {
            putInt(offset, (int) value);
        } else {
            putLongLong(offset, value);
        }
    }

    public long getLong(long offset) {
        return getRuntime().longSize() == 4 ? getInt(offset) : getLongLong(offset);
    }

    public void putLong(long offset, long value) {
        if (getRuntime().longSize() == 4) {
            putInt(offset, (int) value);
        } else {
            putLongLong(offset, value);
        }
    }

    @Override
    public void putInt(Type type, long offset, long value) {
        switch (type.getNativeType()) {
            case SCHAR:
            case UCHAR:
                putByte(offset, (byte) value);
                break;

            case SSHORT:
            case USHORT:
                putShort(offset, (short) value);
                break;

            case SINT:
            case UINT:
                putInt(offset, (int) value);
                break;

            case SLONG:
            case ULONG:
                putNativeLong(offset, value);
                break;

            case SLONGLONG:
            case ULONGLONG:
                putLongLong(offset, value);
                break;
            default:
                throw new IllegalArgumentException("unsupported integer type: " + type.getNativeType());
        }
    }

    @Override
    public long getInt(Type type, long offset) {
        switch (type.getNativeType()) {
            case SCHAR:
            case UCHAR:
                return getByte(offset);

            case SSHORT:
            case USHORT:
                return getShort(offset);

            case SINT:
            case UINT:
                return getInt(offset);

            case SLONG:
            case ULONG:
                return getNativeLong(offset);

            case SLONGLONG:
            case ULONGLONG:
                return getLongLong(offset);

            default:
                throw new IllegalArgumentException("unsupported integer type: " + type.getNativeType());
        }
    }

    public AbstractMemoryIO slice(long offset) {
        return new ShareMemoryIO(this, offset);
    }

    public AbstractMemoryIO slice(long offset, long size) {
        return new BoundedMemoryIO(this, offset, size);
    }


    public void transferTo(long offset, Pointer other, long otherOffset, long count) {
        Pointer dst = other instanceof DelegatingMemoryIO ? ((DelegatingMemoryIO) other).getDelegatedMemoryIO() : other;

        dst.checkBounds(otherOffset, count);

        if (dst instanceof AbstractArrayMemoryIO) {
            AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) dst;
            get(offset, aio.array(), aio.offset() + (int) otherOffset, (int) count);

        } else if (dst instanceof AbstractBufferMemoryIO && ((AbstractBufferMemoryIO) dst).getByteBuffer().hasArray()) {
            ByteBuffer buf = ((AbstractBufferMemoryIO) dst).getByteBuffer();
            get(offset, buf.array(), buf.arrayOffset() + buf.position() + (int) otherOffset, (int) count);

        } else {
            for (long i = 0; i < count; ++i) {
                other.putByte(otherOffset + i, getByte(offset + i));
            }
        }
    }

    public void transferFrom(long offset, Pointer other, long otherOffset, long count) {
        Pointer src = other instanceof DelegatingMemoryIO ? ((DelegatingMemoryIO) other).getDelegatedMemoryIO() : other;

        src.checkBounds(otherOffset, count);

        if (src instanceof AbstractArrayMemoryIO) {
            AbstractArrayMemoryIO aio = (AbstractArrayMemoryIO) src;
            put(offset, aio.array(), aio.offset() + (int) otherOffset, (int) count);

        } else if (src instanceof AbstractBufferMemoryIO && ((AbstractBufferMemoryIO) src).getByteBuffer().hasArray()) {
            ByteBuffer buf = ((AbstractBufferMemoryIO) src).getByteBuffer();
            put(offset, buf.array(), buf.arrayOffset() + buf.position() + (int) otherOffset, (int) count);
        
        } else {
            // Do a byte-at-a-time copy
            for (long i = 0; i < count; ++i) {
                putByte(offset + i, other.getByte(otherOffset + i));
            }
        }
    }
}
