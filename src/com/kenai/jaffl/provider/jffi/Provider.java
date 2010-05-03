
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.Runtime;
import java.util.Map;


public final class Provider extends com.kenai.jaffl.FFIProvider {
    private final NativeRuntime runtime;
    
    public Provider() {
        this.runtime = NativeRuntime.getInstance();
    }

    public final Runtime getRuntime() {
        return runtime;
    }

    @Override
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return loadLibrary(new NativeLibrary(libraryName), interfaceClass, libraryOptions);
    }

    @Override
    public <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, String... libraryNames) {
        return loadLibrary(new NativeLibrary(libraryNames), interfaceClass, libraryOptions);
    }

    private <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        final boolean compile = Boolean.parseBoolean(System.getProperty("jaffl.compile.enabled", "true"));

        try {
            if (compile && AsmLibraryLoader.getInstance().isInterfaceSupported(interfaceClass, libraryOptions)) {
                return AsmLibraryLoader.getInstance().loadLibrary(library, interfaceClass, libraryOptions);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        
        return ProxyLibraryLoader.getInstance().loadLibrary(library, interfaceClass, libraryOptions);
    }
}
