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

import jnr.ffi.LibraryOption;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapperAdapter;
import jnr.ffi.mapper.TypeMapper;
import jnr.ffi.provider.NativeFunction;
import jnr.ffi.provider.NullTypeMapper;

import java.util.Map;

public abstract class LibraryLoader {

    static SignatureTypeMapper getSignatureTypeMapper(Map<LibraryOption, ?> libraryOptions) {
        SignatureTypeMapper typeMapper;
        if (libraryOptions.containsKey(LibraryOption.TypeMapper)) {
            Object tm = libraryOptions.get(LibraryOption.TypeMapper);
            if (tm instanceof SignatureTypeMapper) {
                typeMapper = (SignatureTypeMapper) tm;
            } else if (tm instanceof TypeMapper) {
                typeMapper = new SignatureTypeMapperAdapter((TypeMapper) tm);
            } else {
                throw new IllegalArgumentException("TypeMapper option is not a valid TypeMapper instance");
            }
        } else {
            typeMapper = new NullTypeMapper();
        }
        return typeMapper;
    }

    abstract <T> T loadLibrary(NativeLibrary library, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, boolean failImmediately);
}
