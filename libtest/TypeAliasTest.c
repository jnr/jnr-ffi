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


#ifdef unix //we should be able to include all posix headers and therefore __USE_POSIX is defined ...
#define GENERATE_POSIX_TESTS

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

#endif

#ifdef linux
#include <sys/eventfd.h>
#endif

struct testTypeAliases {
#ifdef GENERATE_POSIX_TESTS
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
#ifdef ino64_t
    ino64_t ta_field_ino64_t;
#endif
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
#ifdef rlim_t
    rlim_t ta_field_rlim_t;
#endif
    cc_t ta_field_cc_t;
    speed_t ta_field_speed_t;
    tcflag_t ta_field_tcflag_t;
#endif
#ifdef linux
    eventfd_t ta_field_eventfd_t;
#endif
#ifdef GENERATE_POSIX_TESTS    
    nfds_t ta_field_nfds_t;
    useconds_t ta_field_useconds_t; 
    ptrdiff_t ta_field_ptrdiff_t;
    suseconds_t ta_field_;
    wchar_t ta_field_wchar_t;
    wint_t ta_field_wint_t;
#endif    
};

//typedef unsigned long int unsigned_long_int;
//typedef signed long int signed_long_int;


int sizeof_unsigned_long_int() {
    unsigned long int v = -1;
    if (v > 0) {
        return sizeof(unsigned long int);
    } else {
        return -((long int)sizeof(unsigned long int));
    }
}

unsigned long int test__unsigned_long_int(unsigned long int value) {
    return ~value;
}

int sizeof_signed_long_int() {
    signed long int v = -1;
    if (v > 0) {
        return sizeof(signed long int);
    } else {
        return -((long int)sizeof(signed long int));
    }
}

signed long int test__signed_long_int(signed long int value) {
    return ~value;
}


#define GENERATE_SIZEOF_TEST(dt_name)\
int sizeof_##dt_name() {\
      return sizeof(dt_name);\
} 

#define GENERATE_INTEGER_TEST(dt_name)\
    dt_name test__##dt_name(dt_name value) {\
      return ~value;\
    } \
int sizeof_##dt_name() {\
    dt_name v = -1;\
    if (v > 0) {\
      return sizeof(dt_name);\
    } else {\
      return -((long int)sizeof(dt_name));\
    } \
}\

    
//#define __USE_POSIX    
#ifdef GENERATE_POSIX_TESTS    

GENERATE_INTEGER_TEST(int8_t)
GENERATE_INTEGER_TEST(u_int8_t)
GENERATE_INTEGER_TEST(int16_t)
GENERATE_INTEGER_TEST(u_int16_t)
GENERATE_INTEGER_TEST(int32_t)
GENERATE_INTEGER_TEST(u_int32_t)
GENERATE_INTEGER_TEST(int64_t)
GENERATE_INTEGER_TEST(u_int64_t)
GENERATE_INTEGER_TEST(blkcnt_t)
GENERATE_INTEGER_TEST(blksize_t)
//TODO how to test dataType char * ??         
GENERATE_SIZEOF_TEST(caddr_t)

GENERATE_INTEGER_TEST(cc_t)
GENERATE_INTEGER_TEST(clock_t)
GENERATE_INTEGER_TEST(dev_t)
#ifdef linux
GENERATE_INTEGER_TEST(eventfd_t)
#endif
GENERATE_INTEGER_TEST(fsblkcnt_t)
GENERATE_INTEGER_TEST(fsfilcnt_t)
GENERATE_INTEGER_TEST(gid_t)
GENERATE_INTEGER_TEST(id_t)
GENERATE_INTEGER_TEST(in_addr_t)
GENERATE_INTEGER_TEST(in_port_t)
#ifdef ino64_t
GENERATE_INTEGER_TEST(ino64_t)
#endif
GENERATE_INTEGER_TEST(ino_t)
GENERATE_INTEGER_TEST(intptr_t)
GENERATE_INTEGER_TEST(key_t)
GENERATE_INTEGER_TEST(mode_t)
GENERATE_INTEGER_TEST(nfds_t)
GENERATE_INTEGER_TEST(nlink_t)
GENERATE_INTEGER_TEST(off_t)
GENERATE_INTEGER_TEST(pid_t)
GENERATE_INTEGER_TEST(ptrdiff_t)
#ifdef rlim_t
GENERATE_INTEGER_TEST(rlim_t)
#endif
GENERATE_INTEGER_TEST(sa_family_t)
GENERATE_INTEGER_TEST(size_t)
GENERATE_INTEGER_TEST(socklen_t)
GENERATE_INTEGER_TEST(speed_t)
GENERATE_INTEGER_TEST(ssize_t)
GENERATE_INTEGER_TEST(suseconds_t)
#ifdef apple
GENERATE_INTEGER_TEST(swblk_t)
#endif
GENERATE_INTEGER_TEST(tcflag_t)
GENERATE_INTEGER_TEST(time_t)
GENERATE_INTEGER_TEST(uid_t)
GENERATE_INTEGER_TEST(uintptr_t)
GENERATE_INTEGER_TEST(useconds_t)
GENERATE_INTEGER_TEST(wchar_t)
GENERATE_INTEGER_TEST(wint_t)
#endif    

#ifdef windows
GENERATE_INTEGER_TEST(HANDLE)
#endif    
        
#ifdef IDE_SETUP
Do not compile IDE Setup 
#endif


