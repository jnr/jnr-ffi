package jnr.ffi.provider;

import jnr.ffi.TstUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.lang.reflect.Method;

public class InterfaceScannerTest {
    private static final Method SPLITERATOR;

    public interface Collection<T> extends java.util.Collection<T>{}

    static {
        Method s = null;
        try {
            s = Collection.class.getMethod("spliterator");
        } catch (Exception e) {
            // leave null, tests will be skipped
        }
        SPLITERATOR = s;
    }

    @Test public void loadTestLib() throws Exception {
        // If we're on Java 8+, proceed
        Assume.assumeNotNull(SPLITERATOR);

        Collection lib = TstUtil.loadTestLib(Collection.class);

        // spliterator function does not exist in the test lib, so this only works if we're skipping default methods
        Object spliterator = SPLITERATOR.invoke(lib);

        Assert.assertNotNull(spliterator);
        Assert.assertEquals("java.util.Spliterators$IteratorSpliterator", spliterator.getClass().getName());
    }
}
