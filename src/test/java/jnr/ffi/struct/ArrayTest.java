/* 
 * Copyright (C) 2011 Wayne Meissner
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

import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;
import jnr.ffi.provider.AbstractArrayMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class ArrayTest {
    public static interface TestLib {
        byte ptr_ret_int8_t(@In s8[] s, int index);
        class PointerStruct extends Struct {

            public final Signed8 s8 = new Signed8();
            public final Pointer p = new Pointer();

            public PointerStruct(Runtime runtime) {
                super(runtime);
            }
        }
    }

    static TestLib testlib;
    static Runtime runtime;
    public ArrayTest() {
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
    public static final class s8 extends Struct {
        public final Signed8 s8 = new Signed8();

        public s8(Runtime runtime) {
            super(runtime);
        }

    }
    public static final class s32 extends Struct {
        public final Signed8 s8 = new Signed8();
        public s32(Runtime runtime) {
            super(runtime);
        }
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void s8Array() {
        s8[] array = Struct.arrayOf(runtime, s8.class, 10);
        assertEquals("Array length incorrect", 10, array.length);
        for (int i = 0; i < array.length; ++i) {
            assertNotNull("Memory not allocated for array member", Struct.getMemory(array[i]));
        }
        Pointer ptr = ((DelegatingMemoryIO) Struct.getMemory(array[0])).getDelegatedMemoryIO();
        for (int i = 0; i < array.length; ++i) {
            assertSame("Different backing memory", ptr, ((DelegatingMemoryIO) Struct.getMemory(array[i])).getDelegatedMemoryIO());
        }
        if (ptr instanceof AbstractArrayMemoryIO) {
            assertEquals("Incorrect size", array.length, ((AbstractArrayMemoryIO)ptr).length());
        }
        for (int i = 0; i < array.length; ++i) {
            array[i].s8.set((byte) i);
        }
        for (int i = 0; i < array.length; ++i) {
            assertEquals("Incorrect value written to native memory at index " + i,
                    (byte) i, testlib.ptr_ret_int8_t(array, i));
        }
    }
}