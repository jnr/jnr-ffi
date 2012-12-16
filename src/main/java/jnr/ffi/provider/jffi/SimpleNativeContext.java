package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.ToNativeContext;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class SimpleNativeContext implements ToNativeContext, FromNativeContext {
    private final jnr.ffi.Runtime runtime;
    private final Collection<Annotation> annotations;

    SimpleNativeContext(jnr.ffi.Runtime runtime, Collection<Annotation> annotations) {
        this.runtime = runtime;
        this.annotations = annotations;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    public final jnr.ffi.Runtime getRuntime() {
        return runtime;
    }
}
