package com.kenai.jaffl;

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

    /** 32 bit Sun sparc */
    SPARC,

    /** 64 bit Sun sparc */
    SPARCV9,

    /** IBM zSeries S/390 */
    S390X,

    /** 32 bit MIPS (used by nestedvm) */
    MIPS32,
    
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
        return name().toLowerCase();
    }
}
