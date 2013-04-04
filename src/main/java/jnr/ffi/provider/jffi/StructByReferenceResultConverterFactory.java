package jnr.ffi.provider.jffi;

import jnr.ffi.*;
import jnr.ffi.mapper.FromNativeContext;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.provider.converters.StructByReferenceFromNativeConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class StructByReferenceResultConverterFactory {
    private final Map<Class<? extends Struct>, FromNativeConverter<? extends Struct, Pointer>> converters
            = new ConcurrentHashMap<Class<? extends Struct>, FromNativeConverter<? extends Struct, Pointer>>();
    
    private final AsmClassLoader classLoader;
    private final boolean asmEnabled;

    public StructByReferenceResultConverterFactory(AsmClassLoader classLoader, boolean asmEnabled) {
        this.classLoader = classLoader;
        this.asmEnabled = asmEnabled;
    }

    public final FromNativeConverter<? extends Struct, Pointer> get(Class<? extends Struct> structClass,
                                                                    FromNativeContext fromNativeContext) {
        FromNativeConverter<? extends Struct, Pointer> converter = converters.get(structClass);
        if (converter == null) {
            synchronized (converters) {
                if ((converter = converters.get(structClass)) == null) {
                    converters.put(structClass, converter = createConverter(fromNativeContext.getRuntime(), structClass, fromNativeContext));
                }
            }
        }

        return converter;
    }
    
    private FromNativeConverter<? extends Struct, Pointer> createConverter(jnr.ffi.Runtime runtime,
                                                                           Class<? extends Struct> structClass,
                                                                           FromNativeContext fromNativeContext) {
        return asmEnabled
            ? AsmStructByReferenceFromNativeConverter.newStructByReferenceConverter(runtime, structClass, 0, classLoader)
            : StructByReferenceFromNativeConverter.getInstance(structClass, fromNativeContext);
    }
}
