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
public class ByteByReferenceTest {
    public ByteByReferenceTest() {
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
        byte ptr_ret_int8_t(ByteByReference p, int offset);
        void ptr_set_int_t(ByteByReference p, int offset, byte value);
    }
    
    public static interface TestLibInOnly {
        byte ptr_ret_int8_t(@In ByteByReference p, int offset);
        void ptr_set_int8_t(@In ByteByReference p, int offset, byte value);
    }
    
    public static interface TestLibOutOnly {
        byte ptr_ret_int8_t(@Out ByteByReference p, int offset);
        void ptr_set_int8_t(@Out ByteByReference p, int offset, byte value);
    }
    
    @Test public void inOnlyReferenceSet() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final byte MAGIC = (byte) 0xef;
        ByteByReference ref = new ByteByReference(MAGIC);
        assertEquals("Wrong value passed", MAGIC, lib.ptr_ret_int8_t(ref, 0));
    }

    @Test public void inOnlyByteReferenceNotWritten() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final byte MAGIC = (byte) 0xef;
        ByteByReference ref = new ByteByReference(MAGIC);
        lib.ptr_set_int8_t(ref, 0, (byte) 0);
        assertEquals("Int reference written when it should not be", Byte.valueOf(MAGIC), ref.getValue());
    }

    @Test public void outOnlyByteReferenceNotRead() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final byte MAGIC = (byte) 0xef;
        ByteByReference ref = new ByteByReference(MAGIC);
        assertTrue("Reference value passed to native code when it should not be", MAGIC != lib.ptr_ret_int8_t(ref, 0));
    }

    @Test public void outOnlyByteReferenceGet() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final byte MAGIC = (byte) 0xef;
        ByteByReference ref = new ByteByReference((byte) 0);
        lib.ptr_set_int8_t(ref, 0, MAGIC);
        assertEquals("Reference value not set", Byte.valueOf(MAGIC), ref.getValue());
    }

}