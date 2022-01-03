package jnr.ffi.number.utils;

import java.util.Arrays;
import java.util.List;

public abstract class NumberRange {

    public static abstract class Range {
        public abstract long lower();
        public abstract long upper();
    }

    public abstract long minValue();

    public abstract long maxValue();

    public long normalValue() {return 0;}

    public long offset() {return 16;}

    public final Range low = new Range() {
        @Override
        public long lower() {return minValue() - offset();}

        @Override
        public long upper() {return minValue() + offset();}
    };

    public final Range normal = new Range() {
        @Override
        public long lower() {return normalValue() - offset();}

        @Override
        public long upper() {return normalValue() + offset();}
    };

    public final Range high = new Range() {
        @Override
        public long lower() {return maxValue() - offset();}

        @Override
        public long upper() {return maxValue() + offset();}
    };

    public final List<Range> ranges = Arrays.asList(low, normal, high);

    public static final NumberRange BYTE = new NumberRange() {
        @Override
        public long minValue() {return Byte.MIN_VALUE;}

        @Override
        public long maxValue() {return Byte.MAX_VALUE;}
    };

    public static final NumberRange SHORT = new NumberRange() {
        @Override
        public long minValue() {return Short.MIN_VALUE;}

        @Override
        public long maxValue() {return Short.MAX_VALUE;}
    };

    public static final NumberRange INT = new NumberRange() {
        @Override
        public long minValue() {return Integer.MIN_VALUE;}

        @Override
        public long maxValue() {return Integer.MAX_VALUE;}
    };

    // We physically cannot truly test long to its limits because the loop will over/underflow,
    // so just use something slightly within the limits
    public static final NumberRange LONG = new NumberRange() {
        @Override
        public long minValue() {return Long.MIN_VALUE + (offset() + 1);}

        @Override
        public long maxValue() {return Long.MAX_VALUE - (offset() + 1);}
    };
}
