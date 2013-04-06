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

package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallContext;
import com.kenai.jffi.Closure;
import com.kenai.jffi.ClosureMagazine;
import com.kenai.jffi.ClosureManager;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.provider.FromNativeType;
import jnr.ffi.provider.ToNativeType;
import jnr.ffi.util.ref.FinalizableWeakReference;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import static jnr.ffi.provider.jffi.ClosureUtil.getParameterType;
import static jnr.ffi.provider.jffi.ClosureUtil.getResultType;
import static jnr.ffi.provider.jffi.InvokerUtil.getCallContext;
import static jnr.ffi.provider.jffi.InvokerUtil.getNativeCallingConvention;

/**
 *
 */
public final class NativeClosureFactory<T> {
    private final jnr.ffi.Runtime runtime;
    private final ConcurrentMap<Integer, ClosureReference> closures = new ConcurrentHashMap<Integer, ClosureReference>();
    private final CallContext callContext;
    private final NativeClosureProxy.Factory closureProxyFactory;
    private final ConcurrentLinkedQueue<NativeClosurePointer> freeQueue = new ConcurrentLinkedQueue<NativeClosurePointer>();
    private ClosureMagazine currentMagazine;


    protected NativeClosureFactory(jnr.ffi.Runtime runtime, CallContext callContext,
                                   NativeClosureProxy.Factory closureProxyFactory) {
        this.runtime = runtime;
        this.closureProxyFactory = closureProxyFactory;
        this.callContext = callContext;
    }

    static <T> NativeClosureFactory newClosureFactory(jnr.ffi.Runtime runtime, Class<T> closureClass,
                                                      SignatureTypeMapper typeMapper, AsmClassLoader classLoader) {

        Method callMethod = null;
        for (Method m : closureClass.getMethods()) {
            if (m.isAnnotationPresent(Delegate.class) && Modifier.isPublic(m.getModifiers())
                    && !Modifier.isStatic(m.getModifiers())) {
                callMethod = m;
                break;
            }
        }
        if (callMethod == null) {
            throw new NoSuchMethodError("no public non-static delegate method defined in " + closureClass.getName());
        }

        Class[] parameterTypes = callMethod.getParameterTypes();
        FromNativeType[] parameterSigTypes = new FromNativeType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            parameterSigTypes[i] = getParameterType(runtime, callMethod, i, typeMapper);
        }
        ToNativeType resultType = getResultType(runtime, callMethod, typeMapper);

        return new NativeClosureFactory(runtime, getCallContext(resultType, parameterSigTypes, getNativeCallingConvention(callMethod), false),
                NativeClosureProxy.newProxyFactory(runtime, callMethod, resultType, parameterSigTypes, classLoader));
    }

    private void expunge(ClosureReference ref, Integer key) {
        // Fast case - no chained elements; can just remove from the hash map
        if (ref.next == null && closures.remove(key, ref)) {
            return;
        }

        // Remove from chained list
        synchronized (closures) {
            for (ClosureReference clref = closures.get(key), prev = clref; clref != null; prev = clref, clref = clref.next) {
                if (clref == ref) {
                    if (prev != clref) {
                        // if not first element in list, just remove this one
                        prev.next = clref.next;

                    } else {
                        // first element in list, replace with the next if non-null, else remove from map
                        if (clref.next != null) {
                            closures.replace(key, clref, clref.next);
                        } else {
                            closures.remove(key, clref);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void recycle(NativeClosurePointer ptr) {
        freeQueue.add(ptr);
    }

    final class ClosureReference extends FinalizableWeakReference<Object> {
        volatile ClosureReference next;
        private final NativeClosureFactory factory;
        private final NativeClosurePointer pointer;
        private final Integer key;


        private ClosureReference(Object referent, Integer key, NativeClosureFactory factory,
                                 NativeClosurePointer pointer) {
            super(referent, NativeFinalizer.getInstance().getFinalizerQueue());
            this.factory = factory;
            this.key = key;
            this.pointer = pointer;
        }

        public void finalizeReferent() {
            clear();
            factory.expunge(this, key);
            factory.recycle(pointer);
        }

        Object getCallable() {
            return get();
        }

        Pointer getPointer() {
            return pointer;
        }
    }

    NativeClosurePointer allocateClosurePointer() {
        NativeClosurePointer closurePointer = freeQueue.poll();
        if (closurePointer != null) {
            return closurePointer;
        }

        NativeClosureProxy proxy = closureProxyFactory.newClosureProxy();
        Closure.Handle closureHandle = null;

        synchronized (this) {
            do {
                if (currentMagazine == null || ((closureHandle = currentMagazine.allocate(proxy)) == null)) {
                    currentMagazine = ClosureManager.getInstance().newClosureMagazine(callContext,
                            closureProxyFactory.getInvokeMethod());
                }
            } while (closureHandle == null);
        }

        return new NativeClosurePointer(runtime, closureHandle, proxy);
    }

    NativeClosurePointer newClosure(Object callable, Integer key) {
        return newClosureReference(callable, key).pointer;
    }

    ClosureReference newClosureReference(Object callable, Integer key) {

        NativeClosurePointer ptr = allocateClosurePointer();
        ClosureReference ref = new ClosureReference(callable, key, this, ptr);
        ptr.proxy.closureReference = ref;
        if (closures.putIfAbsent(key, ref) == null) {
            return ref;
        }

        synchronized (closures) {
            do {
                // prepend and make new pointer the list head
                ref.next = closures.get(key);

                // If old value already removed (e.g. by expunge), just put the new value in
                if (ref.next == null && closures.putIfAbsent(key, ref) == null) {
                    break;
                }
            } while (!closures.replace(key, ref.next, ref));
        }

        return ref;
    }

    ClosureReference getClosureReference(Object callable) {
        Integer key = System.identityHashCode(callable);
        ClosureReference ref = closures.get(key);
        if (ref != null) {
            // Simple case - no identity hash code clash - just return the ptr
            if (ref.getCallable() == callable) {
                return ref;
            }

            // There has been a key clash, search the list
            synchronized (closures) {
                while ((ref = ref.next) != null) {
                    if (ref.getCallable() == callable) {
                        return ref;
                    }
                }
            }
        }

        return newClosureReference(callable, key);
    }
}
