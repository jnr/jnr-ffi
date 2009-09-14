
package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.FFIProvider;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.provider.MemoryManager;
import com.kenai.jaffl.provider.NativeInvocationHandler;
import java.util.Map;

/**
 *
 */
public final class JNAProvider extends FFIProvider {
    private final MemoryManager memoryManager = new JNAMemoryManager();

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
    

    @Override
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return interfaceClass.cast(NativeInvocationHandler.wrapInterface(new JNALibrary(libraryName),
                interfaceClass, libraryOptions));
    }

    @Override
    public <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions,
            String... libraryNames) {
        return loadLibrary(libraryNames[0], interfaceClass, libraryOptions);
    }

    @Override
    public int getLastError() {
        return com.sun.jna.Native.getLastError();
    }

    @Override
    public void setLastError(int error) {
        com.sun.jna.Native.setLastError(error);
    }
}
