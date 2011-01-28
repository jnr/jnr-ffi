

package com.kenai.jaffl;

import com.kenai.jaffl.util.EnumMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class EnumTest {

    public EnumTest() {
    }
    public static enum TestEnum implements EnumMapper.IntegerEnum {
        A(1),
        B(2),
        C(3),
        Z(100);
        TestEnum(int value) {
            this.value = value;
        }
        
        public int intValue() {
            return value;
        }
        private final int value;
    }
    
    public static interface TestLib {
        public int ret_int32_t(TestEnum e);
        public int add_int32_t(TestEnum i1, TestEnum i2);
    }

    public static interface ReturnEnumLib {
        public TestEnum ret_int32_t(int e);
        public TestEnum ret_int32_t(TestEnum e);
        public TestEnum add_int32_t(int i1, int i2);
        public TestEnum add_int32_t(TestEnum i1, TestEnum i2);
    }
    static TestLib testlib;
    static ReturnEnumLib retenum;
    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        retenum = TstUtil.loadTestLib(ReturnEnumLib.class);
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
    @Test
    public void enumArgument() {
        assertEquals("Wrong value returned for enum", TestEnum.Z.intValue(), testlib.ret_int32_t(TestEnum.Z));
        assertEquals("Wrong value returned for enum", TestEnum.C.intValue(), testlib.add_int32_t(TestEnum.A, TestEnum.B));
    }
    @Test
    public void returnEnum() {
        assertEquals("Wrong value returned for enum", TestEnum.Z, retenum.ret_int32_t(TestEnum.Z.intValue()));
        assertEquals("Wrong value returned for enum", TestEnum.C, retenum.add_int32_t(1, 2));
    }
}