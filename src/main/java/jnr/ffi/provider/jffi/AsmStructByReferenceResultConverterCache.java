package jnr.ffi.provider.jffi;

import jnr.ffi.*;
import jnr.ffi.mapper.FromNativeConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsmStructByReferenceResultConverterCache {
    private final Map<Class<? extends Struct>, FromNativeConverter<? extends Struct, Pointer>> converters
            = new ConcurrentHashMap<Class<? extends Struct>, FromNativeConverter<? extends Struct, Pointer>>();
    
    private final AsmClassLoader classLoader;

    public AsmStructByReferenceResultConverterCache(AsmClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public final FromNativeConverter<? extends Struct, Pointer> get(jnr.ffi.Runtime runtime, 
                                                                    Class<? extends Struct> structClass,
                                                                    int flags) {
        FromNativeConverter<? extends Struct, Pointer> converter = converters.get(structClass);
        if (converter == null) {
            synchronized (converters) {
                if ((converter = converters.get(structClass)) == null) {
                    converters.put(structClass, converter = AsmStructByReferenceFromNativeConverter.newStructByReferenceConverter(runtime, structClass, 0, classLoader));
                }
            }
        }

        return converter;
    }
}
