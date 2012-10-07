package jnr.ffi;

import java.util.Map;

final class InvalidProvider extends FFIProvider {
    private final String message;
    private final Throwable cause;
    private final Runtime runtime;

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
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        throw newLoadError();
    }

    @Override
    public <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, String... libraryNames) {
        throw newLoadError();
    }

    private UnsatisfiedLinkError newLoadError() {
        UnsatisfiedLinkError error = new UnsatisfiedLinkError(message);
        error.initCause(cause);
        throw error;
    }
}
