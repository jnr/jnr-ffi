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

/**
 * Compiles asm trampoline stubs for java class methods
 */
abstract class StubCompiler {
    
    public static final StubCompiler newCompiler() {
        return Platform.getPlatform().getCPU() == Platform.CPU.X86_64
                && Platform.getPlatform().getOS() == Platform.OS.LINUX
                ? new X86_64StubCompiler() : new DummyStubCompiler();
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
}
