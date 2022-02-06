package jnr.ffi.struct;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.TstUtil;
import jnr.ffi.Union;
import jnr.ffi.struct.NumericStructTest.NumericStruct;
import jnr.ffi.struct.NumericUnionTest.NumericUnion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests NestedUnion.c
 * This tests a union that contains an inner struct, an inner union,
 * a pointer to a struct and a pointer to a union.
 */
@SuppressWarnings("RedundantCast")
public class NestedUnionTest {

    private static final int ITERATIONS_COUNT = 100_000;

    // ThreadLocalRandom instead of Random because we want a nextLong() method with bounds which Random doesn't have
    private static final ThreadLocalRandom R = ThreadLocalRandom.current();

    private static Lib lib;
    private static Runtime runtime;

    public static class NestedUnion extends Union {
        public final NumericStruct inner_NumericStruct = inner(NumericStruct.class);
        public final NumericUnion inner_NumericUnion = inner(NumericUnion.class);

        public final StructRef<NumericStruct> ptr_NumericStruct = new StructRef<>(NumericStruct.class);
        public final StructRef<NumericUnion> ptr_NumericUnion = new StructRef<>(NumericUnion.class);

        public NestedUnion(Runtime runtime) {super(runtime);}

        public NestedUnion() {this(runtime);}
    }

    public static interface Lib {
        // ========================= Inner Struct =======================================

        public byte nested_union_inner_struct_get_int8_t(NestedUnion u);
        public void nested_union_inner_struct_set_int8_t(NestedUnion u, byte v);

        public long nested_union_inner_struct_get_int64_t(NestedUnion u);
        public void nested_union_inner_struct_set_int64_t(NestedUnion u, long v);

        public float nested_union_inner_struct_get_float(NestedUnion u);
        public void nested_union_inner_struct_set_float(NestedUnion u, float v);

        public double nested_union_inner_struct_get_double(NestedUnion u);
        public void nested_union_inner_struct_set_double(NestedUnion u, double v);

        public Pointer nested_union_inner_struct_get_pointer(NestedUnion u);
        public void nested_union_inner_struct_set_pointer(NestedUnion u, Pointer v);

        // ========================= Pointer Struct =====================================

        public byte nested_union_ptr_struct_get_int8_t(NestedUnion u);
        public void nested_union_ptr_struct_set_int8_t(NestedUnion u, byte v);

        public long nested_union_ptr_struct_get_int64_t(NestedUnion u);
        public void nested_union_ptr_struct_set_int64_t(NestedUnion u, long v);

        public float nested_union_ptr_struct_get_float(NestedUnion u);
        public void nested_union_ptr_struct_set_float(NestedUnion u, float v);

        public double nested_union_ptr_struct_get_double(NestedUnion u);
        public void nested_union_ptr_struct_set_double(NestedUnion u, double v);

        public Pointer nested_union_ptr_struct_get_pointer(NestedUnion u);
        public void nested_union_ptr_struct_set_pointer(NestedUnion u, Pointer v);

        // ========================= Inner Union ========================================

        public byte nested_union_inner_union_get_int8_t(NestedUnion u);
        public void nested_union_inner_union_set_int8_t(NestedUnion u, byte v);

        public long nested_union_inner_union_get_int64_t(NestedUnion u);
        public void nested_union_inner_union_set_int64_t(NestedUnion u, long v);

        public float nested_union_inner_union_get_float(NestedUnion u);
        public void nested_union_inner_union_set_float(NestedUnion u, float v);

        public double nested_union_inner_union_get_double(NestedUnion u);
        public void nested_union_inner_union_set_double(NestedUnion u, double v);

        public Pointer nested_union_inner_union_get_pointer(NestedUnion u);
        public void nested_union_inner_union_set_pointer(NestedUnion u, Pointer v);

        // ========================= Pointer Union ======================================

        public byte nested_union_ptr_union_get_int8_t(NestedUnion u);
        public void nested_union_ptr_union_set_int8_t(NestedUnion u, byte v);

        public long nested_union_ptr_union_get_int64_t(NestedUnion u);
        public void nested_union_ptr_union_set_int64_t(NestedUnion u, long v);

        public float nested_union_ptr_union_get_float(NestedUnion u);
        public void nested_union_ptr_union_set_float(NestedUnion u, float v);

