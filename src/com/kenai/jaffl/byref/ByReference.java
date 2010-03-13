
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

public interface ByReference<T> {
    /**
     * Gets the size of the native buffer required to store the value
     * 
     * @return the size in bytes of the native type
     */
    int nativeSize(Runtime runtime);

    /**
     * Copies the java value to native memory
     * 
     * @param buffer the native memory buffer.
     */
    void marshal(Pointer memory, long offset);
    
    /**
     * Copies the java value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    void unmarshal(Pointer memory, long offset);
    
    T getValue();
}
