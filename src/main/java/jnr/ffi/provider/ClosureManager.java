package jnr.ffi.provider;

import jnr.ffi.Closure;

/**
 *
 */
public interface ClosureManager {
    public abstract <T extends Closure> T newClosure(Class<? extends T> closureClass, T instance);
}
