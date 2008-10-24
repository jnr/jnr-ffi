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
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.jna.invokers.MarshallingInvoker;
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
        boolean needMarshal = false;
        for (Class c : method.getParameterTypes()) {
            if (ByReference.class.isAssignableFrom(c)) {
                needMarshal = true;
            }
        }
        com.sun.jna.NativeLibrary lib = com.sun.jna.NativeLibrary.getInstance(libraryName);
        return needMarshal 
                ? new MarshallingInvoker(lib, method)
                : new SimpleInvoker(lib, method);
    }

    public Object libraryLock() {
        return com.sun.jna.NativeLibrary.getInstance(libraryName);
    }
}
