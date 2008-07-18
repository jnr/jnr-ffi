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

package jafl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class Platform {
    public enum OS {
        DARWIN,
        FREEBSD,
        LINUX,
        MAC,
        NETBSD,
        OPENBSD,
        SUNOS,
        WINDOWS,

        UNKNOWN;
    }
    public enum ARCH {
        I386,
        PPC,
        SPARC,
        SPARCV9,
        UNKNOWN;
    }
    private static final Platform platform;
    private static final String stubLibraryName = "jffi";
    private static final OS osType;
    public static final ARCH archType;
    private static final int javaVersionMajor;
    static {
        String osName = System.getProperty("os.name").split(" ")[0];
        OS os = OS.UNKNOWN;
        try {
            os = OS.valueOf(osName.toUpperCase());
        } catch (Exception ex) {
            throw new ExceptionInInitializerError("Unknown Operating System");
        }
        switch (os) {
            case MAC:
            case DARWIN:
                platform = new MacOSX();
                break;
            case LINUX:
                platform = new Linux();
                break;
            case WINDOWS:
                platform = new Windows();
                break;
            default:
                platform = new Platform();
                break;
        }
        osType = os;
        ARCH arch = ARCH.UNKNOWN;
        try {
            arch = ARCH.valueOf(System.getProperty("os.arch").toUpperCase());
        } catch (Exception ex) {
            throw new ExceptionInInitializerError("Unknown CPU architecture");
        }
        archType = arch;
        int version = 5;
        try {
            String versionString = System.getProperty("java.vm.version");
            if (versionString != null) {
                String[] v = versionString.split("\\.");
                version = Integer.valueOf(v[1]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        javaVersionMajor = version;
    }
    private final int addressSize;
    private Platform() {
        final int dataModel = Integer.getInteger("sun.arch.data.model");
        if (dataModel != 32 && dataModel != 64) {
            throw new IllegalArgumentException("Unsupported data model");
        }
        addressSize = dataModel;
    }
    public static final Platform getPlatform() {
        return platform;
    }
    public static final OS getOS() {
        return osType;
    }
    public static final ARCH getArch() {
        return archType;
    }
    public static final boolean is64() {
        return getPlatform().addressSize() == 64;
    }
    public static final int getJavaVersion() {
        return javaVersionMajor;
    }
    public static final boolean isMac() {
        return osType == OS.MAC || osType == OS.DARWIN;
    }
    public static final boolean isLinux() {
        return osType == OS.LINUX;
    }
    public static final boolean isWindows() {
        return osType == OS.WINDOWS;
    }
    public static final boolean isSolaris() {
        return osType == OS.SUNOS;
    }
    public static final boolean isFreeBSD() {
        return osType == OS.FREEBSD;
    }
    public static final boolean isUnix() {
        return !isWindows();
    }
    /**
     * Gets the size of a C 'long' on the native platform.
     *
     * @return the size of a long in bits
     */
    public int longSize() {
        return addressSize;
    }

    /**
     * Gets the size of a C address/pointer on the native platform.
     *
     * @return the size of a pointer in bits
     */
    public int addressSize() {
        return addressSize;
    }

    /**
     * Gets the name of this <tt>Platform</tt>.
     *
     * @return The name of this platform.
     */
    public final String getName() {
        if (Platform.isMac()) {
            return "Darwin";
        }
        String osName = System.getProperty("os.name").split(" ")[0];
        return System.getProperty("os.arch") + "-" + osName;
    }

    public String mapLibraryName(String libName) {
        //
        // A specific version was requested - use as is for search
        //
        if (libName.matches(getLibraryNamePattern())) {
            return libName;
        }
        return System.mapLibraryName(libName);
    }

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

    /**
     * A {@link Platform} subclass representing the MacOS system.
     */
    private static class MacOSX extends Platform {
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
    }
    /**
     * A {@link Platform} subclass representing the Linux operating system.
     */
    private static class Linux extends Platform {
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
    }

    /**
     * A {@link Platform} subclass representing the Windows system.
     */
    private static class Windows extends Platform {
        @Override
        public int longSize() {
            return 32;
        }
        @Override
        public String getLibraryNamePattern() {
            return ".*\\.dll$";
        }
    }
}

