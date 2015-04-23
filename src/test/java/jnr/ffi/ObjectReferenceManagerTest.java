/* 
 * Copyright (C) 2012 Wayne Meissner
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
