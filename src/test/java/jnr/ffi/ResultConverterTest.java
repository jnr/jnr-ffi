package jnr.ffi;

import jnr.ffi.LibraryOption;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.Library;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wayne
 */
public class ResultConverterTest {
    public static final class TestType {
        public final String str;

        public TestType(String str) {
            this.str = str;
        }

    }

    public static interface TestLib {
        TestType strdup(CharSequence cs);
        void cfree(Pointer ptr);
    }

    public static interface Libc {
        Pointer calloc(int nmemb, int size);
        Pointer malloc(int size);
        void free(Pointer ptr);
        void cfree(Pointer ptr);
    }

    public static final class TestTypeResultConverter implements FromNativeConverter<TestType, Pointer> {

        public Class<Pointer> nativeType() {
            return Pointer.class;
        }

        public TestType fromNative(Pointer nativeValue, FromNativeContext context) {
            return new TestType(nativeValue.getString(0));
        }
    }

    static final TypeMapper mapper = new TypeMapper() {

        public FromNativeConverter getFromNativeConverter(Class type) {
            if (TestType.class == type) {
                return new TestTypeResultConverter();
            }
            return null;
        }

        public ToNativeConverter getToNativeConverter(Class type) {
            return null;
        }
    };
    static TestLib testlib;
    static Runtime runtime;

    public ResultConverterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Map<LibraryOption, Object> options = new HashMap<LibraryOption, Object>();
        options.put(LibraryOption.TypeMapper, mapper);
        System.setProperty("jaffl.compiler.dump", "true");
        testlib = Library.loadLibrary("c", TestLib.class, options);
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
    @Test public void testCustomResult() {
        final String MAGIC = "test";
        TestType t = testlib.strdup(MAGIC);
        assertNotNull(t);
        assertEquals("contents not set", MAGIC, t.str);
    }
}