/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kenai.jaffl.provider;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;

/**
 *
 * @author wayne
 */
public class NullMemoryIO extends AbstractMemoryIO {
    private static final String msg = "Attempted access to a NULL memory address";

    public static final MemoryIO INSTANCE = new NullMemoryIO();

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

    @Override
    public void setMemory(long offset, long size, byte value) {
        throw npe();
    }
}
