package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.util.*;

class Util {
    static Collection<Annotation> annotationCollection(Annotation[] annotations) {
        if (annotations.length > 1) {
            Arrays.sort(annotations, AnnotationComparator.INSTANCE);
            return Collections.unmodifiableList(Arrays.asList(annotations));

        } else if (annotations.length > 0) {
            return Collections.singletonList(annotations[0]);

        } else {
            return Collections.emptyList();
        }
    }

    private static final class AnnotationComparator implements Comparator<Annotation> {
        static final Comparator<Annotation> INSTANCE = new AnnotationComparator();

        public int compare(Annotation o1, Annotation o2) {
            return o1.annotationType().getName().compareTo(o2.annotationType().getName());
        }
    }
}
