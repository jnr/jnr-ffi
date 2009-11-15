
package com.kenai.jaffl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides mapping from Enum values to native integers and vice-versa
 */
public class EnumMapper {
    
    public static interface IntegerEnum {
        public int intValue();
    }
    private static final class Entry {
        Map<Integer, Enum> enumMap = new HashMap<Integer, Enum>();
        Map<Enum, Integer> valueMap = new HashMap<Enum, Integer>();
    }
    private static final EnumMapper mapper = new EnumMapper();
    public static EnumMapper getInstance() {
        return mapper;
    }
    private static final int getIntegerValue(Enum e) {
        if (e instanceof IntegerEnum) {
            return ((IntegerEnum) e).intValue();
        } else {
            return e.ordinal();
        }
    }
    private Entry createEntry(Class<? extends Enum> enumClass) {
        Entry entry = new Entry();
        for (Enum e : enumClass.getEnumConstants()) {
            int intValue = getIntegerValue(e);
            entry.enumMap.put(intValue, e);
            entry.valueMap.put(e, intValue);
        }
        return entry;
    }
    private Entry getEntry(Class<? extends Enum> enumClass) {
        Entry entry = enums.get(enumClass);
        if (entry == null) {
            // 
            // When building the entry, lock on the class so other lookups can proceed
            // without waiting for the build.
            //
            synchronized (enumClass) {
                //
                // Re-check in case two threads tried at the same time and fell through
                // to here.
                //
                if (!enums.containsKey(enumClass)) {
                    enums.put(enumClass, entry = createEntry(enumClass));
                } else {
                    entry = enums.get(enumClass);
                }
            }
        }
        return entry;
    }
    public int intValue(Enum value) {
        //return getEntry(value.getClass()).valueMap.get(value);
        return getIntegerValue(value);
    }
    
    public <E extends Enum<E>> E valueOf(int value, Class<E> enumClass) {
        Enum e = getEntry(enumClass).enumMap.get(value);
        if (e == null) {
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
                throw new IllegalArgumentException("No known Enum mapping for value " + value + " of type " + enumClass.getName());
            }
        }
        return enumClass.cast(e);
    }
    private final Map<Class<? extends Enum>, Entry> enums = new ConcurrentHashMap<Class<? extends Enum>, Entry>();
}
