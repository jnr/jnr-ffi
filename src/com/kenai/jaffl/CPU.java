package com.kenai.jaffl;

/**
 * The common names of cpu architectures.
 *
 * <b>Note</b> The names of the enum values are used in other parts of the
 * code to determine where to find the native stub library.  Do not rename.
 */
public enum CPU {

    /** Intel ia32 */
    I386,
    /** AMD 64 bit (aka EM64T/X64) */
    X86_64,
    /** Power PC 32 bit */
    PPC,
    /** Power PC 64 bit */
    PPC64,
    /** Sun sparc 32 bit */
    SPARC,
    /** Sun sparc 64 bit */
    SPARCV9,
    /** IBM zSeries S/390 64 bit */
    S390X,
    /** 32 bit MIPS (used by nestedvm) */
    MIPS32,
    /** Unknown CPU */
    UNKNOWN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
