/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.provider.jna.marshallers;

import com.kenai.jaffl.provider.StringIO;
import com.kenai.jaffl.provider.jna.InvocationSession;

/**
 *
 * @author wayne
 */
public class CharSequenceMarshaller extends BaseMarshaller {
    public CharSequenceMarshaller(MarshalContext ctx) {
        super(ctx);
    }

    public Object marshal(InvocationSession session, Object value) {
        // Handle null or regular String values natively in JNA
        if (value == null || value instanceof String) {
            return value;
        }
        final StringIO io = StringIO.getStringIO();
        final CharSequence cs = (CharSequence) value;
        return io.toNative(cs, cs.length(), true);
    }

}
