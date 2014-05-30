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

#ifndef __mips__
# include <stdint.h>
#endif

#if !defined(_STDINT_H) && !defined(_SYS__STDINT_H_)
typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed long long int64_t;
#endif

#define REF(T) void ref_##T(T arg, T* result) { *result = arg; }
#define ADD(T) void ref_add_##T(T arg1, T arg2, T* result) { *result = arg1 + arg2; }
#define SUB(T) void ref_sub_##T(T arg1, T arg2, T* result) { *result = arg1 - arg2; }
#define MUL(T) void ref_mul_##T(T arg1, T arg2, T* result) { *result = arg1 * arg2; }
#define DIV(T) void ref_div_##T(T arg1, T arg2, T* result) { *result = arg1 / arg2; }
#define TEST(T) ADD(T) SUB(T) MUL(T) DIV(T) REF(T)

TEST(int8_t);
TEST(int16_t);
TEST(int32_t);
TEST(int64_t);
TEST(float);
TEST(double);

