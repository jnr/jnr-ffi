package jnr.ffi.provider.jffi;

import com.kenai.jffi.Function;
import com.kenai.jffi.HeapInvocationBuffer;
import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.LoadedLibrary;

/**
 *
 */
public abstract class AbstractAsmLibraryInterface implements LoadedLibrary {
    public static final com.kenai.jffi.Invoker ffi = com.kenai.jffi.Invoker.getInstance();
    public static final NativeRuntime runtime = NativeRuntime.getInstance();

    // Strong ref to keep the library alive
    protected final NativeLibrary library;

    public AbstractAsmLibraryInterface(NativeLibrary library) {
        this.library = library;
    }

    public final jnr.ffi.Runtime getRuntime() {
        return runtime;
    }
}
