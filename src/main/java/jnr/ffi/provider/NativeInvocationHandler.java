/*
 * Copyright (C) 2008-2012 Wayne Meissner
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * InvocationHandler used to map invocations on a java interface to the correct native function.
 */
public class NativeInvocationHandler implements InvocationHandler {
    private volatile Map<Method, Invoker> fastLookupTable;
    private final Map<Method, Invoker> invokerMap;
    private final MethodHandles.Lookup lookup;

    /**
     * Creates a new InvocationHandler instance.
     * 
     * @param invokers A map of method invokers
     *
     */
    public NativeInvocationHandler(Map<Method, Invoker> invokers, MethodHandles.Lookup lookup) {
        this.invokerMap = invokers;
        this.fastLookupTable = Collections.emptyMap();
        this.lookup = lookup;
    }

    public Object invoke(Object self, Method method, Object[] argArray) throws Throwable {
        // skip default methods
        if (method.isDefault()) {
            if (lookup == null) {
                throw new AbstractMethodError("default methods not supported without Lookup or code generation");
            }

            MethodHandle handle = lookup.unreflectSpecial(method, method.getDeclaringClass());
            Object[] newArgs = new Object[argArray.length + 1];
            System.arraycopy(argArray, 0, newArgs, 1, argArray.length);
            newArgs[0] = self;
            return handle.invokeWithArguments(newArgs);
        }

        Invoker invoker = fastLookupTable.get(method);
        return invoker != null ? invoker.invoke(self, argArray) : lookupAndCacheInvoker(method).invoke(self, argArray);
    }

    private synchronized Invoker lookupAndCacheInvoker(Method method) {
        Invoker invoker;
        if ((invoker = fastLookupTable.get(method)) != null) {
            return invoker;
        }

        Map<Method, Invoker> map = new IdentityHashMap<Method, Invoker>(fastLookupTable);
        map.put(method, invoker = invokerMap.get(method));
        if (invoker == null) {
            throw new UnsatisfiedLinkError("no invoker for native method " + method.getName());
        }

        fastLookupTable = map;
        return invoker;
    }
}