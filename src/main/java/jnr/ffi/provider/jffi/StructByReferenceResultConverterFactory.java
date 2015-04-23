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

package jnr.ffi.provider.jffi;

import jnr.ffi.*;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.provider.converters.StructByReferenceFromNativeConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class StructByReferenceResultConverterFactory {
    private final Map<Class<? extends Struct>, FromNativeConverter<? extends Struct, Pointer>> converters
            = new ConcurrentHashMap<Class<? extends Struct>, FromNativeConverter<? extends Struct, Pointer>>();
    
    private final AsmClassLoader classLoader;
    private final boolean asmEnabled;

    public StructByReferenceResultConverterFactory(AsmClassLoader classLoader, boolean asmEnabled) {
        this.classLoader = classLoader;
        this.asmEnabled = asmEnabled;
    }

    public final FromNativeConverter<? extends Struct, Pointer> get(Class<? extends Struct> structClass,
                                                                    FromNativeContext fromNativeContext) {
        FromNativeConverter<? extends Struct, Pointer> converter = converters.get(structClass);
        if (converter == null) {
            synchronized (converters) {
                if ((converter = converters.get(structClass)) == null) {
                    converters.put(structClass, converter = createConverter(fromNativeContext.getRuntime(), structClass, fromNativeContext));
                }
            }
        }

        return converter;
    }
    
    private FromNativeConverter<? extends Struct, Pointer> createConverter(jnr.ffi.Runtime runtime,
                                                                           Class<? extends Struct> structClass,
                                                                           FromNativeContext fromNativeContext) {
        return asmEnabled
            ? AsmStructByReferenceFromNativeConverter.newStructByReferenceConverter(runtime, structClass, 0, classLoader)
            : StructByReferenceFromNativeConverter.getInstance(structClass, fromNativeContext);
    }
}
