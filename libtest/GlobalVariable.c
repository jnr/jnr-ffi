/*
 * Copyright (C) 2007-2010 Wayne Meissner
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

#include <sys/types.h>
#include <stdint.h>

typedef int8_t s8;
typedef uint8_t u8;
typedef int16_t s16;
typedef uint16_t u16;
typedef int32_t s32;
typedef uint32_t u32;
typedef int64_t s64;
typedef uint64_t u64;
typedef signed long sL;
typedef unsigned long uL;
typedef float f32;
typedef double f64;
#if !defined(__OpenBSD__)
typedef unsigned long ulong;
#endif
typedef void* pointer;
typedef void* P;

#define GVAR(T) \
    extern T gvar_##T; \
    T gvar_##T = (T) -1; \
    T gvar_##T##_get() { return gvar_##T; }; \
    void gvar_##T##_set(T v) { gvar_##T = v; }

GVAR(s8);
GVAR(u8);
GVAR(s16);
GVAR(u16);
GVAR(s32);
GVAR(u32);
GVAR(s64);
GVAR(u64);
GVAR(long);
GVAR(ulong);
GVAR(pointer);

struct gstruct {
    long data;
};

struct gstruct gvar_gstruct = { -1 };

struct gstruct*
gvar_gstruct_get(void)
{
    return &gvar_gstruct;
}

void
gvar_gstruct_set(const struct gstruct* val)
{ 
    gvar_gstruct = *val;
}
