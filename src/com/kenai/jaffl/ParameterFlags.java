/* 
 * Copyright (C) 2008 Wayne Meissner
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

package com.kenai.jaffl;

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
    public static final int TRANSIENT = 0x04;
    public static final int PINNED = 0x08;
    public static final int NULTERMINATE = 0x10;
    
    public static final int parse(Annotation[] annotations) {
        int flags = 0;
        for (Annotation a : annotations) {
            flags |= a instanceof Out ? OUT : 0;
            flags |= a instanceof In ? IN : 0;
            flags |= a instanceof Transient ? TRANSIENT : 0;
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
