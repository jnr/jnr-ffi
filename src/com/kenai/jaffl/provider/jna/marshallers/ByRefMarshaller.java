
package com.kenai.jaffl.provider.jna.marshallers;

import com.kenai.jaffl.byref.ByReference;
import com.kenai.jaffl.provider.jna.InvocationSession;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Marshals ByReference types to native JNA types.
 */
public class ByRefMarshaller extends BaseMarshaller {
    public ByRefMarshaller(MarshalContext ctx) {
        super(ctx);
    }

    public Object marshal(InvocationSession session, Object value) {
        if (value == null) {
            return null;
        }
        final ByReference ref = (ByReference) value;
        final ByteBuffer buf = ByteBuffer.allocate(ref.nativeSize()).order(ByteOrder.nativeOrder());
        if (ctx.isIn()) {
            ref.marshal(buf);
        }
        if (ctx.isOut()) {
            session.addPostInvoke(new InvocationSession.PostInvoke() {

                public void postInvoke() {
                    ref.unmarshal(buf);
                }
            });
        }
        return buf;
    }
}
