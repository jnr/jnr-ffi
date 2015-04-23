/*
 * Copyright (C) 2011 Wayne Meissner
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

import jnr.ffi.provider.LoadedLibrary;

/**
 *
 */
public abstract class AbstractAsmLibraryInterface implements LoadedLibrary {
    public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
    protected final jnr.ffi.Runtime runtime;

    // Strong ref to keep the library alive
    protected final NativeLibrary library;

    public AbstractAsmLibraryInterface(jnr.ffi.Runtime runtime, NativeLibrary library) {
        this.runtime = runtime;
        this.library = library;
    }

    public final jnr.ffi.Runtime getRuntime() {
        return runtime;
    }

    final NativeLibrary getLibrary() {
        return library;
    }
}
