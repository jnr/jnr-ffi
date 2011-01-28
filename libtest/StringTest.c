
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
    strcat(dst, src);
}
