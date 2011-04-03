package jnr.ffi.provider.jffi;

import jnr.ffi.Closure;
import jnr.ffi.provider.ClosureManager;

import java.util.Collections;
import java.util.Map;

/**
 *
 */
final class NativeClosureManager implements ClosureManager {
    private volatile Map<Class<? extends Closure>, NativeClosureFactory> factories = Collections.emptyMap();
    private final NativeRuntime runtime;

    NativeClosureManager(NativeRuntime runtime) {
        this.runtime = runtime;
    }

    public <T extends Closure> T newClosure(Class<? extends T> closureClass, T instance) {
        NativeClosureFactory<T> factory = factories.get(closureClass);
        if (factory != null) {
            return factory.newClosure(instance);
        }
        return null;
    }
}
