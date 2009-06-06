
package com.kenai.jaffl;

import com.kenai.jaffl.annotations.Direct;
import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.NulTerminate;
import com.kenai.jaffl.annotations.Out;
import com.kenai.jaffl.annotations.Pinned;
import com.kenai.jaffl.annotations.Transient;
import java.lang.annotation.Annotation;

/**
 *
 * @author wayne
 */
public final class ParameterFlags {
    private ParameterFlags() {}
    public static final int OUT = 0x01;
    public static final int IN = 0x02;
    public static final int PINNED = 0x04;
    public static final int NULTERMINATE = 0x08;
    public static final int TRANSIENT = 0x10;
    public static final int DIRECT = 0x20;

    public static final int parse(Annotation[] annotations) {
        int flags = 0;
        for (Annotation a : annotations) {
            flags |= a instanceof Out ? OUT : 0;
            flags |= a instanceof In ? IN : 0;
            flags |= a instanceof Transient ? TRANSIENT : 0;
            flags |= a instanceof Direct ? DIRECT : 0;
            flags |= a instanceof Pinned ? PINNED : 0;
            flags |= a instanceof NulTerminate ? NULTERMINATE : 0;
        }
        return flags;
    }
    /**
     * Checks if the annotation is a recognised parameter flag.
     * 
     * @param annotation the annotation to check.
     * @return <tt>true</tt> if the annotation is a parameter flag
     */
    public static final boolean isFlag(Annotation annotation) {
        return annotation instanceof Pinned 
                || annotation instanceof Transient
                || annotation instanceof Direct
                || annotation instanceof NulTerminate
                || annotation instanceof Out 
                || annotation instanceof In;
    }
    public static final boolean isPinned(int flags) {
        return (flags & PINNED) != 0;
    }
    public static final boolean isTransient(int flags) {
        return (flags & TRANSIENT) != 0;
    }
    public static final boolean isDirect(int flags) {
        return (flags & DIRECT) != 0;
    }
    public static final boolean isNulTerminate(int flags) {
        return (flags & NULTERMINATE) != 0;
    }
    public static final boolean isOut(int flags) {
        return (flags & (OUT | IN)) != IN;
    }
    public static final boolean isIn(int flags) {
        return (flags & (OUT | IN)) != OUT;
    }
}
