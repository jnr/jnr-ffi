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

import jnr.ffi.*;
import jnr.ffi.Runtime;
import java.nio.ByteBuffer;

/**
 * Base implementations of some MemoryIO operations.
 */
abstract public class AbstractMemoryIO extends Pointer {
    private final Runtime runtime;
    
    protected static final void checkBounds(long size, long off, long len) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    protected AbstractMemoryIO(Runtime runtime) {
        this.runtime = runtime;
    }

    public final Runtime getRuntime() {
        return runtime;
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

    public void checkBounds(long offset, long size) {
        // No bounds checking by default
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

    public AbstractMemoryIO slice(long offset) {
        return new ShareMemoryIO(this, offset);
    }

    public AbstractMemoryIO slice(long offset, long size) {
        return new BoundedMemoryIO(this, offset, size);
    }

    public void putNativeLong(long offset, long value) {
        if (getRuntime().longSize() == 4) {
            putInt(offset, (int) value);
        } else {
            putLong(offset, value);
        }
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
