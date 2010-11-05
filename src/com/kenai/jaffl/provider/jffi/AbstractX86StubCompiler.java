/*
 * Copyright (C) 2010 Wayne Meissner
 *
 * This file is part of jnr.
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

package com.kenai.jaffl.provider.jffi;

import com.kenai.jffi.MemoryIO;
import com.kenai.jffi.NativeMethod;
import com.kenai.jffi.NativeMethods;
import com.kenai.jffi.PageManager;
import com.kenai.jnr.x86asm.Assembler;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Base class for most X86_32/X86_64 stub compilers
 */
abstract class AbstractX86StubCompiler extends StubCompiler {
    private static final class StaticDataHolder {
        // Keep a reference from the loaded class to the pages holding the code for that class.
        static final Map<Class, PageHolder> PAGES
                = Collections.synchronizedMap(new WeakHashMap<Class, PageHolder>());
    }
    final List<Stub> stubs = new LinkedList<Stub>();


    final class Stub {
        final String name;
        final String signature;
        final Assembler assembler;

        public Stub(String name, String signature, Assembler assembler) {
            this.name = name;
            this.signature = signature;
            this.assembler = assembler;
        }
    }

    final class PageHolder {
        final long memory;
        final long pageCount;

        public PageHolder(long memory, long pageCount) {
            this.memory = memory;
            this.pageCount = pageCount;
        }

        @Override
        protected void finalize() throws Throwable {
            PageManager.getInstance().freePages(memory, (int) pageCount);
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
        PageHolder page = new PageHolder(code, npages);

        // Now relocate/copy all the assembler stubs into the real code area
        List<NativeMethod> methods = new ArrayList<NativeMethod>(stubs.size());
        long fn = code;
        for (Stub stub : stubs) {
            Assembler asm = stub.assembler;
            // align the start of all functions on a 8 byte boundary
            fn = align(fn, 8);
            ByteBuffer buf = MemoryIO.getInstance().newDirectByteBuffer(fn, asm.codeSize()).order(ByteOrder.LITTLE_ENDIAN);
            stub.assembler.relocCode(buf, fn);

            methods.add(new NativeMethod(fn, stub.name, stub.signature));
            fn += asm.codeSize();
        }

        pm.protectPages(code, (int) npages, PageManager.PROT_READ | PageManager.PROT_EXEC);

        NativeMethods.register(clazz, methods);
        StaticDataHolder.PAGES.put(clazz, page);
    }

    static final int align(int offset, int align) {
        return align + ((offset - 1) & ~(align - 1));
    }

    static final long align(long offset, long align) {
        return align + ((offset - 1) & ~(align - 1));
    }
}
