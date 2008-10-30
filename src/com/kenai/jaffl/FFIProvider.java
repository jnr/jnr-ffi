
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
    public abstract MemoryIO wrap(Pointer address);

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
