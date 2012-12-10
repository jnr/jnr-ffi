package jnr.ffi;

import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.TypeMapper;

import java.io.File;
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
 * LibC libc = LibraryLoader.create()
 *     .library("c")
 *     .search("/usr/lib")
 *     .load(LibC.class);
 *
 * libc.puts("Hello, World");
 *
 * }
 * </pre></p>
 */
public abstract class LibraryLoader {
    private final List<String> searchPaths = new ArrayList<String>();
    private final List<String> libraryNames = new ArrayList<String>();
    private final Map<LibraryOption, Object> optionMap = new EnumMap<LibraryOption, Object>(LibraryOption.class);

    public static LibraryLoader create() {
        return FFIProvider.getSystemProvider().createLibraryLoader();
    }

    /**
     * Adds a library to be loaded.  Multiple libraries can be specified using multiple calls
     * to this method, and all libraries will be searched to resolve symbols (e.g. functions, variables).
     *
     * @param libraryName The name or path of library to load.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader library(String libraryName) {
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
    public LibraryLoader search(String path) {
        searchPaths.add(path);
        return this;
    }

    /**
     * Sets an option when loading libraries.
     *
     * @see LibraryOption
     *
     * @param option The option to set
     * @param value The value for the option.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader option(LibraryOption option, Object value) {
        optionMap.put(option, value);
        return this;
    }


    /**
     * Sets the type mapper to use when resolving method parameter and result types.
     *
     * @param typeMapper The type mapper to use.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader mapper(TypeMapper typeMapper) {
        optionMap.put(LibraryOption.TypeMapper, typeMapper);
        return this;
    }

    /**
     * Sets the type mapper to use when resolving method parameter and result types.
     *
     * @param typeMapper The type mapper to use.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader mapper(SignatureTypeMapper typeMapper) {
        optionMap.put(LibraryOption.TypeMapper, typeMapper);

        return this;
    }

    /**
     * Sets the function mapper to use when resolving symbols in this library.
     *
     * @param typeMapper The function mapper to use.
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader mapper(FunctionMapper typeMapper) {
        optionMap.put(LibraryOption.FunctionMapper, typeMapper);
        return this;
    }

    /**
     * Sets the native function calling convention.
     *
     * <p>This is only needed on windows platforms - unless explicitly specified, all platforms assume
     * {@link CallingConvention#DEFAULT} as the calling convention.
     *
     * @return The {@code LibraryLoader} instance.
     */
    public LibraryLoader convention(CallingConvention convention) {
        optionMap.put(LibraryOption.CallingConvention, convention);
        return this;
    }

    /**
     * Sets the calling convention of the library to the Windows stdcall calling convention
     *
     * @return This {@code LibraryLoader} instance.
     */
    public final LibraryLoader stdcall() {
        return convention(CallingConvention.STDCALL);
    }

    /**
     * Loads a native library and links the methods defined in {@code interfaceClass}
     * to native methods in the library.
     *
     * @param interfaceClass the interface that describes the native library interface
     * @return an instance of {@code interfaceclass} that will call the native methods.
     */
    public <T> T load(Class<T> interfaceClass) {
        return loadLibrary(interfaceClass, Collections.unmodifiableList(libraryNames), getSearchPaths(),
                Collections.unmodifiableMap(optionMap));
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
     * @param libraryNames A list of libraries to load & search for symbols
     * @param searchPaths The paths to search for libraries to be loaded
     * @param options The options to apply when loading the library
     * @param <T> The generic type of the interface
     * @return an instance of {@code interfaceClass} that will call the native methods.
     */
    protected abstract <T> T loadLibrary(Class<T> interfaceClass, Collection<String> libraryNames,
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
