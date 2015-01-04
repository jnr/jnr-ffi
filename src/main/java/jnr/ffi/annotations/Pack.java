/*
 * Copyright (C) 2011 Wayne Meissner
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

import jnr.ffi.annotations.PackEnabler;

/**
 * Explicitly specifies the packing for structure fields.
 *
 * Using this annation means that each field in the structure will be
 * padded by at most {@code padding} bytes, rather than using the default
 * compiler packing strategy. This is analogous to the {@code pragma pack}
 * C compiler pragma. It should be used with great caution.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface Pack {
    public int padding();
    public Class<? extends PackEnabler> enabler() default PackEnabler.class;
}
