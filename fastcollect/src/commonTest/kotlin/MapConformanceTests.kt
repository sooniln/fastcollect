@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package io.github.sooniln.fastcollect

import kotlin.test.*

// Three tests per generated map type. Deep behavioural coverage lives in Int2LongHashMapTests (HashMap.kte),
// Int2IntHashMapTests (InterleavedHashMap.kte) and Int2LongMapDefaultsTests (Map.kte), with randomized
// differential coverage in RandomizedWorkloadTests; these classes exist to prove each of the twelve expansions is
// wired up and its logic holds for that key/value pair. Most of that proof is at compile time - naming every
// factory for the expansion will not compile if one is missing or mistyped - so the runtime assertions stay
// thin. Anything genuinely type-specific belongs in FloatDoubleSemanticsTests, ByteSemanticsTests or
// AnyValueMapTests. Keep the classes identical to one another apart from the key and value types.

class Int2ByteMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Int, Byte>(), int2ByteMapOf().asMap())
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte()), int2ByteMapOf(1 to 1.toByte()).asMap())
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte(), 2 to 2.toByte()), int2ByteMapOf(1 to 1.toByte(), 2 to 2.toByte()).asMap())
        assertEquals(emptyMap<Int, Byte>(), emptyInt2ByteMap().asMap())

        assertEquals(emptyMap<Int, Byte>(), mutableInt2ByteMapOf().asMap())
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte()), mutableInt2ByteMapOf(1 to 1.toByte()).asMap())
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte(), 2 to 2.toByte()), mutableInt2ByteMapOf(1 to 1.toByte(), 2 to 2.toByte()).asMap())

        assertEquals(mapOf<Int, Byte>(1 to 1.toByte()), buildInt2ByteMap { set(1, 1.toByte()) }.asMap())
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte()), Int2ByteHashMap().apply { set(1, 1.toByte()) }.asMap())
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte()), Int2ByteHashMap(mapOf<Int, Byte>(1 to 1.toByte())).asMap())
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte()), Int2ByteHashMap(mutableInt2ByteMapOf(1 to 1.toByte())).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Int2ByteHashMap()

        assertTrue(map.isDefaultValue(map.put(1, 1.toByte())), "an absent key returns the default value")
        assertEquals(1.toByte(), map.put(1, 2.toByte()))
        map[2] = 3.toByte()

        assertEquals(2.toByte(), map[1])
        assertEquals(3.toByte(), map.getValue(2))
        assertEquals(4.toByte(), map.getOrDefault(9, 4.toByte()))
        assertTrue(map.containsKey(2))
        assertTrue(map.containsValue(3.toByte()))
        assertFalse(map.containsValue(9.toByte()))
        assertFailsWith<NoSuchElementException> { map.getValue(9) }

        assertEquals(2.toByte(), map.putIfAbsent(1, 4.toByte()), "a present key is not overwritten")
        assertEquals(2.toByte(), map.replace(1, 4.toByte()))
        assertFailsWith<NoSuchElementException> { map.replace(9, 1.toByte()) }

        assertEquals(4.toByte(), map.getOrElse(1) { 9.toByte() })
        assertEquals(9.toByte(), map.getOrPut(3) { 9.toByte() })
        assertEquals(1.toByte(), map.merge(4, 1.toByte()) { _, _ -> 2.toByte() }, "merge inserts without combining")
        assertEquals(2.toByte(), map.merge(4, 1.toByte()) { _, _ -> 2.toByte() })

        map.putAll(mutableInt2ByteMapOf(5 to 1.toByte()))
        map.putAll(mapOf<Int, Byte>(6 to 1.toByte()))
        assertEquals(6, map.size)

        assertEquals(4.toByte(), map.removeKey(1))
        assertFailsWith<NoSuchElementException> { map.removeKey(1) }
        assertFalse(map.remove(2, 1.toByte()), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2, 3.toByte()))
        assertEquals(1.toByte(), map.removeOrElse(5) { 9.toByte() })
        assertEquals(9.toByte(), map.removeOrElse(5) { 9.toByte() })

        // a key that genuinely stores the default value is still present
        val stored = Int2ByteHashMap()
        stored[1] = stored[9]
        assertTrue(stored.containsKey(1))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1) { calls++; 1.toByte() }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Int2ByteHashMap()
        for (i in 0..<300) many[i.toInt()] = 1.toByte()
        for (i in 0..<300 step 2) assertEquals(1.toByte(), many.removeKey(i.toInt()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toInt()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableInt2ByteMapOf(1 to 1.toByte(), 2 to 2.toByte(), 3 to 3.toByte())
        val expected = mapOf<Int, Byte>(1 to 1.toByte(), 2 to 2.toByte(), 3 to 3.toByte())

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Int, Byte>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Int, Byte>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Int>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Int, Byte>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1) traverser.remove() else traverser.value = 4.toByte()
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Int, Byte>(2 to 4.toByte(), 3 to 4.toByte()), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2))
        assertFalse(map.keys.contains(1))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4.toByte()))

        val entry = int2ByteMapOf(1 to 1.toByte()).iterator().next()
        val (key, value) = entry
        assertEquals(1, key)
        assertEquals(1.toByte(), value)
        assertEquals<Int2ByteMap.Entry>(AbstractInt2ByteMap.SimpleEntry(1, 1.toByte()), entry)
        assertEquals(mapOf<Int, Byte>(1 to 1.toByte()).entries.single(), entry.asEntry())

        // equality holds against a different Int2ByteMap implementation
        val hash: Int2ByteMap = mutableInt2ByteMapOf(1 to 1.toByte())
        assertEquals(int2ByteMapOf(1 to 1.toByte()), hash)
        assertEquals(hash, int2ByteMapOf(1 to 1.toByte()))
        assertEquals(int2ByteMapOf(1 to 1.toByte()).hashCode(), hash.hashCode())
        assertEquals(emptyInt2ByteMap(), mutableInt2ByteMapOf() as Int2ByteMap)
        assertNotEquals(int2ByteMapOf(1 to 2.toByte()), hash)
    }
}

