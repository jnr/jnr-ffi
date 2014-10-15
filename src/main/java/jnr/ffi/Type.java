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

package jnr.ffi;

/**
 * Type is the superclass for all internal types used by jnr-ffi.
 *
 * <p>
 *     Use this type to access meta-data about a native type, such as its size or natural alignment.
 * <p>
 *     To obtain an instance of this class, use {@link jnr.ffi.Runtime#findType(NativeType)} or
 *     {@link jnr.ffi.Runtime#findType(TypeAlias)}.
 * <p>
 * Example
 * <pre>
 * {@code
 *
 *    Type pointerType = runtime.findType(NativeType.ADDRESS);
 *
 *    System.out.println("The size of a pointer on this platform is " + pointerType.size());
 * }
 * </pre>
 */
public abstract class Type {
    /**
     * The size in bytes of this type.
     *
     * @return An integer
     */
    public abstract int size();

    /**
     * The native alignment of this type, in bytes
     *
     * @return An integer
     */
    public abstract int alignment();

    /**
     * The native type of this type
     *
     * @return the native type of this type
     */
    public abstract NativeType getNativeType();
}
