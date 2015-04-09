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

package jnr.ffi.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public final class DefaultTypeMapper implements TypeMapper {
    private final Map<Class, ToNativeConverter> toNativeConverters;
    private final Map<Class, FromNativeConverter> fromNativeConverters;

    public DefaultTypeMapper() {
        toNativeConverters = new LinkedHashMap<Class, ToNativeConverter>();
        fromNativeConverters = new LinkedHashMap<Class, FromNativeConverter>();
    }

    public final void put(Class javaClass, DataConverter converter) {
        toNativeConverters.put(javaClass, converter);
        fromNativeConverters.put(javaClass, converter);
    }

    public final void put(Class javaClass, ToNativeConverter converter) {
        toNativeConverters.put(javaClass, converter);
    }

    public final void put(Class javaClass, FromNativeConverter converter) {
        fromNativeConverters.put(javaClass, converter);
    }

    public FromNativeConverter getFromNativeConverter(Class type) {
        return fromNativeConverters.get(type);
    }

    public ToNativeConverter getToNativeConverter(Class type) {
        return toNativeConverters.get(type);
    }
}
