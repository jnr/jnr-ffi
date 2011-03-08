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

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.util.EnumMapper;


public class EnumResultConverter implements FromNativeConverter {
    private final EnumMapper mapper;
    private final Class enumClass;

    EnumResultConverter(Class enumClass) {
        this.mapper = EnumMapper.getInstance(enumClass.asSubclass(Enum.class));
        this.enumClass = enumClass;
    }

    public Object fromNative(Object nativeValue, FromNativeContext context) {
        return mapper.valueOf((Integer) nativeValue);
    }

    public Class nativeType() {
        return Integer.class;
    }
}
