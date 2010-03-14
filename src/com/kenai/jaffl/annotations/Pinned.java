
package com.kenai.jaffl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as being pinnable.
 * <p>
 * This means the data for the parameter is not copied to/from native memory.
 * Instead, the JVM memory is locked and passed directly to the native code.
 * </p>
 * <p>
 * <b>IMPORTANT:</b> This should not be used for functions that may block on 
 * network or filesystem access such as read(2), write(2), stat(2).
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Pinned {
}
