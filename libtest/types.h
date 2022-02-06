#include <stdio.h>
#include <stdbool.h>
#if !defined(__mips___) || defined(__PASE__)
# include <stdint.h>
#endif

// Let's define the stdint.h typedefs ourselves if they can't be found
#if !defined (_STDINT_H_) && !defined(_STDINT_H) && !defined(_SYS__STDINT_H_) && !defined(_H_STDINT)
typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed long long int64_t;
typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef unsigned long long uint64_t;
#endif

typedef unsigned long ulong;
typedef long double ldouble;
typedef void* pointer;
typedef enum Enum_t {e0, e1, e2, e3} Enum;