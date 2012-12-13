package jnr.ffi;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class InvocationTest {
    public static interface TestLib {
        int ret_int32_t(int i);
    }

    static TestLib testlib;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
    }

    @Test
    public void hammer() throws Throwable {
        for (int i = 0; i < 1000000; i++) {
            assertEquals(i, testlib.ret_int32_t(i));
        }
    }
}
