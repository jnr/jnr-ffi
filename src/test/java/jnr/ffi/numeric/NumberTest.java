package jnr.ffi.numeric;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import jnr.ffi.TstUtil;
import jnr.ffi.numeric.utils.BooleanNumberTestLib;
import jnr.ffi.numeric.utils.NumberTestLib;

import static jnr.ffi.numeric.utils.NumberOps.doubleLoop;
import static jnr.ffi.numeric.utils.NumberOps.doubleReturnLoop;
import static jnr.ffi.numeric.utils.NumberOps.floatLoop;
import static jnr.ffi.numeric.utils.NumberOps.floatReturnLoop;
import static jnr.ffi.numeric.utils.NumberOps.rangesLoop;
import static jnr.ffi.numeric.utils.NumberOps.returnLoop;
import static jnr.ffi.numeric.utils.NumberUtils.box;
import static jnr.ffi.numeric.utils.NumberUtils.nativeLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that Native and Java numerical operations match
 * This uses the native functions from NumberTest.c
 */
@SuppressWarnings("RedundantCast") // Better to be explicit with which primitive type you want to use
public class NumberTest {

    private static NumberTestLib testlib;
    private static BooleanNumberTestLib testBoolean;

    @BeforeAll
    public static void beforeAll() {
        testlib = TstUtil.loadTestLib(NumberTestLib.class);
        testBoolean = TstUtil.loadTestLib(BooleanNumberTestLib.class);
    }

    /*
     * The testing strategy we use to test arithmetic operations is essentially
     * Boundary Value Analysis, we use 3 ranges for each number type:
     * low: uses min value (like Byte.MIN_VALUE) but starts less than that
     *      technically an underflow and goes beyond it a little, so that we end
     *      up testing beyond and within the bounds
     * normal: uses 0 but similarly starts slightly below it and goes slightly above it
     *         like -16 until +16
     * high: likewise but using the max value (like Byte.MAX_VALUE) starting from
     *       slightly less than the value and overflowing to slightly more than it
     *
     * For arithmetic operations that require 2 arguments we test all the possibilities
     * within these 3 ranges together to get a very large sample size but isn't ridiculously
     * large as to end up taking minutes, so we end up with 9 possible combinations.
     *
     * For return operations (that just return the passed in single argument) we only
     * loop once, thus ending up with only the 3 ranges.
     */

    // ========================= Byte ===============================

    @Test
    public void testByteAddition() {
        rangesLoop((byte i1, byte i2) -> {
            assertEquals((byte) (i1 + i2), testlib.add_int8_t(i1, i2),
                    "byte addition failed for " + i1 + " + " + i2);
            assertEquals(box((byte) (i1 + i2)), testlib.add_int8_t(box(i1), box(i2)),
                    "boxed byte addition failed for " + i1 + " + " + i2);
        });
    }

    @Test
    public void testByteSubtraction() {
        rangesLoop((byte i1, byte i2) -> {
            assertEquals((byte) (i1 - i2), testlib.sub_int8_t(i1, i2),
                    "byte subtraction failed for " + i1 + " - " + i2);
            assertEquals(box((byte) (i1 - i2)), testlib.sub_int8_t(box(i1), box(i2)),
                    "boxed byte subtraction failed for " + i1 + " - " + i2);
        });
    }

    @Test
    public void testByteMultiplication() {
        rangesLoop((byte i1, byte i2) -> {
            assertEquals((byte) (i1 * i2), testlib.mul_int8_t(i1, i2),
                    "byte multiplication failed for " + i1 + " * " + i2);
            assertEquals(box((byte) (i1 * i2)), testlib.mul_int8_t(box(i1), box(i2)),
                    "boxed byte multiplication failed for " + i1 + " * " + i2);
        });
    }

    @Test
    public void testByteDivision() {
        rangesLoop((byte i1, byte i2) -> {
            if (i2 != 0) { // avoid division by zero!
                assertEquals((byte) (i1 / i2), testlib.div_int8_t(i1, i2),
                        "byte division failed for " + i1 + " / " + i2);
                assertEquals(box((byte) (i1 / i2)), testlib.div_int8_t(box(i1), box(i2)),
                        "boxed byte division failed for " + i1 + " / " + i2);
            }
        });
    }

