
package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.*;
import com.kenai.jaffl.provider.BoundedMemoryIO;
import com.kenai.jaffl.provider.AbstractBufferMemoryIO;
import com.kenai.jaffl.provider.NullMemoryIO;
import com.kenai.jaffl.provider.ShareMemoryIO;
import java.nio.ByteBuffer;

/**
 *
 */
public final class MemoryUtil {
    public static final MemoryIO slice(MemoryIO io, long offset) {
        return new ShareMemoryIO(io, offset);
    }
    public static final MemoryIO slice(MemoryIO io, long offset, long size) {
        return new BoundedMemoryIO(io, offset, size);
    }
    public static final MemoryIO wrap(ByteBuffer buffer) {
        return new BufferMemoryIO(buffer);
    }
    public static final MemoryIO wrap(byte[] array, int offset, int size) {
        return new BufferMemoryIO(ByteBuffer.wrap(array, offset, size));
    }
    /*
    public static final MemoryIO wrap(Address address, long size) {
        return wrap(address.nativeAddress(), size);
    }
    public static final MemoryIO wrap(long address, long size) {
        return address == 0 ? getNullIO() : new BoundedNativeIO(address, size);
    }
    public static final MemoryIO wrap(Address address) {
        return wrap(address.nativeAddress());
    }
    public static final MemoryIO wrap(long address) {
        return address == 0 ? getNullIO() : new NativeIO(address);
    }
    */
    public static final MemoryIO getNullIO() {
        return NullMemoryIO.INSTANCE;
    }
}
