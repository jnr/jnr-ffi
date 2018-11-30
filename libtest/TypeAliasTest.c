/*
 * Copyright (C) 2018 Arne Plöse
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

//THESE here to make the IDE Editor happy uncomment 
//#define linux_ide

#ifdef linux_ide
#define IDE_SETUP
#define __USE_POSIX
#define unix
#define linux
#endif



#ifdef unix //we shzould be able to include all posix headers and therefore __USE_POSIX is defined ...
//Include all posix headers....
#include <aio.h>
#include <arpa/inet.h>
#include <assert.h>
#include <complex.h>
#include <cpio.h>
#include <ctype.h>
#include <dirent.h>
#include <dlfcn.h>
#include <errno.h>
#include <fcntl.h>
#include <fenv.h>
#include <float.h>
#include <fmtmsg.h>//XSI
#include <fnmatch.h>
#include <ftw.h>//XSI 
#include <glob.h>
#include <grp.h>
#include <iconv.h>
#include <inttypes.h>
#include <iso646.h>
#include <langinfo.h>
//#include <libgen.h>//XSI
#include <limits.h>
#include <locale.h>
#include <math.h>
#include <monetary.h>
//#include <mqueue.h>//XSI
//#include <ndbm.h>//XSI
#include <net/if.h>
#include <netdb.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <nl_types.h>
#include <poll.h>
#include <pthread.h>
#include <pwd.h>
#include <regex.h>
#include <sched.h>
//#include <search.h>//XSI
#include <semaphore.h>
#include <setjmp.h>
#include <signal.h>
//#include <spawn.h>//XSI
#include <stdarg.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
//#include <stropts.h>//XSI
//#include <sys/ipc.h>//XSI
#include <sys/mman.h>
//#include <sys/msg.h>//XSI
//#include <sys/resource.h>//XSI
#include <sys/select.h>
//#include <sys/sem.h>//XSI
//#include <sys/shm.h>//XSI
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/statvfs.h>
//#include <sys/time.h>//XSI
#include <sys/times.h>
#include <sys/types.h>
//#include <sys/uio.h>//XSI
#include <sys/un.h>
#include <sys/utsname.h>
#include <sys/wait.h>
//#include <syslog.h>//XSI
#include <tar.h>
#include <termios.h>
#include <tgmath.h>
#include <time.h>
//#include <trace.h>//XSI
//#include <ulimit.h>//XSI
#include <unistd.h>
//#include <utime.h>//XSI
//#include <utmpx.h>//XSI
//#include <wchar.h>//XSI
#include <wctype.h>
#include <wordexp.h>

#ifndef ino64_t
//Type name mismatch ?? Just fix it TODO investigate later...
#define ino64_t __ino64_t
#endif

#ifndef rlim_t
//Type name mismatch ?? Just fix it TODO investigate later...
#define rlim_t __rlim_t
#endif
#endif

#ifdef linux
#include <sys/eventfd.h>
#endif

struct testTypeAliases {
#ifdef __USE_POSIX
    int8_t ta_field_int8_t;
    u_int8_t ta_field_u_int8_t;
    int16_t ta_field_int16_t;
    u_int16_t ta_field_u_int16_t;
    int32_t ta_field_int32_t;
    u_int32_t ta_field_u_int32_t;
    int64_t ta_field_int64_t;
    u_int64_t ta_field_u_int64_t;
    intptr_t ta_field_intptr_t;
    uintptr_t ta_field_uintptr_t;
    caddr_t ta_field_caddr_t;
    dev_t ta_field_dev_t;
    blkcnt_t ta_field_blkcnt_t;
    blksize_t ta_field_blksize_t;
    gid_t ta_field_gid_t;
    in_addr_t ta_field_in_addr_t;
    in_port_t ta_field_in_port_t;
    ino_t ta_field_ino_t;
    ino64_t ta_field_ino64_t;
    key_t ta_field_key_t;
    mode_t ta_field_mode_t;
    nlink_t ta_field_nlink_t;
    id_t ta_field_id_t;
    pid_t ta_field_pid_t;
    off_t ta_field_off_t;
//TODO where is this from??    swblk_t ta_field_swblk_t;
    uid_t ta_field_uid_t;
    clock_t ta_field_clock_t;
    size_t ta_field_size_t;
    ssize_t ta_field_ssize_t;
    time_t ta_field_time_t;
    fsblkcnt_t ta_field_fsblkcnt_t;
    fsfilcnt_t ta_field_fsfilcnt_t;
    sa_family_t ta_field_sa_family_t;
    socklen_t ta_field_socklen_t;
    rlim_t ta_field_rlim_t;
    cc_t ta_field_cc_t;
    speed_t ta_field_speed_t;
    tcflag_t ta_field_tcflag_t;
#endif
#ifdef linux
    eventfd_t ta_field_eventfd_t;
#endif
#ifdef __USE_POSIX    
    nfds_t ta_field_nfds_t;
    useconds_t ta_field_useconds_t; 
    ptrdiff_t ta_field_ptrdiff_t;
    suseconds_t ta_field_;
    wchar_t ta_field_wchar_t;
    wint_t ta_field_wint_t;
#endif    
};

//#define __USE_POSIX    
#ifdef __USE_POSIX    

int sizeOf_int8_t() {
    return sizeof(int8_t);
}

int8_t test__int8_t(int8_t value) {
    return ~value;
}

int sizeOf_u_int8_t() {
    return sizeof(u_int8_t);
}

u_int8_t test__u_int8_t(u_int8_t value) {
    return ~value;
}

int sizeOf_int16_t() {
    return sizeof(int16_t);
}

int16_t test__int16_t(int16_t value) {
    return ~value;
}

int sizeOf_u_int16_t() {
    return sizeof(u_int16_t);
}

u_int16_t test__u_int16_t(u_int16_t value) {
    return ~value;
}

int sizeOf_int32_t() {
    return sizeof(int32_t);
}

int32_t test__int32_t(int32_t value) {
    return ~value;
}

int sizeOf_u_int32_t() {
    return sizeof(u_int32_t);
}

u_int32_t test__u_int32_t(u_int32_t value) {
    return ~value;
}

int sizeOf_int64_t() {
    return sizeof(int64_t);
}

int64_t test__int64_t(int64_t value) {
    return ~value;
}

int sizeOf_u_int64_t() {
    return sizeof(u_int64_t);
}

u_int64_t test__u_int64_t(u_int64_t value) {
    return ~value;
}


#endif    

#ifdef IDE_SETUP
Do not compile IDE Setup 
#endif


