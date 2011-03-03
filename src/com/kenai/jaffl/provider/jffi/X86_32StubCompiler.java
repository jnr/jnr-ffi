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

package com.kenai.jaffl.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Internals;
import static com.kenai.jnr.x86asm.Asm.*;
import com.kenai.jnr.x86asm.Assembler;
import static com.kenai.jaffl.provider.jffi.CodegenUtils.*;

/**
 * Stub compiler for i386 unix
 */
final class X86_32StubCompiler extends AbstractX86StubCompiler {

    @Override
    boolean canCompile(Class returnType, Class[] parameterTypes, CallingConvention convention) {
        if (returnType != byte.class && returnType != short.class && returnType != int.class
                && returnType != long.class && returnType != float.class && returnType != double.class
                && returnType != void.class) {
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

        return true;
    }

    @Override
    void compile(Function function, String name, Class returnType, Class[] parameterTypes, CallingConvention convention, boolean saveErrno) {

        int psize = 0;
        for (Class t : parameterTypes) {
            if (t == byte.class || t == short.class || t == int.class || t == float.class) {
                psize += 4;
            } else if (t == long.class || t == double.class) {
                psize += 8;
            } else {
                throw new IllegalArgumentException("invalid parameter type" + t);
            }
        }

        int rsize = 0;
        if (double.class == returnType || float.class == returnType) {
            rsize = 16;
        } else if (long.class == returnType) {
            rsize = 8;
        } else if (byte.class == returnType || short.class == returnType || int.class == returnType) {
            rsize = 4;
        } else if (void.class == returnType) {
            rsize = 0;
        } else {
            throw new IllegalArgumentException("invalid return type " + returnType);
        }
        
        //
        // JNI functions all look like:
        // foo(JNIEnv* env, jobject self, arg...)

        // We need to align the stack to 16 bytes, then copy all the old args
        // into the new parameter space.
        // It already has 4 bytes pushed (the return address) so we need to account for that.
        //        
        final int stackadj = align(Math.max(psize, rsize) + 4, 16) - 4;

        Assembler a = new Assembler(X86_32);

        a.sub(esp, imm(stackadj));

        // memcpy the parameters from the orig stack to the new location
        for (int i = 0; i < psize; i += 4)  {
            a.mov(eax, dword_ptr(esp, stackadj + 4 + 8 + i));
            a.mov(dword_ptr(esp, i), eax);
        }

        // Call to the actual native function
        a.mov(eax, imm(function.getFunctionAddress()));
        a.call(eax);
        
        if (saveErrno) {
            int save = 0;
            if (float.class == returnType) {
                a.fstp(dword_ptr(esp, save));
            } else if (double.class == returnType) {
                a.fstp(qword_ptr(esp, save));
            } else if (long.class == returnType) {
                a.mov(dword_ptr(esp, save), eax);
                a.mov(dword_ptr(esp, save + 4), edx);
            } else if (void.class == returnType) {
                // Do nothing for void values
            } else {
                a.mov(dword_ptr(esp, save), eax);
            }

            // Save the errno in a thread-local variable
            a.mov(eax, imm(errnoFunctionAddress));
            a.call(eax);

            // Retrieve return value and put it back in the appropriate return register
            if (float.class == returnType) {
                a.fld(dword_ptr(esp, save));

            } else if (double.class == returnType) {
                a.fld(qword_ptr(esp, save));

            } else if (long.class == returnType) {
                a.mov(eax, dword_ptr(esp, save));
                a.mov(edx, dword_ptr(esp, save + 4));

            } else if (void.class == returnType) {
                // Do nothing for void values

            } else {
                a.mov(eax, dword_ptr(esp, save));
            }
        }

        // Restore esp to the original position and return
        a.add(esp, imm(stackadj));
        a.ret();

        stubs.add(new Stub(name, sig(returnType, parameterTypes), a));
    }

}
