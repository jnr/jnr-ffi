
package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.mapper.FromNativeConverter;
import com.kenai.jaffl.mapper.ToNativeConverter;
import com.kenai.jaffl.mapper.TypeMapper;
import com.kenai.jaffl.provider.Invoker;
import com.kenai.jaffl.provider.jna.invokers.DefaultMarshallingInvoker;
import com.kenai.jaffl.provider.jna.invokers.FromNativeMarshallingInvoker;
import com.kenai.jaffl.provider.jna.invokers.SimpleInvoker;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
final class JNALibrary extends com.kenai.jaffl.provider.Library {
    private static final Map<String, WeakReference<com.sun.jna.NativeLibrary>> nativeLibraries
            = new ConcurrentHashMap<String, WeakReference<com.sun.jna.NativeLibrary>>();
    private final String libraryName;
    JNALibrary(String libraryName) {
        this.libraryName = libraryName;
    }
    public Invoker getInvoker(Method method, Map<LibraryOption, ?> options) {
        TypeMapper typeMapper = (TypeMapper) options.get(LibraryOption.TypeMapper);
        if (typeMapper == null) {
            typeMapper = emptyTypeMapper;
        }
        boolean needParameterConversion = false;
        boolean needResultConversion = false;
        for (Class c : method.getParameterTypes()) {
            if (typeMapper.getToNativeConverter(c) != null) {
                needParameterConversion = true;
            } else if (ByReference.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            } else if (StringBuffer.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            } else if (StringBuilder.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            } else if (CharSequence.class.isAssignableFrom(c)) {
                needParameterConversion = true;
            }
        }
        if (typeMapper.getFromNativeConverter(method.getReturnType()) != null) {
            needResultConversion = true;
        }
        com.sun.jna.NativeLibrary lib = getNativeLibrary(libraryName);
        
        if (!needParameterConversion && !needResultConversion) {
            return new SimpleInvoker(lib, method);
        } else if (needParameterConversion && !needResultConversion) {
            return new DefaultMarshallingInvoker(lib, method, typeMapper);
        } else {
            return new FromNativeMarshallingInvoker(lib, method, typeMapper);
        }
    }

    public Object libraryLock() {
        return com.sun.jna.NativeLibrary.getInstance(libraryName);
    }
    private static final com.sun.jna.NativeLibrary getNativeLibrary(final String libraryName) {
        WeakReference<com.sun.jna.NativeLibrary> ref = nativeLibraries.get(libraryName);
        com.sun.jna.NativeLibrary lib = ref != null ? ref.get() : null;
        if (lib == null) {
            try {
                lib = com.sun.jna.NativeLibrary.getInstance(locateLibrary(libraryName));
            } catch (UnsatisfiedLinkError ex) {
                lib = com.sun.jna.NativeLibrary.getInstance(libraryName);
            }
            nativeLibraries.put(libraryName, new WeakReference<com.sun.jna.NativeLibrary>(lib));
        }
        return lib;
    }
    private static final TypeMapper emptyTypeMapper = new TypeMapper() {

        public FromNativeConverter getFromNativeConverter(Class type) {
            return null;
        }

        public ToNativeConverter getToNativeConverter(Class type) {
            return null;
        }
    };
}
