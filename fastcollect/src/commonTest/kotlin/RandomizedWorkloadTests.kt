package io.github.sooniln.fastcollect

import kotlin.random.Random
import kotlin.test.*

class RandomizedWorkloadTests {

    private val seeds = listOf(20260828L, 1L, -7L)
    private val operations = 5_000

    // ---------- list / deque ----------

    @Test
    fun deque_matchesAReferenceList() {
        for (seed in seeds) {
            val deque = IntArrayDeque()
            val reference = ArrayDeque<Int>()
            val random = Random(seed)

            repeat(operations) { step ->
                val where = "seed $seed step $step"
                when (random.nextInt(14)) {
                    0 -> random.nextInt(-50, 50).let { deque.addFirst(it); reference.addFirst(it) }
                    1 -> random.nextInt(-50, 50).let { deque.addLast(it); reference.addLast(it) }
                    2 -> if (reference.isNotEmpty()) assertEquals(reference.removeFirst(), deque.removeFirst(), where)
                    3 -> if (reference.isNotEmpty()) assertEquals(reference.removeLast(), deque.removeLast(), where)
                    4 -> {
                        val index = random.nextInt(reference.size + 1)
                        val value = random.nextInt(-50, 50)
                        deque.add(index, value)
                        reference.add(index, value)
                    }
                    5 -> if (reference.isNotEmpty()) {
                        val index = random.nextInt(reference.size)
                        assertEquals(reference.removeAt(index), deque.removeAt(index), where)
                    }
                    6 -> if (reference.isNotEmpty()) {
                        val index = random.nextInt(reference.size)
                        val value = random.nextInt(-50, 50)
                        deque[index] = value
                        reference[index] = value
                    }
                    7 -> {
                        val value = random.nextInt(-50, 50)
                        assertEquals(reference.indexOf(value), deque.indexOf(value), "$where indexOf($value)")
                        assertEquals(reference.lastIndexOf(value), deque.lastIndexOf(value), "$where lastIndexOf($value)")
                        assertEquals(reference.contains(value), deque.contains(value), "$where contains($value)")
                    }
                    8 -> {
                        val cutoff = random.nextInt(-50, 50)
                        assertEquals(reference.removeAll { it < cutoff }, deque.removeAll { it < cutoff }, where)
                    }
                    9 -> {
                        val cutoff = random.nextInt(-50, 50)
                        assertEquals(reference.retainAll { it < cutoff }, deque.retainAll { it < cutoff }, where)
                    }
                    10 -> {
                        // traverser-driven removal must visit every element exactly once
                        val cutoff = random.nextInt(-50, 50)
                        val visited = mutableListOf<Int>()
                        val expected = reference.toList()
                        val traverser = deque.traverser()
                        while (traverser.forward()) {
                            visited.add(traverser.value)
                            if (traverser.value < cutoff) traverser.remove()
                        }
                        assertEquals(expected, visited, "$where traverser visit order")
                        reference.removeAll { it < cutoff }
                    }
                    11 -> if (reference.isNotEmpty()) {
                        deque.sort()
                        reference.sortWith(naturalOrder())
                    }
                    12 -> {
                        deque.reverse()
                        reference.reverse()
                    }
                    13 -> if (random.nextBoolean()) deque.ensureCapacity(random.nextInt(0, 200)) else deque.trimToSize()
                }

                assertEquals(reference.size, deque.size, "$where size")
                assertEquals(reference.isEmpty(), deque.isEmpty(), "$where isEmpty")
            }

            assertEquals(reference.toList(), deque.toBoxedList(), "seed $seed final contents")
            assertEquals(reference.toList(), deque.copyInto(IntArray(deque.size)).toList(), "seed $seed copyInto")
        }
    }

    // ---------- hash set ----------

