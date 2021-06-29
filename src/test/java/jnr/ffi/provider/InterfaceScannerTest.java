package jnr.ffi.provider;

import jnr.ffi.TstUtil;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class InterfaceScannerTest {
    private static final Method SPLITERATOR;

    static {
        Method s = null;
        try {
            Collection.class.getMethod("spliterator");
        } catch (Exception e) {
            // leave null, tests will be skipped
        }
        SPLITERATOR = s;
    }

    @Test public void loadTestLib() throws Exception {
        // If we're on Java 8+, proceed
        assumeTrue(SPLITERATOR != null);

        Collection lib = TstUtil.loadTestLib(Collection.class);

        // spliterator function does not exist in the test lib, so this only works if we're skipping default methods
        Object spliterator = SPLITERATOR.invoke(lib);

        assertNotNull(spliterator);
        assertEquals("java.util.Spliterator", spliterator.getClass().getName());
    }
}
