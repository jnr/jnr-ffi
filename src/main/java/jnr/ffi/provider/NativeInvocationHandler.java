/*
 * Copyright (C) 2008-2010 Wayne Meissner
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

package jnr.ffi.provider;

import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.Platform;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.annotations.Synchronized;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * InvocationHandler used to map invocations on a java interface to the
 * correct native function.
 */
public class NativeInvocationHandler implements InvocationHandler {

    private final InvokerMap invokers;
    private final Library library;
    private final Map<LibraryOption, Object> optionsMap;
    private final Class<?> interfaceClass;
    
    /**
     * Creates a new InvocationHandler instance.
     * 
     * @param library the native library to call
     * @param interfaceClass the interface that defines the methods in the 
     * native library that will be accessed via this handler.
     * @param optionsMap a dictionary of options to apply to this library.
     */
    public NativeInvocationHandler(Library library, Class<?> interfaceClass,
            Map<LibraryOption, ?> optionsMap) {
        this.library = library;
        this.interfaceClass = interfaceClass;
        this.optionsMap = new HashMap<LibraryOption, Object>(optionsMap);
        if (interfaceClass.getAnnotation(StdCall.class) != null) {
            this.optionsMap.put(LibraryOption.CallingConvention, CallingConvention.STDCALL);
        }
        
        invokers = new InvokerMap(interfaceClass.getDeclaredMethods().length);
    }
    
