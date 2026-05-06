# Memory Benchmarks #

All memory benchmarks were only run on JVM, where usage was captured with the JOL (Java Object Layout) library for Int
collections (IntList, IntSet, Int2IntMap and equivalents). Memory measurements on non-JVM platforms are not available
yet.

## Memory Usage ##

Our first benchmarks track how object size grows as the number of elements in a collection increases. From the graphs
below it is apparent that FastCollect is vastly more memory efficient than standard Kotlin collections and KDS, and
slightly more memory efficient than fastutils or Eclipse.

### List ###

Note that the FastCollect line is being overlaid by the Eclipse line, which is why it's not visible.

![List Memory Usage](list_memory.svg)

### Set ###

![Set Memory Usage](set_memory.svg)

### Map ###

![Map Memory Usage](map_memory.svg)

## Empty Collection Memory Usage ##

An often overlooked axis in memory benchmarking is testing the overhead of empty collections. Empty (or small sized)
collections can be very common in many programming scenarios. An 'ideal' empty collection would use no memory since it
has nothing to store. Obviously we can't store anything with zero overhead in reality, so here we compare the memory
usage of 1,000,000 empty collections.

The results show that FastCollect consistently matches or exceeds Kotlin collections for low overhead. Fastutil and KDS are
surprisingly bad for Sets and Maps (indeed one of the impetuses for this project was that fastutil wasted enough space
for empty / small collections that programs were running out of memory for reasonably sized data sets).

### List ###

![Empty List Memory Usage](empty_list_memory_split.svg)

### Set ###

![Empty Set Memory Usage](empty_set_memory_split.svg)

### Map ###

![Empty Map Memory Usage](empty_map_memory_split.svg)

## Collection Splits Memory Usage ##

Continuing down this rabbit hole of extra overhead, we now compare the amount of overhead required to store 1,000,000
Ints in a variety of ways, i.e., 1,000,000 collections of 1 Int, 500,000 collections of 2 Ints, etc... The theoretical
minimum space required (4 * 1,000,000 = 4,000,000 bytes) is subtracted from the actual space used to obtain the overhead
displayed below.

As expected from the empty collection results, everyone except Kotlin collections has lower overhead for lists (since
Kotlin collections store boxed values). For sets and maps KDS has a very high overhead, sometimes more than Kotlin
collections even though it is supposedly storing primitives... FastCollect and Eclipse trade off the lowest overhead,
followed by fastutils.

### List ###

![List Memory Overhead Splits](list_memory_split.svg)

### Set ###

![Set Memory Overhead Splits](set_memory_split.svg)

### Map ###

![Map Memory Overhead Splits](map_memory_split.svg)
