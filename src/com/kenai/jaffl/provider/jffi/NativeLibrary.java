
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.LoadedLibrary;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NativeLibrary extends com.kenai.jaffl.provider.Library {

    private final String[] libraryNames;
    
    private volatile List<com.kenai.jffi.Library> nativeLibraries = Collections.EMPTY_LIST;
    
    NativeLibrary(String name) {
        this.libraryNames = new String[] { name };
    }

    NativeLibrary(String... names) {
        this.libraryNames = (String[]) names.clone();
    }

    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        if (method.getDeclaringClass().equals(LoadedLibrary.class)) {
            return new GetRuntimeInvoker();
        }

        return DefaultInvokerFactory.getInstance().createInvoker(method, this, options);
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
            throw new SymbolNotFoundError(com.kenai.jffi.Library.getLastError());
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
        
        for (String libraryName : libraryNames) {
            com.kenai.jffi.Library lib;
            
            lib = com.kenai.jffi.Library.getCachedInstance(libraryName, com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.LOCAL);
            if (lib == null) {
                String path;
                if (libraryName != null && (path = locateLibrary(libraryName)) != null && !libraryName.equals(path)) {
                    lib = com.kenai.jffi.Library.getCachedInstance(path, com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.LOCAL);
                }
            }
            if (lib == null) {
                throw new UnsatisfiedLinkError(com.kenai.jffi.Library.getLastError());
            }
            libs.add(lib);
        }

        return Collections.unmodifiableList(libs);
    }

    private static final class GetRuntimeInvoker implements Invoker {

        public Object invoke(Object[] parameters) {
            return NativeRuntime.getInstance();
        }
    }
}
