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

/**
 *
 */
public final class ToNativeTypes {

    public static ToNativeType create(ToNativeConverter converter) {
        if (converter == null) {
            return null;
        }
        return converter.getClass().isAnnotationPresent(ToNativeConverter.Cacheable.class)
                ? new Cacheable(converter) : new UnCacheable(converter);
    }

    @ToNativeType.Cacheable
    static class Cacheable extends AbstractToNativeType {
        public Cacheable(ToNativeConverter converter) {
            super(converter);
        }
    }

    static class UnCacheable extends AbstractToNativeType {
        public UnCacheable(ToNativeConverter converter) {
            super(converter);
        }
    }
}
