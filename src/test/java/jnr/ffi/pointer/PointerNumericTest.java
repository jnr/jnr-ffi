package jnr.ffi.pointer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import jnr.ffi.Memory;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.TstUtil;
import jnr.ffi.annotations.In;

import static jnr.ffi.NativeType.ADDRESS;
import static jnr.ffi.NativeType.DOUBLE;
import static jnr.ffi.NativeType.FLOAT;
import static jnr.ffi.NativeType.SCHAR;
import static jnr.ffi.NativeType.SINT;
import static jnr.ffi.NativeType.SLONG;
import static jnr.ffi.NativeType.SLONGLONG;
import static jnr.ffi.NativeType.SSHORT;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests PointerNumericTest.c
 * This is intended to test pointers that point to native numeric types
 */
public class PointerNumericTest {

    public enum Enum {e0, e1, e2, e3}

    public static interface TestLib {
        public byte ptr_num_ret_int8_t(Pointer p, int offset);
        public void ptr_num_set_int8_t(Pointer p, int offset, byte value);

        public short ptr_num_ret_int16_t(Pointer p, int offset);
        public void ptr_num_set_int16_t(Pointer p, int offset, short value);

        public int ptr_num_ret_int32_t(Pointer p, int offset);
        public void ptr_num_set_int32_t(Pointer p, int offset, int value);

        public long ptr_num_ret_long(Pointer p, int offset);
        public void ptr_num_set_long(Pointer p, int offset, long value);

        public long ptr_num_ret_int64_t(Pointer p, int offset);
        public void ptr_num_set_int64_t(Pointer p, int offset, long value);

        public float ptr_num_ret_float(Pointer p, int offset);
        public void ptr_num_set_float(Pointer p, int offset, float value);

        public double ptr_num_ret_double(Pointer p, int offset);
        public void ptr_num_set_double(Pointer p, int offset, double value);

        public boolean ptr_num_ret_boolean(Pointer p, int offset);
        public void ptr_num_set_boolean(Pointer p, int offset, boolean value);

        public Enum ptr_num_ret_Enum(Pointer p, int offset);
        public void ptr_num_set_Enum(Pointer p, int offset, Enum value);

        public Pointer ptr_num_ret_pointer(Pointer p, int offset);
        public void ptr_num_set_pointer(Pointer p, int offset, Pointer value);

        public Pointer ptr_num_arr_get(Pointer array, int index);
        public Pointer ptr_num_arr_get(@In Pointer[] array, int index);
        // TODO: 09-Jan-2022 @basshelal: Without this @In annotation we get a SIGSEGV, look into why this is and
        //  document it!

        public void ptr_num_arr_set(Pointer array, int index, Pointer value);
        public void ptr_num_arr_set(@In Pointer[] array, int index, Pointer value);
        // TODO: 09-Jan-2022 @basshelal: Without this @In annotation the tests fail because values change, also look
        //  into this and document it!
    }

    /** Number of values to put into a pointer in each test */
    private static final int COUNT = 100_000;

    /** Size of array of pointers for array tests */
    private static final int ARRAY_SIZE = 10_000;

    private static final Random R = new Random();

    private static TestLib testlib;
    private static Runtime runtime;

    @BeforeAll
    public static void beforeAll() {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }

    private static Pointer malloc(int bytes) {
        return Memory.allocateDirect(runtime, bytes);
    }

    private static int sizeOf(NativeType type) {
        return runtime.findType(type).size();
    }

    /*
     * The testing strategy we use here is essentially as follows:
     * * Allocate a pointer to fit COUNT times the type we are testing, ie 100_000 ints
     * * Create a control array that will have COUNT number of the type we are testing ie int[]
     * * Loop over all the bytes we just allocated
     * * Put a new random value of the type we are testing into the pointer and into the control array
     * * Ensure that at that index, the value we just put is found in the pointer using the native method and the JNR
     *   Pointer methods like Pointer.getInt()
     * * After the loop, use the JNR bulk get methods to check that the returned array matches tha control array,
     *   meaning all ints in the pointer are the same as the ints in the control array
     */

    // ========================= Byte ===============================

