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

import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.Out;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class ArrayTest {
    private static interface TestLib {
        Pointer ptr_return_array_element(Pointer[] array, int index);
        void ptr_set_array_element(Pointer[] array, int index, Pointer value);
        byte ptr_ret_int8_t(byte[] p, int offset);
        short ptr_ret_int16_t(short[] p, int offset);
        int ptr_ret_int32_t(int[] p, int offset);
        long ptr_ret_int64_t(long[] p, int offset);
        float ptr_ret_float(float[] p, int offset);
        Pointer ptr_ret_pointer(Pointer[] p, int offset);
        double ptr_ret_double(double[] p, int offset);
        void ptr_set_int8_t(byte[] p, int offset, byte value);
        void ptr_set_int16_t(short[] p, int offset, short value);
        void ptr_set_int32_t(int[] p, int offset, int value);
        void ptr_set_int64_t(long[] p, int offset, long value);
        void ptr_set_float(float[] p, int offset, float value);
        void ptr_set_double(double[] p, int offset, double value);
        void ptr_set_pointer(Pointer[] p, int offset, Pointer value);
        Pointer ptr_malloc(int size);
        void ptr_free(Pointer ptr);
    }
    private static interface TestLibInOnly {
        byte ptr_ret_int8_t(@In byte[] p, int offset);
        short ptr_ret_int16_t(@In short[] p, int offset);
        int ptr_ret_int32_t(@In int[] p, int offset);
        long ptr_ret_int64_t(@In long[] p, int offset);
        float ptr_ret_float(@In float[] p, int offset);
        void ptr_set_int8_t(@In byte[] p, int offset, byte value);
        void ptr_set_int16_t(@In short[] p, int offset, short value);
        void ptr_set_int32_t(@In int[] p, int offset, int value);
        void ptr_set_int64_t(@In long[] p, int offset, long value);
        void ptr_set_float(@In float[] p, int offset, float value);
        void ptr_set_double(@In double[] p, int offset, double value);
        void ptr_set_pointer(@In Pointer[] p, int offset, Pointer value);
    }
    private static interface TestLibOutOnly {
        byte ptr_ret_int8_t(@Out byte[] p, int offset);
        short ptr_ret_int16_t(@Out short[] p, int offset);
        int ptr_ret_int32_t(@Out int[] p, int offset);
        long ptr_ret_int64_t(@Out long[] p, int offset);
        float ptr_ret_float(@Out float[] p, int offset);
        void ptr_set_int8_t(@Out byte[] p, int offset, byte value);
        void ptr_set_int16_t(@Out short[] p, int offset, short value);
        void ptr_set_int32_t(@Out int[] p, int offset, int value);
        void ptr_set_int64_t(@Out long[] p, int offset, long value);
        void ptr_set_float(@Out float[] p, int offset, float value);
        void ptr_set_double(@Out double[] p, int offset, double value);
        void ptr_set_pointer(@Out Pointer[] p, int offset, Pointer value);
    }
    static TestLib testlib;
    public ArrayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
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

    @Test
    public void byteByReference() {
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = { MAGIC };
        assertEquals("byte reference not read correctly", MAGIC, testlib.ptr_ret_int8_t(ref, 0));
        final byte MAGIC2 = (byte) 0xca;
        testlib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals("byte reference not written correctly", MAGIC2, ref[0]);
    }
    @Test
    public void shortByReference() {
        final short MAGIC = (short) 0xfee1;
        short[] ref = { MAGIC };
        assertEquals("short reference not read correctly", MAGIC, testlib.ptr_ret_int16_t(ref, 0));
        final short MAGIC2 = (short) 0xcafe;
        testlib.ptr_set_int16_t(ref, 0, MAGIC2);
        assertEquals("short reference not written correctly", MAGIC2, ref[0]);
    }
    @Test
    public void intByReference() {
        final int MAGIC = (int) 0xfee1dead;
        int[] ref = { MAGIC };
        assertEquals("int reference not read correctly", MAGIC, testlib.ptr_ret_int32_t(ref, 0));
        final int MAGIC2 = (int) 0xcafebabe;
        testlib.ptr_set_int32_t(ref, 0, MAGIC2);
        assertEquals("int reference not written correctly", MAGIC2, ref[0]);
    }
    @Test
    public void longByReference() {
        final long MAGIC = 0x1234fee1dead6789L;
        long[] ref = { MAGIC };
        assertEquals("long reference not read correctly", MAGIC, testlib.ptr_ret_int64_t(ref, 0));
        final long MAGIC2 = 0xcafebabe12345678L;
        testlib.ptr_set_int64_t(ref, 0, MAGIC2);
        assertEquals("long reference not written correctly", MAGIC2, ref[0]);
    }
//    @Test
//    public void pointerByReference() {
//        final Pointer MAGIC = new Pointer(0xfee1dead);
//        Pointer[] ref = { MAGIC };
//        assertEquals("Pointer reference not read correctly", MAGIC, testlib.ptr_ret_pointer(ref, 0));
//        final Pointer MAGIC2 = new Pointer(0xcafebabe);
//        testlib.ptr_set_pointer(ref, 0, MAGIC2);
//        assertEquals("Pointer reference not written correctly", MAGIC2, ref[0]);
//    }
    @Test
    public void floatByReference() {
        final float MAGIC = (float) 0xfee1dead;
        float[] ref = { MAGIC };
        assertEquals("float reference not read correctly", MAGIC, testlib.ptr_ret_float(ref, 0), 0.0f);
        final float MAGIC2 = (float) 0xcafebabe;
        testlib.ptr_set_float(ref, 0, MAGIC2);
        assertEquals("float reference not written correctly", MAGIC2, ref[0], 0f);
    }
    @Test
    public void doubleByReference() {
        final double MAGIC = 0x1234fee1dead6789L;
        double[] ref = { MAGIC };
        assertEquals("double reference not read correctly", MAGIC, testlib.ptr_ret_double(ref, 0), 0d);
        final double MAGIC2 = (double) 0xcafebabe12345678L;
        testlib.ptr_set_double(ref, 0, MAGIC2);
        assertEquals("double reference not written correctly", MAGIC2, ref[0], 0d);
    }
    
    //@Test
    public void inOnlyByteByReference() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = { MAGIC };
        assertEquals("byte reference not read correctly", MAGIC, lib.ptr_ret_int8_t(ref, 0));
        final byte MAGIC2 = (byte) 0xca;
        lib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals("byte array read from native memory when it should not be", MAGIC, ref[0]);
    }
    //@Test
    public void inOnlyByteArray() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = new byte[1024];
        ref[0] = MAGIC;
        assertEquals("byte array not read correctly", MAGIC, lib.ptr_ret_int8_t(ref, 0));
        final byte MAGIC2 = (byte) 0xca;
        lib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals("byte array read from native memory when it should not be", MAGIC, ref[0]);
    }
    //@Test
    public void outOnlyByteByReference() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = { MAGIC };
        
        final byte MAGIC2 = (byte) 0xca;
        lib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals("byte reference not copied from native memory", MAGIC2, ref[0]);
    }
}