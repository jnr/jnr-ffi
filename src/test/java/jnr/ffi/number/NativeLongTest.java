package jnr.ffi.number;

import org.junit.jupiter.api.Test;

import jnr.ffi.NativeLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link jnr.ffi.NativeLong}
 */
public class NativeLongTest {

    @Test
    public void NativeLong_valueOf() {
        for (int i = -1000; i < 1000; ++i) {
            assertEquals(i, NativeLong.valueOf(i).intValue(), "Incorrect value from valueOf(" + i + ")");
        }
        for (long i = -1000; i < 1000; ++i) {
            assertEquals(i, NativeLong.valueOf(i).longValue(), "Incorrect value from valueOf(" + i + ")");
        }
    }
}
