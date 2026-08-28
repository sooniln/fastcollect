package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Deep coverage for HashMap.kte. Int2Long is the representative expansion: the key and value types are distinct,
 * so a test that confuses the two fails to compile rather than passing silently. The other ten HashMap.kte
 * expansions are covered by MapConformanceTests plus the type-semantics suites; Int2IntHashMap comes from
 * InterleavedHashMap.kte instead and has its own deep suite.
 *
 * These are the deterministic edge cases. Long random operation sequences diffed against a reference HashMap -
 * which is what drives probing, deletion chains, growth and shrink - live in RandomizedWorkloadTests.
 */
class Int2LongHashMapTests {

    private val absent = Long.MIN_VALUE

    @Test
    fun constructors_produceExpectedContents() {
        assertTrue(Int2LongHashMap().isEmpty())
        assertTrue(Int2LongHashMap(64).isEmpty())

        val source = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        assertEquals<Int2LongMap>(source, Int2LongHashMap(source))
        assertEquals<Int2LongMap>(source, Int2LongHashMap(mapOf(1 to 10L, 2 to 20L)))
    }

    @Test
    fun constructor_customDefaultValue_isUsedForAbsentKeys() {
        val map = Int2LongHashMap(0, -1L)
        assertEquals(-1L, map[9])
        assertTrue(map.isDefaultValue(-1L))
        assertFalse(map.isDefaultValue(absent))

        map[9] = -1L
        assertTrue(map.containsKey(9), "a key whose stored value equals the default is still present")
        assertEquals(1, map.size)
    }

    @Test
    fun constructor_negativeCapacity_throws() {
        assertFailsWith<IllegalArgumentException> { Int2LongHashMap(-1) }
    }

    // ---------- reads ----------

    @Test
    fun get_absentKey_returnsDefaultValue() {
        val map = Int2LongHashMap()
        assertEquals(absent, map[1])
        assertTrue(map.isDefaultValue(map[1]))
    }

    @Test
    fun getValue_returnsValueOrThrows() {
        val map = Int2LongHashMap().apply { set(1, 10L) }
        assertEquals(10L, map.getValue(1))
        assertFailsWith<NoSuchElementException> { map.getValue(2) }
    }

    @Test
    fun getOrDefault_prefersTheStoredValue() {
        val map = Int2LongHashMap().apply { set(1, 10L) }
        assertEquals(10L, map.getOrDefault(1, 99L))
        assertEquals(99L, map.getOrDefault(2, 99L))
    }

    @Test
    fun containsKey_andContainsValue() {
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        assertTrue(map.containsKey(1))
        assertFalse(map.containsKey(3))
        assertTrue(map.containsValue(20L))
        assertFalse(map.containsValue(30L))
        assertFalse(Int2LongHashMap().containsValue(absent))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        // the default value doubles as the "absent" signal, so a key that genuinely stores it must not vanish
        val map = Int2LongHashMap()
        map[1] = absent
        assertTrue(map.containsKey(1))
        assertEquals(1, map.size)
        assertEquals(absent, map[1])
        assertTrue(map.containsValue(absent))
        assertEquals(absent, map.getValue(1))
        assertEquals(absent, map.getOrElse(1) { 99L })
        assertEquals(absent, map.getOrPut(1) { 99L })
    }

    @Test
    fun zeroKey_isAnOrdinaryKey() {
        // zero is the boundary key most likely to be confused with an empty slot
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        assertFalse(map.containsKey(0))
        assertEquals(absent, map[0])
        assertFailsWith<NoSuchElementException> { map.getValue(0) }

        map[0] = 99L
        assertTrue(map.containsKey(0))
        assertEquals(99L, map[0])
        assertEquals(10L, map[1])
        assertEquals(3, map.size)

        assertEquals(99L, map.removeKey(0))
        assertFalse(map.containsKey(0))
        assertEquals(2, map.size)
    }

    // ---------- writes ----------

