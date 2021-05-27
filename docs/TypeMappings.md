# Type Mappings

# Primitive Types

All Java primitives are mapped to their size equivalent C types:

| C Type | Java Type | Size |
|--------|-----------|------|
| `char` | `byte` | 8 bit integer |
| `short` | `short` | 16 bit integer |
| `int` | `int` | 32 bit integer |
| `long` | `long` | natural long, 32 bits on 32 bit systems, 64 bits on 64 bit systems |
| `float` | `float` | 32 bit floating point |
| `double` | `double` | 64 bit floating point |

The signedness and width can be additionally modified using annotations for example, for C `long long` use a `long`
with the `@LongLong` annotation.

In addition to these types there exist numerous annotations for common C types for example `@size_t` on an `int` for C
`size_t`.

`boolean` can also be used in place of a C `int` where a boolean would be expected, check the function's documentation
before doing this.

# Complex Types

Things are just as straightforward and flexible with non-primitives too:

| C Type | Java Type |
|--------|-----------|
| `const char *` | `String` or `Pointer` |
| `void *` | `Pointer` or `Buffer` |
| C enum | Mostly `int` |

For parameters that are used by reference you can use the numerous `ByReference` classes such as
`IntByReference` for C `int *` or `PointerByReference` for C `void **`.

# Structs

Mapping and using C structs in JNR-FFI is very simple and straightforward.

The following C struct from sys/time.h

```c
struct timespec {
    time_t tv_sec;
    long int tv_nsec;
};
```

Will map to Java as:

```java
public class Timespec extends Struct {
    // Struct types can be found as inner classes of the Struct class
    public Struct.SignedLong tv_sec = new Struct.SignedLong();
    public Struct.SignedLong tv_nsec = new Struct.SignedLong();

    // Necessary constructor that takes a Runtime
    public Timespec(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    // You can add your own methods of choice
    public void setTime(long sec, long nsec) {
        tv_sec.set(sec);
        tv_nsec.set(nsec);
    }
}
```

Note that structs use their own types for primitives such as `Struct.Signed32` for a signed 32-bit integer or
`Struct.Double` for a 64-bit float (Java `double`). These types have functions that allow you to set and get the actual
value in addition to other useful utilities such as casting to other types or getting the `Pointer` representing the
memory of the struct field in question.

# Unions

Unions are just a special type of struct and look the same as structs from your point of view

The following contrived C union:

```c
union car {
    int price;
    char name[50];
};
```

Wil map to Java as:

```java
public class Car extends Union { // Extend Union instead of Struct
    // As with Struct, use the types from the Struct class
    public Struct.Signed32 price = new Struct.Signed32();
    public Struct.String name = new Struct.AsciiString(50);

    // Necessary constructor that takes a Runtime
    public Car(Runtime runtime) {
        super(runtime);
    }

    // You can add your own methods of choice
    public void setName(java.lang.String newName) {
        this.name.set(newName);
    }
}
```

# Callback/Function Types

Callback functions are easily mapped using the `@Delegate` annotation on the only method of a single method interface
representing the type of the function, similar to Java functional interfaces.

For the following contrived C code:

```c
typedef int MyCallback(void *data);

int setCallback(MyCallback *callback);
```

Will map to Java as:

```java
public interface ExampleLibrary {
    public interface MyCallback { // type representing callback
        @Delegate
        int invoke(Pointer data); // function name doesn't matter, it just needs to be the only function and have @Delegate
    }

    int setCallback(MyCallback callback);

}
```

# Global Variables

Global variables are uncommon in C libraries however JNR-FFI still supports them.

A variable is placed into the library interface as function with the same name as the variable and of type
`Variable`.