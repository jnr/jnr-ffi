/*
 * Copyright (C) 2012 Wayne Meissner
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

package jnr.ffi.provider.jffi;

import com.kenai.jffi.PageManager;
import jnr.ffi.util.ref.FinalizablePhantomReference;
import jnr.ffi.util.ref.FinalizableReferenceQueue;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class TransientNativeMemory extends DirectMemoryIO {
    /** Keeps strong references to the magazine until cleanup */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<Magazine, Boolean> referenceSet = new ConcurrentHashMap<Magazine, Boolean>();

    private static final ThreadLocal<Magazine> currentMagazine = new ThreadLocal<Magazine>();
    private static final int PAGES_PER_MAGAZINE = 2;

    private final Sentinel sentinel;
    private final int size;
    
    public static DirectMemoryIO allocate(jnr.ffi.Runtime runtime, int size, int align, boolean clear) {
        if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        }

        if (size > 256) { /* Only use the transient allocator for small, short lived allocations */
            return new AllocatedDirectMemoryIO(runtime, size, clear);
        }

        Magazine magazine = currentMagazine.get();
        Sentinel sentinel = magazine != null ? magazine.sentinel() : null;
        long address;

        if (sentinel == null || (address = magazine.allocate(size, align)) == 0) {
            PageManager pm = PageManager.getInstance();
            long memory;
            do {
                memory = pm.allocatePages(PAGES_PER_MAGAZINE, PageManager.PROT_READ | PageManager.PROT_WRITE);
                if (memory != 0L && memory != -1L) {
                    break;
                }

                // No available pages; trigger a full GC to reclaim some memory
                System.gc();
                FinalizableReferenceQueue.cleanUpAll(); 
            } while (true);

            referenceSet.put(magazine = new Magazine(sentinel = new Sentinel(), pm, memory, PAGES_PER_MAGAZINE), Boolean.TRUE);
            currentMagazine.set(magazine);
            address = magazine.allocate(size, align);
        }

        return new TransientNativeMemory(runtime, sentinel, address, size);
    }


    TransientNativeMemory(jnr.ffi.Runtime runtime, Sentinel sentinel, long address, int size) {
        super(runtime, address);
        this.sentinel = sentinel;
        this.size = size;
    }

    private static long align(long offset, long align) {
        return (offset + align - 1L) & ~(align - 1L);
    }


    @Override
    public long size() {
        return this.size;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransientNativeMemory) {
            TransientNativeMemory mem = (TransientNativeMemory) obj;
            return mem.size == size && mem.address() == address();
        }

        return super.equals(obj);
    }

    public final void dispose() { /* not-implemented */ }


    private static final class Sentinel {}
    
    /**
     * Holder for a group of memory allocations.
     */
    private static final class Magazine extends FinalizablePhantomReference<Sentinel> {
        private final Reference<Sentinel> sentinelReference;
        private final PageManager pm;
        private final long page;
        private final long end;
        private final int pageCount;
        private long memory;

        Magazine(Sentinel sentinel, PageManager pm, long page, int pageCount) {
            super(sentinel, NativeFinalizer.getInstance().getFinalizerQueue());
            this.sentinelReference = new WeakReference<Sentinel>(sentinel);
            this.pm = pm;
            this.memory = this.page = page;
            this.pageCount = pageCount;
            this.end = memory + (pageCount * pm.pageSize());
        }
        
        Sentinel sentinel() {
            return sentinelReference.get();
        }

        long allocate(int size, int align) {
            long address = align(this.memory, align);
            if (address + size <= end) {
                memory = address + size;
                return address;
            }

            return 0L;
        }

        public final void finalizeReferent() {
            pm.freePages(page, pageCount);
            referenceSet.remove(this);
        }
    }
}
