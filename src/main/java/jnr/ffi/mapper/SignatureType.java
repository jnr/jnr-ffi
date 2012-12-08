package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

/**
*
*/
public interface SignatureType {
    public Class getDeclaredType();
    public Collection<Annotation> getAnnotations();
    public java.lang.reflect.Type getGenericType();
}