class Int2IntMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Int, Int>(), int2IntMapOf().asMap())
        assertEquals(mapOf<Int, Int>(1 to 1), int2IntMapOf(1 to 1).asMap())
        assertEquals(mapOf<Int, Int>(1 to 1, 2 to 2), int2IntMapOf(1 to 1, 2 to 2).asMap())
        assertEquals(emptyMap<Int, Int>(), emptyInt2IntMap().asMap())

        assertEquals(emptyMap<Int, Int>(), mutableInt2IntMapOf().asMap())
        assertEquals(mapOf<Int, Int>(1 to 1), mutableInt2IntMapOf(1 to 1).asMap())
        assertEquals(mapOf<Int, Int>(1 to 1, 2 to 2), mutableInt2IntMapOf(1 to 1, 2 to 2).asMap())

        assertEquals(mapOf<Int, Int>(1 to 1), buildInt2IntMap { set(1, 1) }.asMap())
        assertEquals(mapOf<Int, Int>(1 to 1), Int2IntHashMap().apply { set(1, 1) }.asMap())
        assertEquals(mapOf<Int, Int>(1 to 1), Int2IntHashMap(mapOf<Int, Int>(1 to 1)).asMap())
        assertEquals(mapOf<Int, Int>(1 to 1), Int2IntHashMap(mutableInt2IntMapOf(1 to 1)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Int2IntHashMap()

        assertTrue(map.isDefaultValue(map.put(1, 1)), "an absent key returns the default value")
        assertEquals(1, map.put(1, 2))
        map[2] = 3

        assertEquals(2, map[1])
        assertEquals(3, map.getValue(2))
        assertEquals(4, map.getOrDefault(9, 4))
        assertTrue(map.containsKey(2))
        assertTrue(map.containsValue(3))
        assertFalse(map.containsValue(9))
        assertFailsWith<NoSuchElementException> { map.getValue(9) }

        assertEquals(2, map.putIfAbsent(1, 4), "a present key is not overwritten")
        assertEquals(2, map.replace(1, 4))
        assertFailsWith<NoSuchElementException> { map.replace(9, 1) }

        assertEquals(4, map.getOrElse(1) { 9 })
        assertEquals(9, map.getOrPut(3) { 9 })
        assertEquals(1, map.merge(4, 1) { _, _ -> 2 }, "merge inserts without combining")
        assertEquals(2, map.merge(4, 1) { _, _ -> 2 })

        map.putAll(mutableInt2IntMapOf(5 to 1))
        map.putAll(mapOf<Int, Int>(6 to 1))
        assertEquals(6, map.size)

        assertEquals(4, map.removeKey(1))
        assertFailsWith<NoSuchElementException> { map.removeKey(1) }
        assertFalse(map.remove(2, 1), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2, 3))
        assertEquals(1, map.removeOrElse(5) { 9 })
        assertEquals(9, map.removeOrElse(5) { 9 })

        // a key that genuinely stores the default value is still present
        val stored = Int2IntHashMap()
        stored[1] = stored[9]
        assertTrue(stored.containsKey(1))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1) { calls++; 1 }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Int2IntHashMap()
        for (i in 0..<300) many[i.toInt()] = 1
        for (i in 0..<300 step 2) assertEquals(1, many.removeKey(i.toInt()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toInt()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableInt2IntMapOf(1 to 1, 2 to 2, 3 to 3)
        val expected = mapOf<Int, Int>(1 to 1, 2 to 2, 3 to 3)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Int, Int>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Int, Int>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Int>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Int, Int>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1) traverser.remove() else traverser.value = 4
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Int, Int>(2 to 4, 3 to 4), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2))
        assertFalse(map.keys.contains(1))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4))

        val entry = int2IntMapOf(1 to 1).iterator().next()
        val (key, value) = entry
        assertEquals(1, key)
        assertEquals(1, value)
        assertEquals<Int2IntMap.Entry>(AbstractInt2IntMap.SimpleEntry(1, 1), entry)
        assertEquals(mapOf<Int, Int>(1 to 1).entries.single(), entry.asEntry())

        // equality holds against a different Int2IntMap implementation
        val hash: Int2IntMap = mutableInt2IntMapOf(1 to 1)
        assertEquals(int2IntMapOf(1 to 1), hash)
        assertEquals(hash, int2IntMapOf(1 to 1))
        assertEquals(int2IntMapOf(1 to 1).hashCode(), hash.hashCode())
        assertEquals(emptyInt2IntMap(), mutableInt2IntMapOf() as Int2IntMap)
        assertNotEquals(int2IntMapOf(1 to 2), hash)
    }
}

class Int2LongMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Int, Long>(), int2LongMapOf().asMap())
        assertEquals(mapOf<Int, Long>(1 to 1L), int2LongMapOf(1 to 1L).asMap())
        assertEquals(mapOf<Int, Long>(1 to 1L, 2 to 2L), int2LongMapOf(1 to 1L, 2 to 2L).asMap())
        assertEquals(emptyMap<Int, Long>(), emptyInt2LongMap().asMap())

        assertEquals(emptyMap<Int, Long>(), mutableInt2LongMapOf().asMap())
        assertEquals(mapOf<Int, Long>(1 to 1L), mutableInt2LongMapOf(1 to 1L).asMap())
        assertEquals(mapOf<Int, Long>(1 to 1L, 2 to 2L), mutableInt2LongMapOf(1 to 1L, 2 to 2L).asMap())

        assertEquals(mapOf<Int, Long>(1 to 1L), buildInt2LongMap { set(1, 1L) }.asMap())
        assertEquals(mapOf<Int, Long>(1 to 1L), Int2LongHashMap().apply { set(1, 1L) }.asMap())
        assertEquals(mapOf<Int, Long>(1 to 1L), Int2LongHashMap(mapOf<Int, Long>(1 to 1L)).asMap())
        assertEquals(mapOf<Int, Long>(1 to 1L), Int2LongHashMap(mutableInt2LongMapOf(1 to 1L)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Int2LongHashMap()

        assertTrue(map.isDefaultValue(map.put(1, 1L)), "an absent key returns the default value")
        assertEquals(1L, map.put(1, 2L))
        map[2] = 3L

        assertEquals(2L, map[1])
        assertEquals(3L, map.getValue(2))
        assertEquals(4L, map.getOrDefault(9, 4L))
        assertTrue(map.containsKey(2))
        assertTrue(map.containsValue(3L))
        assertFalse(map.containsValue(9L))
        assertFailsWith<NoSuchElementException> { map.getValue(9) }

        assertEquals(2L, map.putIfAbsent(1, 4L), "a present key is not overwritten")
        assertEquals(2L, map.replace(1, 4L))
        assertFailsWith<NoSuchElementException> { map.replace(9, 1L) }

        assertEquals(4L, map.getOrElse(1) { 9L })
        assertEquals(9L, map.getOrPut(3) { 9L })
        assertEquals(1L, map.merge(4, 1L) { _, _ -> 2L }, "merge inserts without combining")
        assertEquals(2L, map.merge(4, 1L) { _, _ -> 2L })

        map.putAll(mutableInt2LongMapOf(5 to 1L))
        map.putAll(mapOf<Int, Long>(6 to 1L))
        assertEquals(6, map.size)

        assertEquals(4L, map.removeKey(1))
        assertFailsWith<NoSuchElementException> { map.removeKey(1) }
        assertFalse(map.remove(2, 1L), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2, 3L))
        assertEquals(1L, map.removeOrElse(5) { 9L })
        assertEquals(9L, map.removeOrElse(5) { 9L })

        // a key that genuinely stores the default value is still present
        val stored = Int2LongHashMap()
        stored[1] = stored[9]
        assertTrue(stored.containsKey(1))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1) { calls++; 1L }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Int2LongHashMap()
        for (i in 0..<300) many[i.toInt()] = 1L
        for (i in 0..<300 step 2) assertEquals(1L, many.removeKey(i.toInt()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toInt()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableInt2LongMapOf(1 to 1L, 2 to 2L, 3 to 3L)
        val expected = mapOf<Int, Long>(1 to 1L, 2 to 2L, 3 to 3L)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Int, Long>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Int, Long>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Int>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Int, Long>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1) traverser.remove() else traverser.value = 4L
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Int, Long>(2 to 4L, 3 to 4L), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2))
        assertFalse(map.keys.contains(1))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4L))

        val entry = int2LongMapOf(1 to 1L).iterator().next()
        val (key, value) = entry
        assertEquals(1, key)
        assertEquals(1L, value)
        assertEquals<Int2LongMap.Entry>(AbstractInt2LongMap.SimpleEntry(1, 1L), entry)
        assertEquals(mapOf<Int, Long>(1 to 1L).entries.single(), entry.asEntry())

        // equality holds against a different Int2LongMap implementation
        val hash: Int2LongMap = mutableInt2LongMapOf(1 to 1L)
        assertEquals(int2LongMapOf(1 to 1L), hash)
        assertEquals(hash, int2LongMapOf(1 to 1L))
        assertEquals(int2LongMapOf(1 to 1L).hashCode(), hash.hashCode())
        assertEquals(emptyInt2LongMap(), mutableInt2LongMapOf() as Int2LongMap)
        assertNotEquals(int2LongMapOf(1 to 2L), hash)
    }
}

