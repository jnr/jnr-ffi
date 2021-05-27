# Type Mappings

# Primitive Types

# Complex Types

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
    public DefaultNativeTimespec(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    // You can add your own methods of your choice
    public void setTime(long[] timespec) {
        tv_sec.set(timespec[0]);
        tv_nsec.set(timespec[1]);
    }
}
```

# Unions

Unions are just a special type of struct and look the same as structs from your point of view

The following contrived C union:

```c
union car {
  char name[50];
  int price;
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

    // You can add your own methods of your choice
    public void setName(java.lang.String newName) {
        this.name.set(newName);
    }
}
```

# Global Variables

See JNR-POSIX environ() as an example

# Callback/Function Types