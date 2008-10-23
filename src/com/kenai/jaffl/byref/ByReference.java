/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jaffl.byref;

import java.nio.ByteBuffer;

public interface ByReference<T> {
    /**
     * Gets the size of the native buffer required to store the value
     * 
     * @return the size in bytes of the native type
     */
    int nativeSize();
    /**
     * Copies the java value to native memory
     * 
     * @param buffer the native memory buffer.
     */
    void marshal(ByteBuffer buffer);
    
    /**
     * Copies the java value from native memory
     * 
     * @param buffer the native memory buffer.
     */
    void unmarshal(ByteBuffer buffer);
    
    T getValue();
}