class Int2FloatMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Int, Float>(), int2FloatMapOf().asMap())
        assertEquals(mapOf<Int, Float>(1 to 1.5f), int2FloatMapOf(1 to 1.5f).asMap())
        assertEquals(mapOf<Int, Float>(1 to 1.5f, 2 to 2.5f), int2FloatMapOf(1 to 1.5f, 2 to 2.5f).asMap())
        assertEquals(emptyMap<Int, Float>(), emptyInt2FloatMap().asMap())

        assertEquals(emptyMap<Int, Float>(), mutableInt2FloatMapOf().asMap())
        assertEquals(mapOf<Int, Float>(1 to 1.5f), mutableInt2FloatMapOf(1 to 1.5f).asMap())
        assertEquals(mapOf<Int, Float>(1 to 1.5f, 2 to 2.5f), mutableInt2FloatMapOf(1 to 1.5f, 2 to 2.5f).asMap())

        assertEquals(mapOf<Int, Float>(1 to 1.5f), buildInt2FloatMap { set(1, 1.5f) }.asMap())
        assertEquals(mapOf<Int, Float>(1 to 1.5f), Int2FloatHashMap().apply { set(1, 1.5f) }.asMap())
        assertEquals(mapOf<Int, Float>(1 to 1.5f), Int2FloatHashMap(mapOf<Int, Float>(1 to 1.5f)).asMap())
        assertEquals(mapOf<Int, Float>(1 to 1.5f), Int2FloatHashMap(mutableInt2FloatMapOf(1 to 1.5f)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Int2FloatHashMap()

        assertTrue(map.isDefaultValue(map.put(1, 1.5f)), "an absent key returns the default value")
        assertEquals(1.5f, map.put(1, 2.5f))
        map[2] = 3.5f

        assertEquals(2.5f, map[1])
        assertEquals(3.5f, map.getValue(2))
        assertEquals(4.5f, map.getOrDefault(9, 4.5f))
        assertTrue(map.containsKey(2))
        assertTrue(map.containsValue(3.5f))
        assertFalse(map.containsValue(9.5f))
        assertFailsWith<NoSuchElementException> { map.getValue(9) }

        assertEquals(2.5f, map.putIfAbsent(1, 4.5f), "a present key is not overwritten")
        assertEquals(2.5f, map.replace(1, 4.5f))
        assertFailsWith<NoSuchElementException> { map.replace(9, 1.5f) }

        assertEquals(4.5f, map.getOrElse(1) { 9.5f })
        assertEquals(9.5f, map.getOrPut(3) { 9.5f })
        assertEquals(1.5f, map.merge(4, 1.5f) { _, _ -> 2.5f }, "merge inserts without combining")
        assertEquals(2.5f, map.merge(4, 1.5f) { _, _ -> 2.5f })

        map.putAll(mutableInt2FloatMapOf(5 to 1.5f))
        map.putAll(mapOf<Int, Float>(6 to 1.5f))
        assertEquals(6, map.size)

        assertEquals(4.5f, map.removeKey(1))
        assertFailsWith<NoSuchElementException> { map.removeKey(1) }
        assertFalse(map.remove(2, 1.5f), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2, 3.5f))
        assertEquals(1.5f, map.removeOrElse(5) { 9.5f })
        assertEquals(9.5f, map.removeOrElse(5) { 9.5f })

        // a key that genuinely stores the default value is still present
        val stored = Int2FloatHashMap()
        stored[1] = stored[9]
        assertTrue(stored.containsKey(1))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1) { calls++; 1.5f }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Int2FloatHashMap()
        for (i in 0..<300) many[i.toInt()] = 1.5f
        for (i in 0..<300 step 2) assertEquals(1.5f, many.removeKey(i.toInt()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toInt()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableInt2FloatMapOf(1 to 1.5f, 2 to 2.5f, 3 to 3.5f)
        val expected = mapOf<Int, Float>(1 to 1.5f, 2 to 2.5f, 3 to 3.5f)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Int, Float>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Int, Float>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Int>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Int, Float>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1) traverser.remove() else traverser.value = 4.5f
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Int, Float>(2 to 4.5f, 3 to 4.5f), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2))
        assertFalse(map.keys.contains(1))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4.5f))

        val entry = int2FloatMapOf(1 to 1.5f).iterator().next()
        val (key, value) = entry
        assertEquals(1, key)
        assertEquals(1.5f, value)
        assertEquals<Int2FloatMap.Entry>(AbstractInt2FloatMap.SimpleEntry(1, 1.5f), entry)
        assertEquals(mapOf<Int, Float>(1 to 1.5f).entries.single(), entry.asEntry())

        // equality holds against a different Int2FloatMap implementation
        val hash: Int2FloatMap = mutableInt2FloatMapOf(1 to 1.5f)
        assertEquals(int2FloatMapOf(1 to 1.5f), hash)
        assertEquals(hash, int2FloatMapOf(1 to 1.5f))
        assertEquals(int2FloatMapOf(1 to 1.5f).hashCode(), hash.hashCode())
        assertEquals(emptyInt2FloatMap(), mutableInt2FloatMapOf() as Int2FloatMap)
        assertNotEquals(int2FloatMapOf(1 to 2.5f), hash)
    }
}

class Int2DoubleMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Int, Double>(), int2DoubleMapOf().asMap())
        assertEquals(mapOf<Int, Double>(1 to 1.5), int2DoubleMapOf(1 to 1.5).asMap())
        assertEquals(mapOf<Int, Double>(1 to 1.5, 2 to 2.5), int2DoubleMapOf(1 to 1.5, 2 to 2.5).asMap())
        assertEquals(emptyMap<Int, Double>(), emptyInt2DoubleMap().asMap())

        assertEquals(emptyMap<Int, Double>(), mutableInt2DoubleMapOf().asMap())
        assertEquals(mapOf<Int, Double>(1 to 1.5), mutableInt2DoubleMapOf(1 to 1.5).asMap())
        assertEquals(mapOf<Int, Double>(1 to 1.5, 2 to 2.5), mutableInt2DoubleMapOf(1 to 1.5, 2 to 2.5).asMap())

        assertEquals(mapOf<Int, Double>(1 to 1.5), buildInt2DoubleMap { set(1, 1.5) }.asMap())
        assertEquals(mapOf<Int, Double>(1 to 1.5), Int2DoubleHashMap().apply { set(1, 1.5) }.asMap())
        assertEquals(mapOf<Int, Double>(1 to 1.5), Int2DoubleHashMap(mapOf<Int, Double>(1 to 1.5)).asMap())
        assertEquals(mapOf<Int, Double>(1 to 1.5), Int2DoubleHashMap(mutableInt2DoubleMapOf(1 to 1.5)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Int2DoubleHashMap()

        assertTrue(map.isDefaultValue(map.put(1, 1.5)), "an absent key returns the default value")
        assertEquals(1.5, map.put(1, 2.5))
        map[2] = 3.5

        assertEquals(2.5, map[1])
        assertEquals(3.5, map.getValue(2))
        assertEquals(4.5, map.getOrDefault(9, 4.5))
        assertTrue(map.containsKey(2))
        assertTrue(map.containsValue(3.5))
        assertFalse(map.containsValue(9.5))
        assertFailsWith<NoSuchElementException> { map.getValue(9) }

        assertEquals(2.5, map.putIfAbsent(1, 4.5), "a present key is not overwritten")
        assertEquals(2.5, map.replace(1, 4.5))
        assertFailsWith<NoSuchElementException> { map.replace(9, 1.5) }

        assertEquals(4.5, map.getOrElse(1) { 9.5 })
        assertEquals(9.5, map.getOrPut(3) { 9.5 })
        assertEquals(1.5, map.merge(4, 1.5) { _, _ -> 2.5 }, "merge inserts without combining")
        assertEquals(2.5, map.merge(4, 1.5) { _, _ -> 2.5 })

        map.putAll(mutableInt2DoubleMapOf(5 to 1.5))
        map.putAll(mapOf<Int, Double>(6 to 1.5))
        assertEquals(6, map.size)

        assertEquals(4.5, map.removeKey(1))
        assertFailsWith<NoSuchElementException> { map.removeKey(1) }
        assertFalse(map.remove(2, 1.5), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2, 3.5))
        assertEquals(1.5, map.removeOrElse(5) { 9.5 })
        assertEquals(9.5, map.removeOrElse(5) { 9.5 })

        // a key that genuinely stores the default value is still present
        val stored = Int2DoubleHashMap()
        stored[1] = stored[9]
        assertTrue(stored.containsKey(1))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1) { calls++; 1.5 }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Int2DoubleHashMap()
        for (i in 0..<300) many[i.toInt()] = 1.5
        for (i in 0..<300 step 2) assertEquals(1.5, many.removeKey(i.toInt()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toInt()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableInt2DoubleMapOf(1 to 1.5, 2 to 2.5, 3 to 3.5)
        val expected = mapOf<Int, Double>(1 to 1.5, 2 to 2.5, 3 to 3.5)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Int, Double>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Int, Double>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Int>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Int, Double>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1) traverser.remove() else traverser.value = 4.5
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Int, Double>(2 to 4.5, 3 to 4.5), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2))
        assertFalse(map.keys.contains(1))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4.5))

        val entry = int2DoubleMapOf(1 to 1.5).iterator().next()
        val (key, value) = entry
        assertEquals(1, key)
        assertEquals(1.5, value)
        assertEquals<Int2DoubleMap.Entry>(AbstractInt2DoubleMap.SimpleEntry(1, 1.5), entry)
        assertEquals(mapOf<Int, Double>(1 to 1.5).entries.single(), entry.asEntry())

        // equality holds against a different Int2DoubleMap implementation
        val hash: Int2DoubleMap = mutableInt2DoubleMapOf(1 to 1.5)
        assertEquals(int2DoubleMapOf(1 to 1.5), hash)
        assertEquals(hash, int2DoubleMapOf(1 to 1.5))
        assertEquals(int2DoubleMapOf(1 to 1.5).hashCode(), hash.hashCode())
        assertEquals(emptyInt2DoubleMap(), mutableInt2DoubleMapOf() as Int2DoubleMap)
        assertNotEquals(int2DoubleMapOf(1 to 2.5), hash)
    }
}

