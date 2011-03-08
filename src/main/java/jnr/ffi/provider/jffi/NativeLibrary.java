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

import jnr.ffi.LibraryOption;
import jnr.ffi.provider.Invoker;
import jnr.ffi.provider.LoadedLibrary;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NativeLibrary extends jnr.ffi.provider.Library {

    private final String[] libraryNames;
    
    private volatile List<com.kenai.jffi.Library> nativeLibraries = Collections.EMPTY_LIST;
    
    NativeLibrary(String name) {
        this.libraryNames = new String[] { name };
    }

    NativeLibrary(String... names) {
        this.libraryNames = (String[]) names.clone();
    }

    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        if (method.getDeclaringClass().equals(LoadedLibrary.class)) {
            return new GetRuntimeInvoker();
        }

        return DefaultInvokerFactory.getInstance().createInvoker(method, this, options);
    }

    public Object libraryLock() {
        return this;
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
            
            lib = com.kenai.jffi.Library.getCachedInstance(libraryName, com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.LOCAL);
            if (lib == null) {
                String path;
                if (libraryName != null && (path = locateLibrary(libraryName)) != null && !libraryName.equals(path)) {
                    lib = com.kenai.jffi.Library.getCachedInstance(path, com.kenai.jffi.Library.LAZY | com.kenai.jffi.Library.LOCAL);
                }
            }
            if (lib == null) {
                throw new UnsatisfiedLinkError(com.kenai.jffi.Library.getLastError());
            }
            libs.add(lib);
        }

        return Collections.unmodifiableList(libs);
    }

    private static final class GetRuntimeInvoker implements Invoker {

        public Object invoke(Object[] parameters) {
            return NativeRuntime.getInstance();
        }
    }
}
