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

import jnr.ffi.NativeLong;

/**
 * Parameter and return type support for the old NativeLong type
 */
import jnr.ffi.mapper.*;

@ToNativeConverter.NoContext
@FromNativeConverter.NoContext
@ToNativeConverter.Cacheable
@FromNativeConverter.Cacheable
public final class NativeLongConverter extends AbstractDataConverter<NativeLong, Long> {
    private static final DataConverter INSTANCE = new NativeLongConverter();

    public static DataConverter<NativeLong, Long> getInstance() {
        return INSTANCE;
    }

    public Class<Long> nativeType() {
        return Long.class;
    }

    public Long toNative(NativeLong value, ToNativeContext toNativeContext) {
        return value.longValue();
    }

    public NativeLong fromNative(Long value, FromNativeContext fromNativeContext) {
        return NativeLong.valueOf(value);
    }
}