class Int2AnyMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Int, String?>(), int2AnyMapOf<String?>().asMap())
        assertEquals(mapOf<Int, String?>(1 to "v1"), int2AnyMapOf(1 to "v1").asMap())
        assertEquals(mapOf<Int, String?>(1 to "v1", 2 to "v2"), int2AnyMapOf(1 to "v1", 2 to "v2").asMap())
        assertEquals(emptyMap<Int, String?>(), emptyInt2AnyMap<String?>().asMap())

        assertEquals(emptyMap<Int, String?>(), mutableInt2AnyMapOf<String?>().asMap())
        assertEquals(mapOf<Int, String?>(1 to "v1"), mutableInt2AnyMapOf(1 to "v1").asMap())
        assertEquals(mapOf<Int, String?>(1 to "v1", 2 to "v2"), mutableInt2AnyMapOf(1 to "v1", 2 to "v2").asMap())

        assertEquals(mapOf<Int, String?>(1 to "v1"), buildInt2AnyMap<String?> { set(1, "v1") }.asMap())
        assertEquals(mapOf<Int, String?>(1 to "v1"), Int2AnyHashMap<String?>().apply { set(1, "v1") }.asMap())
        assertEquals(mapOf<Int, String?>(1 to "v1"), Int2AnyHashMap(mapOf<Int, String?>(1 to "v1")).asMap())
        assertEquals(mapOf<Int, String?>(1 to "v1"), Int2AnyHashMap(mutableInt2AnyMapOf(1 to "v1")).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Int2AnyHashMap<String?>()

        assertTrue(map.isDefaultValue(map.put(1, "v1")), "an absent key returns the default value")
        assertEquals("v1", map.put(1, "v2"))
        map[2] = "v3"

        assertEquals("v2", map[1])
        assertEquals("v3", map.getValue(2))
        assertEquals("v4", map.getOrDefault(9, "v4"))
        assertTrue(map.containsKey(2))
        assertTrue(map.containsValue("v3"))
        assertFalse(map.containsValue("v9"))
        assertFailsWith<NoSuchElementException> { map.getValue(9) }

        assertEquals("v2", map.putIfAbsent(1, "v4"), "a present key is not overwritten")
        assertEquals("v2", map.replace(1, "v4"))
        assertFailsWith<NoSuchElementException> { map.replace(9, "v1") }

        assertEquals("v4", map.getOrElse(1) { "v9" })
        assertEquals("v9", map.getOrPut(3) { "v9" })
        assertEquals("v1", map.merge(4, "v1") { _, _ -> "v2" }, "merge inserts without combining")
        assertEquals("v2", map.merge(4, "v1") { _, _ -> "v2" })

        map.putAll(mutableInt2AnyMapOf(5 to "v1"))
        map.putAll(mapOf<Int, String?>(6 to "v1"))
        assertEquals(6, map.size)

        assertEquals("v4", map.removeKey(1))
        assertFailsWith<NoSuchElementException> { map.removeKey(1) }
        assertFalse(map.remove(2, "v1"), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2, "v3"))
        assertEquals("v1", map.removeOrElse(5) { "v9" })
        assertEquals("v9", map.removeOrElse(5) { "v9" })

        // a key that genuinely stores the default value is still present
        val stored = Int2AnyHashMap<String?>()
        stored[1] = stored[9]
        assertTrue(stored.containsKey(1))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1) { calls++; "v1" }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Int2AnyHashMap<String?>()
        for (i in 0..<300) many[i.toInt()] = "v1"
        for (i in 0..<300 step 2) assertEquals("v1", many.removeKey(i.toInt()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toInt()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableInt2AnyMapOf(1 to "v1", 2 to "v2", 3 to "v3")
        val expected = mapOf<Int, String?>(1 to "v1", 2 to "v2", 3 to "v3")

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Int, String?>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Int, String?>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Int>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Int, String?>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1) traverser.remove() else traverser.value = "v4"
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Int, String?>(2 to "v4", 3 to "v4"), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2))
        assertFalse(map.keys.contains(1))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains("v4"))

        val entry = int2AnyMapOf(1 to "v1").iterator().next()
        val (key, value) = entry
        assertEquals(1, key)
        assertEquals("v1", value)
        assertEquals<Int2AnyMap.Entry<String?>>(AbstractInt2AnyMap.SimpleEntry(1, "v1"), entry)
        assertEquals(mapOf<Int, String?>(1 to "v1").entries.single(), entry.asEntry())

        // equality holds against a different Int2AnyMap implementation
        val hash: Int2AnyMap<String?> = mutableInt2AnyMapOf(1 to "v1")
        assertEquals(int2AnyMapOf(1 to "v1"), hash)
        assertEquals(hash, int2AnyMapOf(1 to "v1"))
        assertEquals(int2AnyMapOf(1 to "v1").hashCode(), hash.hashCode())
        assertEquals(emptyInt2AnyMap<String?>(), mutableInt2AnyMapOf<String?>() as Int2AnyMap<String?>)
        assertNotEquals(int2AnyMapOf(1 to "v2"), hash)
    }
}

