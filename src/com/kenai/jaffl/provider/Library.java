
package com.kenai.jaffl.provider;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.Platform;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public abstract class Library {
    private static final List<String> userLibraryPath = new CopyOnWriteArrayList<String>();
    private static final Map<String, List<String>> customSearchPaths
            = new ConcurrentHashMap<String, List<String>>();
    
    public abstract Invoker getInvoker(Method method, Map<LibraryOption, ?> options);
    public abstract Object libraryLock();
    
    public static String locateLibrary(String libraryName) {
        if (new File(libraryName).isAbsolute()) {
            return libraryName;
        }
        List<String> searchPath = new LinkedList<String>();

        //
        // Prepend any custom search paths specifically for this library
        //
        searchPath.addAll(0, com.kenai.jaffl.Library.getLibraryPath(libraryName));
        searchPath.addAll(userLibraryPath);
        String path = Platform.getNativePlatform().locateLibrary(libraryName, searchPath);
        return path != null ? path : null;
    }
    
    private static final List<String> getPropertyPaths(String propName) {
        String value = System.getProperty(propName);
        if (value != null) {
            String[] paths = value.split(File.pathSeparator);
            return new ArrayList<String>(Arrays.asList(paths));
        }
        return Collections.emptyList();
    }

    static {
        userLibraryPath.addAll(getPropertyPaths("jaffl.library.path"));
        // Add JNA paths for compatibility
        userLibraryPath.addAll(getPropertyPaths("jna.library.path"));
    }
}
