
package jnr.ffi.struct;

import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.struct.PaddingTest.TestLib.LongPadding;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class PaddingTest {
    static TestLib testlib;
    static Runtime runtime;

    public static interface TestLib {

        static final class LongPadding extends Struct {

            public final Signed8 s8 = new Signed8();
            public final Padding pad = new Padding(NativeType.SLONG, 3);

            public LongPadding() {
                super(runtime);
            }
        }
    }
    public PaddingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Library.getRuntime(testlib);
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

    @Test public void longPadding() throws Throwable {
        Type longType = runtime.findType(NativeType.SLONG);
        final int SIZE = longType.alignment() + (longType.size() * 3);
        assertEquals("incorrect size", SIZE, Struct.size(new LongPadding()));
    }

}