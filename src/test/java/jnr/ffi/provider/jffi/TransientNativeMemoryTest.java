package jnr.ffi.provider.jffi;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

public class TransientNativeMemoryTest {

    /**
     * The thread that allocates must not keep the class loader of this library alive once nothing else uses it. Here
     * that class loader is a disposable one, like that of an application that loads this library in a class loader of
     * its own.
     */
    @Test public void allocatingDoesNotKeepTheClassLoaderAlive() throws Exception {
        WeakReference<ClassLoader> loader = allocateInDisposableLoader();
        for (int i = 0; i < 10 && loader.get() != null; i++) {
            System.gc();
            Thread.sleep(100);
        }

        assertNull(loader.get(), "class loader was not collected after allocating transient memory");
    }

    private static WeakReference<ClassLoader> allocateInDisposableLoader() throws Exception {
        List<URL> classpath = new ArrayList<URL>();
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            classpath.add(new File(path).toURI().toURL());
        }
        URLClassLoader loader = new URLClassLoader(classpath.toArray(new URL[0]), ClassLoader.getSystemClassLoader().getParent());
        Class<?> runtimeClass = loader.loadClass(jnr.ffi.Runtime.class.getName());
        Object runtime = runtimeClass.getMethod("getSystemRuntime").invoke(null);
        loader.loadClass(TransientNativeMemory.class.getName())
                .getMethod("allocate", runtimeClass, int.class, int.class, boolean.class)
                .invoke(null, runtime, 16, 8, true);
        loader.close();

        return new WeakReference<ClassLoader>(loader);
    }
}
