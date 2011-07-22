package jnr.ffi.annotations;

import jnr.ffi.CallingConvention;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delegate {
    CallingConvention convention() default CallingConvention.DEFAULT;
}
