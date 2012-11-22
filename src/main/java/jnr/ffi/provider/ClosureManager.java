package jnr.ffi.provider;

/**
 *
 */
public interface ClosureManager {
    public abstract <T> T newClosure(Class<? extends T> closureClass, T instance);
    public abstract <T> jnr.ffi.Pointer getClosurePointer(Class<? extends T> closureClass, T instance);
}
