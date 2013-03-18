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

import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.InAccessibleMemoryIO;
import jnr.ffi.provider.ParameterFlags;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a CharSequence[] array to a Pointer parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class CharSequenceArrayParameterConverter implements ToNativeConverter<CharSequence[], Pointer> {
    private final jnr.ffi.Runtime runtime;
    private final int parameterFlags;

    public static ToNativeConverter<CharSequence[], Pointer> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return !ParameterFlags.isOut(parameterFlags)
                ? new CharSequenceArrayParameterConverter(toNativeContext.getRuntime(), parameterFlags)
                : new CharSequenceArrayParameterConverter.Out(toNativeContext.getRuntime(), parameterFlags);
    }

    CharSequenceArrayParameterConverter(jnr.ffi.Runtime runtime, int parameterFlags) {
        this.runtime = runtime;
        this.parameterFlags = parameterFlags;
    }

    @Override
    public Pointer toNative(CharSequence[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }

        StringArray stringArray = StringArray.allocate(runtime, array.length + 1);
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                stringArray.put(i, array[i]);
            }
        }

        return stringArray;
    }

    public static final class Out extends CharSequenceArrayParameterConverter implements PostInvocation<CharSequence[], Pointer> {
        Out(jnr.ffi.Runtime runtime, int parameterFlags) {
            super(runtime, parameterFlags);
        }

        @Override
        public void postInvoke(CharSequence[] array, Pointer primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                StringArray stringArray = (StringArray) primitive;
                for (int i = 0; i < array.length; i++) {
                    array[i] = stringArray.get(i);
                }
            }
        }
    }

    @Override
    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    private final static class StringArray extends InAccessibleMemoryIO {
        private final Pointer memory;
        private List<Pointer> stringMemory;
        private final Charset charset = Charset.defaultCharset();

        private StringArray(Runtime runtime, Pointer memory, int capacity) {
            super(runtime, memory.address(), memory.isDirect());
            this.memory = memory;
            this.stringMemory = new ArrayList<Pointer>(capacity);
        }

        String get(int idx) {
            Pointer ptr = memory.getPointer(idx * getRuntime().addressSize());
            return ptr != null ? ptr.getString(0) : null;
        }

        void put(int idx, CharSequence str) {
            if (str == null) {
                memory.putAddress(idx * getRuntime().addressSize(), 0L);
            } else {
                ByteBuffer buf = charset.encode(CharBuffer.wrap(str));
                Pointer ptr = Memory.allocateDirect(getRuntime(), buf.remaining() + 4, true);
                ptr.put(0, buf.array(), 0, buf.remaining());
                stringMemory.add(idx, ptr);
                memory.putPointer(idx * getRuntime().addressSize(), ptr);
            }
        }

        @Override
        public long size() {
            return memory.size();
        }

        static StringArray allocate(Runtime runtime, int capacity) {
            Pointer memory = Memory.allocateDirect(runtime, capacity * runtime.addressSize());
            return new StringArray(runtime, memory, capacity);
        }

    }
}
