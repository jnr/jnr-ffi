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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


import static com.kenai.jnr.x86asm.Asm.*;
import com.kenai.jnr.x86asm.Assembler;
import com.kenai.jffi.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import static com.kenai.jaffl.provider.jffi.CodegenUtils.*;

/**
 * Compilers method trampoline stubs for x86_64 
 */
final class X86_64StubCompiler extends StubCompiler {
    // Keep a reference from the loaded class to the pages holding the code for that class.
    private static final Map<Class, PageHolder> pages
            = Collections.synchronizedMap(new WeakHashMap<Class, PageHolder>());

    private final List<Stub> stubs = new LinkedList<Stub>();
    

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
    final boolean canCompile(Class returnType, Class[] parameterTypes, CallingConvention convention) {
        
        if (returnType != byte.class && returnType != short.class && returnType != int.class
                && returnType != long.class && returnType != float.class && returnType != double.class) {
            return false;
        }

        // There is only one calling convention; SYSV, so abort if someone tries to use stdcall
        if (convention != CallingConvention.DEFAULT) {
            return false;
        }

        int fCount = 0;
        int iCount = 0;

        for (Class t : parameterTypes) {
            if (t == byte.class || t == short.class || t == int.class || t == long.class) {
                ++iCount;
            } else if (t == float.class || t == double.class) {
                ++fCount;
            } else {
                // Fail on anything else
                return false;
            }
        }

        // We can only safely compile methods with up to 6 integer and 8 floating point parameters
        if (iCount > 6 || fCount > 8) {
            return false;
        }
        
        return true;
    }

    @Override
    final void compile(Function function, String name, Class returnType, Class[] parameterTypes,
            CallingConvention convention, boolean saveErrno) {
        
        int fCount = 0;
        int iCount = 0;

        for (Class t : parameterTypes) {
            if (t == byte.class || t == short.class || t == int.class || t == long.class) {
                ++iCount;
            } else if (t == float.class || t == double.class) {
                ++fCount;
            } else {
                throw new IllegalArgumentException("invalid parameter type");
            }
        }
        
        Assembler a = new Assembler(X86_64);
        
        //
        // JNI functions all look like:
        // foo(JNIEnv* env, jobject self, arg...)
        // on AMD64, those sit in %rdi, %rsi, %rdx, %rcx, %r8 and %r9
        // So we need to shuffle all the integer args up to over-write the
        // env and self arguments
        //
        if (iCount > 0) {
            a.mov(rdi, rdx);
        }
        if (iCount > 1) {
            a.mov(rsi, rcx);
        }
        if (iCount > 2) {
            a.mov(rdx, r8);
        }
        if (iCount > 3) {
            a.mov(rcx, r9);
        }

        // For args 5 & 6 of the function, they would have been pushed on the stack
        if (iCount > 4) {
            a.mov(r8, qword_ptr(rsp, 8));
        }

        if (iCount > 5) {
            a.mov(r9, qword_ptr(rsp, 16));
        }
        if (iCount > 6) {
            throw new IllegalArgumentException("integer argument count > 6");
        }

        // All the integer registers are loaded; there nothing to do for the floating
        // registers, as the first 8 args are already in xmm0..xmm7, so just sanity check
        if (fCount > 8) {
            throw new IllegalArgumentException("float argument count > 8");
        }

        
        if (saveErrno) {
            // Need to align the stack to 16 bytes for function call.
            // It already has 8 bytes pushed (the return address), so making space
            // to save the return value from the function neatly aligns it to 16 bytes
            a.sub(rsp, imm(8));

            // Call to the actual native function
            a.mov(rax, imm(function.getFunctionAddress()));
            a.call(rax);

            // Save the integer return on the stack
            a.mov(qword_ptr(rsp, 0), rax);

            // Save the errno in a thread-local variable
            a.mov(rax, imm(Internals.getErrnoSaveFunction()));
            a.call(rax);
            // Retrieve return value, and restore rsp to original position
            a.pop(rax);
            a.ret();
        } else {
            // Since there is no need to return here to save the errno, and the
            // stack was not modified, we can just do a jmpq directly to the
            // native function, and let it return back to the caller.
            a.mov(rax, imm(function.getFunctionAddress()));
            a.jmp(rax);
        }


        stubs.add(new Stub(name, sig(returnType, parameterTypes), a));
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
        pages.put(clazz, page);
    }

    private static final long align(long offset, long align) {
        return align + ((offset - 1) & ~(align - 1));
    }
}
