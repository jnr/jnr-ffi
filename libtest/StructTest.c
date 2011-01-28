
#include <stdio.h>
#include <stdbool.h>
#include <stdint.h>
#include <string.h>

typedef char Signed8;
typedef short Signed16;
typedef int Signed32;
typedef long long Signed64;
typedef float Float32;
typedef double Float64;
typedef long SignedLong;

struct test1 {
    char b;
    short s;
    int i;
    long long j;
    SignedLong l;
    float f;
    double d;
    char string[32];
};

#define T(x, type) \
    type struct_field_##type(struct test1* t) { return t->x; } \
    struct type##Align { char first; type value; }; \
    type struct_align_##type(struct type##Align* a) { return a->value; }

T(b, Signed8);
T(s, Signed16);
T(i, Signed32);
T(j, Signed64);
T(f, Float32);
T(d, Float64);

Signed64 struct_field_SignedLong(struct test1* t) { return t->l; } 
struct SignedLongAlign { char first; SignedLong value; };
Signed64 struct_align_SignedLong(struct SignedLongAlign* a) { return a->value; }

void 
struct_set_string(struct test1* t, char* s) 
{
    strcpy(t->string, s);
}

struct test1*
struct_make_struct(char b, short s, int i, long long ll, float f, double d) 
{
    static struct test1 t;
    memset(&t, 0, sizeof(t));
    t.b = b;
    t.s = s;
    t.i = i;
    t.j = ll;
    t.f = f;
    t.d = d;
    return &t;
}
