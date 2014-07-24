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

import jnr.ffi.Library;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public interface FunctionMapper {
    public static interface Context {
        @Deprecated
        public abstract Library getLibrary();
        public abstract boolean isSymbolPresent(String name);
        public Collection<Annotation> getAnnotations();
    }

    /**
     * Translate the (Java) function name into its (native) equivalent. If the
     * name is not present in the map, it is to return the supplied name (same
     * object exactly).
     *
     * @param functionName to translate
     * @param context for translation
     * @return native equivalent or <code>functionName</code> if not in map
     */
    public String mapFunctionName(String functionName, Context context);

    public static final class Builder {
        private final Map<String, String> functionNameMap = Collections.synchronizedMap(new HashMap<String, String>());

        public Builder map(String javaName, String nativeFunction) {
            functionNameMap.put(javaName, nativeFunction);
            return this;
        }

        public FunctionMapper build() {
            return new SimpleFunctionMapper(functionNameMap);
        }
    }

    /**
     * An implementation of {@link jnr.ffi.mapper.FunctionMapper} that maps 1:1 between java symbols and native functions
     */
    public static final FunctionMapper IDENTITY = new FunctionMapper() {
        @Override
        public String mapFunctionName(String functionName, Context context) {
            return functionName;
        }
    };
}
