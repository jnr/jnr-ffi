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
import jnr.ffi.annotations.LongLong;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.DelegatingMemoryIO;
import jnr.ffi.provider.ParameterFlags;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Converts a Pointer[] array to a long[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class StructArrayParameterConverter implements ToNativeConverter<Struct[], Pointer> {
    protected final jnr.ffi.Runtime runtime;
    protected final int parameterFlags;

    public static ToNativeConverter<Struct[], Pointer> getInstance(ToNativeContext toNativeContext, Class structClass) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return !ParameterFlags.isOut(parameterFlags)
                ? new StructArrayParameterConverter(toNativeContext.getRuntime(), parameterFlags)
                : new StructArrayParameterConverter.Out(toNativeContext.getRuntime(), structClass.asSubclass(Struct.class), parameterFlags);
    }

    StructArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    @Override
    public Pointer toNative(Struct[] structs, ToNativeContext context) {
        if (structs == null) {
            return null;
        }
        Pointer memory = Struct.getMemory(structs[0], parameterFlags);
        if (!(memory instanceof DelegatingMemoryIO)) {
            throw new RuntimeException("Struct array must be backed by contiguous array");
        }
        return ((DelegatingMemoryIO) memory).getDelegatedMemoryIO();
    }

    public static final class Out extends StructArrayParameterConverter implements PostInvocation<Struct[], Pointer> {
        private final Constructor<? extends Struct> constructor;
        Out(jnr.ffi.Runtime runtime, Class<? extends Struct> structClass, int parameterFlags) {
            super(runtime, parameterFlags);
            Constructor<? extends Struct> cons;
            try {
                cons = structClass.getConstructor(jnr.ffi.Runtime.class);
            } catch (NoSuchMethodException nsme) {
                throw new RuntimeException(structClass.getName() + " has no constructor that accepts jnr.ffi.Runtime");
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            this.constructor = cons;
        }

        @Override
        public void postInvoke(Struct[] structs, Pointer primitive, ToNativeContext context) {
            if (structs != null && primitive != null) {
                try {
                    int off = 0;
                    for (int i = 0; i < structs.length; i++) {
                        structs[i] = constructor.newInstance(runtime);
                        int structSize = align(Struct.size(structs[i]), Struct.alignment(structs[i]));
                        structs[i].useMemory(primitive.slice(off = align(off, Struct.alignment(structs[i])), structSize));
                        off += structSize;
                    }
                } catch (InstantiationException ie) {
                    throw new RuntimeException(ie);
                } catch (IllegalAccessException iae) {
                    throw new RuntimeException(iae);
                } catch (InvocationTargetException ite) {
                    throw new RuntimeException(ite);
                }
            }
        }
    }

    private static int align(int offset, int align) {
        return (offset + align - 1) & ~(align - 1);
    }

}
