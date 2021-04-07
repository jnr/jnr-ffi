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
import jnr.a64asm.Assembler_A64;
import jnr.a64asm.CPU_A64;
import jnr.a64asm.Immediate;
import jnr.a64asm.Offset;
import jnr.a64asm.Post_index;
import jnr.a64asm.Pre_index;
import jnr.a64asm.Register;
import jnr.a64asm.Shift;
import jnr.ffi.CallingConvention;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;

import static jnr.ffi.provider.jffi.CodegenUtils.sig;

/**
 * Compilers method trampoline stubs for x86_64
 */
final class ARM_64StubCompiler extends AbstractA64StubCompiler {

    ARM_64StubCompiler(jnr.ffi.Runtime runtime) {
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

    static final Register[] srcRegisters32 = { Register.gpw(2), Register.gpw(3), Register.gpw(4), Register.gpw(5), Register.gpw(6), Register.gpw(7) };
    static final Register[] srcRegisters64 = { Register.gpb(2), Register.gpb(3), Register.gpb(4), Register.gpb(5), Register.gpb(6), Register.gpb(7) };
    static final Register[] dstRegisters32 = { Register.gpw(0), Register.gpw(1), Register.gpw(2), Register.gpw(3), Register.gpw(4),Register.gpw(5), Register.gpw(6), Register.gpw(7) };
    static final Register[] dstRegisters64 = { Register.gpb(0), Register.gpb(1), Register.gpb(2), Register.gpb(3), Register.gpb(4),Register.gpb(5), Register.gpb(6), Register.gpb(7) };

    @Override
    final void compile(Function function, String name, ResultType resultType, ParameterType[] parameterTypes,
                       Class resultClass, Class[] parameterClasses, CallingConvention convention, boolean saveErrno) {
        Assembler_A64 a = new Assembler_A64(CPU_A64.A64);
        int iCount = iCount(parameterTypes);
        int fCount = fCount(parameterTypes);

        //usage of sp and imm() from Asm.java creates problems; better use Register.gpb(31) and Immediate.imm()
        Pre_index pindex = new Pre_index(Register.gpb(31),Immediate.imm(-32));
        a.stp(Register.gpb(29),Register.gpb(30),pindex);
        a.mov(Register.gpb(29),Register.gpb(31));
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
        // on AARCH64, those sit in X0-X7/W0-W7
        // So we need to shuffle all the integer args up to over-write the
        // env and self arguments

        for (int i = 0; i < Math.min(iCount, 6); i++) {
            switch (parameterTypes[i].getNativeType()) {
                case SCHAR:
                    a.sxtb(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;

                case UCHAR:
                    a.uxtb(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;

                case SSHORT:
                    a.sxth(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;

                case USHORT:
                    a.uxth(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;

                case SINT:
                    a.sxtw(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;

                case UINT:
                    a.uxtw(srcRegisters64[i], srcRegisters32[i]);
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;

                default:
                    a.mov(dstRegisters64[i], srcRegisters64[i]);
                    break;
            }
        }

        if (iCount > 6) {
            throw new IllegalArgumentException("integer argument count > 6");
        }

        // All the integer registers are loaded; there nothing to do for the floating
        // registers, as the first 8 args are already in xmm0..xmm7, so just sanity check
        if (fCount > 8) {
            throw new IllegalArgumentException("float argument count > 8");
        }

        Offset offset = new Offset(Register.gpb(29),Immediate.imm(16));
        long function_addr = function.getFunctionAddress();
        short funn_addr_chunks = (short) (function_addr & 0x000000000000ffff);
        Shift sh;
        int count;
        a.mov(Register.gpb(9),Immediate.imm(funn_addr_chunks));
        for (count = 1; count < 4; count++){
            sh = new Shift(1,16*(count));
            funn_addr_chunks = (short) ((function_addr >> (16*count)) & 0x000000000000ffff);
            a.movk(Register.gpb(9),Immediate.imm(funn_addr_chunks),sh);
        }
        a.blr(Register.gpb(9));
        if (saveErrno) {
            // Save the return on the stack
            switch (resultType.getNativeType()) {
                case VOID:
                    // No need to save/reload return value registers
                    break;
                default:
                    a.str(dstRegisters64[0], offset);
                    break;
            }

            // Save the errno in a thread-local variable
            function_addr = errnoFunctionAddress;
            funn_addr_chunks = (short) (function_addr & 0x000000000000ffff);
            a.mov(Register.gpb(9),Immediate.imm(funn_addr_chunks));
            for (count = 1; count < 4; count++){
                sh = new Shift(1, 16 * count);
                funn_addr_chunks = (short) ((function_addr >> (16 * count)) & 0x000000000000ffff);
                a.movk(Register.gpb(9),Immediate.imm(funn_addr_chunks),sh);
            }
            a.blr(Register.gpb(9));
            // Retrieve return value and put it back in the appropriate return register
            switch (resultType.getNativeType()) {
                case VOID:
                    // No need to save/reload return value registers
                    break;

                case SCHAR:
                    a.ldrsb(dstRegisters64[0],offset);
                    break;

                case UCHAR:
                    a.ldrb(dstRegisters64[0],offset);
                    break;

                case SSHORT:
                    a.ldrsh(dstRegisters64[0],offset);
                    break;

                case USHORT:
                    a.ldrh(dstRegisters64[0],offset);
                    break;

                case SINT:
                    a.ldrsw(dstRegisters64[0],offset);
                    break;

                case UINT:
                    a.ldr(dstRegisters64[0],offset);
                    break;

                default:
                    a.ldr(dstRegisters64[0],offset);
                    break;
            }
        } else {
            // sign/zero extend the result

            switch (resultType.getNativeType()) {
                case SCHAR:
                    a.sxtb(dstRegisters64[0], dstRegisters32[0]);
                    break;

                case UCHAR:
                    a.uxtb(dstRegisters64[0], dstRegisters32[0]);
                    break;

                case SSHORT:
                    a.sxth(dstRegisters64[0], dstRegisters32[0]);
                    break;

                case USHORT:
                    a.uxth(dstRegisters64[0], dstRegisters32[0]);
                    break;

                case SINT:
                    a.sxtw(dstRegisters64[0], dstRegisters32[0]);
                    break;

                case UINT:
                    a.uxtw(dstRegisters64[0], dstRegisters32[0]);
                    break;
            }
        }

        Post_index posindex = new Post_index(Register.gpb(31),Immediate.imm(32));
        a.ldp(Register.gpb(29),Register.gpb(30),posindex );
        a.ret((Register)null);
        stubs_A64.add(new Stub(name, sig(resultClass, parameterClasses), a));
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