    @Test
    fun hashSet_matchesAReferenceHashSet() {
        for (seed in seeds) {
            val set = IntHashSet()
            val reference = HashSet<Int>()
            val random = Random(seed)

            repeat(operations) { step ->
                val where = "seed $seed step $step"
                // the domain spans zero, which is the initial empty-slot marker
                val element = random.nextInt(-100, 100)
                when (random.nextInt(8)) {
                    0 -> assertEquals(reference.add(element), set.add(element), "$where add($element)")
                    1 -> assertEquals(reference.remove(element), set.remove(element), "$where remove($element)")
                    2 -> assertEquals(reference.contains(element), set.contains(element), "$where contains($element)")
                    3 -> {
                        val batch = List(random.nextInt(1, 8)) { random.nextInt(-100, 100) }
                        assertEquals(reference.addAll(batch), set.addAll(batch), "$where addAll")
                    }
                    4 -> {
                        val batch = List(random.nextInt(1, 8)) { random.nextInt(-100, 100) }
                        assertEquals(reference.removeAll(batch.toSet()), set.removeAll(batch), "$where removeAll")
                    }
                    5 -> {
                        val iterator = set.iterator()
                        var removed = false
                        while (iterator.hasNext()) {
                            if (iterator.nextInt() == element) { iterator.remove(); removed = true }
                        }
                        assertEquals(reference.remove(element), removed, "$where iterator.remove($element)")
                    }
                    6 -> {
                        val cutoff = random.nextInt(-100, 100)
                        val visited = mutableListOf<Int>()
                        val traverser = set.traverser()
                        while (traverser.forward()) {
                            visited.add(traverser.value)
                            if (traverser.value < cutoff) traverser.remove()
                        }
                        assertEquals(reference.sorted(), visited.sorted(), "$where traverser visited every element once")
                        reference.removeAll { it < cutoff }
                    }
                    7 -> if (random.nextBoolean()) set.ensureCapacity(random.nextInt(0, 500)) else set.trimToSize()
                }

                assertEquals(reference.size, set.size, "$where size")
            }

            assertEquals(reference.sorted(), set.toBoxedList().sorted(), "seed $seed final contents")
        }
    }

    // ---------- hash maps ----------

    @Test
    fun hashMap_matchesAReferenceHashMap() {
        // HashMap.kte, with distinct key and value types so a key/value mix-up cannot type-check
        for (seed in seeds) {
            val map = Int2LongHashMap()
            val reference = HashMap<Int, Long>()
            val absent = Long.MIN_VALUE
            val random = Random(seed)

            repeat(operations) { step ->
                val where = "seed $seed step $step"
                // key domain spans zero (the empty-slot marker); value domain spans the default value
                val key = random.nextInt(-100, 100)
                val value = if (random.nextInt(10) == 0) absent else random.nextLong(-50, 50)

                when (random.nextInt(12)) {
                    0 -> assertEquals(reference.put(key, value) ?: absent, map.put(key, value), "$where put($key)")
                    1 -> { map[key] = value; reference[key] = value }
                    2 -> {
                        val expected = if (reference.containsKey(key)) reference.getValue(key) else { reference[key] = value; absent }
                        assertEquals(expected, map.putIfAbsent(key, value), "$where putIfAbsent($key)")
                    }
                    3 -> assertEquals(reference[key] ?: absent, map[key], "$where get($key)")
                    4 -> assertEquals(reference.containsKey(key), map.containsKey(key), "$where containsKey($key)")
                    5 -> assertEquals(reference.remove(key) ?: absent, map.remove(key), "$where remove($key)")
                    6 -> {
                        val matches = reference[key] == value
                        if (matches) reference.remove(key)
                        assertEquals(matches, map.remove(key, value), "$where remove($key, $value)")
                    }
                    7 -> {
                        val expected = reference.getOrPut(key) { value }
                        assertEquals(expected, map.getOrPut(key) { value }, "$where getOrPut($key)")
                    }
                    8 -> {
                        val old = reference[key]
                        val merged = if (old == null) value else old + value
                        reference[key] = merged
                        assertEquals(merged, map.merge(key, value) { a, b -> a + b }, "$where merge($key)")
                    }
                    9 -> {
                        val iterator = map.iterator()
                        while (iterator.hasNext()) {
                            val entry = iterator.next()
                            if (entry.key == key) iterator.remove() else entry.value = entry.value
                        }
                        reference.remove(key)
                    }
                    10 -> {
                        val cutoff = random.nextInt(-100, 100)
                        val visited = mutableListOf<Int>()
                        val traverser = map.traverser()
                        while (traverser.forward()) {
                            visited.add(traverser.key)
                            if (traverser.key < cutoff) traverser.remove() else traverser.value = traverser.value
                        }
                        assertEquals(reference.keys.sorted(), visited.sorted(), "$where traverser visited every entry once")
                        reference.keys.removeAll { it < cutoff }
                    }
                    11 -> if (random.nextBoolean()) map.ensureCapacity(random.nextInt(0, 500)) else map.trimToSize()
                }

                assertEquals(reference.size, map.size, "$where size")
                if (step % 100 == 0) {
                    assertEquals(reference.keys.sorted(), map.keys.toBoxedList().sorted(), "$where keys view")
                    assertEquals(reference.values.sorted(), map.values.toBoxedList().sorted(), "$where values view")
                }
            }

            assertEquals(reference, map.asMap(), "seed $seed final contents")
        }
    }

