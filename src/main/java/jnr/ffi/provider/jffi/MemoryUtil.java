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

package jnr.ffi.provider.jffi;

import jnr.ffi.provider.BoundedMemoryIO;
import jnr.ffi.provider.NullMemoryIO;

public final class MemoryUtil {
    static final NullMemoryIO NULL = new NullMemoryIO(NativeRuntime.getInstance());

    private MemoryUtil() {}

    static jnr.ffi.Pointer newPointer(long ptr) {
        return ptr != 0 ? new DirectMemoryIO(NativeRuntime.getInstance(), ptr) : null;
    }

    static jnr.ffi.Pointer newPointer(int ptr) {
        return ptr != 0 ? new DirectMemoryIO(NativeRuntime.getInstance(), ptr) : null;
    }
    
    static jnr.ffi.Pointer newPointer(long ptr, long size) {
        return ptr != 0 ? new BoundedMemoryIO(new DirectMemoryIO(NativeRuntime.getInstance(), ptr), 0, size) : null;
    }
}
