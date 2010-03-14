
package com.kenai.jaffl;

/**
 * NativeType defines the primitive types supported internally.
 *
 * Usually you will not use these types directly, and should instead use the standard
 * types such as {@link Pointer}, {@link NativeLong}, or any of the normal java
 * types such as {@code int}, {@code short}.
 *
 * All other types are defined in terms of these primitive types.
 */
public enum NativeType {
    /** Void type.  Only used for function return types. */
    VOID,

    /** Signed char.  Equivalent to a C char or signed char type.  Usually 1 byte in size. */
    SCHAR,

    /** Unsigned char.  Equivalent to a C unsigned char type.  Usually 1 byte in size */
    UCHAR,

    /** Signed short integer.  Equivalent to a C short or signed short type.  Usually 2 bytes in size. */
    SSHORT,

    /** Unsigned short integer.  Equivalent to a C unsigned short type.  Usually 2 bytes in size. */
    USHORT,

    /** Signed integer.  Equivalent to a C int or signed int type.  Usually 4 bytes in size. */
    SINT,

    /** Unsigned integer.  Equivalent to a C unsigned int type.  Usually 4 bytes in size. */
    UINT,

    /** Signed long integer.  Equivalent to a C long or signed long type.  Can be either 4 or 8 bytes in size, depending on the platform. */
    SLONG,

    /** Unsigned long integer.  Equivalent to a C unsigned long type.  Can be either 4 or 8 bytes in size, depending on the platform. */
    ULONG,

    /** Signed long long integer.  Equivalent to a C long long or signed long long type.  Usually 8 bytes in size. */
    SLONGLONG,

    /** Unsigned long long integer.  Equivalent to a C unsigned long long type.  Usually 8 bytes in size. */
    ULONGLONG,

    /** Single precision floating point.  Equivalent to a C float type.  Usually 4 bytes in size. */
    FLOAT,

    /** Double precision floating point.  Equivalent to a C double type.  Usually 4 bytes in size. */
    DOUBLE,

    /** Native memory address.  Equivalent to a C void* or char* pointer type.  Can be either 4 or 8 bytes in size, depending on the platform. */
    ADDRESS;
}
