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
 * Indicates that the temporary native memory allocated for an {@code @Out} paramneter
 * should be cleared before passing to the native function.
 *
 * <p>By default, parameters that are annotated as {@code @Out} only do not clear
 * the data in the temporary native memory area allocated when a java heap object
 * is passed in as the parameter, so the memory passed to the native function is
 * full of garbage data.  After the native method returns, the native memory is
 * copied back to java, which is usually not a problem, since the native function
 * will have updated the memory with valid data.  However, if the native function
 * fails, the garbage data that was in the temporary native memory will be copied
 * back to java.
 *
 * @see In
 * @see Out
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Clear {

}
