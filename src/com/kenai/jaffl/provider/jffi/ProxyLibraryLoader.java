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
import com.kenai.jaffl.provider.NativeInvocationHandler;
import java.util.Map;

class ProxyLibraryLoader extends LibraryLoader {
    private static final LibraryLoader INSTANCE = new ProxyLibraryLoader();

    static final LibraryLoader getInstance() {
        return INSTANCE;
    }

    <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return interfaceClass.cast(NativeInvocationHandler.wrapInterface(library, interfaceClass, libraryOptions));
    }

    boolean isInterfaceSupported(Class interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return true;
    }
}
