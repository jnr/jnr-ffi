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

import com.kenai.jffi.ObjectParameterStrategy;
import com.kenai.jffi.ObjectParameterType;

/**
 *
 */
abstract public class ParameterStrategy extends ObjectParameterStrategy {
    /* objectCount is accessed directly from asm code - do not change */
    public final int objectCount;

    protected ParameterStrategy(StrategyType type) {
        super(type);
        objectCount = type == HEAP ? 1 : 0;
    }

    protected ParameterStrategy(StrategyType type, ObjectParameterType parameterType) {
        super(type, parameterType);
        objectCount = type == HEAP ? 1 : 0;
    }
}
