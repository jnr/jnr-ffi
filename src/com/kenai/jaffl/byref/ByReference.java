
package com.kenai.jaffl.byref;

import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.Runtime;

/**
 * A ByReference subclass is used when a primitive parameter must be passed
 * by-reference.
 *
 * <p>For example, the following C code,
 * <p><blockquote><pre>
 * {@code
 * extern void get_a(int * ap);
 *
 * int foo(void) {
 *     int a;
 *     // pass a reference to 'a' so get_a() can fill it out
 *     get_a(&a);
 *
 *     return a;
 * }
 * }
 * </pre></blockquote>
 * <p>Would be declared in java as
 * <p><blockquote><pre>
 * {@code
 * interface Lib {
 *     void get_a(@Out IntByReference ap);
 * }
 * }
 * </pre></blockquote>
 * <p>and used like this
 * <p><blockquote><pre>
 * IntByReference ap = new IntByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.getValue());
 * </pre></blockquote>
 * @param <T>
 */
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
     * @param memory the native memory buffer.
     */
    void marshal(Pointer memory, long offset);
    
    /**
     * Copies the java value from native memory
     * 
     * @param memory the native memory buffer.
     */
    void unmarshal(Pointer memory, long offset);
    
    T getValue();
}
