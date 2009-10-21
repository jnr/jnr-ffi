
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.provider.Invoker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class Library extends com.kenai.jaffl.provider.Library {

    private final String[] libraryNames;
    
    private volatile List<com.kenai.jffi.Library> nativeLibraries = Collections.EMPTY_LIST;

    Library(String[] names, Map<LibraryOption, ?> options) {
        this.libraryNames = (String[]) names.clone();
        if (Boolean.TRUE.equals(options.get(LibraryOption.LoadNow))) {
            nativeLibraries = loadNativeLibraries();
        }
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

    long getSymbolAddress(String name) {
        for (com.kenai.jffi.Library l : getNativeLibraries()) {
            long address = l.getSymbolAddress(name);
            if (address != 0) {
                return address;
            }
        }
        return 0;
    }

    long findSymbolAddress(String name) {
        long address = getSymbolAddress(name);
        if (address == 0) {
            throw new UnsatisfiedLinkError(name + " not found");
        }
        return address;
    }

    private synchronized List<com.kenai.jffi.Library> getNativeLibraries() {
        if (!this.nativeLibraries.isEmpty()) {
            return nativeLibraries;
        }
        return nativeLibraries = loadNativeLibraries();
    }

    private synchronized List<com.kenai.jffi.Library> loadNativeLibraries() {
        List<com.kenai.jffi.Library> libs = new ArrayList<com.kenai.jffi.Library>();
        List<String> errors = new ArrayList<String>(0);

        for (String libraryName : libraryNames) {
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
            libs.add(lib);
        }

        return Collections.unmodifiableList(libs);
    }
}
