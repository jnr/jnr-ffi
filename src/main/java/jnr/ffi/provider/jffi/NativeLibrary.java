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

package jnr.ffi.provider.jffi;

import jnr.ffi.Platform;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NativeLibrary {
    private final List<String> libraryNames;
    private final List<String> searchPaths;
    
    private volatile List<com.kenai.jffi.Library> nativeLibraries = Collections.emptyList();

    NativeLibrary(Collection<String> libraryNames, Collection<String> searchPaths) {
        this.libraryNames = Collections.unmodifiableList(new ArrayList<String>(libraryNames));
        this.searchPaths = Collections.unmodifiableList(new ArrayList<String>(searchPaths));
    }

    private String locateLibrary(String libraryName) {
        if (new File(libraryName).isAbsolute()) {
            return libraryName;
        }

        return Platform.getNativePlatform().locateLibrary(libraryName, searchPaths);
    }

    long getSymbolAddress(String name) {
        for (com.kenai.jffi.Library l : getNativeLibraries()) {
            long address = l.getSymbolAddress(name);
            if (address != 0) {
                return address;
            }
        }
        return 0;
    }

    long findSymbolAddress(String name) {
        long address = getSymbolAddress(name);
        if (address == 0) {
            throw new SymbolNotFoundError(com.kenai.jffi.Library.getLastError());
        }
        return address;
    }

    private synchronized List<com.kenai.jffi.Library> getNativeLibraries() {
        if (!this.nativeLibraries.isEmpty()) {
            return nativeLibraries;
        }
        return nativeLibraries = loadNativeLibraries();
    }

    private synchronized List<com.kenai.jffi.Library> loadNativeLibraries() {
        List<com.kenai.jffi.Library> libs = new ArrayList<com.kenai.jffi.Library>();
        
        for (String libraryName : libraryNames) {
            com.kenai.jffi.Library lib;
            
            lib = openLibrary(libraryName);
            if (lib == null) {
                String path;
                if (libraryName != null && (path = locateLibrary(libraryName)) != null && !libraryName.equals(path)) {
                    lib = openLibrary(path);
                }
            }
            if (lib == null) {
                throw new UnsatisfiedLinkError(com.kenai.jffi.Library.getLastError());
            }
            libs.add(lib);
        }

        return Collections.unmodifiableList(libs);
    }

    private static final Pattern BAD_ELF = Pattern.compile("(.*): (invalid ELF header|file too short|invalid file format)");
    private static final Pattern ELF_GROUP = Pattern.compile("GROUP\\s*\\(\\s*(\\S*).*\\)");

    private static com.kenai.jffi.Library openLibrary(String path) {
        com.kenai.jffi.Library lib;

        lib = com.kenai.jffi.Library.getCachedInstance(path, com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.GLOBAL);
        if (lib != null) {
            return lib;
        }
        
        // If dlopen() fails with 'invalid ELF header', then it is likely to be a ld script - parse it for the real library path
        Matcher badElf = BAD_ELF.matcher(com.kenai.jffi.Library.getLastError());
        if (badElf.lookingAt()) {
            File f = new File(badElf.group(1));
            if (f.isFile() && f.length() < (4 * 1024)) {
                Matcher sharedObject = ELF_GROUP.matcher(readAll(f));
                if (sharedObject.find()) {
                    return com.kenai.jffi.Library.getCachedInstance(sharedObject.group(1), com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.GLOBAL);
                }
            }
        }
        
        return null;
    }
    
    private static String readAll(File f) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        
        } finally {
            if (br != null) try { br.close(); } catch (IOException e) { throw new RuntimeException(e); }
        }
    }
}
