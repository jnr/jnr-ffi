
package com.kenai.jaffl.provider.jna.invokers;

import java.lang.reflect.Method;

/**
 * An {@link Invoker} subclass that passes its arguments direct to JNA
 */
public class SimpleInvoker extends BaseInvoker {

    public SimpleInvoker(com.sun.jna.NativeLibrary library, Method method) {
        super(library, method);
    }

    public Object invoke(Object[] parameters) {
        return function.invoke(returnType, parameters, functionOptions);
    }
}
