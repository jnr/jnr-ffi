package jnr.ffi;

import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.ClosureManager;

import java.nio.ByteOrder;

/**
 * A {@link Runtime} subclass that throws exceptions for all methods
 */
class InvalidRuntime extends Runtime {
    private final String message;
    private final Throwable cause;

    InvalidRuntime(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public Type findType(NativeType type) {
        throw newLoadError();
    }

    @Override
    public Type findType(TypeAlias type) {
        throw newLoadError();
    }

    @Override
    public MemoryManager getMemoryManager() {
        throw newLoadError();
    }

    @Override
    public ClosureManager getClosureManager() {
        throw newLoadError();
    }

    @Override
    public ObjectReferenceManager newObjectReferenceManager() {
        throw newLoadError();
    }

    @Override
    public int getLastError() {
        throw newLoadError();
    }

    @Override
    public void setLastError(int error) {
        throw newLoadError();
    }

    @Override
    public long addressMask() {
        throw newLoadError();
    }

    @Override
    public int addressSize() {
        throw newLoadError();
    }

    @Override
    public int longSize() {
        throw newLoadError();
    }

    @Override
    public ByteOrder byteOrder() {
        throw newLoadError();
    }

    private UnsatisfiedLinkError newLoadError() {
        UnsatisfiedLinkError error = new UnsatisfiedLinkError(message);
        error.initCause(cause);
        throw error;
    }
}
