
package com.kenai.jaffl.provider.jna.invokers;

import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.Out;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.jna.JNATypeMapper;
import com.kenai.jaffl.provider.jna.marshallers.MarshalContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Base {@link Invoker} that most implementations will derive from.
 */
public abstract class BaseInvoker implements Invoker {
    protected static final String OPTION_INVOKING_METHOD = "invoking-method";
    protected static final com.sun.jna.TypeMapper jnaTypeMapper = new JNATypeMapper();
    protected final com.sun.jna.Function function;
    protected final Class returnType;
    protected final Map<String, Object> functionOptions;

    protected BaseInvoker(com.sun.jna.NativeLibrary library, Method method) {
        function = library.getFunction(method.getName());
        returnType = method.getReturnType();
        functionOptions = new IdentityHashMap<String, Object>(4);
        functionOptions.put(com.sun.jna.Library.OPTION_TYPE_MAPPER, jnaTypeMapper);
        functionOptions.put(OPTION_INVOKING_METHOD, method);
    }
    
    protected static final MarshalContext getContext(Method m, int i) {
        Annotation[] annotations = m.getParameterAnnotations()[i];
        boolean in = false;
        boolean out = false;
        for (int n = 0; n < annotations.length; ++n) {
            in = (annotations[n] instanceof In) ? true : in;
            out = (annotations[n] instanceof Out) ? true : out;
        }
        // If neither is set, assume param is both IN & OUT
        if (!in && !out) {
            in = out = true;
        }
        return new MarshalContext(in, out);
    }
}