    /**
     * Creates a new InvocationHandler mapping methods in the <tt>interfaceClass</tt>
     * to functions in the native library.
     * @param <T> the type of <tt>interfaceClass</tt>
     * @param libraryName the native library to load
     * @param interfaceClass the interface that contains the native method description
     * @param optionsMap a dictionary of options to apply to this library.
     * @return a new instance of <tt>interfaceClass</tt> that can be used to call
     * functions in the native library.
     */
    public static <T> T wrapInterface(Library library, Class<T> interfaceClass,
            Map<LibraryOption, ?> optionsMap) {
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), 
                new Class[]{ interfaceClass, LoadedLibrary.class },
                new NativeInvocationHandler(library, interfaceClass, optionsMap)));
    }
    
    /**
     * Gets the {@link Invoker} for a method.
     * 
     * @param method the method defined in the interface class
     * @return the <tt>Invoker</tt> to use to invoke the native function
     */
    private Invoker getInvoker(Method method) {
        Invoker invoker = invokers.get(method);
        if (invoker != null) {
            return invoker;
        }
        return createInvoker(method);
    }
    
    /**
     * Creates a new <tt>Invoker</tt> to invoke <tt>method</tt> in the native library
     * @param method the method to invoke
     * @return a new <tt>Invoker</tt>
     */
    private synchronized Invoker createInvoker(Method method) {
        //
        // Recheck in case another thread already created the same mapping
        //
        Invoker invoker = invokers.get(method);
        if (invoker != null) {
            return invoker;
        }
        invoker = library.getInvoker(method, optionsMap);
        
        //
        // If either the method or the library is specified as requiring
        // synchronization, then wrap the raw invoker in a synchronized proxy
        //
        if (method.getAnnotation(Synchronized.class) != null
                || interfaceClass.getAnnotation(Synchronized.class) != null) {
            //
            // Synchronize on the library, so multiple different interface
            // definitions for the same library synchronize on the same object.
            //
            invoker = new SynchronizedInvoker(invoker, library.libraryLock());
        }
        
        invokers.put(method, invoker);
        return invoker;
    }
    
    public Object invoke(Object self, Method method, Object[] argArray) throws Throwable {
        return getInvoker(method).invoke(argArray);
    }
    
    private static final class SynchronizedInvoker implements Invoker {
        private final Object lock;
        private final Invoker invoker;
        public SynchronizedInvoker(Invoker invoker, Object lock) {
            this.invoker = invoker;
            this.lock = lock;
        }
        public Object invoke(Object[] parameters) {
            synchronized (lock) {
                return invoker.invoke(parameters);
            }
        }
    }
    /**
     * A custom map-like class for fast mapping between a <tt>Method</tt> and
     * an <tt>Invoker</tt>.
     * <p>
     * This is not a standard <tt>Map</tt> - it does not support removing or
     * replacing of elements, just gets and insertions.
     * </p>
     * <p>
     * It is optimized for many-reads, minimal writes.
     * </p>
     */
    private static final class InvokerMap {
        //
        // According to the new JMM:
        // In effect, because the new memory model places stricter constraints 
        // on reordering of volatile field accesses with other field accesses, 
        // volatile or not, anything that was visible to thread A when it writes
        // to volatile field f becomes visible to thread B when it reads f. 
        //
        // Ergo, by doing copy-on-write of the entire array to a temp variable, 
        // then inserting into the temp array, and assigning that temp array 
        // to 'entries' after all writes are done, will ensure that everything
        // is fully visible to readers.
        //
        private volatile Object[] entries;
        private static final float loadFactor = 0.5f;
        
        /**
         * Creates a new <tt>Method->Invoker</tt> map
         * 
         * @param maxEntries the maximum size of the map.  No more entries than 
         * this can be added.
         */
        public InvokerMap(final int maxEntries) {
            final int capacity = (int) ((float) maxEntries / loadFactor) + 1;
            int size = 1;
            // Make the table power-of-2 sized so indexFor() is faster
            while (size < capacity) {
                size <<= 1;
            }
            entries = new Object[size * 2]; // write volatile
        }
        
        /**
         * Creates a new mapping between a <tt>Method</tt> and an <tt>Invoker</tt>
         * 
         * @param method the <tt>Method<tt> to create the mapping for
         * @param invoker the <tt>Invoker</tt> to associate with the method.
         */
        public final synchronized void put(Method method, Invoker invoker) {
            Object[] tmp = new Object[entries.length];
            System.arraycopy(entries, 0, tmp, 0, tmp.length);
            int start = indexFor(method, tmp.length);
            Loop: for (int loop = 0; loop < 2; ++loop) {    
                for (int i = start; i < tmp.length - 1; i += 2) {
                    if (tmp[i] == null) {
                        tmp[i] = method;
                        tmp[i + 1] = invoker;
                        break Loop;
                    }
                }
                start = 0; // wrap around
            }
            entries = tmp; // write volatile
        }
        
        /**
         * Gets an <tt>Invoker</tt> from the map for <tt>method</tt>
         * 
         * @param method the method to get an <tt>Invoker</tt> for
         * @return an Invoker if the method is in the table, else null
         */
        public final Invoker get(Method method) {
            Object[] tmp = entries; // read volatile
            int start = indexFor(method, tmp.length);
            Loop: for (int loop = 0; loop < 2; ++loop) {
                for (int i = start; i < tmp.length - 1; i += 2) {
                    if (tmp[i] == method) {
                        return (Invoker) tmp[i + 1];
                    }
                    if (tmp[i] == null) {
                        break Loop;
                    }
                }
                start = 0; // wrap around
            }
            return null;
        }
        
        /**
         * Abstracts different hashing methods, depending on the underlying jvm
         */
        private static interface Hasher {
            public int hash(Method key);
        }
        
        /**
         * Uses System.identityHashCode() to generate a hash for the method.
         * <p>
         * This is fastest on JDK6+ where System.identityHashCode() is an intrinsic
         * and is inlined.
         * </p>
         */
        private static final class IdentityHasherSingleton {
            private static final class IdentityHasher implements Hasher {
                public final int hash(Method key) {
                    final int h = System.identityHashCode(key);
                    return (h << 1) - (h << 8);
                }
            }
            public static final Hasher getInstance() { return new IdentityHasher(); }
        }
        
        /**
         * Uses Method#hashCode to generate a hash for the method.
         * <p>
         * This is faster than using System.identityHashCode() on JDK5, since
         * System.identityHashCode() is not inlined.
         * </p>
         */
        private static final class NameHasherSingleton {
            private static final class NameHasher implements Hasher {
                public final int hash(Method key) {
                    int h = key.hashCode();
                    h += (h <<  15) ^ 0xffffcd7d;
                    h ^= (h >>> 10);
                    h += (h <<   3);
                    h ^= (h >>>  6);
                    h += (h <<   2) + (h << 14);
                    return h ^ (h >>> 16);
                }
            }
            public static final Hasher getInstance() { return new NameHasher(); }
        }
        
        //
        // On JVM6 and above, System.identityHashCode is inlined so very fast
        //
        private static final Hasher hasher = isJava6()
                ? IdentityHasherSingleton.getInstance() : NameHasherSingleton.getInstance();
        
        /**
         * Gets the index in the hash table of the method.
         * 
         * @param key the method to locate
         * @param length the length of the entries array
         * @return
         */
        private static final int indexFor(Method key, int length) {
            return hasher.hash(key) & (length - 1);
        }
    }

    
    private static final boolean isJava6() {
        try {
            String versionString = System.getProperty("java.version");
            if (versionString != null) {
                String[] v = versionString.split("\\.");
                return Integer.valueOf(v[1]) >= 6;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}