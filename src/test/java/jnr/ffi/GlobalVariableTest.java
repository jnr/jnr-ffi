/* 
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi;


import jnr.ffi.annotations.Delegate;
import jnr.ffi.types.u_int32_t;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GlobalVariableTest {
    public static interface ClosureIrV {
        @Delegate void call(int value);
    }

    public static interface TestLib {
        @u_int32_t Variable<Long> gvar_s32();
        int gvar_s32_get();
        void gvar_s32_set(int value);
        Variable<ClosureIrV> gvar_pointer();
        Pointer gvar_pointer_get();
    }

    @Test
    public void testIntegerVariableSet() {
        TestLib lib = TstUtil.loadTestLib(TestLib.class);
        Variable<Long> var = lib.gvar_s32();
        final long MAGIC = 0xdeadbeef;
        var.set(MAGIC);
        assertEquals(MAGIC, lib.gvar_s32_get());
    }

    @Test
    public void testIntegerVariableGet() {
        TestLib lib = TstUtil.loadTestLib(TestLib.class);
        Variable<Long> var = lib.gvar_s32();
        final int MAGIC = 0xdeadbeef;
        lib.gvar_s32_set(MAGIC);
        assertEquals(MAGIC, var.get().intValue());
    }

    @Test
    public void testCallbackVariableSet() {
        TestLib lib = TstUtil.loadTestLib(TestLib.class);
        Variable<ClosureIrV> var = lib.gvar_pointer();
        var.set(new ClosureIrV() {
            public void call(int value) {
            }
        });
        assertNotNull(lib.gvar_pointer_get());
    }

//    @Test public void testCallbackVariableGet() {
//        TestLib lib = TstUtil.loadTestLib(TestLib.class);
//        Variable<ClosureIrV> var = lib.gvar_pointer();
//        final boolean[] called = { false };
//        final int[] values = { 0 };
//
//        var.set(new ClosureIrV() {
//            public void call(int value) {
//                called[0] = true;
//                values[0] = value;
//            }
//        });
//        assertNotNull(lib.gvar_pointer_get());
//        assertNotNull(var.get());
//        var.get().call(0xdeadbeef);
//        assertTrue(called[0]);
//        assertEquals(0xdeadbeef, values[0]);
//    }
}
