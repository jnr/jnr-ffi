/*
 * Copyright (C) 2007-2010 Wayne Meissner
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

package jnr.ffi;

import jnr.ffi.annotations.*;
import jnr.ffi.types.int32_t;
import jnr.ffi.types.int8_t;
import jnr.ffi.types.size_t;
import org.junit.*;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PointerTest {

    public PointerTest() {
    }
    public static interface TestLib {
        Pointer ptr_return_array_element(@In Pointer[] array, int index);
        void ptr_set_array_element(@Out Pointer[] array, int index, Pointer value);
        byte ptr_ret_int8_t(Pointer p, int offset);
        byte ptr_ret_int8_t(Address p, int offset);
        short ptr_ret_int16_t(Pointer p, int offset);
        int ptr_ret_int32_t(Pointer p, int offset);
        @LongLong long ptr_ret_int64_t(Pointer p, int offset);
        float ptr_ret_float(Pointer p, int offset);
        double ptr_ret_double(Pointer p, int offset);
        void ptr_set_int8_t(Pointer p, int offset, @int8_t byte value);
        void ptr_set_int16_t(Pointer p, int offset, short value);
        void ptr_set_int32_t(Pointer p, int offset, @int32_t int value);
        void ptr_set_int32_t(Pointer p, int offset, @int32_t long value);
        void ptr_set_int64_t(Pointer p, int offset, @LongLong long value);
        void ptr_set_float(Pointer p, int offset, float value);
        void ptr_set_double(Pointer p, int offset, double value);
        void ptr_reverse_l5(Pointer p1, Pointer p2, Pointer p3, Pointer p4, Pointer p5);
        void ptr_reverse_l6(Pointer p1, Pointer p2, Pointer p3, Pointer p4, Pointer p5, Pointer p6);

        public static final class Foo extends Struct {
            public final UnsignedLong l1 = new UnsignedLong();
            public final UnsignedLong l2 = new UnsignedLong();
            public final UnsignedLong l3 = new UnsignedLong();

            public Foo(Runtime runtime) {
                super(runtime);
            }
        }
        int fill_struct_from_longs(@size_t long l1, @size_t long l2, @Out Foo[] foo, @size_t long l3);

        Pointer ptr_malloc(@size_t int size);
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
        runtime = Runtime.getRuntime(testlib);
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
    public void testPointerSetLongLong() {
        
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
            p.putLongLong(i, MAGIC);
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
    public void testPointerGetLongLong() {
        
        Pointer p = testlib.ptr_malloc(SIZE);
        long MAGIC = 0xFEE1DEADABCDEF12L;
        for (int i = 0; i < SIZE - 7; ++i) {
            testlib.ptr_set_int64_t(p, i, MAGIC);
            assertEquals("Long not set at offset " + i, MAGIC, p.getLongLong(i));
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

    @Test
    public void testP5() {
        Pointer[] p = new Pointer[5];
        long[] v = { 1, 2, 3, 4, 5 };
        for (int i  = 0; i < p.length; ++i) {
            if ((i % 2) == 0) {
                p[i] = Memory.allocate(Runtime.getRuntime(testlib), 8);
            } else {
                p[i] = Memory.allocateDirect(Runtime.getRuntime(testlib), 8);
            }
            p[i].putLongLong(0, v[i]);
        }
        testlib.ptr_reverse_l5(p[0], p[1], p[2], p[3], p[4]);
        for (int i  = 0; i < p.length; ++i) {
            assertEquals("not same value for pointer " + (i + 1), v[v.length - i - 1], p[i].getLongLong(0));
        }
    }

    @Test
    public void testP6() {
        Pointer[] p = new Pointer[6];
        long[] v = { 1, 2, 3, 4, 5, 6};
        for (int i  = 0; i < p.length; ++i) {
            if ((i % 2) == 0) {
                p[i] = Memory.allocate(Runtime.getRuntime(testlib), 8);
            } else {
                p[i] = Memory.allocateDirect(Runtime.getRuntime(testlib), 8);
            }
            p[i].putLongLong(0, v[i]);
        }
        testlib.ptr_reverse_l6(p[0], p[1], p[2], p[3], p[4], p[5]);
        for (int i  = 0; i < p.length; ++i) {
            assertEquals("not same value for pointer " + (i + 1), v[v.length - i - 1], p[i].getLongLong(0));
        }
    }

    @Test public void nullTerminatedStringArray() {
        Runtime runtime = Runtime.getRuntime(testlib);
        Pointer[] array = new Pointer[10];
        String[] in = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = Memory.allocateDirect(runtime, 128);
            array[i].putString(0, in[i] = Integer.toString(i), 128, Charset.defaultCharset());
        }
        Pointer memory = Memory.allocateDirect(runtime, (2 * array.length + 1) * runtime.addressSize(), true);
        memory.put(array.length * runtime.addressSize(), array, 0, array.length);
        String[] out = memory.getNullTerminatedStringArray(array.length * runtime.addressSize());
        assertArrayEquals(in, out);
    }

    @Test public void nullTerminatedPointerArray() {
        Runtime runtime = Runtime.getRuntime(testlib);
        Pointer[] array = new Pointer[10];
        String[] in = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = Memory.allocateDirect(runtime, 128);
            array[i].putString(0, in[i] = Integer.toString(i), 128, Charset.defaultCharset());
        }
        Pointer memory = Memory.allocateDirect(runtime, (2 * array.length + 1) * runtime.addressSize(), true);
        memory.put(array.length * runtime.addressSize(), array, 0, array.length);
        Pointer[] out = memory.getNullTerminatedPointerArray(array.length * runtime.addressSize());
        assertArrayEquals(array, out);
    }

    @Test
    public void testAddressSetByte() {

        Pointer p = testlib.ptr_malloc(SIZE);
        byte MAGIC = (byte) 0xFE;
        for (int i = 0; i < SIZE; ++i) {
            p.putByte(i, MAGIC);
            assertEquals("Byte not set at offset " + i, MAGIC, testlib.ptr_ret_int8_t(Address.valueOf(p.address()), i));
        }
    }

    @Test
    public void pointerArrayGetElement() {
        Pointer[] ary = new Pointer[10];
        Pointer p1 = ary[0] = runtime.getMemoryManager().newPointer(0xdeadbeef & runtime.addressMask());
        Pointer p2 = ary[9] = runtime.getMemoryManager().newPointer(0xfee1dead & runtime.addressMask());
        assertEquals(p1, testlib.ptr_return_array_element(ary, 0));
        assertEquals(p2, testlib.ptr_return_array_element(ary, 9));
    }

    @Test
    public void pointerArraySetElement() {
        Pointer[] ary = new Pointer[10];
        Pointer p1 = runtime.getMemoryManager().newPointer(0xdeadbeef & runtime.addressMask());
        Pointer p2 = runtime.getMemoryManager().newPointer(0xfee1dead & runtime.addressMask());
        testlib.ptr_set_array_element(ary, 0, p1);
        assertEquals(p1, ary[0]);
        testlib.ptr_set_array_element(ary, 9, p2);
        assertEquals(p2, ary[9]);
    }

    @Test public void mixObjectsAndPrimitives() {
        TestLib.Foo[] structs = Struct.arrayOf(runtime, TestLib.Foo.class, 1);
        TestLib.Foo foo = structs[0];

        testlib.fill_struct_from_longs(0xdeadL, 0xbeefL, structs, 0x1eefcafe);
        assertEquals(0xdeadL, foo.l1.get());
        assertEquals(0xbeefL, foo.l2.get());
        assertEquals(0x1eefcafeL, foo.l3.get());
    }
//    @Test
//    public void testLibcMalloc() {
//        Pointer p = libc.malloc(SIZE);
//        libc.free(p);
//    }
}
