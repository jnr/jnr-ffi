package jnr.ffi.provider.converters;

import jnr.ffi.Memory;
import jnr.ffi.Pointer;
import jnr.ffi.byref.ByReference;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 *
 */
@ToNativeConverter.Cacheable
public class ByReferenceParameterConverter implements ToNativeConverter<ByReference, Pointer> {
    private static final ToNativeConverter<ByReference, Pointer> IN = new ByReferenceParameterConverter(ParameterFlags.IN);
    private static final ToNativeConverter<ByReference, Pointer> OUT = new ByReferenceParameterConverter.Out(ParameterFlags.OUT);
    private static final ToNativeConverter<ByReference, Pointer> INOUT = new ByReferenceParameterConverter.Out(ParameterFlags.IN | ParameterFlags.OUT);
    private final int parameterFlags;

    private ByReferenceParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    public static ToNativeConverter<ByReference, Pointer> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? ParameterFlags.isIn(parameterFlags) ? INOUT : OUT : IN;
    }

    public Pointer toNative(ByReference value, ToNativeContext context) {
        if (value == null) {
            return null;
        }

        Pointer memory =  Memory.allocate(context.getRuntime(), value.nativeSize(context.getRuntime()));
        if (ParameterFlags.isIn(parameterFlags)) {
            value.toNative(context.getRuntime(), memory, 0);
        }

        return memory;
    }

    public Class<Pointer> nativeType() {
        return Pointer.class;
    }

    public static final class Out extends ByReferenceParameterConverter implements ToNativeConverter.PostInvocation<ByReference, Pointer> {
        public Out(int parameterFlags) {
            super(parameterFlags);
        }

        public void postInvoke(ByReference byReference, Pointer pointer, ToNativeContext context) {
            if (byReference != null && pointer != null) {
                byReference.fromNative(context.getRuntime(), pointer, 0);
            }
        }
    }
}
