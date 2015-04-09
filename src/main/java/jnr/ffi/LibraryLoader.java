/*
 * Copyright (C) 2012 Wayne Meissner
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

import jnr.ffi.mapper.*;
import jnr.ffi.provider.FFIProvider;
import jnr.ffi.provider.LoadedLibrary;
import jnr.ffi.provider.NativeInvocationHandler;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Loads a native library and maps it to a java interface.
 *
 * <p><strong>Example usage</strong></p>
 * <pre>
 * {@code
 *
 * public interface LibC {
 *    int puts(String str);
 * }
 *
 * LibC libc = LibraryLoader.create(LibC.class).load("c");
 *
 * libc.puts("Hello, World");
 *
 * }
 * </pre>
 */
public abstract class LibraryLoader<T> {
    private final List<String> searchPaths = new ArrayList<String>();
    private final List<String> libraryNames = new ArrayList<String>();
    private final List<SignatureTypeMapper> typeMappers = new ArrayList<SignatureTypeMapper>();
    private final List<FunctionMapper> functionMappers = new ArrayList<FunctionMapper>();
    private final Map<LibraryOption, Object> optionMap = new EnumMap<LibraryOption, Object>(LibraryOption.class);
    private final TypeMapper.Builder typeMapperBuilder = new TypeMapper.Builder();
    private final FunctionMapper.Builder functionMapperBuilder = new FunctionMapper.Builder();
    private final Class<T> interfaceClass;
    private boolean failImmediately = false;


    /**
     * Creates a new {@code LibraryLoader} instance.
     *
     * @param <T> The library type.
     * @param interfaceClass the interface that describes the native library functions
     * @return A {@code LibraryLoader} instance.
     */
    public static <T> LibraryLoader<T> create(Class<T> interfaceClass) {
        return FFIProvider.getSystemProvider().createLibraryLoader(interfaceClass);
    }

