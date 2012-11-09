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

import jnr.ffi.Pointer;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;
import jnr.ffi.provider.ClosureManager;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 *
 */
final class NativeClosureManager implements ClosureManager {
    private volatile Map<Class<? extends Object>, NativeClosureFactory> factories = new IdentityHashMap<Class<? extends Object>, NativeClosureFactory>();
    private final NativeRuntime runtime;
    private final TypeMapper typeMapper;

    NativeClosureManager(NativeRuntime runtime, TypeMapper typeMapper) {
        this.runtime = runtime;
        this.typeMapper = typeMapper;
    }

    <T extends Object> NativeClosureFactory<T> getClosureFactory(Class<T> closureClass) {
        NativeClosureFactory<T> factory = factories.get(closureClass);
        if (factory != null) {
            return factory;
        }

        return initClosureFactory(closureClass);
    }

    public <T extends Object> T newClosure(Class<? extends T> closureClass, T instance) {
        NativeClosureFactory<T> factory = factories.get(closureClass);
        if (factory != null) {
            //return factory.newClosure(instance);
        }
        return null;
    }

    public final <T extends Object> jnr.ffi.Pointer getClosurePointer(Class<? extends T> closureClass, T instance) {
        return getClosureFactory(closureClass).getClosureReference(instance).getPointer();
    }

    synchronized <T extends Object> NativeClosureFactory<T> initClosureFactory(Class<T> closureClass) {
        NativeClosureFactory<T> factory = factories.get(closureClass);
        if (factory != null) {
            return factory;
        }


        factory = NativeClosureFactory.newClosureFactory(runtime, closureClass, typeMapper);
        Map<Class<? extends Object>, NativeClosureFactory> factories = new IdentityHashMap<Class<? extends Object>, NativeClosureFactory>();
        factories.putAll(this.factories);
        factories.put(closureClass, factory);
        this.factories = factories;

        return factory;
    }

    <T extends Object> ToNativeConverter<T, Pointer> newClosureSite(Class<T> closureClass) {
        return new ClosureSite<T>(getClosureFactory(closureClass));
    }

    private static final class ClosureSite<T> implements ToNativeConverter<T, Pointer> {
        private final NativeClosureFactory<T> factory;
        private NativeClosureFactory.ClosureReference closureReference = null;

        private ClosureSite(NativeClosureFactory<T> factory) {
            this.factory = factory;
        }

        public Pointer toNative(T value, ToNativeContext context) {
            NativeClosureFactory.ClosureReference ref = closureReference;
            if (ref != null && ref.getCallable() == value) {
                return ref.getPointer();
            }

            ref = factory.getClosureReference(value);
            if (closureReference == null || closureReference.get() == null) {
                closureReference = ref;
            }

            return ref.getPointer();
        }

        public Class<Pointer> nativeType() {
            return Pointer.class;
        }
    }
}