    @Test
    public void testPointerGetByte() {
        final int sizeInBytes = sizeOf(SCHAR);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        byte[] values = new byte[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            byte value = (byte) R.nextInt();
            p.putByte(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int8_t(p, i),
                    "Incorrect byte value at offset " + i);
            assertEquals(p.getByte(i), testlib.ptr_num_ret_int8_t(p, i),
                    "Incorrect byte value at offset " + i);
        }
        byte[] dest = new byte[COUNT];
        p.get(0, dest, 0, COUNT);

        assertArrayEquals(values, dest,
                "Incorrect bytes returned from bulk get method");
    }

    @Test
    public void testPointerSetByte() {
        final int sizeInBytes = sizeOf(SCHAR);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        byte[] values = new byte[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            byte value = (byte) R.nextInt();
            testlib.ptr_num_set_int8_t(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int8_t(p, i),
                    "Incorrect byte value at offset " + i);
            assertEquals(p.getByte(i), testlib.ptr_num_ret_int8_t(p, i),
                    "Incorrect byte value at offset " + i);
        }
        byte[] dest = new byte[COUNT];
        p.get(0, dest, 0, COUNT);

        assertArrayEquals(values, dest,
                "Incorrect bytes returned from bulk get method");
    }

    // ========================= Short ==============================

    @Test
    public void testPointerGetShort() {
        final int sizeInBytes = sizeOf(SSHORT);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        short[] values = new short[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            short value = (short) R.nextInt();
            p.putShort(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int16_t(p, i),
                    "Incorrect short value at offset " + i);
            assertEquals(p.getShort(i), testlib.ptr_num_ret_int16_t(p, i),
                    "Incorrect short value at offset " + i);
        }
        short[] dest = new short[COUNT];
        p.get(0, dest, 0, COUNT);

        assertArrayEquals(values, dest,
                "Incorrect shorts returned from bulk get method");
    }

    @Test
    public void testPointerSetShort() {
        final int sizeInBytes = sizeOf(SSHORT);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        short[] values = new short[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            short value = (short) R.nextInt();
            testlib.ptr_num_set_int16_t(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int16_t(p, i),
                    "Incorrect short value at offset " + i);
            assertEquals(p.getShort(i), testlib.ptr_num_ret_int16_t(p, i),
                    "Incorrect short value at offset " + i);
        }
        short[] dest = new short[COUNT];
        p.get(0, dest, 0, COUNT);

        assertArrayEquals(values, dest,
                "Incorrect shorts returned from bulk get method");
    }

    // ========================= Int ================================

    @Test
    public void testPointerGetInt() {
        final int sizeInBytes = sizeOf(SINT);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        int[] values = new int[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            int value = R.nextInt();
            p.putInt(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int32_t(p, i),
                    "Incorrect int value at offset " + i);
            assertEquals(p.getInt(i), testlib.ptr_num_ret_int32_t(p, i),
                    "Incorrect int value at offset " + i);
        }
        int[] dest = new int[COUNT];
        p.get(0, dest, 0, COUNT);

        assertArrayEquals(values, dest,
                "Incorrect ints returned from bulk get method");
    }

    @Test
    public void testPointerSetInt() {
        final int sizeInBytes = sizeOf(SINT);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        int[] values = new int[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            int value = R.nextInt();
            testlib.ptr_num_set_int32_t(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int32_t(p, i),
                    "Incorrect int value at offset " + i);
            assertEquals(p.getInt(i), testlib.ptr_num_ret_int32_t(p, i),
                    "Incorrect int value at offset " + i);
        }
        int[] dest = new int[COUNT];
        p.get(0, dest, 0, COUNT);

        assertArrayEquals(values, dest,
                "Incorrect ints returned from bulk get method");
    }

    // ========================= NativeLong =========================

