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

import java.util.Map;

/**
 * Options that apply to a library
 */
public enum LibraryOption {
    /**
     * Function calls should save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link jnr.ffi.annotations.IgnoreError} annotation.
     *
     * @see LibraryLoader#saveError(Map, boolean, boolean)
     */
    SaveError,

    /**
     * Function calls should NOT save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link jnr.ffi.annotations.SaveError} annotation.
     *
     * @see LibraryLoader#saveError(Map, boolean, boolean)
     */
    IgnoreError,
    
    /**
     * A type mapper which maps java types to native types is present.
     */
    TypeMapper,

    /**
     * A function mapper which maps from java function names to native function names.
     */
    FunctionMapper,

    /**
     * The type of calling convention.
     *
     * @see CallingConvention
     */
    CallingConvention,

    /**
     * Load the library into memory immediately, instead of lazily loading it
     */
    LoadNow,

    /**
     * Relevant for GNU/Linux {@link Platform.Linux} only
     *
     * Prefer custom paths over system paths when loading a library, even if the custom path has a lower version.
     *
     * By default JNR-FFI will choose the library of the desired name with the highest version, whether in the custom
     * paths or the system default paths.
     *
     * This can be a problem if you are distributing your own library for example {@code libfoo.so} and the system
     * paths <i>happen</i> to have a {@code libfoo.so.4} for example, in which case JNR-FFI will prefer the
     * higher version despite your explicit custom paths.
     *
     * By using this option, JNR-FFI will know to prefer the custom paths, even if they have a lower version, this
     * ensures consistent behaviors across environments.
     */
    PreferCustomPaths
}
