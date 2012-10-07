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

package jnr.ffi;

import java.util.Map;

/**
 * This class defines the facilities a jaffl FFI provider must provide.
 */
public abstract class FFIProvider {
    /**
     * Gets an instance of <tt>FFIProvider</tt>
     *
     * @return an instance of <tt>FFIProvider</tt>
     */
    static final FFIProvider getSystemProvider() {
        return SystemProviderSingletonHolder.INSTANCE;
    }

    protected FFIProvider() {}

    /** Gets the default <tt>Runtime</tt> for this provider */
    public abstract Runtime getRuntime();
    
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
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @param interfaceClass the interface that describes the native library interface
     * @param libraryOptions options
     * @param libraryNames the list of libraries to load
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public abstract <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions,
            String... libraryNames);
    
    private static final class SystemProviderSingletonHolder {
        private static final FFIProvider INSTANCE = getInstance();

        static final FFIProvider getInstance() {
            String providerName = System.getProperty("jnr.ffi.provider");
            if (providerName == null) {
                Package pkg = FFIProvider.class.getPackage();
                String pkgName = pkg != null && pkg.getName() != null ? pkg.getName() : "jnr.ffi";
                providerName = pkgName + ".provider.jffi.Provider";
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
