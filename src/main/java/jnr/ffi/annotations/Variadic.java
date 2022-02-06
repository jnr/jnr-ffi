package jnr.ffi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies that a non-varargs function binding will call a variadic C function with the specified number of fixed
 * arguments.
 *
 * Platforms that pass variadic arguments differently than fixed arguments will need this annotation if the Java binding
 * does not itself use varargs. Without Java varargs or this annotation, there's no way for jnr-ffi to know where fixed
 * args end and variadic arguments begin, causing them all to be passed the way fixed arguments get passed on the given
 * platform.
 *
 * See https://github.com/jnr/jnr-ffi/pull/292
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Variadic {
    int fixedCount();
}
