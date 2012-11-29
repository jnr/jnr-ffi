package jnr.ffi.provider.jffi;

import jnr.ffi.Library;
import jnr.ffi.mapper.FunctionMapper;

import java.lang.annotation.Annotation;
import java.util.Collection;

public final class NativeFunctionMapperContext implements FunctionMapper.Context {
    private final NativeLibrary library;

    private final Collection<Annotation> annotations;

    public NativeFunctionMapperContext(NativeLibrary library, Collection<Annotation> annotations) {
        this.library = library;
        this.annotations = annotations;
    }

    public Library getLibrary() {
        return null;
    }

    public boolean isSymbolPresent(String name) {
        return library.getSymbolAddress(name) != 0L;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }
}
