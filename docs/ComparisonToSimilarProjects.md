# Comparison to Similar Projects

JNR-FFI is by no means the first or only library or tool that aims to connect Java to the native world. There exist many
projects similar to JNR-FFI which approach this problem in various ways. Below are *some* of the popular ones and how
they compare to JNR-FFI. Of course, we try our best to be as objective as possible, but of course there will be some
level of bias given the circumstances.

Before choosing which library to use to connect to the native world, make sure that going native is the right choice for
you and that you are aware of the costs that doing so incurs by reading the document [Why Use JNR.](./WhyUseJNR.md)

## JNA (Java Native Access)

[JNA (Java Native Access)](https://github.com/java-native-access/jna) is the closest library to JNR-FFI. JNA has the
same core goal, easy access to native libraries. JNA also has a similar approach to doing this by making users write
mappings and using the calls to the mapped functions as requests to call to the native library functions. JNA even has
quite similar APIs. Despite this though, there are some important differences that you should take into consideration
before choosing which one to use.

### Why use JNR-FFI

JNR-FFI has a much more complete and modern API. JNR-FFI encourages you to use type alias annotations as much as
possible to help keep width and size of parameters correct on different platforms. On JNA you need to create a
special `SizeT` class type for your `size_t` types but with JNR-FFI all you need is an annotation `@size_t`. JNA
requires you to create inner classes to support a by-reference parameter whereas in JNR-FFI you can use any of the
`ByReference` classes such as `DoubleByReference` or `PointerByReference`.

JNR-FFI has significantly superior performance, nearing that of hand-written JNI in some cases, even in comparison to
JNA's *"direct mapping"*.
[Alexander Zakusylo has an excellent benchmark showing this](https://github.com/zakgof/java-native-benchmark). Of
course, performance is only a small part of the equation, and the difference is likely negligible for many use cases,
however, if performance is a top priority for you, JNR-FFI is the superior choice.

### Why use JNA

JNA is much more mature than JNR-FFI. With more commits, more contributors, more users, and used in projects by large
companies, JNA has a larger and more vibrant community. If you have a question with JNA, chances are, it's already been
asked and answered. JNR-FFI in comparison is younger and smaller but still used by many including
JRuby, [see who uses JNR-FFI here.](./ProjectsUsingJNR.md)

Somewhat related to its better maturity, JNA has better overall documentation too. Again, if you have a question with
JNA, there's a good chance it's answered by the documentation or at least clarified by it. This also makes debugging
with JNA generally easier because you can skim through the code with the well written JavaDoc comments to guide you.
This is something JNR-FFI is aiming to improve on (these documents are a start and testament to that resolve), but JNA
is still better by some margin in this regard.

## Project Panama

Many years ago the OpenJDK team announced [Project Panama](https://openjdk.java.net/projects/panama/), an official
addition to the JDK itself to provide better interaction with native libraries. This has been many years in the making
and seen many delays and hiccups but may prove to be a great solution to the problem that can be built into the JDK
itself. However, Project Panama currently (as of May 2021) has some serious shortcomings in comparison to its
competitors including JNR-FFI.

### Why use JNR-FFI

Unlike Project Panama, JNR-FFI is available today to use by all and is in fact used in production
code [on many projects including JRuby](./ProjectsUsingJNR.md). While JNA may be more mature than JNR-FFI, both are
significantly more mature and tested than Project Panama which is not yet officially released and has seen delays
before.

Even if Project Panama was released today, it would likely require the latest JDK to be usable on, leaving much of the
Java user-base out of its functionality. JNR-FFI requires only requires a JDK level of 8 or higher meaning even projects
still stuck behind on older JDKs can enjoy the benefits JNR-FFI brings.

Another thing to note is that JNR-FFI is constantly being updated and improved and gaining access to those improvements
is as easy as changing a couple of lines in your `build.gradle` or `pom.xml`. Updating your JDK (which Project Panama
will be built into) is not as straightforward and updates to Project Panama, or even the JDK for that matter, may be
less frequent or impressive than those from JNR-FFI.

### Why use Project Panama

Theoretically Project Panama should have significant improvements over its competition by the fact it will likely be
built into the platform. This means that Project Panama will probably have a close connection to the JDK and even JVMs
in general, improving usability and performance since they can change the JDK to fit in with Project Panama. If this is
indeed the case, then the reasons to use anything *but* Project Panama would be very slim.

Similar to the above, since Project Panama is built by those who develop the JDK itself, they're probably some of the
best Java developers in the world with the greatest knowledge of the Java platform as a whole. Thus, their creation
(Project Panama) is likely to be of a very high quality and likely more well engineered and developed than others. This
is of course only an assumption, but one that is at least somewhat sound, especially given the previous point about
Project Panama's deep integration with the platform as a whole as well.

## JNI (Java Native Interface)

JNI (Java Native Interface) is the oldest of all and is the original way of connecting Java to the native world. JNI was
designed early in Java's development and growth and was for a long time, the only way to connect Java to the native
world. However, there are some significant and important issues to be aware of with JNI, which themselves lead to the
popularity of all the previous tools.

### Why use JNR-FFI

Using JNR-FFI means going purely Java, you don't need to write tedious error-prone JNI C code. This is especially true
if you are not the developer of the native library you wish to call and are just using it as an API, something that is a
very common use case. In such a use case with JNI, you would still need to write JNI code in C and have to compile it
and ensure its correctness. By using JNR-FFI you are essentially cutting your possible code-base size significantly (in
comparison to a JNI based approach) and using only Java, something many Java developers would (obviously) find very
appealing.

JNR-FFI is generally more cross-platform compatible, at least given your library exists on all platforms. Of course this
assumes cross-platform availability of your library but even if that is only partially true, it is still better than JNI
which requires you to *compile* the C code for all platforms you intend to support, essentially completely stripping
away the cross-platform ease of Java development completely. With JNR-FFI, you can at least never have to worry about
compiling C libraries for different platforms.

JNR-FFI is very fast, nearly as fast as JNI in some cases and with none of the pain points. Writing *good* JNI code that
is actually performant and safe is more difficult than it looks. JNR-FFI alleviates all of that and is effectively  
just as fast and in some cases comparable, providing you with all the benefits and none of the problems.

### Why use JNI

The arguments for using JNI over JNR-FFI or any other similar alternative listed above are quite weak. In almost every
case, you will want to use something that *isn't* JNI, but there are a couple of arguments for JNI nonetheless.

For size conscious applications, a JNI implementation will likely provide the smallest size, especially if the supported
platforms are few. This is a rare occurrence but Java is known to run on even small smart cards or other platforms where
size requirements can be an active concern. Depending only on a minimal JDK and some custom JNI libraries is *much*
less size consuming than any non-JNI based alternative.

JNI is the most performant way of accessing native libraries with Java. This is true, but only gives part of the
picture. Firstly, JNR-FFI is extremely performant, in some cases coming comparable to hand-written JNI. Secondly, and
more importantly, writing **performant** *and* **safe** JNI code is difficult and very error-prone. The development cost
necessary to gain such a small improvement in performance is for most and seen by most as a bad trade-off. Nevertheless,
if the absolute performance limits of Java are what is necessary for you at *any* cost, then JNI is more suitable for
you.