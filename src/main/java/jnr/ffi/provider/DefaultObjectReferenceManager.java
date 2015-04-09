/*
 * Copyright (C) 2011 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider;

import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public final class DefaultObjectReferenceManager extends ObjectReferenceManager {
    private final Runtime runtime;
    private final ConcurrentMap<Long, ObjectReference> references = new ConcurrentHashMap<Long, ObjectReference>();

    public DefaultObjectReferenceManager(Runtime runtime) {
        this.runtime = runtime;
    }

    public Pointer add(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("reference to null value not allowed");
        }

        long nextId = id(obj);

        ObjectReference ptr;
        while (references.putIfAbsent(nextId, ptr = new ObjectReference(runtime, nextId, obj)) != null) {
            // A collision on the identity hash is extremely rare, but possible, so probe for a vacant slot
            ++nextId;
        }

        return ptr;
    }

    public boolean remove(Pointer reference) {
        ObjectReference entry = references.remove(reference.address());
        return entry != null;
    }

    public Object get(Pointer reference) {
        ObjectReference ptr = references.get(reference.address());
        return ptr != null ? ptr.referent : null;
    }

    private long id(Object obj) {
        return ((0xcafebabeL << 32) | (System.identityHashCode(obj) & 0xffffffffL)) & runtime.addressMask();
    }

    private static final class ObjectReference extends InAccessibleMemoryIO {
        private final Object referent;

        public ObjectReference(jnr.ffi.Runtime runtime, long address, Object referent) {
            super(runtime, address, true);
            this.referent = referent;
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
