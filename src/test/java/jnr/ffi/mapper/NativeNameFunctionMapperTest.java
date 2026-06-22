package jnr.ffi.mapper;

import jnr.ffi.LibraryLoader;
import jnr.ffi.annotations.NativeName;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

/**
 * Created by Andrew
 * on 01.07.2017.
 */
public class NativeNameFunctionMapperTest {
    static Lib lib;

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        lib = LibraryLoader.create(Lib.class)
                           .mapper(new NativeNameFunctionMapper())
                           .load("test");
    }

    @Test
    public void testSameFunctionResult()
            throws Exception {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int randomInt = random.nextInt();
            Assert.assertEquals(lib.ret_int32_t(randomInt), lib.returnInt(randomInt));
        }
    }

    public static interface Lib {
        public int ret_int32_t(int value);

        @NativeName("ret_int32_t")
        public int returnInt(int value);
    }
}
