package jnr.ffi.struct;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import jnr.ffi.Memory;
import jnr.ffi.NativeLong;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.TstUtil;
import jnr.ffi.types.u_int16_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests NumericStruct.c
 * This tests a native struct that contains only every possible numeric type including
 * booleans, enums and pointers (C void *)
 */
public class NumericStructTest {

    private static final int ITERATIONS_COUNT = 100_000;
    private static final short MAX_UNSIGNED_BYTE = 0xff;
    private static final int MAX_UNSIGNED_SHORT = 0xffff;
    private static final long MAX_UNSIGNED_INT = 0xffff_ffffL;
    private static final long MAX_UNSIGNED_LONG = Long.MAX_VALUE; // Not true, but it's the most we can fit in a long

    // ThreadLocalRandom instead of Random because we want a nextLong() method with bounds which Random doesn't have
    private static final ThreadLocalRandom R = ThreadLocalRandom.current();

    private static Lib lib;
    private static Runtime runtime;

    public enum Enum {e0, e1, e2, e3}

    public static class FfiStrList extends Struct {
        //public final UTF8String count = new UTF8String(32); <-- works as per example
        public final Struct.Signed64 count = new Struct.Signed64(); //<-- not working
        //public final PointerByReference data; //TODO add in rest of structure
        //public final Struct.Pointer[] data; // Or this ???
        public FfiStrList(jnr.ffi.Runtime runtime /*, java.lang.String[] sAry*/) {
            super(runtime);
        }
    }

    public static class NumericStruct extends Struct {
        public final Struct.Signed8 val_int8_t = new Struct.Signed8();
        public final Struct.Signed16 val_int16_t = new Struct.Signed16();
        public final Struct.Signed32 val_int32_t = new Struct.Signed32();
        public final Struct.SignedLong val_long = new Struct.SignedLong();
        public final Struct.Signed64 val_int64_t = new Struct.Signed64();

        public final Struct.Unsigned8 val_uint8_t = new Struct.Unsigned8();
        public final Struct.Unsigned16 val_uint16_t = new Struct.Unsigned16();
        public final Struct.Unsigned32 val_uint32_t = new Struct.Unsigned32();
        public final Struct.UnsignedLong val_ulong = new Struct.UnsignedLong();
        public final Struct.Unsigned64 val_uint64_t = new Struct.Unsigned64();

        public final Struct.Float val_float = new Struct.Float();
        public final Struct.Double val_double = new Struct.Double();

        public final Struct.Boolean val_bool = new Struct.Boolean();
        public final Struct.Enum<NumericStructTest.Enum> val_Enum = new Struct.Enum<>(NumericStructTest.Enum.class);
        public final Struct.Pointer val_pointer = new Struct.Pointer();

        public NumericStruct(Runtime runtime) {super(runtime);}

        public NumericStruct() {this(runtime);}

        public void reset() {
            val_int8_t.set(0);
            val_int16_t.set(0);
            val_int32_t.set(0);
            val_long.set(0);
            val_int64_t.set(0);
            val_uint8_t.set(0);
            val_uint16_t.set(0);
            val_uint32_t.set(0);
            val_ulong.set(0);
            val_uint64_t.set(0);
            val_float.set(0);
            val_double.set(0);
            val_bool.set(false);
            val_Enum.set(0);
            val_pointer.set(0);
        }
    }

    public static interface Lib {

        public int struct_num_al_test(FfiStrList s);
        public byte struct_num_get_int8_t(NumericStruct s);
        public void struct_num_set_int8_t(NumericStruct s, byte v);

        public short struct_num_get_int16_t(NumericStruct s);
        public void struct_num_set_int16_t(NumericStruct s, short v);

        public int struct_num_get_int32_t(NumericStruct s);
        public void struct_num_set_int32_t(NumericStruct s, int v);

        public NativeLong struct_num_get_long(NumericStruct s);
        public void struct_num_set_long(NumericStruct s, NativeLong v);

        public long struct_num_get_int64_t(NumericStruct s);
        public void struct_num_set_int64_t(NumericStruct s, long v);

        public @u_int8_t
        short struct_num_get_uint8_t(NumericStruct s);
        public void struct_num_set_uint8_t(NumericStruct s, @u_int8_t short v);

        public @u_int16_t
        int struct_num_get_uint16_t(NumericStruct s);
        public void struct_num_set_uint16_t(NumericStruct s, @u_int16_t int v);

