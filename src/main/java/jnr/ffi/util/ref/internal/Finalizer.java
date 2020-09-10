/*
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.util.ref.internal;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread that finalizes referents. All references should implement
 * {@code com.google.common.base.FinalizableReference}.
 *
 * <p>While this class is public, we consider it to be *internal* and not part
 * of our published API. It is public so we can access it reflectively across
 * class loaders in secure environments.
 * </p>
 * <p>This class can't depend on other Google Collections code. If we were
 * to load this class in the same class loader as the rest of
 * Google Collections, this thread would keep an indirect strong reference
 * to the class loader and prevent it from being garbage collected. This
 * poses a problem for environments where you want to throw away the class
 * loader. For example, dynamically reloading a web application or unloading
 * an OSGi bundle.
 * </p>
 * <p>{@code com.google.common.base.FinalizableReferenceQueue} loads this class
 * in its own class loader. That way, this class doesn't prevent the main
 * class loader from getting garbage collected, and this class can detect when
 * the main class loader has been garbage collected and stop itself.
 * </p>
 */
public class Finalizer implements Runnable {

    private static final Logger logger
            = Logger.getLogger(Finalizer.class.getName());

    /**
     * Name of FinalizableReference.class.
     */
    private static final String FINALIZABLE_REFERENCE
            = "jnr.ffi.util.ref.FinalizableReference";
    private Thread thread;

    /**
     * Starts the Finalizer thread. FinalizableReferenceQueue calls this method
     * reflectively.
     *
     * @param finalizableReferenceClass FinalizableReference.class
     * @param frq                       reference to instance of FinalizableReferenceQueue that started
     *                                  this thread
     * @return ReferenceQueue which Finalizer will poll
     */
    public static ReferenceQueue<Object> startFinalizer(
            Class<?> finalizableReferenceClass, Object frq) {
    /*
     * We use FinalizableReference.class for two things:
     *
     * 1) To invoke FinalizableReference.finalizeReferent()
     *
     * 2) To detect when FinalizableReference's class loader has to be garbage
     * collected, at which point, Finalizer can stop running
     */
        if (!finalizableReferenceClass.getName().equals(FINALIZABLE_REFERENCE)) {
            throw new IllegalArgumentException(
                    "Expected " + FINALIZABLE_REFERENCE + ".");
        }

        Finalizer finalizer = new Finalizer(finalizableReferenceClass, frq);
        finalizer.start();
        return finalizer.queue;
    }

    private final WeakReference<Class<?>> finalizableReferenceClassReference;
    private final PhantomReference<Object> frqReference;
    private final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

    private static final Field inheritableThreadLocals;
    private static final Constructor<Thread> inheritableThreadlocalsConstructor;

    static {
        // Try the constructor first because it is cleaner and doesn't produce warnings on Java 9.
        Constructor<Thread> itlc = null;
        try {
            itlc = getInheritableThreadLocalsConstructor();
        } catch (Throwable t) {
        }

        Field itl = null;
        if (itlc == null) {
            try {
                itl = getInheritableThreadLocalsField();
            } catch (Throwable t) {
            }
        }

        inheritableThreadLocals = itl;
        inheritableThreadlocalsConstructor = itlc;

        if (itl == null && itlc == null) {
            logger.log(Level.INFO, "Couldn't access Thread.inheritableThreadLocals or appropriate constructor."
                    + " Reference finalizer threads will inherit thread local values.");
        }
    }

    /**
     * Constructs a new finalizer thread.
     */
    private Finalizer(Class<?> finalizableReferenceClass, Object frq) {
        this.finalizableReferenceClassReference
                = new WeakReference<Class<?>>(finalizableReferenceClass);

        // Keep track of the FRQ that started us so we know when to stop.
        this.frqReference = new PhantomReference<Object>(frq, queue);
    }

    public void start() {
        if (inheritableThreadlocalsConstructor != null) {
            try {
                this.thread = inheritableThreadlocalsConstructor.newInstance(
                        Thread.currentThread().getThreadGroup(),
                        this,
                        Finalizer.class.getName(),
                        0,
                        false
                );
            } catch (Throwable t) {
                logger.log(Level.INFO, "Failed to disable thread local values inherited"
                        + " by reference finalizer thread.", t);

                // fall through and try field tweak
            }
        }

        if (this.thread == null) {
            this.thread = new Thread(this, Finalizer.class.getName());
            if (inheritableThreadLocals != null) {
                try {
                    inheritableThreadLocals.set(this.thread, null);
                } catch (Throwable t) {
                    logger.log(Level.INFO, "Failed to clear thread local values inherited"
                            + " by reference finalizer thread.", t);
                }
            }
        }

        thread.setDaemon(true);
        thread.setPriority(Thread.MAX_PRIORITY);
        // Set the context class loader to null in order to avoid
        // keeping a strong reference to an application classloader.
        thread.setContextClassLoader(null);
        thread.start();
    }

    /**
     * Loops continuously, pulling references off the queue and cleaning them up.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            try {
                if (!cleanUp(queue.remove())) {
                    break;
                }
            } catch (InterruptedException e) { /* ignore */ }
        }
    }

    /**
     * Cleans up a single reference. Catches and logs all throwables.
     */
    private boolean cleanUp(Reference<?> reference) {
        Method finalizeReferentMethod = getFinalizeReferentMethod();
        if (finalizeReferentMethod == null) {
            return false;
        }
        do {
      /*
       * This is for the benefit of phantom references. Weak and soft
       * references will have already been cleared by this point.
       */
            reference.clear();

            if (reference == frqReference) {
        /*
         * The client no longer has a reference to the
         * FinalizableReferenceQueue. We can stop.
         */
                return false;
            }

            try {
                finalizeReferentMethod.invoke(reference);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
            }

      /*
       * Loop as long as we have references available so as not to waste
       * CPU looking up the Method over and over again.
       */
        } while ((reference = queue.poll()) != null);
        return true;
    }

    /**
     * Looks up FinalizableReference.finalizeReferent() method.
     */
    private Method getFinalizeReferentMethod() {
        Class<?> finalizableReferenceClass
                = finalizableReferenceClassReference.get();
        if (finalizableReferenceClass == null) {
      /*
       * FinalizableReference's class loader was reclaimed. While there's a
       * chance that other finalizable references could be enqueued
       * subsequently (at which point the class loader would be resurrected
       * by virtue of us having a strong reference to it), we should pretty
       * much just shut down and make sure we don't keep it alive any longer
       * than necessary.
       */
            return null;
        }
        try {
            return finalizableReferenceClass.getMethod("finalizeReferent");
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }


    public static Field getInheritableThreadLocalsField() {
        try {
            Field inheritableThreadLocals
                    = Thread.class.getDeclaredField("inheritableThreadLocals");
            inheritableThreadLocals.setAccessible(true);
            return inheritableThreadLocals;
        } catch (Throwable t) {
            return null;
        }
    }

    public static Constructor<Thread> getInheritableThreadLocalsConstructor() {
        try {
            return Thread.class.getConstructor(ThreadGroup.class, Runnable.class, String.class, long.class, boolean.class);
        } catch (Throwable t) {
            return null;
        }
    }
}
