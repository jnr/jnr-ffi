/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi;

import jnr.ffi.provider.LoadedLibrary;
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
    
    /** The name of this library */
    private final String name;

    private Library(String libraryName) {
        name = libraryName;
    }

    public static final Runtime getRuntime(Object obj) {
        return ((LoadedLibrary) obj).getRuntime();
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
        return FFIProvider.getSystemProvider().loadLibrary(libraryName, interfaceClass, libraryOptions);
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
        return FFIProvider.getSystemProvider().loadLibrary(interfaceClass, libraryOptions, libraryNames);
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

    /**
     * Gets the custom search path for a library.
     *
     * @param libraryName The library to retrieve the path for.
     *
     * @return A <tt>List</tt> of <tt>String</tt> instances.
     */
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
    
    /**
     * Gets the name of this library
     * 
     * @return The name of this library as a <tt>String</tt> 
     */
    public String getName() {
        return name;
    }
}
