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

import jafl.util.BufferUtil;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;


/**
 * Interface to reading/writing various types of memory
 */
abstract public class MemoryIO {
    abstract public byte getByte(long offset);
    abstract public short getShort(long offset);
    abstract public int getInt(long offset);
    abstract public long getLong(long offset);
    abstract public float getFloat(long offset);
    abstract public double getDouble(long offset);
    abstract public void putByte(long offset, byte value);
    abstract public void putShort(long offset, short value);
    abstract public void putInt(long offset, int value);
    abstract public void putLong(long offset, long value);
    abstract public void putFloat(long offset, float value);
    abstract public void putDouble(long offset, double value);
    abstract public void get(long offset, byte[] dst, int off, int len);
    abstract public void put(long offset, byte[] dst, int off, int len);
    abstract public void get(long offset, short[] dst, int off, int len);
    abstract public void put(long offset, short[] dst, int off, int len);
    abstract public void get(long offset, int[] dst, int off, int len);
    abstract public void put(long offset, int[] dst, int off, int len);
    abstract public void get(long offset, long[] dst, int off, int len);
    abstract public void put(long offset, long[] dst, int off, int len);
    abstract public void get(long offset, float[] dst, int off, int len);
    abstract public void put(long offset, float[] dst, int off, int len);
    abstract public void get(long offset, double[] dst, int off, int len);
    abstract public void put(long offset, double[] dst, int off, int len);
    abstract public MemoryIO getMemoryIO(long offset);
    abstract public MemoryIO getMemoryIO(long offset, long size);
    abstract public Pointer getPointer(long offset);
    abstract public void putPointer(long offset, Pointer value);

