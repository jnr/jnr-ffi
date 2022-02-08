# Memory Management

## Pointers
A `Pointer` instance stores reference to a native memory address. As it can
represent various data like struct, arrays, java objects, etc. it needs to
be used with caution since any illegal access can cause segfaults and JVM crash.
The `Pointer` class provides methods to return different representations of
the stored data, for example:
  - `getInt(long offset)`: Reads 32-bit int value at given offset
  - `getPointer(long offset, int size)`: Reads a pointer at given offset
  - `array()`: Returns an array if it backs this pointer

## Runtime
A `Runtime` instance for the loaded library can be obtained using
the `Runtime.getRuntime()` method. It gives access to important
services like `ObjectReferenceManager` and `MemoryManager`.

### MemoryManager
As the name suggests, it provides various methods to allocate memory
for use with native functions.
  - `allocate`: Allocates Java memory
  - `allocateDirect`: Allocates native memory
  - `allocateTemporary`: Allocates transient native memory

The `Memory` class also provides utility methods to handle memory
allocation for common use-cases. It uses `MemoryManager` internally.

### ObjectReferenceManager
Any native memory associated with a Java object is released as soon as
the object gets garbage-collected. An `ObjectReferenceManager` provides
handy methods to keep objects strongly-referenced as long as its native
memory is in use. This can be helpful while working with function pointers,
which are associated with lambda functions on the Java side. Use `add`
to register any object and use `remove` to dereference the registered object. 