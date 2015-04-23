/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi.util;

import java.lang.annotation.Annotation;
import java.util.Comparator;

/**
 * Sorts annotations according to name
 */
final class AnnotationNameComparator implements Comparator<Annotation> {
    static final Comparator<Annotation> INSTANCE = new AnnotationNameComparator();

    public static Comparator<Annotation> getInstance() {
        return INSTANCE;
    }

    public int compare(Annotation o1, Annotation o2) {
        return o1.annotationType().getName().compareTo(o2.annotationType().getName());
    }

    public boolean equals(Object other) {
        return other != null && getClass().equals(other.getClass());
    }
}