    @Test
    public void testByteReturn() {
        returnLoop((byte i1) -> {
            assertEquals((byte) i1, testlib.ret_int8_t(i1),
                    "byte return failed for " + i1);
            assertEquals(box((byte) i1), testlib.ret_int8_t(box(i1)),
                    "boxed byte return failed for " + i1);
        });
    }

    // ========================= Short ==============================

    @Test
    public void testShortAddition() {
        rangesLoop((short i1, short i2) -> {
            assertEquals((short) (i1 + i2), testlib.add_int16_t(i1, i2),
                    "short addition failed for " + i1 + " + " + i2);
            assertEquals(box((short) (i1 + i2)), testlib.add_int16_t(box(i1), box(i2)),
                    "boxed short addition failed for " + i1 + " + " + i2);
        });
    }

    @Test
    public void testShortSubtraction() {
        rangesLoop((short i1, short i2) -> {
            assertEquals((short) (i1 - i2), testlib.sub_int16_t(i1, i2),
                    "short subtraction failed for " + i1 + " - " + i2);
            assertEquals(box((short) (i1 - i2)), testlib.sub_int16_t(box(i1), box(i2)),
                    "boxed short subtraction failed for " + i1 + " - " + i2);
        });
    }

    @Test
    public void testShortMultiplication() {
        rangesLoop((short i1, short i2) -> {
            assertEquals((short) (i1 * i2), testlib.mul_int16_t(i1, i2),
                    "short multiplication failed for " + i1 + " * " + i2);
            assertEquals(box((short) (i1 * i2)), testlib.mul_int16_t(box(i1), box(i2)),
                    "boxed short multiplication failed for " + i1 + " * " + i2);
        });
    }

    @Test
    public void testShortDivision() {
        rangesLoop((short i1, short i2) -> {
            if (i2 != 0) { // avoid division by zero!
                assertEquals((short) (i1 / i2), testlib.div_int16_t(i1, i2),
                        "short division failed for " + i1 + " / " + i2);
                assertEquals(box((short) (i1 / i2)), testlib.div_int16_t(box(i1), box(i2)),
                        "boxed short division failed for " + i1 + " / " + i2);
            }
        });
    }

    @Test
    public void testShortReturn() {
        returnLoop((short i1) -> {
            assertEquals((short) i1, testlib.ret_int16_t(i1),
                    "short return failed for " + i1);
            assertEquals(box((short) i1), testlib.ret_int16_t(box(i1)),
                    "boxed short return failed for " + i1);
        });
    }

    // ========================= Int ================================

    @Test
    public void testIntAddition() {
        rangesLoop((int i1, int i2) -> {
            assertEquals((int) (i1 + i2), testlib.add_int32_t(i1, i2),
                    "int addition failed for " + i1 + " + " + i2);
            assertEquals(box((int) (i1 + i2)), testlib.add_int32_t(box(i1), box(i2)),
                    "boxed int addition failed for " + i1 + " + " + i2);
        });
    }

    @Test
    public void testIntSubtraction() {
        rangesLoop((int i1, int i2) -> {
            assertEquals((int) (i1 - i2), testlib.sub_int32_t(i1, i2),
                    "int subtraction failed for " + i1 + " - " + i2);
            assertEquals(box((int) (i1 - i2)), testlib.sub_int32_t(box(i1), box(i2)),
                    "boxed int subtraction failed for " + i1 + " - " + i2);
        });
    }

    @Test
    public void testIntMultiplication() {
        rangesLoop((int i1, int i2) -> {
            assertEquals((int) (i1 * i2), testlib.mul_int32_t(i1, i2),
                    "int multiplication failed for " + i1 + " * " + i2);
            assertEquals(box((int) (i1 * i2)), testlib.mul_int32_t(box(i1), box(i2)),
                    "boxed int multiplication failed for " + i1 + " * " + i2);
        });
    }

    @Test
    public void testIntDivision() {
        rangesLoop((int i1, int i2) -> {
            if (i2 != 0 && i2 != -1) { // avoid division by zero!
                // -1 causes native SIGFPE when doing -2147483648 / -1 because
                // result (2147483648) would be beyond int32 limits (MAX: 2147483647)
                // strange because this doesn't happen with the smaller number types
                assertEquals((int) (i1 / i2), testlib.div_int32_t(i1, i2),
                        "int division failed for " + i1 + " / " + i2);
                assertEquals(box((int) (i1 / i2)), testlib.div_int32_t(box(i1), box(i2)),
                        "boxed int division failed for " + i1 + " / " + i2);
            }
        });
    }

