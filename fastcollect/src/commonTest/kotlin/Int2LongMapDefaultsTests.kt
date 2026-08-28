package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Coverage for Map.kte: the factories, the EmptyInt2LongMap/SingletonInt2LongMap implementations, the inline
 * lookup helpers (getOrElse/getOrPut/merge/replaceOrSet/removeOrElse) and the boxed wrappers. These are default
 * implementations shared by every generated map, so they are tested once here rather than per expansion.
 */
class Int2LongMapDefaultsTests {

    private val absent = Long.MIN_VALUE

    // ---------- factories ----------

    @Test
    fun factories_produceExpectedContents() {
        assertTrue(int2LongMapOf().isEmpty())
        assertEquals(1, int2LongMapOf(1 to 10L).size)
        assertEquals(2, int2LongMapOf(1 to 10L, 2 to 20L).size)

        assertTrue(mutableInt2LongMapOf().isEmpty())
        assertEquals(10L, mutableInt2LongMapOf(1 to 10L)[1])
        assertEquals(20L, mutableInt2LongMapOf(1 to 10L, 2 to 20L)[2])

        val built = buildInt2LongMap { set(1, 10L); set(2, 20L) }
        assertEquals(2, built.size)
        assertEquals(10L, built[1])
        assertEquals(1, buildInt2LongMap(8) { set(1, 10L) }.size)
    }

    @Test
    fun varargFactory_lastDuplicateKeyWins() {
        val map = int2LongMapOf(1 to 10L, 1 to 20L)
        assertEquals(1, map.size)
        assertEquals(20L, map[1])
    }

    // ---------- EmptyInt2LongMap ----------

    @Test
    fun emptyMap_reportsEverythingAbsent() {
        val empty = emptyInt2LongMap()
        assertEquals(0, empty.size)
        assertTrue(empty.isEmpty())
        assertFalse(empty.containsKey(0))
        assertFalse(empty.containsKey(1))
        assertFalse(empty.containsValue(0L))
        assertEquals(absent, empty[1])
        assertFailsWith<NoSuchElementException> { empty.getValue(1) }
        assertEquals(99L, empty.getOrDefault(1, 99L))
        assertEquals(99L, empty.getOrElse(1) { 99L })
        assertFalse(empty.iterator().hasNext())
        assertFalse(empty.traverser().forward())
        assertTrue(empty.keys.isEmpty())
        assertTrue(empty.values.isEmpty())
        assertEquals("{}", empty.toString())
    }

    // ---------- SingletonInt2LongMap ----------

    @Test
    fun singletonMap_holdsExactlyOneEntry() {
        val single = int2LongMapOf(1 to 10L)
        assertEquals(1, single.size)
        assertFalse(single.isEmpty())
        assertTrue(single.containsKey(1))
        assertFalse(single.containsKey(2))
        assertTrue(single.containsValue(10L))
        assertFalse(single.containsValue(20L))
        assertEquals(10L, single[1])
        assertEquals(absent, single[2])
        assertEquals(10L, single.getValue(1))
        assertFailsWith<NoSuchElementException> { single.getValue(2) }
        assertEquals(listOf(1), single.keys.toBoxedList())
        assertEquals(listOf(10L), single.values.toBoxedList())
        assertEquals("{1=10}", single.toString())
    }

    @Test
    fun singletonMap_iteratorAndTraverserYieldOneEntry() {
        val single = int2LongMapOf(1 to 10L)

        val iterator = single.iterator()
        assertTrue(iterator.hasNext())
        val entry = iterator.next()
        assertEquals(1, entry.key)
        assertEquals(10L, entry.value)
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }

