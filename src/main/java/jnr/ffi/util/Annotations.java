package jnr.ffi.util;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Utilities for collections of annotations
 */
public final class Annotations {
    private Annotations() {}

    public static Collection<Annotation> sortedAnnotationCollection(Annotation[] annotations) {
        if (annotations.length > 1) {
            return sortedAnnotationCollection(Arrays.asList(annotations));

        } else if (annotations.length > 0) {
            return Collections.singletonList(annotations[0]);

        } else {
            return Collections.emptyList();
        }
    }

    public static Collection<Annotation> sortedAnnotationCollection(Collection<Annotation> annotations) {
        // If already sorted, or empty, or only one element, no need to sort again
        if (annotations.size() < 2 || (annotations instanceof SortedSet && ((SortedSet) annotations).comparator() instanceof AnnotationNameComparator)) {
            return annotations;
        }

        SortedSet<Annotation> sorted = new TreeSet<Annotation>(AnnotationNameComparator.getInstance());
        sorted.addAll(annotations);

        return Collections.unmodifiableSortedSet(sorted);
    }
}
