package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Coverage for the map key/value views and the entry-stream projections. The views are live: they read straight
 * through to the map rather than snapshotting it, so a change to the map is visible through a view obtained
 * earlier.
 *
 * Both hash-map implementations are exercised, because the views are declared separately in HashMap.kte and
 * InterleavedHashMap.kte.
 */
class MapViewTests {

    // ---------- keys ----------

    @Test
    fun keys_reportsTheMapKeys() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L, 3 to 30L)
        val keys = map.keys

        assertEquals(3, keys.size)
        assertFalse(keys.isEmpty())
        assertEquals(listOf(1, 2, 3), keys.toBoxedList().sorted())
        assertTrue(keys.contains(1))
        assertFalse(keys.contains(4))
        assertTrue(keys.containsAll(intListOf(1, 3)))
        assertFalse(keys.containsAll(intListOf(1, 4)))
    }

    @Test
    fun keys_isALiveViewNotASnapshot() {
        val map = mutableInt2LongMapOf(1 to 10L)
        val keys = map.keys

        map[2] = 20L
        assertEquals(2, keys.size, "the view must see later insertions")
        assertTrue(keys.contains(2))

        map.removeKey(1)
        assertEquals(listOf(2), keys.toBoxedList())

        map.clear()
        assertTrue(keys.isEmpty())
        assertFalse(keys.contains(2))
    }

    @Test
    fun keys_iterationVisitsEveryKey() {
        val map = mutableInt2LongMapOf()
        for (i in 1..50) map[i] = i.toLong()
        map[0] = 0L

        val fromForeachKey = mutableListOf<Int>()
        map.traverseKeys { fromForeachKey.add(it) }

        // hash iteration order is unspecified, so only the multiset is guaranteed
        assertEquals((0..50).toList(), fromForeachKey.sorted())
        assertEquals((0..50).toList(), map.keys.toBoxedList().sorted())
    }

    @Test
    fun keys_traverserVisitsEveryKeyOnce() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L, 3 to 30L)

        val seen = mutableListOf<Int>()
        val traverser = map.keys.traverser()
        while (traverser.forward()) seen.add(traverser.value)

        assertEquals(listOf(1, 2, 3), seen.sorted())
    }

    @Test
    fun keys_behavesAsASetForEqualityAndRendering() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        assertEquals(intSetOf(1, 2), map.keys)
        assertEquals(intSetOf(1, 2).hashCode(), map.keys.hashCode())
        assertEquals(listOf("1", "2"), map.keys.toString().removeSurrounding("[", "]").split(", ").sorted())
    }

    @Test
    fun keys_onTheInterleavedMap() {
        val map = Int2IntHashMap()
        for (i in 1..20) map[i] = i * 10
        val keys = map.keys

        assertEquals(20, keys.size)
        assertEquals((1..20).toList(), keys.toBoxedList().sorted())
        assertTrue(keys.contains(7))
        assertFalse(keys.contains(21))

        map.removeKey(7)
        assertFalse(keys.contains(7))
        assertEquals(19, keys.size)
    }

    // ---------- values ----------

    @Test
    fun values_reportsTheMapValues() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L, 3 to 30L)
        val values = map.values

        assertEquals(3, values.size)
        assertFalse(values.isEmpty())
        assertEquals(listOf(10L, 20L, 30L), values.toBoxedList().sorted())
        assertTrue(values.contains(20L))
        assertFalse(values.contains(40L))
        assertTrue(values.containsAll(longListOf(10L, 30L)))
        assertFalse(values.containsAll(longListOf(10L, 40L)))
    }

    @Test
    fun values_retainsDuplicates() {
        // unlike keys, values is a plain collection: the same value stored under two keys appears twice
        val map = mutableInt2LongMapOf(1 to 7L, 2 to 7L, 3 to 8L)
        assertEquals(3, map.values.size)
        assertEquals(listOf(7L, 7L, 8L), map.values.toBoxedList().sorted())
    }

    @Test
    fun values_isALiveViewNotASnapshot() {
        val map = mutableInt2LongMapOf(1 to 10L)
        val values = map.values

        map[2] = 20L
        assertEquals(2, values.size)
        assertTrue(values.contains(20L))

        map[1] = 99L
        assertFalse(values.contains(10L), "the view must see later value updates")
        assertTrue(values.contains(99L))

        map.clear()
        assertTrue(values.isEmpty())
    }

    @Test
    fun values_iterationVisitsEveryValue() {
        val map = mutableInt2LongMapOf()
        for (i in 1..50) map[i] = i.toLong() * 10

        val fromEntries = mutableListOf<Long>()
        map.traverse { _, v -> fromEntries.add(v) }

        // hash iteration order is unspecified, so only the multiset is guaranteed
        assertEquals(fromEntries.sorted(), map.values.toBoxedList().sorted())
    }

    @Test
    fun values_traverserVisitsEveryValueOnce() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L, 3 to 30L)

        val seen = mutableListOf<Long>()
        val traverser = map.values.traverser()
        while (traverser.forward()) seen.add(traverser.value)

        assertEquals(listOf(10L, 20L, 30L), seen.sorted())
    }

    @Test
    fun values_onTheInterleavedMap() {
        val map = Int2IntHashMap()
        for (i in 1..20) map[i] = i * 10
        val values = map.values

        assertEquals(20, values.size)
        assertEquals((1..20).map { it * 10 }, values.toBoxedList().sorted())
        assertTrue(values.contains(70))
        assertFalse(values.contains(7))

        map[1] = 999
        assertTrue(values.contains(999))
        assertFalse(values.contains(10))
    }

    // ---------- empty and singleton maps ----------

    @Test
    fun emptyAndSingletonMapViews() {
        val empty = emptyInt2LongMap()
        assertTrue(empty.keys.isEmpty())
        assertTrue(empty.values.isEmpty())
        assertFalse(empty.keys.contains(1))
        assertFalse(empty.values.contains(1L))

        val single = int2LongMapOf(1 to 10L)
        assertEquals(listOf(1), single.keys.toBoxedList())
        assertEquals(listOf(10L), single.values.toBoxedList())
        assertTrue(single.keys.contains(1))
        assertTrue(single.values.contains(10L))
        assertFalse(single.keys.contains(2))
    }

    // ---------- views over the boxed wrapper ----------

    @Test
    fun asMap_keysAndValuesAndEntries_agreeWithThePrimitiveViews() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        val view = map.asMap()

        assertEquals(setOf(1, 2), view.keys)
        assertEquals(listOf(10L, 20L), view.values.sorted())
        assertEquals(mapOf(1 to 10L, 2 to 20L).entries, view.entries)
    }

    @Test
    fun asMap_entrySetValue_writesThroughToTheMap() {
        val map = mutableInt2LongMapOf(1 to 10L)
        val entry = map.asMap().entries.single()

        assertEquals(10L, entry.setValue(20L))
        assertEquals(20L, map[1])
    }

    @Test
    fun asMap_keysAndEntriesRemove_writeThroughToTheMap() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L, 3 to 30L)
        val view = map.asMap()

        assertTrue(view.keys.remove(1))
        assertFalse(map.containsKey(1))

        val iterator = view.entries.iterator()
        iterator.next()
        iterator.remove()
        assertEquals(1, map.size)
    }
}