    @Test
    fun put_returnsThePreviousValueOrTheDefault() {
        val map = Int2LongHashMap()
        assertEquals(absent, map.put(1, 10L))
        assertEquals(10L, map.put(1, 20L))
        assertEquals(20L, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun set_isPutWithoutTheReturnValue() {
        val map = Int2LongHashMap()
        map[1] = 10L
        map[1] = 20L
        assertEquals(20L, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun putIfAbsent_onlyWritesWhenTheKeyIsMissing() {
        val map = Int2LongHashMap()
        assertEquals(absent, map.putIfAbsent(1, 10L))
        assertEquals(10L, map[1])

        assertEquals(10L, map.putIfAbsent(1, 99L))
        assertEquals(10L, map[1])
    }

    @Test
    fun putIfAbsent_treatsAStoredDefaultValueAsPresent() {
        val map = Int2LongHashMap()
        map[1] = absent
        assertEquals(absent, map.putIfAbsent(1, 99L))
        assertEquals(absent, map[1], "the stored default value must not be overwritten")
        assertEquals(1, map.size)
    }

    @Test
    fun replace_updatesAnExistingKeyOrThrows() {
        val map = Int2LongHashMap().apply { set(1, 10L) }
        assertEquals(10L, map.replace(1, 20L))
        assertEquals(20L, map[1])
        assertFailsWith<NoSuchElementException> { map.replace(2, 0L) }
        assertFalse(map.containsKey(2), "a failed replace must not insert")
    }

    @Test
    fun removeKey_returnsValueOrThrows() {
        val map = Int2LongHashMap().apply { set(1, 10L) }
        assertEquals(10L, map.removeKey(1))
        assertEquals(0, map.size)
        assertFailsWith<NoSuchElementException> { map.removeKey(1) }
    }

    @Test
    fun remove_byKey_returnsThePreviousValueOrTheDefault() {
        val map = Int2LongHashMap().apply { set(1, 10L) }
        assertEquals(10L, map.remove(1))
        assertEquals(absent, map.remove(1))
    }

    @Test
    fun remove_byKeyAndValue_onlyRemovesOnAMatch() {
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        assertFalse(map.remove(1, 99L))
        assertEquals(10L, map[1], "a mismatched value must leave the entry alone")

        assertTrue(map.remove(1, 10L))
        assertFalse(map.containsKey(1))
        assertFalse(map.remove(3, 0L))
        assertEquals(1, map.size)
    }

    @Test
    fun clear_emptiesAndAllowsReuse() {
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        map.clear()
        assertTrue(map.isEmpty())
        assertFalse(map.containsKey(1))
        map[3] = 30L
        assertEquals(30L, map[3])
        assertEquals(1, map.size)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val target = Int2LongHashMap().apply { set(1, 10L) }
        target.putAll(Int2LongHashMap().apply { set(2, 20L); set(3, 30L) })
        assertEquals(3, target.size)
        assertEquals(listOf(10L, 20L, 30L), listOf(target[1], target[2], target[3]))
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val target = Int2LongHashMap().apply { set(1, 10L) }
        target.putAll(mapOf(2 to 20L, 3 to 30L))
        assertEquals(3, target.size)
        assertEquals(listOf(10L, 20L, 30L), listOf(target[1], target[2], target[3]))
    }

    @Test
    fun putAll_fromLargerMap_overwritesAndDoesNotAlias() {
        val target = Int2LongHashMap().apply { set(1, -1L); set(2, -2L) }
        val source = Int2LongHashMap()
        for (i in 1..50) source[i] = i.toLong() * 100

        target.putAll(source)

        assertEquals(50, target.size)
        for (i in 1..50) assertEquals(i.toLong() * 100, target[i], "entry $i")

        // the two maps must be independent afterwards
        source[1] = -999L
        assertEquals(100L, target[1])
    }

    // ---------- capacity ----------

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        map.ensureCapacity(10_000)
        assertEquals(2, map.size)
        assertEquals(10L, map[1])
        assertEquals(20L, map[2])
    }

    @Test
    fun ensureCapacity_negative_throws() {
        assertFailsWith<IllegalArgumentException> { Int2LongHashMap().ensureCapacity(-1) }
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2LongHashMap(1000).apply { set(1, 10L); set(2, 20L) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10L, map[1])
        assertEquals(20L, map[2])
    }

    // ---------- iteration ----------

    @Test
    fun foreach_matchesIterator() {
        val map = Int2LongHashMap()
        for (i in 1..50) map[i] = i.toLong() + 1000
        map[0] = 9999L

        val fromIterator = mutableMapOf<Int, Long>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            fromIterator[entry.key] = entry.value
        }

        val fromForeach = mutableMapOf<Int, Long>()
        map.foreach { k, v -> fromForeach[k] = v }

        assertEquals(fromIterator, fromForeach)
        assertEquals(51, fromForeach.size)
    }

    @Test
    fun foreach_emptyAndSingletonMap_matchesIterator() {
        val fromEmpty = mutableListOf<Pair<Int, Long>>()
        Int2LongHashMap().foreach { k, v -> fromEmpty.add(k to v) }
        assertEquals(emptyList(), fromEmpty)

        val fromSingleton = mutableListOf<Pair<Int, Long>>()
        Int2LongHashMap().apply { set(1, 42L) }.foreach { k, v -> fromSingleton.add(k to v) }
        assertEquals(listOf(1 to 42L), fromSingleton)
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        val map = Int2LongHashMap()
        for (i in 1..200) map[i] = i.toLong()

        val visited = mutableListOf<Int>()
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            visited.add(iterator.next().key)
            iterator.remove()
        }

        assertEquals((1..200).toList(), visited.sorted())
        assertTrue(map.isEmpty())
    }

    @Test
    fun iteratorSetValue_writesThroughToTheMap() {
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value = entry.value * 2
        }
        assertEquals(20L, map[1])
        assertEquals(40L, map[2])
    }

