package jnr.ffi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LibraryLoaderTest {
    public static interface TestLib {
        int setLastError(int error);
    }

    @Test public void loadShouldNotThrowExceptions() {
        try {
            LibraryLoader.create(TestLib.class).load("non-existant-library");
        } catch (Throwable t) {
            fail("load raised exception " + t);
        }
    }
    
    @Test(expected = UnsatisfiedLinkError.class)
    public void failImmediatelyShouldThrowULE() {
        LibraryLoader.create(TestLib.class).failImmediately().load("non-existant-library");
    }

    @Test(expected = UnsatisfiedLinkError.class)
    public void invocationOnFailedLoadShouldThrowULE() {
        TestLib lib = LibraryLoader.create(TestLib.class).failImmediately().load("non-existant-library");
        lib.setLastError(0);
    }
}