    protected LibraryLoader(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    /**
     * Adds a library to be loaded.  Multiple libraries can be specified using additional calls
     * to this method, and all libraries will be searched to resolve symbols (e.g. functions, variables).
     *
     * @param libraryName The name or path of library to load.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> library(String libraryName) {
        this.libraryNames.add(libraryName);
        return this;
    }

    /**
     * Adds a path to search for libraries.  Multiple paths can be specified using multiple calls
     * to this method, and all paths will be searched..
     *
     * @param path A directory to search.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> search(String path) {
        searchPaths.add(path);
        return this;
    }

    /**
     * Sets an option when loading libraries.
     *
     * @see LibraryOption
     *
     * @param option The option to set.
     * @param value The value for the option.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> option(LibraryOption option, Object value) {
        switch (option) {
            case TypeMapper:
                if (value instanceof SignatureTypeMapper) {
                    mapper((SignatureTypeMapper) value);

                } else if (value instanceof TypeMapper) {
                    mapper((TypeMapper) value);

                } else if (value != null) {
                    throw new IllegalArgumentException("invalid TypeMapper: " + value.getClass());
                }
                break;

            case FunctionMapper:
                mapper((FunctionMapper) value);
                break;

            default:
                optionMap.put(option, value);
        }

        return this;
    }


    /**
     * Adds a type mapper to use when resolving method parameter and result types.
     *
     * Multiple type mappers can be specified by additional calls to this method, and
     * each mapper will be tried in order until one is successful.
     *
     * @param typeMapper The type mapper to use.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> mapper(TypeMapper typeMapper) {
        typeMappers.add(new SignatureTypeMapperAdapter(typeMapper));
        return this;
    }

    /**
     * Adds a type mapper to use when resolving method parameter and result types.
     *
     * Multiple type mappers can be specified by additional calls to this method, and
     * each mapper will be tried in order until one is successful.
     *
     * @param typeMapper The type mapper to use.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> mapper(SignatureTypeMapper typeMapper) {
        typeMappers.add(typeMapper);
        return this;
    }

    /**
     * Adds a custom java type mapping.
     *
     * @param <J> The Java type.
     * @param javaType The java type to convert to a native type.
     * @param toNativeConverter A {@link jnr.ffi.mapper.ToNativeConverter} that will convert from the java type to a native type.
     * @return The {@code LibraryLoader} instance.
     */
    public <J> LibraryLoader<T> map(Class<? extends J> javaType, ToNativeConverter<? extends J, ?> toNativeConverter) {
        typeMapperBuilder.map(javaType, toNativeConverter);
        return this;
    }

    /**
     * Adds a custom java type mapping.
     *
     * @param <J> The Java type.
     * @param javaType The java type to convert to a native type.
     * @param fromNativeConverter A {@link jnr.ffi.mapper.ToNativeConverter} that will convert from the native type to a java type.
     * @return The {@code LibraryLoader} instance.
     */
    public <J> LibraryLoader<T> map(Class<? extends J> javaType, FromNativeConverter<? extends J, ?> fromNativeConverter) {
        typeMapperBuilder.map(javaType, fromNativeConverter);
        return this;
    }

    public <J> LibraryLoader<T> map(Class<? extends J> javaType, DataConverter<? extends J, ?> dataConverter) {
        typeMapperBuilder.map(javaType, dataConverter);
        return this;
    }

    /**
     * Adds a function mapper to use when resolving symbols in this library.
     *
     * Multiple function mappers can be specified by additional calls to this method, and
     * each mapper will be tried in order, until one is successful.
     *
     * @param functionMapper The function mapper to use.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> mapper(FunctionMapper functionMapper) {
        functionMappers.add(functionMapper);
        return this;
    }

    /**
     * Adds a function name mapping to use when resolving symbols in this library.
     *
     * @param javaName The java method name.
     * @param nativeFunction The native library symbol to map the java method name to.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> map(String javaName, String nativeFunction) {
        functionMapperBuilder.map(javaName, nativeFunction);
        return this;
    }

    /**
     * Sets the native function calling convention.
     *
     * <p>This is only needed on windows platforms - unless explicitly specified, all platforms assume
     * {@link CallingConvention#DEFAULT} as the calling convention.
     *
     * @param convention The calling convention.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader<T> convention(CallingConvention convention) {
        optionMap.put(LibraryOption.CallingConvention, convention);
        return this;
    }

    /**
     * Sets the calling convention of the library to the Windows stdcall calling convention
     *
     * @return This {@code LibraryLoader} instance.
     */
    public final LibraryLoader<T> stdcall() {
        return convention(CallingConvention.STDCALL);
    }

    /**
     * Turns off lazy propagation of load failures.  By default, {@link jnr.ffi.LibraryLoader#load()} will not fail 
     * immediately if any libraries cannot be loaded - instead, it will create an instance of the library interface 
     * that re-throws any load errors when invoked.
     * 
     * Calling this method will make {@link jnr.ffi.LibraryLoader#load()} throw errors immediately.
     *
     * @return This {@code LibraryLoader} instance.
     */
    public final LibraryLoader<T> failImmediately() {
        failImmediately = true;
        return this;
    }

    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @param libraryName The name or path of library to load.
     * @return an implementation of the interface provided to {@link #create(Class)} that will call the native methods.
     */
    public T load(String libraryName) {
        return library(libraryName).load();
    }

    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @return an implementation of the interface provided to {@link #create(Class)} that will call the native methods.
     */
    public T load() {
        if (libraryNames.isEmpty()) {
            throw new UnsatisfiedLinkError("no library names specified");
        }

        typeMappers.add(0, new SignatureTypeMapperAdapter(typeMapperBuilder.build()));
        optionMap.put(LibraryOption.TypeMapper, typeMappers.size() > 1 ? new CompositeTypeMapper(typeMappers) : typeMappers.get(0));

        functionMappers.add(0, functionMapperBuilder.build());
        optionMap.put(LibraryOption.FunctionMapper, functionMappers.size() > 1 ? new CompositeFunctionMapper(functionMappers) : functionMappers.get(0));

        try {
            return loadLibrary(interfaceClass, Collections.unmodifiableList(libraryNames), getSearchPaths(),
                Collections.unmodifiableMap(optionMap));
        
        } catch (LinkageError error) {
            if (failImmediately) throw error;
            return createErrorProxy(error);
        
        } catch (Exception ex) {
            RuntimeException re = ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
            if (failImmediately) throw re;
            
            return createErrorProxy(re);
        }
    }
    
    private T createErrorProxy(final Throwable ex) {
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
            new Class[] { interfaceClass, LoadedLibrary.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    throw ex;
                }
            })
        );
    }

    private Collection<String> getSearchPaths() {
        List<String> paths = new ArrayList<String>(searchPaths);
        paths.addAll(StaticDataHolder.USER_LIBRARY_PATH);

        return Collections.unmodifiableList(paths);
    }

    /**
     * Implemented by FFI providers to load the actual library.
     *
     * @param interfaceClass The java class that describes the functions to be mapped.
     * @param libraryNames A list of libraries to load and search for symbols.
     * @param searchPaths The paths to search for libraries to be loaded.
     * @param options The options to apply when loading the library.
     * @return an instance of {@code interfaceClass} that will call the native methods.
     */
    protected abstract T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames,
                                         Collection<String> searchPaths, Map<LibraryOption, Object> options);


    private static final class StaticDataHolder {
        private static final List<String> USER_LIBRARY_PATH;
        static {
            List<String> paths = new ArrayList<String>();
            try {
                paths.addAll(getPropertyPaths("jnr.ffi.library.path"));
                paths.addAll(getPropertyPaths("jaffl.library.path"));
                // Add JNA paths for compatibility
                paths.addAll(getPropertyPaths("jna.library.path"));
                paths.addAll(getPropertyPaths("java.library.path"));
            } catch (Exception ignored) {}
            USER_LIBRARY_PATH = Collections.unmodifiableList(new ArrayList<String>(paths));
        }
    }


    private static List<String> getPropertyPaths(String propName) {
        String value = System.getProperty(propName);
        if (value != null) {
            String[] paths = value.split(File.pathSeparator);
            return new ArrayList<String>(Arrays.asList(paths));
        }
        return Collections.emptyList();
    }
}
