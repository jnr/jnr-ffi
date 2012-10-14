package jnr.ffi;

/**
 * Interface to getting/setting a library global variable
 */
public interface Variable<T> {
    public T get();
    public void set(T value);
}
