
package com.kenai.jaffl;

import java.util.Collections;
import java.util.Map;

/**
 *
 */
public final class Library {
    private final String name;
    private Library(String libraryName) {
        name = libraryName;
    }
    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     * 
     * @param libraryName the name of the library to load
     * @param interfaceClass the interface that describes the native library interface
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public static <T> T loadLibrary(String libraryName, Class<T> interfaceClass) {
        final Map<LibraryOption, ?> options = Collections.emptyMap();
        return loadLibrary(libraryName, interfaceClass, options);
    }
    
    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     * 
     * @param libraryName the name of the library to load
     * @param interfaceClass the interface that describes the native library interface
     * @param libraryOptions options
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public static <T> T loadLibrary(String libraryName, Class<T> interfaceClass,
            Map<LibraryOption, ?> libraryOptions) {
        return FFIProvider.getProvider().loadLibrary(libraryName, interfaceClass, libraryOptions);
    }
    public static final Library getInstance(String libraryName) {
        return new Library(libraryName);
    }
    public boolean hasFunction(String method) {
        return false;
    }
    public String getName() {
        return name;
    }
}
