
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;

/**
 * An instance of {@link TypeMapper} which always returns null
 */
public class NullTypeMapper implements TypeMapper {
    public static final TypeMapper INSTANCE = new NullTypeMapper();
    
    public FromNativeConverter getFromNativeConverter(Class type) {
        return null;
    }

    public ToNativeConverter getToNativeConverter(Class type) {
        return null;
    }

}
