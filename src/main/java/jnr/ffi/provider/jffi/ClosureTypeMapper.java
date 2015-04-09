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

package jnr.ffi.provider.jffi;

import jnr.ffi.Struct;
import jnr.ffi.mapper.*;
import jnr.ffi.mapper.FromNativeType;
import jnr.ffi.mapper.ToNativeType;
import jnr.ffi.provider.converters.EnumConverter;
import jnr.ffi.provider.ParameterFlags;
import jnr.ffi.provider.converters.StringResultConverter;
import jnr.ffi.provider.converters.StructByReferenceToNativeConverter;

final class ClosureTypeMapper implements SignatureTypeMapper {
    private FromNativeConverter getFromNativeConverter(SignatureType type, FromNativeContext context) {
        if (Enum.class.isAssignableFrom(type.getDeclaredType())) {
            return EnumConverter.getInstance(type.getDeclaredType().asSubclass(Enum.class));

        } else if (CharSequence.class.isAssignableFrom(type.getDeclaredType())) {
            return StringResultConverter.getInstance(context);

        } else {
            return null;
        }
    }

    private ToNativeConverter getToNativeConverter(SignatureType type, ToNativeContext context) {
        if (Enum.class.isAssignableFrom(type.getDeclaredType())) {
            return EnumConverter.getInstance(type.getDeclaredType().asSubclass(Enum.class));

        } else if (Struct.class.isAssignableFrom(type.getDeclaredType())) {
            return StructByReferenceToNativeConverter.getInstance(context);


        } else {
            return null;
        }
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        return FromNativeTypes.create(getFromNativeConverter(type, context));
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        return ToNativeTypes.create(getToNativeConverter(type, context));
    }
}
