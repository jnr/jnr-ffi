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
import jnr.ffi.annotations.LongLong;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.MemoryManager;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Pointer[] array to a long[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class Pointer64ArrayParameterConverter implements ToNativeConverter<Pointer[], long[]> {
    protected final jnr.ffi.Runtime runtime;
    protected final int parameterFlags;

    public static ToNativeConverter<Pointer[], long[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return !ParameterFlags.isOut(parameterFlags)
                ? new Pointer64ArrayParameterConverter(toNativeContext.getRuntime(), parameterFlags)
                : new Pointer64ArrayParameterConverter.Out(toNativeContext.getRuntime(), parameterFlags);
    }

    Pointer64ArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    @LongLong
    public Class<long[]> nativeType() {
        return long[].class;
    }

    @Override
    public long[] toNative(Pointer[] pointers, ToNativeContext context) {
        if (pointers == null) {
            return null;
        }
        long[] primitive = new long[pointers.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < pointers.length; i++) {
                if (pointers[i] != null && !pointers[i].isDirect()) {
                    throw new IllegalArgumentException("invalid pointer in array at index " + i);
                }
                primitive[i] = pointers[i] != null ? pointers[i].address() : 0L;
            }
        }

        return primitive;
    }

    public static final class Out extends Pointer64ArrayParameterConverter implements ToNativeConverter.PostInvocation<Pointer[], long[]> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(Pointer[] pointers, long[] primitive, ToNativeContext context) {
            if (pointers != null && primitive != null) {
                MemoryManager mm = runtime.getMemoryManager();
                for (int i = 0; i < pointers.length; i++) {
                    pointers[i] = mm.newPointer(primitive[i]);
                }
            }
        }
    }

}
