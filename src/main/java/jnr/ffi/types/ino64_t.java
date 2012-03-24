  
package jnr.ffi.types;

import jnr.ffi.TypeAlias;
import jnr.ffi.annotations.TypeDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER, ElementType.METHOD })
@TypeDefinition(alias = TypeAlias.ino64_t)
public @interface ino64_t {

}
