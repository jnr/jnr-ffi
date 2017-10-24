package jnr.ffi.struct;

public final class Offset extends Number {
    private final int offset;
    public Offset(int offset) {
        this.offset = offset;
    }
    @Override
    public int intValue() {
        return offset;
    }
    @Override
    public long longValue() {
        return offset;
    }
    @Override
    public float floatValue() {
        return offset;
    }
    @Override
    public double doubleValue() {
        return offset;
    }
}
