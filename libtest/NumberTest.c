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
#ifndef __mips__
# include <stdint.h>
#endif

#ifndef _STDINT_H
typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed long long int64_t;
typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef unsigned long long uint64_t;
#endif
typedef char Signed8;
typedef short Signed16;
typedef int Signed32;
typedef long long Signed64;
typedef float Float32;
typedef double Float64;
typedef long SignedLong;
typedef void* pointer;
typedef unsigned long ulong;

#define ADD(T) T add_##T(T arg1, T arg2) { return arg1 + arg2; }
#define SUB(T) T sub_##T(T arg1, T arg2) { return arg1 - arg2; }
#define MUL(T) T mul_##T(T arg1, T arg2) { return arg1 * arg2; }
#define DIV(T) T div_##T(T arg1, T arg2) { return arg1 / arg2; }
#define RET(T) T ret_##T(T arg1) { return arg1; }
typedef char* ptr;
#define TEST(T) ADD(T) SUB(T) MUL(T) DIV(T) RET(T)
TEST(int8_t);
TEST(int16_t);
TEST(int32_t);
TEST(int64_t);
TEST(uint8_t);
TEST(uint16_t);
TEST(uint32_t);
TEST(uint64_t);
TEST(float);
TEST(double);
TEST(long);
TEST(ulong);
TEST(Signed8);
TEST(Signed16);
TEST(Signed32);
TEST(Signed64);
TEST(SignedLong);
TEST(Float32);
TEST(Float64);
RET(pointer);