    @Test
    public void testIntReturn() {
        returnLoop((int i1) -> {
            assertEquals((int) i1, testlib.ret_int32_t(i1),
                    "int return failed for " + i1);
            assertEquals(box((int) i1), testlib.ret_int32_t(box(i1)),
                    "boxed int return failed for " + i1);
        });
    }

    // ========================= NativeLong =========================

    /*
     * NativeLongs are in C `long` which are variable size depending on platform,
     * 32bit on 32bit platforms and 64bit on 64bit platforms.
     * We also test JNR-FFI NativeLong as a mapping in addition to the Java primitive and boxed
     * type (long and Long respectively) as usual
     */

    @Test
    public void testNativeLongAddition() {
        rangesLoop((long i1, long i2) -> {
            assertEquals((long) (i1 + i2), testlib.add_long(i1, i2),
                    "native long addition failed for " + i1 + " + " + i2);
            assertEquals(box((long) (i1 + i2)), testlib.add_long(box(i1), box(i2)),
                    "boxed native long addition failed for " + i1 + " + " + i2);
            assertEquals(nativeLong((long) (i1 + i2)), testlib.add_long(nativeLong(i1), nativeLong(i2)),
                    "jnr boxed native long addition failed for " + i1 + " + " + i2);
        });
    }

    @Test
    public void testNativeLongSubtraction() {
        rangesLoop((long i1, long i2) -> {
            assertEquals((long) (i1 - i2), testlib.sub_long(i1, i2),
                    "native long subtraction failed for " + i1 + " - " + i2);
            assertEquals(box((long) (i1 - i2)), testlib.sub_long(box(i1), box(i2)),
                    "boxed native long subtraction failed for " + i1 + " - " + i2);
            assertEquals(nativeLong((long) (i1 - i2)), testlib.sub_long(nativeLong(i1), nativeLong(i2)),
                    "jnr boxed native long subtraction failed for " + i1 + " - " + i2);
        });
    }

    @Test
    public void testNativeLongMultiplication() {
        rangesLoop((long i1, long i2) -> {
            assertEquals((long) (i1 * i2), testlib.mul_long(i1, i2),
                    "native long multiplication failed for " + i1 + " * " + i2);
            assertEquals(box((long) (i1 * i2)), testlib.mul_long(box(i1), box(i2)),
                    "boxed native long multiplication failed for " + i1 + " * " + i2);
            assertEquals(nativeLong((long) (i1 * i2)), testlib.mul_long(nativeLong(i1), nativeLong(i2)),
                    "jnr boxed native long multiplication failed for " + i1 + " * " + i2);
        });
    }

    @Test
    public void testNativeLongDivision() {
        rangesLoop((long i1, long i2) -> {
            if (i2 != 0 && i2 != -1) { // avoid division by zero!
                // -1 shouldn't fail with long because our range is different from int32 but
                // keeping it in case we make it similar in the future
                assertEquals((long) (i1 / i2), testlib.div_long(i1, i2),
                        "native long division failed for " + i1 + " / " + i2);
                assertEquals(box((long) (i1 / i2)), testlib.div_long(box(i1), box(i2)),
                        "boxed native long division failed for " + i1 + " / " + i2);
                assertEquals(nativeLong((long) (i1 / i2)), testlib.div_long(nativeLong(i1), nativeLong(i2)),
                        "jnr boxed native long division failed for " + i1 + " / " + i2);
            }
        });
    }

    @Test
    public void testNativeLongReturn() {
        returnLoop((long i1) -> {
            assertEquals((long) i1, testlib.ret_long(i1),
                    "native long return failed for " + i1);
            assertEquals(box((long) i1), testlib.ret_long(box(i1)),
                    "boxed native long return failed for " + i1);
            assertEquals(nativeLong((long) i1), testlib.ret_long(nativeLong(i1)),
                    "jnr boxed native long return failed for " + i1);
        });
    }

    // ========================= LongLong ===========================

