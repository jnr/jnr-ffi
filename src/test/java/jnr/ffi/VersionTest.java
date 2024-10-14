package jnr.ffi;

import jnr.ffi.annotations.Function;
import jnr.ffi.annotations.Version;

import jnr.ffi.types.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class VersionTest {
    public VersionTest() {
    }

    public static interface TestLib {
        public int answer();
        public int old_answer();
        public int new_answer();

        @Function("answer")
        @Version("VERS_1.0")
        public int answer_1_0();

        @Function("answer")
        @Version("VERS_1.1")
        public int answer_1_1();
    }
    static TestLib testlib;

    public static interface OldTestLib {
        @Version("VERS_1.0")
        public int answer();
    }
    static OldTestLib oldtestlib;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        oldtestlib = TstUtil.loadTestLib(OldTestLib.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testAnswer() {
        int res = testlib.answer();
        assertEquals(42, res);
    }

    @Test
    public void testOldAnswer() {
        int res = testlib.old_answer();
        assertEquals(41, res);
    }

    @Test
    public void testNewAnswer() {
        int res = testlib.new_answer();
        assertEquals(42, res);
    }

    @Test
    public void testAnswer_1_0() {
        int res = testlib.answer_1_0();
        assertEquals(41, res);
    }

    @Test
    public void testAnswer_1_1() {
        int res = testlib.answer_1_1();
        assertEquals(42, res);
    }

    @Test
    public void oldTestAnswer() {
        int res = oldtestlib.answer();
        assertEquals(41, res);
    }
}
