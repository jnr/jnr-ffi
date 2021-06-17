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

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.TstUtil;
import jnr.ffi.Union;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnionTest {
    public static interface TestLib {

    }
    
    public UnionTest() {
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

    public static final class union extends Union {
        public final Signed8 s8 = new Signed8();
        public final Unsigned8 u8 = new Unsigned8();
        public final Signed16 s16 = new Signed16();
        public final Unsigned16 u16 = new Unsigned16();
        public final Signed32 s32 = new Signed32();
        public final Unsigned32 u32 = new Unsigned32();
        public final Signed64 s64 = new Signed64();
        public final Unsigned64 u64 = new Unsigned64();

        public union() {
            super(runtime);
        }


    }

    public static final class innerStruct extends Struct {
        public final Unsigned8 firstByte = new Unsigned8();
        public final Unsigned8 secondByte = new Unsigned8();

        public innerStruct() {
            super(runtime);
        }
    }

    public static final class unionWithStruct extends Union {
        public final Unsigned16 u16 = new Unsigned16();
        public final innerStruct inner = inner(new innerStruct());

        public unionWithStruct() {
            super(runtime);
        }
    }

    @Test public void offsetTest() {
        union u = new union();
        assertEquals(0L, u.s8.offset(), "Not at offset 0");
        assertEquals(0L, u.u8.offset(), "Not at offset 0");
        assertEquals(0L, u.s16.offset(), "Not at offset 0");
        assertEquals(0L, u.u16.offset(), "Not at offset 0");
        assertEquals(0L, u.s32.offset(), "Not at offset 0");
        assertEquals(0L, u.s32.offset(), "Not at offset 0");
        assertEquals(0L, u.s64.offset(), "Not at offset 0");
        assertEquals(0L, u.u64.offset(), "Not at offset 0");
    }
    @Test public void s8s16() {
        union u = new union();
        final int MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xef00 : 0x00ef;
        u.s16.set((short) MAGIC);
        assertEquals((byte) 0xef, u.s8.get(), "Wrong value");
    }
    
    @Test public void s8s32() {
        union u = new union();
        final int MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xef000000 : 0x000000ef;
        u.s32.set(MAGIC);
        assertEquals((byte) 0xef, u.s8.get(), "Wrong value");
    }
    
    @Test public void s16s32() {
        union u = new union();
        final int MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xdead0000 : 0x0000dead;
        u.s32.set(MAGIC);
        assertEquals((short) 0xdead, u.s16.get(), "Wrong value");
    }
    @Test public void s8s64() {
        union u = new union();
        final long MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xef00000000000000L : 0xefL;
        u.s64.set(MAGIC);
        assertEquals((byte) 0xef, u.s8.get(), "Wrong value");
    }
    
    @Test public void s16s64() {
        union u = new union();
        final long MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xbeef000000000000L : 0xbeefL;
        u.s64.set(MAGIC);
        assertEquals((short) 0xbeef, u.s16.get(), "Wrong value");
    }
    
    @Test public void s32s64() {
        union u = new union();
        final long MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xdeadbeef00000000L : 0xdeadbeefL;
        u.s64.set(MAGIC);
        assertEquals(0xdeadbeef, u.s32.get(), "Wrong value");
    }

    @Test
    public void innerStructSize() {
        unionWithStruct unionWithStruct = new unionWithStruct();
        assertEquals(2, Struct.size(unionWithStruct));
    }
}