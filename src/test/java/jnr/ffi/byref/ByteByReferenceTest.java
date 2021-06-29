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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ByteByReferenceTest {
    public ByteByReferenceTest() {
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
        assertEquals(MAGIC, lib.ptr_ret_int8_t(ref, 0), "Wrong value passed");
    }

    @Test public void inOnlyByteReferenceNotWritten() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final byte MAGIC = (byte) 0xef;
        ByteByReference ref = new ByteByReference(MAGIC);
        lib.ptr_set_int8_t(ref, 0, (byte) 0);
        assertEquals(Byte.valueOf(MAGIC), ref.getValue(), "Int reference written when it should not be");
    }

    @Test public void outOnlyByteReferenceNotRead() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final byte MAGIC = (byte) 0xef;
        ByteByReference ref = new ByteByReference(MAGIC);
        assertNotEquals(MAGIC, lib.ptr_ret_int8_t(ref, 0), "Reference value passed to native code when it should not be");
    }

    @Test public void outOnlyByteReferenceGet() {
        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
        final byte MAGIC = (byte) 0xef;
        ByteByReference ref = new ByteByReference((byte) 0);
        lib.ptr_set_int8_t(ref, 0, MAGIC);
        assertEquals(Byte.valueOf(MAGIC), ref.getValue(), "Reference value not set");
    }

}