class Long2ByteMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Long, Byte>(), long2ByteMapOf().asMap())
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte()), long2ByteMapOf(1L to 1.toByte()).asMap())
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte(), 2L to 2.toByte()), long2ByteMapOf(1L to 1.toByte(), 2L to 2.toByte()).asMap())
        assertEquals(emptyMap<Long, Byte>(), emptyLong2ByteMap().asMap())

        assertEquals(emptyMap<Long, Byte>(), mutableLong2ByteMapOf().asMap())
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte()), mutableLong2ByteMapOf(1L to 1.toByte()).asMap())
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte(), 2L to 2.toByte()), mutableLong2ByteMapOf(1L to 1.toByte(), 2L to 2.toByte()).asMap())

        assertEquals(mapOf<Long, Byte>(1L to 1.toByte()), buildLong2ByteMap { set(1L, 1.toByte()) }.asMap())
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte()), Long2ByteHashMap().apply { set(1L, 1.toByte()) }.asMap())
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte()), Long2ByteHashMap(mapOf<Long, Byte>(1L to 1.toByte())).asMap())
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte()), Long2ByteHashMap(mutableLong2ByteMapOf(1L to 1.toByte())).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Long2ByteHashMap()

        assertTrue(map.isDefaultValue(map.put(1L, 1.toByte())), "an absent key returns the default value")
        assertEquals(1.toByte(), map.put(1L, 2.toByte()))
        map[2L] = 3.toByte()

        assertEquals(2.toByte(), map[1L])
        assertEquals(3.toByte(), map.getValue(2L))
        assertEquals(4.toByte(), map.getOrDefault(9L, 4.toByte()))
        assertTrue(map.containsKey(2L))
        assertTrue(map.containsValue(3.toByte()))
        assertFalse(map.containsValue(9.toByte()))
        assertFailsWith<NoSuchElementException> { map.getValue(9L) }

        assertEquals(2.toByte(), map.putIfAbsent(1L, 4.toByte()), "a present key is not overwritten")
        assertEquals(2.toByte(), map.replace(1L, 4.toByte()))
        assertFailsWith<NoSuchElementException> { map.replace(9L, 1.toByte()) }

        assertEquals(4.toByte(), map.getOrElse(1L) { 9.toByte() })
        assertEquals(9.toByte(), map.getOrPut(3L) { 9.toByte() })
        assertEquals(1.toByte(), map.merge(4L, 1.toByte()) { _, _ -> 2.toByte() }, "merge inserts without combining")
        assertEquals(2.toByte(), map.merge(4L, 1.toByte()) { _, _ -> 2.toByte() })

        map.putAll(mutableLong2ByteMapOf(5L to 1.toByte()))
        map.putAll(mapOf<Long, Byte>(6L to 1.toByte()))
        assertEquals(6, map.size)

        assertEquals(4.toByte(), map.removeKey(1L))
        assertFailsWith<NoSuchElementException> { map.removeKey(1L) }
        assertFalse(map.remove(2L, 1.toByte()), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2L, 3.toByte()))
        assertEquals(1.toByte(), map.removeOrElse(5L) { 9.toByte() })
        assertEquals(9.toByte(), map.removeOrElse(5L) { 9.toByte() })

        // a key that genuinely stores the default value is still present
        val stored = Long2ByteHashMap()
        stored[1L] = stored[9L]
        assertTrue(stored.containsKey(1L))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1L) { calls++; 1.toByte() }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Long2ByteHashMap()
        for (i in 0..<300) many[i.toLong()] = 1.toByte()
        for (i in 0..<300 step 2) assertEquals(1.toByte(), many.removeKey(i.toLong()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toLong()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableLong2ByteMapOf(1L to 1.toByte(), 2L to 2.toByte(), 3L to 3.toByte())
        val expected = mapOf<Long, Byte>(1L to 1.toByte(), 2L to 2.toByte(), 3L to 3.toByte())

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Long, Byte>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Long, Byte>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Long>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Long, Byte>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1L) traverser.remove() else traverser.value = 4.toByte()
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Long, Byte>(2L to 4.toByte(), 3L to 4.toByte()), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2L))
        assertFalse(map.keys.contains(1L))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4.toByte()))

        val entry = long2ByteMapOf(1L to 1.toByte()).iterator().next()
        val (key, value) = entry
        assertEquals(1L, key)
        assertEquals(1.toByte(), value)
        assertEquals<Long2ByteMap.Entry>(AbstractLong2ByteMap.SimpleEntry(1L, 1.toByte()), entry)
        assertEquals(mapOf<Long, Byte>(1L to 1.toByte()).entries.single(), entry.asEntry())

        // equality holds against a different Long2ByteMap implementation
        val hash: Long2ByteMap = mutableLong2ByteMapOf(1L to 1.toByte())
        assertEquals(long2ByteMapOf(1L to 1.toByte()), hash)
        assertEquals(hash, long2ByteMapOf(1L to 1.toByte()))
        assertEquals(long2ByteMapOf(1L to 1.toByte()).hashCode(), hash.hashCode())
        assertEquals(emptyLong2ByteMap(), mutableLong2ByteMapOf() as Long2ByteMap)
        assertNotEquals(long2ByteMapOf(1L to 2.toByte()), hash)
    }
}

class Long2IntMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Long, Int>(), long2IntMapOf().asMap())
        assertEquals(mapOf<Long, Int>(1L to 1), long2IntMapOf(1L to 1).asMap())
        assertEquals(mapOf<Long, Int>(1L to 1, 2L to 2), long2IntMapOf(1L to 1, 2L to 2).asMap())
        assertEquals(emptyMap<Long, Int>(), emptyLong2IntMap().asMap())

        assertEquals(emptyMap<Long, Int>(), mutableLong2IntMapOf().asMap())
        assertEquals(mapOf<Long, Int>(1L to 1), mutableLong2IntMapOf(1L to 1).asMap())
        assertEquals(mapOf<Long, Int>(1L to 1, 2L to 2), mutableLong2IntMapOf(1L to 1, 2L to 2).asMap())

        assertEquals(mapOf<Long, Int>(1L to 1), buildLong2IntMap { set(1L, 1) }.asMap())
        assertEquals(mapOf<Long, Int>(1L to 1), Long2IntHashMap().apply { set(1L, 1) }.asMap())
        assertEquals(mapOf<Long, Int>(1L to 1), Long2IntHashMap(mapOf<Long, Int>(1L to 1)).asMap())
        assertEquals(mapOf<Long, Int>(1L to 1), Long2IntHashMap(mutableLong2IntMapOf(1L to 1)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Long2IntHashMap()

        assertTrue(map.isDefaultValue(map.put(1L, 1)), "an absent key returns the default value")
        assertEquals(1, map.put(1L, 2))
        map[2L] = 3

        assertEquals(2, map[1L])
        assertEquals(3, map.getValue(2L))
        assertEquals(4, map.getOrDefault(9L, 4))
        assertTrue(map.containsKey(2L))
        assertTrue(map.containsValue(3))
        assertFalse(map.containsValue(9))
        assertFailsWith<NoSuchElementException> { map.getValue(9L) }

        assertEquals(2, map.putIfAbsent(1L, 4), "a present key is not overwritten")
        assertEquals(2, map.replace(1L, 4))
        assertFailsWith<NoSuchElementException> { map.replace(9L, 1) }

        assertEquals(4, map.getOrElse(1L) { 9 })
        assertEquals(9, map.getOrPut(3L) { 9 })
        assertEquals(1, map.merge(4L, 1) { _, _ -> 2 }, "merge inserts without combining")
        assertEquals(2, map.merge(4L, 1) { _, _ -> 2 })

        map.putAll(mutableLong2IntMapOf(5L to 1))
        map.putAll(mapOf<Long, Int>(6L to 1))
        assertEquals(6, map.size)

        assertEquals(4, map.removeKey(1L))
        assertFailsWith<NoSuchElementException> { map.removeKey(1L) }
        assertFalse(map.remove(2L, 1), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2L, 3))
        assertEquals(1, map.removeOrElse(5L) { 9 })
        assertEquals(9, map.removeOrElse(5L) { 9 })

        // a key that genuinely stores the default value is still present
        val stored = Long2IntHashMap()
        stored[1L] = stored[9L]
        assertTrue(stored.containsKey(1L))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1L) { calls++; 1 }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Long2IntHashMap()
        for (i in 0..<300) many[i.toLong()] = 1
        for (i in 0..<300 step 2) assertEquals(1, many.removeKey(i.toLong()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toLong()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableLong2IntMapOf(1L to 1, 2L to 2, 3L to 3)
        val expected = mapOf<Long, Int>(1L to 1, 2L to 2, 3L to 3)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Long, Int>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Long, Int>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Long>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Long, Int>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1L) traverser.remove() else traverser.value = 4
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Long, Int>(2L to 4, 3L to 4), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2L))
        assertFalse(map.keys.contains(1L))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4))

        val entry = long2IntMapOf(1L to 1).iterator().next()
        val (key, value) = entry
        assertEquals(1L, key)
        assertEquals(1, value)
        assertEquals<Long2IntMap.Entry>(AbstractLong2IntMap.SimpleEntry(1L, 1), entry)
        assertEquals(mapOf<Long, Int>(1L to 1).entries.single(), entry.asEntry())

        // equality holds against a different Long2IntMap implementation
        val hash: Long2IntMap = mutableLong2IntMapOf(1L to 1)
        assertEquals(long2IntMapOf(1L to 1), hash)
        assertEquals(hash, long2IntMapOf(1L to 1))
        assertEquals(long2IntMapOf(1L to 1).hashCode(), hash.hashCode())
        assertEquals(emptyLong2IntMap(), mutableLong2IntMapOf() as Long2IntMap)
        assertNotEquals(long2IntMapOf(1L to 2), hash)
    }
}