    @Test
    public void testLongAddition() {
        rangesLoop((long i1, long i2) -> {
            assertEquals((long) (i1 + i2), testlib.add_int64_t(i1, i2),
                    "long addition failed for " + i1 + " + " + i2);
            assertEquals(box((long) (i1 + i2)), testlib.add_int64_t(box(i1), box(i2)),
                    "boxed long addition failed for " + i1 + " + " + i2);
        });
    }

    @Test
    public void testLongSubtraction() {
        rangesLoop((long i1, long i2) -> {
            assertEquals((long) (i1 - i2), testlib.sub_int64_t(i1, i2),
                    "long subtraction failed for " + i1 + " - " + i2);
            assertEquals(box((long) (i1 - i2)), testlib.sub_int64_t(box(i1), box(i2)),
                    "boxed long subtraction failed for " + i1 + " - " + i2);
        });
    }

    @Test
    public void testLongMultiplication() {
        rangesLoop((long i1, long i2) -> {
            assertEquals((long) (i1 * i2), testlib.mul_int64_t(i1, i2),
                    "long multiplication failed for " + i1 + " * " + i2);
            assertEquals(box((long) (i1 * i2)), testlib.mul_int64_t(box(i1), box(i2)),
                    "boxed long multiplication failed for " + i1 + " * " + i2);
        });
    }

    @Test
    public void testLongDivision() {
        rangesLoop((long i1, long i2) -> {
            if (i2 != 0 && i2 != -1) { // avoid division by zero!
                // -1 shouldn't fail with long because our range is different from int32 but
                // keeping it in case we make it similar in the future
                assertEquals((long) (i1 / i2), testlib.div_int64_t(i1, i2),
                        "long division failed for " + i1 + " / " + i2);
                assertEquals(box((long) (i1 / i2)), testlib.div_int64_t(box(i1), box(i2)),
                        "boxed long division failed for " + i1 + " / " + i2);
            }
        });
    }

    @Test
    public void testLongReturn() {
        returnLoop((long i1) -> {
            assertEquals((long) i1, testlib.ret_int64_t(i1),
                    "long return failed for " + i1);
            assertEquals(box((long) i1), testlib.ret_int64_t(box(i1)),
                    "boxed long return failed for " + i1);
        });
    }

    /*
     * Floats and Doubles are tested by looping over a large number of times and using random
     * numbers as test arguments since bounds testing is not an option here unlike with the
     * other number types
     */

    // ========================= Float ==============================

    @Test
    public void testFloatAddition() {
        floatLoop((float i1, float i2) -> {
            assertEquals((float) (i1 + i2), testlib.add_float(i1, i2),
                    "float addition failed for " + i1 + " + " + i2);
            assertEquals(box((float) (i1 + i2)), testlib.add_float(box(i1), box(i2)),
                    "boxed float addition failed for " + i1 + " + " + i2);
        });
    }

    @Test
    public void testFloatSubtraction() {
        floatLoop((float i1, float i2) -> {
            assertEquals((float) (i1 - i2), testlib.sub_float(i1, i2),
                    "float subtraction failed for " + i1 + " - " + i2);
            assertEquals(box((float) (i1 - i2)), testlib.sub_float(box(i1), box(i2)),
                    "boxed float subtraction failed for " + i1 + " - " + i2);
        });
    }

    @Test
    public void testFloatMultiplication() {
        floatLoop((float i1, float i2) -> {
            assertEquals((float) (i1 * i2), testlib.mul_float(i1, i2),
                    "float multiplication failed for " + i1 + " * " + i2);
            assertEquals(box((float) (i1 * i2)), testlib.mul_float(box(i1), box(i2)),
                    "boxed float multiplication failed for " + i1 + " * " + i2);
        });
    }

    @Test
    public void testFloatDivision() {
        floatLoop((float i1, float i2) -> {
            if (i2 != 0.0f) { // avoid division by zero!
                assertEquals((float) (i1 / i2), testlib.div_float(i1, i2),
                        "float division failed for " + i1 + " / " + i2);
                assertEquals(box((float) (i1 / i2)), testlib.div_float(box(i1), box(i2)),
                        "boxed float division failed for " + i1 + " / " + i2);
            }
        });
    }

    @Test
    public void testFloatReturn() {
        floatReturnLoop((float i1) -> {
            assertEquals((float) i1, testlib.ret_float(i1),
                    "float return failed for " + i1);
            assertEquals(box((float) i1), testlib.ret_float(box(i1)),
                    "boxed float return failed for " + i1);
        });
    }

