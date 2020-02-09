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
import jnr.x86asm.Mem;
import jnr.x86asm.Register;

import static jnr.ffi.provider.jffi.CodegenUtils.sig;
import static jnr.x86asm.Asm.*;

/**
 * Stub compiler for i386 unix
 */
final class X86_32StubCompiler extends AbstractX86StubCompiler {

    X86_32StubCompiler(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    boolean canCompile(ResultType returnType, ParameterType[] parameterTypes, CallingConvention convention) {

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

        // There is only one calling convention; SYSV, so abort if someone tries to use stdcall
        if (convention != CallingConvention.DEFAULT) {
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
        return true;
    }


    @Override
    void compile(Function function, String name, ResultType resultType, ParameterType[] parameterTypes, Class resultClass, Class[] parameterClasses, CallingConvention convention, boolean saveErrno) {

        int psize = 0;
        for (ParameterType t : parameterTypes) {
            psize += parameterSize(t);
        }

        int rsize = resultSize(resultType);


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

        // copy and convert the parameters from the orig stack to the new location
        for (int i = 0, srcoff = 0, dstoff = 0; i < parameterTypes.length; i++)  {
            int srcParameterSize = parameterSize(parameterClasses[i]);
            int dstParameterSize = parameterSize(parameterTypes[i]);
            int disp = stackadj + 4 + 8 + srcoff;

            switch (parameterTypes[i].getNativeType()) {
                case SCHAR:
                case SSHORT:
                    a.movsx(eax, ptr(esp, disp, parameterTypes[i].getNativeType()));
                    break;

                case UCHAR:
                case USHORT:
                    a.movzx(eax, ptr(esp, disp, parameterTypes[i].getNativeType()));
                    break;

                default:
                    a.mov(eax, dword_ptr(esp, disp));
                    break;
            }
            a.mov(dword_ptr(esp, dstoff), eax);

            if (dstParameterSize > 4) {
                switch (parameterTypes[i].getNativeType()) {
                    case SLONGLONG:
                    case ULONGLONG:
                        if (long.class != parameterClasses[i]) {
                            // sign extend from int.class -> long long
                            a.sar(eax, imm(31));
                        } else {
                            a.mov(eax, dword_ptr(esp, disp + 4));
                        }
                        break;
                    case DOUBLE:
                        a.mov(eax, dword_ptr(esp, disp + 4));
                        break;
                    default:
                        // impossible
                        break;
                }
                a.mov(dword_ptr(esp, dstoff + 4), eax);
            }

            dstoff += dstParameterSize;
            srcoff += srcParameterSize;
        }


        // Call to the actual native function
        a.call(imm(function.getFunctionAddress() & 0xffffffffL));
        
        if (saveErrno) {
            int save = 0;
            switch (resultType.getNativeType()) {
                case FLOAT:
                    a.fstp(dword_ptr(esp, save));
                    break;

                case DOUBLE:
                    a.fstp(qword_ptr(esp, save));
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    a.mov(dword_ptr(esp, save), eax);
                    a.mov(dword_ptr(esp, save + 4), edx);
                    break;

                case VOID:
                    // No need to save for void values
                    break;

                default:
                    a.mov(dword_ptr(esp, save), eax);
            }

            // Save the errno in a thread-local variable
            a.call(imm(errnoFunctionAddress & 0xffffffffL));

            // Retrieve return value and put it back in the appropriate return register
            switch (resultType.getNativeType()) {
                case FLOAT:
                    a.fld(dword_ptr(esp, save));
                    break;

                case DOUBLE:
                    a.fld(qword_ptr(esp, save));
                    break;

                case SCHAR:
                    a.movsx(eax, byte_ptr(esp, save));
                    break;

                case UCHAR:
                    a.movzx(eax, byte_ptr(esp, save));
                    break;

                case SSHORT:
                    a.movsx(eax, word_ptr(esp, save));
                    break;

                case USHORT:
                    a.movzx(eax, word_ptr(esp, save));
                    break;

                case SLONGLONG:
                case ULONGLONG:
                    a.mov(eax, dword_ptr(esp, save));
                    a.mov(edx, dword_ptr(esp, save + 4));
                    break;

                case VOID:
                    // No need to save for void values
                    break;

                default:
                    a.mov(eax, dword_ptr(esp, save));
            }

        } else {

            switch (resultType.getNativeType()) {
                case SCHAR:
                    a.movsx(eax, al);
                    break;

                case UCHAR:
                    a.movzx(eax, al);
                    break;

                case SSHORT:
                    a.movsx(eax, ax);
                    break;

                case USHORT:
                    a.movzx(eax, ax);
                    break;
            }
        }

        if (long.class == resultClass) {
            // sign or zero extend the result to 64 bits
            switch (resultType.getNativeType()) {
                case SCHAR:
                case SSHORT:
                case SINT:
                case SLONG:
                    // sign extend eax to edx:eax
                    a.mov(edx, eax);
                    a.sar(edx, imm(31));
                    break;

                case UCHAR:
                case USHORT:
                case UINT:
                case ULONG:
                case ADDRESS:
                    a.mov(edx, imm(0));
                    break;
            }

        }
        if (boolean.class == resultClass) {
            switch (resultType.getNativeType()) {
                case SLONGLONG:
                case ULONGLONG:
                    a.or_(eax, edx);
                    break;
                default:
                    a.test(eax, eax);
                    break;
            }
            a.setne(al);
        }
        // Restore esp to the original position and return
        a.add(esp, imm(stackadj));
        a.ret();

        stubs.add(new Stub(name, sig(resultClass, parameterClasses), a));
    }

    static int parameterSize(ParameterType parameterType) {
        switch (parameterType.getNativeType()) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case ADDRESS:
            case FLOAT:
                return 4;

            case SLONGLONG:
            case ULONGLONG:
            case DOUBLE:
                return 8;

            default:
                throw new IllegalArgumentException("invalid parameter type" + parameterType);
        }
    }

    static int parameterSize(Class t) {
        if (boolean.class == t || byte.class == t || short.class == t || char.class == t | int.class == t || float.class == t) {
            return 4;

        } else if (long.class == t || double.class == t) {
            return 8;
        }
        throw new IllegalArgumentException("invalid parameter type" + t);
    }


    static int resultSize(ResultType resultType) {
        switch (resultType.getNativeType()) {
            case SCHAR:
            case UCHAR:
            case SSHORT:
            case USHORT:
            case SINT:
            case UINT:
            case SLONG:
            case ULONG:
            case ADDRESS:
                return 4;

            case SLONGLONG:
            case ULONGLONG:
                return 8;

            case FLOAT:
            case DOUBLE:
                return 16;

            case VOID:
                return 0;

            default:
                throw new IllegalArgumentException("invalid return type " + resultType);
        }
    }

    static Mem ptr(Register base, long disp, NativeType nativeType) {
        switch (nativeType) {
            case SCHAR:
            case UCHAR:
                return byte_ptr(base, disp);

            case SSHORT:
            case USHORT:
                return word_ptr(base, disp);

            default:
                return dword_ptr(base, disp);
        }
    }

}
