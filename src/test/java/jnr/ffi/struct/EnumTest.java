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

package jnr.ffi.struct;

import jnr.ffi.NativeLong;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.TstUtil;
import jnr.ffi.util.EnumMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnumTest {

    public EnumTest() {
    }
    public enum TestEnum {
        ZERO,
        B,
        MAGIC
    }
    public class struct1 extends Struct {
        public final Enum8<TestEnum> b = new Enum8<TestEnum>(TestEnum.class);
        public final Enum16<TestEnum> s = new Enum16<TestEnum>(TestEnum.class);
        public final Enum32<TestEnum> i = new Enum32<TestEnum>(TestEnum.class);
        public final Enum64<TestEnum> i64 = new Enum64<TestEnum>(TestEnum.class);
        public final EnumLong<TestEnum> l = new EnumLong<TestEnum>(TestEnum.class);

        public struct1() {
            super(runtime);
        }

    }
    public static interface TestLib {
        byte struct_field_Signed8(struct1 s);
        short struct_field_Signed16(struct1 s);
        int struct_field_Signed32(struct1 s);
        long struct_field_Signed64(struct1 s);
        float struct_field_Float32(struct1 s);
        double struct_field_Float64(struct1 s);
        short struct_align_Signed16(Int16Align s);
        int struct_align_Signed32(Int32Align s);
        long struct_align_Signed64(Int64Align s);
        NativeLong struct_align_SignedLong(LongAlign s);
//        float struct_align_Float32(Float32Align s);
//        double struct_align_Float64(Float64Align s);
//        void struct_set_string(struct1 s, String string);
    }
    static TestLib testlib;
    static Runtime runtime;

    @BeforeAll
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    public static class Int16Align extends Struct {
        public final Enum8<TestEnum> first = new Enum8<TestEnum>(TestEnum.class);
        public final Enum16<TestEnum> s = new Enum16<TestEnum>(TestEnum.class);

        public Int16Align() {
            super(runtime);
        }

    }
    public static class Int32Align extends Struct {
        public final Enum8<TestEnum> first = new Enum8<TestEnum>(TestEnum.class);
        public final Enum32<TestEnum> i = new Enum32<TestEnum>(TestEnum.class);

        public Int32Align() {
            super(runtime);
        }
    }
    public static class Int64Align extends Struct {
        public final Enum8<TestEnum> first = new Enum8<TestEnum>(TestEnum.class);
        public final Enum64<TestEnum> l = new Enum64<TestEnum>(TestEnum.class);

        public Int64Align() {
            super(runtime);
        }
    }

    public static class LongAlign extends Struct {
        public final Enum8<TestEnum> first = new Enum8<TestEnum>(TestEnum.class);
        public final EnumLong<TestEnum> l = new EnumLong<TestEnum>(TestEnum.class);

        public LongAlign() {
            super(runtime);
        }

    }
    @Test public void testInt8InitialValue() {
        struct1 s = new struct1();
        assertEquals(TestEnum.ZERO, s.b.get(), "default value not zero");
    }
    @Test public void testInt8Set() {
        struct1 s = new struct1();
        final TestEnum MAGIC = TestEnum.MAGIC;
        s.b.set(MAGIC);
        assertEquals(MAGIC, s.b.get(), "Byte value not set correctly");
    }
    @Test 
    public void byteField() {
        final byte MAGIC = (byte) EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC);
        struct1 s = new struct1();
        s.b.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed8(s), "byte field not set");
        s.b.set(TestEnum.ZERO);
        assertEquals((byte) 0, testlib.struct_field_Signed8(s), "byte field not cleared");
    }
    @Test 
    public void shortField() {
        final short MAGIC = (short) EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC);
        struct1 s = new struct1();
        s.s.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed16(s), "short field not set");
        s.s.set(TestEnum.ZERO);
        assertEquals((short) 0, testlib.struct_field_Signed16(s), "short field not cleared");
    }
    @Test 
    public void intField() {
        final int MAGIC = EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC);
        struct1 s = new struct1();
        s.i.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed32(s), "int field not set");
        s.i.set(TestEnum.ZERO);
        assertEquals(0, testlib.struct_field_Signed32(s), "int field not cleared");
    }
    @Test 
    public void int64Field() {
        final long MAGIC = EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC);
        struct1 s = new struct1();
        s.i64.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed64(s), "long field not set");
        s.i64.set(TestEnum.ZERO);
        assertEquals(0L, testlib.struct_field_Signed64(s), "long field not cleared");
    }
    @Test 
    public void alignInt16Field() {
        final short MAGIC = (short) EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC);
        Int16Align s = new Int16Align();
        s.s.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed16(s), "short field not aligned");
    }
    @Test 
    public void alignSigned32Field() {
        final int MAGIC = (int) EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC);
        Int32Align s = new Int32Align();
        s.i.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed32(s), "int field not aligned");
    }
    @Test 
    public void alignSigned64Field() {
        final long MAGIC = EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC);
        Int64Align s = new Int64Align();
        s.l.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed64(s), "long field not aligned");
    }
    @Test 
    public void alignSignedLongField() {
        final NativeLong MAGIC = new NativeLong(EnumMapper.getInstance(TestEnum.class).intValue(TestEnum.MAGIC));
        LongAlign s = new LongAlign();
        s.l.set(TestEnum.MAGIC);

        assertEquals(MAGIC, testlib.struct_align_SignedLong(s), "native long field not aligned");
    }
}
