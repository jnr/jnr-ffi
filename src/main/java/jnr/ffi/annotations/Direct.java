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
 * Indicates that a {@link jnr.ffi.Struct}} parameter should be
 * backed by a persistent native memory block.
 * 
 * <p>Without the {@code @Direct} annotation, the native code will allocate a
 * temporary native memory block for the parameter, and free it immediately after
 * the call.
 *
 * <p>By using {@code @Direct}, the native memory block is permanently associated
 * with the {@link jnr.ffi.Struct} instance, and will remain allocated
 * for as long as the {@code Struct} instance remains strongly referenced by java code.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
public @interface Direct {

}
