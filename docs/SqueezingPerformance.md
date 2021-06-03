# Squeezing Performance

Performance should already be good enough for 99% of people, but to squeeze out more when necessary, there are some
tricks.

These are ordered from least to most inconvenient (and drastic)

## `@IgnoreError`

A huge performance improvement can be gained by telling JNR-FFI to not save the last errno. By default, JNR-FFI will
always save the last errno for your convenience and for better debugging, this does have a cost to performance which, if
you're willing to lose some debugging information, can be omitted.

You can do this by using the annotation `@IgnoreError` on each method of your library you want to ignore, or
alternatively can be done library-wide by using the `LibraryOption.IgnoreError` `LibraryOption` at load time of your
library. You can also do the exact opposite using the `@SaveError` annotation and the `LibraryOption.SaveError`
`LibraryOption`. It is best to combine these together, most often done by annotating the performance sensitive methods
with `@IgnoreError` and keeping the default behavior of saving the error on.

## Use Parameter Annotations such as `@In` and `@Out`

The main performance overhead is in the conversion from Java types to native types, ie going to and from the native
world. The `jnr.ffi.annotations` package contains many annotations you can use to increase information to JNR-FFI about
how it should handle conversion of types. For example an `@In` parameter is one that is passed from Java to native and
not expected to be used again, this tells JNR-FFI to avoid the reconverting back from native to Java since it's not
necessary. Likewise for `@Out` parameters, which are expected to be created by native and used by Java but never read to
by native and thus do not need reconversion. There exist many such annotations that, when used correctly, can increase
performance by informing JNR-FFI to avoid unnecessary operations. Use care when adding these annotations though, as
incorrect usage could lead to unexpected behavior.

## Use Only What is Necessary From the Native Library

This is more of a general best practice, but will have performance improvements on initial library loading and will mean
fewer mappings to write and of course less code to maintain.

Try to use a more lean approach to how you create your library interfaces keeping as few functions as necessary, when in
doubt comment it out!

## Use Smaller Libraries if Possible

This too is a best practice but is sometimes unavoidable especially if it's not your own library and of course it's
probably better (regarding complexity and points of failure) to load one large library than many smaller ones.

This will also mostly be a performance improvement at library load time and not on function call performance.

Note that size in this case can be both number of exported symbols and size on disk of the library.

## Only Call to Native When Necessary

This is more drastic, but technically also a best practice. By going to the native world you are giving up a lot of
great advantages such as exceptions and debugging and putting yourself into more risks, such as VM crashes. This is a
sacrifice that you should not be giving up lightly, and you should be doing so only when absolutely necessary. This is
mentioned more in the [Why Use JNR-FFI document](./WhyUseJNR.md), which goes over the costs of going native and why you
should and shouldn't. Whenever and wherever possible try to reduce leaving the JVM unless absolutely necessary as it is
often more performant (and safer) to remain on the JVM.
