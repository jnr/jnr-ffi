
package com.kenai.jaffl.provider.jna.invokers;

import com.kenai.jaffl.mapper.TypeMapper;
import com.sun.jna.NativeLibrary;
import java.lang.reflect.Method;

/**
 *
 */
public class DefaultMarshallingInvoker extends MarshallingInvoker {

    public DefaultMarshallingInvoker(NativeLibrary library, Method method, TypeMapper typeMapper) {
        super(library, method, typeMapper);
    }
    public Object invoke(Object[] parameters) {
        return invokeFunction(parameters, functionOptions);
    }

}
