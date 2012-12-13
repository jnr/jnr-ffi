/*
 * Copyright (C) 2009-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi;

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

    /** Double precision floating point.  Equivalent to a C double type.  Usually 8 bytes in size. */
    DOUBLE,

    /** Native struct type */
    STRUCT,

    /** Native memory address.  Equivalent to a C void* or char* pointer type.  Can be either 4 or 8 bytes in size, depending on the platform. */
    ADDRESS
}
