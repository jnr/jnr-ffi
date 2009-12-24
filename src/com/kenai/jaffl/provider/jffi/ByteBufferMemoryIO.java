
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.AbstractBufferMemoryIO;
import java.nio.ByteBuffer;

public class ByteBufferMemoryIO extends AbstractBufferMemoryIO {

    public ByteBufferMemoryIO(ByteBuffer buffer) {
        super(buffer);
    }
    public MemoryIO getMemoryIO(long offset) {
        return MemoryUtil.newMemoryIO(getAddress(offset));
    }

    public MemoryIO getMemoryIO(long offset, long size) {
        return MemoryUtil.newMemoryIO(getAddress(offset), size);
    }

    public Pointer getPointer(long offset) {
        return MemoryUtil.newPointer(getAddress(offset));
    }

    public void putPointer(long offset, Pointer value) {
        putAddress(offset, value.address());
    }
}
