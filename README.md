[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.sooniln/fastcollect)](https://central.sonatype.com/artifact/io.github.sooniln/fastcollect)
[![javadoc](https://javadoc.io/badge2/io.github.sooniln/fastcollect/javadoc.svg)](https://javadoc.io/doc/io.github.sooniln/fastcollect)

# FastCollect

A library for high-performance primitive collections in the Kotlin ecosystem.

FastCollect can be seen as a response to [fastutil](https://github.com/vigna/fastutil) project, but for Kotlin. FastUtil
has served as the library of choice for representing primitive collections in Java for many years. While it is usable by
Kotlin on JVM platforms it ends up being awkward, as it does not support many common Kotlin patterns and idioms, and
also cannot be used outside the JVM ecosystem. FastCollect aims to solve these problems.

## Quick Start

FastCollect can be used as a drop in replacement for Kotlin standard library collections, and should provide immediate
memory and CPU improvements without any further work. However, many of the Kotlin standard library APIs are not flexible
enough to properly support primitives without boxing penalties. In order to avoid boxing penalties, FastCollect has
marked methods that force boxing as deprecated (for visibility in IDEs, the methods will still function exactly as
expected) and provided alternatives.

You can add FastCollect as a dependency in your project with:

#### Gradle

```groovy
implementation 'io.github.sooniln:fastcollect-kotlin:0.0.1'
```

#### Maven

```xml
<dependency>
    <groupId>io.github.sooniln</groupId>
    <artifactId>fastcollect-kotlin</artifactId>
    <version>0.0.1</version>
</dependency>
```

Note that it is generally preferable to keep FastCollect collections as specifically typed as possible. I.e. when
possible, prefer:
```kotlin
val set: IntHashSet
set = IntHashSet()
```
instead of:
```kotlin
val set: Set<Int>
set = IntHashSet()
```
This helps ensure that methods which avoid boxing penalties are appropriately available, and that extension methods work
as expected.

Using FastCollect types should be quite straightforward for anyone familiar with standard Kotlin/Java collections.
FastCollect provides ArrayList/ArrayDeque, HashSet, and HashMap analogues that can store primitives. FastCollect does
not currently support Maps with reference typed values, but this may come in a future release.

> [!NOTE]
> FastCollect does not currently support Byte/Short types for HashSet/HashMap, and does not support Float/Double keys
> for HashMap. This is done out of a desire to reduce binary size and bloat by eliminating use cases that are unlikely
> to be very common or useful. If you feel you have a compelling use case that is not currently supported, please reach
> out.

### ConcurrentModificationException

The standard Kotlin libraries may make reasonable efforts to throw ConcurrentModificationException if they detect
collections being modified in inappropriate ways. This already only a best effort, with no guarantees made, but
FastCollect makes even less of an effort in the interests of performance.

## Benchmarking

In benchmarking, FastCollect generally outperforms standard Kotlin collections by orders of magnitude, and usually
slightly out-performs fastutil (though the performance difference between fastutil and FastCollect is unlikely to amount
to much except perhaps in the tightest of loops). It's important to note that a key advantage of primitive collections
is not just less CPU usage, but far less memory usage, which has beneficial effects in all sorts of ways (for example
promoting less CPU usage since more data can now fit in various caches).

## Generated Code

FastCollect generates most of its collection classes from templates in order to reduce the amount of copy/pasted code
present. Note that contrary to many source control guidelines and common practice, this project checks the generated
code directly into the repository. While this is less than ideal in terms of the build pipeline, this project has public
APIs composed of generated code, and it is important for clients and users that the actual code (rather than just the
generation templates) is viewable, searchable, and parseable within the repository itself.

## Detecting Boxing

While FastCollect deprecates collection methods that lead to boxing, this does not help detect indirect usage of these
methods which may still be causing performance problems. Thus on JVM and native platforms, FastCollect supports setting
the 'fastcollect-throw-on-boxing' property which will cause FastCollect to crash and emit a stack trace if it detects
usage of methods which are causing boxing.

On JVM this is a system property, which can be set via a java flag: `java -Dfastcollect-throw-on-boxing=true ...`. For
native applications this can be set via a command line flag `--fastcollect-throw-on-boxing=true`.

## Notes on JVM Boxing and Escape Analysis

Modern JVM runtimes are quite good at analyzing and optimizing code while they're running. This means that in simple
scenarios the JVM can completely elide some boxing penalties even where you might expect to see them. For this reason,
switching from collection methods that imply boxing to FastCollect methods that prevent boxing may not always directly
improve performance. However, FastCollect methods that avoid boxing will never perform worse than methods that may box,
and for more complex code usage will likely deliver performance improvements, so it is always worth migrating to
FastCollect methods that avoid boxing where reasonable.
