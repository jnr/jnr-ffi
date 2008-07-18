/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jafl;

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
    static final class NullIO extends AbstractMemoryIO {
        private static final String msg = "Attempted access to a NULL memory address";
        static final MemoryIO INSTANCE = new NullIO();
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

        
    }
}
