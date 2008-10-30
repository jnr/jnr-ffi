
package com.kenai.jaffl.byref;

import java.nio.ByteBuffer;

public interface ByReference<T> {
    /**
     * Gets the size of the native buffer required to store the value
     * 
     * @return the size in bytes of the native type
     */
    int nativeSize();
    /**
     * Copies the java value to native memory
     * 
     * @param buffer the native memory buffer.
     */
    void marshal(ByteBuffer buffer);
    
    /**
     * Copies the java value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    void unmarshal(ByteBuffer buffer);
    
    T getValue();
}
