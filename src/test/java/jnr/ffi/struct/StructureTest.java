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
import jnr.ffi.TstUtil;
import jnr.ffi.TypeAlias;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.types.size_t;
import jnr.ffi.types.ssize_t;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static jnr.ffi.TypeAlias.ssize_t;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    public static class structWithStructRef extends Struct {
        public final StructRef<struct1> mStructRef = new StructRef<struct1>(struct1.class);
        public structWithStructRef(jnr.ffi.Runtime runtime) {
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
        assertEquals((byte) 0, s.b.get(), "default value not zero");
    }

    @Test public void testInt8Set() {
        struct1 s = new struct1(runtime);
        final byte MAGIC = (byte) 0xfe;
        s.b.set(MAGIC);
        assertEquals(MAGIC, s.b.get(), "Byte value not set correctly");
    }

    @Test
    public void byteField() {
        final byte MAGIC = (byte) 0xbe;
        struct1 s = new struct1(runtime);
        s.b.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed8(s), "byte field not set");
        s.b.set((byte) 0);
        assertEquals((byte) 0, testlib.struct_field_Signed8(s), "byte field not cleared");
    }

    @Test
    public void shortField() {
        final short MAGIC = (short) 0xbeef;
        struct1 s = new struct1(runtime);
        s.s.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed16(s), "short field not set");
        s.s.set((short) 0);
        assertEquals((short) 0, testlib.struct_field_Signed16(s), "short field not cleared");
    }

    @Test
    public void intField() {
        final int MAGIC = 0xdeadbeef;
        struct1 s = new struct1(runtime);
        s.i.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed32(s), "int field not set");
        s.i.set(0);
        assertEquals(0, testlib.struct_field_Signed32(s), "int field not cleared");
    }
    @Test 
    public void int64Field() {
        final long MAGIC = 0x1234deadbeef5678L;
        struct1 s = new struct1(runtime);
        s.i64.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_field_Signed64(s), "long field not set");
        s.i64.set(0);
        assertEquals(0L, testlib.struct_field_Signed64(s), "long field not cleared");
    }
    @Test 
    public void alignInt16Field() {
        final short MAGIC = (short) 0xbeef;
        Int16Align s = new Int16Align(runtime);
        s.s.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed16(s), "short field not aligned");
    }
    @Test 
    public void alignSigned32Field() {
        final int MAGIC = (int) 0xdeadbeef;
        Int32Align s = new Int32Align(runtime);
        s.i.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed32(s), "int field not aligned");
    }
    @Test 
    public void alignSigned64Field() {
        final long MAGIC = 0x1234deadbeef5678L;
        Int64Align s = new Int64Align(runtime);
        s.l.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_align_Signed64(s), "long field not aligned");
    }
    @Test 
    public void alignSignedLongField() {
        final NativeLong MAGIC = new NativeLong(0xdeadbeef);
        LongAlign s = new LongAlign(runtime);
        s.l.set(MAGIC);

        assertEquals(MAGIC, testlib.struct_align_SignedLong(s), "native long field not aligned");
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
        assertEquals(B, s.b.get(), "Incorrect byte value in struct");
        assertEquals(S, s.s.get(), "Incorrect short value in struct");
        assertEquals(I, s.i.get(), "Incorrect int value in struct");
        assertEquals(L, s.i64.get(), "Incorrect int64 value in struct");
        assertEquals(F, s.f.get(), 0.0001, "Incorrect float value in struct");
        assertEquals(D, s.d.get(), 0.0001, "Incorrect double value in struct");

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
        assertEquals(0L, s.byteArray[0].offset(), "First element not at correct offset");
        assertEquals(1L, s.byteArray[1].offset(), "Second element not at correct offset");
        assertEquals(7L, s.byteArray[7].offset(), "Last element not at correct offset");
    }
    private static final class Unsigned8Test extends Struct {
        public final Unsigned8 u8 = new Unsigned8();

        public Unsigned8Test() {
            super(runtime);
        }

    }

    @Test
    public void structRef()
    {
        structWithStructRef structWithStructRef = new structWithStructRef(runtime);
        struct1 s = new struct1(runtime);
        s.i.set(12);
        structWithStructRef.mStructRef.set(s);
        assertEquals(s.i.get(), structWithStructRef.mStructRef.get().i.get(), "Struct field not equals");
        structWithStructRef.mStructRef.set(new struct1[]{s});
        assertEquals(s.i.get(), structWithStructRef.mStructRef.get(1)[0].i.get(), "Struct field not equals");
    }

    @Test
    public void unsigned8() {
        Unsigned8Test s = new Unsigned8Test();
        final short MAGIC = (short) Byte.MAX_VALUE + 1;
        s.u8.set(MAGIC);
        assertEquals(MAGIC, s.u8.shortValue(), "Incorrect unsigned byte value");
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
        assertEquals(MAGIC, s.u64.longValue(), "Incorrect unsigned long long value");
        // Just make sure that an Unsigned64 doesn't do anything weird with negative values
        s.u64.set(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, s.u64.longValue(), "Incorrect unsigned long long value");
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
        assertEquals(MAGIC, s.ul.longValue(), "Incorrect unsigned long value");
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

    private class DoubleInnerTest extends Struct {
        public final Signed32 s32 = new Signed32();
        public final InnerTest innerTestStruct = inner(new InnerTest());

        public DoubleInnerTest() {
            super(runtime);
        }
    }

    @Test public void innerStruct() {
        InnerTest t = new InnerTest();
        Pointer io = Struct.getMemory(t);
        io.putInt(0, 0xdeadbeef);
        io.putByte(4, (byte) 0x12);
        assertEquals((byte) 0x12, t.s.s8.get(), "incorrect inner struct field value");
    }

    @Test public void doubleInnerStruct() {
        DoubleInnerTest t = new DoubleInnerTest();
        Pointer io = Struct.getMemory(t);
        io.putInt(0, 0xffffffff);
        io.putInt(4, 0xffffffff);
        assertEquals(0, t.innerTestStruct.s.s8.get());
    }

    public static class structWithPointer extends Struct {
        public final Pointer pointer = new Pointer();

        public structWithPointer(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }

    @Test
    public void abstractPointer() {
        structWithPointer s = new structWithPointer(runtime);
        Pointer p = Memory.allocate(s.getRuntime(), NativeType.UCHAR);
        p.putByte(0, (byte) 0xff);
        s.pointer.set(p);
        assertNotNull(s.pointer.get(), "Abstract pointer was not copied");
        assertEquals(p.getByte(0), s.pointer.get().getByte(0), "Abstract pointer value does not match");
    }

    @Test
    public void tempPointer() {
        structWithPointer s = new structWithPointer(runtime);
        Pointer p = Memory.allocateTemporary(s.getRuntime(), NativeType.UCHAR);
        p.putByte(0, (byte) 0xff);
        s.pointer.set(p);
        assertNotNull(s.pointer.get(), "Temp pointer was not copied");
        assertEquals(p.getByte(0), s.pointer.get().getByte(0), "Temp pointer value does not match");
    }

    @Test
    public void directPointer() {
        structWithPointer s = new structWithPointer(runtime);
        Pointer p = Memory.allocateDirect(s.getRuntime(), NativeType.UCHAR);
        p.putByte(0, (byte) 0xff);
        s.pointer.set(p);
        assertNotNull(s.pointer.get(), "Direct pointer was not copied");
        assertEquals(p.getByte(0), s.pointer.get().getByte(0), "Direct pointer value does not match");
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
