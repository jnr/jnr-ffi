package jnr.ffi.provider;

/**
 *
 */
public interface ClosureManager {
    public abstract <T extends Object> T newClosure(Class<? extends T> closureClass, T instance);
}
