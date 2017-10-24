package jnr.ffi.struct;

public class UTF8String extends UTFString {
    private Struct struct;

    public UTF8String(Struct struct, int size) {
        super(struct, size, Struct.UTF8);
        this.struct = struct;
    }
}
