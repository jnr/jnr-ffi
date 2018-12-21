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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public abstract class AbstractRuntime extends Runtime {
    private final static Logger LOGGER = Logger.getLogger(AbstractRuntime.class.getCanonicalName());
    private final Map<NativeType, Type> types;
    private final long addressMask;
    private final int addressSize;
    private final int longSize;
    private final ByteOrder byteOrder;
    
    public AbstractRuntime(ByteOrder byteOrder, EnumMap<NativeType, Type> typeMap) {
        this.byteOrder = byteOrder;
        
        types = new EnumMap(NativeType.class);
        for (NativeType nt : EnumSet.allOf(NativeType.class)) {
            final Type t = typeMap.get(nt);
            if (t == null) {
                types.put(nt, new BadType(nt.toString()));
                LOGGER.log(Level.SEVERE, "Can't find {0} for {1}", new Object[]{Type.class.getCanonicalName(), nt});
            } else {
                types.put(nt, t);
            }
        }
        
        this.addressSize = types.get(NativeType.ADDRESS).size();
        this.longSize = types.get(NativeType.SLONG).size();
        this.addressMask = addressSize == 4 ? 0xffffffffL : 0xffffffffffffffffL;
    }


    /** Looks up the runtime-specific that corresponds to the pseudo-type */
    public final Type findType(NativeType type) {
        return types.get(type);
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
