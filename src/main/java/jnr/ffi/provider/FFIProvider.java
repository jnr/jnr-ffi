/*
 * Copyright (C) 2008-2011 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider;

import jnr.ffi.*;

/**
 * This class defines the facilities a JNR FFI provider must provide.
 *
 * <strong>You most likely do NOT want to use this class directly</strong>
 */
public abstract class FFIProvider {
    /**
     * Gets an instance of <tt>FFIProvider</tt>
     *
     * @return an instance of <tt>FFIProvider</tt>
     */
    public static FFIProvider getSystemProvider() {
        return SystemProviderSingletonHolder.INSTANCE;
    }

    protected FFIProvider() {}

    /** 
     * Gets the default <tt>Runtime</tt> for this provider.
     *
     * @return the runtime.
     */
    public abstract jnr.ffi.Runtime getRuntime();

    /**
     *  Creates a new {@link LibraryLoader} instance.
     *
     *  @param <T> The library type.
     *  @param interfaceClass The library interface class.
     *  @return the {@code LibraryLoader} instance.
     */
    public abstract <T> LibraryLoader<T> createLibraryLoader(Class<T> interfaceClass);

    private static final class SystemProviderSingletonHolder {
        private static final FFIProvider INSTANCE = getInstance();

        static FFIProvider getInstance() {
            String providerName = System.getProperty("jnr.ffi.provider");
            if (providerName == null) {
                Package pkg = FFIProvider.class.getPackage();
                String pkgName = pkg != null && pkg.getName() != null ? pkg.getName() : "jnr.ffi.provider";
                providerName = pkgName + ".jffi.Provider";
            }

            try {
                return (FFIProvider) Class.forName(providerName).newInstance();

            } catch (Throwable ex) {
                return newInvalidProvider("could not load FFI provider " + providerName, ex);
            }
        }
    }

    private static FFIProvider newInvalidProvider(String message, Throwable cause) {
        return new InvalidProvider(message, cause);
    }
}
