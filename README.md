# JNR-FFI [![Build Status](https://travis-ci.org/jnr/jnr-ffi.svg)](https://travis-ci.org/jnr/jnr-ffi)

[JNR-FFI](https://github.com/jnr/jnr-ffi) is a Java library for loading native libraries without writing JNI code by
hand, or using tools such as SWIG.

## Installation

Latest version:
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jnr/jnr-ffi/badge.svg)](https://search.maven.org/artifact/com.github.jnr/jnr-ffi)

Apache Maven:

```xml
<dependency>
  <groupId>com.github.jnr</groupId>
  <artifactId>jnr-ffi</artifactId>
  <version>x.y.z</version>
</dependency>
```

Gradle Kotlin:

```kotlin
implementation("com.github.jnr:jnr-ffi:x.y.z")
```

Gradle Groovy:

```groovy
implementation 'com.github.jnr:jnr-ffi:x.y.z'
```

## Example

```java
import jnr.ffi.LibraryLoader;

public class HelloWorld {
    public interface LibC { // A representation of libC in Java
        int puts(String s); // mapping of the puts function, in C `int puts(const char *s);`
    }

    public static void main(String[] args) {
        LibC libc = LibraryLoader.create(LibC.class).load("c"); // load the "c" library into the libc variable

        libc.puts("Hello World!"); // prints "Hello World!" to console
    }
}
```

View more details in [our user documentation](./docs/README.md).

## LICENSE

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
