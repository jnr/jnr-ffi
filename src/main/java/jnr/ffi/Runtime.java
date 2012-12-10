/*
 * Copyright (C) 2010 Wayne Meissner
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

import jnr.ffi.provider.FFIProvider;
import jnr.ffi.provider.LoadedLibrary;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.ClosureManager;

import java.nio.ByteOrder;

/**
 * Accessor for various runtime specific parameters
 */
public abstract class Runtime {

    /**
     * Gets the global Runtime for the current FFI provider
     *
     * @return The system runtime
     */
    public static Runtime getSystemRuntime() {
        return SingletonHolder.SYSTEM_RUNTIME;
    }

    /**
     * Returns the runtime associated with the library instance.
     *
     * @param library A loaded library instance as returned from {@link LibraryLoader#load(Class)}
     * @return The runtime that loaded the library
     */
    public static Runtime getRuntime(Object library) {
        return ((LoadedLibrary) library).getRuntime();
    }

    /** singleton holder for the default Runtime */
    private static final class SingletonHolder {
        public static final Runtime SYSTEM_RUNTIME = FFIProvider.getSystemProvider().getRuntime();
    }

    /**
     * Looks up the runtime-specific type that corresponds to the pseudo-type
     *
     * @return A {@code Type} instance
     */
    public abstract Type findType(NativeType type);

    /**
     * Looks up the runtime-specific type that corresponds to the type alias
     *
     * @return A {@code Type} instance
     */
    public abstract Type findType(TypeAlias type);

    /** 
     * Gets the native memory manager for this runtime
     *
     * @return The {@link MemoryManager} of the runtime
     */
    public abstract MemoryManager getMemoryManager();

    /**
     * Gets the native closure manager for this runtime
     *
     * @return The {@link ClosureManager} of the runtime
     */
    public abstract ClosureManager getClosureManager();

    /**
     * Creates a new {@code ObjectReferenceManager}
     *
     * @return A new {@link ObjectReferenceManager}
     */
    public abstract <T> ObjectReferenceManager<T> newObjectReferenceManager();

    /**
     * Gets the last native error code.
     * <p>
     * This returns the errno value that was set at the time of the last native
     * function call.
     *
     * @return The errno value.
     */
    public abstract int getLastError();

    /**
     * Sets the native error code.
     *
     * @param error The value to set errno to.
     */
    public abstract void setLastError(int error);

    /**
     * Gets the address mask for this runtime
     *
     * @return The address mask for the runtime.
     */
    public abstract long addressMask();

    /**
     * Gets the size of an address (e.g. a pointer) for this runtime
     *
     * @return The size of an address in bytes.
     */
    public abstract int addressSize();

    /**
     * Gets the size of a C long integer for this runtime
     *
     * @return The size of a C long integer in bytes.
     */
    public abstract int longSize();

    /**
     * Gets the native byte order of the runtime.
     *
     * @return The byte order of the runtime
     */
    public abstract ByteOrder byteOrder();
    
}
