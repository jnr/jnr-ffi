
package com.kenai.jaffl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the parameter is an OUT parameter.
 * 
 * <p>
 * If a parameter is tagged only as <tt>Out</tt>, and a temporary native memory 
 * area needs to be allocated before passing the data to native memory, then
 * the java data is not copied to native memory before the call, but is copied
 * back to java from the native memory area after the call.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Out {

}
