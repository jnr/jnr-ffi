
package com.kenai.jaffl.struct;

import com.kenai.jaffl.Library;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.TstUtil;
import com.kenai.jaffl.struct.AlignmentTest.TestLib.PointerStruct;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class AlignmentTest {
    public static interface TestLib {
        class PointerStruct extends Struct {

            public final Signed8 s8 = new Signed8();
            public final Pointer p = new Pointer();

            public PointerStruct() {
                super(runtime);
            }
        }
    }

    static TestLib testlib;
    static Runtime runtime;
    public AlignmentTest() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    

    @Test public void alignPointer() throws Throwable {
        PointerStruct s = new PointerStruct();
        final int SIZE = runtime.addressSize() == 4 ? 8 : 16;
        assertEquals("Incorrect pointer field alignment", SIZE, StructUtil.getSize(s));
    }

}