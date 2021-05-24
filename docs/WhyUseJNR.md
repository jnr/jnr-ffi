# Why Use JNR-FFI?

Before using library such as JNR-FFI or others that allow you to interact with native libraries you need to be fully
sure that this is something you actually need, as there are (sometimes hidden) costs to making such a decision.

If you're looking for why to use JNR-FFI over some other similar
library [see the document that aims to answer that exact question.](ComparisonToSimilarProjects.md)

## Why Go Native?

There are many reasons why one would consider interacting with the native world, they can often be summarized into:

* [I need to improve the performance of my application](#improving-performance)
* [I need to use some low level API that is as a native library such as OpenGL](#using-low-level-apis)

### Improving Performance

Going native does **not** guarantee that you will get improved performance, in fact it is often more beneficial to
improve your *Java* code than to go native. This is simply because switching to and from the JVM incurs a
**significant** overhead and disallows many JVM optimizations that have been developed over the last decades. Indeed, it
is not uncommon to have pure Java code (especially well optimized) perform much better than Java with native code,
including with well written JNI code.

If your only reason for thinking about going native is for performance improvements you may want to think carefully
about that as the performance improvement is not a guarantee, and the additional costs of adding native components to
your codebase is not insignificant, see [the below section for more.](#the-cost-of-leaving-the-jvm)

### Using Low Level APIs

This is the most common reason for going native, simply to use some low level API that the JDK does not provide (or at
least not well enough). This is a good enough reason to go native, especially to provide or create bindings to a native
library that you will be making extensive use of. Of course make sure that there do not already exist bindings for your
library, for example there exist multiple excellent Java bindings to OpenGL alleviating the need for going native on
your end as it has been handled for you. Also make sure that there isn't a library that provides the same or similar
functionality already in pure Java. For example, if all you need is file system manipulation then there exist many
libraries that provide that functionality very well including the JDK's own file system APIs.

If there isn't already an existing library for your needs then going to the native level is a good option especially if
a native library already does what you intend and all you'll need to do is interact with it. This is th best reason to
go the native level, however, be aware
of [the costs that you will incur by leaving the JVM and interacting with the native world.](#the-cost-of-leaving-the-jvm)

## The Cost of Leaving the JVM

Adding native components to your projects can have a significant and sometimes hidden cost that you may not be aware of
until you're already heavily invested. Knowing these costs early can help you be more informed about what decision to
make and about how different your development experience may be.

### Compatibility

By far the greatest cost is the reduced compatibility of your program. The mantra "Write once, run anywhere" that
applies to almost all Java programs (including yours without a native component) may no longer be true, and even if it
remains true, you still have to be *platform-aware*, something most Java developers are not used to.

There are three possibilities:

* The library may not exist for all platforms; you now have less supported platforms
* The library may not behave identically on all platforms; your program may behave differently on different platforms
* You may need to compile and distribute the library yourself for all your supported platforms; both of the above apply
  *AND* you need to compile and distribute the native library yourself, but you have full control

In all cases you will always have *some* Java code that will do some platform checking to behave differently on
different platforms and this in itself is strange to many Java developers. Remember that a *"platform"* includes both
Operating System *AND* CPU Architecture.

### JVM Crashes

### Increased Complexity

### Increased Knowledge Requirements

need to be knowledgable in C

### Reduced Performance

## A Changing Ecosystem