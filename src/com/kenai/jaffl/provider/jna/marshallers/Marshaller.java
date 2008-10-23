
package com.kenai.jaffl.provider.jna.marshallers;

import com.kenai.jaffl.provider.jna.InvocationSession;

/**
 * Interface to marshal java types to JNA types
 */
public interface Marshaller {
    public Object marshal(InvocationSession session, Object value);
}
