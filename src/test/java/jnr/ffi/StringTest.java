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

package jnr.ffi;

import jnr.ffi.annotations.In;
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
        boolean string_equals(String s1, String s2);
        boolean string_equals(CharSequence s1, byte[] s2);        
        void string_set(StringBuffer dst, CharSequence src);
        void string_set(StringBuilder dst, CharSequence src);
        void string_concat(StringBuilder dst, CharSequence src);
        void string_concat(StringBuffer dst, CharSequence src);
        String ptr_return_array_element(@In String[] array, int index);
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

    @Test public void testStringParams() {
        assertTrue("strings should be equal", testlib.string_equals("test", "test"));
        assertFalse("strings should not be equal", testlib.string_equals("test", "deadbeef"));
    }

    @Test public void stringResult() {
        final String MAGIC = "deadbeef";
        assertEquals(MAGIC, testlib.ptr_return_array_element(new String[] { MAGIC }, 0));
    }
}
