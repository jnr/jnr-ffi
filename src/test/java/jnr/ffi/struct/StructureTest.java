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

import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.TstUtil;
import jnr.ffi.types.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static jnr.ffi.TypeAlias.*;

/**
 *
 */
public class StructureTest {

    public StructureTest() {
    }
    
    public static interface TestLib {
        byte struct_field_Signed8(struct1 s);
        short struct_field_Signed16(struct1 s);
        int struct_field_Signed32(struct1 s);
        @LongLong long struct_field_Signed64(struct1 s);
        float struct_field_Float32(struct1 s);
        double struct_field_Float64(struct1 s);
        short struct_align_Signed16(Int16Align s);
        int struct_align_Signed32(Int32Align s);
        @LongLong long struct_align_Signed64(Int64Align s);
        NativeLong struct_align_SignedLong(LongAlign s);
        struct1 struct_make_struct(byte b, short s, int i, @LongLong long ll, float f, double d);
//        float struct_align_Float32(Float32Align s);
//        double struct_align_Float64(Float64Align s);
//        void struct_set_string(struct1 s, String string);
    }
    static TestLib testlib;
    static Runtime runtime;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
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
    public static class struct1 extends Struct {
        public final Signed8 b = new Signed8();
        public final Signed16 s = new Signed16();
        public final Signed32 i = new Signed32();
        public final Signed64 i64 = new Signed64();
        public final SignedLong l = new SignedLong();
        public final Float f = new Float();
        public final Double d = new Double();

        public struct1(jnr.ffi.Runtime runtime) {
            super(runtime);
        }


    }
    public static class Int16Align extends Struct {
        public final Signed8 first = new Signed8();
        public final Signed16 s = new Signed16();

        public Int16Align(jnr.ffi.Runtime runtime) {
            super(runtime);
        }

    }
    public static class Int32Align extends Struct {
        public final Signed8 first = new Signed8();
        public final Signed32 i = new Signed32();

        public Int32Align(jnr.ffi.Runtime runtime) {
            super(runtime);
        }

    }
    public static class Int64Align extends Struct {
        public final Signed8 first = new Signed8();
        public final Signed64 l = new Signed64();

        public Int64Align(jnr.ffi.Runtime runtime) {
            super(runtime);
        }

    }
    public static class LongAlign extends Struct {
        public final Signed8 first = new Signed8();
        public final SignedLong l = new SignedLong();

        public LongAlign(jnr.ffi.Runtime runtime) {
            super(runtime);
        }

    }

    @Test public void testInt8InitialValue() {
        struct1 s = new struct1(runtime);
        assertEquals("default value not zero", (byte) 0, s.b.get());
    }

    @Test public void testInt8Set() {
        struct1 s = new struct1(runtime);
        final byte MAGIC = (byte) 0xfe;
        s.b.set(MAGIC);
        assertEquals("Byte value not set correctly", MAGIC, s.b.get());
    }

    @Test
    public void byteField() {
        final byte MAGIC = (byte) 0xbe;
        struct1 s = new struct1(runtime);
        s.b.set(MAGIC);
        
        assertEquals("byte field not set", MAGIC, testlib.struct_field_Signed8(s));
        s.b.set((byte) 0);
        assertEquals("byte field not cleared", (byte) 0, testlib.struct_field_Signed8(s));
    }

    @Test
    public void shortField() {
        final short MAGIC = (short) 0xbeef;
        struct1 s = new struct1(runtime);
        s.s.set(MAGIC);
        
        assertEquals("short field not set", MAGIC, testlib.struct_field_Signed16(s));
        s.s.set((short) 0);
        assertEquals("short field not cleared", (short) 0, testlib.struct_field_Signed16(s));
    }

    @Test
    public void intField() {
        final int MAGIC = 0xdeadbeef;
        struct1 s = new struct1(runtime);
        s.i.set(MAGIC);
        
        assertEquals("int field not set", MAGIC, testlib.struct_field_Signed32(s));
        s.i.set(0);
        assertEquals("int field not cleared", 0, testlib.struct_field_Signed32(s));
    }
    @Test 
    public void int64Field() {
        final long MAGIC = 0x1234deadbeef5678L;
        struct1 s = new struct1(runtime);
        s.i64.set(MAGIC);
        
        assertEquals("long field not set", MAGIC, testlib.struct_field_Signed64(s));
        s.i64.set(0);
        assertEquals("long field not cleared", 0L, testlib.struct_field_Signed64(s));
    }
    @Test 
    public void alignInt16Field() {
        final short MAGIC = (short) 0xbeef;
        Int16Align s = new Int16Align(runtime);
        s.s.set(MAGIC);
        
        assertEquals("short field not aligned", MAGIC, testlib.struct_align_Signed16(s));
    }
    @Test 
    public void alignSigned32Field() {
        final int MAGIC = (int) 0xdeadbeef;
        Int32Align s = new Int32Align(runtime);
        s.i.set(MAGIC);
        
        assertEquals("int field not aligned", MAGIC, testlib.struct_align_Signed32(s));
    }
    @Test 
    public void alignSigned64Field() {
        final long MAGIC = 0x1234deadbeef5678L;
        Int64Align s = new Int64Align(runtime);
        s.l.set(MAGIC);
        
        assertEquals("long field not aligned", MAGIC, testlib.struct_align_Signed64(s));
    }
    @Test 
    public void alignSignedLongField() {
        final NativeLong MAGIC = new NativeLong(0xdeadbeef);
        LongAlign s = new LongAlign(runtime);
        s.l.set(MAGIC);
        
        assertEquals("native long field not aligned", MAGIC, testlib.struct_align_SignedLong(s));
    }
    @Test
    public void returnStructAddress() throws Throwable {
        final byte B = 0x11;
        final short S = 0x2222;
        final int I = 0x33333333;
        final long L = 0x4444444444444444L;
        final float F = (float) 0x55555555;
        final double D = (double) 0x6666666666666666L;
        struct1 s = testlib.struct_make_struct(B, S, I, L, F, D);
        assertEquals("Incorrect byte value in struct", B, s.b.get());
        assertEquals("Incorrect short value in struct", S, s.s.get());
        assertEquals("Incorrect int value in struct", I, s.i.get());
        assertEquals("Incorrect int64 value in struct", L, s.i64.get());
        assertEquals("Incorrect float value in struct", F, s.f.get(), 0.0001);
        assertEquals("Incorrect double value in struct", D, s.d.get(), 0.0001);
        
    }
    private static final class ArrayTest extends Struct {
        public final Signed8[] byteArray = array(new Signed8[8]);

