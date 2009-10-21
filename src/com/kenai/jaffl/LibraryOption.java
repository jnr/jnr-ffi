
package com.kenai.jaffl;

/**
 * Options that apply to a library
 */
public enum LibraryOption {
    /**
     * Function calls should save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link com.googlecode.jffi.annotations.IgnoreError} annotation.
     */
    SaveError,
    /**
     * Function calls should NOT save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link com.googlecode.jffi.annotations.SaveError} annotation.
     */
    IgnoreError,
    
    /**
     * A type mapper which maps java types to native types is present.
     */
    TypeMapper,

    /**
     * A function mapper which maps from java function names to native function names.
     */
    FunctionMapper,
    
    /**
     * The type of calling convention.
     * @see CallingConvention
     */
    CallingConvention,

    /**
     * Load the library into memory immediately, instead of lazily loading it
     */
    LoadNow;
}
