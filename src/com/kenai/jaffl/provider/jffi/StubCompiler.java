
package com.kenai.jaffl.provider.jffi;

import com.kenai.jaffl.Platform;
import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Internals;
import com.kenai.jffi.PageManager;
import com.kenai.jnr.x86asm.Assembler;
import com.kenai.jnr.x86asm.CPU;

/**
 * Compiles asm trampoline stubs for java class methods
 */
abstract class StubCompiler {
    // If the version of jffi exports the jffi_save_errno function address,
    // then it is recent enough to support PageManager and NativeMethods as well.
    static final long errnoFunctionAddress = getErrnoSaveFunction();
    static final boolean hasPageManager = hasPageManager();
    static final boolean hasAssembler = hasAssembler();
    
    public static final StubCompiler newCompiler() {
        if (errnoFunctionAddress != 0 && hasPageManager && hasAssembler) {
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

    private static final boolean hasPageManager() {
        try {
            // Just try and allocate/free a page to check the PageManager is working
            long page = PageManager.getInstance().allocatePages(1, PageManager.PROT_READ | PageManager.PROT_WRITE);
            PageManager.getInstance().freePages(page, 1);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static final boolean hasAssembler() {
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
