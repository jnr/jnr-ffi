#include <stdio.h>
#include <stddef.h>
#include <string.h>

typedef struct _A {
    int x;
    char y;
} A;

typedef struct _B {
    char x;
    int y;
} B;

typedef struct _C {
    char x;
    char y;
    int z;
} C;

typedef struct _D {
    char x;
    long long y;
    int z;
} D;

typedef struct _E {
    char x;
    D y;
} E;

typedef struct _Array1 {
    D t[3];
} Array1;

typedef union _MyLargeInteger {

    struct {
        unsigned int LowPart;
        int HighPart;
    } u;
    long long QuadPart;
} MyLargeInteger;

typedef struct _F {
    int x;
    MyLargeInteger y;
} F;

typedef struct _G {
    char x;
    MyLargeInteger y;
    int z;
} G;

typedef struct _Array2 {
    G t[3];
} Array2;

typedef union _Union1 {
    int intVal[2];
    char ch[8];
    MyLargeInteger my;
    short ss[4];
    long long u;
} Union1;

typedef struct _H {
    char x[3];
} H;

typedef union _Union2 {
    H x[5];
} Union2;

typedef struct _J {
    short x;
    char y[3];
} J;

typedef union _Union3 {
    J x[5];
    char y[13];
} Union3;

struct Result {
    int align;
    int size;
};

#define DUMP(type)                              \
do {                                            \
typedef struct _AlignType##type {               \
    char c;                                     \
    type d;                                     \
} AlignType##type;                              \
if(strcmp(#type,name)==0){                      \
    res.align = offsetof(AlignType##type, d);   \
    res.size = sizeof(type);                    \
}                                               \
} while(0)

static struct Result getTypeDescription(const char * name) {
    struct Result res = {0, 0};
    DUMP(A);
    DUMP(B);
    DUMP(C);
    DUMP(D);
    DUMP(E);
    DUMP(Array1);
    DUMP(MyLargeInteger);
    DUMP(F);
    DUMP(G);
    DUMP(Array2);
    DUMP(Union1);
    DUMP(H);
    DUMP(Union2);
    DUMP(J);
    DUMP(Union3);
    return res;
}

int getTypeSize(const char * name) {
    return getTypeDescription(name).size;
}

int getTypeAlign(const char * name) {
    return getTypeDescription(name).align;
}
