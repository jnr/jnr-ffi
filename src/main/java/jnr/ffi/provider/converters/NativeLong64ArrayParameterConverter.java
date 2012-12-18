package jnr.ffi.provider.converters;

import jnr.ffi.NativeLong;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.provider.ParameterFlags;

/**
 * Converts a NativeLong[] array to a primitive long[] array parameter
 */
@ToNativeConverter.NoContext
@ToNativeConverter.Cacheable
public class NativeLong64ArrayParameterConverter implements ToNativeConverter<NativeLong[], long[]> {
    private static final ToNativeConverter<NativeLong[], long[]> IN = new NativeLong64ArrayParameterConverter(ParameterFlags.IN);
    private static final ToNativeConverter<NativeLong[], long[]> OUT = new NativeLong64ArrayParameterConverter.Out(ParameterFlags.OUT);
    private static final ToNativeConverter<NativeLong[], long[]> INOUT = new NativeLong64ArrayParameterConverter.Out(ParameterFlags.IN | ParameterFlags.OUT);

    private final int parameterFlags;

    public static ToNativeConverter<NativeLong[], long[]> getInstance(ToNativeContext toNativeContext) {
        int parameterFlags = ParameterFlags.parse(toNativeContext.getAnnotations());
        return ParameterFlags.isOut(parameterFlags) ? ParameterFlags.isIn(parameterFlags) ? INOUT : OUT : IN;
    }

    private NativeLong64ArrayParameterConverter(int parameterFlags) {
        this.parameterFlags = parameterFlags;
    }

    @Override
    public long[] toNative(NativeLong[] array, ToNativeContext context) {
        if (array == null) {
            return null;
        }
        long[] primitive = new long[array.length];
        if (ParameterFlags.isIn(parameterFlags)) {
            for (int i = 0; i < array.length; i++) {
                primitive[i] = array[i] != null ? array[i].intValue() : 0;
            }
        }

        return primitive;
    }

    public static final class Out extends NativeLong64ArrayParameterConverter implements PostInvocation<NativeLong[], long[]> {
        Out(int parameterFlags) {
            super(parameterFlags);
        }

        @Override
        public void postInvoke(NativeLong[] array, long[] primitive, ToNativeContext context) {
            if (array != null && primitive != null) {
                for (int i = 0; i < array.length; i++) {
                    array[i] = NativeLong.valueOf(primitive[i]);
                }
            }
        }
    }

    @Override
    @LongLong
    public Class<long[]> nativeType() {
        return long[].class;
    }
}
