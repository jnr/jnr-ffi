
package com.kenai.jaffl.util;

import java.nio.ByteBuffer;
import java.util.List;

public interface BufferPool {
    public ByteBuffer get(int size);
    public void put(ByteBuffer buffer);
    public void putAll(List<ByteBuffer> list);
}
