/*
 * Copyright (C) 2026 Wayne Meissner
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

import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for premature reclamation of converter-produced native memory while a
 * native call is still in flight.
 *
 * A {@link ToNativeConverter} that returns a freshly allocated direct {@link Pointer} (as
 * jnr-posix's WString does for Windows paths) leaves the returned buffer object as the only
 * reference to the native memory. The generated stub passes the buffer by raw address, so
 * unless the stub keeps the object reachable until the call returns, a GC while the call is
 * in flight lets the transient memory allocator unmap the pages while the native function is
 * still using them. Observed in the wild as EXCEPTION_ACCESS_VIOLATION in _wchmod on Windows
 * under JRuby's bundler (paths marshalled per call via WString).
 *
 * The test drives the same code path through poll(2), whose semantics make the race
 * deterministic instead of a microsecond window: the kernel blocks for the timeout and only
 * then writes revents back into the pollfd buffer, so the buffer must remain valid for the
 * full duration of the call. With an fd of -1, poll is a pure sleep that returns 0 and
 * writes revents = 0. If the buffer's pages have been reclaimed mid-call, poll instead
 * fails with EFAULT.
 */
public class KeepAliveTest {

    /**
     * Mimics jnr-posix's WString: converted to a freshly allocated direct Pointer per call,
     * with nothing else referencing the native memory for the duration of the call. The
     * allocation is small enough to be served by the TransientNativeMemory magazine
     * allocator, whose pages are unmapped once no live buffer object references them.
     */
    public static final class PollFds {
        static final jnr.ffi.Runtime runtime = jnr.ffi.Runtime.getSystemRuntime();

        public static final ToNativeConverter<PollFds, Pointer> Converter = new ToNativeConverter<PollFds, Pointer>() {
            public Pointer toNative(PollFds value, ToNativeContext context) {
                if (value == null) {
                    return null;
                }

                // struct pollfd { int fd; short events; short revents; }
                Pointer memory = Memory.allocateDirect(runtime, 8, true);
                memory.putInt(0, -1);   // fd = -1: ignored by poll, which just sleeps
                memory.putShort(4, (short) 1); // events = POLLIN
                return memory;
            }

            public Class<Pointer> nativeType() {
                return Pointer.class;
            }
        };
    }

    public interface LibC {
        int poll(PollFds fds, int nfds, int timeout);
    }

    private static LibC libc;

    @BeforeAll
    public static void beforeAll() {
        Assumptions.assumeFalse(Platform.getNativePlatform().getOS() == Platform.OS.WINDOWS);

        LibraryLoader<LibC> loader = LibraryLoader.create(LibC.class);
        loader.library(Platform.getNativePlatform().getStandardCLibraryName());
        loader.option(LibraryOption.TypeMapper, new TypeMapper() {
            public FromNativeConverter getFromNativeConverter(Class type) {
                return null;
            }

            public ToNativeConverter getToNativeConverter(Class type) {
                return PollFds.class == type ? PollFds.Converter : null;
            }
        });
        libc = loader.load();
    }

    @Test
    public void parameterMemoryRemainsValidWhileCallInFlight() throws Exception {
        // Warm up until the generated stub is JIT-compiled; interpreted frames pin all
        // locals regardless of liveness, so the reclamation window only opens once the
        // stub is compiled with liveness-based oop maps.
        for (int i = 0; i < 20_000; i++) {
            assertEquals(0, libc.poll(new PollFds(), 1, 0));
        }

        // Hammer the collector while poll blocks in native code holding the buffer's
        // address. Without a keepalive in the generated stub, the buffer's magazine is
        // unmapped mid-call and poll fails with EFAULT when writing revents back.
        final AtomicBoolean done = new AtomicBoolean();
        Thread gcThread = new Thread(new Runnable() {
            public void run() {
                while (!done.get()) {
                    System.gc();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        gcThread.setDaemon(true);
        gcThread.start();

        try {
            for (int i = 0; i < 10; i++) {
                int rc = libc.poll(new PollFds(), 1, 300);
                assertEquals(0, rc, "poll failed (errno=" + jnr.ffi.Runtime.getRuntime(libc).getLastError() + ") - pollfd buffer reclaimed while call in flight");
            }
        } finally {
            done.set(true);
            gcThread.join();
        }
    }
}
