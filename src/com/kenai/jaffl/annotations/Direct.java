
package com.kenai.jaffl.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the parameter requires native memory.
 * 
 * This means it can be backed by allocated native memory, that is freed only
 * after the Struct instance goes out of scope.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Direct {

}
