package com.kenai.jaffl.provider;

import com.kenai.jaffl.NativeType;

/**
 *
 */
public final class BadType implements com.kenai.jaffl.Type {

    private final NativeType nativeType;

    public BadType(NativeType type) {
        this.nativeType = type;
    }

    public final int alignment() {
        throw new RuntimeException("invalid type: " + nativeType);
    }

    public final int size() {
        throw new RuntimeException("invalid type: " + nativeType);
    }
}