class Long2LongMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Long, Long>(), long2LongMapOf().asMap())
        assertEquals(mapOf<Long, Long>(1L to 1L), long2LongMapOf(1L to 1L).asMap())
        assertEquals(mapOf<Long, Long>(1L to 1L, 2L to 2L), long2LongMapOf(1L to 1L, 2L to 2L).asMap())
        assertEquals(emptyMap<Long, Long>(), emptyLong2LongMap().asMap())

        assertEquals(emptyMap<Long, Long>(), mutableLong2LongMapOf().asMap())
        assertEquals(mapOf<Long, Long>(1L to 1L), mutableLong2LongMapOf(1L to 1L).asMap())
        assertEquals(mapOf<Long, Long>(1L to 1L, 2L to 2L), mutableLong2LongMapOf(1L to 1L, 2L to 2L).asMap())

        assertEquals(mapOf<Long, Long>(1L to 1L), buildLong2LongMap { set(1L, 1L) }.asMap())
        assertEquals(mapOf<Long, Long>(1L to 1L), Long2LongHashMap().apply { set(1L, 1L) }.asMap())
        assertEquals(mapOf<Long, Long>(1L to 1L), Long2LongHashMap(mapOf<Long, Long>(1L to 1L)).asMap())
        assertEquals(mapOf<Long, Long>(1L to 1L), Long2LongHashMap(mutableLong2LongMapOf(1L to 1L)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Long2LongHashMap()

        assertTrue(map.isDefaultValue(map.put(1L, 1L)), "an absent key returns the default value")
        assertEquals(1L, map.put(1L, 2L))
        map[2L] = 3L

        assertEquals(2L, map[1L])
        assertEquals(3L, map.getValue(2L))
        assertEquals(4L, map.getOrDefault(9L, 4L))
        assertTrue(map.containsKey(2L))
        assertTrue(map.containsValue(3L))
        assertFalse(map.containsValue(9L))
        assertFailsWith<NoSuchElementException> { map.getValue(9L) }

        assertEquals(2L, map.putIfAbsent(1L, 4L), "a present key is not overwritten")
        assertEquals(2L, map.replace(1L, 4L))
        assertFailsWith<NoSuchElementException> { map.replace(9L, 1L) }

        assertEquals(4L, map.getOrElse(1L) { 9L })
        assertEquals(9L, map.getOrPut(3L) { 9L })
        assertEquals(1L, map.merge(4L, 1L) { _, _ -> 2L }, "merge inserts without combining")
        assertEquals(2L, map.merge(4L, 1L) { _, _ -> 2L })

        map.putAll(mutableLong2LongMapOf(5L to 1L))
        map.putAll(mapOf<Long, Long>(6L to 1L))
        assertEquals(6, map.size)

        assertEquals(4L, map.removeKey(1L))
        assertFailsWith<NoSuchElementException> { map.removeKey(1L) }
        assertFalse(map.remove(2L, 1L), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2L, 3L))
        assertEquals(1L, map.removeOrElse(5L) { 9L })
        assertEquals(9L, map.removeOrElse(5L) { 9L })

        // a key that genuinely stores the default value is still present
        val stored = Long2LongHashMap()
        stored[1L] = stored[9L]
        assertTrue(stored.containsKey(1L))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1L) { calls++; 1L }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Long2LongHashMap()
        for (i in 0..<300) many[i.toLong()] = 1L
        for (i in 0..<300 step 2) assertEquals(1L, many.removeKey(i.toLong()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toLong()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableLong2LongMapOf(1L to 1L, 2L to 2L, 3L to 3L)
        val expected = mapOf<Long, Long>(1L to 1L, 2L to 2L, 3L to 3L)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Long, Long>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Long, Long>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Long>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Long, Long>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1L) traverser.remove() else traverser.value = 4L
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Long, Long>(2L to 4L, 3L to 4L), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2L))
        assertFalse(map.keys.contains(1L))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4L))

        val entry = long2LongMapOf(1L to 1L).iterator().next()
        val (key, value) = entry
        assertEquals(1L, key)
        assertEquals(1L, value)
        assertEquals<Long2LongMap.Entry>(AbstractLong2LongMap.SimpleEntry(1L, 1L), entry)
        assertEquals(mapOf<Long, Long>(1L to 1L).entries.single(), entry.asEntry())

        // equality holds against a different Long2LongMap implementation
        val hash: Long2LongMap = mutableLong2LongMapOf(1L to 1L)
        assertEquals(long2LongMapOf(1L to 1L), hash)
        assertEquals(hash, long2LongMapOf(1L to 1L))
        assertEquals(long2LongMapOf(1L to 1L).hashCode(), hash.hashCode())
        assertEquals(emptyLong2LongMap(), mutableLong2LongMapOf() as Long2LongMap)
        assertNotEquals(long2LongMapOf(1L to 2L), hash)
    }
}

