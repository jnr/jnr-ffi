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

/**
 *
 */
public final class NullObjectParameterStrategy extends ParameterStrategy {
    public static final ParameterStrategy NULL = new NullObjectParameterStrategy();

    public NullObjectParameterStrategy() {
        super(DIRECT);
    }

    @Override
    public long address(Object parameter) {
        return 0;
    }

    @Override
    public Object object(Object parameter) {
        throw new NullPointerException("null reference");
    }

    @Override
    public int offset(Object parameter) {
        throw new NullPointerException("null reference");
    }

    @Override
    public int length(Object parameter) {
        throw new NullPointerException("null reference");
    }
}
