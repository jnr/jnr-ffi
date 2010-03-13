
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.AbstractBufferMemoryIO;
import java.nio.ByteBuffer;

public class ByteBufferMemoryIO extends AbstractBufferMemoryIO {

    public ByteBufferMemoryIO(ByteBuffer buffer) {
        super(NativeRuntime.getInstance(), buffer);
    }

    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(getAddress(offset));
    }

    public Pointer getPointer(long offset, long size) {
        return MemoryUtil.newPointer(getAddress(offset), size);
    }
    
    public void putPointer(long offset, Pointer value) {
        putAddress(offset, value.address());
    }
}
