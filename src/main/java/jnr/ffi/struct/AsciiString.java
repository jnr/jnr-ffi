package jnr.ffi.struct;

public class AsciiString extends UTFString {
    private Struct struct;

    public AsciiString(Struct struct, int size) {
        super(struct, size, Struct.ASCII);
        this.struct = struct;
    }
}
