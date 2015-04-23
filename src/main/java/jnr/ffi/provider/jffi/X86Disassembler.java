/*
 * Copyright (C) 2012 Wayne Meissner
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

import jnr.ffi.*;
import jnr.ffi.LibraryLoader;
import jnr.ffi.Runtime;
import jnr.ffi.mapper.DefaultTypeMapper;
import jnr.ffi.mapper.ToNativeContext;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.types.intptr_t;
import jnr.ffi.types.size_t;
import jnr.ffi.types.u_int64_t;
import jnr.ffi.types.u_int8_t;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
class X86Disassembler {

    public enum Syntax { INTEL, ATT }

    public enum Mode { I386, X86_64 }

    private final UDis86 udis86;
    final Pointer ud;


    static final class SingletonHolder {
        static final UDis86 INSTANCE = loadUDis86();
        static final long intel = ((AbstractAsmLibraryInterface) INSTANCE).getLibrary().findSymbolAddress("ud_translate_intel");
        static final long att = ((AbstractAsmLibraryInterface) INSTANCE).getLibrary().findSymbolAddress("ud_translate_att");
    }

    static UDis86 loadUDis86() {
        DefaultTypeMapper typeMapper = new DefaultTypeMapper();
        typeMapper.put(X86Disassembler.class, new X86DisassemblerConverter());
        return LibraryLoader.create(UDis86.class)
                .library("udis86")
                .search("/usr/local/lib")
                .search("/opt/local/lib")
                .search("/usr/lib")
                .mapper(typeMapper)
                .load();
    }

    @ToNativeConverter.NoContext
    public static final class X86DisassemblerConverter implements ToNativeConverter<X86Disassembler, Pointer> {
        public Pointer toNative(X86Disassembler value, ToNativeContext context) {
            return value.ud;
        }

        public Class<Pointer> nativeType() {
            return Pointer.class;
        }
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
        this.ud = Memory.allocateDirect(Runtime.getRuntime(udis86), 1024, true);
        this.udis86.ud_init(this.ud);
    }

    public void setSyntax(Syntax syntax) {
        udis86.ud_set_syntax(this, syntax == Syntax.INTEL ? SingletonHolder.intel : SingletonHolder.att);
    }

    public void setMode(Mode mode) {
        udis86.ud_set_mode(this, mode == Mode.I386 ? 32 : 64);
    }

    public void setInputBuffer(Pointer buffer, int size) {
        udis86.ud_set_input_buffer(this, buffer, size);
    }

    public boolean disassemble() {
        return udis86.ud_disassemble(this) != 0;
    }

    public String insn() {
        return udis86.ud_insn_asm(this);
    }

    public long offset() {
        return udis86.ud_insn_off(this);
    }

    public String hex() {
        return udis86.ud_insn_hex(this);
    }

    @NoX86
    @NoTrace
    public static interface UDis86 {
        void ud_init(Pointer ud);
        void ud_set_mode(X86Disassembler ud, @u_int8_t int mode);
        void ud_set_pc(X86Disassembler ud, @u_int64_t int pc);
        void ud_set_input_buffer(X86Disassembler ud, Pointer data, @size_t long len);
        void ud_set_vendor(X86Disassembler ud, int vendor);
        void ud_set_syntax(X86Disassembler ud, @intptr_t long translator);
        void ud_input_skip(X86Disassembler ud, @size_t long size);
        int ud_input_end(X86Disassembler ud);
        int ud_decode(X86Disassembler ud);
        int ud_disassemble(X86Disassembler ud);
        String ud_insn_asm(X86Disassembler ud);
        @intptr_t long ud_insn_ptr(X86Disassembler ud);
        @u_int64_t long ud_insn_off(X86Disassembler ud);
        String ud_insn_hex(X86Disassembler ud);
        int ud_insn_len(X86Disassembler ud);
    }
}
