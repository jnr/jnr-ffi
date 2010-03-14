
package com.kenai.jaffl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the parameter is an IN parameter.
 *
 * <p>When a java object is passed to a native function as a pointer
 * (for example {@link com.kenai.jaffl.Pointer}, {@link com.kenai.jaffl.struct.Struct}, {@link java.nio.ByteBuffer}),
 * then a temporary native memory block is allocated, the java data is copied to
 * the temporary memory and the address of the temporary memory is passed to the function.
 * After the function returns, the java data is automatically updated from the
 * contents of the native memory.
 *
 * <p>As this extra copying can be expensive, parameters can be annotated with {@code @In}
 * so the data is only copied from java {@code IN} to native memory, but not copied
 * back {@code OUT} from native memory to java memory.
 *
 * <p>Parameters with neither a {@code @In} nor a {@code @Out} annotation will copy both ways.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface In {

}
