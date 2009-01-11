
package com.kenai.jaffl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class Platform {
    private final OS os;
    private final CPU cpu;
    private final int addressSize;
    private final long addressMask;
    private final int javaVersionMajor;

    public enum OS {
        DARWIN,
        FREEBSD,
        NETBSD,
        OPENBSD,
        LINUX,
        SOLARIS,
        WINDOWS,

        UNKNOWN;
    }
    public enum CPU {
        I386,
        X86_64,
        POWERPC,
        POWERPC64,
        SPARC,
        SPARCV9,
        UNKNOWN;
    }
    private static final class SingletonHolder {
        static final Platform PLATFORM = determinePlatform(determineOS());
        }
    private static final OS determineOS() {
        String osName = System.getProperty("os.name").split(" ")[0].toLowerCase();
        if (osName.startsWith("mac") || osName.startsWith("darwin")) {
            return OS.DARWIN;
        } else if (osName.startsWith("linux")) {
            return OS.LINUX;
        } else if (osName.startsWith("sunos") || osName.startsWith("solaris")) {
            return OS.SOLARIS;
        } else if (osName.startsWith("openbsd")) {
            return OS.OPENBSD;
        } else if (osName.startsWith("freebsd")) {
            return OS.FREEBSD;
        } else {
            throw new ExceptionInInitializerError("Unsupported operating system");
        }
    }
    private static final Platform determinePlatform(OS os) {
        switch (os) {
            case DARWIN:
                return new Darwin();
            case LINUX:
                return new Linux();
            case WINDOWS:
                return new Windows();
            case UNKNOWN:
                throw new ExceptionInInitializerError("Unsupported operating system");
            default:
                return new Default(os);
        }
    }
    private static final CPU determineCPU() {
        String archString = System.getProperty("os.arch").toLowerCase();
        if ("x86".equals(archString) || "i386".equals(archString) || "i86pc".equals(archString)) {
            return CPU.I386;
        } else if ("x86_64".equals(archString) || "amd64".equals(archString)) {
            return CPU.X86_64;
        } else if ("ppc".equals(archString) || "powerpc".equals(archString)) {
            return CPU.POWERPC;
        } else if ("ppc64".equals(archString)) {
            return CPU.POWERPC64;
        } else {
            throw new ExceptionInInitializerError("Unsupported CPU architecture: " + archString);
        }
    }

    private Platform(OS os) {
        this.os = os;
        this.cpu = determineCPU();
        int dataModel = Integer.getInteger("sun.arch.data.model");
        if (dataModel != 32 && dataModel != 64) {
            switch (cpu) {
                case I386:
                case POWERPC:
                case SPARC:
                    dataModel = 32;
                    break;
                case X86_64:
                case POWERPC64:
                case SPARCV9:
                    dataModel = 64;
                    break;
                default:
                    throw new ExceptionInInitializerError("Cannot determine cpu address size");
            }
        }
        addressSize = dataModel;
        addressMask = addressSize == 32 ? 0xffffffffL : 0xffffffffffffffffL;
        int version = 5;
        try {
            String versionString = System.getProperty("java.version");
            if (versionString != null) {
                String[] v = versionString.split("\\.");
                version = Integer.valueOf(v[1]);
            }
        } catch (Exception ex) {
            throw new ExceptionInInitializerError("Could not determine java version");
        }
        javaVersionMajor = version;
    }
    /**
     * Gets the current <tt>Platform</tt>
     *
     * @return The current platform.
     */
    public static final Platform getPlatform() {
        return SingletonHolder.PLATFORM;
    }

    /**
     * Gets the current Operating System.
     *
     * @return A <tt>OS</tt> value representing the current Operating System.
     */
    public final OS getOS() {
        return os;
    }

    /**
     * Gets the current processor architecture the JVM is running on.
     *
     * @return A <tt>CPU</tt> value representing the current processor architecture.
     */
    public final CPU getCPU() {
        return cpu;
    }

    /**
     * Gets the version of the Java Virtual Machine (JVM) jffi is running on.
     *
     * @return A number representing the java version.  e.g. 5 for java 1.5, 6 for java 1.6
     */
    public final int getJavaMajorVersion() {
        return javaVersionMajor;
    }
    public final boolean isBSD() {
        return os == OS.FREEBSD || os == os.OPENBSD || os == OS.NETBSD || os == OS.DARWIN;
    }
    public final boolean isUnix() {
        return os != OS.WINDOWS;
    }
    /**
     * Gets the size of a C 'long' on the native platform.
     *
     * @return the size of a long in bits
     */
    public final int longSize() {
        return addressSize;
    }

    /**
     * Gets the size of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     */
    public final int addressSize() {
        return addressSize;
    }

    /**
     * Gets the 32/64bit mask of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     */
    public final long addressMask() {
        return addressMask;
    }

    /**
     * Gets the name of this <tt>Platform</tt>.
     *
     * @return The name of this platform.
     */
    public String getName() {
        String osName = System.getProperty("os.name").split(" ")[0];
        return System.getProperty("os.arch") + "-" + osName;
    }

    /**
     * Maps from a generic library name (e.g. "c") to the platform specific library name.
     *
     * @param libName The library name to map
     * @return The mapped library name.
     */
    public String mapLibraryName(String libName) {
        //
        // A specific version was requested - use as is for search
        //
        if (libName.matches(getLibraryNamePattern())) {
            return libName;
        }
        return System.mapLibraryName(libName);
    }

    /**
     * Gets the regex string used to match platform-specific libraries
     * @return
     */
    public String getLibraryNamePattern() {
        return "lib.*\\.so.*$";
    }

    /**
     * Searches through a list of directories for a native library.
     *
     * @param libName the base name (e.g. "c") of the library to locate
     * @param libraryPath the list of directories to search
     * @return the path of the library
     */
    public File locateLibrary(String libName, List<String> libraryPath) {
        String mappedName = mapLibraryName(libName);
        for (String path : libraryPath) {
            File libFile = new File(path, mappedName);
            if (libFile.exists()) {
                return libFile;
            }
        }
        // Default to letting the system search for it
        return new File(mappedName);
    }
    
    private static final class Default extends Platform {

        public Default(OS os) {
            super(os);
        }

    }
    /**
     * A {@link Platform} subclass representing the MacOS system.
     */
    private static final class Darwin extends Platform {

        public Darwin() {
            super(OS.DARWIN);
        }

        @Override
        public String mapLibraryName(String libName) {
            //
            // A specific version was requested - use as is for search
            //
            if (libName.matches(getLibraryNamePattern())) {
                return libName;
            }
            return "lib" + libName + ".dylib";
        }
        @Override
        public String getLibraryNamePattern() {
            return "lib.*\\.(dylib|jnilib)$";
        }
        
        @Override
        public String getName() {
            return "Darwin";
        }

    }
    /**
     * A {@link Platform} subclass representing the Linux operating system.
     */
    private static final class Linux extends Platform {

        public Linux() {
            super(OS.LINUX);
        }

        @Override
        public File locateLibrary(final String libName, List<String> libraryPath) {
            FilenameFilter filter = new FilenameFilter() {
                Pattern p = Pattern.compile("lib" + libName + "\\.so\\.[0-9]+$");
                String exact = "lib" + libName + ".so";
                public boolean accept(File dir, String name) {
                    return p.matcher(name).matches() || exact.equals(name);
                }
            };

            List<File> matches = new LinkedList<File>();
            for (String path : libraryPath) {
                File[] files = new File(path).listFiles(filter);
                if (files != null && files.length > 0) {
                    matches.addAll(Arrays.asList(files));
                }
            }

            //
            // Search through the results and return the highest numbered version
            // i.e. libc.so.6 is preferred over libc.so.5
            //
            int version = 0;
            String bestMatch = null;
            for (File file : matches) {
                String path = file.getAbsolutePath();
                if (bestMatch == null && path.endsWith(".so")) {
                    bestMatch = path;
                    version = 0;
                } else {
                    String num = path.substring(path.lastIndexOf(".so.") + 4);
                    try {
                        if (Integer.parseInt(num) >= version) {
                            bestMatch = path;
                        }
                    } catch (NumberFormatException e) {
                    } // Just skip if not a number
                }
            }
            return bestMatch != null ? new File(bestMatch) : new File(mapLibraryName(libName));
        }
        @Override
        public String mapLibraryName(String libName) {
            // Older JDK on linux map 'c' to 'libc.so' which doesn't work
            return "c".equals(libName) || "libc.so".equals(libName)
                    ? "libc.so.6" : super.mapLibraryName(libName);
        }
    }

    /**
     * A {@link Platform} subclass representing the Windows system.
     */
    private static class Windows extends Platform {

        public Windows() {
            super(OS.WINDOWS);
        }

        @Override
        public String getLibraryNamePattern() {
            return ".*\\.dll$";
        }
    }
}

