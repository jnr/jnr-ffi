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
#include <sys/param.h>
#ifndef __mips__
# include <stdint.h>
#endif

#ifdef __mips64
# include <stdint.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
typedef void* ptr;
typedef void* pointer;
#ifdef _WIN32
typedef char* caddr_t;
#endif
#if !defined(_STDINT_H_) && !defined(_STDINT_H) && !defined(_SYS__STDINT_H_)
typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed long long int64_t;
#endif
#define RET(T) T ptr_ret_##T(void* arg1, int offset) { \
    T tmp; memcpy(&tmp, (caddr_t) arg1 + offset, sizeof(tmp)); return tmp; \
}
#define SET(T) void ptr_set_##T(void* arg1, int offset, T value) { \
    memcpy((caddr_t) arg1 + offset, &value, sizeof(value)); \
}
#define TEST(T) SET(T) RET(T)

TEST(int8_t);
TEST(int16_t);
TEST(int32_t);
TEST(int64_t);
TEST(float);
TEST(double);
TEST(pointer);

void*
ptr_return_array_element(void **ptrArray, int arrayIndex) 
{
    return ptrArray[arrayIndex];
}

void
ptr_set_array_element(void **ptrArray, int arrayIndex, void *value)
{    
    ptrArray[arrayIndex] = value;
}

void*
ptr_from_buffer(void* ptr)
{
    return ptr;
}
void*
ptr_malloc(int size) 
{
    void* ptr = malloc(size);
    memset(ptr, 0, size);
    return ptr;
}
void
ptr_free(void* ptr)
{
    free(ptr);
}

#define swap(p1, p2) do { typeof(*p1) tmp__ = *p1; *p1 = *p2; *p2 = tmp__; } while (0)
void
ptr_reverse_l6(long* l1, long* l2, long* l3, long* l4, long* l5, long* l6)
{
    swap(l1, l6);
    swap(l2, l5);
    swap(l3, l4);
}

void
ptr_reverse_l5(long* l1, long* l2, long* l3, long* l4, long* l5)
{
    swap(l1, l5);
    (void)(l3);
    swap(l2, l4);
}


void
ptr_rotate_l5(long* l1, long* l2, long* l3, long* l4, long* l5)
{
    long tmp = *l1;
    swap(l5, l1);
    swap(l4, l5);
    swap(l3, l4);
    swap(l2, l3);
    *l2 = tmp;
}

void
ptr_rotate_l6(long* l1, long* l2, long* l3, long* l4, long* l5, long* l6)
{
    long t = *l1;
    swap(l6, l1);
    swap(l5, l6);
    swap(l4, l5);
    swap(l3, l4);
    swap(l2, l3);
    *l2 = t;
}
