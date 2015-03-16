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

package jnr.ffi.mapper;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * Context for conversion from native to Java.
 */
public interface FromNativeContext {
    /**
     * Gets a sorted list of annotations
     *
     * @return a sorted list of annotations for this native type
     */
    public abstract Collection<Annotation> getAnnotations();

    /**
     * Gets the <tt>Runtime</tt> used for the conversion.
     *
     * @return The runtime used for the conversion.
     */
    public jnr.ffi.Runtime getRuntime();
}
