# Getting Started

## Hello World

Hello World is as easy as:

```java
import jnr.ffi.LibraryLoader;

public class HelloWorld {
    public interface LibC { // A representation of libC in Java
        int puts(String s); // mapping of the puts function, in C `int puts(const char *s);`
    }

    public static void main(String[] args) {
        LibC libc = LibraryLoader.create(LibC.class).load("c"); // load the "c" library into the libc variable

        libc.puts("Hello World!"); // prints "Hello World!" to console
    }
}
```

## Step-by-Step

The mapping process is simple even for larger, more complex use cases.

For the example below we'll use some of the C functions regarding environment variables.

### 1. Create an interface which maps the desired functions of the native library

We want to use these C functions in Java:

```c
int setenv(const char *name, const char *value, int overwrite);

int unsetenv(const char *name);

char *getenv(const char *name);

int clearenv(void);
```

We do so by creating an interface that represents the native library and add the functions with the type mappings from C
to Java, all mappings can be seen in the [Type Mappings document](TypeMappings.md).

This results in:

```java
public interface LibC {
    int setenv(String name, String value, boolean overwrite); // overwrite can be int but boolean makes more sense

    int unsetenv(String name);

    String getenv(String name);

    int clearenv();
}
```

We can add annotations for improved performance to tell JNR-FFI about how we will use parameters so it knows how to
optimize conversions. You can read more about this in the [Squeezing Performance document](SqueezingPerformance.md).
This is not necessary (as is stated in the [Squeezing Performance document](SqueezingPerformance.md)) but demonstrates
the flexibility and control JNR-FFI provides you if you have use for it.

```java
public interface LibC {
    int setenv(@In String name, @In String value, @In boolean overwrite);

    int unsetenv(@In String name);

    String getenv(@In String name);

    int clearenv();
}
```

### 2. Load the native library

Next we need to tell JNR-FFI how to load our library:

```java
import java.util.HashMap;

import jnr.ffi.LibraryLoader;

public class Example {
    public static void main(String[] args) {
        // Add library options to customize library behavior
        Map<LibraryOption, Object> libraryOptions = new HashMap<>();
        libraryOptions.put(LibraryOption.LoadNow, true); // load immediately instead of lazily (ie on first use)
        libraryOptions.put(LibraryOption.IgnoreError, true); // calls shouldn't save last errno after call
        String libName = Platform.getNativePlatform().getStandardCLibraryName(); // platform specific name for libC

        LibC libc = LibraryLoader.loadLibrary(
                LibC.class,
                libraryOptions,
                libName
        );
    }
}
```

Note how in the above example we used a more complex way to load the library than before.

This way we can customize the behavior of the library in both how its loaded and how JNR-FFI calls it.

JNR-FFI is very flexible and gives you a lot of options and control over how the native library is called, see the
[`LibraryLoader`](../src/main/java/jnr/ffi/LibraryLoader.java) class for more.

Also note the `libName` variable instead of `"c"`, this is because some platforms (namely Windows) use a different name
for the C standard library. The [`Platform`](../src/main/java/jnr/ffi/Platform.java) class is aware of platform specific
information such as this.

### 3. Call the native library functions

Now we can use the native library as if it is a Java API, JNR-FFI will handle the calling and converting for us.

```java
public class Example {
    public static void main(String[] args) {
        Map<LibraryOption, Object> libraryOptions = new HashMap<>();
        libraryOptions.put(LibraryOption.LoadNow, true);
        libraryOptions.put(LibraryOption.IgnoreError, true);
        String libName = Platform.getNativePlatform().getStandardCLibraryName();

        LibC libc = LibraryLoader.loadLibrary(
                LibC.class,
                libraryOptions,
                libName
        );

        final String pwdKey = "PWD"; // key for working directory
        final String shellKey = "SHELL"; // key for system shell (bash, zsh etc)

        String pwd = libc.getenv(pwdKey);
        System.out.println(pwd); // prints current directory

        libc.setenv(pwdKey, "/", true); // set PWD to /
        System.out.println(libc.getenv(pwdKey)); // prints /

        libc.unsetenv(pwdKey); // unset PWD
        System.out.println(libc.getenv(pwdKey)); // prints null (it is null not the String "null")

        System.out.println(libc.getenv(shellKey)); // prints system shell, /bin/bash on most Unixes
        libc.clearenv(); // clear all environment variables
        System.out.println(libc.getenv(shellKey)); // prints null (it is null not the String "null")
        System.out.println(libc.getenv("_")); // even the special "_" environment variable is now null
    }
}
```

If you are interested in using POSIX functions in Java, check out our other
library [JNR-POSIX](https://github.com/jnr/jnr-posix), it uses JNR-FFI to call POSIX functions and is a good place to
see production code that uses JNR-FFI.
