package jnr.ffi.number;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;

import jnr.ffi.TstUtil;
import jnr.ffi.util.EnumMapper;

import static jnr.ffi.number.utils.NumberOps.returnLoop;
import static jnr.ffi.number.utils.NumberUtils.box;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that JNR-FFI converts to and from Java enums and native enums correctly
 * Native enums are nothing more than numbers (generally int32) and behave in the same way.
 */
@SuppressWarnings("RedundantCast") // Better to be explicit with which primitive type you want to use
public class EnumTest {

    /**
     * Enum without any mapping functions, uses ordinal value to determine mapping
     */
    public enum SimpleEnum {e0, e1, e2, e3}

    /**
     * Enum that uses a custom mapping function
     */
    public static enum MappedEnum implements EnumMapper.IntegerEnum {
        e0, e1, e2, e3;

        @Override
        public int intValue() {return ordinal() * 8;}
    }

    public enum BitField implements EnumMapper.IntegerEnum {
        A(0x1),
        B(0x2),
        C(0x4);

        private final int value;

        BitField(int value) {this.value = value;}

        @Override
        public int intValue() {return value;}
    }

    public static interface EnumLib {
        public SimpleEnum ret_Enum(SimpleEnum e);
        public MappedEnum ret_Enum(MappedEnum e);
    }

    public static interface NumberSimpleEnumLib {
        public SimpleEnum ret_Enum(int i);
        public int ret_Enum(SimpleEnum e);
    }

    public static interface NumberMappedEnumLib {
        public MappedEnum ret_Enum(int i);
        public int ret_Enum(MappedEnum e);
    }

    public static interface NumericLib {
        public byte ret_Enum(byte e);
        public Byte ret_Enum(Byte e);

        public short ret_Enum(short e);
        public Short ret_Enum(Short e);

        public int ret_Enum(int e);
        public Integer ret_Enum(Integer e);
    }

    public static interface EnumSetLib {
        public int ret_int32_t(EnumSet<BitField> enumSet);
    }

    public static interface ReturnEnumSetLib {
        public EnumSet<BitField> ret_int32_t(EnumSet<BitField> bitfield);
        public EnumSet<BitField> ret_int32_t(int bitfield);
    }

    private static EnumLib EnumLib;
    private static NumberSimpleEnumLib simpleEnumLib;
    private static NumberMappedEnumLib mappedEnumLib;
    private static NumericLib numericLib;
    private static EnumSetLib enumSetLib;
    private static ReturnEnumSetLib returnEnumSetLib;

    @BeforeAll
    public static void beforeAll() {
        EnumLib = TstUtil.loadTestLib(EnumLib.class);
        simpleEnumLib = TstUtil.loadTestLib(NumberSimpleEnumLib.class);
        mappedEnumLib = TstUtil.loadTestLib(NumberMappedEnumLib.class);
        numericLib = TstUtil.loadTestLib(NumericLib.class);
        enumSetLib = TstUtil.loadTestLib(EnumSetLib.class);
        returnEnumSetLib = TstUtil.loadTestLib(ReturnEnumSetLib.class);
    }

    /**
     * Even if the native function expects an enum,
     * it can be mapped as a number and will behave exactly as if it were a number function.
     * Longs are not supported
     */
    @Test
    public void testReturnEnumNumbers() {
        returnLoop((byte i1) -> {
            assertEquals((byte) i1, numericLib.ret_Enum((byte) i1),
                    "Return enum as byte failed for " + i1);
            assertEquals(box((byte) i1), numericLib.ret_Enum(box((byte) i1)),
                    "Return enum as boxed byte failed for " + i1);
        });
        returnLoop((short i1) -> {
            assertEquals((short) i1, numericLib.ret_Enum((short) i1),
                    "Return enum as short failed for " + i1);
            assertEquals(box((short) i1), numericLib.ret_Enum(box((short) i1)),
                    "Return enum as boxed short failed for " + i1);
        });
        returnLoop((int i1) -> {
            assertEquals((int) i1, numericLib.ret_Enum((int) i1),
                    "Return enum as int failed for " + i1);
            assertEquals(box((int) i1), numericLib.ret_Enum(box((int) i1)),
                    "Return enum as boxed int failed for " + i1);
        });
    }

    @Test
    public void testSimpleEnum() {
        Arrays.asList(SimpleEnum.values()).forEach((SimpleEnum e) -> {
            assertEquals(e, EnumLib.ret_Enum(e),
                    "Incorrect SimpleEnum returned");
            assertEquals(e, simpleEnumLib.ret_Enum(e.ordinal()),
                    "Incorrect SimpleEnum returned");
            assertEquals(e.ordinal(), simpleEnumLib.ret_Enum(e),
                    "Incorrect SimpleEnum returned");
        });
    }

    @Test
    public void testMappedEnum() {
        Arrays.asList(MappedEnum.values()).forEach((MappedEnum e) -> {
            assertEquals(e, EnumLib.ret_Enum(e),
                    "Incorrect MappedEnum returned");
            assertEquals(e, mappedEnumLib.ret_Enum(e.intValue()),
                    "Incorrect MappedEnum returned");
            assertEquals(e.intValue(), mappedEnumLib.ret_Enum(e),
                    "Incorrect MappedEnum returned");
        });
    }

    /**
     * Many C libraries use ORing enum values as a way to indicate the combination of those values,
     * these are often used for flags that may be joined together.
     * JNR-FFI allows this same practice of course by also ORing the values, but in addition to this
     * by adding support for EnumSets as a more elegant Java friendly approach.
     * It behaves in the same way as ORing the values together,
     * see {@link jnr.ffi.provider.converters.EnumSetConverter} for the implementation details.
     */
    @Test
    public void testEnumSet() {
        // (A | B) == [A, B] regarding conversions
        int result = BitField.A.intValue() | BitField.B.intValue();
        EnumSet<BitField> set = EnumSet.of(BitField.A, BitField.B);
        assertEquals(set, returnEnumSetLib.ret_int32_t(set));
        assertEquals(result, enumSetLib.ret_int32_t(set));
        assertEquals(set, returnEnumSetLib.ret_int32_t(result));
    }
}
