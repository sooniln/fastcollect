package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Deep coverage for Int2IntHashMap, which is the one map generated from InterleavedHashMap.kte rather than
 * HashMap.kte. Zero is its most awkward boundary value in both the key and the value position, so that is what
 * this suite concentrates on; the generic map behaviour shared with every other expansion is covered by
 * Int2LongHashMapTests and Int2LongMapDefaultsTests.
 *
 * Long random operation sequences diffed against a reference HashMap, with a value domain narrow enough that
 * zero-valued entries come up constantly, live in RandomizedWorkloadTests.
 */
class Int2IntHashMapTests {

    private val absent = Int.MIN_VALUE

    @Test
    fun constructors_produceExpectedContents() {
        assertTrue(Int2IntHashMap().isEmpty())
        assertTrue(Int2IntHashMap(64).isEmpty())

        val source = Int2IntHashMap().apply { set(1, 10); set(2, 20) }
        assertEquals<Int2IntMap>(source, Int2IntHashMap(source))
        assertEquals<Int2IntMap>(source, Int2IntHashMap(mapOf(1 to 10, 2 to 20)))
    }

    @Test
    fun constructor_customDefaultValue_isUsedForAbsentKeys() {
        val map = Int2IntHashMap(0, -1)
        assertEquals(-1, map[9])
        assertTrue(map.isDefaultValue(-1))
        assertFalse(map.isDefaultValue(absent))
    }

    @Test
    fun constructor_negativeCapacity_throws() {
        assertFailsWith<IllegalArgumentException> { Int2IntHashMap(-1) }
    }

    // ---------- zero ----------

    @Test
    fun zeroKey_isAnOrdinaryKey() {
        val map = Int2IntHashMap().apply { set(1, 10); set(2, 20) }
        assertFalse(map.containsKey(0))
        assertEquals(absent, map[0])
        assertFailsWith<NoSuchElementException> { map.getValue(0) }

        map[0] = 5
        assertTrue(map.containsKey(0))
        assertEquals(5, map[0])
        assertEquals(10, map[1])
        assertEquals(3, map.size)
    }

    @Test
    fun zeroKeyMappedToZero_isStillFindable() {
        // (0, 0) is the entry a packed-slot layout is most likely to lose, reached both by writing the value
        // directly and by writing it through an entry
        val direct = Int2IntHashMap().apply { set(0, 5); set(1, 1) }
        direct[0] = 0
        assertEquals(2, direct.size)
        assertTrue(direct.containsKey(0), "key 0 should still be findable after setting its value to 0")
        assertEquals(0, direct[0])
        assertEquals(1, direct[1])

        val viaEntry = Int2IntHashMap().apply { set(0, 5); set(1, 1) }
        val iterator = viaEntry.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key == 0) entry.value = 0
        }
        assertEquals(2, viaEntry.size)
        assertTrue(viaEntry.containsKey(0))
        assertEquals(0, viaEntry[0])
        assertEquals(1, viaEntry[1])
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = Int2IntHashMap()
        map[1] = absent
        assertTrue(map.containsKey(1))
        assertEquals(1, map.size)
        assertEquals(absent, map[1])
        assertTrue(map.containsValue(absent))
        assertEquals(absent, map.getOrElse(1) { 99 })
    }

    // ---------- capacity ----------

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2IntHashMap().apply { set(1, 10); set(2, 20) }
        map.ensureCapacity(10_000)
        assertEquals(2, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
    }

    @Test
    fun ensureCapacity_negative_throws() {
        assertFailsWith<IllegalArgumentException> { Int2IntHashMap().ensureCapacity(-1) }
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2IntHashMap(1000).apply { set(1, 10); set(2, 20) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
    }

    // ---------- iteration ----------

    @Test
    fun traverseRemove_visitsEveryEntryExactlyOnceAndRemovesMatching() {
        val map = Int2IntHashMap()
        for (i in 1..50) map[i] = i + 1000

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

    // ---------- equality ----------

    @Test
    fun equals_matchesAnyInt2IntMapImplementation() {
        val hash: Int2IntMap = Int2IntHashMap().apply { set(1, 10) }
        val singleton = int2IntMapOf(1 to 10)
        assertEquals(singleton, hash)
        assertEquals(hash, singleton)
        assertEquals(singleton.hashCode(), hash.hashCode())

        val empty: Int2IntMap = Int2IntHashMap()
        assertEquals(emptyInt2IntMap(), empty)
        assertEquals(emptyInt2IntMap().hashCode(), empty.hashCode())

        assertNotEquals(int2IntMapOf(1 to 99), hash)
        assertNotEquals(emptyInt2IntMap(), hash)
    }
}
