package jnr.ffi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformTest {

    private static final Platform.Linux LINUX = new Platform.Linux();
    private static final List<String> DEFAULT_LIB_PATHS = LibraryLoader.DefaultLibPaths.PATHS;
    private File tmpDir;

    @Before
    public void createTempDir() throws IOException {
        tmpDir = mkTmpDir();
    }

    @After
    public void deleteTempDir() throws IOException {
        rmDir(tmpDir);
    }

    private void rmDir(File dir) throws IOException {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    rmDir(child);
                } else {
                    rm(child);
                }
            }
        }
        rm(dir);
    }

    private void rm(File file) throws IOException {
        if (!file.delete()) {
            throw new IOException("Could not delete " + file);
        }
    }

    private File mkTmpDir() throws IOException {
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        String suffix = System.currentTimeMillis() + "-";
        for (int counter = 0; counter < 100; counter++) {
            File tempDir = new File(tmp, suffix + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IOException("Could not create temp dir");
    }

    private void mkLib(String mappedLibName, String version) throws IOException {
        String name = versionedLibName(mappedLibName, version);
        File file = new File(tmpDir, name);
        if (!file.createNewFile())
            throw new IOException("Could not create file " + name);
    }

    private void testVersionComparison(String expected, String... versions) throws Exception {
        String libName = "test";
        String mappedLibName = "lib" + libName + ".so";
        for (String version : versions) {
            mkLib(mappedLibName, version);
        }

        String locatedLibrary = LINUX.locateLibrary(libName, Collections.singletonList(tmpDir.getAbsolutePath()));
        Assert.assertEquals(new File(tmpDir, versionedLibName(mappedLibName, expected)).getAbsolutePath(), locatedLibrary);
    }

    private String versionedLibName(String mappedLibName, String version) {
        String name = mappedLibName;
        if (!version.isEmpty()) {
            name += "." + version;
        }
        return name;
    }

    @Test
    public void testNoVersionComparison() throws Exception {
        testVersionComparison("", "");
    }

    @Test
    public void testSingleComponentVersionComparison() throws Exception {
        testVersionComparison("42", "", "5", "6", "42");
    }

    @Test
    public void testMultiComponentVersionComparison() throws Exception {
        testVersionComparison("42.1.3.4", "", "5", "6.1", "42", "42.1", "42.0.5", "42.1.3.4");
    }

    // library should not be found AND should fail to load
    @Test
    public void testLibraryLocationsFailNotFound() {
        String libName = "should-not-find-me";
        List<String> libLocations = Platform.getNativePlatform().libraryLocations(libName, null);
        Assert.assertTrue(libLocations.isEmpty());
        boolean failed = false;
        try {
            LibraryLoader.loadLibrary(TestLib.class, new HashMap<>(), libName);
        } catch (LinkageError e) {
            failed = true;
        }
        Assert.assertTrue(failed);
    }

    // library should be found but client had unknown symbols, so failed to load even though it exists
    @Test
    public void testLibraryLocationsFailBadSymbol() {
        String libName = "test";
        List<String> libLocations = Platform.getNativePlatform().libraryLocations(libName, null);
        Assert.assertFalse(libLocations.isEmpty());
        boolean failed = false;
        try {
            TestLibIncorrect lib = LibraryLoader.loadLibrary(TestLibIncorrect.class, new HashMap<>(), libName);
            lib.incorrectFunctionShouldNotBeFound(0);
        } catch (LinkageError e) {
            failed = true;
        }
        Assert.assertTrue(failed);
    }

    // library should be found AND should load correctly because correct symbols
    @Test
    public void testLibraryLocationsSuccessful() {
        String libName = "test";
        List<String> libLocations = Platform.getNativePlatform().libraryLocations(libName, null);
        Assert.assertFalse(libLocations.isEmpty());
        TestLib lib = LibraryLoader.loadLibrary(TestLib.class, new HashMap<>(), libName); // should succeed
        Assert.assertNotNull(lib);
    }

    public static interface TestLib {
        int setLastError(int error);
    }

    public static interface TestLibIncorrect {
        int incorrectFunctionShouldNotBeFound(int error);
    }

    private List<String> libPaths() {
        ArrayList<String> libPaths = new ArrayList<>();
        libPaths.add(tmpDir.getAbsolutePath()); // custom first because this how we do it in LibraryLoader
        libPaths.addAll(DEFAULT_LIB_PATHS);
        return libPaths;
    }

    // No preference but custom and system have same version so first found wins which is always custom
    @Test
    public void testDefaultLocateLibrarySameVersions() throws IOException {
        mkLib("libc.so", "6");
        List<String> libPaths = libPaths();

        String locatedPath = LINUX.locateLibrary("c", libPaths, null);
        File locatedFile = new File(locatedPath);

        // locatedFile is in tmpDir, ie our custom won because it had same version
        Assert.assertEquals(tmpDir.getAbsolutePath(), locatedFile.getParentFile().getAbsolutePath());
    }

    // No preference but custom has lower version so system wins
    @Test
    public void testDefaultLocateLibraryLowerCustomVersion() throws IOException {
        mkLib("libc.so", "4");
        List<String> libPaths = libPaths();

        String locatedPath = LINUX.locateLibrary("c", libPaths, null);
        File locatedFile = new File(locatedPath);

        // locatedFile is not in tmpDir, ie system won
        Assert.assertNotEquals(tmpDir.getAbsolutePath(), locatedFile.getParentFile().getAbsolutePath());
    }

    // No preference but custom has no version so system wins
    @Test
    public void testDefaultLocateLibraryNoCustomVersion() throws IOException {
        mkLib("libc.so", "");
        List<String> libPaths = libPaths();

        String locatedPath = LINUX.locateLibrary("c", libPaths, null);
        File locatedFile = new File(locatedPath);

        // locatedFile is not in tmpDir, ie system won
        Assert.assertNotEquals(tmpDir.getAbsolutePath(), locatedFile.getParentFile().getAbsolutePath());
    }

    // No preference but custom has higher version so custom wins
    @Test
    public void testDefaultLocateLibraryHigherCustomVersion() throws IOException {
        mkLib("libc.so", "9");
        List<String> libPaths = libPaths();

        String locatedPath = LINUX.locateLibrary("c", libPaths, null);
        File locatedFile = new File(locatedPath);

        // locatedFile is in tmpDir, ie our custom won because it had same version
        Assert.assertEquals(tmpDir.getAbsolutePath(), locatedFile.getParentFile().getAbsolutePath());
    }

    // Prefer custom but custom has no version so custom wins
    @Test
    public void testPreferCustomLocateLibraryNoVersion() throws IOException {
        mkLib("libc.so", "");
        List<String> libPaths = libPaths();

        Map<LibraryOption, Object> options = Collections.singletonMap(LibraryOption.PreferCustomPaths, true);

        String locatedPath = LINUX.locateLibrary("c", libPaths, options);
        File locatedFile = new File(locatedPath);

        // locatedFile is in tmpDir, ie our custom won because prefer custom is true
        Assert.assertEquals(tmpDir.getAbsolutePath(), locatedFile.getParentFile().getAbsolutePath());
    }

    // Prefer custom but custom has no version so custom wins
    @Test
    public void testPreferCustomLocateLibraryLowerVersion() throws IOException {
        mkLib("libc.so", "2");
        List<String> libPaths = libPaths();

        Map<LibraryOption, Object> options = Collections.singletonMap(LibraryOption.PreferCustomPaths, true);

        String locatedPath = LINUX.locateLibrary("c", libPaths, options);
        File locatedFile = new File(locatedPath);

        // locatedFile is in tmpDir, ie our custom won because prefer custom is true
        Assert.assertEquals(tmpDir.getAbsolutePath(), locatedFile.getParentFile().getAbsolutePath());
    }

    // Prefer custom but custom is actually a default path so that custom wins
    @Test
    public void testPreferCustomLocateLibrarySystemPath() {
        List<String> libCLocations = LINUX.libraryLocations("c", null);
        Assume.assumeTrue(libCLocations.size() > 2);

        Map<LibraryOption, Object> options = Collections.singletonMap(LibraryOption.PreferCustomPaths, true);

        String location = libCLocations.get(1); // get second best location, often something like /usr/lib/
        String customSystemPath = new File(location).getParentFile().getAbsolutePath();

        ArrayList<String> libPaths = new ArrayList<>();
        // user added a path from DEFAULT_LIB_PATHS as a custom path
        libPaths.add(customSystemPath);
        libPaths.addAll(DEFAULT_LIB_PATHS);

        String locatedPath = LINUX.locateLibrary("c", libPaths, options);
        File locatedFile = new File(locatedPath);

        // locatedFile is in customSystemPath, ie our custom won because prefer custom is true
        //    Assert.assertEquals(customSystemPath, locatedFile.getParentFile().getAbsolutePath());
    }
}
