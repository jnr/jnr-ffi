jnr-ffi [![Build Status](https://travis-ci.org/jnr/jnr-ffi.png)](https://travis-ci.org/jnr/jnr-ffi)
======

[jnr-ffi](https://github.com/jnr/jnr-ffi) is a java library for loading native libraries without writing JNI code by hand, or using tools such as SWIG.

Example
------

    package helloworld;

    import jnr.ffi.LibraryLoader;

    public class HelloWorld {
        public static interface LibC {
            int puts(String s);
        }

        public static void main(String[] args) {
            LibC libc = LibraryLoader.create().library("c").load(LibC.class);

            libc.puts("Hello, World");
        }
    }

Supported Types
------

All java primitives are mapped simply to the equivalent C types.

* byte - 8 bit signed integer
* short - 16 bit signed integer
* int - 32 bit signed integer
* long - natural long (i.e. 32 bits wide on 32 bit systems, 64 bit wide on 64bit systems)
* float - 32 bit float
* double - 64 bit float

The width and/or signed-ness of these basic types can be specified using one of the type alias annotations.
 e.g.

    // Use the correct width for the result from getpid(3)
    @pid_t long getpid();

    // read(2) returns a signed long result, and its length parameter is an unsigned long
    @ssize_t long read(int fd, Pointer data, @size_t long len);


In addition, the following java types are mapped to a C pointer

* String - equivalent to "const char *"
* Pointer - equivalent to "void *"
* Buffer - equivalent to "void *"

