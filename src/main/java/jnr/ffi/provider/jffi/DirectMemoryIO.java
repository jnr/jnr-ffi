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

package jnr.ffi.provider.jffi;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.AbstractMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class DirectMemoryIO extends AbstractMemoryIO {
    static final com.kenai.jffi.MemoryIO IO = com.kenai.jffi.MemoryIO.getInstance();
    DirectMemoryIO(Runtime runtime, long address) {
        super(runtime, address, true);
    }

    DirectMemoryIO(Runtime runtime, int address) {
        super(runtime, (long) address & 0xffffffffL, true);
    }

    public long size() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public Object array() {
        throw new UnsupportedOperationException("no array");
    }

    @Override
    public int arrayOffset() {
        throw new UnsupportedOperationException("no array");
    }

    @Override
    public int arrayLength() {
        throw new UnsupportedOperationException("no array");
    }

    @Override
    public int hashCode() {
        return (int) ((address() << 32L) ^ address());
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pointer && ((Pointer) obj).address() == address() && ((Pointer) obj).getRuntime().isCompatible(getRuntime());
    }


    public final byte getByte(long offset) {
        return IO.getByte(address() + offset);
    }

    public final short getShort(long offset) {
        return IO.getShort(address() + offset);
    }

    public final int getInt(long offset) {
        return IO.getInt(address() + offset);
    }

    public final long getLongLong(long offset) {
        return IO.getLong(address() + offset);
    }

    public final float getFloat(long offset) {
        return IO.getFloat(address() + offset);
    }

    public final double getDouble(long offset) {
        return IO.getDouble(address() + offset);
    }

    public final void putByte(long offset, byte value) {
        IO.putByte(address() + offset, value);
    }

    public final void putShort(long offset, short value) {
        IO.putShort(address() + offset, value);
    }

    public final void putInt(long offset, int value) {
        IO.putInt(address() + offset, value);
    }
    
    public final void putLongLong(long offset, long value) {
        IO.putLong(address() + offset, value);
    }

    public final void putFloat(long offset, float value) {
        IO.putFloat(address() + offset, value);
    }

    public final void putDouble(long offset, double value) {
        IO.putDouble(address() + offset, value);
    }

    public final void get(long offset, byte[] dst, int off, int len) {
        IO.getByteArray(address() + offset, dst, off, len);
    }

    public final void put(long offset, byte[] src, int off, int len) {
        IO.putByteArray(address() + offset, src, off, len);
    }

    public final void get(long offset, short[] dst, int off, int len) {
        IO.getShortArray(address() + offset, dst, off, len);
    }

    public final void put(long offset, short[] src, int off, int len) {
        IO.putShortArray(address() + offset, src, off, len);
    }

    public final void get(long offset, int[] dst, int off, int len) {
        IO.getIntArray(address() + offset, dst, off, len);
    }

    public final void put(long offset, int[] src, int off, int len) {
        IO.putIntArray(address() + offset, src, off, len);
    }

    public final void get(long offset, long[] dst, int off, int len) {
        IO.getLongArray(address() + offset, dst, off, len);
    }

    public final void put(long offset, long[] src, int off, int len) {
        IO.putLongArray(address() + offset, src, off, len);
    }

    public final void get(long offset, float[] dst, int off, int len) {
        IO.getFloatArray(address() + offset, dst, off, len);
    }

    public final void put(long offset, float[] src, int off, int len) {
        IO.putFloatArray(address() + offset, src, off, len);
    }

    public final void get(long offset, double[] dst, int off, int len) {
        IO.getDoubleArray(address() + offset, dst, off, len);
    }

    public final void put(long offset, double[] src, int off, int len) {
        IO.putDoubleArray(address() + offset, src, off, len);
    }

    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(getRuntime(), IO.getAddress(address() + offset));
    }
    
    public Pointer getPointer(long offset, long size) {
        return MemoryUtil.newPointer(getRuntime(), IO.getAddress(this.address() + offset), size);
    }

    public void putPointer(long offset, Pointer value) {
        IO.putAddress(address() + offset, value != null ? value.address() : 0L);
    }

    public String getString(long offset) {
        return Charset.defaultCharset().decode(ByteBuffer.wrap(IO.getZeroTerminatedByteArray(address() + offset))).toString();
    }


    public String getString(long offset, int maxLength, Charset cs) {
    	long baseAddress = address() + offset;
    	
        if(cs == StandardCharsets.UTF_8){
            final byte[] bytes = IO.getZeroTerminatedByteArray(baseAddress, maxLength);
            return cs.decode(ByteBuffer.wrap(bytes)).toString();
        }else{
        	if(address() == 0) {
        		return null;
        	}
        	
            final byte[] nullCharBytes = new String("\0").getBytes(cs);
            int nullTerminatedLen = 0;
            int matchingBytesCount = 0;
            int i = 0;
            while(i < maxLength){
                if(IO.getByte(baseAddress+i) == nullCharBytes[matchingBytesCount]){
                    matchingBytesCount++;
                    i++;
                } else {
                    matchingBytesCount = 0;
                    i += nullCharBytes.length - (i%nullCharBytes.length);//jump to start of next character
                    continue;
                }
                if(matchingBytesCount == nullCharBytes.length){
                    nullTerminatedLen = i-nullCharBytes.length;//trim to the last byte just before null terminator
                    break;
                }
            }

            if(nullTerminatedLen == 0) {
            	return "";
            }
            
            byte[] bytes = new byte[nullTerminatedLen];
            IO.getByteArray(baseAddress, bytes, 0, nullTerminatedLen);
            return new String(bytes, 0, bytes.length, cs);
        }
    }

    public void putString(long offset, String string, int maxLength, Charset cs) {
        ByteBuffer buf = cs.encode(string);
        int len = Math.min(maxLength, buf.remaining());
        IO.putZeroTerminatedByteArray(address() + offset, buf.array(), buf.arrayOffset() + buf.position(), len);
    }

    public void putZeroTerminatedByteArray(long offset, byte[] src, int off, int len) {
        IO.putZeroTerminatedByteArray(address() + offset, src, off, len);
    }


    public int indexOf(long offset, byte value, int maxlen) {
        return (int) IO.indexOf(address() + offset, value, maxlen);
    }

    public final void setMemory(long offset, long size, byte value) {
        IO.setMemory(this.address() + offset, size, value);
    }

    @Override
    public void transferTo(long offset, Pointer other, long otherOffset, long count) {
        Pointer dst = other instanceof DelegatingMemoryIO ? ((DelegatingMemoryIO) other).getDelegatedMemoryIO() : other;

        if (dst instanceof DirectMemoryIO) {
            other.checkBounds(otherOffset, count);
            memcpy(this, offset, ((DirectMemoryIO) dst), otherOffset, count);
        } else {
            super.transferTo(offset, other, otherOffset, count);
        }
    }

    @Override
    public void transferFrom(long offset, Pointer other, long otherOffset, long count) {
        Pointer src = other instanceof DelegatingMemoryIO ? ((DelegatingMemoryIO) other).getDelegatedMemoryIO() : other;

        if (src instanceof DirectMemoryIO) {
            other.checkBounds(otherOffset, count);
            memcpy(((DirectMemoryIO) src), otherOffset, this, offset, count);
        } else {
            super.transferFrom(offset, other, otherOffset, count);
        }
    }

    private static void memcpy(DirectMemoryIO src, long srcOffset, DirectMemoryIO dst, long dstOffset, long count) {
        IO.memcpy(dst.address() + dstOffset, src.address() + srcOffset, count);
    }
}
