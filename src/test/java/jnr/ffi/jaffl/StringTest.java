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

package jnr.ffi.jaffl;

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
public class StringTest {

    public StringTest() {
    }
    public static interface TestLib {
        boolean string_equals(CharSequence s1, byte[] s2);        
        void string_set(StringBuffer dst, CharSequence src);
        void string_set(StringBuilder dst, CharSequence src);
        void string_concat(StringBuilder dst, CharSequence src);
        void string_concat(StringBuffer dst, CharSequence src);
    }
    static TestLib testlib;
    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
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

   
    @Test
    public void testReadOnlyString() {
        String MAGIC = "deadbeef\u0000";
        assertTrue("String did not equal byte array", testlib.string_equals(MAGIC, MAGIC.getBytes()));
        assertTrue("StringBuffer did not equal byte array", testlib.string_equals(new StringBuffer(MAGIC), MAGIC.getBytes()));
        assertTrue("StringBuilder did not equal byte array", testlib.string_equals(new StringBuilder(MAGIC), MAGIC.getBytes()));        
    }
    
    @Test
    public void testSetStringBuffer() {
        String MAGIC = "deadbeef";
        StringBuffer buffer = new StringBuffer(1024);
        testlib.string_set(buffer, MAGIC);
        assertEquals("StringBuffer was not set", MAGIC, buffer.toString());        
    }
    @Test
    public void testSetStringBuilder() {
        String MAGIC = "deadbeef";
        StringBuilder buffer = new StringBuilder(1024);
        testlib.string_set(buffer, MAGIC);
        assertEquals("StringBuilder was not set", MAGIC, buffer.toString());        
    }
    @Test
    public void testStringBufferAppend() {
        String ORIG = "test ";
        String MAGIC = "deadbeef";
        StringBuffer buffer = new StringBuffer(1024);
        buffer.append(ORIG);
        testlib.string_concat(buffer, MAGIC);
        assertEquals("StringBuilder was not set", ORIG + MAGIC, buffer.toString());        
    }
    @Test
    public void testStringBuilderAppend() {
        String ORIG = "test ";
        String MAGIC = "deadbeef";
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(ORIG);
        testlib.string_concat(buffer, MAGIC);
        assertEquals("StringBuilder was not set", ORIG + MAGIC, buffer.toString());        
    }
}