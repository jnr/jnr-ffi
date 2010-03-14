
package com.kenai.jaffl.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a {@link com.kenai.jaffl.struct.Struct}} parameter should be
 * backed by a persistent native memory block.
 * 
 * <p>Without the {@code @Direct} annotation, the native code will allocate a
 * temporary native memory block for the parameter, and free it immediately after
 * the call.
 *
 * <p>By using {@code @Direct}, the native memory block is permanently associated
 * with the {@link com.kenai.jaffl.struct.Struct} instance, and will remain allocated
 * for as long as the {@code Struct} instance remains strongly referenced by java code.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Direct {

}
