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

import jnr.ffi.Runtime;

import java.util.concurrent.atomic.AtomicBoolean;

class AllocatedDirectMemoryIO extends DirectMemoryIO {
    private final AtomicBoolean allocated = new AtomicBoolean(true);
    private final int size;
    
    public AllocatedDirectMemoryIO(Runtime runtime, int size, boolean clear) {
        super(runtime, IO.allocateMemory(size, clear));
        this.size = size;
        if (address() == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes");
        }
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AllocatedDirectMemoryIO) {
            AllocatedDirectMemoryIO mem = (AllocatedDirectMemoryIO) obj;
            return mem.size == size && mem.address() == address();
        }
        
        return super.equals(obj);
    }

    public final void dispose() {
        if (allocated.getAndSet(false)) {
            IO.freeMemory(address());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (allocated.getAndSet(false)) {
                IO.freeMemory(address());
            }
        } finally {
            super.finalize();
        }
    }
}
