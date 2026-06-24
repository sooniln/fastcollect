[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.sooniln/fastcollect-kotlin)](https://central.sonatype.com/artifact/io.github.sooniln/fastcollect-kotlin)
[![javadoc](https://javadoc.io/badge2/io.github.sooniln/fastcollect-kotlin/javadoc.svg)](https://javadoc.io/doc/io.github.sooniln/fastcollect-kotlin)

# FastCollect

A library for high-performance primitive collections in the Kotlin ecosystem.

As a drop-in replacement for standard Kotlin collections, FastCollect generally reduces memory usage by 4–5× and
improves CPU performance by 2-4×. Details and benchmarks are available in the performance section below. FastCollect
distinguishes itself with a much smaller dependency size (supporting only necessary collections), but performance
comparable to much larger and more complex libraries.

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

Note that performance has only been tested on JVM platforms.

## Quick Start ##

You can add FastCollect as a dependency in your project with:

#### Gradle ####

```groovy
implementation 'io.github.sooniln:fastcollect-kotlin:2.0.0'
```

#### Maven ####

```xml
<dependency>
    <groupId>io.github.sooniln</groupId>
    <artifactId>fastcollect-kotlin</artifactId>
    <version>2.0.0</version>
</dependency>
```

FastCollect can be used as a (mostly) drop-in replacement for Kotlin standard library collections and should provide
immediate memory and CPU improvements without any further changes. You may need to explicitly import FastCollect
extension functions where standard library extension functions were imported silently, and note that FastCollect
currently does not provide extension methods that create new collections (i.e. filter(), etc...).

Using FastCollect types should be quite straightforward for anyone familiar with standard Kotlin/Java collections.
FastCollect provides ArrayList/ArrayDeque, HashSet, and HashMap analogues that can store primitives (and in the case of
maps, primitive keys with reference or primitive values).

> [!NOTE]
> FastCollect currently only supports Int/Long keys for HashSet/HashMap (all types of values are supported). This is
> done out of a desire to reduce binary size and bloat by eliminating use cases that are unlikely to be very common or
> useful. If you feel you have a compelling use case that is not currently supported, please reach out, as support is
> generally trivial to add.

### Examples ###

You'll find that FastCollect collection usage is pretty much exactly like Kotlin collection usage. A few examples of
common APIs follow:

```kotlin
// creating a list
var list = IntArrayList() // create FastCollect list directly
list = mutableIntList(1, 2, 3) // directly create FastCollect list

// get/set by index
var i = list[1]
list[1] = 2

// search for value in list
list.indexOf(1)
list.lastIndexOf(2)
list.contains(3)

// iterate over list
for (i in list) { ... }

// mutate list
list.add(5)
list.remove(5)
list.removeAt(0)
list.clear()

// other operations
list.sort()
list.shuffle()
list.fill(0)
```

```kotlin
// creating a set
var set = IntHashSet() // create FastCollect set directly
set = mutableIntSetOf(1, 2, 3) // directly create FastCollect set

// search for presence in set
set.contains(3)

// iterate over set
for (i in set) { ... }

// mutate set
set.add(5)
set.remove(5)
set.clear()
```

```kotlin
// creating a map
var map = Int2IntHashMap() // create FastCollect map directly
map = mutableInt2IntMapOf(1 to 2, 2 to 4, 3 to 7) // directly create FastCollect map

// get/set by index
var v = map[1]
map[1] = 5

// search for key/value in map
map.containsKey(1)
map.containsValue(2)

// iterate over map
for (k in map.keys) { ... }
for (v in map.values) { ... }
for ((k, v) in map) { ... }

// mutate map
map.remove(5)
map.clear()

// other operations
map.getOrElse(1) { -1 }
```

### ConcurrentModificationException ###

The standard JRE libraries make reasonable efforts to throw ConcurrentModificationException if they detect
collections being modified in inappropriate ways. This already only a best effort with no guarantees made, but
FastCollect makes even less of an effort in the interests of performance. Do not expect FastCollect to throw
ConcurrentModificationException if you are shooting yourself in the foot, except in rare instances.

## Performance ##

A key advantage of primitive collections is not just reduced CPU usage, but substantially lower memory usage, which
has compounding benefits — more data fitting in CPU caches further reduces memory access latency.

A more detailed examination of performance can be found in the [Performance Benchmarks](docs/PERFORMANCE_BENCHMARKS.md)
doc. In benchmarking, FastCollect unsurprisingly outperforms standard Kotlin collections by orders of magnitude (as
expected since FastCollect stores primitives, not boxed types).

### Memory Usage ###

A more detailed examination of memory usage can be found in the [Memory Benchmarks](docs/MEMORY_BENCHMARKS.md) doc. The
overall takeaways are that FastCollect has the lowest memory usage of all benchmarked libraries in virtually all
scenarios, and especially so with empty/small maps and sets (a scenario not uncommon in many areas of scientific
computing such as graphs, but a consideration that many collections neglect).

Map memory usage as a function of collection size:

![Map Memory Usage](docs/map_memory.svg)

## Generated Code

FastCollect generates most of its collection classes from templates in order to reduce the amount of copy/pasted code
present. Contrary to common practice, this project checks the generated code directly into the repository. While this is
non-standard from a build pipeline perspective, this project has public APIs composed of generated code, and it is
important for clients and users that the actual code (rather than just the generation templates) is viewable,
searchable, and parseable within the repository itself.

## HashSet/HashMap Development Notes

Some extra details on the implementation and development of HashSet/HashMap can be found in the
[Hashtable Implementation](docs/HASHTABLE_IMPLEMENTATION.md) docs.
