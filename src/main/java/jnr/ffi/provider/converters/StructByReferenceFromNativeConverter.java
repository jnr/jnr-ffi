/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi.provider.converters;

import jnr.ffi.Pointer;
import jnr.ffi.Struct;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * Converts a native pointer result into a {@link jnr.ffi.Struct}
 */
public class StructByReferenceFromNativeConverter implements FromNativeConverter<Struct, Pointer> {
    private final Constructor<? extends Struct> constructor;

    public static FromNativeConverter<Struct, Pointer> getInstance(Class structClass, FromNativeContext toNativeContext) {
        try {
            return new StructByReferenceFromNativeConverter(structClass.getConstructor(jnr.ffi.Runtime.class));

        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(structClass.getName() + " has no constructor that accepts jnr.ffi.Runtime");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    StructByReferenceFromNativeConverter(Constructor<? extends Struct> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Struct fromNative(Pointer nativeValue, FromNativeContext context) {
        try {
            Struct s = constructor.newInstance(context.getRuntime());
            s.useMemory(nativeValue);
            return s;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }
}
