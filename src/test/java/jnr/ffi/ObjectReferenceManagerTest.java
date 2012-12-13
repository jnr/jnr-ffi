package jnr.ffi;


import org.junit.Test;
import static org.junit.Assert.*;

public class ObjectReferenceManagerTest {

    @Test public void sameObjectReturned() {
        ObjectReferenceManager<String> referenceManager = ObjectReferenceManager.newInstance(Runtime.getSystemRuntime());
        String bar = "bar";
        Pointer ptr = referenceManager.add(bar);
        assertSame(bar, referenceManager.get(ptr));
    }

    @Test public void differentPointerReturnedForSameObject() {
        ObjectReferenceManager<String> referenceManager = ObjectReferenceManager.newInstance(Runtime.getSystemRuntime());
        String bar = "bar";
        Pointer ptr = referenceManager.add(bar);
        Pointer ptr2 = referenceManager.add(bar);
        assertSame(bar, referenceManager.get(ptr));
        assertSame(bar, referenceManager.get(ptr2));
        assertNotSame(ptr, ptr2);
    }

    @Test public void remove() {
        ObjectReferenceManager<String> referenceManager = ObjectReferenceManager.newInstance(Runtime.getSystemRuntime());
        assertTrue(referenceManager.remove(referenceManager.add("bar")));
    }

    @Test public void referenceEqualityOnly() {
        ObjectReferenceManager<String> referenceManager = ObjectReferenceManager.newInstance(Runtime.getSystemRuntime());
        String bar = "bar";
        String bar2 = new String("bar");
        Pointer ptr = referenceManager.add(bar);
        Pointer ptr2 = referenceManager.add(bar2);
        assertNotSame(ptr, ptr2);
        assertNotEquals(ptr, ptr2);
        assertSame(bar, referenceManager.get(ptr));
        assertSame(bar2, referenceManager.get(ptr2));
    }
}
