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

package jnr.ffi.provider;

import jnr.ffi.LibraryOption;
import jnr.ffi.Platform;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 */
public abstract class Library {
    private static final class StaticDataHolder {
        private static final List<String> userLibraryPath = new CopyOnWriteArrayList<String>();
        static {
            userLibraryPath.addAll(getPropertyPaths("jaffl.library.path"));
            // Add JNA paths for compatibility
            userLibraryPath.addAll(getPropertyPaths("jna.library.path"));
        }
    };
    
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
        searchPath.addAll(0, jnr.ffi.Library.getLibraryPath(libraryName));
        searchPath.addAll(StaticDataHolder.userLibraryPath);
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

    
}
