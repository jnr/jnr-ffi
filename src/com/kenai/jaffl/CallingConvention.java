
package com.kenai.jaffl;

/**
 * Native function calling conventions.
 *
 * <p>This is only needed on windows platforms - all platforms assume
 * {@link #DEFAULT} as the calling convention.
 */
public enum CallingConvention {
    /**
     * The default C calling convention
     */
    DEFAULT,
    /**
     * Windows stdcall calling convention
     */
    STDCALL;
}
