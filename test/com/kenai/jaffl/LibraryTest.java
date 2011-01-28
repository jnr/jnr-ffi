
package com.kenai.jaffl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test library locating/loading
 */
public class LibraryTest {

    public LibraryTest() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    public static interface TestLib {
        int setLastError(int error);
    }
    @Test public void loadTestLib() {
        TestLib lib = TstUtil.loadTestLib(TestLib.class);
        assertNotNull("Could not load libtest", lib);
        // This just forces the library to really load and call a function
        lib.setLastError(0);
    }
}