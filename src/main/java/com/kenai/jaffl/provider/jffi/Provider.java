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

package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.Runtime;
import java.util.Map;


public final class Provider extends com.kenai.jaffl.FFIProvider {
    private final NativeRuntime runtime;
    
    public Provider() {
        this.runtime = NativeRuntime.getInstance();
    }

    public final Runtime getRuntime() {
        return runtime;
    }

    @Override
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return loadLibrary(new NativeLibrary(libraryName), interfaceClass, libraryOptions);
    }

    @Override
    public <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, String... libraryNames) {
        return loadLibrary(new NativeLibrary(libraryNames), interfaceClass, libraryOptions);
    }

    private <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        final boolean compile = Boolean.parseBoolean(System.getProperty("jaffl.compile.enabled", "true"));

        try {
            if (compile && AsmLibraryLoader.getInstance().isInterfaceSupported(interfaceClass, libraryOptions)) {
                return AsmLibraryLoader.getInstance().loadLibrary(library, interfaceClass, libraryOptions);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        
        return ProxyLibraryLoader.getInstance().loadLibrary(library, interfaceClass, libraryOptions);
    }
}
