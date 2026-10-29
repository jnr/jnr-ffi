package jnr.ffi.util.ref;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FinalizableReferenceQueueTest {

    // referent of the references that only make the queue finalize the others
    private final Object kept = new Object();

    @Test public void referencesAreFinalizedWhenTheNextReferenceIsCreated() throws Exception {
        FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
        AtomicInteger finalized = new AtomicInteger();
        Reference<Object> reference = new CountingReference(new Object(), queue, finalized);
        awaitEnqueued(reference);

        assertEquals(0, finalized.get(), "finalized before the next reference was created");
        assertTrue(reference.isEnqueued(), "reference was not enqueued");

        new CountingReference(kept, queue, finalized);

        assertEquals(1, finalized.get());
    }

    @Test public void referencesCreatedWhileFinalizingDoNotFinalizeOthers() throws Exception {
        final FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
        final AtomicInteger finalized = new AtomicInteger();
        final AtomicInteger depth = new AtomicInteger();
        final AtomicInteger maxDepth = new AtomicInteger();
        List<Object> referents = new ArrayList<Object>();
        List<Reference<Object>> references = new ArrayList<Reference<Object>>();
        for (int i = 0; i < 1000; i++) {
            referents.add(new Object());
            references.add(new FinalizableWeakReference<Object>(referents.get(i), queue) {
                public void finalizeReferent() {
                    maxDepth.set(Math.max(maxDepth.get(), depth.incrementAndGet()));
                    new CountingReference(kept, queue, finalized);
                    finalized.incrementAndGet();
                    depth.decrementAndGet();
                }
            });
        }
        referents.clear();
        for (Reference<Object> reference : references) {
            awaitEnqueued(reference);
            assertTrue(reference.isEnqueued(), "reference was not enqueued");
        }

        new CountingReference(kept, queue, finalized);

        assertEquals(references.size(), finalized.get());
        assertEquals(1, maxDepth.get(), "finalizeReferent() ran inside another finalizeReferent()");
    }

    @Test public void exceptionsFromFinalizeReferentDoNotReachTheNextReference() throws Exception {
        FinalizableReferenceQueue queue = new FinalizableReferenceQueue();
        AtomicInteger finalized = new AtomicInteger();
        Object referent = new Object();
        Reference<Object> failing = new FinalizableWeakReference<Object>(referent, queue) {
            public void finalizeReferent() {
                throw new IllegalStateException("finalizeReferent() failed");
            }
        };
        Reference<Object> counting = new CountingReference(referent, queue, finalized);
        referent = null;
        awaitEnqueued(failing);
        awaitEnqueued(counting);
        assertTrue(failing.isEnqueued() && counting.isEnqueued(), "references were not enqueued");

        Logger logger = Logger.getLogger(FinalizableReferenceQueue.class.getName());
        Level level = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            new CountingReference(kept, queue, finalized);
        } finally {
            logger.setLevel(level);
        }

        assertEquals(1, finalized.get());
    }

    /**
     * A queue in a static field, the way it's used, must not keep the class loader of this library alive once nothing
     * else uses it. Here that class loader is a disposable one, like that of an application that loads this library
     * in a class loader of its own.
     */
    @Test public void queueDoesNotKeepItsClassLoaderAlive() throws Exception {
        WeakReference<ClassLoader> loader = createQueueInDisposableLoader();
        for (int i = 0; i < 10 && loader.get() != null; i++) {
            System.gc();
            Thread.sleep(100);
        }

        assertNull(loader.get(), "class loader was not collected after creating a queue");
    }

    static final class StaticQueue {
        static final FinalizableReferenceQueue QUEUE = new FinalizableReferenceQueue();
    }

    private static WeakReference<ClassLoader> createQueueInDisposableLoader() throws Exception {
        List<URL> classpath = new ArrayList<URL>();
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            classpath.add(new File(path).toURI().toURL());
        }
        URLClassLoader loader = new URLClassLoader(classpath.toArray(new URL[0]), ClassLoader.getSystemClassLoader().getParent());
        Class.forName(StaticQueue.class.getName(), true, loader);
        loader.close();

        return new WeakReference<ClassLoader>(loader);
    }

    private static void awaitEnqueued(Reference<?> reference) throws InterruptedException {
        for (int i = 0; i < 100 && !reference.isEnqueued(); i++) {
            System.gc();
            Thread.sleep(10);
        }
    }

    private static final class CountingReference extends FinalizableWeakReference<Object> {
        private final AtomicInteger finalized;

        CountingReference(Object referent, FinalizableReferenceQueue queue, AtomicInteger finalized) {
            super(referent, queue);
            this.finalized = finalized;
        }

        public void finalizeReferent() {
            finalized.incrementAndGet();
        }
    }
}