        public ArrayTest() {
            super(runtime);
        }

    }
    @Test
    public void arrayMember() {
        ArrayTest s = new ArrayTest();
        assertEquals("First element not at correct offset", 0L, s.byteArray[0].offset());
        assertEquals("Second element not at correct offset", 1L, s.byteArray[1].offset());
        assertEquals("Last element not at correct offset", 7L, s.byteArray[7].offset());
    }
    private static final class Unsigned8Test extends Struct {
        public final Unsigned8 u8 = new Unsigned8();

        public Unsigned8Test() {
            super(runtime);
        }

    }
    @Test
    public void unsigned8() {
        Unsigned8Test s = new Unsigned8Test();
        final short MAGIC = (short) Byte.MAX_VALUE + 1;
        s.u8.set(MAGIC);
        assertEquals("Incorrect unsigned byte value", MAGIC, s.u8.shortValue());
    }
    private static final class Unsigned16Test extends Struct {
        public final Unsigned16 u16 = new Unsigned16();

        public Unsigned16Test() {
            super(runtime);
        }

    }
    @Test
    public void unsigned16() {
        Unsigned16Test s = new Unsigned16Test();
        final int MAGIC = (int) Short.MAX_VALUE + 1;
        s.u16.set(MAGIC);
        assertEquals("Incorrect unsigned short value", MAGIC, s.u16.intValue());
    }
    private static final class Unsigned32Test extends Struct {
        public final Unsigned32 u32 = new Unsigned32();

        public Unsigned32Test() {
            super(runtime);
        }

    }
    @Test
    public void unsigned32() {
        Unsigned32Test s = new Unsigned32Test();
        final long MAGIC = (long) Integer.MAX_VALUE + 1;
        s.u32.set(MAGIC);
        assertEquals("Incorrect unsigned int value", MAGIC, s.u32.longValue());
    }
    
    private static final class Unsigned64Test extends Struct {
        public final Unsigned64 u64 = new Unsigned64();

        public Unsigned64Test() {
            super(runtime);
        }

    }
    @Test
    public void unsigned64() {
        Unsigned64Test s = new Unsigned64Test();
        final long MAGIC = Long.MAX_VALUE;
        s.u64.set(MAGIC);
        assertEquals("Incorrect unsigned long long value", MAGIC, s.u64.longValue());
        // Just make sure that an Unsigned64 doesn't do anything weird with negative values
        s.u64.set(Long.MIN_VALUE);
        assertEquals("Incorrect unsigned long long value", Long.MIN_VALUE, s.u64.longValue());
    }
    
    private static final class UnsignedLongTest extends Struct {
        public final UnsignedLong ul = new UnsignedLong();

        public UnsignedLongTest() {
            super(runtime);
        }

    }
    @Test
    public void unsignedLong() {
        UnsignedLongTest s = new UnsignedLongTest();
        final long MAGIC = (long) Integer.MAX_VALUE;
        s.ul.set(MAGIC);
        assertEquals("Incorrect unsigned long value", MAGIC, s.ul.longValue());
    }

    private class InnerStruct extends Struct {
        public final Signed8 s8 = new Signed8();

        public InnerStruct() {
            super(runtime);
        }

    }
    private class InnerTest extends Struct {
        public final Signed32 i32 = new Signed32();
        public final InnerStruct s = inner(new InnerStruct());

        public InnerTest() {
            super(runtime);
        }

    }
    @Test public void innerStruct() {
        InnerTest t = new InnerTest();
        Pointer io = Struct.getMemory(t);
        io.putInt(0, 0xdeadbeef);
        io.putByte(4, (byte) 0x12);
        assertEquals("incorrect inner struct field value", (byte) 0x12, t.s.s8.get());
    }

    private class TypeTest extends Struct {
        class Fixnum extends NumberField {
            Fixnum(TypeAlias type) {
                super(NativeType.SINT);
            }

            @Override
            public void set(Number value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int intValue() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }

        class Fubar extends NumberField {
            Fubar(Class<?> type) {
                super(NativeType.SINT);
            }

            @Override
            public void set(Number value) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int intValue() {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }
        public final Fixnum i = new Fixnum(ssize_t);
        public final Fubar fubar = new Fubar(ssize_t.class);

        private TypeTest(Runtime runtime) {
            super(runtime);
        }
    }

    public static interface TestLib2 {
        @ssize_t long write(int fd, Pointer buf, @size_t long len);
    }

}
