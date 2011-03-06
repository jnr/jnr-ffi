/* 
 * Copyright (C) 2007, 2008, 2010 Wayne Meissner
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

package com.kenai.jaffl.byref;


import com.kenai.jaffl.Library;
import com.kenai.jaffl.Memory;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.TstUtil;
import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.Out;
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
        Runtime runtime = Library.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);
        
        PointerByReference ref = new PointerByReference(MAGIC);
        assertEquals("Wrong value passed", MAGIC, lib.ptr_ret_pointer(ref, 0));
    }

    @Test public void inOnlyIntReferenceNotWritten() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        Runtime runtime = Library.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);

        PointerByReference ref = new PointerByReference(MAGIC);

        lib.ptr_set_pointer(ref, 0, null);
        assertEquals("Int reference written when it should not be", MAGIC, ref.getValue());
    }

    @Test public void outOnlyIntReferenceNotRead() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        Runtime runtime = Library.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);

        PointerByReference ref = new PointerByReference(MAGIC);
        assertNotSame("Reference value passed to native code when it should not be", MAGIC, lib.ptr_ret_pointer(ref, 0));
    }

    @Test public void outOnlyIntReferenceGet() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        Runtime runtime = Library.getRuntime(lib);

        final Pointer MAGIC = Memory.allocateDirect(runtime, 123);


        PointerByReference ref = new PointerByReference(Memory.allocateDirect(runtime, 1));
        lib.ptr_set_pointer(ref, 0, MAGIC);
        assertEquals("Reference value not set", MAGIC, ref.getValue());
    }
}