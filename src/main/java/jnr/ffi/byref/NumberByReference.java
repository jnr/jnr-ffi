/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi.byref;

import jnr.ffi.*;
import jnr.ffi.Runtime;

/**
 * NumberByReference is used when the address of a primitive integral value must be passed
 * as a parameter to a function, but the exact type is system dependent.
 *
 * <p>For example, the following C code,
 * <pre>
 * {@code
 *
 * extern void get_size(ssize_t *sp);
 *
 * ssize_t foo(void) {
 *     ssize_t n;
 *     // pass a reference to 'n' so get_size() can fill it out
 *     get_size(&n);
 *
 *     return n;
 * }
 * }
 * </pre>
 * <p>
 * Would be declared in java as
 * <pre>
 * {@code
 *
 * interface Lib {
 *     void get_size(@Out NumberByReference ap);
 * }
 *
 * }
 * </pre>
 * <p>
 * and used like this
 * <pre>
 * {@code
 *
 * NumberByReference size = new NumberByReference(ssize_t);
 * lib.get_size(size);
 * System.out.printf("size from lib=%d\n", size.longValue());
 *
 * }
 * </pre>
 */
public class NumberByReference extends AbstractNumberReference<Number> {
    private final TypeAlias typeAlias;

    public NumberByReference(TypeAlias typeAlias, Number value) {
        super(checkNull(value));
        this.typeAlias = typeAlias;
    }

    public NumberByReference(TypeAlias typeAlias) {
        super(0);
        this.typeAlias = typeAlias;
    }

    @Override
    public int nativeSize(jnr.ffi.Runtime runtime) {
        return runtime.findType(typeAlias).size();
    }

    @Override
    public void fromNative(Runtime runtime, Pointer memory, long offset) {
        switch (runtime.findType(typeAlias).getNativeType()) {
            case SCHAR:
            case UCHAR:
                value = memory.getByte(offset);
                break;

            case SSHORT:
            case USHORT:
                value = memory.getShort(offset);
                break;

            case SINT:
            case UINT:
                value = memory.getInt(offset);
                break;

            case SLONG:
            case ULONG:
                value = memory.getNativeLong(offset);
                break;

            case SLONGLONG:
            case ULONGLONG:
                value = memory.getLongLong(offset);
                break;

            case ADDRESS:
                value = memory.getAddress(offset);
                break;

            case FLOAT:
                value = memory.getFloat(offset);
                break;

            case DOUBLE:
                value = memory.getDouble(offset);
                break;

            default:
                throw new UnsupportedOperationException("unsupported type: " + typeAlias);
        }
    }

    @Override
    public void toNative(Runtime runtime, Pointer memory, long offset) {
        switch (runtime.findType(typeAlias).getNativeType()) {
            case SCHAR:
            case UCHAR:
                memory.putByte(offset, value.byteValue());
                break;

            case SSHORT:
            case USHORT:
                memory.putShort(offset, value.shortValue());
                break;

            case SINT:
            case UINT:
                memory.putInt(offset, value.intValue());
                break;

            case SLONG:
            case ULONG:
                memory.putNativeLong(offset, value.longValue());
                break;

            case SLONGLONG:
            case ULONGLONG:
                memory.putLongLong(offset, value.longValue());
                break;

            case ADDRESS:
                memory.putAddress(offset, value.longValue());
                break;

            case FLOAT:
                memory.putFloat(offset, value.floatValue());
                break;

            case DOUBLE:
                memory.putDouble(offset, value.doubleValue());
                break;

            default:
                throw new UnsupportedOperationException("unsupported type: " + typeAlias);
        }
    }
}
