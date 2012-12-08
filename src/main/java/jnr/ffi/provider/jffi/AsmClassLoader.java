/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static jnr.ffi.provider.jffi.CodegenUtils.ci;
import static jnr.ffi.provider.jffi.CodegenUtils.p;

final class AsmClassLoader extends ClassLoader {
    private final ConcurrentMap<String, Class> definedClasses = new ConcurrentHashMap<String, Class>();

    public AsmClassLoader() {
    }

    public AsmClassLoader(ClassLoader parent) {
        super(parent);
    }


    public Class defineClass(String name, byte[] b) {
        Class klass = defineClass(name, b, 0, b.length);
        definedClasses.putIfAbsent(name, klass);
        resolveClass(klass);
        return klass;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class klass = definedClasses.get(name);
        if (klass != null) {
            return klass;
        }
        return super.findClass(name);
    }
}
