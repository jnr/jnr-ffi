
package com.kenai.jaffl.struct;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.TstUtil;
import com.kenai.jaffl.provider.AbstractArrayMemoryIO;
import com.kenai.jaffl.provider.DelegatingMemoryIO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class ArrayTest {

    public ArrayTest() {
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
    public static final class s8 extends Struct {
        public final Signed8 s8 = new Signed8();
    }
    public static final class s32 extends Struct {
        public final Signed8 s8 = new Signed8();
    }
    private static interface TestLib {
        byte ptr_ret_int8_t(s8[] s, int index);
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void s8Array() {
        TestLib testlib = TstUtil.loadTestLib(TestLib.class);
        s8[] array = StructUtil.newArray(s8.class, 10);
        assertEquals("Array length incorrect", 10, array.length);
        for (int i = 0; i < array.length; ++i) {
            assertNotNull("Memory not allocated for array member", StructUtil.getMemoryIO(array[i]));
        }
        MemoryIO io = ((DelegatingMemoryIO) StructUtil.getMemoryIO(array[0])).getDelegatedMemoryIO();
        for (int i = 0; i < array.length; ++i) {
            assertSame("Different backing memory", io, ((DelegatingMemoryIO) StructUtil.getMemoryIO(array[i])).getDelegatedMemoryIO());
        }
        if (io instanceof AbstractArrayMemoryIO) {
            assertEquals("Incorrect size", array.length, ((AbstractArrayMemoryIO)io).length());
        }
        for (int i = 0; i < array.length; ++i) {
            array[i].s8.set((byte) i);
        }
        for (int i = 0; i < array.length; ++i) {
            assertEquals("Incorrect value written to native memory at index " + i,
                    (byte) i, testlib.ptr_ret_int8_t(array, i));
        }
    }
}