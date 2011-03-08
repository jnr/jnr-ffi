
package jnr.ffi.struct;

import jnr.ffi.struct.Struct;
import jnr.ffi.struct.StructUtil;
import jnr.ffi.Library;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.jaffl.TstUtil;
import jnr.ffi.provider.AbstractArrayMemoryIO;
import jnr.ffi.provider.DelegatingMemoryIO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class ArrayTest {
    public static interface TestLib {
        byte ptr_ret_int8_t(s8[] s, int index);
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
    public ArrayTest() {
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
    public static final class s8 extends Struct {
        public final Signed8 s8 = new Signed8();

        public s8() {
            super(runtime);
        }

    }
    public static final class s32 extends Struct {
        public final Signed8 s8 = new Signed8();
        public s32() {
            super(runtime);
        }
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void s8Array() {
        s8[] array = StructUtil.newArray(runtime, s8.class, 10);
        assertEquals("Array length incorrect", 10, array.length);
        for (int i = 0; i < array.length; ++i) {
            assertNotNull("Memory not allocated for array member", StructUtil.getMemory(array[i]));
        }
        Pointer ptr = ((DelegatingMemoryIO) StructUtil.getMemory(array[0])).getDelegatedMemoryIO();
        for (int i = 0; i < array.length; ++i) {
            assertSame("Different backing memory", ptr, ((DelegatingMemoryIO) StructUtil.getMemory(array[i])).getDelegatedMemoryIO());
        }
        if (ptr instanceof AbstractArrayMemoryIO) {
            assertEquals("Incorrect size", array.length, ((AbstractArrayMemoryIO)ptr).length());
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