package jnr.ffi.provider;

import jnr.ffi.*;
import jnr.ffi.Runtime;

import java.util.Collection;
import java.util.Map;

final class InvalidProvider extends FFIProvider {
    private final String message;
    private final Throwable cause;
    private final jnr.ffi.Runtime runtime;

    InvalidProvider(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
        this.runtime = new InvalidRuntime(message, cause);
    }

    @Override
    public Runtime getRuntime() {
        return runtime;
    }

    @Override
    public LibraryLoader createLibraryLoader() {
        return new LibraryLoader() {
            @Override
            protected <T> T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames, Collection<String> searchPaths, Map<LibraryOption, Object> options) {
                UnsatisfiedLinkError error = new UnsatisfiedLinkError(message);
                error.initCause(cause);
                throw error;
            }
        };
    }
}
