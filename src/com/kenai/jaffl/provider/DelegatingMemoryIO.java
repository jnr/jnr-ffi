
package com.kenai.jaffl.provider;

import com.kenai.jaffl.MemoryIO;

public interface DelegatingMemoryIO {
    public MemoryIO getDelegatedMemoryIO();
}
