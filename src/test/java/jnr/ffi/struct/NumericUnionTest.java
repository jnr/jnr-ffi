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
import jnr.ffi.Union;
import jnr.ffi.types.u_int16_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests NumericUnion.c
 * This tests a native union that contains only every possible numeric type including
 * booleans, enums and pointers (C void *)
 * This is generally identical to {@link NumericStructTest} but with additional tests
 * relating to the memory sharing behavior of unions which structs do not follow
 */
@SuppressWarnings("RedundantCast")
public class NumericUnionTest {

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

    public static class NumericUnion extends Union {
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
        public final Struct.Enum<NumericUnionTest.Enum> val_Enum = new Struct.Enum<>(NumericUnionTest.Enum.class);
        public final Struct.Pointer val_pointer = new Struct.Pointer();

        public NumericUnion(Runtime runtime) {super(runtime);}

        public NumericUnion() {this(runtime);}

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
        public byte union_num_get_int8_t(NumericUnion s);
        public void union_num_set_int8_t(NumericUnion s, byte v);

        public short union_num_get_int16_t(NumericUnion s);
        public void union_num_set_int16_t(NumericUnion s, short v);

        public int union_num_get_int32_t(NumericUnion s);
        public void union_num_set_int32_t(NumericUnion s, int v);

        public NativeLong union_num_get_long(NumericUnion s);
        public void union_num_set_long(NumericUnion s, NativeLong v);

        public long union_num_get_int64_t(NumericUnion s);
        public void union_num_set_int64_t(NumericUnion s, long v);

        public @u_int8_t
        short union_num_get_uint8_t(NumericUnion s);
        public void union_num_set_uint8_t(NumericUnion s, @u_int8_t short v);

        public @u_int16_t
        int union_num_get_uint16_t(NumericUnion s);
        public void union_num_set_uint16_t(NumericUnion s, @u_int16_t int v);

        public @u_int32_t
        long union_num_get_uint32_t(NumericUnion s);
        public void union_num_set_uint32_t(NumericUnion s, @u_int32_t long v);

        public NativeLong union_num_get_ulong(NumericUnion s);
        public void union_num_set_ulong(NumericUnion s, NativeLong v);

        public @u_int64_t
        long union_num_get_uint64_t(NumericUnion s);
        public void union_num_set_uint64_t(NumericUnion s, @u_int64_t long v);

        public float union_num_get_float(NumericUnion s);
        public void union_num_set_float(NumericUnion s, float v);

        public double union_num_get_double(NumericUnion s);
        public void union_num_set_double(NumericUnion s, double v);

        public boolean union_num_get_bool(NumericUnion s);
        public void union_num_set_bool(NumericUnion s, boolean v);

        public Enum union_num_get_Enum(NumericUnion s);
        public void union_num_set_Enum(NumericUnion s, Enum v);

        public Pointer union_num_get_pointer(NumericUnion s);
        public void union_num_set_pointer(NumericUnion s, Pointer v);

        public int union_num_size();
    }

    @BeforeAll
    public static void beforeAll() {
        lib = TstUtil.loadTestLib(Lib.class);
        runtime = Runtime.getRuntime(lib);
    }

    // ========================= Byte ===============================

