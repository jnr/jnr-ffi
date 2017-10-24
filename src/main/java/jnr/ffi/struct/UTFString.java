package jnr.ffi.struct;

import java.nio.charset.Charset;

public class UTFString extends StringField {
    public UTFString( Struct struct, int length, Charset cs) {
        super(struct,length * 8, 8, length, cs); // FIXME: This won't work for non-ASCII

    }
    protected jnr.ffi.Pointer getStringMemory() {
        return getMemory().slice(offset(), length());
    }

    public final java.lang.String get() {
        return getStringMemory().getString(0, length, charset);
    }

    public final void set(java.lang.String value) {
        getStringMemory().putString(0, value, length, charset);
    }
}
