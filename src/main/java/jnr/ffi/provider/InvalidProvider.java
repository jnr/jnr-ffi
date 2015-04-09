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

package jnr.ffi.provider;

import jnr.ffi.*;
import jnr.ffi.Runtime;

import java.util.Collection;
import java.util.Map;

final class InvalidProvider extends FFIProvider {
    private final String message;
    private final Throwable cause;
    private final jnr.ffi.Runtime runtime;

    InvalidProvider(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
        this.runtime = new InvalidRuntime(message, cause);
    }

    @Override
    public Runtime getRuntime() {
        return runtime;
    }

    @Override
    public <T> LibraryLoader<T> createLibraryLoader(Class<T> interfaceClass) {
        return new LibraryLoader<T>(interfaceClass) {
            @Override
            protected T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames, Collection<String> searchPaths, Map<LibraryOption, Object> options) {
                UnsatisfiedLinkError error = new UnsatisfiedLinkError(message);
                error.initCause(cause);
                throw error;
            }
        };
    }
}
