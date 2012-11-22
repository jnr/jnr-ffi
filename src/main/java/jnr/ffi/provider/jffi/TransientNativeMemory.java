package jnr.ffi.provider.jffi;

import com.kenai.jffi.PageManager;
import jnr.ffi.util.ref.FinalizableWeakReference;

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
        if (size > 256) { /* Only use the transient allocator for small, short lived allocations */
            return new AllocatedDirectMemoryIO(runtime, size, clear);
        }

        Magazine magazine = currentMagazine.get();
        Sentinel sentinel = magazine != null ? magazine.get() : null;
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
            return mem.size == size && mem.address() == address;
        }

        return super.equals(obj);
    }


    public String toString() {
        return String.format(getClass().getName() + " address=%x size=%d", address, size());
    }

    public final void dispose() { /* not-implemented */ }


    private static final class Sentinel {}
    /**
     * Holder for a group of memory allocations.
     */
    private static final class Magazine extends FinalizableWeakReference<Sentinel> {
        private final PageManager pm;
        private final long page;
        private final long end;
        private final int pageCount;
        private long memory;

        Magazine(Sentinel sentinel, PageManager pm, long page, int pageCount) {
            super(sentinel, NativeFinalizer.getInstance().getFinalizerQueue());
            this.pm = pm;
            this.memory = this.page = page;
            this.pageCount = pageCount;
            this.end = memory + (pageCount * pm.pageSize());
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