        public double nested_union_ptr_union_get_double(NestedUnion u);
        public void nested_union_ptr_union_set_double(NestedUnion u, double v);

        public Pointer nested_union_ptr_union_get_pointer(NestedUnion u);
        public void nested_union_ptr_union_set_pointer(NestedUnion u, Pointer v);

        public int nested_union_size();
    }

    @BeforeAll
    public static void beforeAll() {
        lib = TstUtil.loadTestLib(Lib.class);
        runtime = Runtime.getRuntime(lib);
    }

    // ========================= Inner Struct =======================

    @Test
    public void testInnerStructByte() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((byte) 0, union.inner_NumericStruct.val_int8_t.get());

            byte value = (byte) R.nextInt();
            union.inner_NumericStruct.val_int8_t.set(value);

            assertEquals((byte) value, union.inner_NumericStruct.val_int8_t.get(),
                    "Incorrect byte value in inner struct");
            assertEquals((byte) value, lib.nested_union_inner_struct_get_int8_t(union),
                    "Incorrect byte value in inner struct");

            value = (byte) R.nextInt();
            lib.nested_union_inner_struct_set_int8_t(union, value);

            assertEquals((byte) value, union.inner_NumericStruct.val_int8_t.get(),
                    "Incorrect byte value in inner struct");
            assertEquals((byte) value, lib.nested_union_inner_struct_get_int8_t(union),
                    "Incorrect byte value in inner struct");

