package jnr.ffi;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import jnr.ffi.annotations.Encoding;
import jnr.ffi.annotations.Meta;
import jnr.ffi.provider.FFIProvider;
import jnr.ffi.types.size_t;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class VarargsTest {
    public static interface C {
        public int snprintf(Pointer buffer, @size_t long bufferSize, String format, Object... varargs);
    }

    static C c;

    @BeforeClass
    public static void setUpClass() throws Exception {
        LibraryLoader<C> loader = FFIProvider.getSystemProvider().createLibraryLoader(C.class);
        c = loader.load("c");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        c = null;
    }

    @Test public void testSizeT() {
        Pointer ptr = Runtime.getRuntime(c).getMemoryManager().allocate(5000);
        int size = c.snprintf(ptr, 5000, "%zu", size_t.class, 12345);
        Assert.assertEquals(5, size);
        String result = ptr.getString(0, size, Charset.defaultCharset());
        Assert.assertEquals("12345", result);
    }

    @Test public void testMetaAscii() throws UnsupportedEncodingException {
        Pointer ptr = Runtime.getRuntime(c).getMemoryManager().allocate(5000);
        int size = c.snprintf(ptr, 5000, "%s", AsciiEncoding.class, "\u7684");
        Assert.assertEquals(1, size);
        String result = ptr.getString(0, size, Charset.forName("ASCII"));
        String expected = new String("\u7684".getBytes("ASCII"), "ASCII");
        Assert.assertEquals(expected, result);
    }

    @Test public void testMetaUtf8() throws UnsupportedEncodingException {
        Pointer ptr = Runtime.getRuntime(c).getMemoryManager().allocate(5000);
        int size = c.snprintf(ptr, 5000, "%s", UTF8Encoding.class, "\u7684");
        Assert.assertEquals(3, size);
        String result = ptr.getString(0, size, Charset.forName("UTF-8"));
        String expected = new String("\u7684".getBytes("UTF-8"), "UTF-8");
        Assert.assertEquals(expected, result);
    }

    @Meta
    @Encoding(value="ASCII")
    public static @interface AsciiEncoding {
    }

    @Meta
    @Encoding(value="UTF-8")
    public static @interface UTF8Encoding {
    }
}
