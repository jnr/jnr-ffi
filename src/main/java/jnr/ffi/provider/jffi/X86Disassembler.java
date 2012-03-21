package jnr.ffi.provider.jffi;

import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.types.intptr_t;
import jnr.ffi.types.size_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
class X86Disassembler {

    public enum Syntax { INTEL, ATT }

    public enum Mode { I386, X86_64 }

    private final UDis86 udis86;
    private final Pointer ud;


    static final class SingletonHolder {
        static final UDis86 INSTANCE = loadUDis86();
        static final long intel = ((AbstractAsmLibraryInterface) INSTANCE).getLibrary().findSymbolAddress("ud_translate_intel");
        static final long att = ((AbstractAsmLibraryInterface) INSTANCE).getLibrary().findSymbolAddress("ud_translate_att");
    }

    static UDis86 loadUDis86() {
        List<String> libraryPaths = Arrays.asList("/opt/local/lib", "/usr/local/lib", "/usr/lib");
        String path = Platform.getNativePlatform().locateLibrary("udis86", libraryPaths);
        NativeLibrary library = new NativeLibrary(path != null ? path : "udis86");

        return new AsmLibraryLoader().loadLibrary(library, UDis86.class, Collections.EMPTY_MAP);
    }

    static boolean isAvailable() {
        try {
            return SingletonHolder.INSTANCE != null;
        } catch (Throwable ex) {
            return false;
        }
    }

    static X86Disassembler create() {
        return new X86Disassembler(SingletonHolder.INSTANCE);
    }

    private X86Disassembler(UDis86 udis86) {
        this.udis86 = udis86;
        this.ud = NativeRuntime.getInstance().getMemoryManager().allocate(1024);
        this.udis86.ud_init(this.ud);
    }

    public void setSyntax(Syntax syntax) {
        udis86.ud_set_syntax(ud, syntax == Syntax.INTEL ? SingletonHolder.intel : SingletonHolder.att);
    }

    public void setMode(Mode mode) {
        udis86.ud_set_mode(ud, mode == Mode.I386 ? 32 : 64);
    }

    public void setInputBuffer(Pointer buffer, int size) {
        udis86.ud_set_input_buffer(ud, buffer, size);
    }

    public boolean disassemble() {
        return udis86.ud_disassemble(ud) != 0;
    }

    public String insn() {
        return udis86.ud_insn_asm(ud);
    }

    @NoX86
    @NoTrace
    public static interface UDis86 {
        void ud_init(Pointer ud);
        void ud_set_mode(Pointer ud, @u_int8_t int mode);
        void ud_set_pc(Pointer ud, @u_int64_t int pc);
        void ud_set_input_buffer(Pointer ud, Pointer data, @size_t long len);
        void ud_set_vendor(Pointer ud, int vendor);
        void ud_set_syntax(Pointer ud, @intptr_t long translator);
        void ud_input_skip(Pointer ud, @size_t long size);
        int ud_input_end(Pointer ud);
        int ud_decode(Pointer ud);
        int ud_disassemble(Pointer ud);
        String ud_insn_asm(Pointer ud);
        Pointer ud_insn_ptr(Pointer ud);
        @u_int64_t long ud_insn_off(Pointer ud);
        String ud_insn_hex(Pointer ud);
        int ud_insn_len(Pointer ud);
        String ud_lookup_mnemonic(int c);
    }
}
