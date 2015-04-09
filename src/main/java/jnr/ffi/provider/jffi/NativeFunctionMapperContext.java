/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi.provider.jffi;

import jnr.ffi.Library;
import jnr.ffi.mapper.FunctionMapper;

import java.lang.annotation.Annotation;
import java.util.Collection;

public final class NativeFunctionMapperContext implements FunctionMapper.Context {
    private final NativeLibrary library;

    private final Collection<Annotation> annotations;

    public NativeFunctionMapperContext(NativeLibrary library, Collection<Annotation> annotations) {
        this.library = library;
        this.annotations = annotations;
    }

    public Library getLibrary() {
        return null;
    }

    public boolean isSymbolPresent(String name) {
        return library.getSymbolAddress(name) != 0L;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }
}
