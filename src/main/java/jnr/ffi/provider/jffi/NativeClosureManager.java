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

import jnr.ffi.Callable;
import jnr.ffi.provider.ClosureManager;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
final class NativeClosureManager implements ClosureManager {
    private volatile Map<Class<? extends Callable>, NativeClosureFactory> factories = new HashMap<Class<? extends Callable>, NativeClosureFactory>();
    private final NativeRuntime runtime;

    NativeClosureManager(NativeRuntime runtime) {
        this.runtime = runtime;
    }

    public <T extends Callable> T newClosure(Class<? extends T> closureClass, T instance) {
        NativeClosureFactory<T> factory = factories.get(closureClass);
        if (factory != null) {
            //return factory.newClosure(instance);
        }
        return null;
    }

    synchronized <T extends Callable> NativeClosureFactory<T> getClosureFactory(Class<T> closureClass) {
        NativeClosureFactory<T> factory = factories.get(closureClass);
        if (factory != null) {
            return factory;
        }

        factory = NativeClosureFactory.newClosureFactory(runtime, closureClass);
        factories.put(closureClass, factory);

        return factory;
    }
}