        val traverser = single.traverser()
        assertFailsWith<IllegalStateException> { traverser.key }
        assertFailsWith<IllegalStateException> { traverser.value }
        assertTrue(traverser.forward())
        assertEquals(1, traverser.key)
        assertEquals(10L, traverser.value)
        assertFalse(traverser.forward())
    }

    // ---------- inline lookup helpers ----------

    @Test
    fun getOrElse_invokesTheLambdaOnlyForAbsentKeys() {
        val map = mutableInt2LongMapOf(1 to 10L)
        var calls = 0
        assertEquals(10L, map.getOrElse(1) { calls++; 99L })
        assertEquals(0, calls)
        assertEquals(99L, map.getOrElse(2) { calls++; 99L })
        assertEquals(1, calls)
        assertFalse(map.containsKey(2), "getOrElse must not insert")
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableInt2LongMapOf()
        map[1] = absent
        var calls = 0
        assertEquals(absent, map.getOrElse(1) { calls++; 99L })
        assertEquals(0, calls, "a key storing the default value is present, not absent")
    }

    @Test
    fun getOrPut_insertsOnlyForAbsentKeys() {
        val map = mutableInt2LongMapOf(1 to 10L)
        var calls = 0
        assertEquals(10L, map.getOrPut(1) { calls++; 99L })
        assertEquals(0, calls)
        assertEquals(1, map.size)

        assertEquals(99L, map.getOrPut(2) { calls++; 99L })
        assertEquals(1, calls)
        assertEquals(99L, map[2])
        assertEquals(2, map.size)
    }

    @Test
    fun merge_insertsWhenAbsentAndCombinesWhenPresent() {
        val map = mutableInt2LongMapOf()
        var calls = 0
        assertEquals(10L, map.merge(1, 10L) { _, _ -> calls++; 0L })
        assertEquals(0, calls, "merge must not call the combiner for an absent key")
        assertEquals(10L, map[1])

        assertEquals(15L, map.merge(1, 5L) { old, value -> calls++; old + value })
        assertEquals(1, calls)
        assertEquals(15L, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun merge_combinerReturningTheOldValue_leavesTheEntryAlone() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(10L, map.merge(1, 5L) { old, _ -> old })
        assertEquals(10L, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun replaceOrSet_returnsTheOldValueOrTheFallback() {
        val map = mutableInt2LongMapOf(1 to 10L)
        var calls = 0

        assertEquals(10L, map.replaceOrSet(1, 20L) { calls++; -1L })
        assertEquals(0, calls, "a present key must not consult the fallback")
        assertEquals(20L, map[1])

        assertEquals(-1L, map.replaceOrSet(2, 30L) { calls++; -1L })
        assertEquals(1, calls)
        assertEquals(30L, map[2], "an absent key must still be inserted")
        assertEquals(2, map.size)
    }

    @Test
    fun replaceOrSet_treatsAStoredDefaultValueAsPresent() {
        val map = mutableInt2LongMapOf()
        map[1] = absent
        var calls = 0
        assertEquals(absent, map.replaceOrSet(1, 20L) { calls++; -1L })
        assertEquals(0, calls)
        assertEquals(20L, map[1])
    }

    @Test
    fun removeOrElse_returnsTheRemovedValueOrTheFallback() {
        val map = mutableInt2LongMapOf(1 to 10L)
        var calls = 0

        assertEquals(10L, map.removeOrElse(1) { calls++; -1L })
        assertEquals(0, calls)
        assertTrue(map.isEmpty())

        assertEquals(-1L, map.removeOrElse(1) { calls++; -1L })
        assertEquals(1, calls)
    }

    @Test
    fun removeOrElse_treatsAStoredDefaultValueAsPresent() {
        val map = mutableInt2LongMapOf()
        map[1] = absent
        var calls = 0
        assertEquals(absent, map.removeOrElse(1) { calls++; -1L })
        assertEquals(0, calls)
        assertTrue(map.isEmpty())
    }

    // ---------- entries ----------

    @Test
    fun entry_destructuresIntoKeyAndValue() {
        val entry = int2LongMapOf(1 to 10L).iterator().next()
        val (key, value) = entry
        assertEquals(1, key)
        assertEquals(10L, value)
        assertEquals(1, entry.component1())
        assertEquals(10L, entry.component2())
    }

    @Test
    fun entryEquals_matchesAnyInt2LongMapEntryImplementation() {
        // AbstractEntry.equals() must hold against ANY Int2LongMap.Entry implementation, not just the one the
        // hash map's iterator happens to hand out
        val fromHashMap: Int2LongMap.Entry = mutableInt2LongMapOf(1 to 10L).iterator().next()
        val fromSingleton: Int2LongMap.Entry = int2LongMapOf(1 to 10L).iterator().next()
        val simple: Int2LongMap.Entry = AbstractInt2LongMap.SimpleEntry(1, 10L)

        for (other in listOf(fromSingleton, simple)) {
            assertEquals(other, fromHashMap)
            assertEquals(fromHashMap, other)
            assertEquals(other.hashCode(), fromHashMap.hashCode())
        }

        assertNotEquals<Int2LongMap.Entry>(AbstractInt2LongMap.SimpleEntry(1, 99L), fromHashMap)
        assertNotEquals<Int2LongMap.Entry>(AbstractInt2LongMap.SimpleEntry(2, 10L), fromHashMap)
    }

    @Test
    fun entry_hashCodeAndToStringFollowTheMapEntryContract() {
        val entry: Int2LongMap.Entry = AbstractInt2LongMap.SimpleEntry(1, 10L)
        assertEquals(1.hashCode() xor 10L.hashCode(), entry.hashCode())
        assertEquals("1=10", entry.toString())
    }

    @Test
    fun asEntry_readOnlyWrapper_matchesTheStandardMapEntryContract() {
        val map = mutableInt2LongMapOf(1 to 10L)
        val wrapped: Map.Entry<Int, Long> = (map as Int2LongMap).iterator().next().asEntry()

        assertEquals(1, wrapped.key)
        assertEquals(10L, wrapped.value)
        assertEquals(mapOf(1 to 10L).entries.single(), wrapped)
        assertEquals(mapOf(1 to 10L).entries.single().hashCode(), wrapped.hashCode())
    }

    @Test
    fun asEntry_mutableWrapper_writesThroughToTheMap() {
        val map = mutableInt2LongMapOf(1 to 10L)
        val wrapped = map.iterator().next().asEntry()

        assertEquals(10L, wrapped.setValue(20L))
        assertEquals(20L, map[1])
        assertEquals(20L, wrapped.value)
    }

    // ---------- boxed map wrapper ----------

    @Test
    fun asMap_readOnlyView_reflectsTheBackingMap() {
        val backing = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        val view: Map<Int, Long> = (backing as Int2LongMap).asMap()

        assertEquals(mapOf(1 to 10L, 2 to 20L), view)
        assertEquals(10L, view[1])
        assertNull(view[3])

        backing[3] = 30L
        assertEquals(mapOf(1 to 10L, 2 to 20L, 3 to 30L), view)
    }

    @Test
    fun asMap_mutableView_writesThrough() {
        val backing = mutableInt2LongMapOf(1 to 10L)
        val view = backing.asMap()

        assertNull(view.put(2, 20L))
        assertEquals(20L, backing[2])

        assertEquals(10L, view.put(1, 99L))
        assertEquals(99L, backing[1])

        assertEquals(99L, view.remove(1))
        assertFalse(backing.containsKey(1))

        view.clear()
        assertTrue(backing.isEmpty())
    }
}
