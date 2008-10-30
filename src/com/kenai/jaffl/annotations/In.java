
package com.kenai.jaffl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the parameter is an IN parameter.
 * <p>
 * If a parameter is tagged only as <tt>In</tt>, and a temporary native memory 
 * area needs to be allocated before passing the data to native memory, then
 * the java data is copied from java memory to native memory, but it is not 
 * copied back to java from the native memory area after the call.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface In {

}