class Long2FloatMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Long, Float>(), long2FloatMapOf().asMap())
        assertEquals(mapOf<Long, Float>(1L to 1.5f), long2FloatMapOf(1L to 1.5f).asMap())
        assertEquals(mapOf<Long, Float>(1L to 1.5f, 2L to 2.5f), long2FloatMapOf(1L to 1.5f, 2L to 2.5f).asMap())
        assertEquals(emptyMap<Long, Float>(), emptyLong2FloatMap().asMap())

        assertEquals(emptyMap<Long, Float>(), mutableLong2FloatMapOf().asMap())
        assertEquals(mapOf<Long, Float>(1L to 1.5f), mutableLong2FloatMapOf(1L to 1.5f).asMap())
        assertEquals(mapOf<Long, Float>(1L to 1.5f, 2L to 2.5f), mutableLong2FloatMapOf(1L to 1.5f, 2L to 2.5f).asMap())

        assertEquals(mapOf<Long, Float>(1L to 1.5f), buildLong2FloatMap { set(1L, 1.5f) }.asMap())
        assertEquals(mapOf<Long, Float>(1L to 1.5f), Long2FloatHashMap().apply { set(1L, 1.5f) }.asMap())
        assertEquals(mapOf<Long, Float>(1L to 1.5f), Long2FloatHashMap(mapOf<Long, Float>(1L to 1.5f)).asMap())
        assertEquals(mapOf<Long, Float>(1L to 1.5f), Long2FloatHashMap(mutableLong2FloatMapOf(1L to 1.5f)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Long2FloatHashMap()

        assertTrue(map.isDefaultValue(map.put(1L, 1.5f)), "an absent key returns the default value")
        assertEquals(1.5f, map.put(1L, 2.5f))
        map[2L] = 3.5f

        assertEquals(2.5f, map[1L])
        assertEquals(3.5f, map.getValue(2L))
        assertEquals(4.5f, map.getOrDefault(9L, 4.5f))
        assertTrue(map.containsKey(2L))
        assertTrue(map.containsValue(3.5f))
        assertFalse(map.containsValue(9.5f))
        assertFailsWith<NoSuchElementException> { map.getValue(9L) }

        assertEquals(2.5f, map.putIfAbsent(1L, 4.5f), "a present key is not overwritten")
        assertEquals(2.5f, map.replace(1L, 4.5f))
        assertFailsWith<NoSuchElementException> { map.replace(9L, 1.5f) }

        assertEquals(4.5f, map.getOrElse(1L) { 9.5f })
        assertEquals(9.5f, map.getOrPut(3L) { 9.5f })
        assertEquals(1.5f, map.merge(4L, 1.5f) { _, _ -> 2.5f }, "merge inserts without combining")
        assertEquals(2.5f, map.merge(4L, 1.5f) { _, _ -> 2.5f })

        map.putAll(mutableLong2FloatMapOf(5L to 1.5f))
        map.putAll(mapOf<Long, Float>(6L to 1.5f))
        assertEquals(6, map.size)

        assertEquals(4.5f, map.removeKey(1L))
        assertFailsWith<NoSuchElementException> { map.removeKey(1L) }
        assertFalse(map.remove(2L, 1.5f), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2L, 3.5f))
        assertEquals(1.5f, map.removeOrElse(5L) { 9.5f })
        assertEquals(9.5f, map.removeOrElse(5L) { 9.5f })

        // a key that genuinely stores the default value is still present
        val stored = Long2FloatHashMap()
        stored[1L] = stored[9L]
        assertTrue(stored.containsKey(1L))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1L) { calls++; 1.5f }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Long2FloatHashMap()
        for (i in 0..<300) many[i.toLong()] = 1.5f
        for (i in 0..<300 step 2) assertEquals(1.5f, many.removeKey(i.toLong()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toLong()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableLong2FloatMapOf(1L to 1.5f, 2L to 2.5f, 3L to 3.5f)
        val expected = mapOf<Long, Float>(1L to 1.5f, 2L to 2.5f, 3L to 3.5f)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Long, Float>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Long, Float>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Long>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Long, Float>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1L) traverser.remove() else traverser.value = 4.5f
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Long, Float>(2L to 4.5f, 3L to 4.5f), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2L))
        assertFalse(map.keys.contains(1L))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4.5f))

        val entry = long2FloatMapOf(1L to 1.5f).iterator().next()
        val (key, value) = entry
        assertEquals(1L, key)
        assertEquals(1.5f, value)
        assertEquals<Long2FloatMap.Entry>(AbstractLong2FloatMap.SimpleEntry(1L, 1.5f), entry)
        assertEquals(mapOf<Long, Float>(1L to 1.5f).entries.single(), entry.asEntry())

        // equality holds against a different Long2FloatMap implementation
        val hash: Long2FloatMap = mutableLong2FloatMapOf(1L to 1.5f)
        assertEquals(long2FloatMapOf(1L to 1.5f), hash)
        assertEquals(hash, long2FloatMapOf(1L to 1.5f))
        assertEquals(long2FloatMapOf(1L to 1.5f).hashCode(), hash.hashCode())
        assertEquals(emptyLong2FloatMap(), mutableLong2FloatMapOf() as Long2FloatMap)
        assertNotEquals(long2FloatMapOf(1L to 2.5f), hash)
    }
}

class Long2DoubleMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Long, Double>(), long2DoubleMapOf().asMap())
        assertEquals(mapOf<Long, Double>(1L to 1.5), long2DoubleMapOf(1L to 1.5).asMap())
        assertEquals(mapOf<Long, Double>(1L to 1.5, 2L to 2.5), long2DoubleMapOf(1L to 1.5, 2L to 2.5).asMap())
        assertEquals(emptyMap<Long, Double>(), emptyLong2DoubleMap().asMap())

        assertEquals(emptyMap<Long, Double>(), mutableLong2DoubleMapOf().asMap())
        assertEquals(mapOf<Long, Double>(1L to 1.5), mutableLong2DoubleMapOf(1L to 1.5).asMap())
        assertEquals(mapOf<Long, Double>(1L to 1.5, 2L to 2.5), mutableLong2DoubleMapOf(1L to 1.5, 2L to 2.5).asMap())

        assertEquals(mapOf<Long, Double>(1L to 1.5), buildLong2DoubleMap { set(1L, 1.5) }.asMap())
        assertEquals(mapOf<Long, Double>(1L to 1.5), Long2DoubleHashMap().apply { set(1L, 1.5) }.asMap())
        assertEquals(mapOf<Long, Double>(1L to 1.5), Long2DoubleHashMap(mapOf<Long, Double>(1L to 1.5)).asMap())
        assertEquals(mapOf<Long, Double>(1L to 1.5), Long2DoubleHashMap(mutableLong2DoubleMapOf(1L to 1.5)).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Long2DoubleHashMap()

        assertTrue(map.isDefaultValue(map.put(1L, 1.5)), "an absent key returns the default value")
        assertEquals(1.5, map.put(1L, 2.5))
        map[2L] = 3.5

        assertEquals(2.5, map[1L])
        assertEquals(3.5, map.getValue(2L))
        assertEquals(4.5, map.getOrDefault(9L, 4.5))
        assertTrue(map.containsKey(2L))
        assertTrue(map.containsValue(3.5))
        assertFalse(map.containsValue(9.5))
        assertFailsWith<NoSuchElementException> { map.getValue(9L) }

        assertEquals(2.5, map.putIfAbsent(1L, 4.5), "a present key is not overwritten")
        assertEquals(2.5, map.replace(1L, 4.5))
        assertFailsWith<NoSuchElementException> { map.replace(9L, 1.5) }

        assertEquals(4.5, map.getOrElse(1L) { 9.5 })
        assertEquals(9.5, map.getOrPut(3L) { 9.5 })
        assertEquals(1.5, map.merge(4L, 1.5) { _, _ -> 2.5 }, "merge inserts without combining")
        assertEquals(2.5, map.merge(4L, 1.5) { _, _ -> 2.5 })

        map.putAll(mutableLong2DoubleMapOf(5L to 1.5))
        map.putAll(mapOf<Long, Double>(6L to 1.5))
        assertEquals(6, map.size)

        assertEquals(4.5, map.removeKey(1L))
        assertFailsWith<NoSuchElementException> { map.removeKey(1L) }
        assertFalse(map.remove(2L, 1.5), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2L, 3.5))
        assertEquals(1.5, map.removeOrElse(5L) { 9.5 })
        assertEquals(9.5, map.removeOrElse(5L) { 9.5 })

        // a key that genuinely stores the default value is still present
        val stored = Long2DoubleHashMap()
        stored[1L] = stored[9L]
        assertTrue(stored.containsKey(1L))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1L) { calls++; 1.5 }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Long2DoubleHashMap()
        for (i in 0..<300) many[i.toLong()] = 1.5
        for (i in 0..<300 step 2) assertEquals(1.5, many.removeKey(i.toLong()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toLong()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableLong2DoubleMapOf(1L to 1.5, 2L to 2.5, 3L to 3.5)
        val expected = mapOf<Long, Double>(1L to 1.5, 2L to 2.5, 3L to 3.5)

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Long, Double>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Long, Double>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Long>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Long, Double>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1L) traverser.remove() else traverser.value = 4.5
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Long, Double>(2L to 4.5, 3L to 4.5), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2L))
        assertFalse(map.keys.contains(1L))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains(4.5))

        val entry = long2DoubleMapOf(1L to 1.5).iterator().next()
        val (key, value) = entry
        assertEquals(1L, key)
        assertEquals(1.5, value)
        assertEquals<Long2DoubleMap.Entry>(AbstractLong2DoubleMap.SimpleEntry(1L, 1.5), entry)
        assertEquals(mapOf<Long, Double>(1L to 1.5).entries.single(), entry.asEntry())

        // equality holds against a different Long2DoubleMap implementation
        val hash: Long2DoubleMap = mutableLong2DoubleMapOf(1L to 1.5)
        assertEquals(long2DoubleMapOf(1L to 1.5), hash)
        assertEquals(hash, long2DoubleMapOf(1L to 1.5))
        assertEquals(long2DoubleMapOf(1L to 1.5).hashCode(), hash.hashCode())
        assertEquals(emptyLong2DoubleMap(), mutableLong2DoubleMapOf() as Long2DoubleMap)
        assertNotEquals(long2DoubleMapOf(1L to 2.5), hash)
    }
}

