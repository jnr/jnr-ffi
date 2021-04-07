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

#include <stdio.h>
#ifndef __mips__
# include <stdint.h>
#endif

#if !defined (_STDINT_H_) && !defined(_STDINT_H) && !defined(_SYS__STDINT_H_)
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