    @Test
    public void testPointerGetNativeLong() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SLONG);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        long[] values = new long[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            long value = R.nextLong();
            p.putNativeLong(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_long(p, i),
                    "Incorrect native long value at offset " + i);
            assertEquals(p.getNativeLong(i), testlib.ptr_num_ret_long(p, i),
                    "Incorrect native long value at offset " + i);
        }
        long[] dest = new long[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect native longs returned from bulk get method");
    }

    @Test
    public void testPointerSetNativeLong() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SLONG);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        long[] values = new long[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            long value = R.nextLong();
            testlib.ptr_num_set_long(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_long(p, i),
                    "Incorrect native long value at offset " + i);
            assertEquals(p.getNativeLong(i), testlib.ptr_num_ret_long(p, i),
                    "Incorrect native long value at offset " + i);
        }
        long[] dest = new long[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect native longs returned from bulk get method");
    }

    // ========================= Long ===============================

    @Test
    public void testPointerGetLong() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SLONGLONG);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        long[] values = new long[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            long value = R.nextLong();
            p.putLongLong(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int64_t(p, i),
                    "Incorrect long value at offset " + i);
            assertEquals(p.getLongLong(i), testlib.ptr_num_ret_int64_t(p, i),
                    "Incorrect long value at offset " + i);
        }
        long[] dest = new long[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect longs returned from bulk get method");
    }

    @Test
    public void testPointerSetLong() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SLONGLONG);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        long[] values = new long[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            long value = R.nextLong();
            testlib.ptr_num_set_int64_t(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_int64_t(p, i),
                    "Incorrect long value at offset " + i);
            assertEquals(p.getLongLong(i), testlib.ptr_num_ret_int64_t(p, i),
                    "Incorrect long value at offset " + i);
        }
        long[] dest = new long[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect longs returned from bulk get method");
    }

    // ========================= Float ==============================

    @Test
    public void testPointerGetFloat() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(FLOAT);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        float[] values = new float[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            float value = R.nextFloat();
            p.putFloat(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_float(p, i),
                    "Incorrect float value at offset " + i);
            assertEquals(p.getFloat(i), testlib.ptr_num_ret_float(p, i),
                    "Incorrect float value at offset " + i);
        }
        float[] dest = new float[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect floats returned from bulk get method");
    }

    @Test
    public void testPointerSetFloat() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(FLOAT);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        float[] values = new float[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            float value = R.nextFloat();
            testlib.ptr_num_set_float(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_float(p, i),
                    "Incorrect float value at offset " + i);
            assertEquals(p.getFloat(i), testlib.ptr_num_ret_float(p, i),
                    "Incorrect float value at offset " + i);
        }
        float[] dest = new float[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect floats returned from bulk get method");
    }

    // ========================= Double =============================

    @Test
    public void testPointerGetDouble() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(DOUBLE);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        double[] values = new double[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            double value = R.nextDouble();
            p.putDouble(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_double(p, i),
                    "Incorrect double value at offset " + i);
            assertEquals(p.getDouble(i), testlib.ptr_num_ret_double(p, i),
                    "Incorrect double value at offset " + i);
        }
        double[] dest = new double[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect doubles returned from bulk get method");
    }

    @Test
    public void testPointerSetDouble() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(DOUBLE);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        double[] values = new double[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            double value = R.nextDouble();
            testlib.ptr_num_set_double(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_double(p, i),
                    "Incorrect double value at offset " + i);
            assertEquals(p.getDouble(i), testlib.ptr_num_ret_double(p, i),
                    "Incorrect double value at offset " + i);
        }
        double[] dest = new double[total];
        p.get(0, dest, 0, total);

        assertArrayEquals(values, dest,
                "Incorrect doubles returned from bulk get method");
    }

    // ========================= Boolean ============================

    @Test
    public void testPointerGetBoolean() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SCHAR);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        boolean[] values = new boolean[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            boolean value = R.nextBoolean();
            byte numericValue = (byte) (value ? 1 : 0);
            p.putByte(i, numericValue);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_boolean(p, i),
                    "Incorrect boolean value at offset " + i);
            assertEquals(p.getByte(i) != 0, testlib.ptr_num_ret_boolean(p, i),
                    "Incorrect boolean value at offset " + i);
        }
        byte[] dest = new byte[total];
        p.get(0, dest, 0, total);
        boolean[] boolDest = new boolean[total];
        for (int i = 0; i < dest.length; i++) {
            boolDest[i] = dest[i] != 0;
        }

        assertArrayEquals(values, boolDest,
                "Incorrect booleans returned from bulk get method");
    }

    @Test
    public void testPointerSetBoolean() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SCHAR);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        boolean[] values = new boolean[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            boolean value = R.nextBoolean();
            testlib.ptr_num_set_boolean(p, i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = value;

            assertEquals(value, testlib.ptr_num_ret_boolean(p, i),
                    "Incorrect boolean value at offset " + i);
            assertEquals(p.getByte(i) != 0, testlib.ptr_num_ret_boolean(p, i),
                    "Incorrect boolean value at offset " + i);
        }
        byte[] dest = new byte[total];
        p.get(0, dest, 0, total);
        boolean[] boolDest = new boolean[total];
        for (int i = 0; i < dest.length; i++) {
            boolDest[i] = dest[i] != 0;
        }

        assertArrayEquals(values, boolDest,
                "Incorrect booleans returned from bulk get method");
    }

    // ========================= Enum ===============================

    @Test
    public void testPointerGetEnum() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SINT);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        Enum[] values = new Enum[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            int value = R.nextInt(Enum.values().length);
            Enum enumValue = Enum.values()[value];
            p.putInt(i, value);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = enumValue;

            assertEquals(enumValue, testlib.ptr_num_ret_Enum(p, i),
                    "Incorrect enum value at offset " + i);
            assertEquals(Enum.values()[p.getInt(i)], testlib.ptr_num_ret_Enum(p, i),
                    "Incorrect enum value at offset " + i);
        }
        int[] dest = new int[total];
        p.get(0, dest, 0, total);
        Enum[] enumDest = new Enum[total];
        for (int i = 0; i < dest.length; i++) {
            enumDest[i] = Enum.values()[dest[i]];
        }

        assertArrayEquals(values, enumDest,
                "Incorrect enums returned from bulk get method");
    }

    @Test
    public void testPointerSetEnum() {
        final int total = COUNT;
        final int sizeInBytes = sizeOf(SINT);
        final int totalBytes = total * sizeInBytes;

        Pointer p = malloc(totalBytes);
        Enum[] values = new Enum[total];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            int value = R.nextInt(Enum.values().length);
            Enum enumValue = Enum.values()[value];
            testlib.ptr_num_set_Enum(p, i, enumValue);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = enumValue;

            assertEquals(enumValue, testlib.ptr_num_ret_Enum(p, i),
                    "Incorrect enum value at offset " + i);
            assertEquals(Enum.values()[p.getInt(i)], testlib.ptr_num_ret_Enum(p, i),
                    "Incorrect enum value at offset " + i);
        }
        int[] dest = new int[total];
        p.get(0, dest, 0, total);
        Enum[] enumDest = new Enum[total];
        for (int i = 0; i < dest.length; i++) {
            enumDest[i] = Enum.values()[dest[i]];
        }

        assertArrayEquals(values, enumDest,
                "Incorrect enums returned from bulk get method");
    }

    // ========================= Pointer ============================

    @Test
    public void testPointerGetPointer() {
        final int sizeInBytes = sizeOf(ADDRESS);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        Pointer[] values = new Pointer[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            long value = R.nextLong();
            Pointer pointerValue = Pointer.newIntPointer(runtime, value);
            p.putPointer(i, pointerValue);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = pointerValue;

            assertEquals(pointerValue, testlib.ptr_num_ret_pointer(p, i),
                    "Incorrect pointer value at offset " + i);
            assertEquals(p.getPointer(i), testlib.ptr_num_ret_pointer(p, i),
                    "Incorrect pointer value at offset " + i);
        }
        long[] dest = new long[COUNT];
        p.get(0, dest, 0, COUNT);
        Pointer[] pointerDest = new Pointer[COUNT];

        for (int i = 0; i < dest.length; i++) {
            pointerDest[i] = Pointer.newIntPointer(runtime, dest[i]);
        }

        assertArrayEquals(values, pointerDest,
                "Incorrect pointers returned from bulk get method");
    }

    @Test
    public void testPointerSetPointer() {
        final int sizeInBytes = sizeOf(ADDRESS);
        final int totalBytes = COUNT * sizeInBytes;

        Pointer p = malloc(totalBytes);
        Pointer[] values = new Pointer[COUNT];

        // skip by sizeInBytes because pointer offset uses byte offset
        for (int i = 0; i < totalBytes; i += sizeInBytes) {
            long value = R.nextLong();
            Pointer pointerValue = Pointer.newIntPointer(runtime, value);
            testlib.ptr_num_set_pointer(p, i, pointerValue);
            // divide by sizeInBytes to undo the sizeInBytes skipping in the loop
            values[i / sizeInBytes] = pointerValue;

            assertEquals(pointerValue, testlib.ptr_num_ret_pointer(p, i),
                    "Incorrect pointer value at offset " + i);
            assertEquals(p.getPointer(i), testlib.ptr_num_ret_pointer(p, i),
                    "Incorrect pointer value at offset " + i);
        }
        long[] dest = new long[COUNT];
        p.get(0, dest, 0, COUNT);
        Pointer[] pointerDest = new Pointer[COUNT];

        for (int i = 0; i < dest.length; i++) {
            pointerDest[i] = Pointer.newIntPointer(runtime, dest[i]);
        }

        assertArrayEquals(values, pointerDest,
                "Incorrect pointers returned from bulk get method");
    }

    // ========================= Pointer Array ======================

    /*
     * We have 2 mappings for a C void ** one using plain Pointer, (which we put Pointers into using
     * Pointer.putPointer()) and one using Pointer[].
     * In both cases, we need to make sure that we put the same pointers into the array, that means both the address
     * of the pointer and the value inside it.
     */

    @Test
    public void testPointerArrayGet() {
        final int intSize = sizeOf(SINT);
        final int pointerSize = runtime.addressSize();
        Pointer ptrArray = malloc(pointerSize * ARRAY_SIZE);
        Pointer[] pointers = new Pointer[ARRAY_SIZE];
        int[] values = new int[ARRAY_SIZE];

        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = malloc(intSize);
            int value = R.nextInt();

            ptr.putInt(0, value);
            ptrArray.putPointer((long) i * pointerSize, ptr);

            pointers[i] = ptr;
            values[i] = value;
        }
        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = testlib.ptr_num_arr_get(ptrArray, i);

            assertEquals(pointers[i], ptr,
                    "Incorrect pointer at index " + i);
            assertEquals(ptrArray.getPointer((long) i * pointerSize), ptr,
                    "Incorrect pointer at index " + i);
            assertEquals(values[i], ptr.getInt(0),
                    "Incorrect pointer value at index " + i);
        }
    }

    @Test
    public void testPointerArraySet() {
        final int intSize = sizeOf(SINT);
        final int pointerSize = runtime.addressSize();
        Pointer ptrArray = malloc(pointerSize * ARRAY_SIZE);
        Pointer[] pointers = new Pointer[ARRAY_SIZE];
        int[] values = new int[ARRAY_SIZE];

        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = malloc(intSize);
            int value = R.nextInt();

            ptr.putInt(0, value);
            testlib.ptr_num_arr_set(ptrArray, i, ptr);

            pointers[i] = ptr;
            values[i] = value;
        }
        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = ptrArray.getPointer((long) i * pointerSize);

            assertEquals(pointers[i], ptr,
                    "Incorrect pointer at index " + i);
            assertEquals(values[i], ptr.getInt(0),
                    "Incorrect pointer value at index " + i);
        }
    }

    @Test
    public void testPointerArrayGetArrayMapping() {
        final int intSize = sizeOf(SINT);
        Pointer[] pointers = new Pointer[ARRAY_SIZE];
        int[] values = new int[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = malloc(intSize);
            int value = R.nextInt();

            ptr.putInt(0, value);

            pointers[i] = ptr;
            values[i] = value;
        }
        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = testlib.ptr_num_arr_get(pointers, i);

            assertEquals(pointers[i], ptr,
                    "Incorrect pointer at index " + i);
            assertEquals(values[i], ptr.getInt(0),
                    "Incorrect pointer value at index " + i);
        }
    }

    @Test
    public void testPointerArraySetArrayMapping() {
        final int intSize = sizeOf(SINT);
        Pointer[] pointers = new Pointer[ARRAY_SIZE];
        int[] values = new int[ARRAY_SIZE];

        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = malloc(intSize);
            int value = R.nextInt();

            ptr.putInt(0, value);
            testlib.ptr_num_arr_set(pointers, i, ptr);

            pointers[i] = ptr;
            values[i] = value;
        }
        for (int i = 0; i < ARRAY_SIZE; i++) {
            Pointer ptr = pointers[i];
            assertEquals(values[i], ptr.getInt(0),
                    "Incorrect pointer value at index " + i);
        }
    }

}
