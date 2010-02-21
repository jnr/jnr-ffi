
package com.kenai.jaffl.struct;

import com.kenai.jaffl.NativeType;
import com.kenai.jaffl.Runtime;
import com.kenai.jaffl.Type;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class PaddingTest {

    public PaddingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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

    static final class LongPadding extends Struct {
        public final Signed8 s8 = new Signed8();
        public final Padding pad = new Padding(NativeType.SLONG, 3);
    }

    @Test public void longPadding() throws Throwable {
        Type longType = Runtime.getDefault().findType(NativeType.SLONG);
        final int SIZE = longType.alignment() + (longType.size() * 3);
        assertEquals("incorrect size", SIZE, StructUtil.getSize(new LongPadding()));
    }

}