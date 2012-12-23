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
 * Converts a long[] array to a primitive int[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class Long32ArrayParameterConverter implements ToNativeConverter<long[], int[]> {
    private static final Long32ArrayParameterConverter IN = new Long32ArrayParameterConverter(ParameterFlags.IN);
    private static final Long32ArrayParameterConverter OUT = new Long32ArrayParameterConverter.Out(ParameterFlags.OUT);
    private static final Long32ArrayParameterConverter INOUT = new Long32ArrayParameterConverter.Out(ParameterFlags.IN | ParameterFlags.OUT);

    private final int parameterFlags;

    public static ToNativeConverter<long[], int[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? ParameterFlags.isIn(parameterFlags) ? INOUT : OUT : IN;
    }

    private Long32ArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public int[] toNative(long[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }

        int[] primitive = new int[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = (int) array[i];
            }
        }

        return primitive;
    }

    public static final class Out extends Long32ArrayParameterConverter implements PostInvocation<long[], int[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(long[] array, int[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = primitive[i];
                }
            }
        }
    }

    @Override
    public Class<int[]> nativeType() {
        return int[].class;
    }
}
