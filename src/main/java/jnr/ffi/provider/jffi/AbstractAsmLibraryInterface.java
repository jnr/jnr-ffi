package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import jnr.ffi.*;
import jnr.ffi.provider.LoadedLibrary;

/**
 *
 */
public abstract class AbstractAsmLibraryInterface implements LoadedLibrary {
    public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();

    // Strong ref to keep the library alive
    protected final NativeLibrary library;

    public AbstractAsmLibraryInterface(NativeLibrary library) {
        this.library = library;
    }

    protected static final HeapInvocationBuffer newInvocationBuffer(Function f) {
        return new HeapInvocationBuffer(f);
    }

    public final jnr.ffi.Runtime __jaffl_runtime__() {
        return NativeRuntime.getInstance();
    }
}
