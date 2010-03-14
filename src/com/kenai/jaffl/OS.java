package com.kenai.jaffl;

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
        return name().toLowerCase();
    }
}
