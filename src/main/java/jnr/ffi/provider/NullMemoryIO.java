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

package jnr.ffi.provider;

import jnr.ffi.Runtime;

public final class NullMemoryIO extends InAccessibleMemoryIO {
    private static final String msg = "attempted access to a NULL memory address";

    public NullMemoryIO(Runtime runtime) {
        super(runtime, 0, true);
    }

    protected final NullPointerException error() {
        return new NullPointerException(msg);
    }

    public long size() {
        return Long.MAX_VALUE;
    }
}
