/* 
 * Copyright (C) 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package jnr.ffi.struct;

import jnr.ffi.Library;
import jnr.ffi.Runtime;
import jnr.ffi.TstUtil;
import java.nio.ByteOrder;

import jnr.ffi.Union;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class UnionTest {
    public static interface TestLib {

    }
    
    public UnionTest() {
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
    @Test public void offsetTest() {
        union u = new union();
        assertEquals("Not at offset 0", 0L, u.s8.offset());
        assertEquals("Not at offset 0", 0L, u.u8.offset());
        assertEquals("Not at offset 0", 0L, u.s16.offset());
        assertEquals("Not at offset 0", 0L, u.u16.offset());
        assertEquals("Not at offset 0", 0L, u.s32.offset());
        assertEquals("Not at offset 0", 0L, u.s32.offset());
        assertEquals("Not at offset 0", 0L, u.s64.offset());
        assertEquals("Not at offset 0", 0L, u.u64.offset());
    }
    @Test public void s8s16() {
        union u = new union();
        final int MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xef00 : 0x00ef;
        u.s16.set((short) MAGIC);
        assertEquals("Wrong value", (byte) 0xef, u.s8.get());
    }
    
    @Test public void s8s32() {
        union u = new union();
        final int MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xef000000 : 0x000000ef;
        u.s32.set(MAGIC);
        assertEquals("Wrong value", (byte) 0xef, u.s8.get());
    }
    
    @Test public void s16s32() {
        union u = new union();
        final int MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xdead0000 : 0x0000dead;
        u.s32.set(MAGIC);
        assertEquals("Wrong value", (short) 0xdead, u.s16.get());
    }
    @Test public void s8s64() {
        union u = new union();
        final long MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xef00000000000000L : 0xefL;
        u.s64.set(MAGIC);
        assertEquals("Wrong value", (byte) 0xef, u.s8.get());
    }
    
    @Test public void s16s64() {
        union u = new union();
        final long MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xbeef000000000000L : 0xbeefL;
        u.s64.set(MAGIC);
        assertEquals("Wrong value", (short) 0xbeef, u.s16.get());
    }
    
    @Test public void s32s64() {
        union u = new union();
        final long MAGIC = runtime.byteOrder().equals(ByteOrder.BIG_ENDIAN)
                ? 0xdeadbeef00000000L : 0xdeadbeefL;
        u.s64.set(MAGIC);
        assertEquals("Wrong value", 0xdeadbeef, u.s32.get());
    }
    
}