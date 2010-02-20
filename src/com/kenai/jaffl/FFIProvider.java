
package com.kenai.jaffl;

import com.kenai.jaffl.provider.MemoryManager;
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
    public static final FFIProvider getProvider() {
        return SingletonHolder.INSTANCE;
    }
    /**
     * Gets the native memory manager for this provider.
     *
     * @return a {@link MemoryManager}
     */
    public abstract MemoryManager getMemoryManager();
    
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


    public abstract Type getType(NativeType type);
    
    private static final class SingletonHolder {
        private static final FFIProvider INSTANCE = getInstance();

        private static final FFIProvider getInstance() {
            String providerName = System.getProperty("jaffl.provider");
            if (providerName == null) {
                providerName = FFIProvider.class.getPackage().getName() + ".provider.jffi.Provider";
            }
            
            try {
                return (FFIProvider) Class.forName(providerName).newInstance();
            } catch (Throwable ex) {
                throw new RuntimeException("Could not load FFI provider " + providerName, ex);
            }
        }
    }
    protected FFIProvider() {}
}
