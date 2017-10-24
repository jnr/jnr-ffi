package jnr.ffi.struct;

import jnr.ffi.NativeType;

import java.nio.charset.Charset;

abstract public class StringField extends Field {
    protected final Charset charset;
    protected final int length;

    protected StringField(Struct struct, NativeType nativeType, int length, Charset cs) {
        super(struct, nativeType);
        this.length = length;
        this.charset = cs;
    }
    protected StringField(Struct struct, int size, int align, int length, Charset cs) {
        super(struct, size, align);
        this.length = length;
        this.charset = cs;
    }
    protected StringField(Struct struct, int size, int align, Offset offset, int length, Charset cs) {
        super(struct, size, align, offset);
        this.length = length;
        this.charset = cs;
    }
    public final int length() {
        return length;
    }

    protected abstract jnr.ffi.Pointer getStringMemory();
    public abstract java.lang.String get();
    public abstract void set(java.lang.String value);

    @Override
    public final java.lang.String toString() {
        return get();
    }
}
