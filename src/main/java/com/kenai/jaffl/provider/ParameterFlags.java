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

package com.kenai.jaffl.provider;

import com.kenai.jaffl.annotations.Direct;
import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.NulTerminate;
import com.kenai.jaffl.annotations.Out;
import com.kenai.jaffl.annotations.Pinned;
import com.kenai.jaffl.annotations.Transient;
import java.lang.annotation.Annotation;

/**
 *
 */
public final class ParameterFlags {
    private ParameterFlags() {}
    /** Contents of the parameter memory will be copied from native memory back to java */
    public static final int OUT = 0x01;
    
    /** Contents of the parameter memory will be copied from from java to native memory */
    public static final int IN = 0x02;

    /** The java array memory should be pinned by the JVM during the function call */
    public static final int PINNED = 0x04;

    /** The contents of the java array should have a zero byte appended */
    public static final int NULTERMINATE = 0x08;

    /** When allocating memory for the parameter, a temporary memory block can be used */
    public static final int TRANSIENT = 0x10;

    /** When allocating memory for the parameter, allocate a persistent memory block */
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
