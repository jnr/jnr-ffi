# JNR-FFI [![Build Status](https://travis-ci.org/jnr/jnr-ffi.svg)](https://travis-ci.org/jnr/jnr-ffi)

[JNR-FFI](https://github.com/jnr/jnr-ffi) is a Java library for loading native libraries without writing JNI code by
hand, or using tools such as SWIG.

## Installation

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jnr/jnr-ffi/badge.svg)](https://search.maven.org/artifact/com.github.jnr/jnr-ffi)

Apache Maven:

```xml
<dependency>
  <groupId>com.github.jnr</groupId>
  <artifactId>jnr-ffi</artifactId>
  <version>2.2.3</version>
</dependency>
```

Gradle Kotlin:

```kotlin
implementation("com.github.jnr:jnr-ffi:2.2.3")
```

Gradle Groovy:

```groovy
implementation 'com.github.jnr:jnr-ffi:2.2.3'
```

## Example

```java
import jnr.ffi.LibraryLoader;

public class HelloWorld {
    public static interface LibC {
        int puts(String s);
    }

    public static void main(String[] args) {
        LibC libc = LibraryLoader.create(LibC.class).load("c");

        libc.puts("Hello, World");
    }
}
```

## Supported Types

All Java primitives are mapped simply to the equivalent C types.

* `byte` - 8 bit signed integer
* `short` - 16 bit signed integer
* `int` - 32 bit signed integer
* `long` - natural long (i.e. 32 bits wide on 32-bit systems, 64 bits wide on 64-bit systems)
* `float` - 32 bit float
* `double` - 64 bit float

The width and/or signed-ness of these basic types can be specified using one of the type alias annotations.
 e.g.

```c
// Use the correct width for the result from getpid(3)
@pid_t long getpid();

// read(2) returns a signed long result, and its length parameter is an unsigned long
@ssize_t long read(int fd, Pointer data, @size_t long len);
```

In addition, the following Java types are mapped to a C pointer

* String - equivalent to `const char *`
* Pointer - equivalent to `void *`
* Buffer - equivalent to `void *`

# LICENSE

```
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


  Alternatively, you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This code is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  version 3 for more details.

  You should have received a copy of the GNU Lesser General Public License
  version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
```