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


import jnr.ffi.Address;
import jnr.ffi.TstUtil;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AddressByReferenceTest {
    public AddressByReferenceTest() {
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
        Address ptr_ret_pointer(AddressByReference p, int offset);
        void ptr_set_pointer(AddressByReference p, int offset, Address value);
    }
    public static interface TestLibInOnly {
        Address ptr_ret_pointer(@In AddressByReference p, int offset);
        void ptr_set_pointer(@In AddressByReference p, int offset, Address value);
    }
    public static interface TestLibOutOnly {
        Address ptr_ret_pointer(@Out AddressByReference p, int offset);
        void ptr_set_pointer(@Out AddressByReference p, int offset, Address value);
    }
    
    @Test public void inOnlyReferenceSet() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final Address MAGIC = Address.valueOf(0xdeadbeef);
        AddressByReference ref = new AddressByReference(MAGIC);
        assertEquals(MAGIC, lib.ptr_ret_pointer(ref, 0), "Wrong value passed");
    }

    @Test public void inOnlyIntReferenceNotWritten() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final Address MAGIC = Address.valueOf(0xdeadbeefL);
        AddressByReference ref = new AddressByReference(MAGIC);
        lib.ptr_set_pointer(ref, 0, Address.valueOf(0));
        assertEquals(MAGIC, ref.getValue(), "Int reference written when it should not be");
    }

    @Test public void outOnlyIntReferenceNotRead() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final Address MAGIC = Address.valueOf(0xdeadbeef);
        AddressByReference ref = new AddressByReference(MAGIC);
        assertNotEquals(MAGIC, lib.ptr_ret_pointer(ref, 0), "Reference value passed to native code when it should not be");
    }

    @Test public void outOnlyIntReferenceGet() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final Address MAGIC = Address.valueOf(0xdeadbeef);
        AddressByReference ref = new AddressByReference(Address.valueOf(0));
        lib.ptr_set_pointer(ref, 0, MAGIC);
        assertEquals(MAGIC, ref.getValue(), "Reference value not set");
    }
}
