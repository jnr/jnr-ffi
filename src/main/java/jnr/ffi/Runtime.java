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
 * Access JNR runtime functionality.
 *
 * <p>
 *     This class is needed by many classes to correctly initialize internal data structures, and each library loaded
 *     has its own instance of this class.
 * <p>
 *     To obtain an instance of this class, use {@link #getRuntime(Object)} on a loaded library.
 * <p>
 *     Example
 *     <pre>
 *     {@code
 *
 *     public interface LibC {
 *         public long write(int fd, Pointer data, long len);
 *     }
 *
 *     LibC library = LibraryLoader.create(LibC.class).load("c");
 *
 *     byte[] bytes = "Hello, World\n".getBytes("UTF-8");
 *
 *     // Use the loaded library's Runtime to allocate memory for the string
 *     jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getRuntime(library);
 *     Pointer buffer = Memory.allocateDirect(runtime, bytes.length);
 *
 *     // Copy the java string data to the native memory, then write the contents to STDOUT
 *     buffer.put(0, bytes, 0, bytes.length);
 *     library.write(1, buffer, bytes.length);
 *     }
 *     </pre>
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
     * @param library A loaded library instance as returned from {@link LibraryLoader#load()}
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
     * @param type The native pseudo-type.
     * @return A {@code Type} instance.
     */
    public abstract Type findType(NativeType type);

    /**
     * Looks up the runtime-specific type that corresponds to the type alias
     *
     * @param type the type alias.
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
     * @param <T> the type parameter of the {@code ObjectReferenceManager}.
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

    /**
     * Indicates whether this <tt>Runtime</tt> instance is compatible with another <tt>Runtime</tt> instance.
     *
     * <p>
     * This is not the same as calling {@link #equals} - this method only indicates whether or not artifacts from the
     * runtime (e.g. memory addresses) are compatible with artifacts from this one.
     * </p>
     *
     * <p>
     * This is mostly for internal use.
     * </p>
     *
     * @param other the other runtime to test for compatibility
     * @return true if the other runtime is compatible with this one
     */
    public abstract boolean isCompatible(jnr.ffi.Runtime other);
}
