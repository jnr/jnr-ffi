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

package jnr.ffi;

/**
 * TypeAliases mostly for POSIX 
 * TODO dprecate this and move this to jnr-posix ???
 * 
 * 
 */
public enum TypeAlias {
    int8_t,
    u_int8_t,
    int16_t,
    u_int16_t,
    int32_t,
    u_int32_t,
    int64_t,
    u_int64_t,
    /**
     * signed long int can be 32 or 64 depending on WORDSIZE or ADRESSSIZE.
     * On 32bit OS this will be 32 bits.
     * On 64bit OS this will be 64 bits.
     * use this for function params and return values
     */
    signed_long_int,
    /**
     * unsigned long int can be 32 or 64 depending on WORDSIZE or ADRESSSIZE.
     * On 32bit OS this will be 32 bits.
     * On 64bit OS this will be 64 bits.
     * use this for function params and return values
     */
    unsigned_long_int,
    intptr_t,
    uintptr_t,
    @Deprecated
    /**
     * This is a pointer to cahr. - so no need to define this here.... Or have a NativeType.ADRESS_CHAR ???
     */        
    caddr_t, 
    dev_t,
    blkcnt_t,
    /**
     * POSIX
     */
    blksize_t,
    gid_t,
    in_addr_t,
    in_port_t,
    ino_t,
    /**
     *TODO Is this not defined as __ino64_t??
     */
    ino64_t,
    key_t,
    /**
     * POSIX
     */
    mode_t,
    nlink_t,
    id_t,
    /**
     * POSIX
     */
    pid_t,
    off_t,
    /**
     * TODO Where is this from
     */
    swblk_t,
    uid_t,
    clock_t,
    /**
     * POSIX
     */
    size_t,
    /**
     * POSIX
     */
    ssize_t,
    time_t,
    fsblkcnt_t,
    fsfilcnt_t,
    sa_family_t,
    socklen_t,
    /**
     * TODO Is this not defines ad  __rlim_t
     */
    rlim_t,
    /**
     * POSIX
     */
    cc_t,
    /**
     * POSIX
     */
    speed_t,
    /**
     * POSIX
     */
    tcflag_t,
    /**
     * Header: eventfd.h
     * Linux:
     * typedef uint64_t eventfd_t;
     */
    eventfd_t,
    /**
     * POSIX
     * Header: poll.h
     * Linux:
     * typedef unsigned long int nfds_t
     */
    nfds_t,
    /**
     * Header: unistd.h
     * Linux:
     * typedef unsigned int __useconds_t;
     * typedef __useconds_t useconds_t;
     */
    useconds_t,
    /**
     * POSIX
     */
    ptrdiff_t,
    /**
     * POSIX
     */
    suseconds_t,
    /**
     * POSIX
     */
    wchar_t,
    /**
     * POSIX
     */
    wint_t,
    /**
     * The windows handle.
     * can be 32 or 64
     */
    HANDLE;
}
