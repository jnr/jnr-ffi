/*
 * Copyright (C) 2010 Wayne Meissner
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

package jnr.ffi;

import java.nio.ByteBuffer;


/**
 * A utility for allocating memory that can be passed to native functions.
 */
public final class Memory {

    private Memory() {
    }
    
    /**
     * Allocates a new block of java memory and wraps it in a {@link Pointer}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return a {@code Pointer} instance that can access the memory.
     */
    public static final Pointer allocate(Runtime runtime, int size) {
        return runtime.getMemoryManager().allocate(size);
    }

    /**
     * Allocates a new block of native memory and wraps it in a {@link Pointer}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     *
     * @return a {@code Pointer} instance that can access the memory.
     */
    public static final Pointer allocateDirect(Runtime runtime, int size) {
        return runtime.getMemoryManager().allocateDirect(size);
    }

    /**
     * Allocates a new block of native memory and wraps it in a {@link Pointer}
     * accessor.
     *
     * @param size The size in bytes of memory to allocate.
     * @param clear Whether the memory contents should be cleared, or left as
     * random data.
     *
     * @return a {@code Pointer} instance that can access the memory.
     */
    public static final Pointer allocateDirect(Runtime runtime, int size, boolean clear) {
        return runtime.getMemoryManager().allocateDirect(size, clear);
    }
}
