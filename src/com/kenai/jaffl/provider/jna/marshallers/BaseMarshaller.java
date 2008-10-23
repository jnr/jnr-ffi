/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.provider.jna.marshallers;

/**
 *
 * @author wayne
 */
public abstract class BaseMarshaller implements Marshaller {
    protected final MarshalContext ctx;
    public BaseMarshaller(MarshalContext ctx) {
        this.ctx = ctx;
    }
}
