# HashMap and HashSet Implementation

Both HashMap and HashSet implementations (will not further distinguish here as implementation is identical in all
important ways) are based on [Robin Hood](https://en.wikipedia.org/wiki/Hash_table#Robin_Hood_hashing) hashing. This may
seem a suboptimal choice at first compared to standard linear probing:

RH hashing's maintenance/checking of the DIB invariant during insertion/removal/retrieval means that the hashcode of
every element in the probe chain is required. RH normally deals with this by caching hashcodes (or at least partial
hashcodes or DIBs). With primitive keys however, the cache size is a substantial fraction (50-100%) of the table size
itself. So either there's a large memory overhead for caching, or it's required to recalculate multiple hashes every
time any insertion/removal/retrieval occurs.

However, there are balancing advantages that come from using primitive keys:

1) Hashcode is exceptionally cheap (identity for Int, xor + shift for Long), but entropy is not well distributed
   across the hashcode.
2) No need to worry about clearing keys to support GC.

The two main benefits of Robin Hood hashing relevant to this implementation are: 1) heavily reduced variance in
insertion, removal, and retrieval by maintaining the Robin Hood invariant; and 2) as a result, support for much higher
load factors, reducing the table's memory requirements.

FastCollect adopts the following design decisions:

1) Do not cache hashcodes/DIBs to avoid the additional memory cost.
2) Recalculate hashcodes/DIBs in the insertion/removal tight loops, and keep those operations as cheap as possible.
3) Completely forgo hashcode/DIB recalculations in the retrieval tight loop; retrieval therefore cannot early-exit
   (one of Robin Hood hashing's benefits), but empirically the performance gains outweigh this cost.
4) Use a cheap hash smear to compensate for the reduced hash quality.

The result is a table that uses less memory while remaining as fast or faster than most linear probing implementations.

## Small Capacity Tables ##

FastCollect goes a step further, and for small tables it will force the loadFactor to 1. This generally has a minimal
performance impact, but make tables much more memory efficient at low capacities. This is particularly important
because many collection libraries do not consider the memory impact of large numbers of empty or small capacity
collections, which often waste excessive amounts of memory.

## Hashcode Entropy ##

For RH hashing with primitives to be successful there are two main problems that needed to be solved 1) a better
distribution of entropy across hashcodes 2) very cheap hash/smear primitives so the code does not become prohibitively
slow in the tight loops required by all insertions/removals.

Since FastCollect does not cache hashcodes, this also opens up an additional possibility - hashcodes can change whenever
the underlying table size changes (since everything is rehashed on resize anyway). The ability to change hashcodes on
resize means the table size can be taken into account when smearing - and since the table size implies which subset of
the hashcode bits will be considered for the slot position calculation it's therefore possible to ensure the highest
entropy bits end up there. This allows a cheap smearing algorithm (which normally would not result in a good
distribution of entropy over the entire hash), and the result is then rotated so the highest entropy part is the part
actually used. The end result is a cheap smear that still delivers good entropy where it's needed.

FastCollect achieves both goals, delivering a high-performance hashing implementation that also uses less memory than
comparable libraries. The smear performs well against a variety of adversarial inputs in testing and benchmarking.
However, several smearing algorithms exist that achieve ~10x better throughput in typical scenarios while degrading
catastrophically (~100x worse) under uncommon adversarial inputs. As a general-purpose library, FastCollect selects the
more robust alternative.

## Accidentally Quadratic ##

There is one final problem for RH hashing (and for linear probing generally, though the problem is most acute
with regards to RH). RH is particularly susceptible to accidentally quadratic pathological performance when insertion
occurs in groups over the same hash slot cluster. Since the RH invariant is designed to keep slots close to home, it is
more susceptible to this, but even normal linear probing is not immune (fastutil HashMap/Set implementations
demonstrated quadratic behavior on simple reinsertion tests for example). The most famous example of this is perhaps
[this bug](https://github.com/rust-lang/rust/issues/36481) when quadratic behavior was detected in the Rust default
hashtable.

A trivial reproduction for an affected table is:

```kotlin
val N = 1000000
var original = HashSet(N).apply { repeat(N) { i -> add(i) } }
var copy = HashSet() // note that we don't pre-size this one - this is important
for (e in original) {
    copy.add(e) // this will eventually exhibit quadratic behavior as the backing table becomes full before resizing
}
```

The bug means that if the internal ordering of an RH table is publicly exposed it opens up the table to DOS attacks
where an insertion sequence following the same order causes O(n²) CPU usage. The general fix is to expose either a
fixed iteration order or a random iteration order. Random order can be achieved by per-instance randomness in the
hashcode/smear, so that ordering in one table has no relationship to the ordering in another table (to be truly
resistant the randomness would actually need to be per backing array instance - whenever a new backing array is
allocated the random seed needs to change, which also invalidates any cached hashcodes). This requires at a minimum
another field to store the random seed per instance, and another operation (usually xor or addition) within the
hashcode/smear.

Complete resistance to deliberate adversarial attacks is outside FastCollect's design goals (and the performance cost
of a full fix is prohibitive). The goal is instead to ensure this cannot occur accidentally, as in the example above,
and that it is extremely unlikely in normal usage.

The primary amelioration in this situation then is to enforce a random ordering of iteration (but no change to the
ordering in the backing array) so that the cost is only paid on iteration. The usual advice is to select a random
starting point and a random odd step (since all odd numbers are coprime with powers of two), and iterate from there.
A simpler and faster solution exists that takes advantage of the Robin Hood invariant.

> [!NOTE]
> This solution appears to be novel, likely because most Robin Hood implementations do not address this problem given
> how rarely it arises in practice.

When analyzing the distribution of DIBs in a Robin Hood table (i.e. how far any element is from its home slot), this
grows logarithmically with respect to the table size. Further, for any 32-bit signed addressable table (i.e. all
platforms supported by FastCollect), the 99th percentile DIB is ≤ 17. So by simply choosing a constant step of 17 in
the iteration method, consecutive returned elements are effectively guaranteed to fall in different home clusters for
any table of reasonable size, but may still be close enough to take advantage of cache locality. This is considerably
faster and less complex than selecting a random start and step, and performs better in practice as well.
