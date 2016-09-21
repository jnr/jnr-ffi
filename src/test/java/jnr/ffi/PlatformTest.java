package jnr.ffi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class PlatformTest {
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

    private void testVersionComparison(String expected, String... versions) throws Exception {
        Platform.Linux linux = new Platform.Linux();
        String libName = "test";
        String mappedLibName = "lib" + libName + ".so";
        for (String version : versions) {
            String name = versionedLibName(mappedLibName, version);
            if (!new File(tmpDir, name).createNewFile()) {
                throw new IOException("Could not create file " + name);
            }
        }

        String locatedLibrary = linux.locateLibrary(libName, Collections.singletonList(tmpDir.getAbsolutePath()));
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
}
