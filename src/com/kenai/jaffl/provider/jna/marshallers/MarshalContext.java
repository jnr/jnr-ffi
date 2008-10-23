package com.kenai.jaffl.provider.jna.marshallers;

/**
 *
 * @author wayne
 */
public final class MarshalContext {
    private final boolean in;
    private final boolean out;
    public MarshalContext(boolean in, boolean out) {
        this.in = in;
        this.out = out;
    }
    public final boolean isIn() {
        return in;
    }
    public final boolean isOut() {
        return out;
    }
}