    @Test
    fun interleavedHashMap_matchesAReferenceHashMap() {
        // InterleavedHashMap.kte packs the key and value into one slot, so its empty-slot marker is a (key, value)
        // *pair*. A narrow value domain makes the (0, 0) collision come up constantly on its own.
        for (seed in seeds) {
            val map = Int2IntHashMap()
            val reference = HashMap<Int, Int>()
            val absent = Int.MIN_VALUE
            val random = Random(seed)

            repeat(operations) { step ->
                val where = "seed $seed step $step"
                val key = random.nextInt(-100, 100)
                val value = if (random.nextInt(20) == 0) absent else random.nextInt(0, 3)

                when (random.nextInt(9)) {
                    0 -> assertEquals(reference.put(key, value) ?: absent, map.put(key, value), "$where put($key)")
                    1 -> { map[key] = value; reference[key] = value }
                    2 -> assertEquals(reference[key] ?: absent, map[key], "$where get($key)")
                    3 -> assertEquals(reference.containsKey(key), map.containsKey(key), "$where containsKey($key)")
                    4 -> assertEquals(reference.remove(key) ?: absent, map.remove(key), "$where remove($key)")
                    5 -> {
                        val matches = reference[key] == value
                        if (matches) reference.remove(key)
                        assertEquals(matches, map.remove(key, value), "$where remove($key, $value)")
                    }
                    6 -> {
                        // writing a value through an entry can land on the packed empty-slot marker
                        val iterator = map.iterator()
                        while (iterator.hasNext()) {
                            val entry = iterator.next()
                            entry.value = value
                            reference[entry.key] = value
                        }
                    }
                    7 -> {
                        val cutoff = random.nextInt(-100, 100)
                        val visited = mutableListOf<Int>()
                        val traverser = map.traverser()
                        while (traverser.forward()) {
                            visited.add(traverser.key)
                            if (traverser.key < cutoff) traverser.remove() else traverser.value = value
                        }
                        assertEquals(reference.keys.sorted(), visited.sorted(), "$where traverser visited every entry once")
                        reference.keys.removeAll { it < cutoff }
                        reference.keys.forEach { reference[it] = value }
                    }
                    8 -> if (random.nextBoolean()) map.ensureCapacity(random.nextInt(0, 500)) else map.trimToSize()
                }

                assertEquals(reference.size, map.size, "$where size")
            }

            assertEquals(reference, map.asMap(), "seed $seed final contents")
        }
    }

