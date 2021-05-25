# Squeezing Performance

TODO @basshelal: this entire document needs a bit more knowledge of the library and the inner workings to be seen as
factually accurate, some of these are my own assumptions

Performance should already be good enough for 99% of people, but to squeeze out more, there are some tricks. This is
only of importance if your function is being called thousands to millions of times a second

The main overhead is in the conversion from Java types to native types, ie going to and from the native world

These are ordered from least to most inconvenient (and drastic)

## Load Library as Early as Possible

TODO @basshelal: Is this even true?? A quick glance at the code indicates LoadNow is never actually used!

This is probably more of a general best practice than anything, but it has a performance improvement on the first native
function call.

By default, JNR-FFI will lazily load the native library, that is, it will load it when you make your first call to the
native library. This makes the first call invoke more instructions than just the native call itself. It also means that
if your native library has any internal errors or if your mapping has problems, you can be informed about this earlier
rather than later.

For this use `LibraryOption.LoadNow` when loading your library such as below:

```java
public class Example {
    public static void main(String[] args) {
        Map<LibraryOption, Object> libraryOptions = new HashMap<>();
        libraryOptions.put(LibraryOption.LoadNow, true); // Use LibraryOption.LoadNow to avoid lazy loading
        String libName = Platform.getNativePlatform().getStandardCLibraryName();
        LibC libc = LibraryLoader.loadLibrary(LibC.class, libraryOptions, libName);
    }
}
```

## Use Only What is Necessary From the Native Library

This too is also more of a general best practice, but will have performance improvements on initial library loading and
will mean fewer mappings to write and of course less code to maintain.

Try to use a more lean approach to how you create your library interfaces keeping as few functions as necessary, when in
doubt comment it out!

## Use Smaller Libraries if Possible

This too is a best practice but is sometimes unavoidable especially if it's not your own library and of course it's
probably better (regarding complexity and points of failure) to load one large library then many smaller ones.

This will also mostly be a performance improvement at library load time and not on function call performance.

Note that size in this case can be both number of exported symbols and size on disk of the library.

## @IgnoreError

## Use Annotations such as @In and @Out

## Only call to native when absolutely necessary

## Use Pointer Over Specific Types