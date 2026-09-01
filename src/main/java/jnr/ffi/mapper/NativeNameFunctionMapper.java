package jnr.ffi.mapper;

import jnr.ffi.annotations.NativeName;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Uses {@link NativeName} annotation to map JNR interface method and native library method with different names.
 * Example:
 * <pre>
 * {@code
 * LibC libc = LibraryLoader.create(LibC.class).mapper(new NativeNameFunctionMapper()).load("lib");
 * }
 * </pre>
 */
public class NativeNameFunctionMapper implements FunctionMapper {
    @Override
    public String mapFunctionName(String functionName, Context context) {
        Collection<Annotation> annotations = context.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof NativeName) {
                return ((NativeName) annotation).value();
            }
        }
        return functionName;
    }
}
