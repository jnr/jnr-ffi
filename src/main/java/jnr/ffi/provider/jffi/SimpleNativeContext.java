package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.ToNativeContext;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class SimpleNativeContext implements ToNativeContext, FromNativeContext {
    private final Collection<Annotation> annotations;

    SimpleNativeContext(Collection<Annotation> annotations) {
        this.annotations = annotations;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }
}
