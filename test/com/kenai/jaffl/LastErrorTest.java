
package com.kenai.jaffl;

import com.kenai.jaffl.annotations.SaveError;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class LastErrorTest {
    public static interface TestLib {
        @SaveError
        int setLastError(int error);
    }
    
    public LastErrorTest() {
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
    
    @Test public void testLastError() {
        TestLib lib = TstUtil.loadTestLib(TestLib.class);
        final int MAGIC = 0xdeadbeef;
        lib.setLastError(MAGIC);
        assertEquals("Wrong errno value", MAGIC, LastError.getLastError());
    }
}