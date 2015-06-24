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


#define MEMSET(buf, value, size) do { \
    int i; for (i = 0; i < size; ++i) buf[i] = value; \
} while(0)
#define MEMCPY(dst, src, size) do { \
    int i; for (i = 0; i < size; ++i) dst[i] = src[i]; \
} while(0)

#define FILL(JTYPE, CTYPE) \
void fill##JTYPE##Buffer(CTYPE* buf, CTYPE value, int size) { MEMSET(buf, value, size); }

#define COPY(JTYPE, CTYPE) \
void copy##JTYPE##Buffer(CTYPE* dst, CTYPE* src, int size) { MEMCPY(dst, src, size); }

#define FUNC(JTYPE, CTYPE) \
    FILL(JTYPE, CTYPE); \
    COPY(JTYPE, CTYPE)
            
FUNC(Byte, char);
FUNC(Short, short);
FUNC(Int, int);
FUNC(Long, long long);
FUNC(Float, float);
FUNC(Double, double);

