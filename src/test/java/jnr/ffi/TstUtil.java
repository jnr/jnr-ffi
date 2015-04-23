/* 
 * Copyright (C) 2011 Wayne Meissner
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

package jnr.ffi;

import jnr.ffi.provider.FFIProvider;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

public final class TstUtil {
    private TstUtil() {}
    private static FFIProvider provider;
    private static String libname = "test";

    public static void setProvider(FFIProvider provider) {
        TstUtil.provider = provider;
    }

    public static void setPath(String path) {
        TstUtil.libname = path;
    }
    
    public static interface HelperLib {
        Pointer ptr_from_buffer(ByteBuffer buf);
    }

    public static <T> T loadTestLib(Class<T> interfaceClass) {
        final Map<LibraryOption, ?> options = Collections.emptyMap();
        return loadTestLib(interfaceClass, options);
    }
    public static <T> T loadTestLib(Class<T> interfaceClass, Map<LibraryOption, ?> options) {
        LibraryLoader<T> loader = (provider != null ? provider : FFIProvider.getSystemProvider()).createLibraryLoader(interfaceClass);

        loader.library(libname);
        for (Map.Entry<LibraryOption, ?> option : options.entrySet()) {
            loader.option(option.getKey(), option.getValue());
        }

        return loader.load();
    }

    public static Pointer getDirectBufferPointer(ByteBuffer buf) {
        return TstUtil.loadTestLib(HelperLib.class).ptr_from_buffer(buf);
    }
}
