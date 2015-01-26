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

import jnr.ffi.provider.FFIProvider;
import jnr.ffi.provider.LoadedLibrary;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @deprecated Use {@link LibraryLoader} instead.
 */
public final class Library {
    private static final Map<String, List<String>> customSearchPaths
            = new ConcurrentHashMap<String, List<String>>();
    
    /** The name of this library */
    private final String name;

    private Library(String libraryName) {
        name = libraryName;
    }

    /**
     * Gets the {@link Runtime} that loaded the library interface.
     *
     * @deprecated Use {@link Runtime#getRuntime(Object)}
     * @param library A library implementation as returned from {@link LibraryLoader#load()}
     * @return The runtime that loaded the library.
     */
    public static Runtime getRuntime(Object library) {
        return ((LoadedLibrary) library).getRuntime();
    }

    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @deprecated see {@link LibraryLoader} for the preferred interface to loading libraries.
     * @param <T> the interface class.
     * @param libraryName the name of the library to load
     * @param interfaceClass the interface that describes the native library interface
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public static <T> T loadLibrary(String libraryName, Class<T> interfaceClass) {
        return loadLibrary(interfaceClass, libraryName);
    }

    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @deprecated see {@link LibraryLoader} for the preferred interface to loading libraries.
     * @param <T> the interface type.
     * @param libraryNames the name of the library to load
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
     * @deprecated see {@link LibraryLoader} for the preferred interface to loading libraries.
     * @param <T> the interface type.
     * @param libraryName the name of the library to load
     * @param interfaceClass the interface that describes the native library interface
     * @param libraryOptions options
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public static <T> T loadLibrary(String libraryName, Class<T> interfaceClass,
            Map<LibraryOption, ?> libraryOptions) {
        return loadLibrary(interfaceClass, libraryOptions, libraryName);
    }

    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @deprecated see {@link LibraryLoader} for the preferred interface to loading libraries.
     * @param <T> the interface type.
     * @param libraryNames the name of the library to load
     * @param interfaceClass the interface that describes the native library interface
     * @param libraryOptions options
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public static <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions,
            String... libraryNames) {
        LibraryLoader<T> loader = FFIProvider.getSystemProvider().createLibraryLoader(interfaceClass);

        for (String libraryName : libraryNames) {
            loader.library(libraryName);
            for (String path : getLibraryPath(libraryName)) {
                loader.search(path);
            }
        }

        for (Map.Entry<LibraryOption, ?> option : libraryOptions.entrySet()) {
            loader.option(option.getKey(), option.getValue());
        }

        return loader.failImmediately().load();
    }
    
    /**
     * Adds a custom search path for a library
     *
     * @deprecated see {@link LibraryLoader} for the preferred interface to loading libraries.
     * @param libraryName the name of the library to search for
     * @param path the path to search for the library in
     */
    public static synchronized void addLibraryPath(String libraryName, File path) {
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
     * @deprecated see {@link LibraryLoader} for the preferred interface to loading libraries.
     * @param libraryName The library to retrieve the path for.
     * @return A <tt>List</tt> of <tt>String</tt> instances.
     */
    public static List<String> getLibraryPath(String libraryName) {
        List<String> customPaths = customSearchPaths.get(libraryName);
        if (customPaths != null) {
            return customPaths;
        }
        return Collections.emptyList();
    }

    @Deprecated
    public static Library getInstance(String libraryName) {
        return new Library(libraryName);
    }
    
    @Deprecated
    public String getName() {
        return name;
    }
}
