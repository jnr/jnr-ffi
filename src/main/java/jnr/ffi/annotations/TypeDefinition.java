package jnr.ffi.annotations;

import jnr.ffi.TypeAlias;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used internally by jnr-ffi to define type aliases.  e.g. ssize_t => long
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.ANNOTATION_TYPE })
public @interface TypeDefinition {
    TypeAlias alias();
}
