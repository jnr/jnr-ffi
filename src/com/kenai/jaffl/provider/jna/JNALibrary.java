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
import com.kenai.jaffl.provider.Invoker;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 *
 */
final class JNALibrary implements com.kenai.jaffl.provider.Library {
    private static final String OPTION_INVOKING_METHOD = "invoking-method";
    private static final com.sun.jna.TypeMapper jnaTypeMapper = new JNATypeMapper();
    private final String libraryName;
    JNALibrary(String libraryName) {
        this.libraryName = libraryName;
    }
    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        return new DefaultInvoker(com.sun.jna.NativeLibrary.getInstance(libraryName), method);
    }

    public Object libraryLock() {
        return com.sun.jna.NativeLibrary.getInstance(libraryName);
    }
    private static class DefaultInvoker implements Invoker {
        private final com.sun.jna.Function function;
        private final Class returnType;
        private final Map<String, Object> functionOptions;
        DefaultInvoker(com.sun.jna.NativeLibrary library, Method method) {
            function = library.getFunction(method.getName());
            returnType = method.getReturnType();
            functionOptions = new IdentityHashMap<String, Object>(4);
            functionOptions.put(com.sun.jna.Library.OPTION_TYPE_MAPPER, jnaTypeMapper);
            functionOptions.put(OPTION_INVOKING_METHOD, method);
        }
        public Object invoke(Object[] parameters) {
            return function.invoke(returnType, parameters, functionOptions);
        }
    }
}