            union.inner_NumericStruct.reset();
        }
    }

    @Test
    public void testInnerStructLong() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((long) 0, union.inner_NumericStruct.val_int64_t.get());

            long value = (long) R.nextLong();
            union.inner_NumericStruct.val_int64_t.set(value);

            assertEquals((long) value, union.inner_NumericStruct.val_int64_t.get(),
                    "Incorrect long value in inner struct");
            assertEquals((long) value, lib.nested_union_inner_struct_get_int64_t(union),
                    "Incorrect long value in inner struct");

            value = (long) R.nextLong();
            lib.nested_union_inner_struct_set_int64_t(union, value);

            assertEquals((long) value, union.inner_NumericStruct.val_int64_t.get(),
                    "Incorrect long value in inner struct");
            assertEquals((long) value, lib.nested_union_inner_struct_get_int64_t(union),
                    "Incorrect long value in inner struct");

            union.inner_NumericStruct.reset();
        }
    }

    @Test
    public void testInnerStructFloat() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((float) 0, union.inner_NumericStruct.val_float.get());

            float value = (float) R.nextFloat();
            union.inner_NumericStruct.val_float.set(value);

            assertEquals((float) value, union.inner_NumericStruct.val_float.get(),
                    "Incorrect float value in inner struct");
            assertEquals((float) value, lib.nested_union_inner_struct_get_float(union),
                    "Incorrect float value in inner struct");

            value = (float) R.nextFloat();
            lib.nested_union_inner_struct_set_float(union, value);

            assertEquals((float) value, union.inner_NumericStruct.val_float.get(),
                    "Incorrect float value in inner struct");
            assertEquals((float) value, lib.nested_union_inner_struct_get_float(union),
                    "Incorrect float value in inner struct");

            union.inner_NumericStruct.reset();
        }
    }

    @Test
    public void testInnerStructDouble() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((double) 0, union.inner_NumericStruct.val_double.get());

            double value = (double) R.nextDouble();
            union.inner_NumericStruct.val_double.set(value);

            assertEquals((double) value, union.inner_NumericStruct.val_double.get(),
                    "Incorrect double value in inner struct");
            assertEquals((double) value, lib.nested_union_inner_struct_get_double(union),
                    "Incorrect double value in inner struct");

            value = (double) R.nextDouble();
            lib.nested_union_inner_struct_set_double(union, value);

            assertEquals((double) value, union.inner_NumericStruct.val_double.get(),
                    "Incorrect double value in inner struct");
            assertEquals((double) value, lib.nested_union_inner_struct_get_double(union),
                    "Incorrect double value in inner struct");

            union.inner_NumericStruct.reset();
        }
    }

    @Test
    public void testInnerStructPointer() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(union.inner_NumericStruct.val_pointer.get());

            long value = (long) R.nextLong(Long.MAX_VALUE);
            union.inner_NumericStruct.val_pointer.set(value);

            assertEquals((long) value, union.inner_NumericStruct.val_pointer.get().address(),
                    "Incorrect pointer address in inner struct");
            assertEquals((long) value, lib.nested_union_inner_struct_get_pointer(union).address(),
                    "Incorrect pointer address in inner struct");

            value = (long) R.nextLong(Long.MAX_VALUE);
            lib.nested_union_inner_struct_set_pointer(union, Pointer.wrap(runtime, value));

            assertEquals((long) value, union.inner_NumericStruct.val_pointer.get().address(),
                    "Incorrect pointer address in inner struct");
            assertEquals((long) value, lib.nested_union_inner_struct_get_pointer(union).address(),
                    "Incorrect pointer address value in inner struct");

            union.inner_NumericStruct.reset();
        }
    }

    // ========================= Pointer Struct =====================

    // Copied from NestedStructTest
    // TODO: 19-Jan-2022 @basshelal: StructRef doesn't use direct memory by default meaning directly getting the
    //  backed struct and changing it doesn't do anything, we need to set the structRef's backing pointer to a
    //  direct pointer from Memory.allocateDirect()
    //  this:
    //  struct.ptr_NumericStruct.get().val_int8_t.set(69);
    //  struct.ptr_NumericStruct.get().val_int8_t.get() == 69; // fails without above Memory.allocateDirect()
    //  because everytime get(): Struct is called an entirely new struct is created on the JVM and it's backing memory
    //  isn't direct memory by default.
    //  I tried fixing this in different ways in Struct but all failed for different reasons,
    //  the code is too messy to fix this and too many changes would have to be made to make this work
    //  This is something we need to fix (or at worst document) but only after we have a good test suite to ensure that
    //  any changes made to fixing this won't break something else

    @Test
    public void testPointerStructByte() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericStruct.set(Memory.allocateDirect(runtime, Struct.size(NumericStruct.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((byte) 0, union.ptr_NumericStruct.get().val_int8_t.get());

            byte value = (byte) R.nextInt();
            union.ptr_NumericStruct.get().val_int8_t.set(value);

            assertEquals((byte) value, union.ptr_NumericStruct.get().val_int8_t.get(),
                    "Incorrect byte value in pointer struct");
            assertEquals((byte) value, lib.nested_union_ptr_struct_get_int8_t(union),
                    "Incorrect byte value in pointer struct");

            value = (byte) R.nextInt();
            lib.nested_union_ptr_struct_set_int8_t(union, value);

            assertEquals((byte) value, union.ptr_NumericStruct.get().val_int8_t.get(),
                    "Incorrect byte value in pointer struct");
            assertEquals((byte) value, lib.nested_union_ptr_struct_get_int8_t(union),
                    "Incorrect byte value in pointer struct");

            union.ptr_NumericStruct.get().reset();
        }
    }

    @Test
    public void testPointerStructLong() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericStruct.set(Memory.allocateDirect(runtime, Struct.size(NumericStruct.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((long) 0, union.ptr_NumericStruct.get().val_int64_t.get());

            long value = (long) R.nextLong();
            union.ptr_NumericStruct.get().val_int64_t.set(value);

            assertEquals((long) value, union.ptr_NumericStruct.get().val_int64_t.get(),
                    "Incorrect long value in pointer struct");
            assertEquals((long) value, lib.nested_union_ptr_struct_get_int64_t(union),
                    "Incorrect long value in pointer struct");

            value = (long) R.nextLong();
            lib.nested_union_ptr_struct_set_int64_t(union, value);

            assertEquals((long) value, union.ptr_NumericStruct.get().val_int64_t.get(),
                    "Incorrect long value in pointer struct");
            assertEquals((long) value, lib.nested_union_ptr_struct_get_int64_t(union),
                    "Incorrect long value in pointer struct");

            union.ptr_NumericStruct.get().reset();
        }
    }

    @Test
    public void testPointerStructFloat() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericStruct.set(Memory.allocateDirect(runtime, Struct.size(NumericStruct.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((float) 0, union.ptr_NumericStruct.get().val_float.get());

            float value = (float) R.nextFloat();
            union.ptr_NumericStruct.get().val_float.set(value);

            assertEquals((float) value, union.ptr_NumericStruct.get().val_float.get(),
                    "Incorrect float value in pointer struct");
            assertEquals((float) value, lib.nested_union_ptr_struct_get_float(union),
                    "Incorrect float value in pointer struct");

            value = (float) R.nextFloat();
            lib.nested_union_ptr_struct_set_float(union, value);

            assertEquals((float) value, union.ptr_NumericStruct.get().val_float.get(),
                    "Incorrect float value in pointer struct");
            assertEquals((float) value, lib.nested_union_ptr_struct_get_float(union),
                    "Incorrect float value in pointer struct");

            union.ptr_NumericStruct.get().reset();
        }
    }

    @Test
    public void testPointerStructDouble() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericStruct.set(Memory.allocateDirect(runtime, Struct.size(NumericStruct.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((double) 0, union.ptr_NumericStruct.get().val_double.get());

            double value = (double) R.nextDouble();
            union.ptr_NumericStruct.get().val_double.set(value);

            assertEquals((double) value, union.ptr_NumericStruct.get().val_double.get(),
                    "Incorrect double value in pointer struct");
            assertEquals((double) value, lib.nested_union_ptr_struct_get_double(union),
                    "Incorrect double value in pointer struct");

            value = (double) R.nextDouble();
            lib.nested_union_ptr_struct_set_double(union, value);

            assertEquals((double) value, union.ptr_NumericStruct.get().val_double.get(),
                    "Incorrect double value in pointer struct");
            assertEquals((double) value, lib.nested_union_ptr_struct_get_double(union),
                    "Incorrect double value in pointer struct");

            union.ptr_NumericStruct.get().reset();
        }
    }

    @Test
    public void testPointerStructPointer() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericStruct.set(Memory.allocateDirect(runtime, Struct.size(NumericStruct.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(union.ptr_NumericStruct.get().val_pointer.get());

            long value = (long) R.nextLong(Long.MAX_VALUE);
            union.ptr_NumericStruct.get().val_pointer.set(value);

            assertEquals((long) value, union.ptr_NumericStruct.get().val_pointer.get().address(),
                    "Incorrect pointer address in pointer struct");
            assertEquals((long) value, lib.nested_union_ptr_struct_get_pointer(union).address(),
                    "Incorrect pointer address in pointer struct");

            value = (long) R.nextLong(Long.MAX_VALUE);
            lib.nested_union_ptr_struct_set_pointer(union, Pointer.wrap(runtime, value));

            assertEquals((long) value, union.ptr_NumericStruct.get().val_pointer.get().address(),
                    "Incorrect pointer address in pointer struct");
            assertEquals((long) value, lib.nested_union_ptr_struct_get_pointer(union).address(),
                    "Incorrect pointer address value in pointer struct");

            union.ptr_NumericStruct.get().reset();
        }
    }

    // ========================= Inner Union ========================

    @Test
    public void testInnerUnionByte() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((byte) 0, union.inner_NumericUnion.val_int8_t.get());

            byte value = (byte) R.nextInt();
            union.inner_NumericUnion.val_int8_t.set(value);

            assertEquals((byte) value, union.inner_NumericUnion.val_int8_t.get(),
                    "Incorrect byte value in inner union");
            assertEquals((byte) value, lib.nested_union_inner_union_get_int8_t(union),
                    "Incorrect byte value in inner union");

            value = (byte) R.nextInt();
            lib.nested_union_inner_union_set_int8_t(union, value);

            assertEquals((byte) value, union.inner_NumericUnion.val_int8_t.get(),
                    "Incorrect byte value in inner union");
            assertEquals((byte) value, lib.nested_union_inner_union_get_int8_t(union),
                    "Incorrect byte value in inner union");

            union.inner_NumericUnion.reset();
        }
    }

    @Test
    public void testInnerUnionLong() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((long) 0, union.inner_NumericUnion.val_int64_t.get());

            long value = (long) R.nextLong();
            union.inner_NumericUnion.val_int64_t.set(value);

            assertEquals((long) value, union.inner_NumericUnion.val_int64_t.get(),
                    "Incorrect long value in inner union");
            assertEquals((long) value, lib.nested_union_inner_union_get_int64_t(union),
                    "Incorrect long value in inner union");

            value = (long) R.nextLong();
            lib.nested_union_inner_union_set_int64_t(union, value);

            assertEquals((long) value, union.inner_NumericUnion.val_int64_t.get(),
                    "Incorrect long value in inner union");
            assertEquals((long) value, lib.nested_union_inner_union_get_int64_t(union),
                    "Incorrect long value in inner union");

            union.inner_NumericUnion.reset();
        }
    }

    @Test
    public void testInnerUnionFloat() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((float) 0, union.inner_NumericUnion.val_float.get());

            float value = (float) R.nextFloat();
            union.inner_NumericUnion.val_float.set(value);

            assertEquals((float) value, union.inner_NumericUnion.val_float.get(),
                    "Incorrect float value in inner union");
            assertEquals((float) value, lib.nested_union_inner_union_get_float(union),
                    "Incorrect float value in inner union");

            value = (float) R.nextFloat();
            lib.nested_union_inner_union_set_float(union, value);

            assertEquals((float) value, union.inner_NumericUnion.val_float.get(),
                    "Incorrect float value in inner union");
            assertEquals((float) value, lib.nested_union_inner_union_get_float(union),
                    "Incorrect float value in inner union");

            union.inner_NumericUnion.reset();
        }
    }

    @Test
    public void testInnerUnionDouble() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((double) 0, union.inner_NumericUnion.val_double.get());

            double value = (double) R.nextDouble();
            union.inner_NumericUnion.val_double.set(value);

            assertEquals((double) value, union.inner_NumericUnion.val_double.get(),
                    "Incorrect double value in inner union");
            assertEquals((double) value, lib.nested_union_inner_union_get_double(union),
                    "Incorrect double value in inner union");

            value = (double) R.nextDouble();
            lib.nested_union_inner_union_set_double(union, value);

            assertEquals((double) value, union.inner_NumericUnion.val_double.get(),
                    "Incorrect double value in inner union");
            assertEquals((double) value, lib.nested_union_inner_union_get_double(union),
                    "Incorrect double value in inner union");

            union.inner_NumericUnion.reset();
        }
    }

    @Test
    public void testInnerUnionPointer() {
        NestedUnion union = new NestedUnion();
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(union.inner_NumericUnion.val_pointer.get());

            long value = (long) R.nextLong(Long.MAX_VALUE);
            union.inner_NumericUnion.val_pointer.set(value);

            assertEquals((long) value, union.inner_NumericUnion.val_pointer.get().address(),
                    "Incorrect pointer address in inner union");
            assertEquals((long) value, lib.nested_union_inner_union_get_pointer(union).address(),
                    "Incorrect pointer address in inner union");

            value = (long) R.nextLong(Long.MAX_VALUE);
            lib.nested_union_inner_union_set_pointer(union, Pointer.wrap(runtime, value));

            assertEquals((long) value, union.inner_NumericUnion.val_pointer.get().address(),
                    "Incorrect pointer address in inner union");
            assertEquals((long) value, lib.nested_union_inner_union_get_pointer(union).address(),
                    "Incorrect pointer address value in inner union");

            union.inner_NumericUnion.reset();
        }
    }

    // ========================= Pointer Union ======================

    @Test
    public void testPointerUnionByte() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericUnion.set(Memory.allocateDirect(runtime, Struct.size(NumericUnion.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((byte) 0, union.ptr_NumericUnion.get().val_int8_t.get());

            byte value = (byte) R.nextInt();
            union.ptr_NumericUnion.get().val_int8_t.set(value);

            assertEquals((byte) value, union.ptr_NumericUnion.get().val_int8_t.get(),
                    "Incorrect byte value in pointer union");
            assertEquals((byte) value, lib.nested_union_ptr_union_get_int8_t(union),
                    "Incorrect byte value in pointer union");

            value = (byte) R.nextInt();
            lib.nested_union_ptr_union_set_int8_t(union, value);

            assertEquals((byte) value, union.ptr_NumericUnion.get().val_int8_t.get(),
                    "Incorrect byte value in pointer union");
            assertEquals((byte) value, lib.nested_union_ptr_union_get_int8_t(union),
                    "Incorrect byte value in pointer union");

            union.ptr_NumericUnion.get().reset();
        }
    }

    @Test
    public void testPointerUnionLong() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericUnion.set(Memory.allocateDirect(runtime, Struct.size(NumericUnion.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((long) 0, union.ptr_NumericUnion.get().val_int64_t.get());

            long value = (long) R.nextLong();
            union.ptr_NumericUnion.get().val_int64_t.set(value);

            assertEquals((long) value, union.ptr_NumericUnion.get().val_int64_t.get(),
                    "Incorrect long value in pointer union");
            assertEquals((long) value, lib.nested_union_ptr_union_get_int64_t(union),
                    "Incorrect long value in pointer union");

            value = (long) R.nextLong();
            lib.nested_union_ptr_union_set_int64_t(union, value);

            assertEquals((long) value, union.ptr_NumericUnion.get().val_int64_t.get(),
                    "Incorrect long value in pointer union");
            assertEquals((long) value, lib.nested_union_ptr_union_get_int64_t(union),
                    "Incorrect long value in pointer union");

            union.ptr_NumericUnion.get().reset();
        }
    }

    @Test
    public void testPointerUnionFloat() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericUnion.set(Memory.allocateDirect(runtime, Struct.size(NumericUnion.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((float) 0, union.ptr_NumericUnion.get().val_float.get());

            float value = (float) R.nextFloat();
            union.ptr_NumericUnion.get().val_float.set(value);

            assertEquals((float) value, union.ptr_NumericUnion.get().val_float.get(),
                    "Incorrect float value in pointer union");
            assertEquals((float) value, lib.nested_union_ptr_union_get_float(union),
                    "Incorrect float value in pointer union");

            value = (float) R.nextFloat();
            lib.nested_union_ptr_union_set_float(union, value);

            assertEquals((float) value, union.ptr_NumericUnion.get().val_float.get(),
                    "Incorrect float value in pointer union");
            assertEquals((float) value, lib.nested_union_ptr_union_get_float(union),
                    "Incorrect float value in pointer union");

            union.ptr_NumericUnion.get().reset();
        }
    }

    @Test
    public void testPointerUnionDouble() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericUnion.set(Memory.allocateDirect(runtime, Struct.size(NumericUnion.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertEquals((double) 0, union.ptr_NumericUnion.get().val_double.get());

            double value = (double) R.nextDouble();
            union.ptr_NumericUnion.get().val_double.set(value);

            assertEquals((double) value, union.ptr_NumericUnion.get().val_double.get(),
                    "Incorrect double value in pointer union");
            assertEquals((double) value, lib.nested_union_ptr_union_get_double(union),
                    "Incorrect double value in pointer union");

            value = (double) R.nextDouble();
            lib.nested_union_ptr_union_set_double(union, value);

            assertEquals((double) value, union.ptr_NumericUnion.get().val_double.get(),
                    "Incorrect double value in pointer union");
            assertEquals((double) value, lib.nested_union_ptr_union_get_double(union),
                    "Incorrect double value in pointer union");

            union.ptr_NumericUnion.get().reset();
        }
    }

    @Test
    public void testPointerUnionPointer() {
        NestedUnion union = new NestedUnion();
        union.ptr_NumericUnion.set(Memory.allocateDirect(runtime, Struct.size(NumericUnion.class)));
        for (int i = 0; i < ITERATIONS_COUNT; i++) {
            assertNull(union.ptr_NumericUnion.get().val_pointer.get());

            long value = (long) R.nextLong(Long.MAX_VALUE);
            union.ptr_NumericUnion.get().val_pointer.set(value);

            assertEquals((long) value, union.ptr_NumericUnion.get().val_pointer.get().address(),
                    "Incorrect pointer address in pointer union");
            assertEquals((long) value, lib.nested_union_ptr_union_get_pointer(union).address(),
                    "Incorrect pointer address in pointer union");

            value = (long) R.nextLong(Long.MAX_VALUE);
            lib.nested_union_ptr_union_set_pointer(union, Pointer.wrap(runtime, value));

            assertEquals((long) value, union.ptr_NumericUnion.get().val_pointer.get().address(),
                    "Incorrect pointer address in pointer union");
            assertEquals((long) value, lib.nested_union_ptr_union_get_pointer(union).address(),
                    "Incorrect pointer address value in pointer union");

            union.ptr_NumericUnion.get().reset();
        }
    }

    // ========================= Other ==============================

    @Test
    public void testSize() {
        NestedUnion union = new NestedUnion();
        assertEquals(Struct.size(union), lib.nested_union_size(),
                "Incorrect union size");
    }
}
