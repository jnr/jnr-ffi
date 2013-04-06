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

import com.kenai.jffi.*;
import jnr.ffi.CallingConvention;
import jnr.ffi.provider.ParameterType;
import jnr.ffi.provider.ResultType;
import jnr.x86asm.Assembler;
import jnr.x86asm.CPU;

/**
 * Compiles asm trampoline stubs for java class methods
 */
abstract class StubCompiler {
    // If the version of jffi exports the jffi_save_errno function address,
    // then it is recent enough to support PageManager and NativeMethods as well.
    static final long errnoFunctionAddress = getErrnoSaveFunction();
    static final boolean hasPageManager = hasPageManager();
    static final boolean hasAssembler = hasAssembler();
    
    public static StubCompiler newCompiler(jnr.ffi.Runtime runtime) {
        if (errnoFunctionAddress != 0 && hasPageManager && hasAssembler) {
            switch (Platform.getPlatform().getCPU()) {
                case I386:
                    if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
                        return new X86_32StubCompiler(runtime);
                    }
                    break;
                case X86_64:
                    if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
                        return new X86_64StubCompiler(runtime);
                    }
                    break;
            }
        }

        return new DummyStubCompiler();
    }

    abstract boolean canCompile(ResultType returnType, ParameterType[] parameterTypes, CallingConvention convention);
    
    abstract void compile(Function function, String name, ResultType returnType, ParameterType[] parameterTypes,
                          Class resultClass, Class[] parameterClasses, CallingConvention convention, boolean saveErrno);

    abstract void attach(Class clazz);

    static final class DummyStubCompiler extends StubCompiler {

        boolean canCompile(ResultType returnType, ParameterType[] parameterTypes, CallingConvention convention) {
            return false;
        }

        @Override
        void compile(Function function, String name, ResultType returnType, ParameterType[] parameterTypes,
                     Class resultClass, Class[] parameterClasses, CallingConvention convention, boolean saveErrno) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        void attach(Class clazz) {
            // do nothing
        }

    }

    private static long getErrnoSaveFunction() {
        try {
            return Internals.getErrnoSaveFunction();
            
        } catch (Throwable t) {
            return 0;
        }
    }

    private static boolean hasPageManager() {
        try {
            // Just try and allocate/free a page to check the PageManager is working
            long page = PageManager.getInstance().allocatePages(1, PageManager.PROT_READ | PageManager.PROT_WRITE);
            PageManager.getInstance().freePages(page, 1);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean hasAssembler() {
        try {
            switch (Platform.getPlatform().getCPU()) {
                case I386:
                    new Assembler(CPU.X86_32);
                    return true;
                case X86_64:
                    new Assembler(CPU.X86_64);
                    return true;
                default:
                    return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }
}
