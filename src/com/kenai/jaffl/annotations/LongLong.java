

package com.kenai.jaffl.annotations;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a long parameter should be treated as native long-long (64bit)
 * instead of the platform-dependent long size.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER, ElementType.METHOD })
public @interface LongLong {

}
