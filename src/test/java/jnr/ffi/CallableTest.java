/* 
 * Copyright (C) 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package jnr.ffi;

import jnr.ffi.annotations.Delegate;
import jnr.ffi.annotations.LongLong;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 */
public class CallableTest {

    public CallableTest() {
    }
    private static TestLib lib;
    @BeforeClass
    public static void setUpClass() throws Exception {
        lib = TstUtil.loadTestLib(TestLib.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    public static interface TestLib {
        public static interface CallableVrV {
            @Delegate public void call();
        }
        void testClosureVrV(CallableVrV closure);
        public static interface CallableVrB {
            @Delegate public byte call();
        }
        byte testClosureVrB(CallableVrB closure);
        public static interface CallableVrS {
            @Delegate public short call();
        }
        short testClosureVrS(CallableVrS closure);
        public static interface CallableVrI {
            @Delegate public int call();
        }
        int testClosureVrI(CallableVrI closure);
        public static interface CallableVrL {
            @Delegate public @LongLong long call();
        }
        @LongLong long testClosureVrL(CallableVrL closure);
        public interface CallableVrF {
            @Delegate public float call();
        }
        float testClosureVrF(CallableVrF closure);
        public interface CallableVrD {
            @Delegate public double call();
        }
        double testClosureVrD(CallableVrD closure);

        public interface CallableBrV {
            @Delegate public void call(byte a1);
        }
        void testClosureBrV(CallableBrV closure, byte a1);

        public interface CallableSrV {
            @Delegate public void call(short a1);
        }
        void testClosureSrV(CallableSrV closure, short a1);

        public interface CallableIrV {
            @Delegate public void call(int a1);
        }
        void testClosureIrV(CallableIrV closure, int a1);
//        void testClosureBrV(Callable closure, byte a1);
//        void testClosureSrV(Callable closure, short a1);

//        void testClosureLrV(Callable closure, long a1);
//        void testClosureFrV(Callable closure, float a1);
//        void testClosureDrV(Callable closure, double a1);
//
//        // closures with small-big-small arguments
//        void testClosureBSBrV(Callable closure, byte a1, short a2, byte a3);
//        void testClosureBIBrV(Callable closure, byte a1, int a2, byte a3);
//        void testClosureBLBrV(Callable closure, byte a1, long a2, byte a3);
//        void testClosureBFBrV(Callable closure, byte a1, float a2, byte a3);
//        void testClosureBDBrV(Callable closure, byte a1, double a2, byte a3);
//
//        void testClosureSBSrV(Callable closure, short a1, byte a2, short a3);
//        void testClosureSISrV(Callable closure, short a1, int a2, short a3);
//        void testClosureSLSrV(Callable closure, short a1, long a2, short a3);
//        void testClosureSFSrV(Callable closure, short a1, float a2, short a3);
//        void testClosureSDSrV(Callable closure, short a1, double a2, short a3);
//
//        // Now big-smaller-smaller
//        void testClosureLSBrV(Callable closure, long a1, short a2, byte a3);
//        // big-smaller-small
//        void testClosureLBSrV(Callable closure, long a1, byte a2, short a3);
        public interface ReusableCallable {
            @Delegate public void call(int a1);
        }
        Pointer ret_pointer(ReusableCallable callable);
    }
    @Test
    public void closureVrV() {
        final boolean[] called = { false };
        final TestLib.CallableVrV closure = new TestLib.CallableVrV() {

            public void call() {
                called[0] = true;
            }
        };
        lib.testClosureVrV(closure);
        assertTrue("Callable not called", called[0]);
    }
    @Test
    public void closureVrB() {
        final boolean[] called = { false };
        final byte MAGIC = (byte) 0xfe;
        TestLib.CallableVrB closure = new TestLib.CallableVrB() {

            public byte call() {
                called[0] = true;
                return MAGIC;
            }
        };
        byte retVal = lib.testClosureVrB(closure);
        assertTrue("Callable not called", called[0]);
        assertEquals("Incorrect return value from closure", MAGIC, retVal);
    }
    @Test
    public void closureVrS() {
        final boolean[] called = { false };
        final short MAGIC = (short) 0xfee1;
        TestLib.CallableVrS closure = new TestLib.CallableVrS() {

            public short call() {
                called[0] = true;
                return MAGIC;
            }
        };
        short retVal = lib.testClosureVrS(closure);
        assertTrue("Callable not called", called[0]);
        assertEquals("Incorrect return value from closure", MAGIC, retVal);
    }
    @Test
    public void closureVrI() {
        final boolean[] called = { false };
        final int MAGIC = (int) 0xfee1dead;
        TestLib.CallableVrI closure = new TestLib.CallableVrI() {

            public int call() {
                called[0] = true;
                return MAGIC;
            }
        };
        int retVal = lib.testClosureVrI(closure);
        assertTrue("Callable not called", called[0]);
        assertEquals("Incorrect return value from closure", MAGIC, retVal);
    }
    @Test
    public void closureVrL() {
        final boolean[] called = { false };
        final long MAGIC = 0xfee1deadcafebabeL;
        TestLib.CallableVrL closure = new TestLib.CallableVrL() {

            public long call() {
                called[0] = true;
                return MAGIC;
            }
        };
        long retVal = lib.testClosureVrL(closure);
        assertTrue("Callable not called", called[0]);
        assertEquals("Incorrect return value from closure", MAGIC, retVal);
    }
    @Test
    public void closureVrF() {
        final boolean[] called = { false };
        final float MAGIC = (float) 0xfee1dead;
        TestLib.CallableVrF closure = new TestLib.CallableVrF() {

            public float call() {
                called[0] = true;
                return MAGIC;
            }
        };
        float retVal = lib.testClosureVrF(closure);
        assertTrue("Callable not called", called[0]);
        assertEquals("Incorrect return value from closure", MAGIC, retVal, 0f);
    }
    @Test
    public void closureVrD() {
        final boolean[] called = { false };
        final double MAGIC = (double) 0xfee1dead;
        TestLib.CallableVrD closure = new TestLib.CallableVrD() {

            public double call() {
                called[0] = true;
                return MAGIC;
            }
        };
        double retVal = lib.testClosureVrD(closure);
        assertTrue("Callable not called", called[0]);
        assertEquals("Incorrect return value from closure", MAGIC, retVal, 0d);
    }
    @Test
    public void closureBrV() {
        final boolean[] called = { false };
        final byte[] val = { 0 };
        final byte MAGIC = (byte) 0xde;
        TestLib.CallableBrV closure = new TestLib.CallableBrV() {

            public void call(byte a1) {
                called[0] = true;
                val[0] = a1;
            }
        };
        lib.testClosureBrV(closure, MAGIC);
        assertTrue("Callable not called", called[0]);
        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
    }
    @Test
    public void closureSrV() {
        final boolean[] called = { false };
        final short[] val = { 0 };
        final short MAGIC = (short) 0xdead;
        TestLib.CallableSrV closure = new TestLib.CallableSrV() {

            public void call(short a1) {
                called[0] = true;
                val[0] = a1;
            }
        };
        lib.testClosureSrV(closure, MAGIC);
        assertTrue("Callable not called", called[0]);
        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
    }
    @Test
    public void closureIrV() {
        final boolean[] called = { false };
        final int[] val = { 0 };
        final int MAGIC = 0xdeadbeef;
        TestLib.CallableIrV closure = new TestLib.CallableIrV() {

            public void call(int a1) {
                called[0] = true;
                val[0] = a1;
            }
        };
        lib.testClosureIrV(closure, MAGIC);
        assertTrue("Callable not called", called[0]);
        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
    }

    @Test
    public void reuseClosure() {
        TestLib.ReusableCallable closure = new TestLib.ReusableCallable() {

            public void call(int a1) {}
        };
        Pointer p1 = lib.ret_pointer(closure);
        Pointer p2 = lib.ret_pointer(closure);
        assertEquals("not same native address for Callable instance", p1, p2);
    }

//    @Test
//    public void closureLrV() {
//        final boolean[] called = { false };
//        final long[] val = { 0 };
//        final long MAGIC = 0xfee1deadcafebabeL;
//        Callable closure = new Callable() {
//
//            public void invoke(long a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureLrV(closure, MAGIC);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
//    }
//    @Test
//    public void closureFrV() {
//        final boolean[] called = { false };
//        final float[] val = { 0 };
//        final float MAGIC = (float) 0xdeadbeef;
//        Callable closure = new Callable() {
//
//            public void invoke(float a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureFrV(closure, MAGIC);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0], 0f);
//    }
//    @Test
//    public void closureDrV() {
//        final boolean[] called = { false };
//        final double[] val = { 0 };
//        final double MAGIC = (double) 0xfee1deadcafebabeL;
//        Callable closure = new Callable() {
//
//            public void invoke(double a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureDrV(closure, MAGIC);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0], 0d);
//    }
//    @Test
//    public void closureBSBrV() {
//        final boolean[] called = { false };
//        final byte[] v1 = { 0 };
//        final short[] v2 = { 0 };
//        final byte[] v3 = { 0 };
//        final byte A1 = (byte) 0x11;
//        final short A2 = (short) 0xfee1;
//        final byte A3 = (byte) 0x22;
//        Callable closure = new Callable() {
//
//            public void invoke(byte a1, short a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBSBrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureBIBrV() {
//        final boolean[] called = { false };
//        final byte[] v1 = { 0 };
//        final int[] v2 = { 0 };
//        final byte[] v3 = { 0 };
//        final byte A1 = (byte) 0x11;
//        final int A2 = (int) 0xfee1dead;
//        final byte A3 = (byte) 0x22;
//        Callable closure = new Callable() {
//
//            public void invoke(byte a1, int a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBIBrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureBLBrV() {
//        final boolean[] called = { false };
//        final byte[] v1 = { 0 };
//        final long[] v2 = { 0 };
//        final byte[] v3 = { 0 };
//        final byte A1 = (byte) 0x11;
//        final long A2 = (long) 0xfee1deadcafebabeL;
//        final byte A3 = (byte) 0x22;
//        Callable closure = new Callable() {
//
//            public void invoke(byte a1, long a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBLBrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureBFBrV() {
//        final boolean[] called = { false };
//        final byte[] v1 = { 0 };
//        final float[] v2 = { 0 };
//        final byte[] v3 = { 0 };
//        final byte A1 = (byte) 0x11;
//        final float A2 = (float) 0xfee1dead;
//        final byte A3 = (byte) 0x22;
//        Callable closure = new Callable() {
//
//            public void invoke(byte a1, float a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBFBrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0], 0f);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureBDBrV() {
//        final boolean[] called = { false };
//        final byte[] v1 = { 0 };
//        final double[] v2 = { 0 };
//        final byte[] v3 = { 0 };
//        final byte A1 = (byte) 0x11;
//        final double A2 = (double) 0xfee1deadcafebabeL;
//        final byte A3 = (byte) 0x22;
//        Callable closure = new Callable() {
//
//            public void invoke(byte a1, double a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBDBrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0], 0d);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureSBSrV() {
//        final boolean[] called = { false };
//        final short[] v1 = { 0 };
//        final byte[] v2 = { 0 };
//        final short[] v3 = { 0 };
//        final short A1 = (short) 0x1111;
//        final byte A2 = (byte) 0xfe;
//        final short A3 = (short) 0x2222;
//        Callable closure = new Callable() {
//
//            public void invoke(short a1, byte a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSBSrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureSISrV() {
//        final boolean[] called = { false };
//        final short[] v1 = { 0 };
//        final int[] v2 = { 0 };
//        final short[] v3 = { 0 };
//        final short A1 = (short) 0x1111;
//        final int A2 = (int) 0xfee1dead;
//        final short A3 = (short) 0x2222;
//        Callable closure = new Callable() {
//
//            public void invoke(short a1, int a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSISrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//
//    @Test
//    public void closureSLSrV() {
//        final boolean[] called = { false };
//        final short[] v1 = { 0 };
//        final long[] v2 = { 0 };
//        final short[] v3 = { 0 };
//        final short A1 = (short) 0x1111;
//        final long A2 = (long) 0xfee1deadcafebabeL;
//        final short A3 = (short) 0x2222;
//        Callable closure = new Callable() {
//
//            public void invoke(short a1, long a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSLSrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureSFSrV() {
//        final boolean[] called = { false };
//        final short[] v1 = { 0 };
//        final float[] v2 = { 0 };
//        final short[] v3 = { 0 };
//        final short A1 = (short) 0x1111;
//        final float A2 = (float) 0xfee1dead;
//        final short A3 = (short) 0x2222;
//        Callable closure = new Callable() {
//
//            public void invoke(short a1, float a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSFSrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0], 0f);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureSDSrV() {
//        final boolean[] called = { false };
//        final short[] v1 = { 0 };
//        final double[] v2 = { 0 };
//        final short[] v3 = { 0 };
//        final short A1 = (short) 0x1111;
//        final double A2 = (double) 0xfee1deadcafebabeL;
//        final short A3 = (short) 0x2222;
//        Callable closure = new Callable() {
//
//            public void invoke(short a1, double a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSDSrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0], 0d);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureLSBrV() {
//        final boolean[] called = { false };
//        final long[] v1 = { 0 };
//        final short[] v2 = { 0 };
//        final byte[] v3 = { 0 };
//        final long A1 = (long) 0xfee1deadcafebabeL;
//        final short A2 = (short) 0x1111;
//        final byte A3 = (byte) 0x22;
//        Callable closure = new Callable() {
//
//            public void invoke(long a1, short a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureLSBrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
//    @Test
//    public void closureLBSrV() {
//        final boolean[] called = { false };
//        final long[] v1 = { 0 };
//        final byte[] v2 = { 0 };
//        final short[] v3 = { 0 };
//        final long A1 = (long) 0xfee1deadcafebabeL;
//        final byte A2 = (byte) 0x11;
//        final short A3 = (short) 0x2222;
//        Callable closure = new Callable() {
//
//            public void invoke(long a1, byte a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureLBSrV(closure, A1, A2, A3);
//        assertTrue("Callable not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
}