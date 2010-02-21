/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.struct;

import com.kenai.jaffl.Runtime;
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
public class AlignmentTest {

    public AlignmentTest() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    class PointerStruct extends Struct {
        public final Signed8 s8 = new Signed8();
        public final Pointer p = new Pointer();
    }

    @Test public void alignPointer() throws Throwable {
        PointerStruct s = new PointerStruct();
        final int SIZE = Runtime.getDefault().addressSize() == 4 ? 8 : 16;
        assertEquals("Incorrect pointer field alignment", SIZE, StructUtil.getSize(s));
    }

}