    @Test
    fun iteratorRemove_beforeNext_throws() {
        val map = Int2LongHashMap().apply { set(1, 10L) }
        assertFailsWith<IllegalStateException> { map.iterator().remove() }
    }

    @Test
    fun iteratorNext_pastEnd_throws() {
        val map = Int2LongHashMap().apply { set(1, 10L) }
        val iterator = map.iterator()
        iterator.next()
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun traverseRemove_visitsEveryEntryExactlyOnceAndRemovesMatching() {
        val map = Int2LongHashMap()
        for (i in 1..50) map[i] = i.toLong() + 1000

        val visited = mutableListOf<Int>()
        val traverser = map.traverser()
        while (traverser.forward()) {
            val key = traverser.key
            visited.add(key)
            if (key % 2 == 0) traverser.remove()
        }

        assertEquals((1..50).toList(), visited.sorted(), "every entry must be visited exactly once")
        assertEquals(25, map.size)
        for (k in 1..50 step 2) assertTrue(map.containsKey(k))
        for (k in 2..50 step 2) assertFalse(map.containsKey(k))
    }

    @Test
    fun traverserSetValue_writesThroughToTheMap() {
        val map = Int2LongHashMap()
        for (i in 1..20) map[i] = i.toLong()

        val traverser = map.traverser()
        while (traverser.forward()) traverser.value = traverser.value * 10

        for (i in 1..20) assertEquals(i.toLong() * 10, map[i])
    }

    @Test
    fun traverser_keyAndValueBeforeFirstForward_throw() {
        val traverser = Int2LongHashMap().apply { set(1, 10L) }.traverser()
        assertFailsWith<IllegalStateException> { traverser.key }
        assertFailsWith<IllegalStateException> { traverser.value }
        assertTrue(traverser.forward())
        assertEquals(1, traverser.key)
        assertEquals(10L, traverser.value)
        assertFalse(traverser.forward())
    }

    @Test
    fun traverser_afterRemove_keyAndValueThrowUntilTheNextForward() {
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        val traverser = map.traverser()
        assertTrue(traverser.forward())
        traverser.remove()
        assertFailsWith<IllegalStateException> { traverser.key }
        assertFailsWith<IllegalStateException> { traverser.value }
        assertTrue(traverser.forward())
        traverser.key
    }

    // ---------- equality ----------

    @Test
    fun equals_matchesAnyInt2LongMapImplementation() {
        // AbstractInt2LongMap.equals() must hold against ANY Int2LongMap implementation, not just the hash map
        val hash: Int2LongMap = Int2LongHashMap().apply { set(1, 10L) }
        val singleton = int2LongMapOf(1 to 10L)
        assertEquals(singleton, hash)
        assertEquals(hash, singleton)
        assertEquals(singleton.hashCode(), hash.hashCode())

        val empty: Int2LongMap = Int2LongHashMap()
        assertEquals(emptyInt2LongMap(), empty)
        assertEquals(empty, emptyInt2LongMap())
        assertEquals(emptyInt2LongMap().hashCode(), empty.hashCode())

        assertNotEquals(int2LongMapOf(1 to 99L), hash)
        assertNotEquals(int2LongMapOf(2 to 10L), hash)
        assertNotEquals(emptyInt2LongMap(), hash)
    }

    @Test
    fun equals_isOrderIndependent() {
        val a = Int2LongHashMap().apply { set(1, 10L); set(2, 20L); set(3, 30L) }
        val b = Int2LongHashMap().apply { set(3, 30L); set(2, 20L); set(1, 10L) }
        assertEquals<Int2LongMap>(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun toString_rendersTheStandardMapForm() {
        assertEquals("{}", Int2LongHashMap().toString())
        assertEquals("{1=10}", Int2LongHashMap().apply { set(1, 10L) }.toString())
    }
}
