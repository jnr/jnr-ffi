
package com.kenai.jaffl.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the parameter is transient.
 * 
 * This means it can be backed by a temporarily allocated native memory block,
 * and after the method call, the native memory can be freed again.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Transient {

}
