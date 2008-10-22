/*
 * Copyright (C) 2008 Wayne Meissner
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

package com.kenai.jaffl.provider.jna;


/**
 * Converter from jaffl types to JNA types
 */
public class JNATypeMapper extends com.sun.jna.DefaultTypeMapper {

    public JNATypeMapper() {
        addTypeConverter(com.kenai.jaffl.Pointer.class, new PointerConverter());
    }
    private static final class PointerConverter implements com.sun.jna.TypeConverter {

        public Object fromNative(Object nativeValue, com.sun.jna.FromNativeContext context) {
            return new JNAPointer((com.sun.jna.Pointer) nativeValue);
        }

        public Class nativeType() {
            return com.sun.jna.Pointer.class;
        }

        public Object toNative(Object value, com.sun.jna.ToNativeContext context) {
            return ((JNAPointer) value).getNativePointer();
        }
    }
}
