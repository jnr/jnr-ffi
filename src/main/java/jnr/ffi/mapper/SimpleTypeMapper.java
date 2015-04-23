/*
 * Copyright (C) 2013 Wayne Meissner
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

package jnr.ffi.mapper;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

final class SimpleTypeMapper implements TypeMapper {
    private final Map<Class, ToNativeConverter<?, ?>> toNativeConverters;
    private final Map<Class, FromNativeConverter<?, ?>> fromNativeConverters;

    public SimpleTypeMapper(Map<Class, ToNativeConverter<?, ?>> toNativeConverters, Map<Class, FromNativeConverter<?, ?>> fromNativeConverters) {
        this.toNativeConverters = Collections.unmodifiableMap(new IdentityHashMap<Class, ToNativeConverter<?, ?>>(toNativeConverters));
        this.fromNativeConverters = Collections.unmodifiableMap(new IdentityHashMap<Class, FromNativeConverter<?, ?>>(fromNativeConverters));
    }

    @Override
    public FromNativeConverter getFromNativeConverter(Class type) {
        return fromNativeConverters.get(type);
    }

    @Override
    public ToNativeConverter getToNativeConverter(Class type) {
        return toNativeConverters.get(type);
    }
}
