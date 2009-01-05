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

package com.kenai.jaffl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author wayne
 */
public class MemoryIOTest {

    public MemoryIOTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    private static final Pointer getBufferPointer(ByteBuffer buffer) {
        return TstUtil.getDirectBufferPointer(buffer);
    }
    private static final MemoryIO wrapPointer(Pointer ptr, int size) {
        return MemoryIO.wrap(ptr, size);
    }
    private static final MemoryIO wrapPointer(Pointer ptr) {
        return MemoryIO.wrap(ptr);
    }
    private static final MemoryIO wrap(ByteBuffer buffer) {
        return MemoryIO.wrap(buffer);
    }
    private static final MemoryIO allocateDirect(int size) {
        return MemoryIO.allocateDirect(size);
    }
    private final void testPutByte(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i < size; ++i) {
            io.putByte(i, (byte) (i + 5));
            assertEquals("Incorrect value at offset " + i, (byte) (i + 5), buffer.get(i));
        }
    }
    private final void testGetByte(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i < size; ++i) {
            buffer.put(i, (byte) (i + 5));
            assertEquals("Incorrect value at offset " + i, (byte) (i + 5), io.getByte(i));
        }
    }
    private final void testPutShort(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - 2; ++i) {
            io.putShort(i, (short) i);
            assertEquals("Incorrect value at offset " + i, (short) i, buffer.getShort(i));
        }
    }
    private final void testGetShort(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - 2; ++i) {
            buffer.putShort(i, (short) i);
            assertEquals("Incorrect value at offset " + i, (short) i, io.getShort(i));
        }
    }
    private final void testPutInt(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - 4; ++i) {
            io.putInt(i, i);
            assertEquals("Incorrect value at offset " + i, i, buffer.getInt(i));
        }
    }
    private final void testGetInt(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - 4; ++i) {
            buffer.putInt(i, i);
            assertEquals("Incorrect value at offset " + i, i, io.getInt(i));
        }
    }
    private final void testPutLong(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - 8; ++i) {
            io.putLong(i, i);
            assertEquals("Incorrect value at offset " + i, (long) i, buffer.getLong(i));
        }
    }
    private final void testGetLong(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - 8; ++i) {
            buffer.putLong(i, i);
            assertEquals("Incorrect value at offset " + i, (long) i, io.getLong(i));
        }
    }
    private final void testPutFloat(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - (Float.SIZE / 8); ++i) {
            io.putFloat(i, i);
            assertEquals("Incorrect value at offset " + i, (float) i, buffer.getFloat(i), 0f);
        }
    }
    private final void testGetFloat(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - (Float.SIZE / 8); ++i) {
            buffer.putFloat(i, i);
            assertEquals("Incorrect value at offset " + i, (float) i, io.getFloat(i), 0f);
        }
    }
    private final void testPutDouble(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - (Double.SIZE / 8); ++i) {
            io.putDouble(i, (double) i);
            assertEquals("Incorrect value at offset " + i, (double) i, buffer.getDouble(i), 0d);
        }
    }
    private final void testGetDouble(MemoryIO io, ByteBuffer buffer, int size) {
        for (int i = 0; i <= size - (Double.SIZE / 8); ++i) {
            buffer.putDouble(i, (double) i);
            assertEquals("Incorrect value at offset " + i, (double) i, io.getDouble(i), 0d);
        }
    }
    @Test public void testBoundedIOPutByte() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE);
        testPutByte(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOGetByte() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE);
        testGetByte(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOPutShort() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutShort(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOGetShort() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetShort(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOPutInt() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutInt(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOGetInt() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetInt(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOPutLong() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutLong(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOGetLong() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetLong(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOPutFloat() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutFloat(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);

    }
    @Test public void testBoundedIOGetFloat() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetFloat(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);

    }
    @Test public void testBoundedIOPutDouble() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutDouble(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test public void testBoundedIOGetDouble() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetDouble(wrapPointer(getBufferPointer(buffer), SIZE), buffer, SIZE);
    }
    @Test
    public void testNegativeBoundedIO() {
        final int SIZE = 16;
        MemoryIO memio = allocateDirect(SIZE);
        try {
            memio.putByte(-1, (byte) 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            
        }
    }
    @Test
    public void testOverflowBoundedIO() {
        final int SIZE = 16;
        MemoryIO memio = allocateDirect(SIZE);
        try {
            memio.putByte(16, (byte) 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            
        }
    }
    @Test public void testNativeIOPutByte() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE);
        testPutByte(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOGetByte() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE);
        testGetByte(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOPutShort() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutShort(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOGetShort() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetShort(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOPutInt() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutInt(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOGetInt() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetInt(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOPutLong() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutLong(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOGetLong() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetLong(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOPutFloat() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutFloat(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);

    }
    @Test public void testNativeIOGetFloat() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetFloat(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOPutDouble() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testPutDouble(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testNativeIOGetDouble() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocateDirect(SIZE).order(ByteOrder.nativeOrder());
        testGetDouble(wrapPointer(getBufferPointer(buffer)), buffer, SIZE);
    }
    @Test public void testBufferIOPutByte() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        testPutByte(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOGetByte() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        testGetByte(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOPutShort() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testPutShort(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOGetShort() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testGetShort(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOPutInt() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testPutInt(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOGetInt() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testGetInt(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOPutLong() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testPutLong(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOGetLong() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testGetLong(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOPutFloat() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testPutFloat(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOGetFloat() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testGetFloat(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOPutDouble() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testPutDouble(wrap(buffer), buffer, SIZE);
    }
    @Test public void testBufferIOGetDouble() {
        final int SIZE = 16;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.nativeOrder());
        testGetDouble(wrap(buffer), buffer, SIZE);
    }
    @Test
    public void testNegativeBufferIO() {
        final int SIZE = 16;
        MemoryIO memio = wrap(ByteBuffer.allocate(SIZE));
        try {
            memio.putByte(-1, (byte) 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            
        }
    }
    @Test
    public void testOverflowBufferIO() {
        final int SIZE = 16;
        MemoryIO memio = wrap(ByteBuffer.allocate(SIZE));
        try {
            memio.putByte(16, (byte) 0);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            
        }
    }
    @Test public void transferDirectToHeap() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        MemoryIO dst = MemoryIO.wrap(buf);
        MemoryIO src = MemoryIO.allocateDirect(1024);
        byte[] MAGIC = "MAGIC".getBytes();
        src.put(0, MAGIC, 0, MAGIC.length);
        src.transferTo(0, dst, 0, MAGIC.length);
        for (int i = 0; i < MAGIC.length; ++i) {
            assertEquals("Wrong byte at index " + i, MAGIC[i], dst.getByte(i));
        }
        for (int i = 0; i < MAGIC.length; ++i) {
            assertEquals("Wrong byte at index " + i, MAGIC[i], buf.get(i));
        }
    }
    @Test public void transferDirectToDirect() throws Exception {
        MemoryIO dst = MemoryIO.allocateDirect(1024);
        MemoryIO src = MemoryIO.allocateDirect(1024);
        final byte[] MAGIC = "MAGIC".getBytes();
        final int SRCOFF = 100;
        final int DSTOFF = 123;
        src.put(SRCOFF, MAGIC, 0, MAGIC.length);
        src.transferTo(SRCOFF, dst, DSTOFF, MAGIC.length);
        for (int i = 0; i < MAGIC.length; ++i) {
            assertEquals("Wrong byte at index " + i, MAGIC[i], dst.getByte(DSTOFF + i));
        }
    }
}