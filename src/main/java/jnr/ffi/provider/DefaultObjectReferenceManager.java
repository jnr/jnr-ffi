package jnr.ffi.provider;

import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public final class DefaultObjectReferenceManager extends ObjectReferenceManager {
    private final Runtime runtime;
    private final ConcurrentMap<Pointer, Object> references = new ConcurrentHashMap<Pointer, Object>();

    public DefaultObjectReferenceManager(Runtime runtime) {
        this.runtime = runtime;
    }

    public Pointer newReference(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("reference to null value not allowed");
        }

        int nextId = System.identityHashCode(obj);

        ObjectReference ptr;
        while (references.putIfAbsent(ptr = new ObjectReference(runtime, nextId), obj) != null) {
            // A collision on the identity hash is extremely rare, but possible, so probe for a vacant slot
            ++nextId;
        }

        return ptr;
    }

    public void freeReference(Pointer reference) {
        references.remove(reference);
    }

    public Object getObject(Pointer reference) {
        return references.get(reference);
    }

    private static final class ObjectReference extends InAccessibleMemoryIO {
        public ObjectReference(jnr.ffi.Runtime runtime, int address) {
            super(runtime, address & 0xffffffffL, true);
        }

        public long size() {
            return 0;
        }

        @Override
        public int hashCode() {
            return (int) address();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Pointer && ((Pointer) obj).address() == address();
        }
    }
}
