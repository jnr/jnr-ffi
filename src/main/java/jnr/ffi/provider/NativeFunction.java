package jnr.ffi.provider;

import jnr.ffi.CallingConvention;
import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.SaveError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class NativeFunction {
    private final Method method;
    private final Collection<Annotation> annotations;
    private final boolean saveError;
    private final CallingConvention callingConvention;

    public NativeFunction(Method method, CallingConvention callingConvention) {
        this.method = method;
        this.annotations = Collections.unmodifiableCollection(Arrays.asList(method.getAnnotations()));
        boolean saveError = true;
        for (Annotation a : annotations) {
            if (a instanceof IgnoreError) {
                saveError = false;
            } else if (a instanceof SaveError) {
                saveError = true;
            }
        }
        this.saveError = saveError;
        this.callingConvention = callingConvention;
    }

    public Collection<Annotation> annotations() {
        return annotations;
    }
    
    public CallingConvention convention() {
        return callingConvention;
    }
    
    public String name() {
        return method.getName();
    }
    
    public boolean isErrnoRequired() {
        return saveError;
    }

    public Method getMethod() {
        return method;
    }
}