        public @u_int32_t
        long struct_num_get_uint32_t(NumericStruct s);
        public void struct_num_set_uint32_t(NumericStruct s, @u_int32_t long v);

        public NativeLong struct_num_get_ulong(NumericStruct s);
        public void struct_num_set_ulong(NumericStruct s, NativeLong v);

        public @u_int64_t
        long struct_num_get_uint64_t(NumericStruct s);
        public void struct_num_set_uint64_t(NumericStruct s, @u_int64_t long v);

        public float struct_num_get_float(NumericStruct s);
        public void struct_num_set_float(NumericStruct s, float v);

        public double struct_num_get_double(NumericStruct s);
        public void struct_num_set_double(NumericStruct s, double v);

        public boolean struct_num_get_bool(NumericStruct s);
        public void struct_num_set_bool(NumericStruct s, boolean v);

        public Enum struct_num_get_Enum(NumericStruct s);
        public void struct_num_set_Enum(NumericStruct s, Enum v);

        public Pointer struct_num_get_pointer(NumericStruct s);
        public void struct_num_set_pointer(NumericStruct s, Pointer v);

        public int struct_num_size();
    }

    @BeforeAll
    public static void beforeAll() {
        lib = TstUtil.loadTestLib(Lib.class);
        runtime = Runtime.getRuntime(lib);
    }

    @Test
    public void testAlNumeric() {
        FfiStrList s = new FfiStrList(runtime);
        s.count.set(34);

        int r = lib.struct_num_al_test(s);
        assertEquals(34, r);
    }

    // ========================= Byte ===============================

