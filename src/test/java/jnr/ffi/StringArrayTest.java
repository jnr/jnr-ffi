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
import jnr.ffi.annotations.Out;
import java.nio.charset.Charset;
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
public class StringArrayTest {

    public StringArrayTest() {
    }

    public static interface TestLib {
        String ptr_return_array_element(@In String[] array, int index);
        void ptr_set_array_element(@Out String[] array, int index, Pointer value);
        String ptr_return_array_element(@In CharSequence[] array, int index);
    }
    
    static TestLib testlib;
    static Runtime runtime;
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void lastElementOfStringArrayShouldBeNull() {
        String[] strings = { "test" };
        String result = testlib.ptr_return_array_element(strings, 1);
        assertNull("last element of string array was not null", result);
    }

    @Test public void lastElementOfCharSequenceArrayShouldBeNull() {
        CharSequence[] strings = { "test" };
        String result = testlib.ptr_return_array_element(strings, 1);
        assertNull("last element of string array was not null", result);
    }


    @Test public void firstElementOfStringArrayShouldNotBeNull() {
        final String MAGIC = "test";
        String[] strings = { MAGIC };
        assertNotNull(testlib.ptr_return_array_element(strings, 0));
    }

    @Test public void firstElementOfStringArrayShouldEqualOriginalValue() {
        final String MAGIC = "test";
        String[] strings = { MAGIC };
        assertEquals(MAGIC, testlib.ptr_return_array_element(strings, 0));
    }

    @Test public void elementsSetByNativeCodeShouldBeReloaded() {
        final String MAGIC = "test";
        String[] strings = new String[1];
        Pointer ptr = Memory.allocateDirect(runtime, 1024);
        ptr.putString(0, MAGIC, 1024, Charset.defaultCharset());
        testlib.ptr_set_array_element(strings, 0, ptr);
        assertEquals(MAGIC, strings[0]);
    }
}