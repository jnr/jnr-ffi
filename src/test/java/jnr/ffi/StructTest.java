package jnr.ffi;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StructTest {

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

    private static final jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getSystemRuntime();

    @Test
    public void testAlignA() {
        test(new A(runtime), 4, 8);
    }

    @Test
    public void testAlignB() {
        test(new B(runtime), 4, 8);
    }

    @Test
    public void testAlignC() {
        test(new C(runtime), 4, 8);
    }

    @Test
    public void testAlignD() {
        test(new D(runtime), 8, 24);
    }

    @Test
    public void testAlignE() {
        test(new E(runtime), 8, 32);
    }

    @Test
    public void testAlignArray1() {
        test(new Array1(runtime), 8, 72);
    }

    @Test
    public void testAlignMyLargeInteger() {
        test(new MyLargeInteger(runtime), 8, 8);
    }

    @Test
    public void testAlignF() {
        test(new F(runtime), 8, 16);
    }

    @Test
    public void testAlignG() {
        test(new G(runtime), 8, 24);
    }

    @Test
    public void testAlignArray2() {
        test(new Array2(runtime), 8, 72);
    }

    @Test
    public void testAlignUnion1() {
        test(new Union1(runtime), 8, 8);
    }

    @Test
    public void testAlignH() {
        test(new H(runtime), 1, 3);
    }

    @Test
    public void testAlignUnion2() {
        test(new Union2(runtime), 1, 15);
    }

    @Test
    public void testAlignJ() {
        test(new J(runtime), 2, 6);
    }

    @Test
    public void testAlignUnion3() {
        test(new Union3(runtime), 2, 30);
    }

    private void test(Struct a, int alignment, int sizeof) {
        String name = a.getClass().getSimpleName();
        assertEquals(name + ".alignment()", alignment, Struct.alignment(a));
        assertEquals("sizeof(" + name + ")", sizeof, sizeof(a));
    }

    private int sizeof(Struct struct) {
        int size = Struct.size(struct);
        int align = Struct.alignment(struct);
        return (size + align - 1) & -align;
    }

}
/*
#include <stdio.h>
#include <stddef.h>

struct A {
    int x;
    char y;
};

struct B {
    char x;
    int y;
};

struct C {
    char x;
    char y;
    int z;
};

struct D {
    char x;
    long long y;
    int z;
};

struct E {
    char x;
    D y;
};

struct Array1 {
    D t[3];
};

union MyLargeInteger {

    struct {
        unsigned int LowPart;
        int HighPart;
    } u;
    long long QuadPart;
};

struct F {
    int x;
    MyLargeInteger y;
};

struct G {
    char x;
    MyLargeInteger y;
    int z;
};

struct Array2 {
    G t[3];
};

union Union1 {
    int intVal[2];
    char ch[8];
    MyLargeInteger my;
    short ss[4];
    long long u;
};

struct H {
    char x[3];
};

struct Union2 {
    H x[5];
};

struct J {
    short x;
    char y[3];
};

union Union3 {
    J x[5];
    char y[13];
};

#define DUMP(type)     \
do{         \
typedef struct _AlignType##type { \
    char c;       \
    type d;       \
} AlignType##type;     \
printf("@Test\npublic void testAlign%s() {\ntest(new %s(runtime), %d, %d);\n}\n", #type, #type, offsetof(AlignType##type, d), sizeof(type));\
} while(0)

int main() {
    DUMP(A);
    DUMP(B);
    DUMP(C);
    DUMP(D);
    DUMP(E);
    DUMP(Array1);
    DUMP(MyLargeInteger);
    DUMP(F);
    DUMP(G);
    DUMP(Array2);
    DUMP(Union1);
    DUMP(H);
    DUMP(Union2);
    DUMP(J);
    DUMP(Union3);
}

*/
