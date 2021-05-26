# Projects Using JNR-FFI

## Our Projects

These are projects developed and maintained by (generally) the same team that develops and maintains JNR-FFI.

### JRuby

By far the largest, most substantial and most important project using JNR-FFI is JRuby, a JVM implementation of the Ruby
programming language. In fact JNR-FFI started out as a library for JRuby to solve Ruby's need for native system calls
and provide the same level of intractability with the native world as other Ruby implementations did. JRuby is JNR-FFI's
largest *"client"* and indeed the lead maintainers of JNR-FFI and the JNR project in general are also the lead
developers of JRuby currently employed by Red Hat. This is all to say that we are *"dogfooding"* JNR-FFI in our own
major large-scale project JRuby.

If you had doubts about whether this library or even the JNR project in general is professional or will survive the
foreseeable future, JRuby's deep dependence on JNR-FFI (and the JNR project) should eliminate those doubts.

### JNR Projects

The JNR Project has multiple libraries that use JNR-FFI to call to native libraries such as JNR-POSIX, JNR-Process and
JNR-ENXIO. Many of these themselves used in JRuby to help increase ease (and performance) of native library calls for
JRuby.

## Community Projects

These are projects developed by the community.

### [JRtMidi](https://github.com/basshelal/JRtMidi)

Java bindings to [RtMidi](https://github.com/thestk/rtmidi) written in Kotlin.

--------------------------------------------------

If you're using JNR-FFI and want to add your project here, make a pull request!