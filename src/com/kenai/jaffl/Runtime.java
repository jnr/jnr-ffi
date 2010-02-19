
package com.kenai.jaffl;

import com.kenai.jaffl.provider.NativeType;

/**
 * Accessor for various runtime specific parameters
 */
public abstract class Runtime {

    public static final Runtime DEFAULT = new Default();

    public abstract Type findType(NativeType type);


    private static final class Default extends Runtime {
        public final Type findType(NativeType type) {
            return FFIProvider.getProvider().getType(type);
        }
    }
}
