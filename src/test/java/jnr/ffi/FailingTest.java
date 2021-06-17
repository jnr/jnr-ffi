package jnr.ffi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FailingTest {

    @Test
    public void shouldFail() {
        // run some code
        long time = 0L;
        for (int i = 0; i < 500_000; i++) {
            time = System.currentTimeMillis();
        }
        assertEquals(0L, time, "This test should fail!");
    }

}
