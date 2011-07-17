package jnr.ffi.provider;

import jnr.ffi.Callable;

/**
 *
 */
public interface ClosureManager {
    public abstract <T extends Callable> T newClosure(Class<? extends T> closureClass, T instance);
}
