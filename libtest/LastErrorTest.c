
#include <errno.h>

int setLastError(int error) {
    errno = error;
    return -1;
}
