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

/**
 * Options that apply to a library
 */
public enum LibraryOption {
    /**
     * Function calls should save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link jnr.ffi.annotations.IgnoreError} annotation.
     */
    SaveError,
    /**
     * Function calls should NOT save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link jnr.ffi.annotations.SaveError} annotation.
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
     * @see CallingConvention
     */
    CallingConvention,

    /**
     * Load the library into memory immediately, instead of lazily loading it
     */
    LoadNow
}
