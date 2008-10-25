
package com.kenai.jaffl.provider.jna.marshallers;

import com.kenai.jaffl.mapper.ToNativeContext;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.provider.jna.InvocationSession;

/**
 * Uses a ToNativeConverter to convert the java argument to a native type
 */
public class ToNativeConverterMarshaller implements Marshaller {
    private final ToNativeConverter toNative;
    private final ToNativeContext toNativeContext;
    public ToNativeConverterMarshaller(ToNativeConverter toNative, ToNativeContext toNativeContext) {
        this.toNative = toNative;
        this.toNativeContext = toNativeContext;
    }
    @SuppressWarnings("unchecked")
    public Object marshal(InvocationSession session, Object value) {
        return toNative.toNative(value, toNativeContext);
    }
}
