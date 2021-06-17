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

import jnr.ffi.annotations.In;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Pinned;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ArrayTest {
    public static interface TestLib {
//        Pointer ptr_return_array_element(Pointer[] array, int index);
//        void ptr_set_array_element(Pointer[] array, int index, Pointer value);
        byte ptr_ret_int8_t(byte[] p, int offset);
        short ptr_ret_int16_t(short[] p, int offset);
        int ptr_ret_int32_t(int[] p, int offset);
        @LongLong long ptr_ret_int64_t(@LongLong long[] p, int offset);
        long ptr_ret_int32_t(long[] p, int offset);
        float ptr_ret_float(float[] p, int offset);
//        Pointer ptr_ret_pointer(Pointer[] p, int offset);
        double ptr_ret_double(double[] p, int offset);
        void ptr_set_int8_t(byte[] p, int offset, byte value);
        void ptr_set_int16_t(short[] p, int offset, short value);
        void ptr_set_int32_t(int[] p, int offset, int value);
        void ptr_set_int64_t(@LongLong long[] p, int offset, @LongLong long value);
        void ptr_set_int32_t(long[] p, int offset, long value);
        void ptr_set_float(float[] p, int offset, float value);
        void ptr_set_double(double[] p, int offset, double value);
//        void ptr_set_pointer(Pointer[] p, int offset, Pointer value);
        Pointer ptr_malloc(int size);
        void ptr_free(Pointer ptr);
    }
    public static interface TestLibInOnly {
        byte ptr_ret_int8_t(@In byte[] p, int offset);
        short ptr_ret_int16_t(@In short[] p, int offset);
        int ptr_ret_int32_t(@In int[] p, int offset);
        @LongLong long ptr_ret_int64_t(@In @LongLong long[] p, int offset);
        long ptr_ret_int32_t(@In long[] p, int offset);
        float ptr_ret_float(@In float[] p, int offset);
        void ptr_set_int8_t(@In byte[] p, int offset, byte value);
        void ptr_set_int16_t(@In short[] p, int offset, short value);
        void ptr_set_int32_t(@In int[] p, int offset, int value);
        void ptr_set_int64_t(@In @LongLong long[] p, int offset, @LongLong long value);
        void ptr_set_int32_t(@In long[] p, int offset, long value);
        void ptr_set_float(@In float[] p, int offset, float value);
        void ptr_set_double(@In double[] p, int offset, double value);
//        void ptr_set_pointer(@In Pointer[] p, int offset, Pointer value);
    }
    public static interface TestLibOutOnly {
        byte ptr_ret_int8_t(@Out byte[] p, int offset);
        short ptr_ret_int16_t(@Out short[] p, int offset);
        int ptr_ret_int32_t(@Out int[] p, int offset);
        @LongLong long ptr_ret_int64_t(@Out @LongLong long[] p, int offset);
        long ptr_ret_int32_t(@Out long[] p, int offset);
        float ptr_ret_float(@Out float[] p, int offset);
        void ptr_set_int8_t(@Out byte[] p, int offset, byte value);
        void ptr_set_int16_t(@Out short[] p, int offset, short value);
        void ptr_set_int32_t(@Out int[] p, int offset, int value);
        void ptr_set_int64_t(@Out @LongLong long[] p, int offset, @LongLong long value);
        void ptr_set_int32_t(@Out long[] p, int offset, long value);
        void ptr_set_float(@Out float[] p, int offset, float value);
        void ptr_set_double(@Out double[] p, int offset, double value);
//        void ptr_set_pointer(@Out Pointer[] p, int offset, Pointer value);
    }

    public static interface TestLibPinned {
        byte ptr_ret_int8_t(@Pinned byte[] p, int offset);
        short ptr_ret_int16_t(@Pinned short[] p, int offset);
        int ptr_ret_int32_t(@Pinned int[] p, int offset);
        @LongLong long ptr_ret_int64_t(@Pinned @LongLong long[] p, int offset);
        long ptr_ret_int32_t(@Pinned long[] p, int offset);
        float ptr_ret_float(@Pinned float[] p, int offset);
        void ptr_set_int8_t(@Pinned byte[] p, int offset, byte value);
        void ptr_set_int16_t(@Pinned short[] p, int offset, short value);
        void ptr_set_int32_t(@Pinned int[] p, int offset, int value);
        void ptr_set_int64_t(@Pinned @LongLong long[] p, int offset, @LongLong long value);
        void ptr_set_int32_t(@Pinned long[] p, int offset, long value);
        void ptr_set_float(@Pinned float[] p, int offset, float value);
        void ptr_set_double(@Pinned double[] p, int offset, double value);
    }

    static TestLib testlib;
    public ArrayTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void byteByReference() {
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = { MAGIC };
        assertEquals(MAGIC, testlib.ptr_ret_int8_t(ref, 0), "byte reference not read correctly");
        final byte MAGIC2 = (byte) 0xca;
        testlib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], "byte reference not written correctly");
    }
    @Test
    public void shortByReference() {
        final short MAGIC = (short) 0xfee1;
        short[] ref = { MAGIC };
        assertEquals(MAGIC, testlib.ptr_ret_int16_t(ref, 0), "short reference not read correctly");
        final short MAGIC2 = (short) 0xcafe;
        testlib.ptr_set_int16_t(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], "short reference not written correctly");
    }
    @Test
    public void intByReference() {
        final int MAGIC = (int) 0xfee1dead;
        int[] ref = { MAGIC };
        assertEquals(MAGIC, testlib.ptr_ret_int32_t(ref, 0), "int reference not read correctly");
        final int MAGIC2 = (int) 0xcafebabe;
        testlib.ptr_set_int32_t(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], "int reference not written correctly");
    }

    @Test
    public void getLongByReference() {
        final long MAGIC = 0x1234fee1dead6789L;
        long[] ref = { MAGIC };
        assertEquals(MAGIC, testlib.ptr_ret_int64_t(ref, 0), "long reference not read correctly");
    }

    @Test
    public void setLongByReference() {

        final long MAGIC = 0xcafebabe12345678L;
        long[] ref = { 0L };
        testlib.ptr_set_int64_t(ref, 0, MAGIC);
        assertEquals(MAGIC, ref[0], "long reference not written correctly");
    }
    
    @Test
    public void setLong32ByReference() {
        if (Runtime.getRuntime(testlib).longSize() == 4) {
            
            final long MAGIC = 0x12345678L;
            long[] ref = {0L};
            testlib.ptr_set_int32_t(ref, 0, MAGIC);
            assertEquals(MAGIC, ref[0], "long reference not written correctly");
        }
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
        assertEquals(MAGIC, testlib.ptr_ret_float(ref, 0), 0.00001, "float reference not read correctly");
        final float MAGIC2 = (float) 0xcafebabe;
        testlib.ptr_set_float(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], 0.00001, "float reference not written correctly");
    }
    @Test
    public void doubleByReference() {
        final double MAGIC = 0x1234fee1dead6789L;
        double[] ref = { MAGIC };
        assertEquals(MAGIC, testlib.ptr_ret_double(ref, 0), 0.00001, "double reference not read correctly");
        final double MAGIC2 = (double) 0xcafebabe12345678L;
        testlib.ptr_set_double(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], 0d, "double reference not written correctly");
    }
    
    //@Test
    public void inOnlyByteByReference() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = { MAGIC };
        assertEquals(MAGIC, lib.ptr_ret_int8_t(ref, 0), "byte reference not read correctly");
        final byte MAGIC2 = (byte) 0xca;
        lib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals(MAGIC, ref[0], "byte array read from native memory when it should not be");
    }
    //@Test
    public void inOnlyByteArray() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = new byte[1024];
        ref[0] = MAGIC;
        assertEquals(MAGIC, lib.ptr_ret_int8_t(ref, 0), "byte array not read correctly");
        final byte MAGIC2 = (byte) 0xca;
        lib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals(MAGIC, ref[0], "byte array read from native memory when it should not be");
    }
    //@Test
    public void outOnlyByteByReference() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final byte MAGIC = (byte) 0xfe;
        byte[] ref = { MAGIC };
        
        final byte MAGIC2 = (byte) 0xca;
        lib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], "byte reference not copied from native memory");
    }

    @Test
    public void pinnedByteByReference() {
        TestLibPinned lib = TstUtil.loadTestLib(TestLibPinned.class);

        final byte MAGIC = (byte) 0xfe;
        byte[] ref = { MAGIC };

        final byte MAGIC2 = (byte) 0xca;
        lib.ptr_set_int8_t(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], "byte reference not copied from native memory");
    }

    @Test
    public void pinnedIntByReference() {
        TestLibPinned lib = TstUtil.loadTestLib(TestLibPinned.class);

        // verify multi-byte values roundtrip through as expected.

        final int MAGIC = 0xabcd1234;
        int[] ref = { MAGIC, MAGIC * 2};

        assertEquals(MAGIC, lib.ptr_ret_int32_t(ref, 0));
        assertEquals(MAGIC * 2, lib.ptr_ret_int32_t(ref, 4));

        final int MAGIC2 = 0xca;
        lib.ptr_set_int32_t(ref, 0, MAGIC2);
        assertEquals(MAGIC2, ref[0], "int reference not copied from native memory");
    }

    @Test
    public void pinnedByteReadingFaster() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        TestLibPinned libPinned = TstUtil.loadTestLib(TestLibPinned.class);

        String result = null;

        ToLongFunction<Runnable> measure = (fn) -> {
            final long deadline = System.nanoTime()
                // 100ms windows seem to give accurate results without being
                // unbearable.
                + (long) (0.1 * 1e9);

            long iters = 0;

            // sort out about how many iterations we can do in our time and
            // then execute that many (so that we're not timing our clock
            // sampling).
            while (System.nanoTime() < deadline) {
                fn.run();

                iters++;
            }

            long t0 = System.nanoTime();

            for (int i = 0; i < iters; i++) {
                fn.run();
            }

            return
                (long) ((iters / (double) (System.nanoTime() - t0)) * 1e9);
        };

        // trying buffers sized from 4 KiB to 16 MiB
        // the costs of copying increase substantially and rapidly as buffer
        // size grows (especially with a cheap foreign function); it's
        // detectable even at small buffer sizes but larger ones give a more
        // convincing view.
        for (int size = 1 << 12; size < 1 << 25; size <<= 1) {
            byte[] ref = new byte[size];

            long copyingsOpsPerSec
                = measure.applyAsLong(() -> lib.ptr_ret_int8_t(ref, 0));

            long pinnedOpsPerSec
                = measure.applyAsLong(() -> libPinned.ptr_ret_int8_t(ref, 0));

            double diff
                = (copyingsOpsPerSec - pinnedOpsPerSec)
                    / (double) pinnedOpsPerSec * 100.0;

            result = String.format(
                "bytes=% 8d diff=%.2f (ops/s copying=%d pinning=%d)",
                size,
                diff,
                copyingsOpsPerSec,
                pinnedOpsPerSec);

            //System.out.println(result);

            // looking for a 90% decrease (as the buffers grow this number
            // grows ever closer to 100%).
            if (diff < -90) {
                return;
            }
        }

        // XXX possible that not all VMs in all configurations will support
        // pinning and will copy on GetPrimitiveArrayCritical. In this case
        // this test will fail.
        // OpenJ9 0.24 and OpenJDK 11 both appear to generally support pinning
        // in testing, and cursory Googling seems to indicate Dalvik does as
        // well.

        fail(
            "unable to find a buffer size at which array pinning becomes "
            + "significantly faster. pinning non-functional? "
            + "last test gave: " + result);
    }
}
