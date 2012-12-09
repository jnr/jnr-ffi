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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the errno valud for a native function need not be saved after
 * the function returns.
 *
 * <p>Due to the nature of the Java Virtual Machine, the errno value must be saved
 * immediately after the native function is called, otherwise internal jvm operations
 * may overwrite it before control is returned to java code.
 *
 * <p>Since it is not possible for jnr-ffi to infer in a generic way whether a native
 * function has succeeded or failed, the C errno value is saved after every native
 * function call - even for the ones that succeed.  This can have a significant
 * performance impact, so for those functions which either don't fail, or for which
 * the errno value can be ignored, can be annotated with {@code @IgnoreError} to
 * avoid unneccessary saving of the errno value.
 *
 * @see SaveError
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreError {

}
