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

package jnr.ffi.mapper;

import java.util.HashMap;
import java.util.Map;

public interface TypeMapper {
    public FromNativeConverter getFromNativeConverter(Class type);
    public ToNativeConverter getToNativeConverter(Class type);

    public static final class Builder {
        private final Map<Class, ToNativeConverter<?,?>> toNativeConverterMap = new HashMap<Class, ToNativeConverter<?, ?>>();
        private final Map<Class, FromNativeConverter<?,?>> fromNativeConverterMap = new HashMap<Class, FromNativeConverter<?,?>>();

        public <T> Builder map(Class<? extends T> javaType, ToNativeConverter<? extends T, ?> toNativeConverter) {
            toNativeConverterMap.put(javaType, toNativeConverter);
            return this;
        }

        public <T> Builder map(Class<? extends T> javaType, FromNativeConverter<? extends T, ?> fromNativeConverter) {
            fromNativeConverterMap.put(javaType, fromNativeConverter);
            return this;
        }

        public <T> Builder map(Class<? extends T> javaType, DataConverter<? extends T, ?> dataConverter) {
            toNativeConverterMap.put(javaType, dataConverter);
            fromNativeConverterMap.put(javaType, dataConverter);
            return this;
        }

        public TypeMapper build() {
            return new SimpleTypeMapper(toNativeConverterMap, fromNativeConverterMap);
        }
    }
}
