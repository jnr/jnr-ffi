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

import jnr.ffi.provider.MemoryManager;
import java.nio.ByteOrder;

/**
 * Accessor for various runtime specific parameters
 */
public abstract class Runtime {

    /** Gets the global Runtime for the current FFI provider */
    public static final Runtime getDefault() {
        return SingletonHolder.DEFAULT_RUNTIME;
    }

    /** singleton holder for the default Runtime */
    private static final class SingletonHolder {
        public static final Runtime DEFAULT_RUNTIME = FFIProvider.getProvider().getRuntime();
    }

    /** Looks up the runtime-specific that corresponds to the pseudo-type */
    public abstract Type findType(NativeType type);

    /** 
     * Gets the native memory manager instance for this runtime
     *
     * @return a {@link MemoryManager}
     */
    public abstract MemoryManager getMemoryManager();

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

    /** Gets the address mask for this runtime */
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
     * Retrieves this runtime's native byte order.
     *
     * @return this runtime's byte order
     */
    public abstract ByteOrder byteOrder();
    
}
