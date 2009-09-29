package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.provider.NativeInvocationHandler;
import java.util.Map;

class ProxyLibraryLoader extends LibraryLoader {
    private static final LibraryLoader INSTANCE = new ProxyLibraryLoader();

    static final LibraryLoader getInstance() {
        return INSTANCE;
    }

    <T> T loadLibrary(Library library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return interfaceClass.cast(NativeInvocationHandler.wrapInterface(library, interfaceClass, libraryOptions));
    }

    boolean isInterfaceSupported(Class interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return true;
    }
}
