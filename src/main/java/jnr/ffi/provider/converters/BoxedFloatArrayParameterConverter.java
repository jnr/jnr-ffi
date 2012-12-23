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
 * Converts a Float[] array to a float[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class BoxedFloatArrayParameterConverter implements ToNativeConverter<Float[], float[]> {
    private static final ToNativeConverter<Float[], float[]> IN = new BoxedFloatArrayParameterConverter(ParameterFlags.IN);
    private static final ToNativeConverter<Float[], float[]> OUT = new BoxedFloatArrayParameterConverter.Out(ParameterFlags.OUT);
    private static final ToNativeConverter<Float[], float[]> INOUT = new BoxedFloatArrayParameterConverter.Out(ParameterFlags.IN | ParameterFlags.OUT);

    private final int parameterFlags;

    public static ToNativeConverter<Float[], float[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? ParameterFlags.isIn(parameterFlags) ? INOUT : OUT : IN;
    }

    BoxedFloatArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public float[] toNative(Float[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        float[] primitive = new float[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = array[i] != null ? array[i] : 0;
            }
        }

        return primitive;
    }

    public static final class Out extends BoxedFloatArrayParameterConverter implements PostInvocation<Float[], float[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(Float[] array, float[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = primitive[i];
                }
            }
        }
    }

    @Override
    public Class<float[]> nativeType() {
        return float[].class;
    }
}
