/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2008 JRuby project
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/

package jaffl.provider.jna;

import com.sun.jna.Pointer;
import jaffl.MemoryIO;

/**
 * JNA implementation of memory I/O operations.
 */
abstract class JNAMemoryIO extends AbstractMemoryIO {
    /**
     * The native memory pointer
     */
    final Object memory;
    
    /**
     * Allocates a new block of java heap memory and wraps it in a {@link MemoryIO}
     * accessor.
     * 
     * @param size The size in bytes of memory to allocate.
     * 
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO allocate(int size) {
//        return BufferIO.allocate(size);
        return null;
    }
    
    /**
     * Allocates a new block of native memory and wraps it in a {@link MemoryIO}
     * accessor.
     * 
     * @param size The size in bytes of memory to allocate.
     * 
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO allocateDirect(int size) {
        return PointerMemoryIO.allocate(size);
    }
    
    /**
     * Creates a new JNA <tt>MemoryIO</tt> instance.
     * 
     * @param memory The memory object to wrap.
     */
    JNAMemoryIO(Object memory) {
        this.memory = memory;
    }
    
    /**
     * Gets the underlying memory object this <tt>MemoryIO</tt> is wrapping.
     * 
     * @return The native pointer or ByteBuffer.
     */
    Object getMemory() {
        return memory;
    }
    
    /**
     * Wraps a <tt>MemoryIO</tt> accessor around an existing native memory area.
     * 
     * @param ptr The native pointer to wrap.
     * @return A new <tt>MemoryIO</tt> instance that can access the memory.
     */
    static JNAMemoryIO wrap(Pointer ptr) {
        return new PointerMemoryIO(ptr);
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JNAMemoryIO) && ((JNAMemoryIO) obj).memory.equals(memory);
    }

    @Override
    public int hashCode() {
        return memory.hashCode();
    }
}
