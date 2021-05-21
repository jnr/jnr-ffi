# Comparison to Similar Projects

https://github.com/zakgof/java-native-benchmark

## JNA (Java Native Access)

The closest competition to JNR-FFI, much slower though, even when using direct mapping mode

### Why use JNR-FFI

Better performance, Better APIs

### Why use JNA

Better documentation, Larger community

## Project Panama

Technically not yet ready or usable

### Why use JNR-FFI

Actually production ready and usable today, constantly updated, usable from JDK8+

### Why use Project Panama

Theoretically built into the language, could be faster

## JNI (Java Native Interface)

### Why use JNR-FFI

No need to write tedious JNI C code, nearly as fast with none of the pain points, cross platform

### Why use JNI

The argument for JNI is quite weak

Full control over the code (can be a bad thing), can be faster than JNR-FFI *if written properly*