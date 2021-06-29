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

import jnr.ffi.Memory;
import jnr.ffi.NativeLong;
import jnr.ffi.NativeType;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.StructLayout;
import jnr.ffi.TstUtil;
import jnr.ffi.Type;
import jnr.ffi.annotations.LongLong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StructLayoutTest {

    public StructLayoutTest() {
    }
    
    public static interface TestLib {
        byte struct_field_Signed8(Pointer s);
        short struct_field_Signed16(Pointer s);
        int struct_field_Signed32(Pointer s);
        @LongLong long struct_field_Signed64(Pointer s);
        float struct_field_Float32(Pointer s);
        double struct_field_Float64(Pointer s);
        short struct_align_Signed16(Pointer s);
        int struct_align_Signed32(Pointer s);
        @LongLong long struct_align_Signed64(Pointer s);
        NativeLong struct_align_SignedLong(Pointer s);
        Pointer struct_make_struct(byte b, short s, int i, @LongLong long ll, float f, double d);
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
    public static class struct1 extends StructLayout {
        public final Signed8 b = new Signed8();
        public final Signed16 s = new Signed16();
        public final Signed32 i = new Signed32();
        public final Signed64 i64 = new Signed64();
        public final SignedLong l = new SignedLong();
        public final Float f = new Float();
        public final Double d = new Double();

        public struct1() {
            super(runtime);
        }


    }
    public static class Int16Align extends StructLayout {
        public final Signed8 first = new Signed8();
        public final Signed16 s = new Signed16();

        public Int16Align() {
            super(runtime);
        }

    }
    public static class Int32Align extends StructLayout {
        public final Signed8 first = new Signed8();
        public final Signed32 i = new Signed32();

        public Int32Align() {
            super(runtime);
        }

    }
    public static class Int64Align extends StructLayout {
        public final Signed8 first = new Signed8();
        public final Signed64 l = new Signed64();

        public Int64Align() {
            super(runtime);
        }

    }
    public static class LongAlign extends StructLayout {
        public final Signed8 first = new Signed8();
        public final SignedLong l = new SignedLong();

        public LongAlign() {
            super(runtime);
        }

    }

    @Test public void testInt8InitialValue() {
        struct1 s = new struct1();
        Pointer ptr = Memory.allocate(runtime, s.size());
        assertEquals((byte) 0, s.b.get(ptr), "default value not zero");
    }

    @Test public void testInt8Set() {
        struct1 s = new struct1();
        Pointer ptr = Memory.allocate(runtime, s.size());
        final byte MAGIC = (byte) 0xfe;
        s.b.set(ptr, MAGIC);
        assertEquals(MAGIC, s.b.get(ptr), "Byte value not set correctly");
    }

    @Test
    public void byteField() {
        final byte MAGIC = (byte) 0xbe;
        struct1 s = new struct1();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.b.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed8(ptr), "byte field not set");
        s.b.set(ptr, (byte) 0);
        assertEquals((byte) 0, testlib.struct_field_Signed8(ptr), "byte field not cleared");
    }

    @Test
    public void shortField() {
        final short MAGIC = (short) 0xbeef;
        struct1 s = new struct1();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.s.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed16(ptr), "short field not set");
        s.s.set(ptr, (short) 0);
        assertEquals((short) 0, testlib.struct_field_Signed16(ptr), "short field not cleared");
    }

    @Test
    public void intField() {
        final int MAGIC = 0xdeadbeef;
        struct1 s = new struct1();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.i.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed32(ptr), "int field not set");
        s.i.set(ptr, 0);
        assertEquals(0, testlib.struct_field_Signed32(ptr), "int field not cleared");
    }
    @Test 
    public void int64Field() {
        final long MAGIC = 0x1234deadbeef5678L;
        struct1 s = new struct1();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.i64.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed64(ptr), "long field not set");
        s.i64.set(ptr, 0);
        assertEquals(0L, testlib.struct_field_Signed64(ptr), "long field not cleared");
    }
    @Test 
    public void alignInt16Field() {
        final short MAGIC = (short) 0xbeef;
        Int16Align s = new Int16Align();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.s.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed16(ptr), "short field not aligned");
    }
    @Test 
    public void alignSigned32Field() {
        final int MAGIC = (int) 0xdeadbeef;
        Int32Align s = new Int32Align();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.i.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed32(ptr), "int field not aligned");
    }
    @Test 
    public void alignSigned64Field() {
        final long MAGIC = 0x1234deadbeef5678L;
        Int64Align s = new Int64Align();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.l.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed64(ptr), "long field not aligned");
    }
    @Test 
    public void alignSignedLongField() {
        final NativeLong MAGIC = new NativeLong(0xdeadbeef);
        LongAlign s = new LongAlign();
        Pointer ptr = Memory.allocate(runtime, s.size());
        s.l.set(ptr, MAGIC);

        assertEquals(MAGIC, testlib.struct_align_SignedLong(ptr), "native long field not aligned");
    }
    @Test
    public void returnStructAddress() throws Throwable {
        final byte B = 0x11;
        final short S = 0x2222;
        final int I = 0x33333333;
        final long L = 0x4444444444444444L;
        final float F = (float) 0x55555555;
        final double D = (double) 0x6666666666666666L;
        struct1 s = new struct1();
        Pointer ptr = testlib.struct_make_struct(B, S, I, L, F, D);
        assertEquals(B, s.b.get(ptr), "Incorrect byte value in struct");
        assertEquals(S, s.s.get(ptr), "Incorrect short value in struct");
        assertEquals(I, s.i.get(ptr), "Incorrect int value in struct");
        assertEquals(L, s.i64.get(ptr), "Incorrect int64 value in struct");
        assertEquals(F, s.f.get(ptr), 0.0001, "Incorrect float value in struct");
        assertEquals(D, s.d.get(ptr), 0.0001, "Incorrect double value in struct");

    }
    private static final class ArrayTest extends StructLayout {
        public final Signed8[] byteArray = array(new Signed8[8]);

        public ArrayTest() {
            super(runtime);
        }

    }
    @Test
    public void arrayMember() {
        ArrayTest s = new ArrayTest();
        assertEquals(0L, s.byteArray[0].offset(), "First element not at correct offset");
        assertEquals(1L, s.byteArray[1].offset(), "Second element not at correct offset");
        assertEquals(7L, s.byteArray[7].offset(), "Last element not at correct offset");
    }
    private static final class Unsigned8Test extends StructLayout {
        public final Unsigned8 u8 = new Unsigned8();

        public Unsigned8Test() {
            super(runtime);
        }

    }
    @Test
    public void unsigned8() {
        Unsigned8Test s = new Unsigned8Test();
        Pointer ptr = Memory.allocate(runtime, s.size());
        final short MAGIC = (short) Byte.MAX_VALUE + 1;
        s.u8.set(ptr, MAGIC);
        assertEquals(MAGIC, s.u8.shortValue(ptr), "Incorrect unsigned byte value");
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
        assertEquals(MAGIC, s.u16.intValue(), "Incorrect unsigned short value");
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
        assertEquals(MAGIC, s.u32.longValue(), "Incorrect unsigned int value");
    }
    
    private static final class Unsigned64Test extends StructLayout {
        public final Unsigned64 u64 = new Unsigned64();

        public Unsigned64Test() {
            super(runtime);
        }

    }
    @Test
    public void unsigned64() {
        Unsigned64Test s = new Unsigned64Test();
        Pointer ptr = Memory.allocate(runtime, s.size());
        final long MAGIC = Long.MAX_VALUE;
        s.u64.set(ptr, MAGIC);
        assertEquals(MAGIC, s.u64.longValue(ptr), "Incorrect unsigned long long value");
        // Just make sure that an Unsigned64 doesn't do anything weird with negative values
        s.u64.set(ptr, Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, s.u64.longValue(ptr), "Incorrect unsigned long long value");
    }
    
    private static final class UnsignedLongTest extends StructLayout {
        public final UnsignedLong ul = new UnsignedLong();

        public UnsignedLongTest() {
            super(runtime);
        }

    }
    @Test
    public void unsignedLong() {
        UnsignedLongTest s = new UnsignedLongTest();
        Pointer ptr = Memory.allocate(runtime, s.size());
        final long MAGIC = (long) Integer.MAX_VALUE;
        s.ul.set(ptr, MAGIC);
        assertEquals(MAGIC, s.ul.longValue(ptr), "Incorrect unsigned long value");
    }

    private class InnerStruct extends StructLayout {
        public final Signed8 s8 = new Signed8();

        public InnerStruct() {
            super(runtime);
        }

    }
    private class InnerTest extends StructLayout {
        public final Signed32 i32 = new Signed32();
        public final InnerStruct s = inner(new InnerStruct());

        public InnerTest() {
            super(runtime);
        }

    }
    @Test public void innerStruct() {
        InnerTest t = new InnerTest();
        Pointer ptr = Memory.allocate(runtime, t.size());
        ptr.putInt(0, 0xdeadbeef);
        ptr.putByte(4, (byte) 0x12);
        assertEquals((byte) 0x12, t.s.s8.get(ptr), "incorrect inner struct field value");
    }

    static final class LongPadding extends StructLayout {

        public final Signed8 s8 = new Signed8();
        public final Padding pad = new Padding(NativeType.SLONG, 3);

        public LongPadding() {
            super(runtime);
        }
    }

    @Test public void longPadding() throws Throwable {
        Type longType = runtime.findType(NativeType.SLONG);
        final int SIZE = longType.alignment() + (longType.size() * 3);
        assertEquals(SIZE, new LongPadding().size(), "incorrect size");
    }

    static final class TailPadding extends StructLayout {

        public final SignedLong sl = new SignedLong();
        public final Signed8 s8 = new Signed8();


        public TailPadding() {
            super(runtime);
        }
    }

    @Test public void tailPadding() throws Throwable {
        Type longType = runtime.findType(NativeType.SLONG);
        assertEquals(longType.size() * 2, new TailPadding().size(), "incorrect size");
        assertEquals(longType.alignment(), new TailPadding().alignment(), "incorrect alignment");
    }
}
