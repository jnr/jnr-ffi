/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.byref;


import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

/**
 * LongLongByReference is used when the address of a native long long value must be passed
 * as a parameter to a function.
 *
 * <p>
 * For example, the following C code,
 * <pre>
 * {@code
 * extern void get_a(long long * ap);
 *
 * long long foo(void) {
 *     long long a;
 *     // pass a reference to 'a' so get_a() can fill it out
 *     get_a(&a);
 *
 *     return a;
 * }
 * }
 * </pre>
 * <p>
 * Would be declared in java as
 * <pre>
 * {@code
 * interface Lib {
 *     void get_a(@Out LongLongByReference ap);
 * }
 * }
 * </pre>
 * <p>
 * and used like this
 * <pre>
 * LongLongByReference ap = new LongLongByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.longValue());
 * </pre>
 */
public final class LongLongByReference extends AbstractNumberReference<Long> {
    
    private long value;
    
    /**
     * Creates a new reference to a long long value initialized to zero.
     */
    public LongLongByReference() {
    }
    
    /**
     * Creates a new reference to a native longlong value
     *
     * @param value the initial native value
     */
    public LongLongByReference(long value) {
        super();
        this.value = value;
    }
    
    /**
     * Copies the value to native memory
     *
     * @param runtime the current runtime.
     * @param memory  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void toNative(Runtime runtime, Pointer memory, long offset) {
        memory.putLongLong(offset, value);
    }

    /**
     * Copies the value from native memory
     *
     * @param runtime the current runtime.
     * @param memory  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void fromNative(Runtime runtime, Pointer memory, long offset) {
        this.value = memory.getLongLong(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @param runtime the current runtime.
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return 8;
    }
    
    @Override
    public Long getValue() {
        return Long.valueOf(value);
    }
    
    @Override
    public byte byteValue() {
        return (byte)value;
    }

    @Override
    public short shortValue() {
        return (short)value;
    }

    @Override
    public int intValue() {
        return (int)value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return (float)value;
    }

    @Override
    public double doubleValue() {
        return (double)value;
    }
}
