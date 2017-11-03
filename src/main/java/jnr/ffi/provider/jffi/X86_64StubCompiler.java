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

import com.kenai.jffi.Function;
import jnr.ffi.*;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.x86asm.Assembler;
import jnr.x86asm.REG;
import jnr.x86asm.Register;

import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.x86asm.Asm.*;

/**
 * Compilers method trampoline stubs for x86_64 
 */
final class X86_64StubCompiler extends AbstractX86StubCompiler {

    X86_64StubCompiler(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    boolean canCompile(ResultType returnType, ParameterType[] parameterTypes, CallingConvention convention) {

        // There is only one calling convention; SYSV, so abort if someone tries to use stdcall
        if (convention != CallingConvention.DEFAULT) {
            return false;
        }

        switch (returnType.getNativeType()) {
            case VOID:
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case SLONGLONG:
            case ULONGLONG:
            case FLOAT:
            case DOUBLE:
            case ADDRESS:
                break;

            default:
                return false;
        }


        int fCount = 0;
        int iCount = 0;

        for (ParameterType t : parameterTypes) {
            switch (t.getNativeType()) {
                case SCHAR:
                case UCHAR:
                case SSHORT:
                case USHORT:
                case SINT:
                case UINT:
                case SLONG:
                case ULONG:
                case SLONGLONG:
                case ULONGLONG:
                case ADDRESS:
                    ++iCount;
                    break;

                case FLOAT:
                case DOUBLE:
                    ++fCount;
                    break;

                default:
                    // Fail on anything else
                    return false;
            }
        }

        // We can only safely compile methods with up to 6 integer and 8 floating point parameters
        return iCount <= 6 && fCount <= 8;
    }


    static final Register[] srcRegisters8 = { dl, cl, r8b, r9b };
    static final Register[] srcRegisters16 = { dx, cx, r8w, r9w };
    static final Register[] srcRegisters32 = { edx, ecx, Register.gpr(REG.REG_R8D), Register.gpr(REG.REG_R9D) };
    static final Register[] srcRegisters64 = { rdx, rcx, r8, r9 };
    static final Register[] dstRegisters32 = { edi, esi, edx, ecx, Register.gpr(REG.REG_R8D), Register.gpr(REG.REG_R9D) };
    static final Register[] dstRegisters64 = { rdi, rsi, rdx, rcx, r8, r9 };

    @Override
    final void compile(Function function, String name, ResultType resultType, ParameterType[] parameterTypes,
                       Class resultClass, Class[] parameterClasses, CallingConvention convention, boolean saveErrno) {

        Assembler a = new Assembler(X86_64);
        int iCount = iCount(parameterTypes);
        int fCount = fCount(parameterTypes);

        boolean canJumpToTarget = !saveErrno & iCount <= 6 & fCount <= 8;
        switch (resultType.getNativeType()) {
            case SINT:
            case UINT:
                canJumpToTarget &= int.class == resultClass;
                break;

            case SLONGLONG:
            case ULONGLONG:
                canJumpToTarget &= long.class == resultClass;
                break;

            case FLOAT:
                canJumpToTarget &= float.class == resultClass;
                break;

            case DOUBLE:
                canJumpToTarget &= double.class == resultClass;
                break;

            case VOID:
                break;

            default:
                canJumpToTarget = false;
                break;
        }

        // JNI functions all look like:
        // foo(JNIEnv* env, jobject self, arg...)
        // on AMD64, those sit in %rdi, %rsi, %rdx, %rcx, %r8 and %r9
        // So we need to shuffle all the integer args up to over-write the
        // env and self arguments
        //
        for (int i = 0; i < Math.min(iCount, 4); i++) {
            switch (parameterTypes[i].getNativeType()) {
                case SCHAR:
                    a.movsx(dstRegisters64[i], srcRegisters8[i]);
                    break;

                case UCHAR:
                    a.movzx(dstRegisters64[i], srcRegisters8[i]);
                    break;

                case SSHORT:
                    a.movsx(dstRegisters64[i], srcRegisters16[i]);
                    break;

                case USHORT:
                    a.movzx(dstRegisters64[i], srcRegisters16[i]);
                    break;

                case SINT:
                    a.movsxd(dstRegisters64[i], srcRegisters32[i]);
                    break;

                case UINT:
                    // mov with a 32bit dst reg zero extends to 64bit
                    a.mov(dstRegisters32[i], srcRegisters32[i]);
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    if (parameterTypes[i].getDeclaredType() != long.class) {
                        a.movsxd(dstRegisters64[i], srcRegisters32[i]);
                    } else {
                        a.mov(dstRegisters64[i], srcRegisters64[i]);
                    }
                    break;

                default:
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;
            }
        }

        if (iCount > 6) {
            throw new IllegalArgumentException("integer argument count > 6");
        }

        // For args 5 & 6 of the function, they would have been pushed on the stack
        for (int i = 4; i < iCount; i++) {
            int disp = 8 + ((4 - i) * 8);
            switch (parameterTypes[i].getNativeType()) {
                case SCHAR:
                    a.movsx(dstRegisters64[i], byte_ptr(rsp, disp));
                    break;

                case UCHAR:
                    a.movzx(dstRegisters64[i], byte_ptr(rsp, disp));
                    break;

                case SSHORT:
                    a.movsx(dstRegisters64[i], word_ptr(rsp, disp));
                    break;

                case USHORT:
                    a.movzx(dstRegisters64[i], word_ptr(rsp, disp));
                    break;

                case SINT:
                    a.movsxd(dstRegisters64[i], dword_ptr(rsp, disp));
                    break;

                case UINT:
                    // mov with a 32bit dst reg zero extends to 64bit
                    a.mov(dstRegisters32[i], dword_ptr(rsp, disp));
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    if (parameterTypes[i].getDeclaredType() != long.class) {
                        a.movsxd(dstRegisters64[i], dword_ptr(rsp, disp));
                    } else {
                        a.mov(dstRegisters64[i], qword_ptr(rsp, disp));
                    }
                    break;

                default:
                    a.mov(dstRegisters64[i], qword_ptr(rsp, disp));
                    break;
            }
        }

        // All the integer registers are loaded; there nothing to do for the floating
        // registers, as the first 8 args are already in xmm0..xmm7, so just sanity check
        if (fCount > 8) {
            throw new IllegalArgumentException("float argument count > 8");
        }

        if (canJumpToTarget) {
            a.jmp(imm(function.getFunctionAddress()));
            stubs.add(new Stub(name, sig(resultClass, parameterClasses), a));
            return;
        }

        // Need to align the stack to 16 bytes for function call.
        // It already has 8 bytes pushed (the return address), so making space
        // to save the return value from the function neatly aligns it to 16 bytes
        int space = resultClass == float.class || resultClass == double.class
                    ? 24 : 8;
        a.sub(rsp, imm(space));

        // Clear %rax, since it is used by varargs functions to determine the number of float registers to be saved
        a.mov(rax, imm(0));

        // Call to the actual native function
        a.call(imm(function.getFunctionAddress()));

        if (saveErrno) {
            // Save the return on the stack
            switch (resultType.getNativeType()) {
                case VOID:
                    // No need to save/reload return value registers
                    break;

                case FLOAT:
                    a.movss(dword_ptr(rsp, 0), xmm0);
                    break;

                case DOUBLE:
                    a.movsd(qword_ptr(rsp, 0), xmm0);
                    break;

                default:
                    a.mov(qword_ptr(rsp, 0), rax);
            }

            // Save the errno in a thread-local variable
            a.call(imm(errnoFunctionAddress));

            // Retrieve return value and put it back in the appropriate return register
            switch (resultType.getNativeType()) {
                case VOID:
                    // No need to save/reload return value registers
                    break;

                case SCHAR:
                    a.movsx(rax, byte_ptr(rsp, 0));
                    break;

                case UCHAR:
                    a.movzx(rax, byte_ptr(rsp, 0));
                    break;

                case SSHORT:
                    a.movsx(rax, word_ptr(rsp, 0));
                    break;

                case USHORT:
                    a.movzx(rax, word_ptr(rsp, 0));
                    break;

                case SINT:
                    a.movsxd(rax, dword_ptr(rsp, 0));
                    break;

                case UINT:
                    // storing a value in eax zeroes out the upper 32 bits of rax
                    a.mov(eax, dword_ptr(rsp, 0));
                    break;

                case FLOAT:
                    a.movss(xmm0, dword_ptr(rsp, 0));
                    break;

                case DOUBLE:
                    a.movsd(xmm0, qword_ptr(rsp, 0));
                    break;

                default:
                    a.mov(rax, qword_ptr(rsp, 0));
                    break;
            }

        } else {
            // sign/zero extend the result
            switch (resultType.getNativeType()) {
                case SCHAR:
                    a.movsx(rax, al);
                    break;

                case UCHAR:
                    a.movzx(rax, al);
                    break;

                case SSHORT:
                    a.movsx(rax, ax);
                    break;

                case USHORT:
                    a.movzx(rax, ax);
                    break;

                case SINT:
                    if (long.class == resultClass) a.movsxd(rax, eax);
                    break;

                case UINT:
                    if (long.class == resultClass) a.mov(eax, eax);
                    break;
            }
        }

        if (boolean.class == resultType.getDeclaredType()) {
            a.test(rax, rax);
            a.setne(rax);
        }

        // Restore rsp to original position
        a.add(rsp, imm(space));
        a.ret();

        stubs.add(new Stub(name, sig(resultClass, parameterClasses), a));
    }

    static int fCount(ParameterType[] parameterTypes) {
        int fCount = 0;

        for (ParameterType t : parameterTypes) {
            switch (t.getNativeType()) {
                case FLOAT:
                case DOUBLE:
                    ++fCount;
                    break;
            }
        }

        return fCount;
    }

    static int iCount(ParameterType[] parameterTypes) {
        int iCount = 0;

        for (ParameterType t : parameterTypes) {
            switch (t.getNativeType()) {
                case SCHAR:
                case UCHAR:
                case SSHORT:
                case USHORT:
                case SINT:
                case UINT:
                case SLONG:
                case ULONG:
                case SLONGLONG:
                case ULONGLONG:
                case ADDRESS:
                    ++iCount;
                    break;
            }
        }

        return iCount;
    }
}
