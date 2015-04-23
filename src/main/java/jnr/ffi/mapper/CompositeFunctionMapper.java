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

package jnr.ffi.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public final class CompositeFunctionMapper implements FunctionMapper {
    private final Collection<FunctionMapper> functionMappers;

    public CompositeFunctionMapper(Collection<FunctionMapper> functionMappers) {
        this.functionMappers = Collections.unmodifiableList(new ArrayList<FunctionMapper>(functionMappers));
    }

    @Override
    public String mapFunctionName(String functionName, Context context) {
        for (FunctionMapper functionMapper : functionMappers) {
            String mappedName = functionMapper.mapFunctionName(functionName, context);
            if (mappedName != functionName) {
                // A translation was explicit in this mapper.
                return mappedName;
            }
        }
        return functionName;
    }
}
