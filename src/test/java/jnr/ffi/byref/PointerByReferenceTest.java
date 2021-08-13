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

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.TstUtil;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class PointerByReferenceTest {
    public PointerByReferenceTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
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
        assertEquals(MAGIC, lib.ptr_ret_pointer(ref, 0), "Wrong value passed");
    }

    @Test public void inOnlyIntReferenceNotWritten() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        Runtime runtime = Runtime.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);

        PointerByReference ref = new PointerByReference(MAGIC);

        lib.ptr_set_pointer(ref, 0, null);
        assertEquals(MAGIC, ref.getValue(), "Int reference written when it should not be");
    }

    @Test public void outOnlyIntReferenceNotRead() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        Runtime runtime = Runtime.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);

        PointerByReference ref = new PointerByReference(MAGIC);
        assertNotSame(MAGIC, lib.ptr_ret_pointer(ref, 0), "Reference value passed to native code when it should not be");
    }

    @Test public void outOnlyIntReferenceGet() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        Runtime runtime = Runtime.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);


        PointerByReference ref = new PointerByReference(Memory.allocateDirect(runtime, 1));
        lib.ptr_set_pointer(ref, 0, MAGIC);
        assertEquals(MAGIC, ref.getValue(), "Reference value not set");
    }
}