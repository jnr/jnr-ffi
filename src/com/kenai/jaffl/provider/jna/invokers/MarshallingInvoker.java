
package com.kenai.jaffl.provider.jna.invokers;

import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.MethodParameterContext;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.jna.InvocationSession;
import com.kenai.jaffl.provider.jna.marshallers.ByRefMarshaller;
import com.kenai.jaffl.provider.jna.marshallers.CharSequenceMarshaller;
import com.kenai.jaffl.provider.jna.marshallers.MarshalContext;
import com.kenai.jaffl.provider.jna.marshallers.Marshaller;
import com.kenai.jaffl.provider.jna.marshallers.StringBufferMarshaller;
import com.kenai.jaffl.provider.jna.marshallers.StringBuilderMarshaller;
import com.kenai.jaffl.provider.jna.marshallers.ToNativeConverterMarshaller;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An {@link Invoker} subclass that needs to custom marshal its arguments
 */
public abstract class MarshallingInvoker extends BaseInvoker {

    private final Marshaller[] marshallers;
    private final int[] marshalIndexes;

    public MarshallingInvoker(com.sun.jna.NativeLibrary library, Method method, TypeMapper typeMapper) {
        super(library, method);
        Class[] types = method.getParameterTypes();
        List<Marshaller> m = new ArrayList<Marshaller>(types.length);
        List<Integer> indexes = new ArrayList<Integer>(types.length);
        for (int i = 0; i < types.length; ++i) {
            MarshalContext ctx = getContext(method, i);
            ToNativeConverter toNative = typeMapper.getToNativeConverter(types[i]);
            if (toNative != null) {
                m.add(new ToNativeConverterMarshaller(toNative,
                        new MethodParameterContext(method, i)));
            } else if (ByReference.class.isAssignableFrom(types[i])) {
                m.add(new ByRefMarshaller(ctx));
            } else if (StringBuffer.class.isAssignableFrom(types[i])) {
                m.add(new StringBufferMarshaller(ctx));
            } else if (StringBuilder.class.isAssignableFrom(types[i])) {
                m.add(new StringBuilderMarshaller(ctx));
            } else if (CharSequence.class.isAssignableFrom(types[i])) {
                m.add(new CharSequenceMarshaller(ctx));

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
    protected final Object invokeFunction(Object[] parameters, Map<String, Object> options) {
        InvocationSession session = new InvocationSession(marshallers.length);
        for (int i = 0; i < marshalIndexes.length; ++i) {
            parameters[marshalIndexes[i]] = marshallers[i].marshal(session, parameters[marshalIndexes[i]]);
        }
        Object retVal = function.invoke(returnType, parameters, options);
        session.finish();
        return retVal;
    }
}
