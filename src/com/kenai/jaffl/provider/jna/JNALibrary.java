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

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.jna.invokers.DefaultMarshallingInvoker;
import com.kenai.jaffl.provider.jna.invokers.FromNativeMarshallingInvoker;
import com.kenai.jaffl.provider.jna.invokers.SimpleInvoker;
import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 */
final class JNALibrary implements com.kenai.jaffl.provider.Library {
    private final String libraryName;
    JNALibrary(String libraryName) {
        this.libraryName = libraryName;
    }
    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        TypeMapper typeMapper = (TypeMapper) options.get(LibraryOption.TypeMapper);
        if (typeMapper == null) {
            typeMapper = emptyTypeMapper;
        }
        boolean needParameterConversion = false;
        boolean needResultConversion = false;
        for (Class c : method.getParameterTypes()) {
            if (typeMapper.getToNativeConverter(c) != null) {
                needParameterConversion = true;
            } else if (ByReference.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            } else if (StringBuffer.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            } else if (StringBuilder.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            } else if (CharSequence.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            }
        }
        if (typeMapper.getFromNativeConverter(method.getReturnType()) != null) {
            needResultConversion = true;
        }
        com.sun.jna.NativeLibrary lib = com.sun.jna.NativeLibrary.getInstance(libraryName);
        if (!needParameterConversion && !needResultConversion) {
            return new SimpleInvoker(lib, method);
        } else if (needParameterConversion && !needResultConversion) {
            return new DefaultMarshallingInvoker(lib, method, typeMapper);
        } else {
            return new FromNativeMarshallingInvoker(lib, method, typeMapper);
        }
    }

    public Object libraryLock() {
        return com.sun.jna.NativeLibrary.getInstance(libraryName);
    }
    private static final TypeMapper emptyTypeMapper = new TypeMapper() {

        public FromNativeConverter getFromNativeConverter(Class type) {
            return null;
        }

        public ToNativeConverter getToNativeConverter(Class type) {
            return null;
        }
    };
}
