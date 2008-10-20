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

package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Address;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * JFFI implementation of native memory operations.
 */
public final class JFFIMemoryIO implements com.kenai.jaffl.MemoryIO {
    /**
     * The native address of the memory (if not a ByteBuffer).
     */
    final com.googlecode.jffi.Address ptr;

    /**
     * The JFFI memory I/O access operations.
     */
    final com.googlecode.jffi.lowlevel.MemoryIO io;

    /**
     * The underlying memory object, either a ByteBuffer or a native pointer.
     */
    final Object memory;

    /**
     * Convenience global accessor for NULL/invalid pointers.
     */
    static final JFFIMemoryIO NULL = wrap(com.googlecode.jffi.Pointer.NULL);

    /**
     * Creates a default Memory I/O accessor that throws NullPointerException
     */
    private JFFIMemoryIO() {
        this.memory = this.ptr = com.googlecode.jffi.Pointer.NULL;
        this.io = com.googlecode.jffi.lowlevel.MemoryIO.wrap(ptr);
    }

    /**
     * Allocates a new java heap memory object.
     *
     * @param size The amount of memory to allocate, in bytes.
     * @return A new {@link MemoryIO} that can access the memory.
     */
    static JFFIMemoryIO allocate(int size) {
        ByteBuffer buf = ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
        return new JFFIMemoryIO(buf, com.googlecode.jffi.lowlevel.MemoryIO.wrap(buf));
    }

    /**
     * Allocates a new native memory object.
     *
     * @param size The amount of memory to allocate, in bytes.
     * @return A new {@link MemoryIO} that can access the memory.
     */
    static JFFIMemoryIO allocateDirect(long size) {
        com.googlecode.jffi.Pointer ptr = new com.googlecode.jffi.Memory(size);
        return new JFFIMemoryIO(ptr, com.googlecode.jffi.lowlevel.MemoryIO.wrap(ptr, size));
    }

    /**
     * Wraps an existing native memory pointer in an abstract {@link MemoryIO} accessor.
     * <p>
     * Memory accesses to the native memory region are not range checked.
     * </p>
     *
     * @param ptr The native memory to wrap.
     * @return A new {@link MemoryIO} accessor.
     */
    static JFFIMemoryIO wrap(com.googlecode.jffi.Address ptr) {
        return new JFFIMemoryIO(ptr, com.googlecode.jffi.lowlevel.MemoryIO.wrap(ptr));
    }

    /**
     * Wraps an existing native memory pointer in an abstract {@link MemoryIO} accessor.
     * <p>
     * All accesses to the native memory region are range checked.
     * </p>
     *
     * @param ptr The native memory to wrap.
     * @return A new {@link MemoryIO} accessor.
     */
    static JFFIMemoryIO wrap(com.googlecode.jffi.Address ptr, long size) {
        return new JFFIMemoryIO(ptr, com.googlecode.jffi.lowlevel.MemoryIO.wrap(ptr, size));
    }

    /**
     * Wraps an existing java <tt>ByteBuffer</tt>in an abstract {@link MemoryIO} accessor.
     *
     * @param buf The ByteBuffer to wrap.
     * @return A new {@link MemoryIO} accessor.
     */
    /*
    static JFFIMemoryIO wrap(ByteBuffer buf) {
        return new JFFIMemoryIO(buf, com.googlecode.jffi.lowlevel.MemoryIO.wrap(buf));
    }
*/
    JFFIMemoryIO(com.googlecode.jffi.Address ptr, com.googlecode.jffi.lowlevel.MemoryIO io) {
        this.memory = this.ptr = ptr;
        this.io = io;
    }
    JFFIMemoryIO(ByteBuffer buf, com.googlecode.jffi.lowlevel.MemoryIO io) {
        this.memory = buf;
        this.ptr = com.googlecode.jffi.Pointer.NULL;
        this.io = io;
    }

