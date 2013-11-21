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

import static org.junit.Assert.assertEquals;

import jnr.ffi.annotations.Pack;
import jnr.ffi.Struct;
import jnr.ffi.TstUtil;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;
import jnr.ffi.Runtime;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for explicitly packed structures.
 */
public class PackedStructTest {
    static TestLib testlib;
    static Runtime runtime;

    public PackedStructTest() {
    }

    @Pack(padding = 2)
    public static class Pack2 extends Struct {
        public final u_int32_t i = new u_int32_t();
        public final u_int64_t l = new u_int64_t();
        public Pack2(Runtime runtime) {
            super(runtime);
        }
    }

    @Pack(padding = 2)
    public static class Pack2Small extends Struct {
        public final u_int8_t v1 = new u_int8_t();
        public final u_int8_t v2 = new u_int8_t();
        // Intentionally ignoring sentinel "deadbeef" field
        public Pack2Small(Runtime runtime) {
            super(runtime);
        }
    }

    public static interface TestLib {
        Pack2 packedstruct_make_struct(@u_int32_t int i, @u_int64_t long l);
        Pack2Small packedstruct_make_tiny(@u_int8_t byte v1, @u_int8_t byte v2);
        Pack2OnOSX packedstruct_make_packed_on_osx(@u_int32_t int i,
                @u_int64_t long l);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }

    @Test
    public void testPackedStructureAlignment() {
        final int i = 0xAAAAAAAA;
        final long l = 0xBBBBBBBBBBBBBBBBL;
        Pack2 p = testlib.packedstruct_make_struct(i, l);
        assertEquals("Incorrect int value in struct", i, p.i.get());
        assertEquals("Incorrect long value in struct", l, p.l.get());
    }

    /**
     * Tests that fields that have natural alignment smaller than the
     * maximum packing value aren't artifically expanded.
     */
    @Test
    public void testFieldsSmallerThanPacking() {
        final byte v1 = 0xA;
        final byte v2 = 0xB;
        Pack2Small p = testlib.packedstruct_make_tiny(v1, v2);
        assertEquals("Incorrect byte value 1 in struct", v1, p.v1.get());
        assertEquals("Incorrect byte value 1 in struct", v2, p.v2.get());
    }

    @Pack(padding = 2, enabler = OSXPackEnabler.class)
    public static class Pack2OnOSX extends Struct {
        public final u_int32_t i = new u_int32_t();
        public final u_int64_t l = new u_int64_t();
        public Pack2OnOSX(Runtime runtime) {
            super(runtime);
        }
    }

    /**
     * Tests optionally enabled packing.
     */
    @Test
    public void testPackEnabler() {
        final int i = 0xAAAAAAAA;
        final long l = 0xBBBBBBBBBBBBBBBBL;
        Pack2OnOSX p = testlib.packedstruct_make_packed_on_osx(i, l);
        assertEquals("Incorrect int value in struct", i, p.i.get());
        assertEquals("Incorrect long value in struct", l, p.l.get());
    }
}
