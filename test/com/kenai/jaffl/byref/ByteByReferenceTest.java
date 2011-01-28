
package com.kenai.jaffl.byref;

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