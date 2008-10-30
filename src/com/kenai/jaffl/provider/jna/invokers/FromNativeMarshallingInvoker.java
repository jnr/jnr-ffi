
package com.kenai.jaffl.provider.jna.invokers;

import com.kenai.jaffl.mapper.FromNativeContext;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.MethodResultContext;
import com.kenai.jaffl.mapper.TypeMapper;
import com.sun.jna.ToNativeConverter;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 *
 */
public class FromNativeMarshallingInvoker extends MarshallingInvoker {
    private final FromNativeConverter resultConverter;
    private final FromNativeContext resultContext;
    private final Class resultType;
    private final Map<String, Object> jnaOptions;
    
    public FromNativeMarshallingInvoker(com.sun.jna.NativeLibrary library, Method method, TypeMapper typeMapper) {
        super(library, method, typeMapper);
        resultConverter = typeMapper.getFromNativeConverter(method.getReturnType());
        resultContext = new MethodResultContext(method);
        resultType = method.getReturnType();
        jnaOptions = new IdentityHashMap<String, Object>();
        jnaOptions.put(com.sun.jna.Library.OPTION_TYPE_MAPPER, fromNativeMapper);
    }
    @SuppressWarnings("unchecked")
    public Object invoke(Object[] parameters) {
        return resultConverter.fromNative(invokeFunction(parameters, jnaOptions), resultContext);
    }
    com.sun.jna.TypeMapper fromNativeMapper = new com.sun.jna.TypeMapper() {

        public com.sun.jna.FromNativeConverter getFromNativeConverter(Class javaType) {
            if (javaType == resultType) {
                return jnaTypeMapper.getFromNativeConverter(resultConverter.nativeType());
            }
            return jnaTypeMapper.getFromNativeConverter(javaType);
        }

        public ToNativeConverter getToNativeConverter(Class javaType) {
            return jnaTypeMapper.getToNativeConverter(javaType);
        }
    };
}
