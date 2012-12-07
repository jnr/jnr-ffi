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
