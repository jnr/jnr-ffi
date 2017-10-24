package jnr.ffi.struct;

import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.TstUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldTest {

    static class CustomChar extends Field {

        CustomChar(Struct struct) {
            super(struct, NativeType.SCHAR);
        }

        public final byte get() {
            return getMemory().getByte(offset());
        }

        public final void set(byte value) {
            getMemory().putByte(offset(), value);
        }
    }

    static class CustomStruct extends Struct {

        CustomStruct(Runtime runtime) {
            super(runtime);
        }

        CustomChar customChar = new CustomChar(this);
        CharStruct innerStruct = inner(new CharStruct(getRuntime()));
    }

    static class CharStruct extends Struct {

        CharStruct(Runtime runtime) {
            super(runtime);
        }

        CustomChar customChar = new CustomChar(this);
    }

    public static interface TestLib {
        byte struct_field_Signed8(CustomStruct s);
        byte struct_field_Signed8(CharStruct s);
    }

    static TestLib testlib;
    static Runtime runtime;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }

    @Test
    public void byteField() {
        final byte MAGIC = (byte) 0xbe;
        CharStruct s = new CharStruct(runtime);
        s.customChar.set(MAGIC);

        assertEquals("byte field not set", MAGIC, testlib.struct_field_Signed8(s));
        s.customChar.set((byte) 0);
        assertEquals("byte field not cleared", (byte) 0, testlib.struct_field_Signed8(s));
    }
}