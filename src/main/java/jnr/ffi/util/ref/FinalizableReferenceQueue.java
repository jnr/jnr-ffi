/*
 * Copyright (C) 2007 The Guava Authors
 * Modified by the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.util.ref;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A reference queue that invokes {@link FinalizableReference#finalizeReferent()} on the references
 * enqueued in it.
 *
 * <p>The references are finalized on the thread that creates the next reference with this queue,
 * not on a thread of their own. A finalizer thread would keep the class loader of this library
 * alive, so an application that loads it in a class loader of its own could never unload it.
 *
 * <p>Keep a strong reference to this object until all of the associated referents have been
 * finalized. If this object is garbage collected earlier, {@code finalizeReferent()} will not be
 * invoked on the remaining references.
 *
 * @author Bob Lee
 * @since 2.0 (imported from Google Collections Library)
 */
public class FinalizableReferenceQueue {

  private static final Logger logger = Logger.getLogger(FinalizableReferenceQueue.class.getName());

  private static final Map<FinalizableReferenceQueue, Boolean> finalizerQueues
          = Collections.synchronizedMap(new WeakHashMap<FinalizableReferenceQueue, Boolean>());

  /**
   * The actual reference queue that new references are registered with.
   */
  final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

  /**
   * Whether a thread is finalizing references from this queue.
   */
  private final AtomicBoolean finalizing = new AtomicBoolean();

  /**
   * Constructs a new queue.
   */
  public FinalizableReferenceQueue() {
    finalizerQueues.put(this, Boolean.TRUE);
  }

  /**
   * Repeatedly dequeues references from the queue and invokes {@link
   * FinalizableReference#finalizeReferent()} on them until the queue is empty. This method is a
   * no-op if another call is already finalizing references from this queue.
   */
  void cleanUp() {
    // Some finalizeReferent() implementations create references themselves, JRuby's FFI memory
    // allocator while holding a lock. If creating those references finalized the remaining ones,
    // the stack would grow with every enqueued reference, and two threads finalizing at the same
    // time could each hold the lock that the other's next finalizeReferent() needs.
    if (!finalizing.compareAndSet(false, true)) {
      return;
    }
    try {
      Reference<?> reference;
      while ((reference = queue.poll()) != null) {
        /*
         * This is for the benefit of phantom references. Weak and soft references will have already
         * been cleared by this point.
         */
        reference.clear();
        try {
          ((FinalizableReference) reference).finalizeReferent();
        } catch (Throwable t) {
          logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
        }
      }
    } finally {
      finalizing.set(false);
    }
  }

  public static void cleanUpAll() {
    try {
      // Iterate over a new array containing all the queues, so we don't risk a concurrent modification exception
      for (Object frq : finalizerQueues.keySet().toArray()) {
        ((FinalizableReferenceQueue) frq).cleanUp();
      }
    } catch (Throwable t) {}
  }
}
