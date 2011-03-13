package jnr.ffi.provider;

import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public final class DefaultObjectReferenceManager extends ObjectReferenceManager {
    private final Runtime runtime;
    private final ConcurrentMap<Pointer, Object> references = new ConcurrentHashMap<Pointer, Object>();
    private final AtomicInteger nextId = new AtomicInteger();

    public DefaultObjectReferenceManager(Runtime runtime) {
        this.runtime = runtime;
    }

    public Pointer newReference(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("reference to null value not allowed");
        }

        while (true) {
            int id = nextId.addAndGet(7);
            Pointer ptr = runtime.getMemoryManager().newPointer(id);
            if (references.putIfAbsent(ptr, obj) == null) {
                return ptr;
            }
        }
    }

    public void freeReference(Pointer reference) {
        references.remove(reference);
    }

    public Object getObject(Pointer reference) {
        return references.get(reference);
    }
}
