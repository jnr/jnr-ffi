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

import com.kenai.jaffl.struct.Struct;
import com.kenai.jaffl.struct.StructUtil;


/**
 * Converter from jaffl types to JNA types
 */
public class JNATypeMapper extends com.sun.jna.DefaultTypeMapper {

    public JNATypeMapper() {
        addTypeConverter(com.kenai.jaffl.Pointer.class, new PointerConverter());
        addTypeConverter(com.kenai.jaffl.struct.Struct.class, new StructConverter());
    }
    private static final class PointerConverter implements com.sun.jna.TypeConverter {

        public Object fromNative(Object nativeValue, com.sun.jna.FromNativeContext context) {
            return nativeValue != null
                    ? new JNAPointer((com.sun.jna.Pointer) nativeValue)
                    : null;
        }

        public Class nativeType() {
            return com.sun.jna.Pointer.class;
        }

        public Object toNative(Object value, com.sun.jna.ToNativeContext context) {
            return ((JNAPointer) value).getNativePointer();
        }
    }
    private static final class StructConverter implements com.sun.jna.TypeConverter {

        public Object fromNative(Object nativeValue, com.sun.jna.FromNativeContext context) {
            if (nativeValue == null) {
                return null;
            }
            Struct s;
            try {
                s = (Struct) context.getTargetType().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            s.useMemory(new PointerMemoryIO((com.sun.jna.Pointer) nativeValue));
            return s;
        }

        public Class nativeType() {
            return com.sun.jna.Pointer.class;
        }

        public Object toNative(Object value, com.sun.jna.ToNativeContext context) {
            return value != null ?
                ((JNAMemoryIO) StructUtil.getMemoryIO((Struct) value)).getMemory()
                : null;
        }
    }
}
