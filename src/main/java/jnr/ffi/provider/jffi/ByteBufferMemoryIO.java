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

import com.kenai.jffi.MemoryIO;
import jnr.ffi.Pointer;
import jnr.ffi.provider.AbstractBufferMemoryIO;

import java.nio.ByteBuffer;

public class ByteBufferMemoryIO extends AbstractBufferMemoryIO {

    public ByteBufferMemoryIO(jnr.ffi.Runtime runtime, ByteBuffer buffer) {
        super(runtime, buffer, address(buffer));
    }

    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(getRuntime(), getAddress(offset));
    }

    public Pointer getPointer(long offset, long size) {
        return MemoryUtil.newPointer(getRuntime(), getAddress(offset), size);
    }
    
    public void putPointer(long offset, Pointer value) {
        putAddress(offset, value != null ? value.address() : 0L);
    }

    private static long address(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            long address = MemoryIO.getInstance().getDirectBufferAddress(buffer);
            return address != 0L ? address + buffer.position() : 0L;
        }

        return 0L;
    }
}
