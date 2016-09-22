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
int testClosureNull(int (*closure)(void), int defaultValue)
{
    return closure ? (*closure)() : defaultValue;
}
void testClosureVrV(void (*closure)(void))
{
    (*closure)();
}
char testClosureVrB(char (*closure)(void))
{
    return (*closure)();
}
short testClosureVrS(short (*closure)(void))
{
    return (*closure)();
}
int testClosureVrI(int (*closure)(void))
{
    return (*closure)();
}
long long testClosureVrL(long long (*closure)(void))
{
    return (*closure)();
}
float testClosureVrF(float (*closure)(void))
{
    return (*closure)();
}
double testClosureVrD(double (*closure)(void))
{
    return (*closure)();
}
void testClosureBrV(void (*closure)(char), char a1)
{
    (*closure)(a1);
}
void testClosureSrV(void (*closure)(short), short a1)
{
    (*closure)(a1);
}
void testClosureIrV(void (*closure)(int), int a1)
{
    (*closure)(a1);
}
void testClosureLrV(void (*closure)(long long), long long a1)
{
    (*closure)(a1);
}
void testClosureFrV(void (*closure)(float), float a1)
{
    (*closure)(a1);
}
void testClosureDrV(void (*closure)(double), double a1)
{
    (*closure)(a1);
}

struct StructClosureIrV {
    void (*closure)(int);
};

void testStructClosureIrV(struct StructClosureIrV *s, int a1)
{
    (*s->closure)(a1);
}
//
// These macros produce functions of the form:
// testClosureBIrV(void (*closure)(char, int), char a1, int a2) {}
//
#define C2_(J1, J2, N1, N2) \
void testClosure##J1##J2##rV(void (*closure)(N1, N2), N1 a1, N2 a2) \
{ \
    (*closure)(a1, a2); \
}

#define C2(J, N) \
    C2_(B, J, char, N) \
    C2_(S, J, short, N) \
    C2_(I, J, int, N) \
    C2_(L, J, long long, N) \
    C2_(F, J, float, N) \
    C2_(D, J, double, N) \


C2(B, char);
C2(S, short);
C2(I, int);
C2(L, long long);
C2(F, float);
C2(D, double);

#define C3_(J1, J2, J3, N1, N2, N3) \
void testClosure##J1##J2##J3##rV(void (*closure)(N1, N2, N3), N1 a1, N2 a2, N3 a3) \
{ \
    (*closure)(a1, a2, a3); \
}


#define C3(J, N) \
    C3_(B, J, B, char, N, char) \
    C3_(S, J, S, short, N, short) \
    C3_(I, J, I, int, N, int) \
    C3_(L, J, L, long long, N, long long) \
    C3_(F, J, F, float, N, float) \
    C3_(D, J, D, double, N, double) \

C3(B, char);
C3(S, short);
C3(I, int);
C3(L, long long);
C3(F, float);
C3(D, double);
C3_(B, S, I, char, short, int);
C3_(B, S, L, char, short, long long);
C3_(L, S, B, long long, short, char);
C3_(L, B, S, long long, char, short);


