/*
 * Copyright (C) 2007-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
