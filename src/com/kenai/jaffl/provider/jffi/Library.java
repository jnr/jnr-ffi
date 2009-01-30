
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.Platform;
import com.kenai.jaffl.provider.Invoker;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Library extends com.kenai.jaffl.provider.Library {
    private static final Map<String, WeakReference<Library>> cache
            = new ConcurrentHashMap<String, WeakReference<Library>>();
    private static final Object cacheLock = new Object();

    private final String libraryName;
    private volatile com.kenai.jffi.Library nativeLibrary;
    public static final Library getInstance(String name) {
        synchronized (cacheLock) {
            WeakReference<Library> ref = cache.get(name);
            Library lib;
            if (ref != null && (lib = ref.get()) != null) {
                return lib;
            }
            lib = new Library(name);
            cache.put(name, new WeakReference<Library>(lib));
            return lib;
        }
    }
    Library(String name) {
        this.libraryName = name;
    }
    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        InvokerFactory factory;
        if (FastIntInvokerFactory.getInstance().isMethodSupported(method)) {
            factory = FastIntInvokerFactory.getInstance();
        } else {
            factory = DefaultInvokerFactory.getInstance();
        }
        return factory.createInvoker(method, this, options);
    }

    public Object libraryLock() {
        return this;
    }
    synchronized com.kenai.jffi.Library getNativeLibrary() {
        if (this.nativeLibrary == null) {
            com.kenai.jffi.Library lib;
            
            lib = com.kenai.jffi.Library.getCachedInstance(libraryName, com.kenai.jffi.Library.LAZY);
            if (lib == null) {
                String path;
                if (libraryName != null && (path = locateLibrary(libraryName)) != null && !libraryName.equals(path)) {
                    lib = com.kenai.jffi.Library.getCachedInstance(path, com.kenai.jffi.Library.LAZY);
                }
            }
            if (lib == null) {
                throw new UnsatisfiedLinkError(com.kenai.jffi.Library.getLastError());
            }
            this.nativeLibrary = lib;
        }
        return nativeLibrary;
    }
}
