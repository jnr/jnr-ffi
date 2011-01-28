
package com.kenai.jaffl.byref;


import com.kenai.jaffl.Address;
import com.kenai.jaffl.TstUtil;
import com.kenai.jaffl.annotations.In;
import com.kenai.jaffl.annotations.Out;
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
public class AddressByReferenceTest {
    public AddressByReferenceTest() {
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
    public static interface TestLib {
        Address ptr_ret_pointer(AddressByReference p, int offset);
        void ptr_set_pointer(AddressByReference p, int offset, Address value);
    }
    public static interface TestLibInOnly {
        Address ptr_ret_pointer(@In AddressByReference p, int offset);
        void ptr_set_pointer(@In AddressByReference p, int offset, Address value);
    }
    public static interface TestLibOutOnly {
        Address ptr_ret_pointer(@Out AddressByReference p, int offset);
        void ptr_set_pointer(@Out AddressByReference p, int offset, Address value);
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test public void dummy() {}
    @Test public void inOnlyReferenceSet() {
        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
        final Address MAGIC = new Address(0xdeadbeef);
        AddressByReference ref = new AddressByReference(MAGIC);
        assertEquals("Wrong value passed", MAGIC, lib.ptr_ret_pointer(ref, 0));
    }
//    @Test public void inOnlyIntReferenceNotWritten() {
//        TestLibInOnly lib = TstUtil.loadTestLib(TestLibInOnly.class);
//        final Address MAGIC = new Address(0xdeadbeef);
//        AddressByReference ref = new AddressByReference(MAGIC);
//        lib.ptr_set_pointer(ref, 0, new Address(0));
//        assertEquals("Int reference written when it should not be", MAGIC, ref.getValue());
//    }
//    @Test public void outOnlyIntReferenceNotRead() {
//        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
//        final Address MAGIC = new Address(0xdeadbeef);
//        AddressByReference ref = new AddressByReference(MAGIC);
//        assertTrue("Reference value passed to native code when it should not be", MAGIC != lib.ptr_ret_pointer(ref, 0));
//    }
//    @Test public void outOnlyIntReferenceGet() {
//        TestLibOutOnly lib = TstUtil.loadTestLib(TestLibOutOnly.class);
//        final Address MAGIC = new Address(0xdeadbeef);
//        AddressByReference ref = new AddressByReference(new Address(0));
//        lib.ptr_set_pointer(ref, 0, MAGIC);
//        assertEquals("Reference value not set", MAGIC, ref.getValue());
//    }
}