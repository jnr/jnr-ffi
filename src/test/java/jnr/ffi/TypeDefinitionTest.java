package jnr.ffi;

import jnr.ffi.annotations.LongLong;
import jnr.ffi.types.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class TypeDefinitionTest {

    public TypeDefinitionTest() {


    }
    public static interface TestLib {
        public @int8_t int add_int8_t(@int8_t int i1, @int8_t int i2);
        public @int8_t Long add_int8_t(@int8_t Long i1, @int8_t Integer i2);
    }

    static TestLib testlib;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        testlib = null;
    }

    @Test public void doNothing() {
        testlib.add_int8_t(1, 2);
    }
}