    com.googlecode.jffi.Address getAddress() {
        return ptr;
    }
    public boolean isNull() {
        return !(memory instanceof ByteBuffer) && com.googlecode.jffi.Pointer.NULL.equals(ptr);
    }
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JFFIMemoryIO) && ((JFFIMemoryIO) obj).memory.equals(memory);
    }

    @Override
    public int hashCode() {
        return memory.hashCode();
    }
    private MemoryIO slice(com.googlecode.jffi.lowlevel.MemoryIO newio) {
        if (memory instanceof ByteBuffer) {
            return new JFFIMemoryIO((ByteBuffer) memory, newio);
        } else if (memory instanceof com.googlecode.jffi.Address) {
            return new JFFIMemoryIO((com.googlecode.jffi.Address) memory, newio);
        } else {
            throw new UnsupportedOperationException("Invalid memory state");
        }
    }
    public MemoryIO slice(long offset) {
        return slice(io.slice(offset));
    }

    public MemoryIO slice(long offset, long size) {
        return slice(io.slice(offset, size));
    }

    public byte getByte(long offset) {
        return io.getByte(offset);
    }

    public short getShort(long offset) {
        return io.getShort(offset);
    }

    public int getInt(long offset) {
        return io.getInt(offset);
    }

    public long getLong(long offset) {
        return io.getLong(offset);
    }

    public long getNativeLong(long offset) {
        return io.getNativeLong(offset);
    }

    public long getAddress(long offset) {
        return io.getAddress(offset);
    }

    public String getString(long offset, int maxLength, Charset cs) {
        return io.getString(offset, maxLength, cs);
    }

    public JFFIMemoryIO getMemoryIO(long offset) {
        long address = getAddress(offset);
        return new JFFIMemoryIO(new com.googlecode.jffi.Address(address),
                com.googlecode.jffi.lowlevel.MemoryIO.wrap(address));
    }

    public MemoryIO getMemoryIO(long offset, long size) {
        long address = io.getAddress(offset);
        com.googlecode.jffi.lowlevel.MemoryIO memio
                = com.googlecode.jffi.lowlevel.MemoryIO.wrap(address, size);
        return new JFFIMemoryIO(new com.googlecode.jffi.Address(address), memio);
    }

    public Pointer getPointer(long offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getFloat(long offset) {
        return io.getFloat(offset);
    }

    public double getDouble(long offset) {
        return io.getDouble(offset);
    }

    public void putByte(long offset, byte value) {
        io.putByte(offset, value);
    }

    public void putShort(long offset, short value) {
        io.putShort(offset, value);
    }

    public void putInt(long offset, int value) {
        io.putInt(offset, value);
    }

    public void putLong(long offset, long value) {
        io.putLong(offset, value);
    }

    public void putNativeLong(long offset, long value) {
        io.putNativeLong(offset, value);
    }

    public void putAddress(long offset, long value) {
        io.putAddress(offset, value);
    }

    public void putAddress(long offset, Address value) {
        io.putAddress(offset, value.nativeAddress());
    }

    public void putFloat(long offset, float value) {
        io.putFloat(offset, value);
    }

    public void putDouble(long offset, double value) {
        io.putDouble(offset, value);
    }

    public void putString(long offset, String string, int maxLength, Charset cs) {
        io.putString(offset, string, maxLength, cs);
    }

    public void putMemoryIO(long offset, MemoryIO value) {
        io.putAddress(offset, ((JFFIMemoryIO) value).getAddress());
    }

    public void putPointer(long offset, Pointer value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void get(long offset, byte[] dst, int off, int len) {
        io.get(offset, dst, off, len);
    }

    public void put(long offset, byte[] dst, int off, int len) {
        io.put(offset, dst, off, len);
    }

    public void get(long offset, short[] dst, int off, int len) {
        io.get(offset, dst, off, len);
    }

    public void put(long offset, short[] dst, int off, int len) {
        io.put(offset, dst, off, len);
    }

    public void get(long offset, int[] dst, int off, int len) {
        io.get(offset, dst, off, len);
    }

    public void put(long offset, int[] dst, int off, int len) {
        io.put(offset, dst, off, len);
    }

    public void get(long offset, long[] dst, int off, int len) {
        io.get(offset, dst, off, len);
    }

    public void put(long offset, long[] dst, int off, int len) {
        io.put(offset, dst, off, len);
    }

    public void get(long offset, float[] dst, int off, int len) {
        io.get(offset, dst, off, len);
    }

    public void put(long offset, float[] dst, int off, int len) {
        io.put(offset, dst, off, len);
    }

    public void get(long offset, double[] dst, int off, int len) {
        io.get(offset, dst, off, len);
    }

    public void put(long offset, double[] dst, int off, int len) {
        io.put(offset, dst, off, len);
    }

    public int indexOf(long offset, byte value) {
        return io.indexOf(offset, value);
    }

    public int indexOf(long offset, byte value, int maxlen) {
        return io.indexOf(offset, value, maxlen);
    }

    public boolean isDirect() {
        return io.isDirect();
    }
    private void slowTransfer(long offset, MemoryIO other, long otherOffset, long count) {
        for (long i = 0; i < count; ++i) {
            other.putByte(otherOffset + count, getByte(offset + count));
        }
    }
    public void transferTo(long offset, MemoryIO other, long otherOffset, long count) {
        if (other instanceof JFFIMemoryIO) {
            io.transferTo(offset, ((JFFIMemoryIO) other).io, otherOffset, count);
        } else {
            slowTransfer(offset, other, otherOffset, count);
        }
    }
/*
    public void marshal(Marshaller marshaller, MarshalContext context, long offset) {
        if (memory instanceof ByteBuffer) {
            if (offset == 0) {
                marshaller.add((ByteBuffer) memory, context);
            } else {
                marshaller.add(Util.slice(((ByteBuffer) memory), (int) offset), context);
            }
        } else {
            marshaller.addAddress(ptr.nativeAddress() + offset);
        }
    }
 */
}
