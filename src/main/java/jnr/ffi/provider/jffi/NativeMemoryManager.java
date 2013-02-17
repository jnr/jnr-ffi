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
import jnr.ffi.provider.BoundedMemoryIO;
import jnr.ffi.provider.IntPointer;

import java.nio.ByteBuffer;

public class NativeMemoryManager implements jnr.ffi.provider.MemoryManager {
    private final jnr.ffi.Runtime runtime;
    private final long addressMask;

    public NativeMemoryManager(NativeRuntime runtime) {
        this.runtime = runtime;
        this.addressMask = runtime.addressMask();
    }
    
    public Pointer allocate(int size) {
        return new ArrayMemoryIO(runtime, size);
    }

    public Pointer allocateDirect(int size) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(runtime, size, 8, true), 0, size);
    }

    public Pointer allocateDirect(int size, boolean clear) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(runtime, size, 8, clear), 0, size);
    }

    public Pointer allocateTemporary(int size) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(runtime, size, 8, true), 0, size);
    }

    public Pointer allocateTemporary(int size, boolean clear) {
        return new BoundedMemoryIO(TransientNativeMemory.allocate(runtime, size, 8, clear), 0, size);
    }

    public Pointer newPointer(ByteBuffer buffer) {
        return new ByteBufferMemoryIO(runtime, buffer);
    }

    public Pointer newPointer(long address) {
        return new DirectMemoryIO(runtime, address & addressMask);
    }

    public Pointer newPointer(long address, long size) {
        return new BoundedMemoryIO(new DirectMemoryIO(runtime, address & addressMask), 0, size);
    }

    public Pointer newOpaquePointer(long address) {
        return new IntPointer(runtime, address);
    }

}
