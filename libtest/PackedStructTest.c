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

#include <inttypes.h>
#include <stdio.h>
#include <stdbool.h>
#include <string.h>

#pragma pack(push, 2)
struct pack2  {
    uint32_t i;
    uint64_t l;
};

struct pack2_small {
    uint8_t tiny;
    uint8_t tiny2;
    uint32_t deadbeef;
};
#pragma pack(pop)

#if defined(__APPLE__)
    #pragma pack(push, 2)
#endif

struct pack2_on_osx {
    uint32_t i;
    uint64_t l;
};

#if defined(__APPLE__)
    #pragma pack(pop)
#endif

struct pack2*
packedstruct_make_struct(uint32_t i, uint64_t l)
{
    static struct pack2 s;
    memset(&s, 0, sizeof(s));
    s.i = i;
    s.l = l;
    return &s;
}

struct pack2_small*
packedstruct_make_tiny(uint8_t v1, uint8_t v2)
{
    static struct pack2_small s;
    memset(&s, 0, sizeof(s));
    s.tiny = v1;
    s.tiny2 = v2;
    s.deadbeef = 0xdeadbeef;
    return &s;
}

struct pack2_on_osx*
packedstruct_make_packed_on_osx(uint32_t i, uint64_t l)
{
    static struct pack2_on_osx s;
    memset(&s, 0, sizeof(s));
    s.i = i;
    s.l = l;
    return &s;
}
