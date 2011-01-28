
#include <stdio.h>
#include <stdint.h>

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
TEST(float);
TEST(double);
TEST(long);
