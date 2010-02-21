
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Runtime;

final class JFFIPointer extends DirectMemoryIO  {

    JFFIPointer(Runtime runtime, long address) {
        super(address);
    }

    JFFIPointer(long address) {
        super(address);
    }

    public int intValue() {
        return (int) address;
    }

    public long longValue() {
        return address;
    }

    public float floatValue() {
        return (float) address;
    }

    public double doubleValue() {
        return (double) address;
    }

    public static final JFFIPointer valueOf(Runtime runtime, long address) {
        return new JFFIPointer(runtime, address);
    }

    public static final JFFIPointer valueOf(Runtime runtime, int address) {
        return new JFFIPointer(runtime, address & 0xffffffffL);
    }

    public static final JFFIPointer valueOf(long address) {
        return new JFFIPointer(NativeRuntime.getInstance(), address);
    }

    public static final JFFIPointer valueOf(int address) {
        return new JFFIPointer(NativeRuntime.getInstance(), address & 0xffffffffL);
    }
}
