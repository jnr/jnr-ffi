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

import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;

/**
 * Proxy to hold information for parameter conversion to native types.
 *
 * <p>
 * This wraps a {@link ToNativeConverter}, but with a simplified interface to
 * ease code generation.
 */
public final class ParameterConverter implements ToNativeConverter{
    private final ToNativeConverter converter;
    private final ToNativeContext ctx;

    public ParameterConverter(ToNativeConverter converter, ToNativeContext ctx) {
        this.converter = converter;
        this.ctx = ctx;
    }

    public final Object toNative(Object value, ToNativeContext unused) {
        return converter.toNative(value, ctx);
    }

    public final Object toNative(Object value) {
        return converter.toNative(value, ctx);
    }

    public final Class nativeType() {
        return converter.nativeType();
    }
}
