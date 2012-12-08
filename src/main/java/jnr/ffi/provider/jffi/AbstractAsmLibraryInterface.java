package jnr.ffi.provider.jffi;

import jnr.ffi.provider.LoadedLibrary;

/**
 *
 */
public abstract class AbstractAsmLibraryInterface implements LoadedLibrary {
    public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
    protected final jnr.ffi.Runtime runtime;

    // Strong ref to keep the library alive
    protected final NativeLibrary library;

    public AbstractAsmLibraryInterface(jnr.ffi.Runtime runtime, NativeLibrary library) {
        this.runtime = runtime;
        this.library = library;
    }

    public final jnr.ffi.Runtime getRuntime() {
        return runtime;
    }

    final NativeLibrary getLibrary() {
        return library;
    }
}
