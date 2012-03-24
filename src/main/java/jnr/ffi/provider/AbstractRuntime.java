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

package jnr.ffi.provider;

import jnr.ffi.NativeType;
import jnr.ffi.Runtime;
import jnr.ffi.Type;

import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.EnumSet;

/**
 *
 */
public abstract class AbstractRuntime extends Runtime {
    private final Type[] types;
    private final long addressMask;
    private final int addressSize;
    private final int longSize;
    private final ByteOrder byteOrder;
    
    public AbstractRuntime(ByteOrder byteOrder, EnumMap<NativeType, Type> typeMap) {
        this.byteOrder = byteOrder;
        
        EnumSet<NativeType> nativeTypes = EnumSet.allOf(NativeType.class);
        types = new Type[nativeTypes.size()];
        for (NativeType t : nativeTypes) {
            types[t.ordinal()] = typeMap.containsKey(t) ? typeMap.get(t) : new BadType(t.toString());
        }
        
        this.addressSize = types[NativeType.ADDRESS.ordinal()].size();
        this.longSize = types[NativeType.SLONG.ordinal()].size();
        this.addressMask = addressSize == 4 ? 0xffffffffL : 0xffffffffffffffffL;
    }


    /** Looks up the runtime-specific that corresponds to the pseudo-type */
    public final Type findType(NativeType type) {
        return types[type.ordinal()];
    }

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
    public final long addressMask() {
        return addressMask;
    }

    /**
     * Gets the size of an address (e.g. a pointer) for this runtime
     *
     * @return The size of an address in bytes.
     */
    public final int addressSize() {
        return addressSize;
    }

    /**
     * Gets the size of a C long integer for this runtime
     *
     * @return The size of a C long integer in bytes.
     */
    public final int longSize() {
        return longSize;
    }

    /**
     * Retrieves this runtime's native byte order.
     *
     * @return this runtime's byte order
     */
    public final ByteOrder byteOrder() {
        return byteOrder;
    }
}
