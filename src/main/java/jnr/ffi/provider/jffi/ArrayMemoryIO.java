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

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.AbstractArrayMemoryIO;


public final class ArrayMemoryIO extends AbstractArrayMemoryIO {

    public ArrayMemoryIO(Runtime runtime, int size) {
        super(runtime, size);
    }

    public ArrayMemoryIO(Runtime runtime, byte[] bytes, int off, int len) {
        super(runtime, bytes, off, len);
    }

    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(getRuntime(), getAddress(offset));
    }

    @Override
    public Pointer getPointer(long offset, long size) {
        return MemoryUtil.newPointer(getRuntime(), getAddress(offset), size);
    }

    @Override
    public void putPointer(long offset, Pointer value) {
        putAddress(offset, value != null ? value.address() : 0L);
    }
}
