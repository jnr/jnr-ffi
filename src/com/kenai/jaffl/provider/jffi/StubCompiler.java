/*
 * Copyright (C) 2010 Wayne Meissner
 *
 * This file is part of jffi.
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

import com.kenai.jaffl.Platform;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Internals;

/**
 * Compiles asm trampoline stubs for java class methods
 */
abstract class StubCompiler {
    // If the version of jffi exports the jffi_save_errno function address,
    // then it is recent enough to support PageManager and NativeMethods as well.
    static final long errnoFunctionAddress = getErrnoSaveFunction();
    
    public static final StubCompiler newCompiler() {
        if (errnoFunctionAddress != 0) {
            switch (Platform.getPlatform().getCPU()) {
                case I386:
                    if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
                        return new X86_32StubCompiler();
                    }
                    break;
                case X86_64:
                    if (Platform.getPlatform().getOS() != Platform.OS.WINDOWS) {
                        return new X86_64StubCompiler();
                    }
                    break;
            }
        }

        return new DummyStubCompiler();
    }

    abstract boolean canCompile(Class returnType, Class[] parameterTypes, CallingConvention convention);
    
    abstract void compile(Function function, String name, Class returnType, Class[] parameterTypes, CallingConvention convention, boolean saveErrno);

    abstract void attach(Class clazz);

    static final class DummyStubCompiler extends StubCompiler {

        @Override
        boolean canCompile(Class returnType, Class[] parameterTypes, CallingConvention convention) {
            return false;
        }

        @Override
        void compile(Function function, String name, Class returnType, Class[] parameterTypes, CallingConvention convention, boolean saveErrno) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        void attach(Class clazz) {
            // do nothing
        }

    }

    private static final long getErrnoSaveFunction() {
        try {
            return Internals.getErrnoSaveFunction();
            
        } catch (Throwable t) {
            return 0;
        }
    }
}
