/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
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

package jnr.ffi.byref;


import jnr.ffi.TstUtil;
import jnr.ffi.Address;
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
 * @author wayne
 */
public class AddressByReferenceTest {
    public AddressByReferenceTest() {
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
        assertEquals("Wrong value passed", MAGIC, lib.ptr_ret_pointer(ref, 0));
    }

    @Test public void inOnlyIntReferenceNotWritten() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final Address MAGIC = Address.valueOf(0xdeadbeefL);
        AddressByReference ref = new AddressByReference(MAGIC);
        lib.ptr_set_pointer(ref, 0, Address.valueOf(0));
        assertEquals("Int reference written when it should not be", MAGIC, ref.getValue());
    }

    @Test public void outOnlyIntReferenceNotRead() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final Address MAGIC = Address.valueOf(0xdeadbeef);
        AddressByReference ref = new AddressByReference(MAGIC);
        assertTrue("Reference value passed to native code when it should not be", !MAGIC.equals(lib.ptr_ret_pointer(ref, 0)));
    }

    @Test public void outOnlyIntReferenceGet() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final Address MAGIC = Address.valueOf(0xdeadbeef);
        AddressByReference ref = new AddressByReference(Address.valueOf(0));
        lib.ptr_set_pointer(ref, 0, MAGIC);
        assertEquals("Reference value not set", MAGIC, ref.getValue());
    }
}
