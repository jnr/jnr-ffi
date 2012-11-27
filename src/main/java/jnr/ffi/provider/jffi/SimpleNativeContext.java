package jnr.ffi.provider.jffi;

import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.ToNativeContext;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: wayne
 * Date: 27/11/12
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleNativeContext implements ToNativeContext, FromNativeContext {
    private final Collection<Annotation> annotations;

    SimpleNativeContext(Annotation[] annotationArray) {
        this.annotations = InvokerUtil.annotationCollection(annotationArray);
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }
}
