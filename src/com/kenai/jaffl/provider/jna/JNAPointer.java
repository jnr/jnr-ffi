/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.Pointer;

/**
 *
 * @author wayne
 */
class JNAPointer implements com.kenai.jaffl.Pointer {
    private final com.sun.jna.Pointer ptr;
    JNAPointer(com.sun.jna.Pointer ptr) {
        this.ptr = ptr;
    }
    com.sun.jna.Pointer getNativePointer() {
        return ptr;
    }
    public static JNAPointer wrap(com.sun.jna.Pointer ptr) {
        return new JNAPointer(ptr);
    }
    public byte getByte(long offset) {
        return ptr.getByte(offset);
    }

    public short getShort(long offset) {
        return ptr.getShort(offset);
    }

    public int getInt(long offset) {
        return ptr.getInt(offset);
    }

    public long getLong(long offset) {
        return ptr.getLong(offset);
    }

    public float getFloat(long offset) {
        return ptr.getFloat(offset);
    }

    public double getDouble(long offset) {
        return ptr.getDouble(offset);
    }

    public void putByte(long offset, byte value) {
        ptr.setByte(offset, value);
    }

    public void putShort(long offset, short value) {
        ptr.setShort(offset, value);
    }

    public void putInt(long offset, int value) {
        ptr.setInt(offset, value);
    }

    public void putLong(long offset, long value) {
        ptr.setLong(offset, value);
    }

    public void putFloat(long offset, float value) {
        ptr.setFloat(offset, value);
    }

    public void putDouble(long offset, double value) {
        ptr.setDouble(offset, value);
    }

    public void get(long offset, byte[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, byte[] src, int off, int len) {
        ptr.write(offset, src, off, len);
    }

    public void get(long offset, short[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, short[] src, int off, int len) {
        ptr.write(offset, src, off, len);
    }

    public void get(long offset, int[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, int[] src, int off, int len) {
        ptr.write(offset, src, off, len);
    }

    public void get(long offset, long[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, long[] src, int off, int len) {
        ptr.write(offset, src, off, len);
    }

    public void get(long offset, float[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, float[] src, int off, int len) {
        ptr.write(offset, src, off, len);
    }

    public void get(long offset, double[] dst, int off, int len) {
        ptr.read(offset, dst, off, len);
    }

    public void put(long offset, double[] src, int off, int len) {
        ptr.write(offset, src, off, len);
    }

    public com.kenai.jaffl.Pointer getPointer(long offset) {
        return new JNAPointer(ptr.getPointer(offset));
    }

    public void putPointer(long offset, Pointer value) {
        ptr.setPointer(offset, ((JNAPointer) value).ptr);
    }

    public String getString(long offset) {
        return ptr.getString(offset);
    }

}
