package jnr.ffi;

import jnr.ffi.mapper.AnnotatedMappedTypeTest;
import jnr.ffi.types.size_t;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertSame;

/**
 * @author Andrew Yefanov.
 * @since 30.08.2017.
 */
public class PointerWrapperTest {
    private static final class CustomPointer extends PointerWrapper {
        private CustomPointer(Pointer pointer) {
            super(pointer);
        }
    }

    public static interface TestLib {
        CustomPointer ptr_malloc(@size_t int size);
        void ptr_free(AnnotatedMappedTypeTest.CustomPointer ptr);
    }

    static AnnotatedMappedTypeTest.TestLib testlib;
    static Runtime runtime;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(AnnotatedMappedTypeTest.TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }

    @Test
    public void returnsInstanceOfCorrectClass() {
        assertSame(AnnotatedMappedTypeTest.CustomPointer.class, testlib.ptr_malloc(1).getClass());
    }

    @Test public void toNative() {
        testlib.ptr_free(testlib.ptr_malloc(1));
    }

}