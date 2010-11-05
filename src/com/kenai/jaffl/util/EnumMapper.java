
package com.kenai.jaffl.util;

import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides mapping from Enum values to native integers and vice-versa
 */
public final class EnumMapper implements FromNativeConverter, ToNativeConverter {

    private static final class StaticDataHolder {
        private static final ConcurrentMap<Class<? extends Enum>, EnumMapper> MAPPERS
                = new ConcurrentHashMap<Class<? extends Enum>, EnumMapper>();

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

    public Object fromNative(Object nativeValue, FromNativeContext context) {
        return valueOf((Number) nativeValue);
    }

    public Class nativeType() {
        return Integer.class;
    }

    public Object toNative(Object value, ToNativeContext context) {
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

    private static EnumMapper addMapper(Class<? extends Enum> enumClass) {
        EnumMapper mapper = new EnumMapper(enumClass);
        StaticDataHolder.MAPPERS.put(enumClass, mapper);
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
        Collections.emptyList();
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