    // ========================= Double =============================

    @Test
    public void testDoubleAddition() {
        doubleLoop((double i1, double i2) -> {
            assertEquals((double) (i1 + i2), testlib.add_double(i1, i2),
                    "double addition failed for " + i1 + " + " + i2);
            assertEquals(box((double) (i1 + i2)), testlib.add_double(box(i1), box(i2)),
                    "boxed double addition failed for " + i1 + " + " + i2);
        });
    }

    @Test
    public void testDoubleSubtraction() {
        doubleLoop((double i1, double i2) -> {
            assertEquals((double) (i1 - i2), testlib.sub_double(i1, i2),
                    "double subtraction failed for " + i1 + " - " + i2);
            assertEquals(box((double) (i1 - i2)), testlib.sub_double(box(i1), box(i2)),
                    "boxed double subtraction failed for " + i1 + " - " + i2);
        });
    }

    @Test
    public void testDoubleMultiplication() {
        doubleLoop((double i1, double i2) -> {
            assertEquals((double) (i1 * i2), testlib.mul_double(i1, i2),
                    "double multiplication failed for " + i1 + " * " + i2);
            assertEquals(box((double) (i1 * i2)), testlib.mul_double(box(i1), box(i2)),
                    "boxed double multiplication failed for " + i1 + " * " + i2);
        });
    }

    @Test
    public void testDoubleDivision() {
        doubleLoop((double i1, double i2) -> {
            if (i2 != 0.0) { // avoid division by zero!
                assertEquals((double) (i1 / i2), testlib.div_double(i1, i2),
                        "double subtraction failed for " + i1 + " / " + i2);
                assertEquals(box((double) (i1 / i2)), testlib.div_double(box(i1), box(i2)),
                        "boxed double subtraction failed for " + i1 + " / " + i2);
            }
        });
    }

    @Test
    public void testDoubleReturn() {
        doubleReturnLoop((double i1) -> {
            assertEquals((double) i1, testlib.ret_double(i1),
                    "double return failed for " + i1);
            assertEquals(box((double) i1), testlib.ret_double(box(i1)),
                    "boxed double return failed for " + i1);
        });
    }

    // ========================= Signedness =========================

    /**
     * Test the signedness of numbers using a mapping larger than the expected,
     * for example for uint32 we will use a 64bit long Java mapping
     * This way it will correctly fit the numbers larger than Integer.MAX_VALUE
     * this works only with the {@link jnr.ffi.types.u_int32_t} annotation
     * and its friends.
     * Not using the correct annotation (but using the larger size) is undefined and
     * will likely behave as though you used the same size (will come back with a sign,
     * see {@link #testUnsignedNumbers()})
     */
    @Test
    public void testSignUsingExtendedMapping() {
        // give a larger bit number with upper half bits being greater than 0 (meaning sign) and expect it to come back
        // signed with all 1s (fs in hex) on upper half bits indicating positive sign
        assertEquals((short) 0xff_be, testlib.ret_int8_t((short) 0x69_be),
                "upper 8 bits not set to 1");
        assertEquals((int) 0xffff_beef, testlib.ret_int16_t((int) 0x6969_beef),
                "upper 16 bits not set to 1");
        assertEquals((long) 0xffff_ffff_deadbeefL, testlib.ret_int32_t((long) 0x6969_6969_deadbeefL),
                "upper 32 bits not set to 1");

        // give a larger bit number with upper half bits being greater than 0 (meaning sign) and expect it to come back
        // signed with all 0s on upper half bits indicating no sign
        assertEquals((short) 0x00_be, testlib.ret_uint8_t((short) 0x69_be),
                "upper 8 bits not set to 0");
        assertEquals((int) 0x0000_beef, testlib.ret_uint16_t((int) 0x6969_beef),
                "upper 16 bits not set to 0");
        assertEquals((long) 0x0000_0000_deadbeefL, testlib.ret_uint32_t((long) 0x6969_6969_deadbeefL),
                "upper 32 bits not set to 0");
    }

