
package com.kenai.jaffl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wayne
 */
public class ClosureTest {

    public ClosureTest() {
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
    // FIXME: re-enable when closure/callbacks are working
    @Test public void nop() {}
    public static interface TestLib {
//        public static interface ClosureVrV extends Closure {
//            public void invoke();
//        }
//        void testClosureVrV(Closure closure);
//        public static interface ClosureVrB extends Closure {
//            public byte invoke();
//        }
//        byte testClosureVrB(Closure closure);
//        public static interface ClosureVrS extends Closure {
//            public short invoke();
//        }
//        short testClosureVrS(Closure closure);
//        int testClosureVrI(Closure closure);
//        long testClosureVrL(Closure closure);
//        float testClosureVrF(Closure closure);
//        public interface ClosureVrD extends Closure {
//            public double invoke();
//        }
//        double testClosureVrD(Closure closure);
//
//        public interface ClosureIrV extends Closure {
//            public void invoke(int a1);
//        }
//        void testClosureBrV(Closure closure, byte a1);
//        void testClosureSrV(Closure closure, short a1);
//        void testClosureIrV(Closure closure, int a1);
//        void testClosureLrV(Closure closure, long a1);
//        void testClosureFrV(Closure closure, float a1);
//        void testClosureDrV(Closure closure, double a1);
//
//        // closures with small-big-small arguments
//        void testClosureBSBrV(Closure closure, byte a1, short a2, byte a3);
//        void testClosureBIBrV(Closure closure, byte a1, int a2, byte a3);
//        void testClosureBLBrV(Closure closure, byte a1, long a2, byte a3);
//        void testClosureBFBrV(Closure closure, byte a1, float a2, byte a3);
//        void testClosureBDBrV(Closure closure, byte a1, double a2, byte a3);
//
//        void testClosureSBSrV(Closure closure, short a1, byte a2, short a3);
//        void testClosureSISrV(Closure closure, short a1, int a2, short a3);
//        void testClosureSLSrV(Closure closure, short a1, long a2, short a3);
//        void testClosureSFSrV(Closure closure, short a1, float a2, short a3);
//        void testClosureSDSrV(Closure closure, short a1, double a2, short a3);
//
//        // Now big-smaller-smaller
//        void testClosureLSBrV(Closure closure, long a1, short a2, byte a3);
//        // big-smaller-small
//        void testClosureLBSrV(Closure closure, long a1, byte a2, short a3);
    }
//    @Test
//    public void closureVrV() {
//        final boolean[] called = { false };
//        final TestLib.ClosureVrV closure = new TestLib.ClosureVrV() {
//
//            public void invoke() {
//                called[0] = true;
//            }
//        };
//        lib.testClosureVrV(closure);
//        assertTrue("Closure not called", called[0]);
//    }
//    @Test
//    public void closureVrB() {
//        final boolean[] called = { false };
//        final byte MAGIC = (byte) 0xfe;
//        TestLib.ClosureVrB closure = new TestLib.ClosureVrB() {
//
//            public byte invoke() {
//                called[0] = true;
//                return MAGIC;
//            }
//        };
//        byte retVal = lib.testClosureVrB(closure);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Incorrect return value from closure", MAGIC, retVal);
//    }
//    @Test
//    public void closureVrS() {
//        final boolean[] called = { false };
//        final short MAGIC = (short) 0xfee1;
//        TestLib.ClosureVrS closure = new TestLib.ClosureVrS() {
//
//            public short invoke() {
//                called[0] = true;
//                return MAGIC;
//            }
//        };
//        short retVal = lib.testClosureVrS(closure);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Incorrect return value from closure", MAGIC, retVal);
//    }
//    @Test
//    public void closureVrI() {
//        final boolean[] called = { false };
//        final int MAGIC = (int) 0xfee1dead;
//        Closure closure = new Closure() {
//
//            public int invoke() {
//                called[0] = true;
//                return MAGIC;
//            }
//        };
//        int retVal = lib.testClosureVrI(closure);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Incorrect return value from closure", MAGIC, retVal);
//    }
//    @Test
//    public void closureVrL() {
//        final boolean[] called = { false };
//        final long MAGIC = 0xfee1deadcafebabeL;
//        Closure closure = new Closure() {
//
//            public long invoke() {
//                called[0] = true;
//                return MAGIC;
//            }
//        };
//        long retVal = lib.testClosureVrL(closure);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Incorrect return value from closure", MAGIC, retVal);
//    }
//    @Test
//    public void closureVrF() {
//        final boolean[] called = { false };
//        final float MAGIC = (float) 0xfee1dead;
//        Closure closure = new Closure() {
//
//            public float invoke() {
//                called[0] = true;
//                return MAGIC;
//            }
//        };
//        float retVal = lib.testClosureVrF(closure);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Incorrect return value from closure", MAGIC, retVal, 0f);
//    }
//    @Test
//    public void closureVrD() {
//        final boolean[] called = { false };
//        final double MAGIC = (double) 0xfee1dead;
//        TestLib.ClosureVrD closure = new TestLib.ClosureVrD() {
//
//            public double invoke() {
//                called[0] = true;
//                return MAGIC;
//            }
//        };
//        double retVal = lib.testClosureVrD(closure);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Incorrect return value from closure", MAGIC, retVal, 0d);
//    }
//    @Test
//    public void closureBrV() {
//        final boolean[] called = { false };
//        final byte[] val = { 0 };
//        final byte MAGIC = (byte) 0xde;
//        Closure closure = new Closure() {
//
//            public void invoke(byte a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureBrV(closure, MAGIC);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
//    }
//    @Test
//    public void closureSrV() {
//        final boolean[] called = { false };
//        final short[] val = { 0 };
//        final short MAGIC = (short) 0xdead;
//        Closure closure = new Closure() {
//
//            public void invoke(short a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureSrV(closure, MAGIC);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
//    }
//    @Test
//    public void closureIrV() {
//        final boolean[] called = { false };
//        final int[] val = { 0 };
//        final int MAGIC = 0xdeadbeef;
//        Closure closure = new Closure() {
//
//            public void invoke(int a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureIrV(closure, MAGIC);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
//    }
//    @Test
//    public void closureLrV() {
//        final boolean[] called = { false };
//        final long[] val = { 0 };
//        final long MAGIC = 0xfee1deadcafebabeL;
//        Closure closure = new Closure() {
//
//            public void invoke(long a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureLrV(closure, MAGIC);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0]);
//    }
//    @Test
//    public void closureFrV() {
//        final boolean[] called = { false };
//        final float[] val = { 0 };
//        final float MAGIC = (float) 0xdeadbeef;
//        Closure closure = new Closure() {
//
//            public void invoke(float a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureFrV(closure, MAGIC);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Wrong value passed to closure", MAGIC, val[0], 0f);
//    }
//    @Test
//    public void closureDrV() {
//        final boolean[] called = { false };
//        final double[] val = { 0 };
//        final double MAGIC = (double) 0xfee1deadcafebabeL;
//        Closure closure = new Closure() {
//
//            public void invoke(double a1) {
//                called[0] = true;
//                val[0] = a1;
//            }
//        };
//        lib.testClosureDrV(closure, MAGIC);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(byte a1, short a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBSBrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(byte a1, int a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBIBrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(byte a1, long a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBLBrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(byte a1, float a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBFBrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(byte a1, double a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureBDBrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(short a1, byte a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSBSrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(short a1, int a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSISrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(short a1, long a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSLSrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(short a1, float a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSFSrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(short a1, double a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureSDSrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(long a1, short a2, byte a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureLSBrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
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
//        Closure closure = new Closure() {
//
//            public void invoke(long a1, byte a2, short a3) {
//                called[0] = true;
//                v1[0] = a1;
//                v2[0] = a2;
//                v3[0] = a3;
//            }
//        };
//        lib.testClosureLBSrV(closure, A1, A2, A3);
//        assertTrue("Closure not called", called[0]);
//        assertEquals("Wrong value passed to closure", A1, v1[0]);
//        assertEquals("Wrong value passed to closure", A2, v2[0]);
//        assertEquals("Wrong value passed to closure", A3, v3[0]);
//    }
}