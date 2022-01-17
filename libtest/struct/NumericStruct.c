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


#define SET_VAL(TYPE) \
    TYPE struct_num_get_##TYPE(NumericStruct *s) { \
        return s->val_##TYPE; \
    }

#define GET_VAL(TYPE) \
    void struct_num_set_##TYPE(NumericStruct *s, TYPE v) { \
        s->val_##TYPE = v; \
    }

// ============================= Set Functions ======================

SET_VAL(int8_t);
SET_VAL(int16_t);
SET_VAL(int32_t);
SET_VAL(long);
SET_VAL(int64_t);

SET_VAL(uint8_t);
SET_VAL(uint16_t);
SET_VAL(uint32_t);
SET_VAL(ulong);
SET_VAL(uint64_t);

SET_VAL(float);
SET_VAL(double);

SET_VAL(bool);
SET_VAL(Enum);
SET_VAL(pointer);

// ============================= Get Functions ======================

GET_VAL(int8_t);
GET_VAL(int16_t);
GET_VAL(int32_t);
GET_VAL(long);
GET_VAL(int64_t);

GET_VAL(uint8_t);
GET_VAL(uint16_t);
GET_VAL(uint32_t);
GET_VAL(ulong);
GET_VAL(uint64_t);

GET_VAL(float);
GET_VAL(double);

GET_VAL(bool);
GET_VAL(Enum);
GET_VAL(pointer);

int struct_num_size() {
    return sizeof(NumericStruct);
}