    @Test
    public void testGetByte() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int8_t.get(),
                    "Union byte value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int8_t(struct),
                    "Union byte value should be 0 after creation and reset");

            byte value = (byte) R.nextInt();
            struct.val_int8_t.set(value);

            assertEquals(value, struct.val_int8_t.get(),
                    "Incorrect union byte value");
            assertEquals(value, lib.union_num_get_int8_t(struct),
                    "Incorrect union byte value");

            struct.reset();
        }
    }

    @Test
    public void testSetByte() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int8_t.get(),
                    "Union byte value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int8_t(struct),
                    "Union byte value should be 0 after creation and reset");

            byte value = (byte) R.nextInt();
            lib.union_num_set_int8_t(struct, value);

            assertEquals(value, struct.val_int8_t.get(),
                    "Incorrect union byte value");
            assertEquals(value, lib.union_num_get_int8_t(struct),
                    "Incorrect union byte value");

            struct.reset();
        }
    }

    // ========================= Short ==============================

    @Test
    public void testGetShort() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int16_t.get(),
                    "Union short value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int16_t(struct),
                    "Union short value should be 0 after creation and reset");

            short value = (short) R.nextInt();
            struct.val_int16_t.set(value);

            assertEquals(value, struct.val_int16_t.get(),
                    "Incorrect union short value");
            assertEquals(value, lib.union_num_get_int16_t(struct),
                    "Incorrect union short value");

            struct.reset();
        }
    }

    @Test
    public void testSetShort() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int16_t.get(),
                    "Union short value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int16_t(struct),
                    "Union short value should be 0 after creation and reset");

            short value = (short) R.nextInt();
            lib.union_num_set_int16_t(struct, value);

            assertEquals(value, struct.val_int16_t.get(),
                    "Incorrect union short value");
            assertEquals(value, lib.union_num_get_int16_t(struct),
                    "Incorrect union short value");

            struct.reset();
        }
    }

    // ========================= Int ================================

    @Test
    public void testGetInt() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int32_t.get(),
                    "Union int value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int32_t(struct),
                    "Union int value should be 0 after creation and reset");

            int value = R.nextInt();
            struct.val_int32_t.set(value);

            assertEquals(value, struct.val_int32_t.get(),
                    "Incorrect union int value");
            assertEquals(value, lib.union_num_get_int32_t(struct),
                    "Incorrect union int value");

            struct.reset();
        }
    }

    @Test
    public void testSetInt() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int32_t.get(),
                    "Union int value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int32_t(struct),
                    "Union int value should be 0 after creation and reset");

            int value = R.nextInt();
            lib.union_num_set_int32_t(struct, value);

            assertEquals(value, struct.val_int32_t.get(),
                    "Incorrect union int value");
            assertEquals(value, lib.union_num_get_int32_t(struct),
                    "Incorrect union int value");

            struct.reset();
        }
    }

    // ========================= NativeLong =========================

    @Test
    public void testGetNativeLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_long.get(),
                    "Union native long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_long(struct).longValue(),
                    "Union native long value should be 0 after creation and reset");

            long value = R.nextLong();
            struct.val_long.set(value);

            assertEquals(value, struct.val_long.get(),
                    "Incorrect union native long value");
            assertEquals(value, lib.union_num_get_long(struct).longValue(),
                    "Incorrect union native long value");

            struct.reset();
        }
    }

    @Test
    public void testSetNativeLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_long.get(),
                    "Union native long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_long(struct).longValue(),
                    "Union native long value should be 0 after creation and reset");

            long value = R.nextLong();
            lib.union_num_set_long(struct, NativeLong.valueOf(value));

            assertEquals(value, struct.val_long.get(),
                    "Incorrect union native long value");
            assertEquals(value, lib.union_num_get_long(struct).longValue(),
                    "Incorrect union native long value");

            struct.reset();
        }
    }

    // ========================= Long ===============================

    @Test
    public void testGetLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int64_t.get(),
                    "Union long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int64_t(struct),
                    "Union long value should be 0 after creation and reset");

            long value = R.nextLong();
            struct.val_int64_t.set(value);

            assertEquals(value, struct.val_int64_t.get(),
                    "Incorrect union long value");
            assertEquals(value, lib.union_num_get_int64_t(struct),
                    "Incorrect union long value");

            struct.reset();
        }
    }

    @Test
    public void testSetLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_int64_t.get(),
                    "Union long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_int64_t(struct),
                    "Union long value should be 0 after creation and reset");

            long value = R.nextLong();
            lib.union_num_set_int64_t(struct, value);

            assertEquals(value, struct.val_int64_t.get(),
                    "Incorrect union long value");
            assertEquals(value, lib.union_num_get_int64_t(struct),
                    "Incorrect union long value");

            struct.reset();
        }
    }

    // ========================= Unsigned Byte ======================

    @Test
    public void testGetUnsignedByte() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint8_t.get(),
                    "Union unsigned byte value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint8_t(struct),
                    "Union unsigned byte value should be 0 after creation and reset");

            short value = (short) R.nextInt(MAX_UNSIGNED_BYTE);
            struct.val_uint8_t.set(value);

            assertEquals(value, struct.val_uint8_t.get(),
                    "Incorrect union unsigned byte value");
            assertEquals(value, lib.union_num_get_uint8_t(struct),
                    "Incorrect union unsigned byte value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedByte() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint8_t.get(),
                    "Union unsigned byte value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint8_t(struct),
                    "Union unsigned byte value should be 0 after creation and reset");

            short value = (short) R.nextInt(MAX_UNSIGNED_BYTE);
            lib.union_num_set_uint8_t(struct, value);

            assertEquals(value, struct.val_uint8_t.get(),
                    "Incorrect union unsigned byte value");
            assertEquals(value, lib.union_num_get_uint8_t(struct),
                    "Incorrect union unsigned byte value");

            struct.reset();
        }
    }

    // ========================= Unsigned Short =====================

    @Test
    public void testGetUnsignedShort() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint16_t.get(),
                    "Union unsigned short value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint16_t(struct),
                    "Union unsigned short value should be 0 after creation and reset");

            int value = R.nextInt(MAX_UNSIGNED_SHORT);
            struct.val_uint16_t.set(value);

            assertEquals(value, struct.val_uint16_t.get(),
                    "Incorrect union unsigned short value");
            assertEquals(value, lib.union_num_get_uint16_t(struct),
                    "Incorrect union unsigned short value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedShort() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint16_t.get(),
                    "Union unsigned short value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint16_t(struct),
                    "Union unsigned short value should be 0 after creation and reset");

            int value = R.nextInt(MAX_UNSIGNED_SHORT);
            lib.union_num_set_uint16_t(struct, value);

            assertEquals(value, struct.val_uint16_t.get(),
                    "Incorrect union unsigned short value");
            assertEquals(value, lib.union_num_get_uint16_t(struct),
                    "Incorrect union unsigned short value");

            struct.reset();
        }
    }

    // ========================= Unsigned Int =======================

    @Test
    public void testGetUnsignedInt() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint32_t.get(),
                    "Union unsigned int value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint32_t(struct),
                    "Union unsigned int value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            struct.val_uint32_t.set(value);

            assertEquals(value, struct.val_uint32_t.get(),
                    "Incorrect union unsigned int value");
            assertEquals(value, lib.union_num_get_uint32_t(struct),
                    "Incorrect union unsigned int value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedInt() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint32_t.get(),
                    "Union unsigned int value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint32_t(struct),
                    "Union unsigned int value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            lib.union_num_set_uint32_t(struct, value);

            assertEquals(value, struct.val_uint32_t.get(),
                    "Incorrect union unsigned int value");
            assertEquals(value, lib.union_num_get_uint32_t(struct),
                    "Incorrect union unsigned int value");

            struct.reset();
        }
    }

    // ========================= Unsigned Native Long ===============

    @Test
    public void testGetUnsignedNativeLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_ulong.get(),
                    "Union unsigned native long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_ulong(struct).longValue(),
                    "Union unsigned native long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            struct.val_ulong.set(value);

            assertEquals(value, struct.val_ulong.get(),
                    "Incorrect union unsigned native long value");
            assertEquals(value, lib.union_num_get_ulong(struct).longValue(),
                    "Incorrect union unsigned native long value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedNativeLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_ulong.get(),
                    "Union unsigned native long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_ulong(struct).longValue(),
                    "Union unsigned native long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_INT);
            lib.union_num_set_ulong(struct, NativeLong.valueOf(value));

            assertEquals(value, struct.val_ulong.get(),
                    "Incorrect union unsigned native long value");
            assertEquals(value, lib.union_num_get_ulong(struct).longValue(),
                    "Incorrect union unsigned native long value");

            struct.reset();
        }
    }

    // ========================= Unsigned Long ======================

    @Test
    public void testGetUnsignedLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint64_t.get(),
                    "Union unsigned long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint64_t(struct),
                    "Union unsigned long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_LONG);
            struct.val_uint64_t.set(value);

            assertEquals(value, struct.val_uint64_t.get(),
                    "Incorrect union unsigned long value");
            assertEquals(value, lib.union_num_get_uint64_t(struct),
                    "Incorrect union unsigned long value");

            struct.reset();
        }
    }

    @Test
    public void testSetUnsignedLong() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_uint64_t.get(),
                    "Union unsigned long value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_uint64_t(struct),
                    "Union unsigned long value should be 0 after creation and reset");

            long value = R.nextLong(MAX_UNSIGNED_LONG);
            lib.union_num_set_uint64_t(struct, value);

            assertEquals(value, struct.val_uint64_t.get(),
                    "Incorrect union unsigned long value");
            assertEquals(value, lib.union_num_get_uint64_t(struct),
                    "Incorrect union unsigned long value");

            struct.reset();
        }
    }

    // ========================= Float ==============================

    @Test
    public void testGetFloat() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_float.get(),
                    "Union float value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_float(struct),
                    "Union float value should be 0 after creation and reset");

            float value = R.nextFloat();
            struct.val_float.set(value);

            assertEquals(value, struct.val_float.get(),
                    "Incorrect union float value");
            assertEquals(value, lib.union_num_get_float(struct),
                    "Incorrect union float value");

            struct.reset();
        }
    }

    @Test
    public void testSetFloat() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_float.get(),
                    "Union float value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_float(struct),
                    "Union float value should be 0 after creation and reset");

            float value = R.nextFloat();
            lib.union_num_set_float(struct, value);

            assertEquals(value, struct.val_float.get(),
                    "Incorrect union float value");
            assertEquals(value, lib.union_num_get_float(struct),
                    "Incorrect union float value");

            struct.reset();
        }
    }

    // ========================= Double =============================

    @Test
    public void testGetDouble() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_double.get(),
                    "Union double value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_double(struct),
                    "Union double value should be 0 after creation and reset");

            double value = R.nextDouble();
            struct.val_double.set(value);

            assertEquals(value, struct.val_double.get(),
                    "Incorrect union double value");
            assertEquals(value, lib.union_num_get_double(struct),
                    "Incorrect union double value");

            struct.reset();
        }
    }

    @Test
    public void testSetDouble() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_double.get(),
                    "Union double value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_double(struct),
                    "Union double value should be 0 after creation and reset");

            double value = R.nextDouble();
            lib.union_num_set_double(struct, value);

            assertEquals(value, struct.val_double.get(),
                    "Incorrect union double value");
            assertEquals(value, lib.union_num_get_double(struct),
                    "Incorrect union double value");

            struct.reset();
        }
    }

    // ========================= Boolean ============================

    @Test
    public void testGetBoolean() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertFalse(struct.val_bool.get(),
                    "Union boolean value should be 0 (false) after creation and reset");
            assertFalse(lib.union_num_get_bool(struct),
                    "Union boolean value should be 0 (false) after creation and reset");

            boolean value = R.nextBoolean();
            struct.val_bool.set(value);

            assertEquals(value, struct.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals(value, lib.union_num_get_bool(struct),
                    "Incorrect union boolean value");

            struct.reset();
        }
    }

    @Test
    public void testSetBoolean() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertFalse(struct.val_bool.get(),
                    "Union boolean value should be 0 (false) after creation and reset");
            assertFalse(lib.union_num_get_bool(struct),
                    "Union boolean value should be 0 (false) after creation and reset");

            boolean value = R.nextBoolean();
            lib.union_num_set_bool(struct, value);

            assertEquals(value, struct.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals(value, lib.union_num_get_bool(struct),
                    "Incorrect union boolean value");

            struct.reset();
        }
    }

    // ========================= Enum ===============================

    @Test
    public void testGetEnum() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_Enum.get().ordinal(),
                    "Union enum value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_Enum(struct).ordinal(),
                    "Union enum value should be 0 after creation and reset");

            int value = R.nextInt(Enum.values().length);
            struct.val_Enum.set(value);

            assertEquals(value, struct.val_Enum.get().ordinal(),
                    "Incorrect union enum value");
            assertEquals(value, lib.union_num_get_Enum(struct).ordinal(),
                    "Incorrect union enum value");

            struct.reset();
        }
    }

    @Test
    public void testSetEnum() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals(0, struct.val_Enum.get().ordinal(),
                    "Union enum value should be 0 after creation and reset");
            assertEquals(0, lib.union_num_get_Enum(struct).ordinal(),
                    "Union enum value should be 0 after creation and reset");

            int value = R.nextInt(Enum.values().length);
            lib.union_num_set_Enum(struct, Enum.values()[value]);

            assertEquals(value, struct.val_Enum.get().ordinal(),
                    "Incorrect union enum value");
            assertEquals(value, lib.union_num_get_Enum(struct).ordinal(),
                    "Incorrect union enum value");

            struct.reset();
        }
    }

    // ========================= Pointer ============================

    @Test
    public void testGetPointer() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(struct.val_pointer.get(),
                    "Union pointer should be null after creation and reset");
            assertNull(lib.union_num_get_pointer(struct),
                    "Union pointer should be null after creation and reset");

            int value = R.nextInt();

            Pointer pointer = Memory.allocateDirect(runtime, runtime.findType(NativeType.SINT).size());
            pointer.putInt(0, value);

            struct.val_pointer.set(pointer);

            assertNotNull(struct.val_pointer.get());

            assertEquals(value, struct.val_pointer.get().getInt(0),
                    "Incorrect union pointer value");
            assertEquals(value, lib.union_num_get_pointer(struct).getInt(0),
                    "Incorrect union pointer value");

            struct.reset();
        }
    }

    @Test
    public void testSetPointer() {
        NumericUnion struct = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(struct.val_pointer.get(),
                    "Union pointer should be null after creation and reset");
            assertNull(lib.union_num_get_pointer(struct),
                    "Union pointer should be null after creation and reset");

            int value = R.nextInt();

            Pointer pointer = Memory.allocateDirect(runtime, runtime.findType(NativeType.SINT).size());
            pointer.putInt(0, value);

            lib.union_num_set_pointer(struct, pointer);

            assertNotNull(struct.val_pointer.get());

            assertEquals(value, struct.val_pointer.get().getInt(0),
                    "Incorrect union pointer value");
            assertEquals(value, lib.union_num_get_pointer(struct).getInt(0),
                    "Incorrect union pointer value");

            struct.reset();
        }
    }

    // ========================= Union Memory =======================

    @Test
    public void testUnionOffsets() {
        NumericUnion union = new NumericUnion();
        assertEquals(0, union.val_int8_t.offset(),
                "Incorrect union offset for byte");
        assertEquals(0, union.val_int16_t.offset(),
                "Incorrect union offset for short");
        assertEquals(0, union.val_int32_t.offset(),
                "Incorrect union offset for int");
        assertEquals(0, union.val_long.offset(),
                "Incorrect union offset for native long");
        assertEquals(0, union.val_int64_t.offset(),
                "Incorrect union offset for long");
        assertEquals(0, union.val_uint8_t.offset(),
                "Incorrect union offset for unsigned byte");
        assertEquals(0, union.val_uint16_t.offset(),
                "Incorrect union offset for unsigned short");
        assertEquals(0, union.val_uint32_t.offset(),
                "Incorrect union offset for unsigned int");
        assertEquals(0, union.val_ulong.offset(),
                "Incorrect union offset for unsigned native long");
        assertEquals(0, union.val_uint64_t.offset(),
                "Incorrect union offset for unsigned long");
        assertEquals(0, union.val_float.offset(),
                "Incorrect union offset for float");
        assertEquals(0, union.val_double.offset(),
                "Incorrect union offset for double");
        assertEquals(0, union.val_bool.offset(),
                "Incorrect union offset for boolean");
        assertEquals(0, union.val_Enum.offset(),
                "Incorrect union offset for enum");
        assertEquals(0, union.val_pointer.offset(),
                "Incorrect union offset for pointer");
    }

    /*
     * For these memory tests we will put the smallest possible type (like a byte for integrals or a float)
     * and expect that when we do a call to get the value of one of the larger types such as int, it will be
     * the same number that we put in because the memory is the same, the extra bytes that an int would need
     * would just be 0s which is why we put the smallest type
     */

    @Test
    public void testUnionMemorySignedIntegrals() {
        NumericUnion union = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            byte value = (byte) R.nextInt();
            union.val_int64_t.set(value);

            assertEquals((byte) value, union.val_int8_t.get(),
                    "Incorrect union byte value");
            assertEquals((short) value, union.val_int16_t.get(),
                    "Incorrect union short value");
            assertEquals((int) value, union.val_int32_t.get(),
                    "Incorrect union int value");
            assertEquals((long) value, union.val_long.get(),
                    "Incorrect union native long value");
            assertEquals((long) value, union.val_int64_t.get(),
                    "Incorrect union long value");

            assertEquals(value != 0, union.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals((long) value, union.val_pointer.longValue(),
                    "Incorrect union pointer address");

            assertEquals(Float.intBitsToFloat(value), union.val_float.get(),
                    "Incorrect union float value");
            assertEquals(Double.longBitsToDouble(value), union.val_double.get(),
                    "Incorrect union double value");
        }
    }

    @Test
    public void testUnionMemoryUnsignedIntegrals() {
        NumericUnion union = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            short value = (short) R.nextInt(MAX_UNSIGNED_BYTE);
            union.val_uint64_t.set(value);

            assertEquals((short) value, union.val_uint8_t.get(),
                    "Incorrect union unsigned byte value");
            assertEquals((int) value, union.val_uint16_t.get(),
                    "Incorrect union unsigned short value");
            assertEquals((long) value, union.val_uint32_t.get(),
                    "Incorrect union unsigned int value");
            assertEquals((long) value, union.val_ulong.get(),
                    "Incorrect union unsigned native long value");
            assertEquals((long) value, union.val_uint64_t.get(),
                    "Incorrect union unsigned long value");

            assertEquals(value != 0, union.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals((long) value, union.val_pointer.longValue(),
                    "Incorrect union pointer address");

            assertEquals(Float.intBitsToFloat(value), union.val_float.get(),
                    "Incorrect union float value");
            assertEquals(Double.longBitsToDouble(value), union.val_double.get(),
                    "Incorrect union double value");
        }
    }

    @Test
    public void testUnionMemoryFloats() {
        NumericUnion union = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            byte value = (byte) R.nextInt();
            union.val_double.set(Double.longBitsToDouble(value));

            assertEquals((byte) value, union.val_int8_t.get(),
                    "Incorrect union byte value");
            assertEquals((short) value, union.val_int16_t.get(),
                    "Incorrect union short value");
            assertEquals((int) value, union.val_int32_t.get(),
                    "Incorrect union int value");
            assertEquals((long) value, union.val_long.get(),
                    "Incorrect union native long value");
            assertEquals((long) value, union.val_int64_t.get(),
                    "Incorrect union long value");

            assertEquals(value != 0, union.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals((long) value, union.val_pointer.longValue(),
                    "Incorrect union pointer address");

            assertEquals(Float.intBitsToFloat(value), union.val_float.get(),
                    "Incorrect union float value");
            assertEquals(Double.longBitsToDouble(value), union.val_double.get(),
                    "Incorrect union double value");
        }
    }

    @Test
    public void testUnionMemoryBoolean() {
        NumericUnion union = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            boolean bool = R.nextBoolean();
            union.val_bool.set(bool);
            int value = bool ? 1 : 0;

            assertEquals((byte) value, union.val_int8_t.get(),
                    "Incorrect union byte value");
            assertEquals((short) value, union.val_int16_t.get(),
                    "Incorrect union short value");
            assertEquals((int) value, union.val_int32_t.get(),
                    "Incorrect union int value");
            assertEquals((long) value, union.val_long.get(),
                    "Incorrect union native long value");
            assertEquals((long) value, union.val_int64_t.get(),
                    "Incorrect union long value");

            assertEquals((short) value, union.val_uint8_t.get(),
                    "Incorrect union unsigned byte value");
            assertEquals((int) value, union.val_uint16_t.get(),
                    "Incorrect union unsigned short value");
            assertEquals((long) value, union.val_uint32_t.get(),
                    "Incorrect union unsigned int value");
            assertEquals((long) value, union.val_ulong.get(),
                    "Incorrect union unsigned native long value");
            assertEquals((long) value, union.val_uint64_t.get(),
                    "Incorrect union unsigned long value");

            assertEquals(bool, union.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals((long) value, union.val_pointer.longValue(),
                    "Incorrect union pointer address");
            assertEquals((int) value, union.val_Enum.get().ordinal(),
                    "Incorrect union enum value");

            assertEquals(Float.intBitsToFloat(value), union.val_float.get(),
                    "Incorrect union float value");
            assertEquals(Double.longBitsToDouble(value), union.val_double.get(),
                    "Incorrect union double value");
        }
    }

    @Test
    public void testUnionMemoryEnum() {
        NumericUnion union = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            byte value = (byte) R.nextInt(Enum.values().length);
            union.val_Enum.set(Enum.values()[value]);

            assertEquals((byte) value, union.val_int8_t.get(),
                    "Incorrect union byte value");
            assertEquals((short) value, union.val_int16_t.get(),
                    "Incorrect union short value");
            assertEquals((int) value, union.val_int32_t.get(),
                    "Incorrect union int value");
            assertEquals((long) value, union.val_long.get(),
                    "Incorrect union native long value");
            assertEquals((long) value, union.val_int64_t.get(),
                    "Incorrect union long value");

            assertEquals(value != 0, union.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals((long) value, union.val_pointer.longValue(),
                    "Incorrect union pointer address");
            assertEquals((int) value, union.val_Enum.get().ordinal(),
                    "Incorrect union enum value");

            assertEquals(Float.intBitsToFloat(value), union.val_float.get(),
                    "Incorrect union float value");
            assertEquals(Double.longBitsToDouble(value), union.val_double.get(),
                    "Incorrect union double value");
        }
    }

    @Test
    public void testUnionMemoryPointer() {
        NumericUnion union = new NumericUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            byte value = (byte) R.nextInt();
            union.val_pointer.set(value);

            assertEquals((byte) value, union.val_int8_t.get(),
                    "Incorrect union byte value");
            assertEquals((short) value, union.val_int16_t.get(),
                    "Incorrect union short value");
            assertEquals((int) value, union.val_int32_t.get(),
                    "Incorrect union int value");
            assertEquals((long) value, union.val_long.get(),
                    "Incorrect union native long value");
            assertEquals((long) value, union.val_int64_t.get(),
                    "Incorrect union long value");

            assertEquals(value != 0, union.val_bool.get(),
                    "Incorrect union boolean value");
            assertEquals((long) value, union.val_pointer.longValue(),
                    "Incorrect union pointer address");

            assertEquals(Float.intBitsToFloat(value), union.val_float.get(),
                    "Incorrect union float value");
            assertEquals(Double.longBitsToDouble(value), union.val_double.get(),
                    "Incorrect union double value");
        }
    }

    // ========================= Other ==============================

    @Test
    public void testSize() {
        NumericUnion union = new NumericUnion();
        assertEquals(lib.union_num_size(), Struct.size(union),
                "Incorrect union size");
    }

}