    /**
     * Test that when mapping a native unsigned number using the same size Java type
     * it will be returned identically, for example for a native uint32 we will use a 32bit Java int,
     * we expect even when given a negative number, it will be returned identically because
     * it is being converted along both paths, to unsigned when going to native, to signed when
     * coming to Java, thus even though the binary data will be the same, what it represents will
     * be different because of Java's lack of unsigned types
     */
    @SuppressWarnings("CodeBlock2Expr")
    @Test
    public void testUnsignedNumbers() {
        returnLoop((byte i1) -> {
            assertEquals((byte) i1, testlib.ret_uint8_t((byte) i1),
                    "unsigned byte return failed for " + i1);
        });
        returnLoop((short i1) -> {
            assertEquals((short) i1, testlib.ret_uint16_t((short) i1),
                    "unsigned short return failed for " + i1);
        });
        returnLoop((int i1) -> {
            assertEquals((int) i1, testlib.ret_uint32_t((int) i1),
                    "unsigned int return failed for " + i1);
        });
        returnLoop((long i1) -> {
            assertEquals((long) i1, testlib.ret_uint64_t((long) i1),
                    "unsigned long return failed for " + i1);
        });
    }

    // ========================= Boolean ============================

    /*
     * When we use Java booleans to map what would be regular integral functions on the native side
     * such as int8 and uint8, JNR-FFI converts 0 to `false` and anything else to `true`
     * Using floating point numbers is undefined
     */

    @Test
    public void testByteToBoolean() {
        AtomicBoolean testedZero = new AtomicBoolean(false);
        returnLoop((byte i1) -> {
            if (i1 == 0) {
                assertFalse(testBoolean.ret_int8_t(i1),
                        "0 as a byte must evaluate to false");
                assertFalse(testBoolean.ret_int8_t(box(i1)),
                        "0 as a boxed byte must evaluate to false");

                assertFalse(testBoolean.ret_uint8_t(i1),
                        "0 as an unsigned byte must evaluate to false");
                assertFalse(testBoolean.ret_uint8_t(box(i1)),
                        "0 as a boxed unsigned byte must evaluate to false");

                testedZero.set(true);
            } else {
                assertTrue(testBoolean.ret_int8_t(i1),
                        i1 + " as a byte must evaluate to true");
                assertTrue(testBoolean.ret_int8_t(box(i1)),
                        i1 + " as a boxed byte must evaluate to true");

                assertTrue(testBoolean.ret_uint8_t(i1),
                        i1 + " as an unsigned byte must evaluate to true");
                assertTrue(testBoolean.ret_uint8_t(box(i1)),
                        i1 + " as a boxed unsigned byte must evaluate to true");
            }
        });
        assertTrue(testedZero.get(), "byte to boolean test did not test 0!");
    }

    @Test
    public void testShortToBoolean() {
        AtomicBoolean testedZero = new AtomicBoolean(false);
        returnLoop((short i1) -> {
            if (i1 == 0) {
                assertFalse(testBoolean.ret_int16_t(i1),
                        "0 as a short must evaluate to false");
                assertFalse(testBoolean.ret_int16_t(box(i1)),
                        "0 as a boxed short must evaluate to false");

                assertFalse(testBoolean.ret_uint16_t(i1),
                        "0 as an unsigned short must evaluate to false");
                assertFalse(testBoolean.ret_uint16_t(box(i1)),
                        "0 as a boxed unsigned short must evaluate to false");
                testedZero.set(true);
            } else {
                assertTrue(testBoolean.ret_int16_t(i1),
                        i1 + " as a short must evaluate to true");
                assertTrue(testBoolean.ret_int16_t(box(i1)),
                        i1 + " as a boxed short must evaluate to true");

                assertTrue(testBoolean.ret_uint16_t(i1),
                        i1 + " as an unsigned short must evaluate to true");
                assertTrue(testBoolean.ret_uint16_t(box(i1)),
                        i1 + " as a boxed unsigned short must evaluate to true");
            }
        });
        assertTrue(testedZero.get(), "short to boolean test did not test 0!");
    }

