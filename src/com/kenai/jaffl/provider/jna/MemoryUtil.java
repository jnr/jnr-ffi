/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.*;
import com.kenai.jaffl.util.BufferUtil;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author wayne
 */
public final class MemoryUtil {
    public static final MemoryIO slice(MemoryIO io, long offset) {
        return new ShareIO(io, offset);
    }
    public static final MemoryIO slice(MemoryIO io, long offset, long size) {
        return new BoundedIO(io, offset, size);
    }
    public static final MemoryIO wrap(ByteBuffer buffer) {
        return new BufferIO(buffer);
    }
    public static final MemoryIO wrap(byte[] array, int offset, int size) {
        return new BufferIO(ByteBuffer.wrap(array, offset, size));
    }
    /*
    public static final MemoryIO wrap(Address address, long size) {
        return wrap(address.nativeAddress(), size);
    }
    public static final MemoryIO wrap(long address, long size) {
        return address == 0 ? getNullIO() : new BoundedNativeIO(address, size);
    }
    public static final MemoryIO wrap(Address address) {
        return wrap(address.nativeAddress());
    }
    public static final MemoryIO wrap(long address) {
        return address == 0 ? getNullIO() : new NativeIO(address);
    }
    */
    public static final MemoryIO getNullIO() {
        return NullIO.INSTANCE;
    }
    static final void checkBounds(long size, long off, long len) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    static final class ShareIO extends AbstractMemoryIO {
        private final long base;
        private final MemoryIO io;
        public ShareIO(MemoryIO parent, long offset) {
            this.io = parent;
            this.base = offset;
        }
        public boolean isDirect() {
            return io.isDirect();
        }
        public byte getByte(long offset) {
            return io.getByte(base + offset);
        }

        public short getShort(long offset) {
            return io.getShort(base + offset);
        }

        public int getInt(long offset) {
            return io.getInt(base + offset);
        }

        public long getLong(long offset) {
            return io.getLong(base + offset);
        }

        public float getFloat(long offset) {
            return io.getFloat(base + offset);
        }

        public double getDouble(long offset) {
            return io.getDouble(base + offset);
        }

        public MemoryIO getMemoryIO(long offset) {
            return io.getMemoryIO(base + offset);
        }

        public MemoryIO getMemoryIO(long offset, long size) {
            return io.getMemoryIO(base + offset, size);
        }

        public Pointer getPointer(long offset) {
            return io.getPointer(base + offset);
        }

        public void putByte(long offset, byte value) {
            io.putByte(base + offset, value);
        }

        public void putShort(long offset, short value) {
            io.putShort(base + offset, value);
        }

        public void putInt(long offset, int value) {
            io.putInt(base + offset, value);
        }

        public void putLong(long offset, long value) {
            io.putLong(base + offset, value);
        }

        public void putFloat(long offset, float value) {
            io.putFloat(base + offset, value);
        }

        
        public void putDouble(long offset, double value) {
            io.putDouble(base + offset, value);
        }

        
        public void putPointer(long offset, Pointer value) {
            io.putPointer(base + offset, value);
        }

        
        public void get(long offset, byte[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, byte[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        
        public void get(long offset, short[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, short[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        
        public void get(long offset, int[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, int[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        
        public void get(long offset, long[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, long[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        
        public void get(long offset, float[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }
        
        public void put(long offset, float[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        
        public void get(long offset, double[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, double[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        
        public int indexOf(long offset, byte value, int maxlen) {
            return io.indexOf(base + offset, value, maxlen);
        }
    }
    static final class BoundedIO extends AbstractMemoryIO {
        private final long base, size;
        private final MemoryIO io;
        public BoundedIO(MemoryIO parent, long offset, long size) {
            this.io = parent;
            this.base = offset;
            this.size = size;
        }
        public boolean isDirect() {
            return io.isDirect();
        }

        
        public byte getByte(long offset) {
            checkBounds(size, base + offset, 1);
            return io.getByte(base + offset);
        }

        
        public short getShort(long offset) {
            checkBounds(size, base + offset, 2);
            return io.getShort(base + offset);
        }

        
        public int getInt(long offset) {
            checkBounds(size, base + offset, 4);
            return io.getInt(base + offset);
        }

        
        public long getLong(long offset) {
            checkBounds(size, base + offset, 8);
            return io.getLong(base + offset);
        }

        
        public float getFloat(long offset) {
            checkBounds(size, base + offset, 4);
            return io.getFloat(base + offset);
        }

        
        public double getDouble(long offset) {
            checkBounds(size, base + offset, 8);
            return io.getDouble(base + offset);
        }

        
        public MemoryIO getMemoryIO(long offset) {
            checkBounds(this.size, base + offset, Address.SIZE / 8);
            return io.getMemoryIO(base + offset);
        }

        
        public MemoryIO getMemoryIO(long offset, long size) {
            checkBounds(this.size, base + offset, Address.SIZE / 8);
            return io.getMemoryIO(base + offset, size);
        }

        
        public Pointer getPointer(long offset) {
            checkBounds(size, base + offset, Address.SIZE / 8);
            return io.getPointer(base + offset);
        }

        
        public void putByte(long offset, byte value) {
            checkBounds(size, base + offset, 1);
            io.putByte(base + offset, value);
        }

        
        public void putShort(long offset, short value) {
            checkBounds(size, base + offset, 2);
            io.putShort(base + offset, value);
        }

        
        public void putInt(long offset, int value) {
            checkBounds(size, base + offset, 4);
            io.putInt(base + offset, value);
        }

        
        public void putLong(long offset, long value) {
            checkBounds(size, base + offset, 8);
            io.putLong(base + offset, value);
        }

        
        public void putFloat(long offset, float value) {
            checkBounds(size, base + offset, 4);
            io.putFloat(base + offset, value);
        }

        
        public void putDouble(long offset, double value) {
            checkBounds(size, base + offset, 8);
            io.putDouble(base + offset, value);
        }

        
        public void putPointer(long offset, Pointer value) {
            checkBounds(size, base + offset, Address.SIZE / 8);
            io.putPointer(base + offset, value);
        }

        
        public void get(long offset, byte[] dst, int off, int len) {
            checkBounds(size, base + offset, len);
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, byte[] dst, int off, int len) {
            checkBounds(size, base + offset, len);
            io.put(base + offset, dst, off, len);
        }
        
        public void get(long offset, short[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Short.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, short[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Short.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }

        
        public void get(long offset, int[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Integer.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, int[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Integer.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }

        
        public void get(long offset, long[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Long.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, long[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Long.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }
        
        public void get(long offset, float[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Float.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, float[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Float.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }

        
        public void get(long offset, double[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Double.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        
        public void put(long offset, double[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Double.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }
        
        public long getAddress(long offset) {
            checkBounds(size, base + offset, Address.SIZE >> 3);
            return io.getAddress(base + offset);
        }

        
        public String getString(long offset, int maxLength, Charset cs) {
            checkBounds(size, base + offset, maxLength);
            return io.getString(base + offset, maxLength, cs);
        }


        
        public void putAddress(long offset, long value) {
            checkBounds(size, base + offset, Address.SIZE >> 3);
            io.putAddress(base + offset, value);
        }

        
        public void putAddress(long offset, Address value) {
            checkBounds(size, base + offset, Address.SIZE >> 3);
            super.putAddress(base + offset, value);
        }

        
        public void putString(long offset, String string, int maxLength, Charset cs) {
            checkBounds(size, base + offset, maxLength);
            io.putString(base + offset, string, maxLength, cs);
        }

        
        public int indexOf(long offset, byte value) {
            return io.indexOf(base + offset, value, (int) size);
        }

        
        public int indexOf(long offset, byte value, int maxlen) {
            checkBounds(size, base + offset, maxlen);
            return io.indexOf(base + offset, value, maxlen);
        }
    }
    static final class NullIO extends AbstractMemoryIO {
        private static final String msg = "Attempted access to a NULL memory address";
        static final MemoryIO INSTANCE = new NullIO();
        private final NullPointerException npe() {
            return new NullPointerException(msg);
        }
        
        public byte getByte(long offset) {
            throw npe();
        }

        
        public short getShort(long offset) {
            throw npe();
        }

        
        public int getInt(long offset) {
            throw npe();
        }

        
        public long getLong(long offset) {
            throw npe();
        }

        
        public float getFloat(long offset) {
            throw npe();
        }

        
        public double getDouble(long offset) {
            throw npe();
        }

        
        public void putByte(long offset, byte value) {
            throw npe();
        }

        
        public void putShort(long offset, short value) {
            throw npe();
        }

        
        public void putInt(long offset, int value) {
            throw npe();
        }

        
        public void putLong(long offset, long value) {
            throw npe();
        }

        
        public void putFloat(long offset, float value) {
            throw npe();
        }

        
        public void putDouble(long offset, double value) {
            throw npe();
        }

        
        public void get(long offset, byte[] dst, int off, int len) {
            throw npe();
        }

        
        public void put(long offset, byte[] dst, int off, int len) {
            throw npe();
        }

        
        public void get(long offset, short[] dst, int off, int len) {
            throw npe();
        }

        
        public void put(long offset, short[] dst, int off, int len) {
            throw npe();
        }

        
        public void get(long offset, int[] dst, int off, int len) {
            throw npe();
        }

        
        public void put(long offset, int[] dst, int off, int len) {
            throw npe();
        }

        
        public void get(long offset, long[] dst, int off, int len) {
            throw npe();
        }

        
        public void put(long offset, long[] dst, int off, int len) {
            throw npe();
        }

        
        public void get(long offset, float[] dst, int off, int len) {
            throw npe();
        }

        
        public void put(long offset, float[] dst, int off, int len) {
            throw npe();
        }

        
        public void get(long offset, double[] dst, int off, int len) {
            throw npe();
        }

        
        public void put(long offset, double[] dst, int off, int len) {
            throw npe();
        }

        
        public MemoryIO getMemoryIO(long offset) {
            throw npe();
        }

        
        public MemoryIO getMemoryIO(long offset, long size) {
            throw npe();
        }

        
        public Pointer getPointer(long offset) {
            throw npe();
        }

        
        public void putPointer(long offset, Pointer value) {
            throw npe();
        }

        
        public int indexOf(long offset, byte value, int maxlen) {
            throw npe();
        }

        
        public boolean isDirect() {
            throw npe();
        }
    }
    static class BufferIO extends AbstractMemoryIO {
        private final ByteBuffer buffer;
        public BufferIO(ByteBuffer buffer) {
            this.buffer = buffer;
        }
        public final boolean isDirect() {
            return buffer.isDirect();
        }
        public byte getByte(long offset) {
            return buffer.get((int) offset);
        }

        public short getShort(long offset) {
            return buffer.getShort((int) offset);
        }

        public int getInt(long offset) {
            return buffer.getInt((int) offset);
        }

        public long getLong(long offset) {
            return buffer.getLong((int) offset);
        }

        public float getFloat(long offset) {
            return buffer.getFloat((int) offset);
        }

        public double getDouble(long offset) {
            return buffer.getDouble((int) offset);
        }

        
        public MemoryIO getMemoryIO(long offset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public MemoryIO getMemoryIO(long offset, long size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        
        public Pointer getPointer(long offset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void putByte(long offset, byte value) {
            buffer.put((int) offset, value);
        }

        public void putShort(long offset, short value) {
            buffer.putShort((int) offset, value);
        }

        public void putInt(long offset, int value) {
            buffer.putInt((int) offset, value);
        }

        public void putLong(long offset, long value) {
            buffer.putLong((int) offset, value);
        }

        public void putFloat(long offset, float value) {
            buffer.putFloat((int) offset, value);
        }

        public void putDouble(long offset, double value) {
            buffer.putDouble((int) offset, value);
        }

        
        public void putPointer(long offset, Pointer value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getString(long offset, int size) {
            return BufferUtil.getString(BufferUtil.slice(buffer, (int) offset), Charset.defaultCharset());
        }
        public void putString(long offset, String string) {
            BufferUtil.putString(BufferUtil.slice(buffer, (int) offset), Charset.defaultCharset(), string);
        }

        
        public void get(long offset, byte[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len).get(dst, off, len);
        }

        
        public void get(long offset, short[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Short.SIZE / 8).asShortBuffer().get(dst, off, len);
        }

        
        public void get(long offset, int[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Integer.SIZE / 8).asIntBuffer().get(dst, off, len);
        }

        
        public void get(long offset, long[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Long.SIZE / 8).asLongBuffer().get(dst, off, len);
        }

        
        public void get(long offset, float[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Float.SIZE / 8).asFloatBuffer().get(dst, off, len);
        }

        
        public void get(long offset, double[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Double.SIZE / 8).asDoubleBuffer().get(dst, off, len);
        }

        
        public void put(long offset, byte[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len).put(dst, off, len);
        }

        
        public void put(long offset, short[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Short.SIZE / 8).asShortBuffer().put(dst, off, len);
        }

        
        public void put(long offset, int[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Integer.SIZE / 8).asIntBuffer().put(dst, off, len);
        }

        
        public void put(long offset, long[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Long.SIZE / 8).asLongBuffer().put(dst, off, len);
        }

        
        public void put(long offset, float[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Float.SIZE / 8).asFloatBuffer().put(dst, off, len);
        }

        
        public void put(long offset, double[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Double.SIZE / 8).asDoubleBuffer().put(dst, off, len);
        }

        
        public String getString(long offset, int maxLength, Charset cs) {
            return BufferUtil.getString(BufferUtil.slice(buffer, (int) offset, maxLength),
                    cs);
        }

        
        public void putString(long offset, String string, int maxLength, Charset cs) {
            BufferUtil.putString(BufferUtil.slice(buffer, (int) offset, maxLength), cs, string);
        }

        
        public int indexOf(long offset, byte value, int maxlen) {
            for (; offset > -1; ++offset) {
                if (buffer.get((int) offset) == value) {
                    return (int) offset;
                }
            }
            return -1;
        }
    }

}
