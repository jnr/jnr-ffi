package jnr.ffi;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

public class StructTest {

    public static interface TestLib {
        int getTypeSize(String type);
        int getTypeAlign(String type);
    }
    public static class A extends Struct {
        private Signed32 x = new Signed32();
        private Signed8 y = new Signed8();
        public A(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class B extends Struct {
        private Signed8 x = new Signed8();
        private Signed32 y = new Signed32();
        public B(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class C extends Struct {
        private Signed8 x = new Signed8();
        private Signed8 y = new Signed8();
        private Signed32 z = new Signed32();
        public C(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class D extends Struct {
        private Signed8 x = new Signed8();
        private Signed64 y = new Signed64();
        private Signed32 z = new Signed32();
        public D(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class E extends Struct {
        private Signed8 x = new Signed8();
        private D y = inner(new D(getRuntime()));
        public E(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class Array1 extends Struct {
        private D[] t = array(new D[3]);
        public Array1(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class LARGE_INTEGER_PART extends Struct {
        private Unsigned32 LowPart = new Unsigned32();
        private Signed32 HighPart = new Signed32();
        public LARGE_INTEGER_PART(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class MyLargeInteger extends Union { // union
        private LARGE_INTEGER_PART u = inner(new LARGE_INTEGER_PART(getRuntime()));
        private Signed64 QuadPart = new Signed64();
        public MyLargeInteger(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class F extends Struct {
        private Signed32 x = new Signed32();
        private MyLargeInteger y = inner(new MyLargeInteger(getRuntime()));
        public F(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class G extends Struct {
        private Signed8 x = new Signed8();
        private MyLargeInteger y = inner(new MyLargeInteger(getRuntime()));
        private Signed32 z = new Signed32();
        public G(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class Array2 extends Struct {
        private G[] t = array(new G[3]);
        Array2(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class Union1 extends Union {
        private Signed32[] intVal = array(new Signed32[2]);
        private Signed8[] ch = array(new Signed8[8]);
        private MyLargeInteger my = inner(new MyLargeInteger(getRuntime()));
        private Signed16[] ss = array(new Signed16[4]);
        private Signed64 u = new Signed64();
        public Union1(jnr.ffi.Runtime runtime) {
            super(runtime);
        }
    }
    public static class H extends Struct {
        private Signed8[] x = array(new Signed8[3]);
        public H(Runtime runtime) {
            super(runtime);
        }
    }
    public static class Union2 extends Union {
        private H[] x = array(new H[5]);
        public Union2(Runtime runtime) {
            super(runtime);
        }
    }
    public static class J extends Struct {
        private Signed16 x = new Signed16();
        private Signed8[] y = array(new Signed8[3]);
        public J(Runtime runtime) {
            super(runtime);
        }
    }
    public static class Union3 extends Union {
        private J[] x = array(new J[5]);
        private Signed8[] y = array(new Signed8[13]);
        public Union3(Runtime runtime) {
            super(runtime);
        }
    }

    private static jnr.ffi.Runtime runtime;

    static TestLib testlib;

    @BeforeClass
    public static void setUpClass() {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = jnr.ffi.Runtime.getRuntime(testlib);
    }

    @Test
    public void testAlignA() {
        test(new A(runtime));
    }

    @Test
    public void testAlignB() {
        test(new B(runtime));
    }

    @Test
    public void testAlignC() {
        test(new C(runtime));
    }

    @Test
    public void testAlignD() {
        test(new D(runtime));
    }

    @Test
    public void testAlignE() {
        test(new E(runtime));
    }

    @Test
    public void testAlignArray1() {
        test(new Array1(runtime));
    }

    @Test
    public void testAlignMyLargeInteger() {
        test(new MyLargeInteger(runtime));
    }

    @Test
    public void testAlignF() {
        test(new F(runtime));
    }

    @Test
    public void testAlignG() {
        test(new G(runtime));
    }

    @Test
    public void testAlignArray2() {
        test(new Array2(runtime));
    }

    @Test
    public void testAlignUnion1() {
        test(new Union1(runtime));
    }

    @Test
    public void testAlignH() {
        test(new H(runtime));
    }

    @Test
    public void testAlignUnion2() {
        test(new Union2(runtime));
    }

    @Test
    public void testAlignJ() {
        test(new J(runtime));
    }

    @Test
    public void testAlignUnion3() {
        test(new Union3(runtime));
    }

    private void test(Struct a) {
        String name = a.getClass().getSimpleName();
        int alignment = testlib.getTypeAlign(name);
        int sizeof = testlib.getTypeSize(name);
        assertEquals(name + ".alignment()", alignment, Struct.alignment(a));
        assertEquals("sizeof(" + name + ")", sizeof, sizeof(a));
    }

    private int sizeof(Struct struct) {
        int size = Struct.size(struct);
        int align = Struct.alignment(struct);
        return (size + align - 1) & -align;
    }

}