    @Test
    public void testGetByte() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int8_t.get(),
                    "Struct byte value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int8_t(struct),
                    "Struct byte value should be 0 after creation and reset");

            byte value = (byte) R.nextInt();
            struct.val_int8_t.set(value);

            assertEquals(value, struct.val_int8_t.get(),
                    "Incorrect struct byte value");
            assertEquals(value, lib.struct_num_get_int8_t(struct),
                    "Incorrect struct byte value");

            struct.reset();
        }
    }

    @Test
    public void testSetByte() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int8_t.get(),
                    "Struct byte value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int8_t(struct),
                    "Struct byte value should be 0 after creation and reset");

            byte value = (byte) R.nextInt();
            lib.struct_num_set_int8_t(struct, value);

            assertEquals(value, struct.val_int8_t.get(),
                    "Incorrect struct byte value");
            assertEquals(value, lib.struct_num_get_int8_t(struct),
                    "Incorrect struct byte value");

            struct.reset();
        }
    }

    // ========================= Short ==============================

    @Test
    public void testGetShort() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int16_t.get(),
                    "Struct short value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int16_t(struct),
                    "Struct short value should be 0 after creation and reset");

            short value = (short) R.nextInt();
            struct.val_int16_t.set(value);

            assertEquals(value, struct.val_int16_t.get(),
                    "Incorrect struct short value");
            assertEquals(value, lib.struct_num_get_int16_t(struct),
                    "Incorrect struct short value");

            struct.reset();
        }
    }

    @Test
    public void testSetShort() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int16_t.get(),
                    "Struct short value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int16_t(struct),
                    "Struct short value should be 0 after creation and reset");

            short value = (short) R.nextInt();
            lib.struct_num_set_int16_t(struct, value);

            assertEquals(value, struct.val_int16_t.get(),
                    "Incorrect struct short value");
            assertEquals(value, lib.struct_num_get_int16_t(struct),
                    "Incorrect struct short value");

            struct.reset();
        }
    }

    // ========================= Int ================================

    @Test
    public void testGetInt() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int32_t.get(),
                    "Struct int value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int32_t(struct),
                    "Struct int value should be 0 after creation and reset");

            int value = R.nextInt();
            struct.val_int32_t.set(value);

            assertEquals(value, struct.val_int32_t.get(),
                    "Incorrect struct int value");
            assertEquals(value, lib.struct_num_get_int32_t(struct),
                    "Incorrect struct int value");

            struct.reset();
        }
    }

    @Test
    public void testSetInt() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int32_t.get(),
                    "Struct int value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int32_t(struct),
                    "Struct int value should be 0 after creation and reset");

            int value = R.nextInt();
            lib.struct_num_set_int32_t(struct, value);

            assertEquals(value, struct.val_int32_t.get(),
                    "Incorrect struct int value");
            assertEquals(value, lib.struct_num_get_int32_t(struct),
                    "Incorrect struct int value");

            struct.reset();
        }
    }

    // ========================= NativeLong =========================

    @Test
    public void testGetNativeLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_long.get(),
                    "Struct native long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_long(struct).longValue(),
                    "Struct native long value should be 0 after creation and reset");

            long value = R.nextLong();
            struct.val_long.set(value);

            assertEquals(value, struct.val_long.get(),
                    "Incorrect struct native long value");
            assertEquals(value, lib.struct_num_get_long(struct).longValue(),
                    "Incorrect struct native long value");

            struct.reset();
        }
    }

    @Test
    public void testSetNativeLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_long.get(),
                    "Struct native long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_long(struct).longValue(),
                    "Struct native long value should be 0 after creation and reset");

            long value = R.nextLong();
            lib.struct_num_set_long(struct, NativeLong.valueOf(value));

            assertEquals(value, struct.val_long.get(),
                    "Incorrect struct native long value");
            assertEquals(value, lib.struct_num_get_long(struct).longValue(),
                    "Incorrect struct native long value");

            struct.reset();
        }
    }

    // ========================= Long ===============================

    @Test
    public void testGetLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int64_t.get(),
                    "Struct long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int64_t(struct),
                    "Struct long value should be 0 after creation and reset");

            long value = R.nextLong();
            struct.val_int64_t.set(value);

            assertEquals(value, struct.val_int64_t.get(),
                    "Incorrect struct long value");
            assertEquals(value, lib.struct_num_get_int64_t(struct),
                    "Incorrect struct long value");

            struct.reset();
        }
    }

    @Test
    public void testSetLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int64_t.get(),
                    "Struct long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_int64_t(struct),
                    "Struct long value should be 0 after creation and reset");

            long value = R.nextLong();
            lib.struct_num_set_int64_t(struct, value);

            assertEquals(value, struct.val_int64_t.get(),
                    "Incorrect struct long value");
            assertEquals(value, lib.struct_num_get_int64_t(struct),
                    "Incorrect struct long value");

            struct.reset();
        }
    }

    // ========================= Unsigned Byte ======================

    @Test
    public void testGetUnsignedByte() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint8_t.get(),
                    "Struct unsigned byte value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint8_t(struct),
                    "Struct unsigned byte value should be 0 after creation and reset");

            short value = (short) R.nextInt(MAX_UNSIGNED_BYTE);
            struct.val_uint8_t.set(value);

            assertEquals(value, struct.val_uint8_t.get(),
                    "Incorrect struct unsigned byte value");
            assertEquals(value, lib.struct_num_get_uint8_t(struct),
                    "Incorrect struct unsigned byte value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedByte() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint8_t.get(),
                    "Struct unsigned byte value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint8_t(struct),
                    "Struct unsigned byte value should be 0 after creation and reset");

            short value = (short) R.nextInt(MAX_UNSIGNED_BYTE);
            lib.struct_num_set_uint8_t(struct, value);

            assertEquals(value, struct.val_uint8_t.get(),
                    "Incorrect struct unsigned byte value");
            assertEquals(value, lib.struct_num_get_uint8_t(struct),
                    "Incorrect struct unsigned byte value");

            struct.reset();
        }
    }

    // ========================= Unsigned Short =====================

    @Test
    public void testGetUnsignedShort() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint16_t.get(),
                    "Struct unsigned short value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint16_t(struct),
                    "Struct unsigned short value should be 0 after creation and reset");

            int value = R.nextInt(MAX_UNSIGNED_SHORT);
            struct.val_uint16_t.set(value);

            assertEquals(value, struct.val_uint16_t.get(),
                    "Incorrect struct unsigned short value");
            assertEquals(value, lib.struct_num_get_uint16_t(struct),
                    "Incorrect struct unsigned short value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedShort() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint16_t.get(),
                    "Struct unsigned short value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint16_t(struct),
                    "Struct unsigned short value should be 0 after creation and reset");

            int value = R.nextInt(MAX_UNSIGNED_SHORT);
            lib.struct_num_set_uint16_t(struct, value);

            assertEquals(value, struct.val_uint16_t.get(),
                    "Incorrect struct unsigned short value");
            assertEquals(value, lib.struct_num_get_uint16_t(struct),
                    "Incorrect struct unsigned short value");

            struct.reset();
        }
    }

    // ========================= Unsigned Int =======================

    @Test
    public void testGetUnsignedInt() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint32_t.get(),
                    "Struct unsigned int value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint32_t(struct),
                    "Struct unsigned int value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            struct.val_uint32_t.set(value);

            assertEquals(value, struct.val_uint32_t.get(),
                    "Incorrect struct unsigned int value");
            assertEquals(value, lib.struct_num_get_uint32_t(struct),
                    "Incorrect struct unsigned int value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedInt() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint32_t.get(),
                    "Struct unsigned int value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint32_t(struct),
                    "Struct unsigned int value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            lib.struct_num_set_uint32_t(struct, value);

            assertEquals(value, struct.val_uint32_t.get(),
                    "Incorrect struct unsigned int value");
            assertEquals(value, lib.struct_num_get_uint32_t(struct),
                    "Incorrect struct unsigned int value");

            struct.reset();
        }
    }

    // ========================= Unsigned Native Long ===============

    @Test
    public void testGetUnsignedNativeLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_ulong.get(),
                    "Struct unsigned native long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_ulong(struct).longValue(),
                    "Struct unsigned native long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            struct.val_ulong.set(value);

            assertEquals(value, struct.val_ulong.get(),
                    "Incorrect struct unsigned native long value");
            assertEquals(value, lib.struct_num_get_ulong(struct).longValue(),
                    "Incorrect struct unsigned native long value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedNativeLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_ulong.get(),
                    "Struct unsigned native long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_ulong(struct).longValue(),
                    "Struct unsigned native long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            lib.struct_num_set_ulong(struct, NativeLong.valueOf(value));

            assertEquals(value, struct.val_ulong.get(),
                    "Incorrect struct unsigned native long value");
            assertEquals(value, lib.struct_num_get_ulong(struct).longValue(),
                    "Incorrect struct unsigned native long value");

            struct.reset();
        }
    }

    // ========================= Unsigned Long ======================

    @Test
    public void testGetUnsignedLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint64_t.get(),
                    "Struct unsigned long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint64_t(struct),
                    "Struct unsigned long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_LONG);
            struct.val_uint64_t.set(value);

            assertEquals(value, struct.val_uint64_t.get(),
                    "Incorrect struct unsigned long value");
            assertEquals(value, lib.struct_num_get_uint64_t(struct),
                    "Incorrect struct unsigned long value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedLong() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint64_t.get(),
                    "Struct unsigned long value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_uint64_t(struct),
                    "Struct unsigned long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_LONG);
            lib.struct_num_set_uint64_t(struct, value);

            assertEquals(value, struct.val_uint64_t.get(),
                    "Incorrect struct unsigned long value");
            assertEquals(value, lib.struct_num_get_uint64_t(struct),
                    "Incorrect struct unsigned long value");

            struct.reset();
        }
    }

    // ========================= Float ==============================

    @Test
    public void testGetFloat() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_float.get(),
                    "Struct float value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_float(struct),
                    "Struct float value should be 0 after creation and reset");

            float value = R.nextFloat();
            struct.val_float.set(value);

            assertEquals(value, struct.val_float.get(),
                    "Incorrect struct float value");
            assertEquals(value, lib.struct_num_get_float(struct),
                    "Incorrect struct float value");

            struct.reset();
        }
    }

    @Test
    public void testSetFloat() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_float.get(),
                    "Struct float value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_float(struct),
                    "Struct float value should be 0 after creation and reset");

            float value = R.nextFloat();
            lib.struct_num_set_float(struct, value);

            assertEquals(value, struct.val_float.get(),
                    "Incorrect struct float value");
            assertEquals(value, lib.struct_num_get_float(struct),
                    "Incorrect struct float value");

            struct.reset();
        }
    }

    // ========================= Double =============================

    @Test
    public void testGetDouble() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_double.get(),
                    "Struct double value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_double(struct),
                    "Struct double value should be 0 after creation and reset");

            double value = R.nextDouble();
            struct.val_double.set(value);

            assertEquals(value, struct.val_double.get(),
                    "Incorrect struct double value");
            assertEquals(value, lib.struct_num_get_double(struct),
                    "Incorrect struct double value");

            struct.reset();
        }
    }

    @Test
    public void testSetDouble() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_double.get(),
                    "Struct double value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_double(struct),
                    "Struct double value should be 0 after creation and reset");

            double value = R.nextDouble();
            lib.struct_num_set_double(struct, value);

            assertEquals(value, struct.val_double.get(),
                    "Incorrect struct double value");
            assertEquals(value, lib.struct_num_get_double(struct),
                    "Incorrect struct double value");

            struct.reset();
        }
    }

    // ========================= Boolean ============================

    @Test
    public void testGetBoolean() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertFalse(struct.val_bool.get(),
                    "Struct boolean value should be 0 (false) after creation and reset");
            assertFalse(lib.struct_num_get_bool(struct),
                    "Struct boolean value should be 0 (false) after creation and reset");

            boolean value = R.nextBoolean();
            struct.val_bool.set(value);

            assertEquals(value, struct.val_bool.get(),
                    "Incorrect struct boolean value");
            assertEquals(value, lib.struct_num_get_bool(struct),
                    "Incorrect struct boolean value");

            struct.reset();
        }
    }

    @Test
    public void testSetBoolean() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertFalse(struct.val_bool.get(),
                    "Struct boolean value should be 0 (false) after creation and reset");
            assertFalse(lib.struct_num_get_bool(struct),
                    "Struct boolean value should be 0 (false) after creation and reset");

            boolean value = R.nextBoolean();
            lib.struct_num_set_bool(struct, value);

            assertEquals(value, struct.val_bool.get(),
                    "Incorrect struct boolean value");
            assertEquals(value, lib.struct_num_get_bool(struct),
                    "Incorrect struct boolean value");

            struct.reset();
        }
    }

    // ========================= Enum ===============================

    @Test
    public void testGetEnum() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_Enum.get().ordinal(),
                    "Struct enum value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_Enum(struct).ordinal(),
                    "Struct enum value should be 0 after creation and reset");

            int value = R.nextInt(Enum.values().length);
            struct.val_Enum.set(value);

            assertEquals(value, struct.val_Enum.get().ordinal(),
                    "Incorrect struct enum value");
            assertEquals(value, lib.struct_num_get_Enum(struct).ordinal(),
                    "Incorrect struct enum value");

            struct.reset();
        }
    }

    @Test
    public void testSetEnum() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_Enum.get().ordinal(),
                    "Struct enum value should be 0 after creation and reset");
            assertEquals(0, lib.struct_num_get_Enum(struct).ordinal(),
                    "Struct enum value should be 0 after creation and reset");

            int value = R.nextInt(Enum.values().length);
            lib.struct_num_set_Enum(struct, Enum.values()[value]);

            assertEquals(value, struct.val_Enum.get().ordinal(),
                    "Incorrect struct enum value");
            assertEquals(value, lib.struct_num_get_Enum(struct).ordinal(),
                    "Incorrect struct enum value");

            struct.reset();
        }
    }

    // ========================= Pointer ============================

    @Test
    public void testGetPointer() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(struct.val_pointer.get(),
                    "Struct pointer should be null after creation and reset");
            assertNull(lib.struct_num_get_pointer(struct),
                    "Struct pointer should be null after creation and reset");

            int value = R.nextInt();

            Pointer pointer = Memory.allocateDirect(runtime, runtime.findType(NativeType.SINT).size());
            pointer.putInt(0, value);

            struct.val_pointer.set(pointer);

            assertNotNull(struct.val_pointer.get());

            assertEquals(value, struct.val_pointer.get().getInt(0),
                    "Incorrect struct pointer value");
            assertEquals(value, lib.struct_num_get_pointer(struct).getInt(0),
                    "Incorrect struct pointer value");

            struct.reset();
        }
    }

    @Test
    public void testSetPointer() {
        NumericStruct struct = new NumericStruct();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(struct.val_pointer.get(),
                    "Struct pointer should be null after creation and reset");
            assertNull(lib.struct_num_get_pointer(struct),
                    "Struct pointer should be null after creation and reset");

            int value = R.nextInt();

            Pointer pointer = Memory.allocateDirect(runtime, runtime.findType(NativeType.SINT).size());
            pointer.putInt(0, value);

            lib.struct_num_set_pointer(struct, pointer);

            assertNotNull(struct.val_pointer.get());

            assertEquals(value, struct.val_pointer.get().getInt(0),
                    "Incorrect struct pointer value");
            assertEquals(value, lib.struct_num_get_pointer(struct).getInt(0),
                    "Incorrect struct pointer value");

            struct.reset();
        }
    }

    // ========================= Other ==============================

    @Test
    public void testSize() {
        NumericStruct struct = new NumericStruct();
        assertEquals(lib.struct_num_size(), Struct.size(struct),
                "Incorrect struct size");
    }
}