    @Test
    public void testIntToBoolean() {
        AtomicBoolean testedZero = new AtomicBoolean(false);
        returnLoop((int i1) -> {
            if (i1 == 0) {
                assertFalse(testBoolean.ret_int32_t(i1),
                        "0 as a int must evaluate to false");
                assertFalse(testBoolean.ret_int32_t(box(i1)),
                        "0 as a boxed int must evaluate to false");

                assertFalse(testBoolean.ret_uint32_t(i1),
                        "0 as an unsigned int must evaluate to false");
                assertFalse(testBoolean.ret_uint32_t(box(i1)),
                        "0 as a boxed unsigned int must evaluate to false");
                testedZero.set(true);
            } else {
                assertTrue(testBoolean.ret_int32_t(i1),
                        i1 + " as a int must evaluate to true");
                assertTrue(testBoolean.ret_int32_t(box(i1)),
                        i1 + " as a boxed int must evaluate to true");

                assertTrue(testBoolean.ret_uint32_t(i1),
                        i1 + " as an unsigned int must evaluate to true");
                assertTrue(testBoolean.ret_uint32_t(box(i1)),
                        i1 + " as a boxed unsigned int must evaluate to true");
            }
        });
        assertTrue(testedZero.get(), "int to boolean test did not test 0!");
    }

    @Test
    public void testNativeLongToBoolean() {
        AtomicBoolean testedZero = new AtomicBoolean(false);
        returnLoop((long i1) -> {
            if (i1 == 0) {
                assertFalse(testBoolean.ret_long(i1),
                        "0 as a native long must evaluate to false");
                assertFalse(testBoolean.ret_long(box(i1)),
                        "0 as a boxed native long must evaluate to false");

                assertFalse(testBoolean.ret_ulong(i1),
                        "0 as an unsigned native long must evaluate to false");
                assertFalse(testBoolean.ret_ulong(box(i1)),
                        "0 as a boxed unsigned native long must evaluate to false");
                testedZero.set(true);
            } else {
                assertTrue(testBoolean.ret_long(i1),
                        i1 + " as a native long must evaluate to true");
                assertTrue(testBoolean.ret_long(box(i1)),
                        i1 + " as a boxed native long must evaluate to true");

                assertTrue(testBoolean.ret_ulong(i1),
                        i1 + " as an unsigned native long must evaluate to true");
                assertTrue(testBoolean.ret_ulong(box(i1)),
                        i1 + " as a boxed unsigned native long must evaluate to true");
            }
        });
        assertTrue(testedZero.get(), "native long to boolean test did not test 0!");
    }

    @Test
    public void testLongToBoolean() {
        AtomicBoolean testedZero = new AtomicBoolean(false);
        returnLoop((long i1) -> {
            if (i1 == 0) {
                assertFalse(testBoolean.ret_int64_t(i1),
                        "0 as a long must evaluate to false");
                assertFalse(testBoolean.ret_int64_t(box(i1)),
                        "0 as a boxed long must evaluate to false");

                assertFalse(testBoolean.ret_uint64_t(i1),
                        "0 as an unsigned long must evaluate to false");
                assertFalse(testBoolean.ret_uint64_t(box(i1)),
                        "0 as a boxed unsigned long must evaluate to false");
                testedZero.set(true);
            } else {
                assertTrue(testBoolean.ret_int64_t(i1),
                        i1 + " as a long must evaluate to true");
                assertTrue(testBoolean.ret_int64_t(box(i1)),
                        i1 + " as a boxed long must evaluate to true");

                assertTrue(testBoolean.ret_uint64_t(i1),
                        i1 + " as an unsigned long must evaluate to true");
                assertTrue(testBoolean.ret_uint64_t(box(i1)),
                        i1 + " as a boxed unsigned long must evaluate to true");
            }
        });
        assertTrue(testedZero.get(), "long to boolean test did not test 0!");
    }

    @Test
    public void testBooleanToBoolean() {
        AtomicBoolean testedZero = new AtomicBoolean(false);
        returnLoop((byte i1) -> {
            boolean b = i1 != 0;
            if (i1 == 0) {
                assertFalse(testBoolean.ret_bool(b),
                        "false must evaluate to false");
                assertFalse(testBoolean.ret_bool(box(b)),
                        "boxed false must evaluate to false");
                testedZero.set(true);
            } else {
                assertTrue(testBoolean.ret_bool(b),
                        "true must evaluate to true");
                assertTrue(testBoolean.ret_bool(box(b)),
                        "boxed true must evaluate to true");
            }
        });
        assertTrue(testedZero.get(), "boolean to boolean test did not test 0!");
    }
}
