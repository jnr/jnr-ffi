
package com.kenai.jaffl;

public interface Type {
    /**
     * The size in bytes of this type.
     *
     * @return An integer
     */
    int size();

    /**
     * The native alignment of this type, in bytes
     *
     * @return An integer
     */
    int alignment();
}
