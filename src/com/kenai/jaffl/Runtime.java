
package com.kenai.jaffl;

import com.kenai.jaffl.provider.MemoryManager;

/**
 * Accessor for various runtime specific parameters
 */
public abstract class Runtime {

    public static final Runtime DEFAULT = new Default();

    public abstract Type findType(NativeType type);
    public abstract MemoryManager getMemoryManager();

    public abstract int getLastError();
    public abstract void setLastError(int error);

    private static final class Default extends Runtime {
        public final Type findType(NativeType type) {
            return FFIProvider.getProvider().getType(type);
        }

        @Override
        public MemoryManager getMemoryManager() {
            return FFIProvider.getProvider().getMemoryManager();
        }

        public final int getLastError() {
            return FFIProvider.getProvider().getLastError();
        }

        public final void setLastError(int error) {
            FFIProvider.getProvider().setLastError(error);
        }

    }
}
