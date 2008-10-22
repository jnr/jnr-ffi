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

package com.kenai.jaffl;

import java.util.Map;

/**
 *
 * @author wayne
 */
public abstract class FFIProvider {
    /**
     * Gets an instance of <tt>FFIProvider</tt>
     *
     * @return an instance of <tt>FFIProvider</tt>
     */
    public static final FFIProvider getProvider() {
        return SingletonHolder.INSTANCE;
    }
    public abstract MemoryIO allocateMemory(int size);
    public abstract MemoryIO allocateMemoryDirect(int size);
    public abstract MemoryIO allocateMemoryDirect(int size, boolean clear);
    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @param libraryName the name of the library to load
     * @param interfaceClass the interface that describes the native library interface
     * @param libraryOptions options
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public abstract <T> T loadLibrary(String libraryName, Class<T> interfaceClass,
            Map<LibraryOption, ?> libraryOptions);

    /**
     * Gets the last native error code.
     * <p>
     * This returns the errno value that was set at the time of the last native
     * function call.
     *
     * @return The errno value.
     */
    public abstract int getLastError();

    /**
     * Sets the native error code.
     *
     * @param error The value to set errno to.
     */
    public abstract void setLastError(int error);

    private static final class SingletonHolder {
        private static final FFIProvider INSTANCE = getInstance();
        private static final FFIProvider getInstance() {
            final boolean useJNA = Boolean.getBoolean("jaffl.usejna");
            FFIProvider provider = null;
            String prefix = FFIProvider.class.getPackage().getName() + ".provider";
            if (false && !useJNA) {
                try {
                    provider = (FFIProvider) Class.forName(prefix + ".jffi.JFFIProvider").newInstance();
                } catch (Throwable ex) {
                }
            }
            if (provider == null) {
                try {
                    provider = (FFIProvider) Class.forName(prefix + ".jna.JNAProvider").newInstance();
                } catch (Throwable ex) {
                    throw new RuntimeException("Could not load FFI provider", ex);
                }
            }
            return provider;
        }
    }
    protected FFIProvider() {}
}
