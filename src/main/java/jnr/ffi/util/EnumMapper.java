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

package jnr.ffi.util;

import jnr.ffi.mapper.AbstractDataConverter;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.ToNativeContext;

import java.util.*;

/**
 * Provides mapping from Enum values to native integers and vice-versa
 */
public final class EnumMapper extends AbstractDataConverter<Enum, Integer> {

    private static final class StaticDataHolder {
        private static volatile Map<Class<? extends Enum>, EnumMapper> MAPPERS = Collections.emptyMap();
    };
    
    private final Class<? extends Enum> enumClass;
    private final Integer[] values;
    private final Map<Integer, Enum> reverseLookupMap = new HashMap<Integer, Enum>();

    private EnumMapper(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;

        EnumSet<? extends Enum> enums = EnumSet.allOf(enumClass);

        this.values = new Integer[enums.size()];
        for (Enum e : enums) {
            Integer value = getIntegerValue(e);
            this.values[e.ordinal()] = value;
            reverseLookupMap.put(value, e);
        }
        
    }

    public Enum fromNative(Integer nativeValue, FromNativeContext context) {
        return valueOf(nativeValue);
    }

    public Class<Integer> nativeType() {
        return Integer.class;
    }

    public Integer toNative(Enum value, ToNativeContext context) {
        return intValue(enumClass.cast(value));
    }

    public static interface IntegerEnum {
        public int intValue();
    }

    public static EnumMapper getInstance(Class<? extends Enum> enumClass) {
        EnumMapper mapper = StaticDataHolder.MAPPERS.get(enumClass);
        if (mapper != null) {
            return mapper;
        }

        return addMapper(enumClass);
    }

    private static synchronized EnumMapper addMapper(Class<? extends Enum> enumClass) {
        EnumMapper mapper = new EnumMapper(enumClass);

        Map<Class<? extends Enum>, EnumMapper> tmp
                = new IdentityHashMap<Class<? extends Enum>, EnumMapper>(StaticDataHolder.MAPPERS);
        tmp.put(enumClass, mapper);

        StaticDataHolder.MAPPERS = tmp;

        return mapper;
    }

    private static final int getIntegerValue(Enum e) {
        if (e instanceof IntegerEnum) {
            return ((IntegerEnum) e).intValue();
        } else {
            return e.ordinal();
        }
    }

    public final int intValue(Enum value) {
        if (value.getClass() != enumClass) {
            throw new IllegalArgumentException("enum class mismatch, " + value.getClass());
        }

        return values[value.ordinal()];
    }

    public Enum valueOf(int value) {
        return valueOf(Integer.valueOf(value));
    }

    public Enum valueOf(Number value) {
        return valueOf(Integer.valueOf(value.intValue()));
    }

    public Enum valueOf(Integer value) {
        Enum e = reverseLookupMap.get(value);
        return e != null ? e : badValue(value);
    }

    private final Enum badValue(Integer value) {
        //
        // No value found - try to find the default value for unknown values.
        // This is useful for enums that aren't fixed in stone and/or where you
        // don't want to throw an Exception for an unknown value.
        //
        try {
            return Enum.valueOf(enumClass, "__UNKNOWN_NATIVE_VALUE");
        } catch (IllegalArgumentException ex) {
            //
            // No default, so just give up and throw an exception
            //
            throw new IllegalArgumentException("No known Enum mapping for value "
                    + value + " of type " + enumClass.getName());
        }
    }
}
