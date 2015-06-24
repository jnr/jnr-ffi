/*
 * Copyright (c) 2007 Wayne Meissner. All rights reserved.
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

#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include <stddef.h>
#include <stdint.h>

typedef char Signed8;
typedef short Signed16;
typedef int Signed32;
typedef long long Signed64;
typedef float Float32;
typedef double Float64;
typedef long SignedLong;

struct test1 {
    char b;
    short s;
    int i;
    long long j;
    SignedLong l;
    float f;
    double d;
    char string[32];
};

#define T(x, type) \
    type struct_field_##type(struct test1* t) { return t->x; } \
    struct type##Align { char first; type value; }; \
    type struct_align_##type(struct type##Align* a) { return a->value; }

T(b, Signed8);
T(s, Signed16);
T(i, Signed32);
T(j, Signed64);
T(f, Float32);
T(d, Float64);

long struct_field_SignedLong(struct test1* t) { return t->l; }
struct SignedLongAlign { char first; SignedLong value; };
long struct_align_SignedLong(struct SignedLongAlign* a) { return a->value; }

void 
struct_set_string(struct test1* t, char* s) 
{
    strcpy(t->string, s);
}

struct test1*
struct_make_struct(char b, short s, int i, long long ll, float f, double d) 
{
    static struct test1 t;
    memset(&t, 0, sizeof(t));
    t.b = b;
    t.s = s;
    t.i = i;
    t.j = ll;
    t.f = f;
    t.d = d;
    return &t;
}

struct foo {
  unsigned long l1,l2, l3;
};

int
fill_struct_from_longs(unsigned long l1, unsigned long l2, struct foo* s, unsigned long l3)
{
    s->l1 = l1;
    s->l2 = l2;
    s->l3 = l3;

  return 0;
}

#define STRUCT_ALIGNMENT(alignment)             \
    struct StructAlignment##alignment {         \
        uint8_t  f0;                            \
        uint16_t f1;                            \
        uint32_t f2;                            \
        uint64_t f3;                            \
        void    *f4;                            \
        };                                      \
                                                \
    int struct_alignment_size_##alignment() {                           \
        return sizeof(struct StructAlignment##alignment);               \
    }                                                                   \
                                                                        \
    int struct_alignment_field_offset_##alignment(int field) {          \
        switch(field) {                                                 \
        case 0:                                                         \
            return offsetof(struct StructAlignment##alignment, f0);     \
        case 1:                                                         \
            return offsetof(struct StructAlignment##alignment, f1);     \
        case 2:                                                         \
            return offsetof(struct StructAlignment##alignment, f2);     \
        case 3:                                                         \
            return offsetof(struct StructAlignment##alignment, f3);     \
        case 4:                                                         \
            return offsetof(struct StructAlignment##alignment, f4);     \
        default:                                                        \
            return -1;                                                  \
        }                                                               \
    }                                                                   \

#pragma pack(push, 1)
STRUCT_ALIGNMENT(1)
#pragma pack(pop)

#pragma pack(push, 2)
STRUCT_ALIGNMENT(2)
#pragma pack(pop)

#pragma pack(push, 4)
STRUCT_ALIGNMENT(4)
#pragma pack(pop)

#pragma pack(push, 8)
STRUCT_ALIGNMENT(8)
#pragma pack(pop)

#pragma pack(push, 16)
STRUCT_ALIGNMENT(16)
#pragma pack(pop)
