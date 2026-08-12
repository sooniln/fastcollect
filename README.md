[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.sooniln/fastcollect-kotlin)](https://central.sonatype.com/artifact/io.github.sooniln/fastcollect-kotlin)
[![javadoc](https://javadoc.io/badge2/io.github.sooniln/fastcollect-kotlin/javadoc.svg)](https://javadoc.io/doc/io.github.sooniln/fastcollect-kotlin)

# FastCollect

A library for high-performance primitive collections in the Kotlin ecosystem.

As a drop-in replacement for standard Kotlin collections, FastCollect generally reduces memory usage by 4–5× and
improves CPU performance by 2-3×. FastCollect distinguishes itself with a much smaller dependency size (supporting
only necessary collections), but performance comparable to or better than much larger and more complex libraries.

FastCollect currently supports the following major platforms (minor platforms have not been listed for brevity, the
Gradle build files are the source of truth):

* jvm
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
implementation 'io.github.sooniln:fastcollect-kotlin:4.0.0'
```

#### Maven ####

```xml
<dependency>
    <groupId>io.github.sooniln</groupId>
    <artifactId>fastcollect-kotlin</artifactId>
    <version>4.0.0</version>
</dependency>
```

FastCollect can be used as a replacement for Kotlin standard library collections and should provide immediate memory and
CPU improvements without any further changes. You may need to explicitly import FastCollect extension functions where
standard library extension functions were imported silently, and note that FastCollect currently does not provide
extension methods that create new collections (i.e. filter(), etc...).

FastCollect can interact with normal Kotlin collections through the use of extension methods like [asList], [asSet],
[asMap], and [asQueue], which produce a thin wrapper around the FastCollect collection which allows it to be used as a
Kotlin collection. Beware that using these wrappers may incur boxing penalties.

Using FastCollect types should be quite straightforward for anyone familiar with standard Kotlin/Java collections.
FastCollect provides ArrayList/ArrayDeque, HashSet, and HashMap analogues that can store primitives (and in the case of
maps, primitive keys with reference or primitive values).

> [!NOTE]
> FastCollect currently only supports Int/Long keys for HashSet/HashMap (all types of values are supported). This is
> done out of a desire to reduce binary size and bloat by eliminating use cases that are unlikely to be very common or
> useful. If you feel you have a compelling use case that is not currently supported, please reach out, as support is
> generally trivial to add.

### Overview ###

Concrete primitive collection types supported:
* **Iterator**
* **ArrayDeque** (and ArrayList via the same API)
* **HashSet**
* **HashMap**
* **PriorityQueue** (and optionally indirect priority queues)
* **Mutable and read-only types** for all of the above

Unsupported collection types:
* **LinkedList** - primitive linked lists are assumed useless in any reasonable scenarios until proven otherwise.
* **ConcurrentHashMap** - supporting arbitrary concurrency within a hashmap is assumed to be useless in any reasonable
  scenario until proven otherwise. In scenarios where arbitrary levels of concurrency are useful (such as in a cache
  perhaps), ConcurrentHashMap is almost always inferior to specialized designs for the problem domain. In scenarios
  where a lower level of concurrency is required, locking is assumed to be superior.
* **LinkedHashSet/Map** - stable ordering within a HashSet/Map is assumed to be unnecessary until proven otherwise.
* **TreeSet/Map** - useful in some scenarios, but often too niche to justify including for the moment.
* **Big collections (64-bit indexing)** - useful only in very niche scenarios.
* **Fully immutable types** - occasionally useful, but do not currently appear to give a sufficient benefit vs read-only
  types and effective immutability to justify inclusion.

#### Floating-Point Comparisons ####

On the JVM, primitive floating-point types obey IEEE floating-point comparisons (positive and negative zeros are equal,
NaN is never equal to anything including itself). Boxed floating-point types however do not obey normal IEEE
floating-point rules (positive and negative zeros are not equal, NaN can be equal to other Nan values).

In order to make the primitive collections in this library maximally useful, all collections internally implement
equality as bit-wise equality. This means that equality used in this library is closer to JVM boxed type equality
than primitive type equality. Within the collections for example, Float.NaN == Float.Nan and -0.0 != 0.0. Care must
be used when interacting with these collections via external lambdas, for example:

```kotlin
var set = mutableFloatSetOf(Float.NaN)
// option 1 - removes NaN from the set
set.remove(Float.NaN)
// option 2 - does not remove NaN from the set
set.removeAll(value -> value == Float.Nan)
```

Default Kotlin equality uses IEEE conventions for primitives. For this reason, FastUtil exposes publicly the comparison
methods it uses internally, as `equalsBoxed()`.

```kotlin
import io.github.sooniln.fastcollect.equalsBoxed

var set = mutableFloatSetOf(Float.NaN)
// option 1 - removes NaN from the set
set.remove(Float.NaN)
// option 2 - removes NaN from the set
set.removeAll(value -> value equalsBoxed Float.Nan)
```

#### ConcurrentModificationException ####

The standard JRE libraries make reasonable efforts to throw ConcurrentModificationException if they detect
collections being modified in inappropriate ways. This already only a best effort with no guarantees made, but
FastCollect makes even less of an effort in the interests of performance. Do not expect FastCollect to throw
ConcurrentModificationException if you are shooting yourself in the foot, except in very rare instances.

### Examples ###

You'll find that FastCollect collection usage is pretty much exactly like Kotlin collection usage. A few
(non-exhaustive) examples of common APIs follow:

```kotlin
// creating a list
var list = IntArrayList()
list = mutableIntList(1, 2, 3)

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

// use the list somewhere a Kotlin list is required
legacyApi(list.asList())
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

// use the set somewhere a Kotlin set is required
legacyApi(set.asSet())
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

// use the map somewhere a Kotlin map is required
legacyApi(map.asMap())
```

```kotlin
// creating a priority queue
var priorityQueue = IntPriorityQueue(descending = true)

// mutate priority queue
priorityQueue.add(5)
priorityQueue.add(2)
priorityQueue.add(8)
priorityQueue.remove(5) // O(N)
priorityQueue.first() // returns 8
priorityQueue.removeFirst() // returns 8
priorityQueue.clear()

// iterate over priority queue
for (e in priorityQueue) { ... }

// use the priority queue somewhere a Kotlin queue is required
legacyApi(priorityQueue.asQueue())
```

## Performance and Memory Usage ##

A key advantage of primitive collections is not just reduced CPU usage, but substantially lower memory usage, which
has compounding benefits — more data fitting in CPU caches further reduces memory access latency.

A more detailed examination of performance and memory usage can be found in
[this post](https://sooniln.github.io/posts/hashmap-benchmarks-2026/). In benchmarking, FastCollect unsurprisingly
outperforms standard Kotlin collections, as well as many other primitive collection libraries.

### Memory Usage ###

A more detailed examination of memory usage can be found in the [Memory Benchmarks](docs/MEMORY_BENCHMARKS.md) doc.
FastCollect has put effort into ensuring that not only are large collections memory efficient (which most primitive
collections libraries accomplish), but also that small/empty collections are memory efficient (which some primitive
collections are shockingly bad at).

## Generated Code

FastCollect generates most of its collection classes from templates in order to reduce the amount of copy/pasted code
present. Contrary to common practice, this project checks the generated code directly into the repository. While this is
non-standard from a build pipeline perspective, this project has public APIs composed of generated code, and it is
important for clients and users that the actual code (rather than just the generation templates) is viewable,
searchable, and parseable within the repository itself.
