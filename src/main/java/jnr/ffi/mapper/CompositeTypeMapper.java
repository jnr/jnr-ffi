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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class CompositeTypeMapper implements SignatureTypeMapper {
    private final Collection<SignatureTypeMapper> signatureTypeMappers;

    public CompositeTypeMapper(SignatureTypeMapper... signatureTypeMappers) {
        this.signatureTypeMappers = Collections.unmodifiableList(Arrays.asList(signatureTypeMappers.clone()));
    }

    public CompositeTypeMapper(Collection<SignatureTypeMapper> signatureTypeMappers) {
        this.signatureTypeMappers = Collections.unmodifiableList(new ArrayList<SignatureTypeMapper>(signatureTypeMappers));
    }

    @Override
    public FromNativeType getFromNativeType(SignatureType type, FromNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            FromNativeType fromNativeType = m.getFromNativeType(type, context);
            if (fromNativeType != null) {
                return fromNativeType;
            }
        }

        return null;
    }

    @Override
    public ToNativeType getToNativeType(SignatureType type, ToNativeContext context) {
        for (SignatureTypeMapper m : signatureTypeMappers) {
            ToNativeType toNativeType = m.getToNativeType(type, context);
            if (toNativeType != null) {
                return toNativeType;
            }
        }

        return null;
    }
}
