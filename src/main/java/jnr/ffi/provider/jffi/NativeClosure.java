package jnr.ffi.provider.jffi;

import jnr.ffi.Closure;

import java.lang.ref.WeakReference;

/**
 *
 */
public abstract class NativeClosure {
    protected final NativeRuntime runtime;
    private final WeakReference<Closure> closureRef;

    public NativeClosure(NativeRuntime runtime, Closure closure) {
        this.runtime = runtime;
        this.closureRef = new WeakReference<Closure>(closure);
    }

    public final void invoke(com.kenai.jffi.Closure.Buffer buffer) {
        Closure closure = closureRef.get();
        if (closure == null) {
            buffer.setLongReturn(0L);
            return;
        }
        invoke(buffer, closure);
    }

    abstract protected void invoke(com.kenai.jffi.Closure.Buffer buffer, Closure closure);
}