class Long2AnyMapConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyMap<Long, String?>(), long2AnyMapOf<String?>().asMap())
        assertEquals(mapOf<Long, String?>(1L to "v1"), long2AnyMapOf(1L to "v1").asMap())
        assertEquals(mapOf<Long, String?>(1L to "v1", 2L to "v2"), long2AnyMapOf(1L to "v1", 2L to "v2").asMap())
        assertEquals(emptyMap<Long, String?>(), emptyLong2AnyMap<String?>().asMap())

        assertEquals(emptyMap<Long, String?>(), mutableLong2AnyMapOf<String?>().asMap())
        assertEquals(mapOf<Long, String?>(1L to "v1"), mutableLong2AnyMapOf(1L to "v1").asMap())
        assertEquals(mapOf<Long, String?>(1L to "v1", 2L to "v2"), mutableLong2AnyMapOf(1L to "v1", 2L to "v2").asMap())

        assertEquals(mapOf<Long, String?>(1L to "v1"), buildLong2AnyMap<String?> { set(1L, "v1") }.asMap())
        assertEquals(mapOf<Long, String?>(1L to "v1"), Long2AnyHashMap<String?>().apply { set(1L, "v1") }.asMap())
        assertEquals(mapOf<Long, String?>(1L to "v1"), Long2AnyHashMap(mapOf<Long, String?>(1L to "v1")).asMap())
        assertEquals(mapOf<Long, String?>(1L to "v1"), Long2AnyHashMap(mutableLong2AnyMapOf(1L to "v1")).asMap())
    }

    @Test
    fun mutationRoundTrip() {
        val map = Long2AnyHashMap<String?>()

        assertTrue(map.isDefaultValue(map.put(1L, "v1")), "an absent key returns the default value")
        assertEquals("v1", map.put(1L, "v2"))
        map[2L] = "v3"

        assertEquals("v2", map[1L])
        assertEquals("v3", map.getValue(2L))
        assertEquals("v4", map.getOrDefault(9L, "v4"))
        assertTrue(map.containsKey(2L))
        assertTrue(map.containsValue("v3"))
        assertFalse(map.containsValue("v9"))
        assertFailsWith<NoSuchElementException> { map.getValue(9L) }

        assertEquals("v2", map.putIfAbsent(1L, "v4"), "a present key is not overwritten")
        assertEquals("v2", map.replace(1L, "v4"))
        assertFailsWith<NoSuchElementException> { map.replace(9L, "v1") }

        assertEquals("v4", map.getOrElse(1L) { "v9" })
        assertEquals("v9", map.getOrPut(3L) { "v9" })
        assertEquals("v1", map.merge(4L, "v1") { _, _ -> "v2" }, "merge inserts without combining")
        assertEquals("v2", map.merge(4L, "v1") { _, _ -> "v2" })

        map.putAll(mutableLong2AnyMapOf(5L to "v1"))
        map.putAll(mapOf<Long, String?>(6L to "v1"))
        assertEquals(6, map.size)

        assertEquals("v4", map.removeKey(1L))
        assertFailsWith<NoSuchElementException> { map.removeKey(1L) }
        assertFalse(map.remove(2L, "v1"), "a mismatched value must not remove the entry")
        assertTrue(map.remove(2L, "v3"))
        assertEquals("v1", map.removeOrElse(5L) { "v9" })
        assertEquals("v9", map.removeOrElse(5L) { "v9" })

        // a key that genuinely stores the default value is still present
        val stored = Long2AnyHashMap<String?>()
        stored[1L] = stored[9L]
        assertTrue(stored.containsKey(1L))
        assertEquals(1, stored.size)
        var calls = 0
        stored.getOrElse(1L) { calls++; "v1" }
        assertEquals(0, calls)

        // enough entries to force several rehashes, then remove half of them again
        val many = Long2AnyHashMap<String?>()
        for (i in 0..<300) many[i.toLong()] = "v1"
        for (i in 0..<300 step 2) assertEquals("v1", many.removeKey(i.toLong()), "lost entry $i")
        for (i in 1..<300 step 2) assertTrue(many.containsKey(i.toLong()), "lost entry $i")

        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun iterationViewsAndEquality() {
        val map = mutableLong2AnyMapOf(1L to "v1", 2L to "v2", 3L to "v3")
        val expected = mapOf<Long, String?>(1L to "v1", 2L to "v2", 3L to "v3")

        // hash iteration order is unspecified, so results are compared as maps
        val fromIterator = mutableMapOf<Long, String?>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }
        assertEquals(expected, fromIterator)

        val fromForeach = mutableMapOf<Long, String?>()
        map.traverse { k, v -> fromForeach[k] = v }
        assertEquals(expected, fromForeach)

        val fromKeys = mutableListOf<Long>()
        map.traverseKeys { fromKeys.add(it) }
        assertEquals(expected.keys, fromKeys.toSet())

        val fromTraverser = mutableMapOf<Long, String?>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            fromTraverser[traverser.key] = traverser.value
            if (traverser.key == 1L) traverser.remove() else traverser.value = "v4"
        }
        assertEquals(expected, fromTraverser)
        assertEquals(mapOf<Long, String?>(2L to "v4", 3L to "v4"), map.asMap())

        assertEquals(2, map.keys.size)
        assertTrue(map.keys.contains(2L))
        assertFalse(map.keys.contains(1L))
        assertEquals(2, map.values.size)
        assertTrue(map.values.contains("v4"))

        val entry = long2AnyMapOf(1L to "v1").iterator().next()
        val (key, value) = entry
        assertEquals(1L, key)
        assertEquals("v1", value)
        assertEquals<Long2AnyMap.Entry<String?>>(AbstractLong2AnyMap.SimpleEntry(1L, "v1"), entry)
        assertEquals(mapOf<Long, String?>(1L to "v1").entries.single(), entry.asEntry())

        // equality holds against a different Long2AnyMap implementation
        val hash: Long2AnyMap<String?> = mutableLong2AnyMapOf(1L to "v1")
        assertEquals(long2AnyMapOf(1L to "v1"), hash)
        assertEquals(hash, long2AnyMapOf(1L to "v1"))
        assertEquals(long2AnyMapOf(1L to "v1").hashCode(), hash.hashCode())
        assertEquals(emptyLong2AnyMap<String?>(), mutableLong2AnyMapOf<String?>() as Long2AnyMap<String?>)
        assertNotEquals(long2AnyMapOf(1L to "v2"), hash)
    }
}
