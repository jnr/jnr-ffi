# -*- makefile -*-
BUILD_OS := $(strip $(shell uname -s | tr '[:upper:]' '[:lower:]'))
OS ?= $(BUILD_OS)
CPU = $(shell uname -m | sed -e 's/i[345678]86/i386/')
MODEL = 32 # Default to 32bit compiles
PLATFORM = $(CPU)-$(OS)
ifeq ($(OS), sunos)
  OS = solaris
endif

# Default value of $OS on Windows is Windows_NT
ifeq ($(OS), Windows_NT)
    # that's how we detect x64...
    ifdef CommonProgramFiles(x86)
      OS = win64
    else
      OS = win32
    endif
endif

SRC_DIR = libtest
BUILD_DIR ?= build
TEST_BUILD_DIR = $(BUILD_DIR)/libtest
# Set defaults to unix (linux/solaris/bsd)
PREFIX = lib
LIBEXT = so
LIBNAME = $(PREFIX)test.$(LIBEXT)

export MACOSX_DEPLOYMENT_TARGET=10.5

CCACHE := $(strip $(realpath $(shell which ccache 2> /dev/null)))

TEST_SRCS = $(wildcard $(SRC_DIR)/*.c)
TEST_OBJS := $(patsubst $(SRC_DIR)/%.c, $(TEST_BUILD_DIR)/%.o, $(TEST_SRCS))

#
# Compiler/linker flags from:
#   http://weblogs.java.net/blog/kellyohair/archive/2006/01/compilation_of_1.html
JFLAGS = -fno-omit-frame-pointer -fno-strict-aliasing
OFLAGS = -O2 $(JFLAGS)
WFLAGS = -W -Wall -Wno-unused -Wno-parentheses
PICFLAGS = -fPIC
SOFLAGS = -shared -Wl,-O1
LDFLAGS += $(SOFLAGS)

IFLAGS = -I"$(BUILD_DIR)"
CFLAGS = $(OFLAGS) $(WFLAGS) $(IFLAGS) $(PICFLAGS) -D_REENTRANT

ifeq ($(OS), win32)
  CC = i686-w64-mingw32-gcc
  LDFLAGS += -Wl,--add-stdcall-alias -static-libgcc
  PREFIX =
  LIBEXT = dll
  PICFLAGS =
endif

ifeq ($(OS), win64)
  CC = x86_64-w64-mingw32-gcc
  LDFLAGS += -Wl,--add-stdcall-alias -static-libgcc
  PREFIX =
  LIBEXT = dll
  PICFLAGS =
endif

ifeq ($(OS), darwin)
  CC = gcc
  ifneq ($(findstring $(CPU), x86_64),)
    ARCHFLAGS = -arch x86_64
  endif
  ifneq ($(findstring $(CPU), ppc powerpc),)
    ARCHFLAGS = -arch ppc
  endif
  
  CFLAGS += -fno-common $(ARCHFLAGS)
  LDFLAGS = $(ARCHFLAGS) -dynamiclib
  LIBEXT = dylib
  PICFLAGS =
  SOFLAGS =
endif

ifeq ($(OS), linux)
  SOFLAGS += -Wl,-soname,$(LIBNAME)
endif

ifeq ($(OS), solaris)
  CC = /usr/sfw/bin/gcc -std=c99
  LD = /usr/ccs/bin/ld
  SOFLAGS = -shared -static-libgcc 
endif

ifeq ($(OS), aix)
  LIBEXT = a
  SOFLAGS = -shared -static-libgcc
  PICFLAGS += -pthread
endif

ifneq ($(findstring cygwin, $(OS)),)
  CFLAGS += -mno-cygwin -mwin32
  LIBEXT = dll
#  PREFIX =
  PICFLAGS=
endif
ifneq ($(findstring mingw, $(OS)),)
  LIBEXT = dll
  PICFLAGS=
endif
ifeq ($(CPU), sparcv9)
  MODEL = 64
endif

ifeq ($(CPU), amd64)
  MODEL = 64
endif

ifeq ($(CPU), x86_64)
  MODEL = 64
endif

# On platforms (linux, solaris) that support both 32bit and 64bit, force building for one or the other
ifneq ($(strip $(findstring $(OS), solaris)),)
  # Change the CC/LD instead of CFLAGS/LDFLAGS, incase other things in the flags
  # makes the libffi build choke
  CC += -m$(MODEL)
  LD += -m$(MODEL)
endif

LIBTEST = $(BUILD_DIR)/$(LIBNAME)

all:	$(LIBTEST)

$(TEST_BUILD_DIR)/%.o : $(SRC_DIR)/%.c
	@mkdir -p $(@D)
	$(CCACHE) $(CC) $(CFLAGS) -c $< -o "$@"

$(LIBTEST):  $(TEST_OBJS)
	$(CC) -o $@ $(LDFLAGS) $(TEST_OBJS) -lm

clean::
	# nothing to do - ant will delete the build dir

debug::
	@echo "SRCS=$(TEST_SRCS)"

print-%:
	@echo '$*=$($*)'
