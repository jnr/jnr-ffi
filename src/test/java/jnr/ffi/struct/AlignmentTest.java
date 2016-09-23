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
import jnr.ffi.struct.AlignmentTest.TestLib.PointerStruct;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class AlignmentTest {
    public static interface TestLib {
        public class PointerStruct extends Struct {

            public final Signed8 s8 = new Signed8();
            public final Pointer p = new Pointer();

            public PointerStruct() {
                super(runtime);
            }
        }

        public class StructAlignment extends Struct {
            public final u_int8_t  f0 = new u_int8_t();
            public final u_int16_t f1 = new u_int16_t();
            public final u_int32_t f2 = new u_int32_t();
            public final u_int64_t f3 = new u_int64_t();
            public final Pointer   f4 = new Pointer();

            public StructAlignment(Alignment alignment) {
                super(runtime, alignment);
            }
        }

        public int struct_alignment_size_1();

        public int struct_alignment_field_offset_1(int field);

        public int struct_alignment_size_2();

        public int struct_alignment_field_offset_2(int field);

        public int struct_alignment_size_4();

        public int struct_alignment_field_offset_4(int field);

        public int struct_alignment_size_8();

        public int struct_alignment_field_offset_8(int field);

        public int struct_alignment_size_16();

        public int struct_alignment_field_offset_16(int field);

        class InnerStructAlignment1 extends Struct {
            class InnerStructAlignment2 extends Struct {
                class InnerStructAlignment3 extends Struct {
                    public final u_int8_t  f0 = new u_int8_t();
                    public final u_int16_t f1 = new u_int16_t();
                    public final u_int32_t f2 = new u_int32_t();
                    public final u_int64_t f3 = new u_int64_t();
                    public final Pointer   f4 = new Pointer();

                    public InnerStructAlignment3(Struct enclosing) {
                        super(runtime, enclosing);
                    }
                }

                public final u_int8_t              f0 = new u_int8_t();
                public final u_int16_t             f1 = new u_int16_t();
                public final u_int32_t             f2 = new u_int32_t();
                public final u_int64_t             f3 = new u_int64_t();
                public final Pointer               f4 = new Pointer();
                public final InnerStructAlignment3 f5 = inner(new InnerStructAlignment3(this));

                public InnerStructAlignment2(Struct enclosing) {
                    super(runtime, enclosing);
                }
            }

            public final u_int8_t              f0 = new u_int8_t();
            public final u_int16_t             f1 = new u_int16_t();
            public final u_int32_t             f2 = new u_int32_t();
            public final u_int64_t             f3 = new u_int64_t();
            public final Pointer               f4 = new Pointer();
            public final InnerStructAlignment2 f5 = inner(new InnerStructAlignment2(this));

            public InnerStructAlignment1(Alignment alignment) {
                super(runtime, alignment);
            }
        }

        int inner_struct_alignment_size_1();

        int inner_struct_alignment_field_offset_1(int level, int field);

        int inner_struct_alignment_size_2();

        int inner_struct_alignment_field_offset_2(int level, int field);

        int inner_struct_alignment_size_4();

        int inner_struct_alignment_field_offset_4(int level, int field);

        int inner_struct_alignment_size_8();

        int inner_struct_alignment_field_offset_8(int level, int field);

        int inner_struct_alignment_size_16();

        int inner_struct_alignment_field_offset_16(int level, int field);
    }

    static TestLib testlib;
    static Runtime runtime;
    public AlignmentTest() {
    }

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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    

    @Test public void alignPointer() throws Throwable {
        PointerStruct s = new PointerStruct();
        final int SIZE = runtime.addressSize() == 4 ? 8 : 16;
        assertEquals("Incorrect pointer field alignment", SIZE, Struct.size(s));
    }

    @Test
    public void alignsStructWhenTheAlignmentValueIs1() {
        TestLib.StructAlignment s = new TestLib.StructAlignment(new Struct.Alignment(1));

        assertEquals("StructAlignment1 size", testlib.struct_alignment_size_1(), Struct.size(s));
        assertEquals("f0 offset", testlib.struct_alignment_field_offset_1(0), s.f0.offset());
        assertEquals("f1 offset", testlib.struct_alignment_field_offset_1(1), s.f1.offset());
        assertEquals("f2 offset", testlib.struct_alignment_field_offset_1(2), s.f2.offset());
        assertEquals("f3 offset", testlib.struct_alignment_field_offset_1(3), s.f3.offset());
        assertEquals("f4 offset", testlib.struct_alignment_field_offset_1(4), s.f4.offset());
    }

    @Test
    public void alignsStructWhenTheAlignmentValueIs2() {
        TestLib.StructAlignment s = new TestLib.StructAlignment(new Struct.Alignment(2));

        assertEquals("StructAlignment2 size", testlib.struct_alignment_size_2(), Struct.size(s));
        assertEquals("f0 offset", testlib.struct_alignment_field_offset_2(0), s.f0.offset());
        assertEquals("f1 offset", testlib.struct_alignment_field_offset_2(1), s.f1.offset());
        assertEquals("f2 offset", testlib.struct_alignment_field_offset_2(2), s.f2.offset());
        assertEquals("f3 offset", testlib.struct_alignment_field_offset_2(3), s.f3.offset());
        assertEquals("f4 offset", testlib.struct_alignment_field_offset_2(4), s.f4.offset());
    }

    @Test
    public void alignsStructWhenTheAlignmentValueIs4() {
        TestLib.StructAlignment s = new TestLib.StructAlignment(new Struct.Alignment(4));

        assertEquals("StructAlignment4 size", testlib.struct_alignment_size_4(), Struct.size(s));
        assertEquals("f0 offset", testlib.struct_alignment_field_offset_4(0), s.f0.offset());
        assertEquals("f1 offset", testlib.struct_alignment_field_offset_4(1), s.f1.offset());
        assertEquals("f2 offset", testlib.struct_alignment_field_offset_4(2), s.f2.offset());
        assertEquals("f3 offset", testlib.struct_alignment_field_offset_4(3), s.f3.offset());
        assertEquals("f4 offset", testlib.struct_alignment_field_offset_4(4), s.f4.offset());
    }

    @Test
    public void alignsStructWhenTheAlignmentValueIs8() {
        TestLib.StructAlignment s = new TestLib.StructAlignment(new Struct.Alignment(8));

        assertEquals("StructAlignment8 size", testlib.struct_alignment_size_8(), Struct.size(s));
        assertEquals("f0 offset", testlib.struct_alignment_field_offset_8(0), s.f0.offset());
        assertEquals("f1 offset", testlib.struct_alignment_field_offset_8(1), s.f1.offset());
        assertEquals("f2 offset", testlib.struct_alignment_field_offset_8(2), s.f2.offset());
        assertEquals("f3 offset", testlib.struct_alignment_field_offset_8(3), s.f3.offset());
        assertEquals("f4 offset", testlib.struct_alignment_field_offset_8(4), s.f4.offset());
    }

    @Test
    public void alignsStructWhenTheAlignmentValueIs16() {
        TestLib.StructAlignment s = new TestLib.StructAlignment(new Struct.Alignment(16));

        assertEquals("StructAlignment16 size", testlib.struct_alignment_size_16(), Struct.size(s));
        assertEquals("f0 offset", testlib.struct_alignment_field_offset_16(0), s.f0.offset());
        assertEquals("f1 offset", testlib.struct_alignment_field_offset_16(1), s.f1.offset());
        assertEquals("f2 offset", testlib.struct_alignment_field_offset_16(2), s.f2.offset());
        assertEquals("f3 offset", testlib.struct_alignment_field_offset_16(3), s.f3.offset());
        assertEquals("f4 offset", testlib.struct_alignment_field_offset_16(4), s.f4.offset());
    }

    @Test
    public void alignsInnerStructWhenTheAlignmentValueIs1() {
        TestLib.InnerStructAlignment1 s = new TestLib.InnerStructAlignment1(new Struct.Alignment(1));

        assertEquals("InnerStructAlignment1 size", testlib.inner_struct_alignment_size_1(), Struct.size(s));
        assertEquals("f0 offset"      , testlib.inner_struct_alignment_field_offset_1(0, 0), s.f0.offset());
        assertEquals("f1 offset"      , testlib.inner_struct_alignment_field_offset_1(0, 1), s.f1.offset());
        assertEquals("f2 offset"      , testlib.inner_struct_alignment_field_offset_1(0, 2), s.f2.offset());
        assertEquals("f3 offset"      , testlib.inner_struct_alignment_field_offset_1(0, 3), s.f3.offset());
        assertEquals("f4 offset"      , testlib.inner_struct_alignment_field_offset_1(0, 4), s.f4.offset());
        assertEquals("f5.f0 offset"   , testlib.inner_struct_alignment_field_offset_1(1, 0), s.f5.f0.offset());
        assertEquals("f5.f1 offset"   , testlib.inner_struct_alignment_field_offset_1(1, 1), s.f5.f1.offset());
        assertEquals("f5.f2 offset"   , testlib.inner_struct_alignment_field_offset_1(1, 2), s.f5.f2.offset());
        assertEquals("f5.f3 offset"   , testlib.inner_struct_alignment_field_offset_1(1, 3), s.f5.f3.offset());
        assertEquals("f5.f4 offset"   , testlib.inner_struct_alignment_field_offset_1(1, 4), s.f5.f4.offset());
        assertEquals("f5.f5.f0 offset", testlib.inner_struct_alignment_field_offset_1(2, 0), s.f5.f5.f0.offset());
        assertEquals("f5.f5.f1 offset", testlib.inner_struct_alignment_field_offset_1(2, 1), s.f5.f5.f1.offset());
        assertEquals("f5.f5.f2 offset", testlib.inner_struct_alignment_field_offset_1(2, 2), s.f5.f5.f2.offset());
        assertEquals("f5.f5.f3 offset", testlib.inner_struct_alignment_field_offset_1(2, 3), s.f5.f5.f3.offset());
        assertEquals("f5.f5.f4 offset", testlib.inner_struct_alignment_field_offset_1(2, 4), s.f5.f5.f4.offset());
    }

    @Test
    public void alignsInnerStructWhenTheAlignmentValueIs2() {
        TestLib.InnerStructAlignment1 s = new TestLib.InnerStructAlignment1(new Struct.Alignment(2));

        assertEquals("InnerStructAlignment2 size", testlib.inner_struct_alignment_size_2(), Struct.size(s));
        assertEquals("f0 offset"      , testlib.inner_struct_alignment_field_offset_2(0, 0), s.f0.offset());
        assertEquals("f1 offset"      , testlib.inner_struct_alignment_field_offset_2(0, 1), s.f1.offset());
        assertEquals("f2 offset"      , testlib.inner_struct_alignment_field_offset_2(0, 2), s.f2.offset());
        assertEquals("f3 offset"      , testlib.inner_struct_alignment_field_offset_2(0, 3), s.f3.offset());
        assertEquals("f4 offset"      , testlib.inner_struct_alignment_field_offset_2(0, 4), s.f4.offset());
        assertEquals("f5.f0 offset"   , testlib.inner_struct_alignment_field_offset_2(1, 0), s.f5.f0.offset());
        assertEquals("f5.f1 offset"   , testlib.inner_struct_alignment_field_offset_2(1, 1), s.f5.f1.offset());
        assertEquals("f5.f2 offset"   , testlib.inner_struct_alignment_field_offset_2(1, 2), s.f5.f2.offset());
        assertEquals("f5.f3 offset"   , testlib.inner_struct_alignment_field_offset_2(1, 3), s.f5.f3.offset());
        assertEquals("f5.f4 offset"   , testlib.inner_struct_alignment_field_offset_2(1, 4), s.f5.f4.offset());
        assertEquals("f5.f5.f0 offset", testlib.inner_struct_alignment_field_offset_2(2, 0), s.f5.f5.f0.offset());
        assertEquals("f5.f5.f1 offset", testlib.inner_struct_alignment_field_offset_2(2, 1), s.f5.f5.f1.offset());
        assertEquals("f5.f5.f2 offset", testlib.inner_struct_alignment_field_offset_2(2, 2), s.f5.f5.f2.offset());
        assertEquals("f5.f5.f3 offset", testlib.inner_struct_alignment_field_offset_2(2, 3), s.f5.f5.f3.offset());
        assertEquals("f5.f5.f4 offset", testlib.inner_struct_alignment_field_offset_2(2, 4), s.f5.f5.f4.offset());
    }

    @Test
    public void alignsInnerStructWhenTheAlignmentValueIs4() {
        TestLib.InnerStructAlignment1 s = new TestLib.InnerStructAlignment1(new Struct.Alignment(4));

        assertEquals("InnerStructAlignment4 size", testlib.inner_struct_alignment_size_4(), Struct.size(s));
        assertEquals("f1 offset"      , testlib.inner_struct_alignment_field_offset_4(0, 1), s.f1.offset());
        assertEquals("f0 offset"      , testlib.inner_struct_alignment_field_offset_4(0, 0), s.f0.offset());
        assertEquals("f2 offset"      , testlib.inner_struct_alignment_field_offset_4(0, 2), s.f2.offset());
        assertEquals("f3 offset"      , testlib.inner_struct_alignment_field_offset_4(0, 3), s.f3.offset());
        assertEquals("f4 offset"      , testlib.inner_struct_alignment_field_offset_4(0, 4), s.f4.offset());
        assertEquals("f5.f0 offset"   , testlib.inner_struct_alignment_field_offset_4(1, 0), s.f5.f0.offset());
        assertEquals("f5.f1 offset"   , testlib.inner_struct_alignment_field_offset_4(1, 1), s.f5.f1.offset());
        assertEquals("f5.f2 offset"   , testlib.inner_struct_alignment_field_offset_4(1, 2), s.f5.f2.offset());
        assertEquals("f5.f3 offset"   , testlib.inner_struct_alignment_field_offset_4(1, 3), s.f5.f3.offset());
        assertEquals("f5.f4 offset"   , testlib.inner_struct_alignment_field_offset_4(1, 4), s.f5.f4.offset());
        assertEquals("f5.f5.f0 offset", testlib.inner_struct_alignment_field_offset_4(2, 0), s.f5.f5.f0.offset());
        assertEquals("f5.f5.f1 offset", testlib.inner_struct_alignment_field_offset_4(2, 1), s.f5.f5.f1.offset());
        assertEquals("f5.f5.f2 offset", testlib.inner_struct_alignment_field_offset_4(2, 2), s.f5.f5.f2.offset());
        assertEquals("f5.f5.f3 offset", testlib.inner_struct_alignment_field_offset_4(2, 3), s.f5.f5.f3.offset());
        assertEquals("f5.f5.f4 offset", testlib.inner_struct_alignment_field_offset_4(2, 4), s.f5.f5.f4.offset());
    }

    @Test
    public void alignsInnerStructWhenTheAlignmentValueIs8() {
        TestLib.InnerStructAlignment1 s = new TestLib.InnerStructAlignment1(new Struct.Alignment(8));

        assertEquals("InnerStructAlignment8 size", testlib.inner_struct_alignment_size_8(), Struct.size(s));
        assertEquals("f1 offset"      , testlib.inner_struct_alignment_field_offset_8(0, 1), s.f1.offset());
        assertEquals("f0 offset"      , testlib.inner_struct_alignment_field_offset_8(0, 0), s.f0.offset());
        assertEquals("f2 offset"      , testlib.inner_struct_alignment_field_offset_8(0, 2), s.f2.offset());
        assertEquals("f3 offset"      , testlib.inner_struct_alignment_field_offset_8(0, 3), s.f3.offset());
        assertEquals("f4 offset"      , testlib.inner_struct_alignment_field_offset_8(0, 4), s.f4.offset());
        assertEquals("f5.f0 offset"   , testlib.inner_struct_alignment_field_offset_8(1, 0), s.f5.f0.offset());
        assertEquals("f5.f1 offset"   , testlib.inner_struct_alignment_field_offset_8(1, 1), s.f5.f1.offset());
        assertEquals("f5.f2 offset"   , testlib.inner_struct_alignment_field_offset_8(1, 2), s.f5.f2.offset());
        assertEquals("f5.f3 offset"   , testlib.inner_struct_alignment_field_offset_8(1, 3), s.f5.f3.offset());
        assertEquals("f5.f4 offset"   , testlib.inner_struct_alignment_field_offset_8(1, 4), s.f5.f4.offset());
        assertEquals("f5.f5.f0 offset", testlib.inner_struct_alignment_field_offset_8(2, 0), s.f5.f5.f0.offset());
        assertEquals("f5.f5.f1 offset", testlib.inner_struct_alignment_field_offset_8(2, 1), s.f5.f5.f1.offset());
        assertEquals("f5.f5.f2 offset", testlib.inner_struct_alignment_field_offset_8(2, 2), s.f5.f5.f2.offset());
        assertEquals("f5.f5.f3 offset", testlib.inner_struct_alignment_field_offset_8(2, 3), s.f5.f5.f3.offset());
        assertEquals("f5.f5.f4 offset", testlib.inner_struct_alignment_field_offset_8(2, 4), s.f5.f5.f4.offset());
    }

    @Test
    public void alignsInnerStructWhenTheAlignmentValueIs16() {
        TestLib.InnerStructAlignment1 s = new TestLib.InnerStructAlignment1(new Struct.Alignment(16));

        assertEquals("InnerStructAlignment16 size", testlib.inner_struct_alignment_size_16(), Struct.size(s));
        assertEquals("f1 offset"      , testlib.inner_struct_alignment_field_offset_16(0, 1), s.f1.offset());
        assertEquals("f0 offset"      , testlib.inner_struct_alignment_field_offset_16(0, 0), s.f0.offset());
        assertEquals("f2 offset"      , testlib.inner_struct_alignment_field_offset_16(0, 2), s.f2.offset());
        assertEquals("f3 offset"      , testlib.inner_struct_alignment_field_offset_16(0, 3), s.f3.offset());
        assertEquals("f4 offset"      , testlib.inner_struct_alignment_field_offset_16(0, 4), s.f4.offset());
        assertEquals("f5.f0 offset"   , testlib.inner_struct_alignment_field_offset_16(1, 0), s.f5.f0.offset());
        assertEquals("f5.f1 offset"   , testlib.inner_struct_alignment_field_offset_16(1, 1), s.f5.f1.offset());
        assertEquals("f5.f2 offset"   , testlib.inner_struct_alignment_field_offset_16(1, 2), s.f5.f2.offset());
        assertEquals("f5.f3 offset"   , testlib.inner_struct_alignment_field_offset_16(1, 3), s.f5.f3.offset());
        assertEquals("f5.f4 offset"   , testlib.inner_struct_alignment_field_offset_16(1, 4), s.f5.f4.offset());
        assertEquals("f5.f5.f0 offset", testlib.inner_struct_alignment_field_offset_16(2, 0), s.f5.f5.f0.offset());
        assertEquals("f5.f5.f1 offset", testlib.inner_struct_alignment_field_offset_16(2, 1), s.f5.f5.f1.offset());
        assertEquals("f5.f5.f2 offset", testlib.inner_struct_alignment_field_offset_16(2, 2), s.f5.f5.f2.offset());
        assertEquals("f5.f5.f3 offset", testlib.inner_struct_alignment_field_offset_16(2, 3), s.f5.f5.f3.offset());
        assertEquals("f5.f5.f4 offset", testlib.inner_struct_alignment_field_offset_16(2, 4), s.f5.f5.f4.offset());
    }
}
