/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.FFIProvider;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.provider.NativeInvocationHandler;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author wayne
 */
public class JNAProvider extends FFIProvider {

    @Override
    public MemoryIO allocateMemory(int size) {
        return JNAMemoryIO.allocate(size);
    }

    @Override
    public MemoryIO allocateMemoryDirect(int size) {
        return JNAMemoryIO.allocateDirect(size);
    }

    @Override
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return interfaceClass.cast(NativeInvocationHandler.wrapInterface(new JNALibrary(libraryName),
                interfaceClass, Collections.EMPTY_MAP));
    }

    @Override
    public int getLastError() {
        return com.sun.jna.Native.getLastError();
    }

    @Override
    public void setLastError(int error) {
        com.sun.jna.Native.setLastError(error);
    }

}
