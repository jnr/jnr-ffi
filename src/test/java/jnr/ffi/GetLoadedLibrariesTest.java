package jnr.ffi;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import jnr.ffi.provider.jffi.NativeLibrary;

/**
 * Tests {@link Runtime#getLoadedLibraries()}
 */
public class GetLoadedLibrariesTest {

    private static final String LIB_NAME = "test";

    // A correct library has loaded correctly, is shown in getLoadedLibraries, and when GC'd, is removed
    @Test
    public void testLoadedLibraryCorrect() {
        TestLib testLib = LibraryLoader.loadLibrary(TestLib.class, null, LIB_NAME);
        Assert.assertNotNull(testLib);
        List<NativeLibrary.LoadedLibraryData> loadedLibraries = Runtime.getLoadedLibraries();
        Assert.assertFalse(loadedLibraries.isEmpty());
        Assert.assertTrue(loadedLibraries.stream().anyMatch((NativeLibrary.LoadedLibraryData libData) ->
                libData.getLibraryNames().contains(LIB_NAME)
        ));

        // unload testLib and force a GC
        testLib = null;
        System.gc();

        // testLib is completely gone
        Assert.assertNull(testLib);
        loadedLibraries = Runtime.getLoadedLibraries();
        Assert.assertTrue(loadedLibraries.isEmpty());
    }

    // Library that didn't load yet doesn't show up in getLoadedLibraries
    @Test
    public void testLazyLoadedLibrary() {
        // because of lazy loading behavior, empty mappings don't load anything
        EmptyLib testLib = LibraryLoader.loadLibrary(EmptyLib.class, null, LIB_NAME);
        Assert.assertNotNull(testLib);
        System.gc(); // in case there are lingering libraries
        List<NativeLibrary.LoadedLibraryData> loadedLibraries = Runtime.getLoadedLibraries();
        Assert.assertTrue(loadedLibraries.isEmpty());
    }

    // Library that failed to load obviously won't show up in getLoadedLibraries
    @Test
    public void testFailedLibrary() {
        try {
            TestLibIncorrect testLib = LibraryLoader.loadLibrary(TestLibIncorrect.class, null, LIB_NAME);
        } catch (Throwable e) {
            if (!(e instanceof UnsatisfiedLinkError))
                throw new AssertionError("Expected to throw UnsatisfiedLinkError but actually didn't");
            List<NativeLibrary.LoadedLibraryData> loadedLibraries = Runtime.getLoadedLibraries();
            Assert.assertTrue(loadedLibraries.isEmpty());
        }
    }

    // Multiple instances of same library show up as a loaded library for each one
    @Test
    public void testMultipleInstancesOfSameLibrary() {
        TestLib testLib0 = LibraryLoader.loadLibrary(TestLib.class, null, LIB_NAME);
        Assert.assertNotNull(testLib0);
        TestLib testLib1 = LibraryLoader.loadLibrary(TestLib.class, null, LIB_NAME);
        Assert.assertNotNull(testLib1);
        TestLib testLib2 = LibraryLoader.loadLibrary(TestLib.class, null, LIB_NAME);
        Assert.assertNotNull(testLib2);

        List<NativeLibrary.LoadedLibraryData> loadedLibraries = Runtime.getLoadedLibraries();
        Assert.assertEquals(3, loadedLibraries.size()); // each instance has its own entry
        Assert.assertEquals(1, loadedLibraries.stream().distinct().count()); // but they are all identical in data
    }

    public static interface TestLib {
        int setLastError(int error);
    }

    public static interface TestLibIncorrect {
        int incorrectFunctionShouldNotBeFound(int error);
    }

    public static interface EmptyLib {
    }
}