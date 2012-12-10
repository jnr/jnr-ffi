package jnr.ffi.provider.jffi;

import jnr.ffi.*;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
class NativeLibraryLoader extends jnr.ffi.LibraryLoader {

    public <T> T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames, Collection<String> searchPaths,
                             Map<LibraryOption, Object> options) {
        NativeLibrary nativeLibrary = new NativeLibrary(libraryNames, searchPaths);

        try {
            return new AsmLibraryLoader().loadLibrary(nativeLibrary, interfaceClass, options);

        } catch (RuntimeException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
