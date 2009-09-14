
package com.kenai.jaffl;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public final class Library {
    private static final Map<String, List<String>> customSearchPaths
            = new ConcurrentHashMap<String, List<String>>();
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
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public static <T> T loadLibrary(Class<T> interfaceClass, String... libraryNames) {
        final Map<LibraryOption, ?> options = Collections.emptyMap();
        return loadLibrary(interfaceClass, options, libraryNames);
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

    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @param libraryName the name of the library to load
     * @param interfaceClass the interface that describes the native library interface
     * @param libraryOptions options
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public static <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions,
            String... libraryNames) {
        return FFIProvider.getProvider().loadLibrary(interfaceClass, libraryOptions, libraryNames);
    }
    
    /**
     * Adds a custom search path for a library
     *
     * @param libraryName the name of the library to search for
     * @param path the path to search for the library in
     */
    public static synchronized final void addLibraryPath(String libraryName, File path) {
        List<String> customPaths = customSearchPaths.get(libraryName);
        if (customPaths == null) {
            customPaths = new CopyOnWriteArrayList<String>();
            customSearchPaths.put(libraryName, customPaths);
        }
        customPaths.add(path.getAbsolutePath());
    }
    public static List<String> getLibraryPath(String libraryName) {
        List<String> customPaths = customSearchPaths.get(libraryName);
        if (customPaths != null) {
            return customPaths;
        }
        return Collections.emptyList();
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
