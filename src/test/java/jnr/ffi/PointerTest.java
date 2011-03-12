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

package jnr.ffi;

import java.nio.ByteOrder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class PointerTest {

    public PointerTest() {
    }
    public static interface TestLib {
        Pointer ptr_return_array_element(Pointer[] array, int index);
        void ptr_set_array_element(Pointer[] array, int index, Pointer value);
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
        
        Pointer ptr_malloc(int size);
        void ptr_free(Pointer ptr);
    }
    static TestLib testlib;
    static Runtime runtime;
    public static interface Libc {
        Pointer calloc(int nmemb, int size);
        Pointer malloc(int size);
        void free(Pointer ptr);
        void cfree(Pointer ptr);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Library.getRuntime(testlib);
//        libc = Library.loadLibrary("c", Libc.class);
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
   
//    @Test
//    public void testGetPointerArrayArgument() throws Exception {
//
//        Pointer MAGIC0 = new Pointer(0xdeadbeef);
//        Pointer MAGIC1 = new Pointer(0xcafebabe);
//        Pointer[] array = { MAGIC0, MAGIC1 };
//
//        assertEquals("Incorrect Pointer at index 0", MAGIC0,
//                testlib.ptr_return_array_element(array, 0));
//        assertEquals("Incorrect Pointer at index 1", MAGIC1,
//                testlib.ptr_return_array_element(array, 1));
//    }
//    @Test
//    public void testSetPointerArrayArgument() throws Exception {
//
//        Pointer MAGIC0 = new Pointer(0xdeadbeef);
//        Pointer MAGIC1 = new Pointer(0xcafebabe);
//        Pointer[] array = { MAGIC0, MAGIC1 };
//
//        testlib.ptr_set_array_element(array, 0, MAGIC1);
//        testlib.ptr_set_array_element(array, 1, MAGIC0);
//        assertEquals("Pointer at index 0 not set", MAGIC1, array[0]);
//        assertEquals("Pointer at index 1 not set", MAGIC0, array[1]);
//    }
//
//    @Test
//    public void testLongPointerValue() throws Exception {
//        long MAGIC0 = 0xdeadbeefL | (Address.SIZE == 64 ? (0xfee1deadL << 32) : 0L);
//        assertEquals("Pointer value not equal", MAGIC0, new Pointer(MAGIC0).nativeAddress());
//    }
    static final int SIZE = 128;
    @Test
    public void testPointerSetByte() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        byte MAGIC = (byte) 0xFE;
        for (int i = 0; i < SIZE; ++i) {
            p.putByte(i, MAGIC);
            assertEquals("Byte not set at offset " + i, MAGIC, testlib.ptr_ret_int8_t(p, i));
        } 
    }
    @Test
    public void testPointerSetShort() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        short MAGIC = (short) 0xFEE1;
        for (int i = 0; i < (SIZE - 1); ++i) {
            p.putShort(i, MAGIC);
            assertEquals("Short not set at offset " + i, MAGIC, testlib.ptr_ret_int16_t(p, i));
        } 
    }
    @Test
    public void testPointerSetInt() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        int MAGIC = (int) 0xFEE1DEAD;
        for (int i = 0; i < (SIZE - 3); ++i) {
            p.putInt(i, MAGIC);
            assertEquals("Integer not set at offset " + i, MAGIC, testlib.ptr_ret_int32_t(p, i));
        } 
    }
    @Test
    public void testPointerSetLong() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        long MAGIC = 0xFEE1DEADABCDEF12L;
        final long l = MAGIC;
        byte[] bytes = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? new byte[]{
                    (byte) (l >>> 56), (byte) (l >>> 48), (byte) (l >>> 40),
                    (byte) (l >>> 32), (byte) (l >>> 24), (byte) (l >>> 16),
                    (byte) (l >>> 8), (byte) (l >>> 0)
                }
                : new byte[]{
                    (byte) (l >>> 0), (byte) (l >>> 8), (byte) (l >>> 16),
                    (byte) (l >>> 24), (byte) (l >>> 32), (byte) (l >>> 40),
                    (byte) (l >>> 48), (byte) (l >>> 56)
                };
        
        for (int i = 0; i < (SIZE - 7); ++i) {
            p.putLong(i, MAGIC);
            for (int idx = 0; idx < 8; ++idx) {
                assertEquals("incorrect byte value at offset= " + i + " idx=" + idx, bytes[idx], p.getByte(i + idx));
            }
            assertEquals("Long not set at offset " + i, MAGIC, testlib.ptr_ret_int64_t(p, i));
        } 
    }
    @Test
    public void testPointerSetFloat() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        float MAGIC = (float) 0xFEE1DEADABCDEF12L;
        for (int i = 0; i < (SIZE - 7); ++i) {
            p.putFloat(i, MAGIC);
            assertEquals("Float not set at offset " + i, MAGIC, testlib.ptr_ret_float(p, i), 0.00001);
        } 
    }
    @Test
    public void testPointerSetDouble() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        double MAGIC = (double) 0xFEE1DEADABCDEF12L;
        
        long l = Double.doubleToRawLongBits(MAGIC);
        byte[] bytes = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN) 
                ? new byte[]{
                    (byte) (l >>> 56), (byte) (l >>> 48), (byte) (l >>> 40),
                    (byte) (l >>> 32), (byte) (l >>> 24), (byte) (l >>> 16),
                    (byte) (l >>> 8), (byte) (l >>> 0)
                }
                : new byte[]{
                    (byte) (l >>> 0), (byte) (l >>> 8), (byte) (l >>> 16),
                    (byte) (l >>> 24), (byte) (l >>> 32), (byte) (l >>> 40),
                    (byte) (l >>> 48), (byte) (l >>> 56)
                };

        p.putDouble(0, MAGIC);
        for (int i = 0; i < 8; ++i) {
            assertEquals("incorrect byte value at idx=" + i, bytes[i], p.getByte(i));
        }
        for (int i = 0; i < (SIZE - 7); ++i) {
            p.putDouble(i, MAGIC);
            assertEquals("Double not set at offset " + i, MAGIC, testlib.ptr_ret_double(p, i), 0.0001E16);
        } 
    }
    @Test
    public void testPointerGetByte() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        byte MAGIC = (byte) 0xFE;
        for (int i = 0; i < SIZE; ++i) {
            testlib.ptr_set_int8_t(p, i, MAGIC);
            assertEquals("Byte not set at offset " + i, MAGIC, p.getByte(i));
        } 
    }
    @Test
    public void testPointerGetShort() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        short MAGIC = (short) 0xFEE1;
        for (int i = 0; i < SIZE - 1; ++i) {
            testlib.ptr_set_int16_t(p, i, MAGIC);
            assertEquals("Short not set at offset " + i, MAGIC, p.getShort(i));
        } 
    }
    @Test
    public void testPointerGetInt() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        int MAGIC = (int) 0xFEE1DEAD;
        for (int i = 0; i < SIZE - 3; ++i) {
            testlib.ptr_set_int32_t(p, i, MAGIC);
            assertEquals("Integer not set at offset " + i, MAGIC, p.getInt(i));
        } 
    }
    @Test
    public void testPointerGetLong() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        long MAGIC = 0xFEE1DEADABCDEF12L;
        for (int i = 0; i < SIZE - 7; ++i) {
            testlib.ptr_set_int64_t(p, i, MAGIC);
            assertEquals("Long not set at offset " + i, MAGIC, p.getLong(i));
        } 
    }
    @Test
    public void testPointerGetFloat() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        float MAGIC = (float) 0xFEE1DEADABCDEF12L;
        for (int i = 0; i < (SIZE - 7); ++i) {
            testlib.ptr_set_float(p, i, MAGIC);
            assertEquals("Float not set at offset " + i, MAGIC, p.getFloat(i), 0.0001);
        } 
    }
    @Test
    public void testPointerGetDouble() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        double MAGIC = (double) 0xFEE1DEADABCDEF12L;
        for (int i = 0; i < (SIZE - 7); ++i) {
            testlib.ptr_set_double(p, i, MAGIC);
            assertEquals("Double not set at offset " + i, MAGIC, p.getDouble(i), 0.00001);
        } 
    }
    @Test
    public void testMalloc() {
        Pointer[] pointers = new Pointer[1024];
        for (int i  = 0; i < pointers.length; ++i) {
            pointers[i] = testlib.ptr_malloc(SIZE);
        }
        for (int i  = 0; i < pointers.length; ++i) {
            testlib.ptr_free(pointers[i]);
        }      
    }
    
//    @Test
//    public void testLibcMalloc() {
//        Pointer p = libc.malloc(SIZE);
//        libc.free(p);
//    }
}