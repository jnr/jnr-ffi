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

import jnr.ffi.mapper.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Provides mapping from Enum values to native integers and vice-versa
 */
@ToNativeConverter.NoContext
@FromNativeConverter.NoContext
public final class EnumMapper {

    private static final class StaticDataHolder {
        private static volatile Map<Class<? extends Enum>, EnumMapper> MAPPERS = Collections.emptyMap();
    }
    
    private final Class<? extends Enum> enumClass;
    private final Integer[] intValues;
    private final Long[] longValues;
    private final Map<Number, Enum> reverseLookupMap = new HashMap<Number, Enum>();

    private EnumMapper(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;

        EnumSet<? extends Enum> enums = EnumSet.allOf(enumClass);

        this.intValues = new Integer[enums.size()];
        this.longValues = new Long[enums.size()];
        Method intValueMethod = getNumberValueMethod(enumClass, int.class);
        Method longValueMethod = getNumberValueMethod(enumClass, long.class);
        for (Enum e : enums) {
            Number value;
            if (longValueMethod != null) {
                value = reflectedNumberValue(e, longValueMethod);

            } else if (intValueMethod != null) {
                value = reflectedNumberValue(e, intValueMethod);

            } else {
                value = e.ordinal();
            }
            intValues[e.ordinal()] = value.intValue();
            longValues[e.ordinal()] = value.longValue();

            reverseLookupMap.put(value, e);
        }
        
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

    private static Method getNumberValueMethod(Class c, Class numberClass) {
        try {
            Method m = c.getDeclaredMethod(numberClass.getSimpleName() + "Value");
            return m != null && numberClass == m.getReturnType() ? m : null;

        } catch (Throwable t) {
            return null;
        }
    }

    private static Number reflectedNumberValue(Enum e, Method m) {
        try {
            return (Number) m.invoke(e);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public final Integer integerValue(Enum value) {
        if (value.getClass() != enumClass) {
            throw new IllegalArgumentException("enum class mismatch, " + value.getClass());
        }

        return intValues[value.ordinal()];
    }

    public final int intValue(Enum value) {
        return integerValue(value);
    }

    public final Long longValue(Enum value) {
        if (value.getClass() != enumClass) {
            throw new IllegalArgumentException("enum class mismatch, " + value.getClass());
        }

        return longValues[value.ordinal()];
    }

    public Enum valueOf(int value) {
        return reverseLookup(value);
    }

    public Enum valueOf(long value) {
        return reverseLookup(value);
    }

    public Enum valueOf(Number value) {
        return reverseLookup(value);
    }

    private Enum reverseLookup(Number value) {
        Enum e = reverseLookupMap.get(value);
        return e != null ? e : badValue(value);
    }

    private Enum badValue(Number value) {
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
