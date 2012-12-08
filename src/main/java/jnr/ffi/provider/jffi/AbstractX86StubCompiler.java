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

package jnr.ffi.provider.jffi;

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.NativeMethod;
import com.kenai.jffi.NativeMethods;
import com.kenai.jffi.PageManager;
import jnr.ffi.*;
import jnr.ffi.Runtime;
import jnr.x86asm.Assembler;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for most X86_32/X86_64 stub compilers
 */
abstract class AbstractX86StubCompiler extends StubCompiler {
    public final static boolean DEBUG = Boolean.getBoolean("jnr.ffi.compile.dump");
    private final jnr.ffi.Runtime runtime;

    protected AbstractX86StubCompiler(jnr.ffi.Runtime runtime) {
        this.runtime = runtime;
    }

    public final Runtime getRuntime() {
        return runtime;
    }

    private static final class StaticDataHolder {
        // Keep a reference from the loaded class to the pages holding the code for that class.
        static final Map<Class, PageHolder> PAGES
                = Collections.synchronizedMap(new WeakHashMap<Class, PageHolder>());
    }
    final List<Stub> stubs = new LinkedList<Stub>();


    static final class Stub {
        final String name;
        final String signature;
        final Assembler assembler;

        public Stub(String name, String signature, Assembler assembler) {
            this.name = name;
            this.signature = signature;
            this.assembler = assembler;
        }
    }

    static final class PageHolder {
        final PageManager pm;
        final long memory;
        final long pageCount;

        public PageHolder(PageManager pm, long memory, long pageCount) {
            this.pm = pm;
            this.memory = memory;
            this.pageCount = pageCount;
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                pm.freePages(memory, (int) pageCount);
            } catch (Throwable t) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, 
                    "Exception when freeing native pages: %s", t.getLocalizedMessage());
            } finally {
                super.finalize();
            }
        }

    }

    @Override
    void attach(Class clazz) {

        if (stubs.isEmpty()) {
            return;
        }

        long codeSize = 0;
        for (Stub stub : stubs) {
            // add 8 bytes for alignment
            codeSize += stub.assembler.codeSize() + 8;
        }

        PageManager pm = PageManager.getInstance();

        long npages = (codeSize + pm.pageSize() - 1) / pm.pageSize();
        // Allocate some native memory for it
        long code = pm.allocatePages((int) npages, PageManager.PROT_READ | PageManager.PROT_WRITE);
        if (code == 0) {
            throw new OutOfMemoryError("allocatePages failed for codeSize=" + codeSize);
        }
        PageHolder page = new PageHolder(pm, code, npages);

        // Now relocate/copy all the assembler stubs into the real code area
        List<NativeMethod> methods = new ArrayList<NativeMethod>(stubs.size());
        long fn = code;
        PrintStream dbg = System.err;
        System.out.flush(); System.err.flush();

        for (Stub stub : stubs) {
            Assembler asm = stub.assembler;
            // align the start of all functions on a 8 byte boundary
            fn = align(fn, 8);
            ByteBuffer buf = ByteBuffer.allocate(asm.codeSize()).order(ByteOrder.LITTLE_ENDIAN);
            stub.assembler.relocCode(buf, fn);
            buf.flip();
            MemoryIO.getInstance().putByteArray(fn, buf.array(), buf.arrayOffset(), buf.limit());

            if (DEBUG && X86Disassembler.isAvailable()) {

                dbg.println(clazz.getName() + "." + stub.name + " " + stub.signature);
                X86Disassembler disassembler = X86Disassembler.create();
                disassembler.setMode(Platform.getNativePlatform().getCPU() == Platform.CPU.I386
                        ? X86Disassembler.Mode.I386 : X86Disassembler.Mode.X86_64);
                disassembler.setSyntax(X86Disassembler.Syntax.INTEL);
                disassembler.setInputBuffer(MemoryUtil.newPointer(runtime, fn), asm.offset());
                while (disassembler.disassemble()) {
                    dbg.printf("%8x: %s\n", disassembler.offset(), disassembler.insn());
                }
                if (buf.remaining() > asm.offset()) {
                    // libudis86 for some reason cannot understand the code asmjit emits for the trampolines
                    dbg.printf("%8x: <indirect call trampolines>\n", asm.offset());
                }
                dbg.println();
            }
            methods.add(new NativeMethod(fn, stub.name, stub.signature));

            fn += asm.codeSize();
        }

        pm.protectPages(code, (int) npages, PageManager.PROT_READ | PageManager.PROT_EXEC);

        NativeMethods.register(clazz, methods);
        StaticDataHolder.PAGES.put(clazz, page);
    }

    static int align(int offset, int align) {
        return (offset + align - 1) & ~(align - 1);
    }

    static long align(long offset, long align) {
        return (offset + align - 1) & ~(align - 1);
    }
}
