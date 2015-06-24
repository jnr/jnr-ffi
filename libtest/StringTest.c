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

#include <string.h>

int 
string_equals(const char* s1, const char* s2)
{
    return strcmp(s1, s2) == 0;
}

void 
string_set(char* s1, const char* s2)
{
    strcpy(s1, s2);
}

void
string_concat(char* dst, const char* src)
{
    char* ep = dst;
    while (*ep)
        ep++;
    strcpy(ep, src);
}

char*
string_duplicate(const char* s1)
{
    return strdup(s1);
}
