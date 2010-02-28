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
    public static interface TestLib {
        byte ptr_ret_int8_t(Pointer p, int offset);
        short ptr_ret_int16_t(Pointer p, int offset);
        int ptr_ret_int32_t(Pointer p, int offset);
        long ptr_ret_int64_t(Pointer p, int offset);
        float ptr_ret_float(Pointer p, int offset);
        double ptr_ret_double(Pointer p, int offset);

        void ptr_set_int8_t(Pointer p, int offset, byte value);
        void ptr_set_int16_t(Pointer p, int offset, short value);
        void ptr_set_int32_t(Pointer p, int offset, int value);
        void ptr_set_int64_t(Pointer p, int offset, long value);
        void ptr_set_float(Pointer p, int offset, float value);
        void ptr_set_double(Pointer p, int offset, double value);
    }

    static TestLib testlib;
    static Runtime runtime;
    
    public MemoryIOTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Library.getRuntime(testlib);
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
    private final MemoryIO direct(int size) {
        return MemoryIO.allocateDirect(runtime, size);
    }

    private final MemoryIO heap(int size) {
        return MemoryIO.allocate(runtime, size);
    }

    private final MemoryIO buffer(int size) {
        return wrap(ByteBuffer.allocate(size).order(runtime.byteOrder()));
    }

    private static final MemoryIO wrap(ByteBuffer buffer) {
        return MemoryIO.wrap(runtime, buffer);
    }
    private static final MemoryIO allocateDirect(int size) {
        return MemoryIO.allocateDirect(runtime, size);
    }
    private final void testPutByte(MemoryIO io, int size) {
        for (int i = 0; i < size; ++i) {
            io.putByte(i, (byte) (i + 5));
            assertEquals("Incorrect value at offset " + i, (byte) (i + 5), testlib.ptr_ret_int8_t(io, i));
        }
    }
    private final void testGetByte(MemoryIO io, int size) {
        for (int i = 0; i < size; ++i) {
            testlib.ptr_set_int8_t(io, i, (byte) (i + 5));
            assertEquals("Incorrect value at offset " + i, (byte) (i + 5), io.getByte(i));
        }
    }
    private final void testPutShort(MemoryIO io, int size) {
        for (int i = 0; i <= size - 2; ++i) {
            io.putShort(i, (short) i);
            assertEquals("Incorrect value at offset " + i, (short) i, testlib.ptr_ret_int16_t(io, i));
        }
    }
    private final void testGetShort(MemoryIO io, int size) {
        for (int i = 0; i <= size - 2; ++i) {
            testlib.ptr_set_int16_t(io, i, (short) i);
            assertEquals("Incorrect value at offset " + i, (short) i, io.getShort(i));
        }
    }
    private final void testPutInt(MemoryIO io, int size) {
        for (int i = 0; i <= size - 4; ++i) {
            io.putInt(i, i);
            assertEquals("Incorrect value at offset " + i, i, testlib.ptr_ret_int32_t(io, i));
        }
    }
    private final void testGetInt(MemoryIO io, int size) {
        for (int i = 0; i <= size - 4; ++i) {
            testlib.ptr_set_int32_t(io, i, i);
            assertEquals("Incorrect value at offset " + i, i, io.getInt(i));
        }
    }
    private final void testPutLong(MemoryIO io, int size) {
        for (int i = 0; i <= size - 8; ++i) {
            io.putLong(i, i);
            assertEquals("Incorrect value at offset " + i, (long) i, testlib.ptr_ret_int64_t(io, i));
        }
    }
    private final void testGetLong(MemoryIO io, int size) {
        for (int i = 0; i <= size - 8; ++i) {
            testlib.ptr_set_int64_t(io, i, i);
            assertEquals("Incorrect value at offset " + i, (long) i, io.getLong(i));
        }
    }
    private final void testPutFloat(MemoryIO io, int size) {
        for (int i = 0; i <= size - (Float.SIZE / 8); ++i) {
            io.putFloat(i, i);
            assertEquals("Incorrect value at offset " + i, (float) i, testlib.ptr_ret_float(io, i), 0.00001);
        }
    }
    private final void testGetFloat(MemoryIO io, int size) {
        for (int i = 0; i <= size - (Float.SIZE / 8); ++i) {
            testlib.ptr_set_float(io, i, (float) i);
            assertEquals("Incorrect value at offset " + i, (float) i, io.getFloat(i), 0.00001);
        }
    }
    private final void testPutDouble(MemoryIO io, int size) {
        for (int i = 0; i <= size - (Double.SIZE / 8); ++i) {
            io.putDouble(i, (double) i);
            assertEquals("Incorrect value at offset " + i, (double) i, testlib.ptr_ret_double(io, i), 0d);
        }
    }
    private final void testGetDouble(MemoryIO io, int size) {
        for (int i = 0; i <= size - (Double.SIZE / 8); ++i) {
            testlib.ptr_set_double(io, i, (double) i);
            assertEquals("Incorrect value at offset " + i, (double) i, io.getDouble(i), 0d);
        }
    }

    

    @Test public void testHeapMemoryIOPutByte() {
        final int SIZE = 16;
        testPutByte(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOGetByte() {
        final int SIZE = 16;
        testGetByte(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOPutShort() {
        final int SIZE = 16;
        testPutShort(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOGetShort() {
        final int SIZE = 16;
        testGetShort(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOPutInt() {
        final int SIZE = 16;
        testPutInt(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOGetInt() {
        final int SIZE = 16;
        testGetInt(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOPutLong() {
        final int SIZE = 16;
        MemoryIO memory = heap(SIZE);
        testPutLong(memory, SIZE);
    }

    @Test public void testHeapMemoryIOGetLong() {
        final int SIZE = 16;
        testGetLong(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOPutFloat() {
        final int SIZE = 16;
        MemoryIO memory = heap(SIZE);
        testPutFloat(memory, SIZE);

    }

    @Test public void testHeapMemoryIOGetFloat() {
        final int SIZE = 16;
        testGetFloat(heap(SIZE), SIZE);
    }

    @Test public void testHeapMemoryIOPutDouble() {
        final int SIZE = 16;
        MemoryIO memory = heap(SIZE);
        testPutDouble(memory, SIZE);
    }

    @Test public void testHeapMemoryIOGetDouble() {
        final int SIZE = 16;
        testGetDouble(heap(SIZE), SIZE);
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
    

    @Test public void testDirectMemoryIOPutByte() {
        final int SIZE = 16;
        testPutByte(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOGetByte() {
        final int SIZE = 16;
        testGetByte(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOPutShort() {
        final int SIZE = 16;
        testPutShort(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOGetShort() {
        final int SIZE = 16;
        testGetShort(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOPutInt() {
        final int SIZE = 16;
        testPutInt(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOGetInt() {
        final int SIZE = 16;
        testGetInt(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOPutLong() {
        final int SIZE = 16;
        testPutLong(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOGetLong() {
        final int SIZE = 16;
        testGetLong(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOPutFloat() {
        final int SIZE = 16;
        testPutFloat(direct(SIZE), SIZE);

    }
    @Test public void testDirectMemoryIOGetFloat() {
        final int SIZE = 16;
        testGetFloat(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOPutDouble() {
        final int SIZE = 16;
        testPutDouble(direct(SIZE), SIZE);
    }
    @Test public void testDirectMemoryIOGetDouble() {
        final int SIZE = 16;
        testGetDouble(direct(SIZE), SIZE);
    }

    
    @Test public void testBufferIOPutByte() {
        final int SIZE = 16;
        testPutByte(buffer(SIZE), SIZE);
    }

    @Test public void testBufferIOGetByte() {
        final int SIZE = 16;
        testGetByte(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOPutShort() {
        final int SIZE = 16;
        testPutShort(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOGetShort() {
        final int SIZE = 16;
        testGetShort(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOPutInt() {
        final int SIZE = 16;
        testPutInt(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOGetInt() {
        final int SIZE = 16;
        testGetInt(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOPutLong() {
        final int SIZE = 16;
        testPutLong(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOGetLong() {
        final int SIZE = 16;
        testGetLong(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOPutFloat() {
        final int SIZE = 16;
        testPutFloat(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOGetFloat() {
        final int SIZE = 16;
        testGetFloat(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOPutDouble() {
        final int SIZE = 16;
        testPutDouble(buffer(SIZE), SIZE);
    }
    @Test public void testBufferIOGetDouble() {
        final int SIZE = 16;
        testGetDouble(buffer(SIZE), SIZE);
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
        MemoryIO dst = MemoryIO.wrap(runtime, buf);
        MemoryIO src = MemoryIO.allocateDirect(runtime, 1024);
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
        MemoryIO dst = MemoryIO.allocateDirect(runtime, 1024);
        MemoryIO src = MemoryIO.allocateDirect(runtime, 1024);
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