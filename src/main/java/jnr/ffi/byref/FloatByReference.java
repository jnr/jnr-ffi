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
 *
 */
public final class FloatByReference extends AbstractNumberReference<Float> {
    private static final Float DEFAULT = Float.valueOf(0f);

    /**
     * Creates a new reference to a short value initialized to zero.
     */
    public FloatByReference() {
        super(DEFAULT);
    }

    /**
     * Creates a new reference to a float value
     * 
     * @param value the initial native value
     */
    public FloatByReference(Float value) {
        super(checkNull(value));
    }

    /**
     * Creates a new reference to a float value
     *
     * @param value the initial native value
     */
    public FloatByReference(float value) {
        super(value);
    }
    
    /**
     * Copies the float value to native memory
     *
     * @param runtime the current runtime.
     * @param buffer  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void toNative(Runtime runtime, Pointer buffer, long offset) {
        buffer.putFloat(offset, value);
    }

    /**
     * Copies the float value from native memory
     *
     * @param runtime the current runtime.
     * @param buffer  the native memory buffer.
     * @param offset  the memory offset.
     */
    public void fromNative(Runtime runtime, Pointer buffer, long offset) {
        this.value = buffer.getFloat(offset);
    }
    
    /**
     * Gets the native size of type of reference in bytes.
     * 
     * @param  runtime the current runtime.
     * @return the size of a byte in bytes
     */
    public final int nativeSize(Runtime runtime) {
        return 4;
    }
}
