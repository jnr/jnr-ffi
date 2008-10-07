/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package jaffl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author wayne
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
                throw new IllegalArgumentException("No known Enum mapping for " + enumClass.getName());
            }
        }
        return enumClass.cast(e);
    }
    private final Map<Class<? extends Enum>, Entry> enums = new ConcurrentHashMap<Class<? extends Enum>, Entry>();
}
