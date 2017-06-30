package jnr.ffi.annotations;

import java.lang.annotation.*;

/**
 * Use if you want to have different names for JNR interface method and native library method.
 * <b/>
 * For mapping to work, use {@link jnr.ffi.mapper.NativeNameFunctionMapper}
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NativeName {
    String value();
}
