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

package jnr.ffi.provider.jffi;

import jnr.ffi.provider.InAccessibleMemoryIO;

/**
 *
 */
class NativeClosurePointer extends InAccessibleMemoryIO {
    private final com.kenai.jffi.Closure.Handle handle;
    final NativeClosureProxy proxy;


    public NativeClosurePointer(jnr.ffi.Runtime runtime, com.kenai.jffi.Closure.Handle handle, NativeClosureProxy proxy) {
        super(runtime, handle.getAddress(), true);
        this.handle = handle;
        this.proxy = proxy;
    }

    @Override
    public long size() {
        return 0;
    }
}
