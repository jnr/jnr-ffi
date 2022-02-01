#include "../types.h"

typedef struct NumericStruct_t {
    int8_t val_int8_t;
    int16_t val_int16_t;
    int32_t val_int32_t;
    long val_long;
    int64_t val_int64_t;

    uint8_t val_uint8_t;
    uint16_t val_uint16_t;
    uint32_t val_uint32_t;
    ulong val_ulong;
    uint64_t val_uint64_t;

    float val_float;
    double val_double;

    bool val_bool;
    Enum val_Enum;
    pointer val_pointer;
} NumericStruct;

typedef union NumericUnion_t {
    int8_t val_int8_t;
    int16_t val_int16_t;
    int32_t val_int32_t;
    long val_long;
    int64_t val_int64_t;

    uint8_t val_uint8_t;
    uint16_t val_uint16_t;
    uint32_t val_uint32_t;
    ulong val_ulong;
    uint64_t val_uint64_t;

    float val_float;
    double val_double;

    bool val_bool;
    Enum val_Enum;
    pointer val_pointer;
} NumericUnion;

typedef struct NestedStruct_t {
    NumericStruct inner_NumericStruct;
    NumericUnion inner_NumericUnion;

    NumericStruct *ptr_NumericStruct;
    NumericUnion *ptr_NumericUnion;
} NestedStruct;

typedef union NestedUnion_t {
    NumericStruct inner_NumericStruct;
    NumericUnion inner_NumericUnion;

    NumericStruct *ptr_NumericStruct;
    NumericUnion *ptr_NumericUnion;
} NestedUnion;
