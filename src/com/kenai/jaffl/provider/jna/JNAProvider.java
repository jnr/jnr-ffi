
package com.kenai.jaffl.provider.jna;

import com.kenai.jaffl.FFIProvider;
import com.kenai.jaffl.LibraryOption;
import com.kenai.jaffl.MemoryIO;
import com.kenai.jaffl.Pointer;
import com.kenai.jaffl.provider.NativeInvocationHandler;
import java.util.Map;

/**
 *
 */
public class JNAProvider extends FFIProvider {

    @Override
    public MemoryIO allocateMemory(int size) {
        return JNAMemoryIO.allocateDirect(size, true);
    }

    @Override
    public MemoryIO allocateMemoryDirect(int size) {
        return JNAMemoryIO.allocateDirect(size);
    }

    @Override
    public MemoryIO allocateMemoryDirect(int size, boolean clear) {
        return JNAMemoryIO.allocateDirect(size, clear);
    }
    @Override
    public MemoryIO wrap(Pointer address) {
        return new PointerMemoryIO(((JNAPointer) address).getNativePointer());
    }

    @Override
    public <T> T loadLibrary(String libraryName, Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions) {
        return interfaceClass.cast(NativeInvocationHandler.wrapInterface(new JNALibrary(libraryName),
                interfaceClass, libraryOptions));
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
