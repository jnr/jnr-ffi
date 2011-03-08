/*
 * Copyright (C) 2010 Wayne Meissner
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

/**
 * An abstract class for common PrimitiveReference functionality
 */
abstract public class AbstractNumberReference<T extends Number> extends Number implements ByReference<T> {
    T value;
    
    protected AbstractNumberReference(T value) {
        this.value = value;
    }

    protected static <T extends Number> T checkNull(T value) {
        if (value == null) {
            throw new NullPointerException("reference value cannot be null");
        }

        return value;
    }

    /**
     * Gets the current value the reference points to.
     *
     * @return the current value.
     */
    public T getValue() {
        return value;
    }
    
    @Override
    public final byte byteValue() {
        return value.byteValue();
    }
    
    @Override
    public final short shortValue() {
        return value.byteValue();
    }

    public final int intValue() {
        return value.intValue();
    }

    @Override
    public final long longValue() {
        return value.longValue();
    }

    @Override
    public final float floatValue() {
        return value.floatValue();
    }

    @Override
    public final double doubleValue() {
        return value.doubleValue();
    }
}
