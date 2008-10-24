
package com.kenai.jaffl.provider.jna.invokers;

import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.provider.jna.InvocationSession;
import com.kenai.jaffl.provider.jna.marshallers.ByRefMarshaller;
import com.kenai.jaffl.provider.jna.marshallers.MarshalContext;
import com.kenai.jaffl.provider.jna.marshallers.Marshaller;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Invoker} subclass that needs to custom marshal its arguments
 */
public class MarshallingInvoker extends BaseInvoker {

    private final Marshaller[] marshallers;
    private final int[] marshalIndexes;

    public MarshallingInvoker(com.sun.jna.NativeLibrary library, Method method) {
        super(library, method);
        Class[] types = method.getParameterTypes();
        List<Marshaller> m = new ArrayList<Marshaller>(types.length);
        List<Integer> indexes = new ArrayList<Integer>(types.length);
        for (int i = 0; i < types.length; ++i) {
            MarshalContext ctx = getContext(method, i);
            if (ByReference.class.isAssignableFrom(types[i])) {
                m.add(new ByRefMarshaller(ctx));
            } else {
                continue;
            }
            indexes.add(i);
        }
        marshallers = m.toArray(new Marshaller[m.size()]);
        marshalIndexes = new int[indexes.size()];
        for (int i = 0; i < marshalIndexes.length; ++i) {
            marshalIndexes[i] = indexes.get(i);
        }
    }

    public Object invoke(Object[] parameters) {
        InvocationSession session = new InvocationSession(marshallers.length);
        for (int i = 0; i < marshalIndexes.length; ++i) {
            parameters[marshalIndexes[i]] = marshallers[i].marshal(session, parameters[marshalIndexes[i]]);
        }
        Object retVal = function.invoke(returnType, parameters, functionOptions);
        session.finish();
        return retVal;
    }
}
