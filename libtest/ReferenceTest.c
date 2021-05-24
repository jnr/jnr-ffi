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

#if !defined(__mips__) || defined(__PASE__)
# include <stdint.h>
#endif

#if !defined(_STDINT_H_) && !defined(_STDINT_H) && !defined(_SYS__STDINT_H_) && !defined(_H_STDINT)
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

