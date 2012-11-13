package jnr.ffi;

/**
 *
 */
public abstract class ObjectReferenceManager<T> {

    public static <T> ObjectReferenceManager<T> newInstance(Runtime runtime) {
        return runtime.newObjectReferenceManager();
    }

    public abstract Pointer newReference(T obj);
    public abstract void freeReference(Pointer reference);
    public abstract T getObject(Pointer reference);
}
