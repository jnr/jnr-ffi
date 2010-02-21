
package com.kenai.jaffl;

import com.kenai.jaffl.provider.MemoryManager;

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
        public static final Runtime DEFAULT_RUNTIME = new Default();
    }

    /** Looks up the runtime-specific that corresponds to the pseudo-type */
    public abstract Type findType(NativeType type);

    /** Gets the memory manager instance used by the runtime */
    public abstract MemoryManager getMemoryManager();

    /** Gets the last error that occurred on the current thread for this runtime */
    public abstract int getLastError();

    /** Sets the errno value for this runtime */
    public abstract void setLastError(int error);

    /** The default runtime */
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
