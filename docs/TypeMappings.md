# Type Mappings

## Numeric Types

### Signed Types

Signed numeric types map to their equivalent size Java primitive or boxed types:

| C Type | Java Type | Size |
|--------|-----------|------|
| `char` | `byte` or `Byte` | 8 bit integer |
| `short` | `short` or `Short` | 16 bit integer |
| `int` | `int` or `Integer` | 32 bit integer |
| `long` | `long` or `Long` or `NativeLong` | natural long, 32 bit integer on 32 bit systems, 64 bit integer on 64 bit systems |
| `long long` | `long` or `Long` | 64 bit integer |
| `float` | `float` or `Float` | 32 bit floating point |
| `double` | `double` or `Double` | 64 bit floating point |

Java `boolean` or `Boolean` can also be used in place of a C numeric type where a boolean would be expected, check the native function's documentation
before doing this. `0` maps to `false` and any other value will be `true`. Floating point types (`float` and `double`) are not supported in this regard.

### Unsigned Types

For native unsigned types you can use the same size Java type (`unsigned char` maps to `byte` or `Byte`) but you will not be able to fit the values greater than the maximum limit for the Java type. 

For example, an `unsigned char` can contain the value `220` but a Java `byte` cannot and will thus underflow to `-36`.

To remedy this you can use a larger size Java type with the corresponding annotation to let JNR-FFI to do the correct conversion. To fit the bounds of unsigned C types use the following table:

| C Type | Java Type | Notes |
|--------|-----------|------|
| `unsigned char` | `@u_int8_t byte` |
| `unsigned short` | `@u_int16_t int` |
| `unsigned int` | `@u_int32_t long` |

Unsigned 64 bit integers (such as `unsigned long` on 64bit systems and `unsigned long long`) are not yet supported to fit values beyond those of Java's `Long.MAX_VALUE`. [This is a documented issue.](https://github.com/jnr/jnr-ffi/issues/289)

### Enums

Native enums can be mapped using any Java integral numeric types such as `int` or `Int`, or by using a similarly valued Java enum to the native one.

For example the C enum:

```c
enum week{Mon, Tue, Wed, Thu, Fri, Sat, Sun};
```

can be mapped in Java as:

```java
public enum Week {MON, TUE, WED, THU, FRI, SAT, SUN;}
```

This works because they have the same "value" which, if undefined, is the order in which the enum entry appears.

If the C enum contains values, the values need to match in Java by implementing a mapping function:

```c
enum State {On = 2, Off = 4, Broken = 8};
```

should be mapped in Java as:

```java
    public static enum State implements EnumMapper.IntegerEnum {
        ON(2), OFF(4), BROKEN(8);

        private final int value;

        public State(int value) {this.value = value;}

        @Override
        public int intValue() {return value;} // mapping function
    }
```

C functions often use enums in "flags" and allow you to combine "flags" by logical OR-ing the values together. For example:

```c
void start_stream(stream_flags flags);
```

```c
// start paused and muted and allow latency adjustment
stream_flags flags = STREAM_START_PAUSED | 
                     STREAM_START_MUTED |
                     STREAM_ADJUST_LATENCY;
start_stream(flags);
```

The same can be done in Java also by OR-ing the values of the enum, but more elegantly, by using an `EnumSet` where such a combined enum would be expected:

```java
public void start_stream(EnumSet<stream_flags> flags);
```

```java
start_stream(EnumSet.of(
    STREAM_START_PAUSED, STREAM_START_MUTED, STREAM_ADJUST_LATENCY
));
```

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