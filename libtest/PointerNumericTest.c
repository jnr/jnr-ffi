#include <sys/types.h>
#include <sys/param.h>

#ifndef __mips__
# include <stdint.h>
#endif

#if defined(__mips64) || defined(__PASE__)
# include <stdint.h>
#endif

#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

// Let's define the stdint.h typedefs ourselves if they can't be found
#if !defined(_STDINT_H_) && !defined(_STDINT_H) && !defined(_SYS__STDINT_H_) && !defined(_H_STDINT)
typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed long long int64_t;
typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef unsigned long long uint64_t;
#endif

typedef long double ldouble;
typedef unsigned long ulong;
typedef void* pointer;
typedef void* ptr;
typedef enum Enum_t {e0, e1, e2, e3} Enum;
typedef bool boolean;

#define RET(T) T ptr_num_ret_##T(void *p, int offset) { \
    T tmp; memcpy(&tmp, (ptr) (p + offset), sizeof(tmp)); return tmp; \
}
#define SET(T) void ptr_num_set_##T(void *p, int offset, T value) { \
    memcpy((ptr) (p + offset), &value, sizeof(value)); \
}

#define TEST(T) SET(T) RET(T)

TEST(int8_t);
TEST(int16_t);
TEST(int32_t);
TEST(long);
TEST(int64_t);

TEST(uint8_t);
TEST(uint16_t);
TEST(uint32_t);
TEST(ulong);
TEST(uint64_t);

TEST(float);
TEST(double);
TEST(ldouble);

TEST(boolean);
TEST(pointer);
TEST(Enum);

void *ptr_num_arr_get(void **array, int index) {
    return array[index];
}

void ptr_num_arr_set(void **array, int index, void *value) {
    array[index] = value;
}
