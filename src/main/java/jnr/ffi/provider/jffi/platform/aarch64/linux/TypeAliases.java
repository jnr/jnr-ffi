/*
 * Copyright (C) 2016 Wayne Meissner
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

package jnr.ffi.provider.jffi.platform.aarch64.linux;

import jnr.ffi.NativeType;
import jnr.ffi.TypeAlias;

import java.util.EnumMap;
import java.util.Map;

public final class TypeAliases {
    public static final Map<TypeAlias, jnr.ffi.NativeType> ALIASES = buildTypeMap();
    private static Map<TypeAlias, jnr.ffi.NativeType> buildTypeMap() {
        Map<TypeAlias, jnr.ffi.NativeType> m = new EnumMap<TypeAlias, jnr.ffi.NativeType>(TypeAlias.class);
        m.put(TypeAlias.int8_t, NativeType.SCHAR);
        m.put(TypeAlias.u_int8_t, NativeType.UCHAR);
        m.put(TypeAlias.int16_t, NativeType.SSHORT);
        m.put(TypeAlias.u_int16_t, NativeType.USHORT);
        m.put(TypeAlias.int32_t, NativeType.SINT);
        m.put(TypeAlias.u_int32_t, NativeType.UINT);
        m.put(TypeAlias.int64_t, NativeType.ADDRESS);
        m.put(TypeAlias.u_int64_t, NativeType.ADDRESS);
        m.put(TypeAlias.intptr_t, NativeType.SLONG);
        m.put(TypeAlias.uintptr_t, NativeType.ULONG);
        m.put(TypeAlias.caddr_t, NativeType.ADDRESS);
        m.put(TypeAlias.dev_t, NativeType.ULONG);
        m.put(TypeAlias.blkcnt_t, NativeType.SLONG);
        m.put(TypeAlias.blksize_t, NativeType.SLONG);
        m.put(TypeAlias.gid_t, NativeType.UINT);
        m.put(TypeAlias.in_addr_t, NativeType.UINT);
        m.put(TypeAlias.in_port_t, NativeType.USHORT);
        m.put(TypeAlias.ino_t, NativeType.ULONG);
        m.put(TypeAlias.ino64_t, NativeType.ULONG);
        m.put(TypeAlias.key_t, NativeType.SINT);
        m.put(TypeAlias.mode_t, NativeType.UINT);
        m.put(TypeAlias.nlink_t, NativeType.ULONG);
        m.put(TypeAlias.id_t, NativeType.UINT);
        m.put(TypeAlias.pid_t, NativeType.SINT);
        m.put(TypeAlias.off_t, NativeType.SLONG);
        m.put(TypeAlias.swblk_t, NativeType.SLONG);
        m.put(TypeAlias.uid_t, NativeType.UINT);
        m.put(TypeAlias.clock_t, NativeType.SLONG);
        m.put(TypeAlias.size_t, NativeType.ULONG);
        m.put(TypeAlias.ssize_t, NativeType.SLONG);
        m.put(TypeAlias.time_t, NativeType.SLONG);
        m.put(TypeAlias.fsblkcnt_t, NativeType.ULONG);
        m.put(TypeAlias.fsfilcnt_t, NativeType.ULONG);
        m.put(TypeAlias.sa_family_t, NativeType.USHORT);
        m.put(TypeAlias.socklen_t, NativeType.UINT);
        m.put(TypeAlias.rlim_t, NativeType.ULONG);
        return m;
    }
}
