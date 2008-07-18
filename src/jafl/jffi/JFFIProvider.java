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

package jafl.jffi;

import jafl.FFIProvider;
import jafl.LibraryOption;
import jafl.MemoryIO;
import java.util.Map;

/**
 *
 * @author wayne
 */
public class JFFIProvider extends FFIProvider {

    @Override
    public MemoryIO allocateMemory(int size) {
        return JFFIMemoryIO.allocate(size);
    }

    @Override
    public MemoryIO allocateMemoryDirect(int size) {
        return JFFIMemoryIO.allocateDirect(size);
    }

    @Override
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return com.googlecode.jffi.Library.loadLibrary(libraryName, interfaceClass);
    }

    @Override
    public int getLastError() {
        return com.googlecode.jffi.LastError.getLastError();
    }

    @Override
    public void setLastError(int error) {
        com.googlecode.jffi.LastError.setLastError(error);
    }

}
