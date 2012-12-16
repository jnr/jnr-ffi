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

import jnr.ffi.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static jnr.ffi.util.Annotations.sortedAnnotationCollection;

/**
 *
 */
public class MethodResultContext implements FromNativeContext {
    private final jnr.ffi.Runtime runtime;
    private final Method method;
    private Collection<Annotation> annotations;

    public MethodResultContext(jnr.ffi.Runtime runtime, Method method) {
        this.runtime = runtime;
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations != null ? annotations : (annotations = sortedAnnotationCollection(method.getAnnotations()));
    }

    public jnr.ffi.Runtime getRuntime() {
        return runtime;
    }
}
