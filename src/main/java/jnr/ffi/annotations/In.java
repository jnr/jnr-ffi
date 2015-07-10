/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the parameter is an IN parameter.
 *
 * <p>When a java object is passed to a native function as a pointer
 * (for example {@link jnr.ffi.Pointer}, {@link jnr.ffi.Struct}, {@link java.nio.ByteBuffer}),
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
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
public @interface In {

}
