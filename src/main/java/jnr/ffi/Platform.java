/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class Platform {
    private static final java.util.Locale LOCALE = java.util.Locale.ENGLISH;
    private final OS os;
    private final CPU cpu;
    private final int addressSize;
    private final int longSize;
    protected final Pattern libPattern;

    private static final class SingletonHolder {
        static final Platform PLATFORM = determinePlatform();
    }

    /**
     * The common names of supported operating systems.
     */
    public enum OS {
        /*
         * Note The names of the enum values are used in other parts of the
         * code to determine where to find the native stub library.  Do not rename.
         */

        /** MacOSX */
        DARWIN,
        /** FreeBSD */
        FREEBSD,
        /** NetBSD */
        NETBSD,
        /** OpenBSD */
        OPENBSD,
        /** Linux */
        LINUX,
        /** Solaris (and OpenSolaris) */
        SOLARIS,
        /** The evil borg operating system */
        WINDOWS,
        /** IBM AIX */
        AIX,
        /** IBM zOS **/
        ZLINUX,
        /** No idea what the operating system is */
        UNKNOWN;

        @Override
        public String toString() {
            return name().toLowerCase(LOCALE);
        }
    }

        /**
     * The supported CPU architectures.
     */
    public enum CPU {
        /*
         * <b>Note</b> The names of the enum values are used in other parts of the
         * code to determine where to find the native stub library.  Do NOT rename.
         */

        /** 32 bit legacy Intel */
        I386,

        /** 64 bit AMD (aka EM64T/X64) */
        X86_64,

        /** 32 bit Power PC */
        PPC,

        /** 64 bit Power PC */
        PPC64,

        /** 64 bit Power PC little endian */
        PPC64LE,

        /** 32 bit Sun sparc */
        SPARC,

        /** 64 bit Sun sparc */
        SPARCV9,

        /** IBM zSeries S/390 */
        S390X,

        /** 32 bit MIPS (used by nestedvm) */
        MIPS32,

        /** 32 bit ARM */
        ARM,

        /**
         * Unknown CPU architecture.  A best effort will be made to infer architecture
         * specific values such as address and long size.
         */
        UNKNOWN;

        /**
         * Returns a {@code String} object representing this {@code CPU} object.
         *
         * @return the name of the cpu architecture as a lower case {@code String}.
         */
        @Override
        public String toString() {
            return name().toLowerCase(LOCALE);
        }
    }

    /**
     * Determines the operating system jffi is running on
     *
     * @return An member of the <tt>OS</tt> enum.
     */
    private static OS determineOS() {
        String osName = System.getProperty("os.name").split(" ")[0];
        if (startsWithIgnoreCase(osName, "mac") || startsWithIgnoreCase(osName, "darwin")) {
            return OS.DARWIN;
        } else if (startsWithIgnoreCase(osName, "linux")) {
            return OS.LINUX;
        } else if (startsWithIgnoreCase(osName, "sunos") || startsWithIgnoreCase(osName, "solaris")) {
            return OS.SOLARIS;
        } else if (startsWithIgnoreCase(osName, "aix")) {
            return OS.AIX;
        } else if (startsWithIgnoreCase(osName, "openbsd")) {
            return OS.OPENBSD;
        } else if (startsWithIgnoreCase(osName, "freebsd")) {
            return OS.FREEBSD;
        } else if (startsWithIgnoreCase(osName, "windows")) {
            return OS.WINDOWS;
        } else {
            return OS.UNKNOWN;
        }
    }

    /**
     * Determines the <tt>Platform</tt> that best describes the <tt>OS</tt>
     *
     * @param os The operating system.
     * @return An instance of <tt>Platform</tt>
     */
    private static Platform determinePlatform(OS os) {
        switch (os) {
            case DARWIN:
                return new Darwin();
            case LINUX:
                return new Linux();
            case WINDOWS:
                return new Windows();
            case UNKNOWN:
                return new Unsupported(os);
            default:
                return new Default(os);
        }
    }

    private static Platform determinePlatform() {
        String providerName = System.getProperty("jnr.ffi.provider");
        try {
            Class c = Class.forName(providerName + "$Platform");
            return (Platform) c.newInstance();
        } catch (ClassNotFoundException ex) {
            return determinePlatform(determineOS());
        } catch (IllegalAccessException ex) {
            throw new ExceptionInInitializerError(ex);
        } catch (InstantiationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    private static CPU determineCPU() {
        String archString = System.getProperty("os.arch");
        if (equalsIgnoreCase("x86", archString) || equalsIgnoreCase("i386", archString) || equalsIgnoreCase("i86pc", archString)) {
            return CPU.I386;
        } else if (equalsIgnoreCase("x86_64", archString) || equalsIgnoreCase("amd64", archString)) {
            return CPU.X86_64;
        } else if (equalsIgnoreCase("ppc", archString) || equalsIgnoreCase("powerpc", archString)) {
            return CPU.PPC;
        } else if (equalsIgnoreCase("ppc64", archString) || equalsIgnoreCase("powerpc64", archString)) {
            if ("little".equals(System.getProperty("sun.cpu.endian"))) {
                return CPU.PPC64LE;
            }
            return CPU.PPC64;
        } else if (equalsIgnoreCase("ppc64le", archString) || equalsIgnoreCase("powerpc64le", archString)) {
            return CPU.PPC64LE;
        } else if (equalsIgnoreCase("s390", archString) || equalsIgnoreCase("s390x", archString)) {
            return CPU.S390X;
        }

        // Try to find by lookup up in the CPU list
        for (CPU cpu : CPU.values()) {
            if (equalsIgnoreCase(cpu.name(), archString)) {
                return cpu;
            }
        }

        return CPU.UNKNOWN;
    }

    public Platform(OS os, CPU cpu, int addressSize, int longSize, String libPattern) {
        this.os = os;
        this.cpu = cpu;
        this.addressSize = addressSize;
        this.longSize = longSize;
        this.libPattern = Pattern.compile(libPattern);
    }
    
    private Platform(OS os) {
        this.os = os;
        this.cpu = determineCPU();
        
        String libpattern;
        switch (os) {
            case WINDOWS:
                libpattern = ".*\\.dll$";
                break;
            case DARWIN:
                libpattern = "lib.*\\.(dylib|jnilib)$";
                break;
            default:
                libpattern = "lib.*\\.so.*$";
                break;
        }
        libPattern = Pattern.compile(libpattern);

        this.addressSize = calculateAddressSize(cpu);
        this.longSize = os == OS.WINDOWS ? 32 : addressSize;
    }

    private static int calculateAddressSize(CPU cpu) {
        int dataModel = Integer.getInteger("sun.arch.data.model");
        if (dataModel != 32 && dataModel != 64) {
            switch (cpu) {
                case I386:
                case PPC:
                case SPARC:
                    dataModel = 32;
                    break;
                case X86_64:
                case PPC64:
                case PPC64LE:
                case SPARCV9:
                case S390X:
                    dataModel = 64;
                    break;
                default:
                    throw new ExceptionInInitializerError("Cannot determine cpu address size");
            }
        }

        return dataModel;
    }

    /**
     * Gets the native <tt>Platform</tt>
     *
     * @return The current platform.
     */
    public static Platform getNativePlatform() {
        return SingletonHolder.PLATFORM;
    }

    @Deprecated
    public static Platform getPlatform() {
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
    
    public final boolean isBSD() {
        return os == OS.FREEBSD || os == OS.OPENBSD || os == OS.NETBSD || os == OS.DARWIN;
    }
    public final boolean isUnix() {
        return os != OS.WINDOWS;
    }

    /**
     * Gets the size of a C 'long' on the native platform.
     *
     * @return the size of a long in bits
     * @deprecated Use {@link Runtime#longSize()} instead.
     */
    public final int longSize() {
        return longSize;
    }

    /**
     * Gets the size of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     * @deprecated Use {@link Runtime#addressSize()} instead.
     */
    public final int addressSize() {
        return addressSize;
    }

    /**
     * Gets the name of this <tt>Platform</tt>.
     *
     * @return The name of this platform.
     */
    public String getName() {
        return cpu + "-" + os;
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
        if (libPattern.matcher(libName).find()) {
            return libName;
        }
        return System.mapLibraryName(libName);
    }

    /**
     * Searches through a list of directories for a native library.
     *
     * @param libName the base name (e.g. "c") of the library to locate
     * @param libraryPath the list of directories to search
     * @return the path of the library
     */
    public String locateLibrary(String libName, List<String> libraryPath) {
        String mappedName = mapLibraryName(libName);
        for (String path : libraryPath) {
            File libFile = new File(path, mappedName);
            if (libFile.exists()) {
                return libFile.getAbsolutePath();
            }
        }
        // Default to letting the system search for it
        return mappedName;
    }
    private static class Supported extends Platform {
        public Supported(OS os) {
            super(os);
        }
    }

    private static class Unsupported extends Platform {
        public Unsupported(OS os) {
            super(os);
        }
    }

    private static final class Default extends Supported {

        public Default(OS os) {
            super(os);
        }

    }
    /**
     * A {@link Platform} subclass representing the MacOS system.
     */
    private static final class Darwin extends Supported {

        public Darwin() {
            super(OS.DARWIN);
        }

        @Override
        public String mapLibraryName(String libName) {
            //
            // A specific version was requested - use as is for search
            //
            if (libPattern.matcher(libName).find()) {
                return libName;
            }
            return "lib" + libName + ".dylib";
        }
        
        @Override
        public String getName() {
            return "Darwin";
        }

    }
    /**
     * A {@link Platform} subclass representing the Linux operating system.
     */
    private static final class Linux extends Supported {

        public Linux() {
            super(OS.LINUX);
        }

        @Override
        public String locateLibrary(final String libName, List<String> libraryPath) {
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
            int bestVersion = -1;
            String bestMatch = null;
            for (File file : matches) {
                String path = file.getAbsolutePath();
                int fileVersion;
                if (path.endsWith(".so")) {
                    fileVersion = 0;
                } else {
                    String num = path.substring(path.lastIndexOf(".so.") + 4);
                    try {
                        fileVersion = Integer.parseInt(num);
                    } catch (NumberFormatException e) {
                        continue; // Just skip if not a number
                    }
                }
                if (fileVersion > bestVersion) {
                    bestMatch = path;
                    bestVersion = fileVersion;
                }
            }
            return bestMatch != null ? bestMatch : mapLibraryName(libName);
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
    private static class Windows extends Supported {

        public Windows() {
            super(OS.WINDOWS);
        }
    }

    private static boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.startsWith(s2)
            || s1.toUpperCase(LOCALE).startsWith(s2.toUpperCase(LOCALE))
            || s1.toLowerCase(LOCALE).startsWith(s2.toLowerCase(LOCALE));
    }

    private static boolean equalsIgnoreCase(String s1, String s2) {
        return s1.equalsIgnoreCase(s2)
            || s1.toUpperCase(LOCALE).equals(s2.toUpperCase(LOCALE))
            || s1.toLowerCase(LOCALE).equals(s2.toLowerCase(LOCALE));
    }
}

