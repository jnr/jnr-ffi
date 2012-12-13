/*
 * Copyright (C) 2012 Wayne Meissner
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

import jnr.ffi.*;

import java.util.Collection;
import java.util.Map;

import static jnr.ffi.provider.jffi.Util.getBooleanProperty;

/**
 *
 */
class NativeLibraryLoader<T>  extends jnr.ffi.LibraryLoader<T> {
    static final boolean ASM_ENABLED = getBooleanProperty("jnr.ffi.asm.enabled", true);

    NativeLibraryLoader(Class<T> interfaceClass) {
        super(interfaceClass);
    }

    public T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames, Collection<String> searchPaths,
                             Map<LibraryOption, Object> options) {
        NativeLibrary nativeLibrary = new NativeLibrary(libraryNames, searchPaths);

        try {
            return ASM_ENABLED
                ? new AsmLibraryLoader().loadLibrary(nativeLibrary, interfaceClass, options)
                : new ReflectionLibraryLoader().loadLibrary(nativeLibrary, interfaceClass, options);

        } catch (RuntimeException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