    @Test
    fun longKeyedHashMap_matchesAReferenceHashMap() {
        // 64-bit keys mix differently from 32-bit ones, and the forced load factor switches at a different size,
        // so the Long-keyed expansion gets its own walk with keys spread across the whole range
        for (seed in seeds) {
            val map = Long2LongHashMap()
            val reference = HashMap<Long, Long>()
            val absent = Long.MIN_VALUE
            val random = Random(seed)

            repeat(operations) { step ->
                val where = "seed $seed step $step"
                val key = random.nextLong() shr random.nextInt(64)
                val value = random.nextLong(-50, 50)

                when (random.nextInt(6)) {
                    0 -> assertEquals(reference.put(key, value) ?: absent, map.put(key, value), "$where put($key)")
                    1 -> assertEquals(reference[key] ?: absent, map[key], "$where get($key)")
                    2 -> assertEquals(reference.containsKey(key), map.containsKey(key), "$where containsKey($key)")
                    3 -> assertEquals(reference.remove(key) ?: absent, map.remove(key), "$where remove($key)")
                    4 -> {
                        // re-read a key that is very likely to be present, to exercise successful probes
                        val existing = reference.keys.firstOrNull() ?: key
                        assertEquals(reference[existing] ?: absent, map[existing], "$where get($existing)")
                    }
                    5 -> if (random.nextBoolean()) map.ensureCapacity(random.nextInt(0, 500)) else map.trimToSize()
                }

                assertEquals(reference.size, map.size, "$where size")
            }

            assertEquals(reference, map.asMap(), "seed $seed final contents")
        }
    }

    // ---------- priority queue ----------

    @Test
    fun priorityQueue_matchesASortedReference() {
        // java.util.PriorityQueue is JVM-only, so the reference is a plain list kept sorted
        for (seed in seeds) {
            val queue = IntPriorityQueue()
            val reference = mutableListOf<Int>()
            val random = Random(seed)

            repeat(operations) { step ->
                val where = "seed $seed step $step"
                val element = random.nextInt(-50, 50)

                when (random.nextInt(8)) {
                    0 -> { queue.add(element); reference.add(element) }
                    1 -> if (reference.isNotEmpty()) assertEquals(reference.min(), queue.first(), "$where first")
                    2 -> if (reference.isNotEmpty()) {
                        val expected = reference.min()
                        assertEquals(expected, queue.removeFirst(), "$where removeFirst")
                        reference.remove(expected)
                    }
                    3 -> assertEquals(reference.remove(element), queue.remove(element), "$where remove($element)")
                    4 -> assertEquals(reference.contains(element), queue.contains(element), "$where contains($element)")
                    5 -> {
                        val cutoff = random.nextInt(-50, 50)
                        assertEquals(reference.removeAll { it < cutoff }, queue.removeAll { it < cutoff }, where)
                    }
                    6 -> {
                        val cutoff = random.nextInt(-50, 50)
                        assertEquals(reference.retainAll { it < cutoff }, queue.retainAll { it < cutoff }, where)
                    }
                    7 -> if (random.nextBoolean()) queue.ensureCapacity(random.nextInt(0, 200)) else queue.trimToSize()
                }

                assertEquals(reference.size, queue.size, "$where size")
                assertEquals(reference.sorted(), queue.toBoxedList().sorted(), "$where contents")
            }

            assertEquals(reference.sorted(), queue.drain(), "seed $seed drains in order")
        }
    }

    @Test
    fun descendingPriorityQueue_matchesAReverseSortedReference() {
        for (seed in seeds) {
            val queue = IntPriorityQueue(descending = true)
            val reference = mutableListOf<Int>()
            val random = Random(seed)

            repeat(operations) { step ->
                val where = "seed $seed step $step"
                if (reference.isEmpty() || random.nextBoolean()) {
                    val element = random.nextInt(-50, 50)
                    queue.add(element)
                    reference.add(element)
                } else {
                    val expected = reference.max()
                    assertEquals(expected, queue.first(), "$where first")
                    assertEquals(expected, queue.removeFirst(), "$where removeFirst")
                    reference.remove(expected)
                }
                assertEquals(reference.size, queue.size, "$where size")
            }

            assertEquals(reference.sortedDescending(), queue.drain(), "seed $seed drains in order")
        }
    }
}
