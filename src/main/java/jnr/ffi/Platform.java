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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
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
        /** DragonFly */
        DRAGONFLY,
        /** Linux */
        LINUX,
        /** Solaris (and OpenSolaris) */
        SOLARIS,
        /** The evil borg operating system */
        WINDOWS,
        /** IBM AIX */
        AIX,
        /** IBM i */
        IBMI,
        /** IBM zOS **/
        ZLINUX,
        /** MidnightBSD **/
        MIDNIGHTBSD,
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

        /** 64 bit ARM */
        AARCH64,

        /** 64 bit MIPS */
        MIPS64EL,

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
     * @return An member of the <code>OS</code> enum.
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
        } else if (startsWithIgnoreCase(osName, "os400") || startsWithIgnoreCase(osName, "os/400")) {
            return OS.IBMI;
        } else if (startsWithIgnoreCase(osName, "openbsd")) {
            return OS.OPENBSD;
        } else if (startsWithIgnoreCase(osName, "freebsd")) {
            return OS.FREEBSD;
        } else if (startsWithIgnoreCase(osName, "dragonfly")) {
            return OS.DRAGONFLY;
        } else if (startsWithIgnoreCase(osName, "windows")) {
            return OS.WINDOWS;
        } else if (startsWithIgnoreCase(osName, "midnightbsd")) {
            return OS.MIDNIGHTBSD;
        } else {
            return OS.UNKNOWN;
        }
    }

    /**
     * Determines the <code>Platform</code> that best describes the <code>OS</code>
     *
     * @param os The operating system.
     * @return An instance of <code>Platform</code>
     */
    private static Platform determinePlatform(OS os) {
        switch (os) {
            case DARWIN:
                return new Darwin();
            case LINUX:
                return new Linux();
            case WINDOWS:
                return new Windows();
            case IBMI:
                return new IbmI();
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
        if (equalsIgnoreCase("x86", archString) ||
                equalsIgnoreCase("i386", archString) ||
                equalsIgnoreCase("i86pc", archString) ||
                equalsIgnoreCase("i686", archString)) {
            return CPU.I386;
        } else if (equalsIgnoreCase("x86_64", archString) || equalsIgnoreCase("amd64", archString)) {
            return CPU.X86_64;
        } else if (equalsIgnoreCase("ppc", archString) || equalsIgnoreCase("powerpc", archString)) {
            if (OS.IBMI.equals(determineOS()))
                return CPU.PPC64;
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
        } else if (equalsIgnoreCase("aarch64", archString)) {
            return CPU.AARCH64;
        } else if (equalsIgnoreCase("arm", archString) || equalsIgnoreCase("armv7l", archString)) {
            return CPU.ARM;
        } else if (equalsIgnoreCase("mips64", archString) || equalsIgnoreCase("mips64el", archString)) {
            return CPU.MIPS64EL;
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
            case IBMI:
                libpattern = "lib.*\\.(so|a\\(shr.o\\)|a\\(shr_64.o\\)|a|so.[\\.0-9]+)$";
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
        Integer dataModel = Integer.getInteger("sun.arch.data.model");
        if (dataModel == null || dataModel != 32 && dataModel != 64) {
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
                case AARCH64:
		case MIPS64EL:
                    dataModel = 64;
                    break;
                default:
                    throw new ExceptionInInitializerError("Cannot determine cpu address size");
            }
        }

        return dataModel;
    }

    /**
     * Gets the native <code>Platform</code>
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
     * @return A <code>OS</code> value representing the current Operating System.
     */
    public final OS getOS() {
        return os;
    }

    /**
     * Gets the current processor architecture the JVM is running on.
     *
     * @return A <code>CPU</code> value representing the current processor architecture.
     */
    public final CPU getCPU() {
        return cpu;
    }
    
    public final boolean isBSD() {
        return os == OS.FREEBSD || os == OS.OPENBSD || os == OS.NETBSD || os == OS.DARWIN || os == OS.DRAGONFLY | os == OS.MIDNIGHTBSD;
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
     * Returns true if the current platform is little endian
     * @return true if little endian, false otherwise or if cannot determine
     */
    public final boolean isLittleEndian() {
        return "little".equals(System.getProperty("sun.cpu.endian"));
    }

    /**
     * Returns true if the current platform is big endian
     * @return true if big endian, false otherwise or if cannot determine
     */
    public final boolean isBigEndian() {
        return "big".equals(System.getProperty("sun.cpu.endian"));
    }

    /**
     * Gets the name of this <code>Platform</code>.
     *
     * @return The name of this platform.
     */
    public String getName() {
        return cpu + "-" + os;
    }

    /**
     * Returns the platform specific standard C library name
     * 
     * @return The standard C library name
     */
    public String getStandardCLibraryName() {
        switch (os) {
        case LINUX:
            return "libc.so.6";
        case SOLARIS:
            return "c";
        case DRAGONFLY:
        case FREEBSD:
        case MIDNIGHTBSD:
        case NETBSD:
            return "c";
        case AIX:
        case IBMI:
            return addressSize == 32
                ? "libc.a(shr.o)"
                : "libc.a(shr_64.o)";
        case WINDOWS:
            return "msvcrt";
        default:
            return "c";
        }
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

    /**
     * Searches through a list of directories for a native library.
     *
     * @param libName      the base name (e.g. "c") of the library to locate
     * @param libraryPaths the list of directories to search
     * @param options      map of {@link LibraryOption}s to customize search behavior
     *                     such as {@link LibraryOption#PreferCustomPaths}
     * @return the path of the library
     */
    public String locateLibrary(String libName, List<String> libraryPaths, Map<LibraryOption, Object> options) {
        return locateLibrary(libName, libraryPaths);
    }

    /**
     * Returns a list of absolute paths to the found locations of a library with the base name {@code libName},
     * if the returned list is empty then the library could not be found and will fail to be loaded as a result.
     *
     * Even if a library is found, this does not guarantee that it will successfully be loaded, it only guarantees
     * that the reason for the failure was not that it was not found.
     *
     * @param libName         the base name (e.g. "c") of the library to locate
     * @param additionalPaths additional paths to search, these take precedence over default paths,
     *                        (as is the behavior in {@link LibraryLoader})
     *                        pass null to only search in the default paths
     * @return the list of absolute paths where the library was found
     */
    public List<String> libraryLocations(String libName, List<String> additionalPaths) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> libDirs = new ArrayList<>();
        if (additionalPaths != null) libDirs.addAll(additionalPaths); // customPaths first!
        libDirs.addAll(LibraryLoader.DefaultLibPaths.PATHS);

        // locateLibrary can either give us an absolute path with the version at the end (for Linux)
        //  or just the name (forwards to mapLibraryName), either way we only want the name, we will
        //  add the parent later from libDirs
        String name = new File(locateLibrary(libName, libDirs)).getName();
        for (String libDir : libDirs) {
            File libFile = new File(libDir, name);
            if (libFile.exists()) {
                result.add(libFile.getAbsolutePath());
            }
        }
        return result;
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

    }

    static final class IbmI extends Supported {
        public IbmI() {
            super(OS.IBMI);
        }

        @Override
        public String mapLibraryName(String libName) {
            //
            // A specific version was requested - use as is for search
            //
            if (libPattern.matcher(libName).find()) {
                return libName;
            }
            return "lib" + libName + ".a(shr_64.o)";
        }
        @Override
        public String locateLibrary(final String libName, List<String> libraryPaths) {
            final Pattern versionedLibPattern = Pattern.compile("lib" + libName + "\\.so((?:\\.[0-9]+)*)$");
            final Pattern dotAorSoPattern = Pattern.compile("lib" + libName + "\\.(a|so)$");
            List<File> dotAorSoFiles = new java.util.LinkedList<File>();

            List<String> searchPaths = new java.util.LinkedList<String>();
            searchPaths.addAll(libraryPaths);
            searchPaths.add("/QOpenSys/pkgs/lib");
            searchPaths.add("/QOpenSys/usr/lib");

            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return dotAorSoPattern.matcher(name).matches() ||  versionedLibPattern.matcher(name).matches() ;
                }
            };
            Map<String, int[]> matches = new LinkedHashMap<String, int[]>();
            for (String path : searchPaths) {
                if(path.toLowerCase(LOCALE).startsWith("/qsys")) {
                    continue;
                }
                File libraryPath = new File(path);
                File[] files = libraryPath.listFiles(filter);
                if (files == null) {
                    continue;
                }

                for (File file : files) {
                    if (dotAorSoPattern.matcher(file.getName()).matches()) {
                        dotAorSoFiles.add(file);
                        continue;
                    }
                    Matcher matcher = versionedLibPattern.matcher(file.getName());
                    String versionString = matcher.matches() ? matcher.group(1) : "";
                    int[] version;
                    if (versionString == null || versionString.isEmpty()) {
                        version = new int[0];
                    } else {
                        String[] parts = versionString.split("\\.");
                        version = new int[parts.length - 1];
                        for (int i = 1; i < parts.length; i++) {
                            version[i - 1] = Integer.parseInt(parts[i]);
                        }
                    }
                    matches.put(file.getAbsolutePath(), version);
                }
            }

            //
            // Search through the results and return the highest numbered version
            // i.e. libc.so.6 is preferred over libc.so.5
            //
            int[] bestVersion = null;
            String bestMatch = null;
            for (Map.Entry<String,int[]> entry : matches.entrySet()) {
                String file = entry.getKey();
                int[] fileVersion = entry.getValue();

                if (Linux.compareVersions(fileVersion, bestVersion) > 0) {
                    bestMatch = file;
                    bestVersion = fileVersion;
                }
            }
            if (null != bestMatch) {
                return bestMatch;
            }
            if (!dotAorSoFiles.isEmpty()) {
                String qualifiedAorSo = dotAorSoFiles.get(0).getAbsolutePath();
                if(qualifiedAorSo.endsWith(".a")) {
                    qualifiedAorSo +="(shr_64.o)";
                }
                return qualifiedAorSo;
            }

            return mapLibraryName(libName);
        }
    }

    /**
     * A {@link Platform} subclass representing the Linux operating system.
     */
    static final class Linux extends Supported {

        // represents a valid library file that matches the search
        private static class Match implements Comparable<Match> {
            String path; // absolute path of library file
            int[] version; // version of library, empty if no version specified
            boolean isCustom; // if path is from a custom searchPath specified by user and not default path

            @Override
            public int compareTo(Match o) { // for Collections.sort() to work
                return compareVersions(o.version, this.version);
            }
        }

        public Linux() {
            super(OS.LINUX);
        }

        @Override
        public String locateLibrary(String libName, List<String> libraryPaths) {
            return locateLibrary(libName, libraryPaths, null);
        }

        @Override
        public String locateLibrary(final String libName, List<String> libraryPaths,
                                    Map<LibraryOption, Object> options) {
            List<Match> matches = getMatches(libName, libraryPaths);
            if (matches.isEmpty()) return mapLibraryName(libName); // no matches, default behavior returns mapped name

            boolean preferCustom = options != null && options.containsKey(LibraryOption.PreferCustomPaths);

            Collections.sort(matches); // sort by version, regardless of location

            Match best = null;
            if (preferCustom) {
                for (Match match : matches) {
                    if (match.isCustom) {
                        best = match;
                        break;
                    }
                }
            }
            return best != null ? best.path : matches.get(0).path;
        }

        private List<Match> getMatches(String libName, List<String> libraryPaths) {
            List<String> customPaths = new ArrayList<>();
            if (LibraryLoader.DefaultLibPaths.PATHS.size() > 0 &&
                    libraryPaths.size() >= LibraryLoader.DefaultLibPaths.PATHS.size()) {
                // we were probably called by JNR-FFI, customs will always be before system paths
                String firstSystemPath = LibraryLoader.DefaultLibPaths.PATHS.get(0);

                // everything before last occurrence of first system path is custom
                int firstSystemPathIndex = libraryPaths.lastIndexOf(firstSystemPath);
                for (int i = 0; i < firstSystemPathIndex; i++) {
                    customPaths.add(libraryPaths.get(i));
                }
            } else {
                // we were probably called by user and not by JNR-FFI, assume all paths are custom
                customPaths.addAll(libraryPaths);
            }

            Pattern exclude;
            // there are /libx32 directories in wild on ubuntu 14.04 and the
            // oracle-java8-installer package
            if (getCPU() == CPU.X86_64) {
                exclude = Pattern.compile(".*(lib[a-z]*32|i[0-9]86).*"); // ignore 32 bit libs on 64-bit
            } else {
                exclude = Pattern.compile(".*(lib[a-z]*64|amd64|x86_64).*"); // ignore 64 bit libs on 32-bit
            }

            final Pattern versionedLibPattern = Pattern.compile("lib" + libName + "\\.so((?:\\.[0-9]+)*)$");

            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return versionedLibPattern.matcher(name).matches();
                }
            };

            List<Match> matches = new ArrayList<>();
            for (String path : libraryPaths) {
                if (exclude.matcher(path).matches()) {
                    continue;
                }

                File libraryPath = new File(path);
                File[] files = libraryPath.listFiles(filter);
                if (files == null) {
                    continue;
                }

                for (File file : files) {
                    Matcher matcher = versionedLibPattern.matcher(file.getName());
                    String versionString = matcher.matches() ? matcher.group(1) : "";
                    int[] version;
                    if (versionString == null || versionString.isEmpty()) {
                        version = new int[0];
                    } else {
                        String[] parts = versionString.split("\\.");
                        version = new int[parts.length - 1];
                        for (int i = 1; i < parts.length; i++) {
                            version[i - 1] = Integer.parseInt(parts[i]);
                        }
                    }
                    Match match = new Match();
                    match.path = file.getAbsolutePath();
                    match.version = version;
                    match.isCustom = customPaths.contains(path);
                    matches.add(match);
                }
            }
            return matches;
        }

        private static int compareVersions(int[] version1, int[] version2) {
            // Null is always smallest
            if (version1 == null) {
                return version2 == null ? 0 : -1;
            }
            if (version2 == null) {
                return 1;
            }

            // Compare component by component
            int commonLength = Math.min(version1.length, version2.length);
            for (int i = 0; i < commonLength; i++) {
                if (version1[i] < version2[i]) {
                    return -1;
                } else if (version1[i] > version2[i]) {
                    return 1;
                }
            }

            // If all components are equal, version with fewest components is smallest
            return Integer.compare(version1.length, version2.length);
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

