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

package jnr.ffi.byref;


import jnr.ffi.Library;
import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.TstUtil;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class PointerByReferenceTest {
    public PointerByReferenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    public static interface TestLib {
        Pointer ptr_ret_pointer(PointerByReference p, int offset);
        void ptr_set_pointer(PointerByReference p, int offset, Pointer value);
    }

    public static interface TestLibInOnly {
        Pointer ptr_ret_pointer(@In PointerByReference p, int offset);
        void ptr_set_pointer(@In PointerByReference p, int offset, Pointer value);
    }

    public static interface TestLibOutOnly {
        Pointer ptr_ret_pointer(@Out PointerByReference p, int offset);
        void ptr_set_pointer(@Out PointerByReference p, int offset, Pointer value);
    }

    
    @Test public void inOnlyReferenceSet() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        Runtime runtime = Runtime.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);
        
        PointerByReference ref = new PointerByReference(MAGIC);
        assertEquals("Wrong value passed", MAGIC, lib.ptr_ret_pointer(ref, 0));
    }

    @Test public void inOnlyIntReferenceNotWritten() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        Runtime runtime = Runtime.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);

        PointerByReference ref = new PointerByReference(MAGIC);

        lib.ptr_set_pointer(ref, 0, null);
        assertEquals("Int reference written when it should not be", MAGIC, ref.getValue());
    }

    @Test public void outOnlyIntReferenceNotRead() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        Runtime runtime = Runtime.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);

        PointerByReference ref = new PointerByReference(MAGIC);
        assertNotSame("Reference value passed to native code when it should not be", MAGIC, lib.ptr_ret_pointer(ref, 0));
    }

    @Test public void outOnlyIntReferenceGet() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        Runtime runtime = Runtime.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);


        PointerByReference ref = new PointerByReference(Memory.allocateDirect(runtime, 1));
        lib.ptr_set_pointer(ref, 0, MAGIC);
        assertEquals("Reference value not set", MAGIC, ref.getValue());
    }
}