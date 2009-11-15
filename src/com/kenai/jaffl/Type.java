
package com.kenai.jaffl;

import com.kenai.jaffl.provider.NativeType;

public interface Type {
    /**
     * The size in bytes of this type.
     *
     * @return An integer
     */
    int size();

    /**
     * The native alignment of this type, in bytes
     *
     * @return An integer
     */
    int alignment();

    public static final Type SCHAR = FFIProvider.getProvider().getType(NativeType.SCHAR);
    public static final Type UCHAR = FFIProvider.getProvider().getType(NativeType.UCHAR);
    public static final Type SSHORT = FFIProvider.getProvider().getType(NativeType.SSHORT);
    public static final Type USHORT = FFIProvider.getProvider().getType(NativeType.USHORT);
    public static final Type SINT = FFIProvider.getProvider().getType(NativeType.SINT);
    public static final Type UINT = FFIProvider.getProvider().getType(NativeType.UINT);
    public static final Type SLONG = FFIProvider.getProvider().getType(NativeType.SLONG);
    public static final Type ULONG = FFIProvider.getProvider().getType(NativeType.ULONG);
    public static final Type SLONGLONG = FFIProvider.getProvider().getType(NativeType.SLONGLONG);
    public static final Type ULONGLONG = FFIProvider.getProvider().getType(NativeType.ULONGLONG);
    public static final Type FLOAT = FFIProvider.getProvider().getType(NativeType.FLOAT);
    public static final Type DOUBLE = FFIProvider.getProvider().getType(NativeType.DOUBLE);
    public static final Type ADDRESS = FFIProvider.getProvider().getType(NativeType.ADDRESS);
}
