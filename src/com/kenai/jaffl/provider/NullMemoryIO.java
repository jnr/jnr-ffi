
package com.kenai.jaffl.provider;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;

public final class NullMemoryIO extends AbstractMemoryIO {
    private static final String msg = "Attempted access to a NULL memory address";

    public static final MemoryIO INSTANCE = new NullMemoryIO();

    private static final NullPointerException npe() {
        return new NullPointerException(msg);
    }

    public final byte getByte(long offset) {
        throw npe();
    }

    public final short getShort(long offset) {
        throw npe();
    }

    public final int getInt(long offset) {
        throw npe();
    }

    public final long getLong(long offset) {
        throw npe();
    }

    public final float getFloat(long offset) {
        throw npe();
    }

    public final double getDouble(long offset) {
        throw npe();
    }

    public final void putByte(long offset, byte value) {
        throw npe();
    }

    public final void putShort(long offset, short value) {
        throw npe();
    }

    public final void putInt(long offset, int value) {
        throw npe();
    }

    public final void putLong(long offset, long value) {
        throw npe();
    }

    public final void putFloat(long offset, float value) {
        throw npe();
    }

    public final void putDouble(long offset, double value) {
        throw npe();
    }

    public final void get(long offset, byte[] dst, int off, int len) {
        throw npe();
    }

    public final void put(long offset, byte[] dst, int off, int len) {
        throw npe();
    }

    public final void get(long offset, short[] dst, int off, int len) {
        throw npe();
    }

    public final void put(long offset, short[] dst, int off, int len) {
        throw npe();
    }

    public final void get(long offset, int[] dst, int off, int len) {
        throw npe();
    }

    public final void put(long offset, int[] dst, int off, int len) {
        throw npe();
    }

    public final void get(long offset, long[] dst, int off, int len) {
        throw npe();
    }

    public final void put(long offset, long[] dst, int off, int len) {
        throw npe();
    }

    public final void get(long offset, float[] dst, int off, int len) {
        throw npe();
    }

    public final void put(long offset, float[] dst, int off, int len) {
        throw npe();
    }

    public final void get(long offset, double[] dst, int off, int len) {
        throw npe();
    }

    public final void put(long offset, double[] dst, int off, int len) {
        throw npe();
    }

    public final MemoryIO getMemoryIO(long offset) {
        throw npe();
    }

    public final MemoryIO getMemoryIO(long offset, long size) {
        throw npe();
    }

    public final Pointer getPointer(long offset) {
        throw npe();
    }

    public final void putPointer(long offset, Pointer value) {
        throw npe();
    }

    public final int indexOf(long offset, byte value, int maxlen) {
        throw npe();
    }

    public final boolean isDirect() {
        return true;
    }

    @Override
    public final void setMemory(long offset, long size, byte value) {
        throw npe();
    }
}
