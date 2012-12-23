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

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a Short[] array to a primitive short[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class BoxedShortArrayParameterConverter implements ToNativeConverter<Short[], short[]> {
    private static final ToNativeConverter<Short[], short[]> IN = new BoxedShortArrayParameterConverter(ParameterFlags.IN);
    private static final ToNativeConverter<Short[], short[]> OUT = new BoxedShortArrayParameterConverter.Out(ParameterFlags.OUT);
    private static final ToNativeConverter<Short[], short[]> INOUT = new BoxedShortArrayParameterConverter.Out(ParameterFlags.IN | ParameterFlags.OUT);
    private final int parameterFlags;

    public static ToNativeConverter<Short[], short[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? ParameterFlags.isIn(parameterFlags) ? INOUT : OUT : IN;
    }

    public BoxedShortArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public short[] toNative(Short[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        short[] primitive = new short[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = array[i] != null ? array[i] : 0;
            }
        }

        return primitive;
    }

    public static final class Out extends BoxedShortArrayParameterConverter implements PostInvocation<Short[], short[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(Short[] array, short[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = primitive[i];
                }
            }
        }
    }

    @Override
    public Class<short[]> nativeType() {
        return short[].class;
    }
}
