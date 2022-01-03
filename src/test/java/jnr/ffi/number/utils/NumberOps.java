package jnr.ffi.number.utils;

import java.util.Random;

public class NumberOps {

    private static final Random R = new Random();

    public static interface ByteOp {
        public void run(byte i1, byte i2);
    }

    public static interface ShortOp {
        public void run(short i1, short i2);
    }

    public static interface IntOp {
        public void run(int i1, int i2);
    }

    public static interface LongOp {
        public void run(long i1, long i2);
    }

    public static interface FloatOp {
        public void run(float i1, float i2);
    }

    public static interface DoubleOp {
        public void run(double i1, double i2);
    }

    public static interface ByteRet {
        public void run(byte i1);
    }

    public static interface ShortRet {
        public void run(short i1);
    }

    public static interface IntRet {
        public void run(int i1);
    }

    public static interface LongRet {
        public void run(long i1);
    }

    public static interface FloatRet {
        public void run(float i1);
    }

    public static interface DoubleRet {
        public void run(double i1);
    }

    public static void rangesLoop(NumberRange range, NumberOps.LongOp op) {
        for (NumberRange.Range range1 : range.ranges) {
            for (NumberRange.Range range2 : range.ranges) {
                for (long range1Counter = range1.lower(); range1Counter <= range1.upper(); range1Counter++) {
                    for (long range2Counter = range2.lower(); range2Counter <= range2.upper(); range2Counter++) {
                        op.run(range1Counter, range2Counter);
                    }
                }
            }
        }
    }

    public static void returnLoop(NumberRange range, NumberOps.LongRet op) {
        for (NumberRange.Range range1 : range.ranges) {
            for (long range1Counter = range1.lower(); range1Counter <= range1.upper(); range1Counter++) {
                op.run(range1Counter);
            }
        }
    }

    public static void rangesLoop(NumberOps.ByteOp op) {
        NumberOps.rangesLoop(NumberRange.BYTE,
                (long l1, long l2) -> op.run((byte) l1, (byte) l2));
    }

    public static void rangesLoop(NumberOps.ShortOp op) {
        NumberOps.rangesLoop(NumberRange.SHORT,
                (long l1, long l2) -> op.run((short) l1, (short) l2));
    }

    public static void rangesLoop(NumberOps.IntOp op) {
        NumberOps.rangesLoop(NumberRange.INT,
                (long l1, long l2) -> op.run((int) l1, (int) l2));
    }

    public static void rangesLoop(NumberOps.LongOp op) {
        NumberOps.rangesLoop(NumberRange.LONG,
                (long l1, long l2) -> op.run((long) l1, (long) l2));
    }

    public static void returnLoop(NumberOps.ByteRet op) {
        NumberOps.returnLoop(NumberRange.BYTE,
                (long l1) -> op.run((byte) l1));
    }

    public static void returnLoop(NumberOps.ShortRet op) {
        NumberOps.returnLoop(NumberRange.SHORT,
                (long l1) -> op.run((byte) l1));
    }

    public static void returnLoop(NumberOps.IntRet op) {
        NumberOps.returnLoop(NumberRange.INT,
                (long l1) -> op.run((int) l1));
    }

    public static void returnLoop(NumberOps.LongRet op) {
        NumberOps.returnLoop(NumberRange.LONG,
                (long l1) -> op.run((long) l1));
    }

    private static final int FLOAT_ITERATIONS = 500_000;

    public static void floatLoop(NumberOps.FloatOp op) {
        for (int i = 0; i < FLOAT_ITERATIONS; i++) {
            float f1 = R.nextFloat();
            float f2 = R.nextFloat();
            op.run(f1, f2);
        }
    }

    public static void floatReturnLoop(NumberOps.FloatRet op) {
        for (int i = 0; i < FLOAT_ITERATIONS; i++) {
            float f1 = R.nextFloat();
            op.run(f1);
        }
    }

    public static void doubleLoop(NumberOps.DoubleOp op) {
        for (int i = 0; i < FLOAT_ITERATIONS; i++) {
            double f1 = R.nextDouble();
            double f2 = R.nextDouble();
            op.run(f1, f2);
        }
    }

    public static void doubleReturnLoop(NumberOps.DoubleRet op) {
        for (int i = 0; i < FLOAT_ITERATIONS; i++) {
            double f1 = R.nextDouble();
            op.run(f1);
        }
    }
}
