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
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public final class StructByReferenceToNativeConverter implements ToNativeConverter<Struct, Pointer> {
    private final int flags;

    public static ToNativeConverter<Struct, Pointer> getInstance(ToNativeContext toNativeContext) {
        return new StructByReferenceToNativeConverter(ParameterFlags.parse(toNativeContext.getAnnotations()));
    }

    StructByReferenceToNativeConverter(int flags) {
        this.flags = flags;
    }

    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public Pointer toNative(Struct value, ToNativeContext ctx) {
        return value != null ? Struct.getMemory(value, flags) : null;
    }
}