    public int indexOf(long offset, byte value) {
        return indexOf(offset, value, Integer.MAX_VALUE);
    }
    abstract public int indexOf(long offset, byte value, int maxlen);
    abstract public boolean isDirect();
    
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
        return LongIO.INSTANCE.getLong(this, offset);
    }
    
    public final void putNativeLong(long offset, long value) {
        LongIO.INSTANCE.putLong(this, offset, value);
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
    public MemoryIO slice(long offset) {
        return new ShareIO(this, offset);
    }
    public MemoryIO slice(long offset, long size) {
        return new BoundedIO(this, offset, size);
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
    //
    // Optimize reading/writing pointers.
    //
    private static interface AddressIO {
        public long getAddress(MemoryIO io, long offset);
        public void putAddress(MemoryIO io, long offset, long address);
        public static class Address32Helper {
            public static final AddressIO INSTANCE = new AddressIO() {
                public long getAddress(MemoryIO io, long offset) {
                    return io.getInt(offset);
                }
                public void putAddress(MemoryIO io, long offset, long address) {
                    io.putInt(offset, (int) address);
                }
            };
        }
        public static class Address64Helper {
            public static final AddressIO INSTANCE = new AddressIO() {
                public long getAddress(MemoryIO io, long offset) {
                    return io.getLong(offset);
                }
                public void putAddress(MemoryIO io, long offset, long address) {
                    io.putLong(offset, address);
                }
            };
        }
        public static final AddressIO INSTANCE = Platform.getPlatform().addressSize() == 32
                ? Address32Helper.INSTANCE : Address64Helper.INSTANCE;
    }
    //
    // Optimize reading/writing native long values.
    //
    private static interface LongIO {
        public long getLong(MemoryIO io, long offset);
        public void putLong(MemoryIO io, long offset, long value);
        public static class Long32Helper {
            public static final LongIO INSTANCE = new LongIO() {
                public long getLong(MemoryIO io, long offset) {
                    return io.getInt(offset);
                }
                public void putLong(MemoryIO io, long offset, long value) {
                    io.putInt(offset, (int) value);
                }
            };
        }
        public static class Long64Helper {
            public static final LongIO INSTANCE = new LongIO() {
                public long getLong(MemoryIO io, long offset) {
                    return io.getLong(offset);
                }
                public void putLong(MemoryIO io, long offset, long value) {
                    io.putLong(offset, value);
                }
            };
        }
        public static final LongIO INSTANCE = Platform.getPlatform().longSize() == 32 
                ? Long32Helper.INSTANCE : Long64Helper.INSTANCE;
    }
    
    private static class BufferIO extends MemoryIO {
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

        @Override
        public MemoryIO getMemoryIO(long offset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MemoryIO getMemoryIO(long offset, long size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
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

        @Override
        public void putPointer(long offset, Pointer value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getString(long offset, int size) {
            return BufferUtil.getString(BufferUtil.slice(buffer, (int) offset), Charset.defaultCharset());
        }
        public void putString(long offset, String string) {
            BufferUtil.putString(BufferUtil.slice(buffer, (int) offset), Charset.defaultCharset(), string);
        }

        @Override
        public void get(long offset, byte[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len).get(dst, off, len);
        }
        
        @Override
        public void get(long offset, short[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Short.SIZE / 8).asShortBuffer().get(dst, off, len);            
        }

        @Override
        public void get(long offset, int[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Integer.SIZE / 8).asIntBuffer().get(dst, off, len);
        }

        @Override
        public void get(long offset, long[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Long.SIZE / 8).asLongBuffer().get(dst, off, len);
        }

        @Override
        public void get(long offset, float[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Float.SIZE / 8).asFloatBuffer().get(dst, off, len);
        }

        @Override
        public void get(long offset, double[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Double.SIZE / 8).asDoubleBuffer().get(dst, off, len);
        }
        
        @Override
        public void put(long offset, byte[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len).put(dst, off, len);
        }

        @Override
        public void put(long offset, short[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Short.SIZE / 8).asShortBuffer().put(dst, off, len);
        }

        @Override
        public void put(long offset, int[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Integer.SIZE / 8).asIntBuffer().put(dst, off, len);
        }

        @Override
        public void put(long offset, long[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Long.SIZE / 8).asLongBuffer().put(dst, off, len);
        }

        @Override
        public void put(long offset, float[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Float.SIZE / 8).asFloatBuffer().put(dst, off, len);
        }

        @Override
        public void put(long offset, double[] dst, int off, int len) {
            BufferUtil.slice(buffer, (int) offset, len * Double.SIZE / 8).asDoubleBuffer().put(dst, off, len);
        }
        
        @Override
        public String getString(long offset, int maxLength, Charset cs) { 
            return BufferUtil.getString(BufferUtil.slice(buffer, (int) offset, maxLength),
                    cs);
        }
        
        @Override
        public void putString(long offset, String string, int maxLength, Charset cs) {
            BufferUtil.putString(BufferUtil.slice(buffer, (int) offset, maxLength), cs, string);
        }
        
        @Override
        public int indexOf(long offset, byte value, int maxlen) {
            for (; offset > -1; ++offset) {
                if (buffer.get((int) offset) == value) {
                    return (int) offset;
                }
            }
            return -1;
        }
    }
    private static final void checkBounds(long size, long off, long len) {
        if ((off | len | (off + len) | (size - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }
    
    private static final class ShareIO extends MemoryIO {
        private final long base;
        private final MemoryIO io;
        public ShareIO(MemoryIO parent, long offset) {
            this.io = parent;
            this.base = offset;
        }
        public boolean isDirect() {
            return io.isDirect();
        }
        
        @Override
        public byte getByte(long offset) {
            return io.getByte(base + offset);
        }

        @Override
        public short getShort(long offset) {
            return io.getShort(base + offset);
        }

        @Override
        public int getInt(long offset) {
            return io.getInt(base + offset);
        }

        @Override
        public long getLong(long offset) {
            return io.getLong(base + offset);
        }

        @Override
        public float getFloat(long offset) {
            return io.getFloat(base + offset);
        }

        @Override
        public double getDouble(long offset) {
            return io.getDouble(base + offset);
        }

        @Override
        public MemoryIO getMemoryIO(long offset) {
            return io.getMemoryIO(base + offset);
        }

        @Override
        public MemoryIO getMemoryIO(long offset, long size) {
            return io.getMemoryIO(base + offset, size);
        }

        @Override
        public Pointer getPointer(long offset) {
            return io.getPointer(base + offset);
        }

        @Override
        public void putByte(long offset, byte value) {
            io.putByte(base + offset, value);
        }

        @Override
        public void putShort(long offset, short value) {
            io.putShort(base + offset, value);
        }

        @Override
        public void putInt(long offset, int value) {
            io.putInt(base + offset, value);
        }

        @Override
        public void putLong(long offset, long value) {
            io.putLong(base + offset, value);
        }

        @Override
        public void putFloat(long offset, float value) {
            io.putFloat(base + offset, value);
        }

        @Override
        public void putDouble(long offset, double value) {
            io.putDouble(base + offset, value);
        }

        @Override
        public void putPointer(long offset, Pointer value) {
            io.putPointer(base + offset, value);
        }

        @Override
        public void get(long offset, byte[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, byte[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        @Override
        public void get(long offset, short[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, short[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        @Override
        public void get(long offset, int[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, int[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        @Override
        public void get(long offset, long[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, long[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        @Override
        public void get(long offset, float[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }
        @Override
        public void put(long offset, float[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        @Override
        public void get(long offset, double[] dst, int off, int len) {
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, double[] dst, int off, int len) {
            io.put(base + offset, dst, off, len);
        }
        @Override
        public int indexOf(long offset, byte value, int maxlen) {
            return io.indexOf(base + offset, value, maxlen);
        }
    }
    private static final class BoundedIO extends MemoryIO {
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
        
        @Override
        public byte getByte(long offset) {
            checkBounds(size, base + offset, 1);
            return io.getByte(base + offset);
        }

        @Override
        public short getShort(long offset) {
            checkBounds(size, base + offset, 2);
            return io.getShort(base + offset);
        }

        @Override
        public int getInt(long offset) {
            checkBounds(size, base + offset, 4);
            return io.getInt(base + offset);
        }

        @Override
        public long getLong(long offset) {
            checkBounds(size, base + offset, 8);
            return io.getLong(base + offset);
        }

        @Override
        public float getFloat(long offset) {
            checkBounds(size, base + offset, 4);
            return io.getFloat(base + offset);
        }

        @Override
        public double getDouble(long offset) {
            checkBounds(size, base + offset, 8);
            return io.getDouble(base + offset);
        }

        @Override
        public MemoryIO getMemoryIO(long offset) {
            checkBounds(this.size, base + offset, Address.SIZE / 8);
            return io.getMemoryIO(base + offset);
        }

        @Override
        public MemoryIO getMemoryIO(long offset, long size) {
            checkBounds(this.size, base + offset, Address.SIZE / 8);
            return io.getMemoryIO(base + offset, size);
        }

        @Override
        public Pointer getPointer(long offset) {
            checkBounds(size, base + offset, Address.SIZE / 8);
            return io.getPointer(base + offset);
        }

        @Override
        public void putByte(long offset, byte value) {
            checkBounds(size, base + offset, 1);
            io.putByte(base + offset, value);
        }

        @Override
        public void putShort(long offset, short value) {
            checkBounds(size, base + offset, 2);
            io.putShort(base + offset, value);
        }

        @Override
        public void putInt(long offset, int value) {
            checkBounds(size, base + offset, 4);
            io.putInt(base + offset, value);
        }

        @Override
        public void putLong(long offset, long value) {
            checkBounds(size, base + offset, 8);
            io.putLong(base + offset, value);
        }

        @Override
        public void putFloat(long offset, float value) {
            checkBounds(size, base + offset, 4);
            io.putFloat(base + offset, value);
        }

        @Override
        public void putDouble(long offset, double value) {
            checkBounds(size, base + offset, 8);
            io.putDouble(base + offset, value);
        }

        @Override
        public void putPointer(long offset, Pointer value) {
            checkBounds(size, base + offset, Address.SIZE / 8);
            io.putPointer(base + offset, value);
        }

        @Override
        public void get(long offset, byte[] dst, int off, int len) {
            checkBounds(size, base + offset, len);
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, byte[] dst, int off, int len) {
            checkBounds(size, base + offset, len);
            io.put(base + offset, dst, off, len);
        }
        @Override
        public void get(long offset, short[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Short.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, short[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Short.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }
        
        @Override
        public void get(long offset, int[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Integer.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, int[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Integer.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }

        @Override
        public void get(long offset, long[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Long.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, long[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Long.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }
        @Override
        public void get(long offset, float[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Float.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, float[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Float.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }
        
        @Override
        public void get(long offset, double[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Double.SIZE / 8);
            io.get(base + offset, dst, off, len);
        }

        @Override
        public void put(long offset, double[] dst, int off, int len) {
            checkBounds(size, base + offset, len * Double.SIZE / 8);
            io.put(base + offset, dst, off, len);
        }
        @Override
        public long getAddress(long offset) {
            checkBounds(size, base + offset, Address.SIZE >> 3);
            return io.getAddress(base + offset);
        }

        @Override
        public String getString(long offset, int maxLength, Charset cs) {
            checkBounds(size, base + offset, maxLength);
            return io.getString(base + offset, maxLength, cs);
        }

        
        @Override
        public void putAddress(long offset, long value) {
            checkBounds(size, base + offset, Address.SIZE >> 3);
            io.putAddress(base + offset, value);
        }

        @Override
        public void putAddress(long offset, Address value) {
            checkBounds(size, base + offset, Address.SIZE >> 3);
            super.putAddress(base + offset, value);
        }

        @Override
        public void putString(long offset, String string, int maxLength, Charset cs) {
            checkBounds(size, base + offset, maxLength);
            io.putString(base + offset, string, maxLength, cs);
        }
        
        @Override
        public int indexOf(long offset, byte value) {
            return io.indexOf(base + offset, value, (int) size);
        }

        @Override
        public int indexOf(long offset, byte value, int maxlen) {
            checkBounds(size, base + offset, maxlen);
            return io.indexOf(base + offset, value, maxlen);
        }
    }
    private static final class NullIO extends MemoryIO {
        private static final String msg = "Attempted access to a NULL memory address";
        private final NullPointerException npe() {
            return new NullPointerException(msg);
        }
        @Override
        public byte getByte(long offset) {
            throw npe();
        }

        @Override
        public short getShort(long offset) {
            throw npe();
        }

        @Override
        public int getInt(long offset) {
            throw npe();
        }

        @Override
        public long getLong(long offset) {
            throw npe();
        }

        @Override
        public float getFloat(long offset) {
            throw npe();
        }

        @Override
        public double getDouble(long offset) {
            throw npe();
        }

        @Override
        public void putByte(long offset, byte value) {
            throw npe();
        }

        @Override
        public void putShort(long offset, short value) {
            throw npe();
        }

        @Override
        public void putInt(long offset, int value) {
            throw npe();
        }

        @Override
        public void putLong(long offset, long value) {
            throw npe();
        }

        @Override
        public void putFloat(long offset, float value) {
            throw npe();
        }

        @Override
        public void putDouble(long offset, double value) {
            throw npe();
        }

        @Override
        public void get(long offset, byte[] dst, int off, int len) {
            throw npe();
        }

        @Override
        public void put(long offset, byte[] dst, int off, int len) {
            throw npe();
        }
        
        @Override
        public void get(long offset, short[] dst, int off, int len) {
            throw npe();
        }

        @Override
        public void put(long offset, short[] dst, int off, int len) {
            throw npe();
        }
        
        @Override
        public void get(long offset, int[] dst, int off, int len) {
            throw npe();
        }

        @Override
        public void put(long offset, int[] dst, int off, int len) {
            throw npe();
        }
        
        @Override
        public void get(long offset, long[] dst, int off, int len) {
            throw npe();
        }

        @Override
        public void put(long offset, long[] dst, int off, int len) {
            throw npe();
        }
        
        @Override
        public void get(long offset, float[] dst, int off, int len) {
            throw npe();
        }

        @Override
        public void put(long offset, float[] dst, int off, int len) {
            throw npe();
        }
        
        @Override
        public void get(long offset, double[] dst, int off, int len) {
            throw npe();
        }

        @Override
        public void put(long offset, double[] dst, int off, int len) {
            throw npe();
        }

        @Override
        public MemoryIO getMemoryIO(long offset) {
            throw npe();
        }

        @Override
        public MemoryIO getMemoryIO(long offset, long size) {
            throw npe();
        }

        @Override
        public Pointer getPointer(long offset) {
            throw npe();
        }

        @Override
        public void putPointer(long offset, Pointer value) {
            throw npe();
        }

        @Override
        public int indexOf(long offset, byte value, int maxlen) {
            throw npe();
        }

        @Override
        public boolean isDirect() {
            throw npe();
        }

        private static final MemoryIO INSTANCE = new NullIO();
    }
}
