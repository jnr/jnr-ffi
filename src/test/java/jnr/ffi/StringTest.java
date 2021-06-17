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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @BeforeAll
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
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

   
    @Test
    public void testReadOnlyString() {
        String MAGIC = "deadbeef\u0000";
        assertTrue(testlib.string_equals(MAGIC, MAGIC.getBytes()), "String did not equal byte array");
        assertTrue(testlib.string_equals(new StringBuffer(MAGIC), MAGIC.getBytes()), "StringBuffer did not equal byte array");
        assertTrue(testlib.string_equals(new StringBuilder(MAGIC), MAGIC.getBytes()), "StringBuilder did not equal byte array");
    }
    
    @Test
    public void testSetStringBuffer() {
        String MAGIC = "deadbeef";
        StringBuffer buffer = new StringBuffer(1024);
        testlib.string_set(buffer, MAGIC);
        assertEquals(MAGIC, buffer.toString(), "StringBuffer was not set");
    }
    @Test
    public void testSetStringBuilder() {
        String MAGIC = "deadbeef";
        StringBuilder buffer = new StringBuilder(1024);
        testlib.string_set(buffer, MAGIC);
        assertEquals(MAGIC, buffer.toString(), "StringBuilder was not set");
    }
    @Test
    public void testStringBufferAppend() {
        String ORIG = "test ";
        String MAGIC = "deadbeef";
        StringBuffer buffer = new StringBuffer(1024);
        buffer.append(ORIG);
        testlib.string_concat(buffer, MAGIC);
        assertEquals(ORIG + MAGIC, buffer.toString(), "StringBuilder was not set");
    }
    @Test
    public void testStringBuilderAppend() {
        String ORIG = "test ";
        String MAGIC = "deadbeef";
        StringBuilder buffer = new StringBuilder(1024);
        buffer.append(ORIG);
        testlib.string_concat(buffer, MAGIC);
        assertEquals(ORIG + MAGIC, buffer.toString(), "StringBuilder was not set");
    }

    @Test public void testStringParams() {
        assertTrue(testlib.string_equals("test", "test"), "strings should be equal");
        assertFalse(testlib.string_equals("test", "deadbeef"), "strings should not be equal");
    }

    @Test public void stringResult() {
        final String MAGIC = "deadbeef";
        assertEquals(MAGIC, testlib.ptr_return_array_element(new String[] { MAGIC }, 0));
    }
}
