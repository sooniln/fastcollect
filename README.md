[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.sooniln/fastcollect-kotlin)](https://central.sonatype.com/artifact/io.github.sooniln/fastcollect-kotlin)
[![javadoc](https://javadoc.io/badge2/io.github.sooniln/fastcollect-kotlin/javadoc.svg)](https://javadoc.io/doc/io.github.sooniln/fastcollect-kotlin)

# FastCollect

A library for high-performance primitive collections in the Kotlin ecosystem.

As a drop-in replacement for standard Kotlin collections, FastCollect generally reduces memory usage by 4–5× and
improves CPU performance by 2-4× (and perhaps >10× on some iteration heavy workloads). Details and benchmarks are
available in the performance section below. FastCollect distinguishes itself with a much smaller dependency size
(supporting only necessary collections), but performance comparable to much larger and more complex libraries.

FastCollect currently supports the following major platforms (minor platforms have not been listed for brevity, the
Gradle build files are the source of truth):

* jvm
* js (nodeJs)
* wasmJs (nodeJs)
* ios (arm64)
* ios (x64)
* linux (x64)
* linux (arm64)
* macos (arm64)
* mingw (x64)

## Quick Start ##

You can add FastCollect as a dependency in your project with:

#### Gradle ####

```groovy
implementation 'io.github.sooniln:fastcollect-kotlin:1.0.0'
```

#### Maven ####

```xml
<dependency>
    <groupId>io.github.sooniln</groupId>
    <artifactId>fastcollect-kotlin</artifactId>
    <version>1.0.0</version>
</dependency>
```

FastCollect can be used as a drop-in replacement for Kotlin standard library collections and should provide immediate
memory and CPU improvements without any further changes. However, many of the Kotlin standard library APIs are not
flexible enough to properly support primitives without risking boxing penalties. Note that on JVM platforms, within
tight loops and boxed values that do not escape, the runtime is often intelligent enough to optimize away boxing -
however this becomes less likely as complexity grows. In addition, the situation on non-JVM platforms can vary
dramatically. In order to avoid potential boxing penalties, FastCollect has marked methods that force boxing as
deprecated (for visibility in IDEs, the methods will still function exactly as expected) and provided alternatives.

It is generally preferable to keep FastCollect collections as specifically typed as possible — for example, prefer:
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
FastCollect provides ArrayList/ArrayDeque, HashSet, and HashMap analogues that can store primitives (and in the case of
maps, primitive keys with reference or primitive values).

> [!NOTE]
> FastCollect currently only supports Int/Long keys for HashSet/HashMap (all types of values are supported). This is
> done out of a desire to reduce binary size and bloat by eliminating use cases that are unlikely to be very common or
> useful. If you feel you have a compelling use case that is not currently supported, please reach out, as support is
> trivial to add.

### ConcurrentModificationException ###

The standard Kotlin libraries may make reasonable efforts to throw ConcurrentModificationException if they detect
collections being modified in inappropriate ways. This already only a best effort, with no guarantees made, but
FastCollect makes even less of an effort in the interests of performance. Do not expect FastCollect to throw
ConcurrentModificationException if you are shooting yourself in the foot, except in rare instances.

## Competitors ##

Competitors and alternatives to FastCollect can be grouped into two main types - those that support Kotlin
Multiplatform, and those that are JVM only.

Other multiplatform collection libraries include:

* Kotlin standard library collections
* [androidX](https://developer.android.com/jetpack/androidx/releases/collection) - AndroidX now offers multiplatform
  primitive collections.

Other JVM-only collection libraries include:

* [fastutil](https://github.com/vigna/fastutil) - the Java standard for primitive collections, supporting a wide variety
  of scientific computing use cases.
* [Eclipse Collections](https://github.com/eclipse-collections/eclipse-collections) - a large multipurpose collections
  library that also includes primitive specific collections.
* [HashSmith](https://github.com/bluuewhale/hash-smith) - a relatively new but fast HashSet/HashMap library for
  reference values (not primitives). Included in benchmarks to test comparison against a modern Swiss table with
  vectorized logic - it is not expected to be competitive for primitive collections.

There are further JVM-only collections libraries, but they tend to be older and relatively unsupported (Koloboke, Trove,
etc...)

## Performance ##

A more detailed examination of performance can be found in the [Performance Benchmarks](docs/PERFORMANCE_BENCHMARKS.md)
doc. In benchmarking, FastCollect unsurprisingly outperforms standard Kotlin collections by orders of magnitude (as
expected since FastCollect stores primitives, not boxed types). Performance is very comparable to fastutil, with slight
variations in who takes the top spot, though the differences are small enough they are unlikely to be significant except
in the most performance-critical code paths.

A key advantage of primitive collections is not just reduced CPU usage, but substantially lower memory usage, which
has compounding benefits — more data fitting in CPU caches further reduces memory access latency.

### Memory Usage ###

A more detailed examination of memory usage can be found in the [Memory Benchmarks](docs/MEMORY_BENCHMARKS.md) doc. The
overall takeaways are that FastCollect has the lowest memory usage of all benchmarked libraries in virtually all
scenarios, and especially so with empty/small maps and sets (a scenario not uncommon in many areas of scientific
computing such as graphs, but a consideration that many collections neglect).

Map memory usage as a function of collection size:

![Map Memory Usage](docs/map_memory.svg)

Memory usage of 1 million empty Sets:

![Empty Map Memory Usage](docs/empty_set_memory_split.svg)

## Generated Code

FastCollect generates most of its collection classes from templates in order to reduce the amount of copy/pasted code
present. Contrary to common practice, this project checks the generated code directly into the repository. While this is
non-standard from a build pipeline perspective, this project has public APIs composed of generated code, and it is
important for clients and users that the actual code (rather than just the generation templates) is viewable,
searchable, and parseable within the repository itself.

## Detecting Boxing

While FastCollect deprecates collection methods that lead to boxing, this does not help detect indirect usage of these
methods which may still be causing performance problems. One problem for example is the use of Kotlin standard library
extension methods (which are always in scope, whereas FastCollect extension methods need to be specifically imported).
Thus on some platforms, FastCollect supports setting the 'fastcollect-warn-on-boxing' property which will output
error logs with stack traces if it detects usage of methods which are causing boxing.

On JVM this is a system property, which can be set via a java flag: `java -Dfastcollect-warn-on-boxing=true ...`. For
native applications this can be set via a command line flag `--fastcollect-warn-on-boxing=true`.

## JVM Boxing and Escape Analysis

Modern JVM runtimes are quite good at analyzing and optimizing code while they're running. This means that in simple
scenarios the JVM can completely elide some boxing penalties even where you might expect to see them. For this reason,
switching from collection methods that imply boxing to FastCollect methods that prevent boxing may not always directly
improve performance (switching the underlying storage will improve performance). However, FastCollect methods that avoid
boxing will never perform worse than methods that may box, and for more complex code usage will likely deliver
substantial performance improvements, so it is always worth migrating to FastCollect methods that avoid boxing where
reasonable.

## HashSet/HashMap Development Notes

Some extra details on the implementation and development of HashSet/HashMap can be found in the
[Hashtable Implementation](docs/HASHTABLE_IMPLEMENTATION.md) docs.
