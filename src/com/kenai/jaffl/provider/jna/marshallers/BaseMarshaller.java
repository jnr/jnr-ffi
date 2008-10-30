
package com.kenai.jaffl.provider.jna.marshallers;

/**
 * Base class for many parameter marshallers
 */
public abstract class BaseMarshaller implements Marshaller {
    protected final MarshalContext ctx;
    public BaseMarshaller(MarshalContext ctx) {
        this.ctx = ctx;
    }
}
