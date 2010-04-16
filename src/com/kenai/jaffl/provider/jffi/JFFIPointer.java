
package com.kenai.jaffl.provider.jffi;

final class JFFIPointer extends DirectMemoryIO  {
    
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

    public static final JFFIPointer valueOf(long address) {
        return new JFFIPointer(address);
    }

    public static final JFFIPointer valueOf(int address) {
        return new JFFIPointer(address & 0xffffffffL);
    }
}
