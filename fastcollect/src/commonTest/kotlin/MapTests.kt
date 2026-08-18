package io.github.sooniln.fastcollect

import kotlin.test.*

// ============================= Int2Byte =============================

class Int2ByteMapTest {
    @Test
    fun mapOf_vararg() {
        val map = int2ByteMapOf(1 to 10.toByte(), 2 to 20.toByte(), 3 to 30.toByte())
        assertEquals(3, map.size)
        assertEquals(10.toByte(), map[1])
        assertEquals(20.toByte(), map[2])
        assertEquals(30.toByte(), map[3])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte(), 2 to 20.toByte())
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildInt2ByteMap {
            set(1, 100.toByte())
            set(2, 110.toByte())
        }
        assertEquals(2, map.size)
        assertEquals(100.toByte(), map[1])
        assertEquals(110.toByte(), map[2])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Byte.MIN_VALUE, int2ByteMapOf(1 to 10.toByte())[99])
        assertEquals(Byte.MIN_VALUE, mutableInt2ByteMapOf(1 to 10.toByte())[99])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(int2ByteMapOf(1 to 10.toByte()).containsValue(10.toByte()))
        assertFalse(int2ByteMapOf(1 to 10.toByte()).containsValue(99.toByte()))
        assertTrue(mutableInt2ByteMapOf(1 to 10.toByte()).containsValue(10.toByte()))
        assertFalse(mutableInt2ByteMapOf(1 to 10.toByte()).containsValue(99.toByte()))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableInt2ByteMapOf().containsKey(0))

        val map = mutableInt2ByteMapOf(1 to 10.toByte())
        assertEquals(Byte.MIN_VALUE, map[0])
        assertFalse(map.containsKey(0))
        assertEquals(Byte.MIN_VALUE, map.remove(0))
        assertEquals(1, map.size)

        map[0] = 5.toByte()
        assertTrue(map.containsKey(0))
        assertEquals(5.toByte(), map[0])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableInt2ByteMapOf().isDefaultValue(Byte.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableInt2ByteMapOf(1 to 42.toByte())
        assertFalse(map.isDefaultValue(42.toByte()))
        assertFalse(map.isDefaultValue(0.toByte()))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableInt2ByteMapOf(1 to Byte.MIN_VALUE)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is the default value")
        assertEquals(Byte.MIN_VALUE, map[1])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10.toByte(), int2ByteMapOf(1 to 10.toByte()).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2ByteMapOf(1 to 10.toByte()).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2ByteMapOf(1 to 10.toByte()).getOrElse(99) { invoked = true; (-1).toByte() }
        assertTrue(invoked)
        assertEquals((-1).toByte(), result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2ByteMapOf(1 to 10.toByte()).getOrElse(1) { invoked = true; (-1).toByte() }
        assertFalse(invoked)
        assertEquals(10.toByte(), result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableInt2ByteMapOf(1 to Byte.MIN_VALUE)
        var invoked = false
        val result = map.getOrElse(1) { invoked = true; (-1).toByte() }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertEquals(Byte.MIN_VALUE, result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals((-99).toByte(), int2ByteMapOf(1 to 10.toByte()).getOrDefault(99, (-99).toByte()))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10.toByte(), int2ByteMapOf(1 to 10.toByte()).getOrDefault(1, (-99).toByte()))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte())
        assertEquals(42.toByte(), map.merge(99, 42.toByte()) { old, new -> (old + new).toByte() })
        assertEquals(42.toByte(), map[99])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte())
        assertEquals(15.toByte(), map.merge(1, 5.toByte()) { old, new -> (old + new).toByte() })
        assertEquals(15.toByte(), map[1])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte())
        assertEquals(42.toByte(), map.getOrPut(99) { 42.toByte() })
        assertEquals(42.toByte(), map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2ByteMapOf(1 to 10.toByte())
        val result = map.getOrPut(1) { invoked = true; 99.toByte() }
        assertFalse(invoked)
        assertEquals(10.toByte(), result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte())
        assertEquals(10.toByte(), map.replace(1, 20.toByte()))
        assertEquals(20.toByte(), map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2ByteMapOf(1 to 10.toByte()).replace(99, 20.toByte()) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte(), 2 to 20.toByte())
        assertEquals(10.toByte(), map.removeKey(1))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2ByteMapOf(1 to 10.toByte()).removeKey(99) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte(), 2 to 20.toByte())
        assertTrue(map.remove(1, 10.toByte()))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte(), 2 to 20.toByte())
        assertFalse(map.remove(1, 20.toByte()))
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableInt2ByteMapOf(1 to 10.toByte())
        assertFalse(map.remove(99, 10.toByte()))
        assertEquals(1, map.size)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = int2ByteMapOf(1 to 10.toByte(), 2 to 20.toByte())
        val dest = mutableInt2ByteMapOf(3 to 30.toByte())
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10.toByte(), dest[1])
        assertEquals(20.toByte(), dest[2])
        assertEquals(30.toByte(), dest[3])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableInt2ByteMapOf(1 to 100.toByte(), 6 to 60.toByte())
        val source = int2ByteMapOf(
            1 to 99.toByte(), 2 to 2.toByte(), 3 to 3.toByte(), 4 to 4.toByte(), 5 to 5.toByte(), 7 to 7.toByte(),
        )
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals(99.toByte(), dest[1])
        assertEquals(2.toByte(), dest[2])
        assertEquals(3.toByte(), dest[3])
        assertEquals(4.toByte(), dest[4])
        assertEquals(5.toByte(), dest[5])
        assertEquals(60.toByte(), dest[6])
        assertEquals(7.toByte(), dest[7])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableInt2ByteMapOf(3 to 30.toByte())
        dest.putAll(mapOf(1 to 10.toByte(), 2 to 20.toByte()))
        assertEquals(3, dest.size)
        assertEquals(10.toByte(), dest[1])
        assertEquals(20.toByte(), dest[2])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2ByteHashMap().apply { set(1, 10.toByte()); set(2, 20.toByte()) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1])
        assertEquals(20.toByte(), map[2])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2ByteHashMap(100).apply { set(1, 10.toByte()); set(2, 20.toByte()) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1])
        assertEquals(20.toByte(), map[2])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters; values are truncated to Byte
        // range via toByte(), consistently on both the write and the read-back assertion.
        val map = Int2ByteHashMap()
        for (i in 1..500) map[i] = (i + 1000).toByte()
        assertEquals(500, map.size)
        for (i in 1..500) assertEquals((i + 1000).toByte(), map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertEquals((i + 1000).toByte(), map.remove(i))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals((i + 1000).toByte(), map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2..500 step 2) assertEquals((i + 1000).toByte(), map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Int2ByteHashMap()
        for (i in 1..500) map[i] = (i + 1000).toByte()

        // mismatched value must not remove the entry
        for (i in 1..500 step 2) assertFalse(map.remove(i, i.toByte()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertTrue(map.remove(i, (i + 1000).toByte()))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals((i + 1000).toByte(), map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Int2ByteHashMap()
        for (i in 1..200) map[i] = (i + 1000).toByte()
        map.trimToSize()

        val visited = mutableListOf<Int>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals((entry.key + 1000).toByte(), entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1..200).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Int2ByteHashMap()
        for (i in 1..50) map[i] = (i + 1000).toByte()
        map[0] = 99.toByte()

        val fromIterator = mutableListOf<Pair<Int, Byte>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Int, Byte>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Int2ByteMap implementation,
        // not just the same concrete class.
        val map: Any = mutableInt2ByteMapOf(1 to 1.toByte(), 2 to 2.toByte())
        assertEquals(int2ByteMapOf(1 to 1.toByte(), 2 to 2.toByte()), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Int2ByteMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableInt2ByteMapOf(1 to 1.toByte()).iterator().next()
        assertEquals(mutableInt2ByteMapOf(1 to 1.toByte()).iterator().next(), entry)
    }
}

// ============================= Int2Int =============================

class Int2IntMapTest {
    @Test
    fun mapOf_vararg() {
        val map = int2IntMapOf(1 to 10, 2 to 20, 3 to 30)
        assertEquals(3, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
        assertEquals(30, map[3])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableInt2IntMapOf(1 to 10, 2 to 20)
        assertEquals(2, map.size)
        assertEquals(10, map[1])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildInt2IntMap {
            set(1, 100)
            set(2, 200)
        }
        assertEquals(2, map.size)
        assertEquals(100, map[1])
        assertEquals(200, map[2])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Int.MIN_VALUE, int2IntMapOf(1 to 10)[99])
        assertEquals(Int.MIN_VALUE, mutableInt2IntMapOf(1 to 10)[99])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(int2IntMapOf(1 to 10).containsValue(10))
        assertFalse(int2IntMapOf(1 to 10).containsValue(99))
        assertTrue(mutableInt2IntMapOf(1 to 10).containsValue(10))
        assertFalse(mutableInt2IntMapOf(1 to 10).containsValue(99))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableInt2IntMapOf().containsKey(0))

        val map = mutableInt2IntMapOf(1 to 10)
        assertEquals(Int.MIN_VALUE, map[0])
        assertFalse(map.containsKey(0))
        assertEquals(Int.MIN_VALUE, map.remove(0))
        assertEquals(1, map.size)

        map[0] = 5
        assertTrue(map.containsKey(0))
        assertEquals(5, map[0])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableInt2IntMapOf().isDefaultValue(Int.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableInt2IntMapOf(1 to 42)
        assertFalse(map.isDefaultValue(42))
        assertFalse(map.isDefaultValue(0))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableInt2IntMapOf(1 to Int.MIN_VALUE)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is the default value")
        assertEquals(Int.MIN_VALUE, map[1])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10, int2IntMapOf(1 to 10).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2IntMapOf(1 to 10).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2IntMapOf(1 to 10).getOrElse(99) { invoked = true; -1 }
        assertTrue(invoked)
        assertEquals(-1, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2IntMapOf(1 to 10).getOrElse(1) { invoked = true; -1 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableInt2IntMapOf(1 to Int.MIN_VALUE)
        var invoked = false
        val result = map.getOrElse(1) { invoked = true; -1 }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertEquals(Int.MIN_VALUE, result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99, int2IntMapOf(1 to 10).getOrDefault(99, -99))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10, int2IntMapOf(1 to 10).getOrDefault(1, -99))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2IntMapOf(1 to 10)
        assertEquals(42, map.merge(99, 42) { old, new -> old + new })
        assertEquals(42, map[99])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2IntMapOf(1 to 10)
        assertEquals(15, map.merge(1, 5) { old, new -> old + new })
        assertEquals(15, map[1])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2IntMapOf(1 to 10)
        assertEquals(42, map.getOrPut(99) { 42 })
        assertEquals(42, map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2IntMapOf(1 to 10)
        val result = map.getOrPut(1) { invoked = true; 99 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableInt2IntMapOf(1 to 10)
        assertEquals(10, map.replace(1, 20))
        assertEquals(20, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2IntMapOf(1 to 10).replace(99, 20) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableInt2IntMapOf(1 to 10, 2 to 20)
        assertEquals(10, map.removeKey(1))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2IntMapOf(1 to 10).removeKey(99) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableInt2IntMapOf(1 to 10, 2 to 20)
        assertTrue(map.remove(1, 10))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableInt2IntMapOf(1 to 10, 2 to 20)
        assertFalse(map.remove(1, 20))
        assertEquals(2, map.size)
        assertEquals(10, map[1])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableInt2IntMapOf(1 to 10)
        assertFalse(map.remove(99, 10))
        assertEquals(1, map.size)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = int2IntMapOf(1 to 10, 2 to 20)
        val dest = mutableInt2IntMapOf(3 to 30)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10, dest[1])
        assertEquals(20, dest[2])
        assertEquals(30, dest[3])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableInt2IntMapOf(1 to 100, 6 to 600)
        val source = int2IntMapOf(1 to 999, 2 to 2, 3 to 3, 4 to 4, 5 to 5, 7 to 7)
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals(999, dest[1])
        assertEquals(2, dest[2])
        assertEquals(3, dest[3])
        assertEquals(4, dest[4])
        assertEquals(5, dest[5])
        assertEquals(600, dest[6])
        assertEquals(7, dest[7])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableInt2IntMapOf(3 to 30)
        dest.putAll(mapOf(1 to 10, 2 to 20))
        assertEquals(3, dest.size)
        assertEquals(10, dest[1])
        assertEquals(20, dest[2])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2IntHashMap().apply { set(1, 10); set(2, 20) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
    }

    @Test
    fun ensureCapacity_zeroOnFreshMap_doesNotCorruptState() {
        // ensureCapacity(0) on a never-used map must not lower the threshold below what put() expects
        val map = Int2IntHashMap()
        map.ensureCapacity(0)
        map[1] = 10
        map[2] = 20
        assertEquals(2, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2IntHashMap(100).apply { set(1, 10); set(2, 20) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
    }

    @Test
    fun trimToSize_onFreshEmptyMap_doesNotCorruptSharedEmptyArray() {
        // trimToSize() on a never-used map shares a backing EMPTY_ARRAY singleton with all other instances;
        // it must not be mutated in place.
        val map = Int2IntHashMap()
        map.trimToSize()
        map[5] = 50
        map[6] = 60
        assertEquals(2, map.size)
        assertEquals(50, map[5])
        assertEquals(60, map[6])

        val freshMap = Int2IntHashMap()
        assertEquals(0, freshMap.size)
        assertFalse(freshMap.containsKey(5))
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Int2IntHashMap()
        for (i in 1..500) map[i] = i + 1000
        assertEquals(500, map.size)
        for (i in 1..500) assertEquals(i + 1000, map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertEquals(i + 1000, map.remove(i))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals(i + 1000, map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2..500 step 2) assertEquals(i + 1000, map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Int2IntHashMap()
        for (i in 1..500) map[i] = i + 1000

        // mismatched value must not remove the entry
        for (i in 1..500 step 2) assertFalse(map.remove(i, i))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertTrue(map.remove(i, i + 1000))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals(i + 1000, map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Int2IntHashMap()
        for (i in 1..200) map[i] = i + 1000
        map.trimToSize()

        val visited = mutableListOf<Int>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals(entry.key + 1000, entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1..200).toList(), visited.sorted())
    }

    @Test
    fun iteratorSetValue_collidingWithEmptySentinel_keepsEntryFindable() {
        // setting an entry's value to a packed (key, value) pair that matches the current empty-slot sentinel
        // must rotate the sentinel rather than making the entry indistinguishable from an empty slot.
        val map = Int2IntHashMap()
        map[0] = 5
        map[1] = 1

        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.key == 0) entry.value = 0
        }

        assertEquals(2, map.size)
        assertTrue(map.containsKey(0), "key 0 should still be findable after setting its value to 0")
        assertEquals(0, map[0])
        assertEquals(1, map[1])
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Int2IntHashMap()
        for (i in 1..50) map[i] = i + 1000
        map[0] = 9999

        val fromIterator = mutableListOf<Pair<Int, Int>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Int, Int>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun foreachKey_matchesKeysIterator() {
        val map = Int2IntHashMap()
        for (i in 1..50) map[i] = i + 1000

        val fromKeys = mutableListOf<Int>()
        val it = map.keys.iterator()
        while (it.hasNext()) fromKeys.add(it.nextInt())

        val fromForeachKey = mutableListOf<Int>()
        map.foreachKey { k -> fromForeachKey.add(k) }

        assertEquals(fromKeys.toSet(), fromForeachKey.toSet())
    }

    @Test
    fun foreach_emptyAndSingletonMap_matchesIterator() {
        val fromEmpty = mutableListOf<Pair<Int, Int>>()
        emptyInt2IntMap().foreach { k, v -> fromEmpty.add(k to v) }
        assertTrue(fromEmpty.isEmpty())

        val fromSingleton = mutableListOf<Pair<Int, Int>>()
        int2IntMapOf(1 to 42).foreach { k, v -> fromSingleton.add(k to v) }
        assertEquals(listOf(1 to 42), fromSingleton)
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Int2IntMap implementation,
        // not just the same concrete class.
        val map: Any = mutableInt2IntMapOf(1 to 1, 2 to 2)
        assertEquals(int2IntMapOf(1 to 1, 2 to 2), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Int2IntMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableInt2IntMapOf(1 to 1).iterator().next()
        assertEquals(mutableInt2IntMapOf(1 to 1).iterator().next(), entry)
    }
}

// ============================= Int2Long =============================

class Int2LongMapTest {
    @Test
    fun mapOf_vararg() {
        val map = int2LongMapOf(1 to 10L, 2 to 20L, 3 to 30L)
        assertEquals(3, map.size)
        assertEquals(10L, map[1])
        assertEquals(20L, map[2])
        assertEquals(30L, map[3])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        assertEquals(2, map.size)
        assertEquals(10L, map[1])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildInt2LongMap {
            set(1, 100L)
            set(2, 200L)
        }
        assertEquals(2, map.size)
        assertEquals(100L, map[1])
        assertEquals(200L, map[2])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Long.MIN_VALUE, int2LongMapOf(1 to 10L)[99])
        assertEquals(Long.MIN_VALUE, mutableInt2LongMapOf(1 to 10L)[99])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(int2LongMapOf(1 to 10L).containsValue(10L))
        assertFalse(int2LongMapOf(1 to 10L).containsValue(99L))
        assertTrue(mutableInt2LongMapOf(1 to 10L).containsValue(10L))
        assertFalse(mutableInt2LongMapOf(1 to 10L).containsValue(99L))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableInt2LongMapOf().containsKey(0))

        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(Long.MIN_VALUE, map[0])
        assertFalse(map.containsKey(0))
        assertEquals(Long.MIN_VALUE, map.remove(0))
        assertEquals(1, map.size)

        map[0] = 5L
        assertTrue(map.containsKey(0))
        assertEquals(5L, map[0])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableInt2LongMapOf().isDefaultValue(Long.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableInt2LongMapOf(1 to 42L)
        assertFalse(map.isDefaultValue(42L))
        assertFalse(map.isDefaultValue(0L))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableInt2LongMapOf(1 to Long.MIN_VALUE)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is the default value")
        assertEquals(Long.MIN_VALUE, map[1])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10L, int2LongMapOf(1 to 10L).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2LongMapOf(1 to 10L).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2LongMapOf(1 to 10L).getOrElse(99) { invoked = true; -1L }
        assertTrue(invoked)
        assertEquals(-1L, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2LongMapOf(1 to 10L).getOrElse(1) { invoked = true; -1L }
        assertFalse(invoked)
        assertEquals(10L, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableInt2LongMapOf(1 to Long.MIN_VALUE)
        var invoked = false
        val result = map.getOrElse(1) { invoked = true; -1L }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertEquals(Long.MIN_VALUE, result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99L, int2LongMapOf(1 to 10L).getOrDefault(99, -99L))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10L, int2LongMapOf(1 to 10L).getOrDefault(1, -99L))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(42L, map.merge(99, 42L) { old, new -> old + new })
        assertEquals(42L, map[99])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(15L, map.merge(1, 5L) { old, new -> old + new })
        assertEquals(15L, map[1])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(42L, map.getOrPut(99) { 42L })
        assertEquals(42L, map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2LongMapOf(1 to 10L)
        val result = map.getOrPut(1) { invoked = true; 99L }
        assertFalse(invoked)
        assertEquals(10L, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(10L, map.replace(1, 20L))
        assertEquals(20L, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2LongMapOf(1 to 10L).replace(99, 20L) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        assertEquals(10L, map.removeKey(1))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2LongMapOf(1 to 10L).removeKey(99) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        assertTrue(map.remove(1, 10L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        assertFalse(map.remove(1, 20L))
        assertEquals(2, map.size)
        assertEquals(10L, map[1])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertFalse(map.remove(99, 10L))
        assertEquals(1, map.size)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = int2LongMapOf(1 to 10L, 2 to 20L)
        val dest = mutableInt2LongMapOf(3 to 30L)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10L, dest[1])
        assertEquals(20L, dest[2])
        assertEquals(30L, dest[3])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableInt2LongMapOf(1 to 100L, 6 to 600L)
        val source = int2LongMapOf(1 to 999L, 2 to 2L, 3 to 3L, 4 to 4L, 5 to 5L, 7 to 7L)
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals(999L, dest[1])
        assertEquals(2L, dest[2])
        assertEquals(3L, dest[3])
        assertEquals(4L, dest[4])
        assertEquals(5L, dest[5])
        assertEquals(600L, dest[6])
        assertEquals(7L, dest[7])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableInt2LongMapOf(3 to 30L)
        dest.putAll(mapOf(1 to 10L, 2 to 20L))
        assertEquals(3, dest.size)
        assertEquals(10L, dest[1])
        assertEquals(20L, dest[2])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2LongHashMap().apply { set(1, 10L); set(2, 20L) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10L, map[1])
        assertEquals(20L, map[2])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2LongHashMap(100).apply { set(1, 10L); set(2, 20L) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10L, map[1])
        assertEquals(20L, map[2])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Int2LongHashMap()
        for (i in 1..500) map[i] = i + 1000L
        assertEquals(500, map.size)
        for (i in 1..500) assertEquals(i + 1000L, map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertEquals(i + 1000L, map.remove(i))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals(i + 1000L, map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2..500 step 2) assertEquals(i + 1000L, map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Int2LongHashMap()
        for (i in 1..500) map[i] = i + 1000L

        // mismatched value must not remove the entry
        for (i in 1..500 step 2) assertFalse(map.remove(i, i.toLong()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertTrue(map.remove(i, i + 1000L))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals(i + 1000L, map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Int2LongHashMap()
        for (i in 1..200) map[i] = i + 1000L
        map.trimToSize()

        val visited = mutableListOf<Int>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals(entry.key + 1000L, entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1..200).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Int2LongHashMap()
        for (i in 1..50) map[i] = i + 1000L
        map[0] = 9999L

        val fromIterator = mutableListOf<Pair<Int, Long>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Int, Long>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Int2LongMap implementation,
        // not just the same concrete class.
        val map: Any = mutableInt2LongMapOf(1 to 1L, 2 to 2L)
        assertEquals(int2LongMapOf(1 to 1L, 2 to 2L), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Int2LongMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableInt2LongMapOf(1 to 1L).iterator().next()
        assertEquals(mutableInt2LongMapOf(1 to 1L).iterator().next(), entry)
    }
}

// ============================= Int2Float =============================

class Int2FloatMapTest {
    @Test
    fun mapOf_vararg() {
        val map = int2FloatMapOf(1 to 10f, 2 to 20f, 3 to 30f)
        assertEquals(3, map.size)
        assertEquals(10f, map[1])
        assertEquals(20f, map[2])
        assertEquals(30f, map[3])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableInt2FloatMapOf(1 to 10f, 2 to 20f)
        assertEquals(2, map.size)
        assertEquals(10f, map[1])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildInt2FloatMap {
            set(1, 100f)
            set(2, 200f)
        }
        assertEquals(2, map.size)
        assertEquals(100f, map[1])
        assertEquals(200f, map[2])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(int2FloatMapOf(1 to 10f)[99].isNaN())
        assertTrue(mutableInt2FloatMapOf(1 to 10f)[99].isNaN())
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(int2FloatMapOf(1 to 10f).containsValue(10f))
        assertFalse(int2FloatMapOf(1 to 10f).containsValue(99f))
        assertTrue(mutableInt2FloatMapOf(1 to 10f).containsValue(10f))
        assertFalse(mutableInt2FloatMapOf(1 to 10f).containsValue(99f))
    }

    @Test
    fun containsValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN values must be found by value queries, and -0.0f must not match 0.0f
        assertTrue(int2FloatMapOf(1 to Float.NaN).containsValue(Float.NaN))
        assertTrue(mutableInt2FloatMapOf(1 to Float.NaN).containsValue(Float.NaN))
        assertFalse(int2FloatMapOf(1 to -0.0f).containsValue(0.0f))
        assertFalse(mutableInt2FloatMapOf(1 to -0.0f).containsValue(0.0f))

        val view = mutableInt2FloatMapOf(1 to Float.NaN).asMap()
        assertTrue(view.entries.contains(mapOf(1 to Float.NaN).entries.first()))

        // updating an entry whose stored value is NaN must not report concurrent modification
        val mutableView = mutableInt2FloatMapOf(1 to Float.NaN).asMutableMap()
        val entry = mutableView.entries.iterator().next()
        assertTrue(entry.setValue(5f).isNaN())
        assertEquals(5f, mutableView[1])
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableInt2FloatMapOf().containsKey(0))

        val map = mutableInt2FloatMapOf(1 to 10f)
        assertTrue(map[0].isNaN())
        assertFalse(map.containsKey(0))
        assertTrue(map.remove(0).isNaN())
        assertEquals(1, map.size)

        map[0] = 5f
        assertTrue(map.containsKey(0))
        assertEquals(5f, map[0])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableInt2FloatMapOf().isDefaultValue(Float.NaN))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableInt2FloatMapOf(1 to 42f)
        assertFalse(map.isDefaultValue(42f))
        assertFalse(map.isDefaultValue(0f))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableInt2FloatMapOf(1 to Float.NaN)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is the default value")
        assertTrue(map[1].isNaN())
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10f, int2FloatMapOf(1 to 10f).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2FloatMapOf(1 to 10f).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2FloatMapOf(1 to 10f).getOrElse(99) { invoked = true; -1f }
        assertTrue(invoked)
        assertEquals(-1f, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2FloatMapOf(1 to 10f).getOrElse(1) { invoked = true; -1f }
        assertFalse(invoked)
        assertEquals(10f, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableInt2FloatMapOf(1 to Float.NaN)
        var invoked = false
        val result = map.getOrElse(1) { invoked = true; -1f }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertTrue(result.isNaN())
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99f, int2FloatMapOf(1 to 10f).getOrDefault(99, -99f))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10f, int2FloatMapOf(1 to 10f).getOrDefault(1, -99f))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2FloatMapOf(1 to 10f)
        assertEquals(42f, map.merge(99, 42f) { old, new -> old + new })
        assertEquals(42f, map[99])

        // a NaN value must be inserted for an absent key even though NaN is the map default
        assertTrue(map.merge(98, Float.NaN) { _, new -> new }.isNaN())
        assertTrue(map.containsKey(98))
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2FloatMapOf(1 to 10f)
        assertEquals(15f, map.merge(1, 5f) { old, new -> old + new })
        assertEquals(15f, map[1])

        // merging -0.0f to 0.0f must store the updated value even though -0.0f == 0.0f
        map[2] = -0.0f
        map.merge(2, 0.0f) { _, new -> new }
        assertEquals(0.0f.toBits(), map[2].toBits())
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2FloatMapOf(1 to 10f)
        assertEquals(42f, map.getOrPut(99) { 42f })
        assertEquals(42f, map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2FloatMapOf(1 to 10f)
        val result = map.getOrPut(1) { invoked = true; 99f }
        assertFalse(invoked)
        assertEquals(10f, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableInt2FloatMapOf(1 to 10f)
        assertEquals(10f, map.replace(1, 20f))
        assertEquals(20f, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2FloatMapOf(1 to 10f).replace(99, 20f) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableInt2FloatMapOf(1 to 10f, 2 to 20f)
        assertEquals(10f, map.removeKey(1))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2FloatMapOf(1 to 10f).removeKey(99) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableInt2FloatMapOf(1 to 10f, 2 to 20f)
        assertTrue(map.remove(1, 10f))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableInt2FloatMapOf(1 to 10f, 2 to 20f)
        assertFalse(map.remove(1, 20f))
        assertEquals(2, map.size)
        assertEquals(10f, map[1])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableInt2FloatMapOf(1 to 10f)
        assertFalse(map.remove(99, 10f))
        assertEquals(1, map.size)
    }

    @Test
    fun remove_keyValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must match NaN for removal, matching boxed semantics
        val nanMap = mutableInt2FloatMapOf(1 to Float.NaN)
        assertTrue(nanMap.remove(1, Float.NaN))
        assertFalse(nanMap.containsKey(1))

        // -0.0f must not match 0.0f for removal
        val negZeroMap = mutableInt2FloatMapOf(1 to -0.0f)
        assertFalse(negZeroMap.remove(1, 0.0f))
        assertTrue(negZeroMap.containsKey(1))
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = int2FloatMapOf(1 to 10f, 2 to 20f)
        val dest = mutableInt2FloatMapOf(3 to 30f)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10f, dest[1])
        assertEquals(20f, dest[2])
        assertEquals(30f, dest[3])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableInt2FloatMapOf(3 to 30f)
        dest.putAll(mapOf(1 to 10f, 2 to 20f))
        assertEquals(3, dest.size)
        assertEquals(10f, dest[1])
        assertEquals(20f, dest[2])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2FloatHashMap().apply { set(1, 10f); set(2, 20f) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10f, map[1])
        assertEquals(20f, map[2])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2FloatHashMap(100).apply { set(1, 10f); set(2, 20f) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10f, map[1])
        assertEquals(20f, map[2])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Int2FloatHashMap()
        for (i in 1..500) map[i] = (i + 1000).toFloat()
        assertEquals(500, map.size)
        for (i in 1..500) assertEquals((i + 1000).toFloat(), map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertEquals((i + 1000).toFloat(), map.remove(i))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals((i + 1000).toFloat(), map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2..500 step 2) assertEquals((i + 1000).toFloat(), map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Int2FloatHashMap()
        for (i in 1..500) map[i] = (i + 1000).toFloat()

        // mismatched value must not remove the entry
        for (i in 1..500 step 2) assertFalse(map.remove(i, i.toFloat()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertTrue(map.remove(i, (i + 1000).toFloat()))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals((i + 1000).toFloat(), map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Int2FloatHashMap()
        for (i in 1..200) map[i] = (i + 1000).toFloat()
        map.trimToSize()

        val visited = mutableListOf<Int>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals((entry.key + 1000).toFloat(), entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1..200).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Int2FloatHashMap()
        for (i in 1..50) map[i] = (i + 1000).toFloat()
        map[0] = 9999f

        val fromIterator = mutableListOf<Pair<Int, Int>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value.toBits())
        }

        val fromForeach = mutableListOf<Pair<Int, Int>>()
        map.foreach { k, v -> fromForeach.add(k to v.toBits()) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Int2FloatMap implementation,
        // not just the same concrete class.
        val map: Any = mutableInt2FloatMapOf(1 to 1f, 2 to 2f)
        assertEquals(int2FloatMapOf(1 to 1f, 2 to 2f), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see Int2FloatHashMap.Entry's equals()) requires equality against ANY
        // Int2FloatMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableInt2FloatMapOf(1 to 1f).iterator().next()
        assertEquals(mutableInt2FloatMapOf(1 to 1f).iterator().next(), entry)
    }
}

// ============================= Int2Double =============================

class Int2DoubleMapTest {
    @Test
    fun mapOf_vararg() {
        val map = int2DoubleMapOf(1 to 10.0, 2 to 20.0, 3 to 30.0)
        assertEquals(3, map.size)
        assertEquals(10.0, map[1])
        assertEquals(20.0, map[2])
        assertEquals(30.0, map[3])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableInt2DoubleMapOf(1 to 10.0, 2 to 20.0)
        assertEquals(2, map.size)
        assertEquals(10.0, map[1])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildInt2DoubleMap {
            set(1, 100.0)
            set(2, 200.0)
        }
        assertEquals(2, map.size)
        assertEquals(100.0, map[1])
        assertEquals(200.0, map[2])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(int2DoubleMapOf(1 to 10.0)[99].isNaN())
        assertTrue(mutableInt2DoubleMapOf(1 to 10.0)[99].isNaN())
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(int2DoubleMapOf(1 to 10.0).containsValue(10.0))
        assertFalse(int2DoubleMapOf(1 to 10.0).containsValue(99.0))
        assertTrue(mutableInt2DoubleMapOf(1 to 10.0).containsValue(10.0))
        assertFalse(mutableInt2DoubleMapOf(1 to 10.0).containsValue(99.0))
    }

    @Test
    fun containsValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN values must be found by value queries, and -0.0 must not match 0.0
        assertTrue(int2DoubleMapOf(1 to Double.NaN).containsValue(Double.NaN))
        assertTrue(mutableInt2DoubleMapOf(1 to Double.NaN).containsValue(Double.NaN))
        assertFalse(int2DoubleMapOf(1 to -0.0).containsValue(0.0))
        assertFalse(mutableInt2DoubleMapOf(1 to -0.0).containsValue(0.0))

        val view = mutableInt2DoubleMapOf(1 to Double.NaN).asMap()
        assertTrue(view.entries.contains(mapOf(1 to Double.NaN).entries.first()))

        // updating an entry whose stored value is NaN must not report concurrent modification
        val mutableView = mutableInt2DoubleMapOf(1 to Double.NaN).asMutableMap()
        val entry = mutableView.entries.iterator().next()
        assertTrue(entry.setValue(5.0).isNaN())
        assertEquals(5.0, mutableView[1])
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableInt2DoubleMapOf().containsKey(0))

        val map = mutableInt2DoubleMapOf(1 to 10.0)
        assertTrue(map[0].isNaN())
        assertFalse(map.containsKey(0))
        assertTrue(map.remove(0).isNaN())
        assertEquals(1, map.size)

        map[0] = 5.0
        assertTrue(map.containsKey(0))
        assertEquals(5.0, map[0])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableInt2DoubleMapOf().isDefaultValue(Double.NaN))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableInt2DoubleMapOf(1 to 42.0)
        assertFalse(map.isDefaultValue(42.0))
        assertFalse(map.isDefaultValue(0.0))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableInt2DoubleMapOf(1 to Double.NaN)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is the default value")
        assertTrue(map[1].isNaN())
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10.0, int2DoubleMapOf(1 to 10.0).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2DoubleMapOf(1 to 10.0).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2DoubleMapOf(1 to 10.0).getOrElse(99) { invoked = true; -1.0 }
        assertTrue(invoked)
        assertEquals(-1.0, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2DoubleMapOf(1 to 10.0).getOrElse(1) { invoked = true; -1.0 }
        assertFalse(invoked)
        assertEquals(10.0, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableInt2DoubleMapOf(1 to Double.NaN)
        var invoked = false
        val result = map.getOrElse(1) { invoked = true; -1.0 }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertTrue(result.isNaN())
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99.0, int2DoubleMapOf(1 to 10.0).getOrDefault(99, -99.0))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10.0, int2DoubleMapOf(1 to 10.0).getOrDefault(1, -99.0))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2DoubleMapOf(1 to 10.0)
        assertEquals(42.0, map.merge(99, 42.0) { old, new -> old + new })
        assertEquals(42.0, map[99])

        // a NaN value must be inserted for an absent key even though NaN is the map default
        assertTrue(map.merge(98, Double.NaN) { _, new -> new }.isNaN())
        assertTrue(map.containsKey(98))
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2DoubleMapOf(1 to 10.0)
        assertEquals(15.0, map.merge(1, 5.0) { old, new -> old + new })
        assertEquals(15.0, map[1])

        // merging -0.0 to 0.0 must store the updated value even though -0.0 == 0.0
        map[2] = -0.0
        map.merge(2, 0.0) { _, new -> new }
        assertEquals(0.0.toBits(), map[2].toBits())
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2DoubleMapOf(1 to 10.0)
        assertEquals(42.0, map.getOrPut(99) { 42.0 })
        assertEquals(42.0, map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2DoubleMapOf(1 to 10.0)
        val result = map.getOrPut(1) { invoked = true; 99.0 }
        assertFalse(invoked)
        assertEquals(10.0, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableInt2DoubleMapOf(1 to 10.0)
        assertEquals(10.0, map.replace(1, 20.0))
        assertEquals(20.0, map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2DoubleMapOf(1 to 10.0).replace(99, 20.0) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableInt2DoubleMapOf(1 to 10.0, 2 to 20.0)
        assertEquals(10.0, map.removeKey(1))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2DoubleMapOf(1 to 10.0).removeKey(99) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableInt2DoubleMapOf(1 to 10.0, 2 to 20.0)
        assertTrue(map.remove(1, 10.0))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableInt2DoubleMapOf(1 to 10.0, 2 to 20.0)
        assertFalse(map.remove(1, 20.0))
        assertEquals(2, map.size)
        assertEquals(10.0, map[1])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableInt2DoubleMapOf(1 to 10.0)
        assertFalse(map.remove(99, 10.0))
        assertEquals(1, map.size)
    }

    @Test
    fun remove_keyValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must match NaN for removal, matching boxed semantics
        val nanMap = mutableInt2DoubleMapOf(1 to Double.NaN)
        assertTrue(nanMap.remove(1, Double.NaN))
        assertFalse(nanMap.containsKey(1))

        // -0.0 must not match 0.0 for removal
        val negZeroMap = mutableInt2DoubleMapOf(1 to -0.0)
        assertFalse(negZeroMap.remove(1, 0.0))
        assertTrue(negZeroMap.containsKey(1))
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = int2DoubleMapOf(1 to 10.0, 2 to 20.0)
        val dest = mutableInt2DoubleMapOf(3 to 30.0)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10.0, dest[1])
        assertEquals(20.0, dest[2])
        assertEquals(30.0, dest[3])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableInt2DoubleMapOf(3 to 30.0)
        dest.putAll(mapOf(1 to 10.0, 2 to 20.0))
        assertEquals(3, dest.size)
        assertEquals(10.0, dest[1])
        assertEquals(20.0, dest[2])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2DoubleHashMap().apply { set(1, 10.0); set(2, 20.0) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10.0, map[1])
        assertEquals(20.0, map[2])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2DoubleHashMap(100).apply { set(1, 10.0); set(2, 20.0) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10.0, map[1])
        assertEquals(20.0, map[2])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Int2DoubleHashMap()
        for (i in 1..500) map[i] = (i + 1000).toDouble()
        assertEquals(500, map.size)
        for (i in 1..500) assertEquals((i + 1000).toDouble(), map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertEquals((i + 1000).toDouble(), map.remove(i))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals((i + 1000).toDouble(), map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2..500 step 2) assertEquals((i + 1000).toDouble(), map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Int2DoubleHashMap()
        for (i in 1..500) map[i] = (i + 1000).toDouble()

        // mismatched value must not remove the entry
        for (i in 1..500 step 2) assertFalse(map.remove(i, i.toDouble()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertTrue(map.remove(i, (i + 1000).toDouble()))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals((i + 1000).toDouble(), map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Int2DoubleHashMap()
        for (i in 1..200) map[i] = (i + 1000).toDouble()
        map.trimToSize()

        val visited = mutableListOf<Int>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals((entry.key + 1000).toDouble(), entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1..200).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Int2DoubleHashMap()
        for (i in 1..50) map[i] = (i + 1000).toDouble()
        map[0] = 9999.0

        val fromIterator = mutableListOf<Pair<Int, Long>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value.toBits())
        }

        val fromForeach = mutableListOf<Pair<Int, Long>>()
        map.foreach { k, v -> fromForeach.add(k to v.toBits()) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Int2DoubleMap
        // implementation, not just the same concrete class.
        val map: Any = mutableInt2DoubleMapOf(1 to 1.0, 2 to 2.0)
        assertEquals(int2DoubleMapOf(1 to 1.0, 2 to 2.0), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see Int2DoubleHashMap.Entry's equals()) requires equality against ANY
        // Int2DoubleMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableInt2DoubleMapOf(1 to 1.0).iterator().next()
        assertEquals(mutableInt2DoubleMapOf(1 to 1.0).iterator().next(), entry)
    }
}

// ============================= Int2Any =============================

class Int2AnyMapTest {
    @Test
    fun mapOf_vararg() {
        val map = int2AnyMapOf(1 to "a", 2 to "b", 3 to "c")
        assertEquals(3, map.size)
        assertEquals("a", map[1])
        assertEquals("b", map[2])
        assertEquals("c", map[3])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableInt2AnyMapOf(1 to "a", 2 to "b")
        assertEquals(2, map.size)
        assertEquals("a", map[1])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildInt2AnyMap<String> {
            set(1, "a")
            set(2, "b")
        }
        assertEquals(2, map.size)
        assertEquals("a", map[1])
        assertEquals("b", map[2])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertNull(int2AnyMapOf(1 to "a")[99])
        assertNull(mutableInt2AnyMapOf(1 to "a")[99])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(int2AnyMapOf(1 to "a").containsValue("a"))
        assertFalse(int2AnyMapOf(1 to "a").containsValue("z"))
        assertTrue(mutableInt2AnyMapOf(1 to "a").containsValue("a"))
        assertFalse(mutableInt2AnyMapOf(1 to "a").containsValue("z"))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableInt2AnyMapOf<String>().containsKey(0))

        val map = mutableInt2AnyMapOf(1 to "a")
        assertNull(map[0])
        assertFalse(map.containsKey(0))
        assertNull(map.remove(0))
        assertEquals(1, map.size)

        map[0] = "z"
        assertTrue(map.containsKey(0))
        assertEquals("z", map[0])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableInt2AnyMapOf<String>().isDefaultValue(null))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableInt2AnyMapOf(1 to "a")
        assertFalse(map.isDefaultValue("a"))
        assertFalse(map.isDefaultValue(""))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableInt2AnyMapOf<String?>(1 to null)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is the default value")
        assertNull(map[1])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals("a", int2AnyMapOf(1 to "a").getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2AnyMapOf(1 to "a").getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2AnyMapOf(1 to "a").getOrElse(99) { invoked = true; "fallback" }
        assertTrue(invoked)
        assertEquals("fallback", result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2AnyMapOf(1 to "a").getOrElse(1) { invoked = true; "fallback" }
        assertFalse(invoked)
        assertEquals("a", result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableInt2AnyMapOf<String?>(1 to null)
        var invoked = false
        val result = map.getOrElse(1) { invoked = true; "fallback" }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertNull(result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals("fallback", int2AnyMapOf(1 to "a").getOrDefault(99, "fallback"))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals("a", int2AnyMapOf(1 to "a").getOrDefault(1, "fallback"))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2AnyMapOf(1 to "a")
        assertEquals("x", map.merge(99, "x") { old, new -> old + new })
        assertEquals("x", map[99])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2AnyMapOf(1 to "a")
        assertEquals("ab", map.merge(1, "b") { old, new -> old + new })
        assertEquals("ab", map[1])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2AnyMapOf(1 to "a")
        assertEquals("x", map.getOrPut(99) { "x" })
        assertEquals("x", map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2AnyMapOf(1 to "a")
        val result = map.getOrPut(1) { invoked = true; "x" }
        assertFalse(invoked)
        assertEquals("a", result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableInt2AnyMapOf(1 to "a")
        assertEquals("a", map.replace(1, "z"))
        assertEquals("z", map[1])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2AnyMapOf(1 to "a").replace(99, "z") }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableInt2AnyMapOf(1 to "a", 2 to "b")
        assertEquals("a", map.removeKey(1))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableInt2AnyMapOf(1 to "a").removeKey(99) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableInt2AnyMapOf(1 to "a", 2 to "b")
        assertTrue(map.remove(1, "a"))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableInt2AnyMapOf(1 to "a", 2 to "b")
        assertFalse(map.remove(1, "b"))
        assertEquals(2, map.size)
        assertEquals("a", map[1])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableInt2AnyMapOf(1 to "a")
        assertFalse(map.remove(99, "a"))
        assertEquals(1, map.size)
    }

    @Test
    fun remove_keyValue_nullValue_matchesStoredNull() {
        val map = mutableInt2AnyMapOf<String?>(1 to null)
        assertTrue(map.remove(1, null))
        assertFalse(map.containsKey(1))
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = int2AnyMapOf(1 to "a", 2 to "b")
        val dest = mutableInt2AnyMapOf(3 to "c")
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals("a", dest[1])
        assertEquals("b", dest[2])
        assertEquals("c", dest[3])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableInt2AnyMapOf(1 to "old", 6 to "f")
        val source = int2AnyMapOf(1 to "new", 2 to "b", 3 to "c", 4 to "d", 5 to "e", 7 to "g")
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals("new", dest[1])
        assertEquals("b", dest[2])
        assertEquals("c", dest[3])
        assertEquals("d", dest[4])
        assertEquals("e", dest[5])
        assertEquals("f", dest[6])
        assertEquals("g", dest[7])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableInt2AnyMapOf(3 to "c")
        dest.putAll(mapOf(1 to "a", 2 to "b"))
        assertEquals(3, dest.size)
        assertEquals("a", dest[1])
        assertEquals("b", dest[2])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2AnyHashMap<String>().apply { set(1, "a"); set(2, "b") }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals("a", map[1])
        assertEquals("b", map[2])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2AnyHashMap<String>(100).apply { set(1, "a"); set(2, "b") }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals("a", map[1])
        assertEquals("b", map[2])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Int2AnyHashMap<String>()
        for (i in 1..500) map[i] = "v$i"
        assertEquals(500, map.size)
        for (i in 1..500) assertEquals("v$i", map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertEquals("v$i", map.remove(i))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals("v$i", map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2..500 step 2) assertEquals("v$i", map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Int2AnyHashMap<String>()
        for (i in 1..500) map[i] = "v$i"

        // mismatched value must not remove the entry
        for (i in 1..500 step 2) assertFalse(map.remove(i, "wrong$i"))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1..500 step 2) assertTrue(map.remove(i, "v$i"))
        assertEquals(250, map.size)
        for (i in 1..500) {
            if (i % 2 == 0) assertEquals("v$i", map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Int2AnyHashMap<String>()
        for (i in 1..200) map[i] = "v$i"
        map.trimToSize()

        val visited = mutableListOf<Int>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals("v${entry.key}", entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1..200).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Int2AnyHashMap<String>()
        for (i in 1..50) map[i] = "v$i"
        map[0] = "v0"

        val fromIterator = mutableListOf<Pair<Int, String>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Int, String>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Int2AnyMap implementation,
        // not just the same concrete class.
        val map: Any = mutableInt2AnyMapOf(1 to "a", 2 to "b")
        assertEquals(int2AnyMapOf(1 to "a", 2 to "b"), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Int2AnyMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableInt2AnyMapOf(1 to "a").iterator().next()
        assertEquals(mutableInt2AnyMapOf(1 to "a").iterator().next(), entry)
    }
}

// ============================= Long2Byte =============================

class Long2ByteMapTest {
    @Test
    fun mapOf_vararg() {
        val map = long2ByteMapOf(1L to 10.toByte(), 2L to 20.toByte(), 3L to 30.toByte())
        assertEquals(3, map.size)
        assertEquals(10.toByte(), map[1L])
        assertEquals(20.toByte(), map[2L])
        assertEquals(30.toByte(), map[3L])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte(), 2L to 20.toByte())
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1L])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildLong2ByteMap {
            set(1L, 100.toByte())
            set(2L, 110.toByte())
        }
        assertEquals(2, map.size)
        assertEquals(100.toByte(), map[1L])
        assertEquals(110.toByte(), map[2L])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Byte.MIN_VALUE, long2ByteMapOf(1L to 10.toByte())[99L])
        assertEquals(Byte.MIN_VALUE, mutableLong2ByteMapOf(1L to 10.toByte())[99L])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(long2ByteMapOf(1L to 10.toByte()).containsValue(10.toByte()))
        assertFalse(long2ByteMapOf(1L to 10.toByte()).containsValue(99.toByte()))
        assertTrue(mutableLong2ByteMapOf(1L to 10.toByte()).containsValue(10.toByte()))
        assertFalse(mutableLong2ByteMapOf(1L to 10.toByte()).containsValue(99.toByte()))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableLong2ByteMapOf().containsKey(0L))

        val map = mutableLong2ByteMapOf(1L to 10.toByte())
        assertEquals(Byte.MIN_VALUE, map[0L])
        assertFalse(map.containsKey(0L))
        assertEquals(Byte.MIN_VALUE, map.remove(0L))
        assertEquals(1, map.size)

        map[0L] = 5.toByte()
        assertTrue(map.containsKey(0L))
        assertEquals(5.toByte(), map[0L])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2ByteMapOf().isDefaultValue(Byte.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableLong2ByteMapOf(1L to 42.toByte())
        assertFalse(map.isDefaultValue(42.toByte()))
        assertFalse(map.isDefaultValue(0.toByte()))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableLong2ByteMapOf(1L to Byte.MIN_VALUE)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is the default value")
        assertEquals(Byte.MIN_VALUE, map[1L])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10.toByte(), long2ByteMapOf(1L to 10.toByte()).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2ByteMapOf(1L to 10.toByte()).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2ByteMapOf(1L to 10.toByte()).getOrElse(99L) { invoked = true; (-1).toByte() }
        assertTrue(invoked)
        assertEquals((-1).toByte(), result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2ByteMapOf(1L to 10.toByte()).getOrElse(1L) { invoked = true; (-1).toByte() }
        assertFalse(invoked)
        assertEquals(10.toByte(), result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableLong2ByteMapOf(1L to Byte.MIN_VALUE)
        var invoked = false
        val result = map.getOrElse(1L) { invoked = true; (-1).toByte() }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertEquals(Byte.MIN_VALUE, result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals((-99).toByte(), long2ByteMapOf(1L to 10.toByte()).getOrDefault(99L, (-99).toByte()))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10.toByte(), long2ByteMapOf(1L to 10.toByte()).getOrDefault(1L, (-99).toByte()))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte())
        assertEquals(42.toByte(), map.merge(99L, 42.toByte()) { old, new -> (old + new).toByte() })
        assertEquals(42.toByte(), map[99L])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte())
        assertEquals(15.toByte(), map.merge(1L, 5.toByte()) { old, new -> (old + new).toByte() })
        assertEquals(15.toByte(), map[1L])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte())
        assertEquals(42.toByte(), map.getOrPut(99L) { 42.toByte() })
        assertEquals(42.toByte(), map[99L])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableLong2ByteMapOf(1L to 10.toByte())
        val result = map.getOrPut(1L) { invoked = true; 99.toByte() }
        assertFalse(invoked)
        assertEquals(10.toByte(), result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte())
        assertEquals(10.toByte(), map.replace(1L, 20.toByte()))
        assertEquals(20.toByte(), map[1L])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2ByteMapOf(1L to 10.toByte()).replace(99L, 20.toByte()) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte(), 2L to 20.toByte())
        assertEquals(10.toByte(), map.removeKey(1L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2ByteMapOf(1L to 10.toByte()).removeKey(99L) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte(), 2L to 20.toByte())
        assertTrue(map.remove(1L, 10.toByte()))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte(), 2L to 20.toByte())
        assertFalse(map.remove(1L, 20.toByte()))
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1L])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableLong2ByteMapOf(1L to 10.toByte())
        assertFalse(map.remove(99L, 10.toByte()))
        assertEquals(1, map.size)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = long2ByteMapOf(1L to 10.toByte(), 2L to 20.toByte())
        val dest = mutableLong2ByteMapOf(3L to 30.toByte())
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10.toByte(), dest[1L])
        assertEquals(20.toByte(), dest[2L])
        assertEquals(30.toByte(), dest[3L])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableLong2ByteMapOf(1L to 100.toByte(), 6L to 60.toByte())
        val source = long2ByteMapOf(
            1L to 99.toByte(), 2L to 2.toByte(), 3L to 3.toByte(), 4L to 4.toByte(), 5L to 5.toByte(),
            7L to 7.toByte(),
        )
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals(99.toByte(), dest[1L])
        assertEquals(2.toByte(), dest[2L])
        assertEquals(3.toByte(), dest[3L])
        assertEquals(4.toByte(), dest[4L])
        assertEquals(5.toByte(), dest[5L])
        assertEquals(60.toByte(), dest[6L])
        assertEquals(7.toByte(), dest[7L])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableLong2ByteMapOf(3L to 30.toByte())
        dest.putAll(mapOf(1L to 10.toByte(), 2L to 20.toByte()))
        assertEquals(3, dest.size)
        assertEquals(10.toByte(), dest[1L])
        assertEquals(20.toByte(), dest[2L])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Long2ByteHashMap().apply { set(1L, 10.toByte()); set(2L, 20.toByte()) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1L])
        assertEquals(20.toByte(), map[2L])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Long2ByteHashMap(100).apply { set(1L, 10.toByte()); set(2L, 20.toByte()) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10.toByte(), map[1L])
        assertEquals(20.toByte(), map[2L])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters; values are truncated to Byte
        // range via toByte(), consistently on both the write and the read-back assertion.
        val map = Long2ByteHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toByte()
        assertEquals(500, map.size)
        for (i in 1L..500L) assertEquals((i + 1000).toByte(), map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertEquals((i + 1000).toByte(), map.remove(i))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toByte(), map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2L..500L step 2) assertEquals((i + 1000).toByte(), map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Long2ByteHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toByte()

        // mismatched value must not remove the entry
        for (i in 1L..500L step 2) assertFalse(map.remove(i, i.toByte()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertTrue(map.remove(i, (i + 1000).toByte()))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toByte(), map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Long2ByteHashMap()
        for (i in 1L..200L) map[i] = (i + 1000).toByte()
        map.trimToSize()

        val visited = mutableListOf<Long>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals((entry.key + 1000).toByte(), entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1L..200L).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Long2ByteHashMap()
        for (i in 1L..50L) map[i] = (i + 1000).toByte()
        map[0L] = 99.toByte()

        val fromIterator = mutableListOf<Pair<Long, Byte>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Long, Byte>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Long2ByteMap implementation,
        // not just the same concrete class.
        val map: Any = mutableLong2ByteMapOf(1L to 1.toByte(), 2L to 2.toByte())
        assertEquals(long2ByteMapOf(1L to 1.toByte(), 2L to 2.toByte()), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Long2ByteMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableLong2ByteMapOf(1L to 1.toByte()).iterator().next()
        assertEquals(mutableLong2ByteMapOf(1L to 1.toByte()).iterator().next(), entry)
    }
}

// ============================= Long2Int =============================

class Long2IntMapTest {
    @Test
    fun mapOf_vararg() {
        val map = long2IntMapOf(1L to 10, 2L to 20, 3L to 30)
        assertEquals(3, map.size)
        assertEquals(10, map[1L])
        assertEquals(20, map[2L])
        assertEquals(30, map[3L])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableLong2IntMapOf(1L to 10, 2L to 20)
        assertEquals(2, map.size)
        assertEquals(10, map[1L])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildLong2IntMap {
            set(1L, 100)
            set(2L, 200)
        }
        assertEquals(2, map.size)
        assertEquals(100, map[1L])
        assertEquals(200, map[2L])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Int.MIN_VALUE, long2IntMapOf(1L to 10)[99L])
        assertEquals(Int.MIN_VALUE, mutableLong2IntMapOf(1L to 10)[99L])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(long2IntMapOf(1L to 10).containsValue(10))
        assertFalse(long2IntMapOf(1L to 10).containsValue(99))
        assertTrue(mutableLong2IntMapOf(1L to 10).containsValue(10))
        assertFalse(mutableLong2IntMapOf(1L to 10).containsValue(99))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableLong2IntMapOf().containsKey(0L))

        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(Int.MIN_VALUE, map[0L])
        assertFalse(map.containsKey(0L))
        assertEquals(Int.MIN_VALUE, map.remove(0L))
        assertEquals(1, map.size)

        map[0L] = 5
        assertTrue(map.containsKey(0L))
        assertEquals(5, map[0L])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2IntMapOf().isDefaultValue(Int.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableLong2IntMapOf(1L to 42)
        assertFalse(map.isDefaultValue(42))
        assertFalse(map.isDefaultValue(0))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableLong2IntMapOf(1L to Int.MIN_VALUE)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is the default value")
        assertEquals(Int.MIN_VALUE, map[1L])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10, long2IntMapOf(1L to 10).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2IntMapOf(1L to 10).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2IntMapOf(1L to 10).getOrElse(99L) { invoked = true; -1 }
        assertTrue(invoked)
        assertEquals(-1, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2IntMapOf(1L to 10).getOrElse(1L) { invoked = true; -1 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableLong2IntMapOf(1L to Int.MIN_VALUE)
        var invoked = false
        val result = map.getOrElse(1L) { invoked = true; -1 }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertEquals(Int.MIN_VALUE, result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99, long2IntMapOf(1L to 10).getOrDefault(99L, -99))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10, long2IntMapOf(1L to 10).getOrDefault(1L, -99))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(42, map.merge(99L, 42) { old, new -> old + new })
        assertEquals(42, map[99L])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(15, map.merge(1L, 5) { old, new -> old + new })
        assertEquals(15, map[1L])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(42, map.getOrPut(99L) { 42 })
        assertEquals(42, map[99L])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableLong2IntMapOf(1L to 10)
        val result = map.getOrPut(1L) { invoked = true; 99 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(10, map.replace(1L, 20))
        assertEquals(20, map[1L])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2IntMapOf(1L to 10).replace(99L, 20) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableLong2IntMapOf(1L to 10, 2L to 20)
        assertEquals(10, map.removeKey(1L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2IntMapOf(1L to 10).removeKey(99L) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableLong2IntMapOf(1L to 10, 2L to 20)
        assertTrue(map.remove(1L, 10))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableLong2IntMapOf(1L to 10, 2L to 20)
        assertFalse(map.remove(1L, 20))
        assertEquals(2, map.size)
        assertEquals(10, map[1L])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertFalse(map.remove(99L, 10))
        assertEquals(1, map.size)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = long2IntMapOf(1L to 10, 2L to 20)
        val dest = mutableLong2IntMapOf(3L to 30)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10, dest[1L])
        assertEquals(20, dest[2L])
        assertEquals(30, dest[3L])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableLong2IntMapOf(1L to 100, 6L to 600)
        val source = long2IntMapOf(1L to 999, 2L to 2, 3L to 3, 4L to 4, 5L to 5, 7L to 7)
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals(999, dest[1L])
        assertEquals(2, dest[2L])
        assertEquals(3, dest[3L])
        assertEquals(4, dest[4L])
        assertEquals(5, dest[5L])
        assertEquals(600, dest[6L])
        assertEquals(7, dest[7L])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableLong2IntMapOf(3L to 30)
        dest.putAll(mapOf(1L to 10, 2L to 20))
        assertEquals(3, dest.size)
        assertEquals(10, dest[1L])
        assertEquals(20, dest[2L])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Long2IntHashMap().apply { set(1L, 10); set(2L, 20) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10, map[1L])
        assertEquals(20, map[2L])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Long2IntHashMap(100).apply { set(1L, 10); set(2L, 20) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10, map[1L])
        assertEquals(20, map[2L])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Long2IntHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toInt()
        assertEquals(500, map.size)
        for (i in 1L..500L) assertEquals((i + 1000).toInt(), map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertEquals((i + 1000).toInt(), map.remove(i))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toInt(), map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2L..500L step 2) assertEquals((i + 1000).toInt(), map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Long2IntHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toInt()

        // mismatched value must not remove the entry
        for (i in 1L..500L step 2) assertFalse(map.remove(i, i.toInt()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertTrue(map.remove(i, (i + 1000).toInt()))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toInt(), map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Long2IntHashMap()
        for (i in 1L..200L) map[i] = (i + 1000).toInt()
        map.trimToSize()

        val visited = mutableListOf<Long>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals((entry.key + 1000).toInt(), entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1L..200L).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Long2IntHashMap()
        for (i in 1L..50L) map[i] = (i + 1000).toInt()
        map[0L] = 9999

        val fromIterator = mutableListOf<Pair<Long, Int>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Long, Int>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Long2IntMap implementation,
        // not just the same concrete class.
        val map: Any = mutableLong2IntMapOf(1L to 1, 2L to 2)
        assertEquals(long2IntMapOf(1L to 1, 2L to 2), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Long2IntMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableLong2IntMapOf(1L to 1).iterator().next()
        assertEquals(mutableLong2IntMapOf(1L to 1).iterator().next(), entry)
    }
}

// ============================= Long2Long =============================

class Long2LongMapTest {
    @Test
    fun mapOf_vararg() {
        val map = long2LongMapOf(1L to 10L, 2L to 20L, 3L to 30L)
        assertEquals(3, map.size)
        assertEquals(10L, map[1L])
        assertEquals(20L, map[2L])
        assertEquals(30L, map[3L])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableLong2LongMapOf(1L to 10L, 2L to 20L)
        assertEquals(2, map.size)
        assertEquals(10L, map[1L])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildLong2LongMap {
            set(1L, 100L)
            set(2L, 200L)
        }
        assertEquals(2, map.size)
        assertEquals(100L, map[1L])
        assertEquals(200L, map[2L])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Long.MIN_VALUE, long2LongMapOf(1L to 10L)[99L])
        assertEquals(Long.MIN_VALUE, mutableLong2LongMapOf(1L to 10L)[99L])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(long2LongMapOf(1L to 10L).containsValue(10L))
        assertFalse(long2LongMapOf(1L to 10L).containsValue(99L))
        assertTrue(mutableLong2LongMapOf(1L to 10L).containsValue(10L))
        assertFalse(mutableLong2LongMapOf(1L to 10L).containsValue(99L))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableLong2LongMapOf().containsKey(0L))

        val map = mutableLong2LongMapOf(1L to 10L)
        assertEquals(Long.MIN_VALUE, map[0L])
        assertFalse(map.containsKey(0L))
        assertEquals(Long.MIN_VALUE, map.remove(0L))
        assertEquals(1, map.size)

        map[0L] = 5L
        assertTrue(map.containsKey(0L))
        assertEquals(5L, map[0L])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2LongMapOf().isDefaultValue(Long.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableLong2LongMapOf(1L to 42L)
        assertFalse(map.isDefaultValue(42L))
        assertFalse(map.isDefaultValue(0L))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableLong2LongMapOf(1L to Long.MIN_VALUE)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is the default value")
        assertEquals(Long.MIN_VALUE, map[1L])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10L, long2LongMapOf(1L to 10L).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2LongMapOf(1L to 10L).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2LongMapOf(1L to 10L).getOrElse(99L) { invoked = true; -1L }
        assertTrue(invoked)
        assertEquals(-1L, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2LongMapOf(1L to 10L).getOrElse(1L) { invoked = true; -1L }
        assertFalse(invoked)
        assertEquals(10L, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableLong2LongMapOf(1L to Long.MIN_VALUE)
        var invoked = false
        val result = map.getOrElse(1L) { invoked = true; -1L }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertEquals(Long.MIN_VALUE, result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99L, long2LongMapOf(1L to 10L).getOrDefault(99L, -99L))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10L, long2LongMapOf(1L to 10L).getOrDefault(1L, -99L))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableLong2LongMapOf(1L to 10L)
        assertEquals(42L, map.merge(99L, 42L) { old, new -> old + new })
        assertEquals(42L, map[99L])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableLong2LongMapOf(1L to 10L)
        assertEquals(15L, map.merge(1L, 5L) { old, new -> old + new })
        assertEquals(15L, map[1L])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableLong2LongMapOf(1L to 10L)
        assertEquals(42L, map.getOrPut(99L) { 42L })
        assertEquals(42L, map[99L])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableLong2LongMapOf(1L to 10L)
        val result = map.getOrPut(1L) { invoked = true; 99L }
        assertFalse(invoked)
        assertEquals(10L, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableLong2LongMapOf(1L to 10L)
        assertEquals(10L, map.replace(1L, 20L))
        assertEquals(20L, map[1L])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2LongMapOf(1L to 10L).replace(99L, 20L) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableLong2LongMapOf(1L to 10L, 2L to 20L)
        assertEquals(10L, map.removeKey(1L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2LongMapOf(1L to 10L).removeKey(99L) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableLong2LongMapOf(1L to 10L, 2L to 20L)
        assertTrue(map.remove(1L, 10L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableLong2LongMapOf(1L to 10L, 2L to 20L)
        assertFalse(map.remove(1L, 20L))
        assertEquals(2, map.size)
        assertEquals(10L, map[1L])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableLong2LongMapOf(1L to 10L)
        assertFalse(map.remove(99L, 10L))
        assertEquals(1, map.size)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = long2LongMapOf(1L to 10L, 2L to 20L)
        val dest = mutableLong2LongMapOf(3L to 30L)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10L, dest[1L])
        assertEquals(20L, dest[2L])
        assertEquals(30L, dest[3L])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableLong2LongMapOf(1L to 100L, 6L to 600L)
        val source = long2LongMapOf(1L to 999L, 2L to 2L, 3L to 3L, 4L to 4L, 5L to 5L, 7L to 7L)
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals(999L, dest[1L])
        assertEquals(2L, dest[2L])
        assertEquals(3L, dest[3L])
        assertEquals(4L, dest[4L])
        assertEquals(5L, dest[5L])
        assertEquals(600L, dest[6L])
        assertEquals(7L, dest[7L])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableLong2LongMapOf(3L to 30L)
        dest.putAll(mapOf(1L to 10L, 2L to 20L))
        assertEquals(3, dest.size)
        assertEquals(10L, dest[1L])
        assertEquals(20L, dest[2L])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Long2LongHashMap().apply { set(1L, 10L); set(2L, 20L) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10L, map[1L])
        assertEquals(20L, map[2L])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Long2LongHashMap(100).apply { set(1L, 10L); set(2L, 20L) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10L, map[1L])
        assertEquals(20L, map[2L])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Long2LongHashMap()
        for (i in 1L..500L) map[i] = i + 1000
        assertEquals(500, map.size)
        for (i in 1L..500L) assertEquals(i + 1000, map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertEquals(i + 1000, map.remove(i))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals(i + 1000, map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2L..500L step 2) assertEquals(i + 1000, map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Long2LongHashMap()
        for (i in 1L..500L) map[i] = i + 1000

        // mismatched value must not remove the entry
        for (i in 1L..500L step 2) assertFalse(map.remove(i, i))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertTrue(map.remove(i, i + 1000))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals(i + 1000, map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Long2LongHashMap()
        for (i in 1L..200L) map[i] = i + 1000
        map.trimToSize()

        val visited = mutableListOf<Long>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals(entry.key + 1000, entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1L..200L).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Long2LongHashMap()
        for (i in 1L..50L) map[i] = i + 1000L
        map[0L] = 9999L

        val fromIterator = mutableListOf<Pair<Long, Long>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Long, Long>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Long2LongMap implementation,
        // not just the same concrete class.
        val map: Any = mutableLong2LongMapOf(1L to 1L, 2L to 2L)
        assertEquals(long2LongMapOf(1L to 1L, 2L to 2L), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Long2LongMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableLong2LongMapOf(1L to 1L).iterator().next()
        assertEquals(mutableLong2LongMapOf(1L to 1L).iterator().next(), entry)
    }
}

// ============================= Long2Float =============================

class Long2FloatMapTest {
    @Test
    fun mapOf_vararg() {
        val map = long2FloatMapOf(1L to 10f, 2L to 20f, 3L to 30f)
        assertEquals(3, map.size)
        assertEquals(10f, map[1L])
        assertEquals(20f, map[2L])
        assertEquals(30f, map[3L])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableLong2FloatMapOf(1L to 10f, 2L to 20f)
        assertEquals(2, map.size)
        assertEquals(10f, map[1L])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildLong2FloatMap {
            set(1L, 100f)
            set(2L, 200f)
        }
        assertEquals(2, map.size)
        assertEquals(100f, map[1L])
        assertEquals(200f, map[2L])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(long2FloatMapOf(1L to 10f)[99L].isNaN())
        assertTrue(mutableLong2FloatMapOf(1L to 10f)[99L].isNaN())
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(long2FloatMapOf(1L to 10f).containsValue(10f))
        assertFalse(long2FloatMapOf(1L to 10f).containsValue(99f))
        assertTrue(mutableLong2FloatMapOf(1L to 10f).containsValue(10f))
        assertFalse(mutableLong2FloatMapOf(1L to 10f).containsValue(99f))
    }

    @Test
    fun containsValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN values must be found by value queries, and -0.0f must not match 0.0f
        assertTrue(long2FloatMapOf(1L to Float.NaN).containsValue(Float.NaN))
        assertTrue(mutableLong2FloatMapOf(1L to Float.NaN).containsValue(Float.NaN))
        assertFalse(long2FloatMapOf(1L to -0.0f).containsValue(0.0f))
        assertFalse(mutableLong2FloatMapOf(1L to -0.0f).containsValue(0.0f))

        val view = mutableLong2FloatMapOf(1L to Float.NaN).asMap()
        assertTrue(view.entries.contains(mapOf(1L to Float.NaN).entries.first()))

        // updating an entry whose stored value is NaN must not report concurrent modification
        val mutableView = mutableLong2FloatMapOf(1L to Float.NaN).asMutableMap()
        val entry = mutableView.entries.iterator().next()
        assertTrue(entry.setValue(5f).isNaN())
        assertEquals(5f, mutableView[1L])
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableLong2FloatMapOf().containsKey(0L))

        val map = mutableLong2FloatMapOf(1L to 10f)
        assertTrue(map[0L].isNaN())
        assertFalse(map.containsKey(0L))
        assertTrue(map.remove(0L).isNaN())
        assertEquals(1, map.size)

        map[0L] = 5f
        assertTrue(map.containsKey(0L))
        assertEquals(5f, map[0L])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2FloatMapOf().isDefaultValue(Float.NaN))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableLong2FloatMapOf(1L to 42f)
        assertFalse(map.isDefaultValue(42f))
        assertFalse(map.isDefaultValue(0f))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableLong2FloatMapOf(1L to Float.NaN)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is the default value")
        assertTrue(map[1L].isNaN())
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10f, long2FloatMapOf(1L to 10f).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2FloatMapOf(1L to 10f).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2FloatMapOf(1L to 10f).getOrElse(99L) { invoked = true; -1f }
        assertTrue(invoked)
        assertEquals(-1f, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2FloatMapOf(1L to 10f).getOrElse(1L) { invoked = true; -1f }
        assertFalse(invoked)
        assertEquals(10f, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableLong2FloatMapOf(1L to Float.NaN)
        var invoked = false
        val result = map.getOrElse(1L) { invoked = true; -1f }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertTrue(result.isNaN())
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99f, long2FloatMapOf(1L to 10f).getOrDefault(99L, -99f))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10f, long2FloatMapOf(1L to 10f).getOrDefault(1L, -99f))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableLong2FloatMapOf(1L to 10f)
        assertEquals(42f, map.merge(99L, 42f) { old, new -> old + new })
        assertEquals(42f, map[99L])

        // a NaN value must be inserted for an absent key even though NaN is the map default
        assertTrue(map.merge(98L, Float.NaN) { _, new -> new }.isNaN())
        assertTrue(map.containsKey(98L))
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableLong2FloatMapOf(1L to 10f)
        assertEquals(15f, map.merge(1L, 5f) { old, new -> old + new })
        assertEquals(15f, map[1L])

        // merging -0.0f to 0.0f must store the updated value even though -0.0f == 0.0f
        map[2L] = -0.0f
        map.merge(2L, 0.0f) { _, new -> new }
        assertEquals(0.0f.toBits(), map[2L].toBits())
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableLong2FloatMapOf(1L to 10f)
        assertEquals(42f, map.getOrPut(99L) { 42f })
        assertEquals(42f, map[99L])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableLong2FloatMapOf(1L to 10f)
        val result = map.getOrPut(1L) { invoked = true; 99f }
        assertFalse(invoked)
        assertEquals(10f, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableLong2FloatMapOf(1L to 10f)
        assertEquals(10f, map.replace(1L, 20f))
        assertEquals(20f, map[1L])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2FloatMapOf(1L to 10f).replace(99L, 20f) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableLong2FloatMapOf(1L to 10f, 2L to 20f)
        assertEquals(10f, map.removeKey(1L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2FloatMapOf(1L to 10f).removeKey(99L) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableLong2FloatMapOf(1L to 10f, 2L to 20f)
        assertTrue(map.remove(1L, 10f))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableLong2FloatMapOf(1L to 10f, 2L to 20f)
        assertFalse(map.remove(1L, 20f))
        assertEquals(2, map.size)
        assertEquals(10f, map[1L])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableLong2FloatMapOf(1L to 10f)
        assertFalse(map.remove(99L, 10f))
        assertEquals(1, map.size)
    }

    @Test
    fun remove_keyValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must match NaN for removal, matching boxed semantics
        val nanMap = mutableLong2FloatMapOf(1L to Float.NaN)
        assertTrue(nanMap.remove(1L, Float.NaN))
        assertFalse(nanMap.containsKey(1L))

        // -0.0f must not match 0.0f for removal
        val negZeroMap = mutableLong2FloatMapOf(1L to -0.0f)
        assertFalse(negZeroMap.remove(1L, 0.0f))
        assertTrue(negZeroMap.containsKey(1L))
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = long2FloatMapOf(1L to 10f, 2L to 20f)
        val dest = mutableLong2FloatMapOf(3L to 30f)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10f, dest[1L])
        assertEquals(20f, dest[2L])
        assertEquals(30f, dest[3L])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableLong2FloatMapOf(3L to 30f)
        dest.putAll(mapOf(1L to 10f, 2L to 20f))
        assertEquals(3, dest.size)
        assertEquals(10f, dest[1L])
        assertEquals(20f, dest[2L])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Long2FloatHashMap().apply { set(1L, 10f); set(2L, 20f) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10f, map[1L])
        assertEquals(20f, map[2L])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Long2FloatHashMap(100).apply { set(1L, 10f); set(2L, 20f) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10f, map[1L])
        assertEquals(20f, map[2L])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Long2FloatHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toFloat()
        assertEquals(500, map.size)
        for (i in 1L..500L) assertEquals((i + 1000).toFloat(), map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertEquals((i + 1000).toFloat(), map.remove(i))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toFloat(), map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2L..500L step 2) assertEquals((i + 1000).toFloat(), map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Long2FloatHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toFloat()

        // mismatched value must not remove the entry
        for (i in 1L..500L step 2) assertFalse(map.remove(i, i.toFloat()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertTrue(map.remove(i, (i + 1000).toFloat()))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toFloat(), map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Long2FloatHashMap()
        for (i in 1L..200L) map[i] = (i + 1000).toFloat()
        map.trimToSize()

        val visited = mutableListOf<Long>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals((entry.key + 1000).toFloat(), entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1L..200L).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Long2FloatHashMap()
        for (i in 1L..50L) map[i] = (i + 1000).toFloat()
        map[0L] = 9999f

        val fromIterator = mutableListOf<Pair<Long, Int>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value.toBits())
        }

        val fromForeach = mutableListOf<Pair<Long, Int>>()
        map.foreach { k, v -> fromForeach.add(k to v.toBits()) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Long2FloatMap
        // implementation, not just the same concrete class.
        val map: Any = mutableLong2FloatMapOf(1L to 1f, 2L to 2f)
        assertEquals(long2FloatMapOf(1L to 1f, 2L to 2f), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see Long2FloatHashMap.Entry's equals()) requires equality against ANY
        // Long2FloatMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableLong2FloatMapOf(1L to 1f).iterator().next()
        assertEquals(mutableLong2FloatMapOf(1L to 1f).iterator().next(), entry)
    }
}

// ============================= Long2Double =============================

class Long2DoubleMapTest {
    @Test
    fun mapOf_vararg() {
        val map = long2DoubleMapOf(1L to 10.0, 2L to 20.0, 3L to 30.0)
        assertEquals(3, map.size)
        assertEquals(10.0, map[1L])
        assertEquals(20.0, map[2L])
        assertEquals(30.0, map[3L])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableLong2DoubleMapOf(1L to 10.0, 2L to 20.0)
        assertEquals(2, map.size)
        assertEquals(10.0, map[1L])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildLong2DoubleMap {
            set(1L, 100.0)
            set(2L, 200.0)
        }
        assertEquals(2, map.size)
        assertEquals(100.0, map[1L])
        assertEquals(200.0, map[2L])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(long2DoubleMapOf(1L to 10.0)[99L].isNaN())
        assertTrue(mutableLong2DoubleMapOf(1L to 10.0)[99L].isNaN())
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(long2DoubleMapOf(1L to 10.0).containsValue(10.0))
        assertFalse(long2DoubleMapOf(1L to 10.0).containsValue(99.0))
        assertTrue(mutableLong2DoubleMapOf(1L to 10.0).containsValue(10.0))
        assertFalse(mutableLong2DoubleMapOf(1L to 10.0).containsValue(99.0))
    }

    @Test
    fun containsValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN values must be found by value queries, and -0.0 must not match 0.0
        assertTrue(long2DoubleMapOf(1L to Double.NaN).containsValue(Double.NaN))
        assertTrue(mutableLong2DoubleMapOf(1L to Double.NaN).containsValue(Double.NaN))
        assertFalse(long2DoubleMapOf(1L to -0.0).containsValue(0.0))
        assertFalse(mutableLong2DoubleMapOf(1L to -0.0).containsValue(0.0))

        val view = mutableLong2DoubleMapOf(1L to Double.NaN).asMap()
        assertTrue(view.entries.contains(mapOf(1L to Double.NaN).entries.first()))

        // updating an entry whose stored value is NaN must not report concurrent modification
        val mutableView = mutableLong2DoubleMapOf(1L to Double.NaN).asMutableMap()
        val entry = mutableView.entries.iterator().next()
        assertTrue(entry.setValue(5.0).isNaN())
        assertEquals(5.0, mutableView[1L])
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableLong2DoubleMapOf().containsKey(0L))

        val map = mutableLong2DoubleMapOf(1L to 10.0)
        assertTrue(map[0L].isNaN())
        assertFalse(map.containsKey(0L))
        assertTrue(map.remove(0L).isNaN())
        assertEquals(1, map.size)

        map[0L] = 5.0
        assertTrue(map.containsKey(0L))
        assertEquals(5.0, map[0L])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2DoubleMapOf().isDefaultValue(Double.NaN))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableLong2DoubleMapOf(1L to 42.0)
        assertFalse(map.isDefaultValue(42.0))
        assertFalse(map.isDefaultValue(0.0))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableLong2DoubleMapOf(1L to Double.NaN)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is the default value")
        assertTrue(map[1L].isNaN())
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10.0, long2DoubleMapOf(1L to 10.0).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2DoubleMapOf(1L to 10.0).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2DoubleMapOf(1L to 10.0).getOrElse(99L) { invoked = true; -1.0 }
        assertTrue(invoked)
        assertEquals(-1.0, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2DoubleMapOf(1L to 10.0).getOrElse(1L) { invoked = true; -1.0 }
        assertFalse(invoked)
        assertEquals(10.0, result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableLong2DoubleMapOf(1L to Double.NaN)
        var invoked = false
        val result = map.getOrElse(1L) { invoked = true; -1.0 }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertTrue(result.isNaN())
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99.0, long2DoubleMapOf(1L to 10.0).getOrDefault(99L, -99.0))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10.0, long2DoubleMapOf(1L to 10.0).getOrDefault(1L, -99.0))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableLong2DoubleMapOf(1L to 10.0)
        assertEquals(42.0, map.merge(99L, 42.0) { old, new -> old + new })
        assertEquals(42.0, map[99L])

        // a NaN value must be inserted for an absent key even though NaN is the map default
        assertTrue(map.merge(98L, Double.NaN) { _, new -> new }.isNaN())
        assertTrue(map.containsKey(98L))
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableLong2DoubleMapOf(1L to 10.0)
        assertEquals(15.0, map.merge(1L, 5.0) { old, new -> old + new })
        assertEquals(15.0, map[1L])

        // merging -0.0 to 0.0 must store the updated value even though -0.0 == 0.0
        map[2L] = -0.0
        map.merge(2L, 0.0) { _, new -> new }
        assertEquals(0.0.toBits(), map[2L].toBits())
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableLong2DoubleMapOf(1L to 10.0)
        assertEquals(42.0, map.getOrPut(99L) { 42.0 })
        assertEquals(42.0, map[99L])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableLong2DoubleMapOf(1L to 10.0)
        val result = map.getOrPut(1L) { invoked = true; 99.0 }
        assertFalse(invoked)
        assertEquals(10.0, result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableLong2DoubleMapOf(1L to 10.0)
        assertEquals(10.0, map.replace(1L, 20.0))
        assertEquals(20.0, map[1L])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2DoubleMapOf(1L to 10.0).replace(99L, 20.0) }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableLong2DoubleMapOf(1L to 10.0, 2L to 20.0)
        assertEquals(10.0, map.removeKey(1L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2DoubleMapOf(1L to 10.0).removeKey(99L) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableLong2DoubleMapOf(1L to 10.0, 2L to 20.0)
        assertTrue(map.remove(1L, 10.0))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableLong2DoubleMapOf(1L to 10.0, 2L to 20.0)
        assertFalse(map.remove(1L, 20.0))
        assertEquals(2, map.size)
        assertEquals(10.0, map[1L])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableLong2DoubleMapOf(1L to 10.0)
        assertFalse(map.remove(99L, 10.0))
        assertEquals(1, map.size)
    }

    @Test
    fun remove_keyValue_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must match NaN for removal, matching boxed semantics
        val nanMap = mutableLong2DoubleMapOf(1L to Double.NaN)
        assertTrue(nanMap.remove(1L, Double.NaN))
        assertFalse(nanMap.containsKey(1L))

        // -0.0 must not match 0.0 for removal
        val negZeroMap = mutableLong2DoubleMapOf(1L to -0.0)
        assertFalse(negZeroMap.remove(1L, 0.0))
        assertTrue(negZeroMap.containsKey(1L))
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = long2DoubleMapOf(1L to 10.0, 2L to 20.0)
        val dest = mutableLong2DoubleMapOf(3L to 30.0)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10.0, dest[1L])
        assertEquals(20.0, dest[2L])
        assertEquals(30.0, dest[3L])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableLong2DoubleMapOf(3L to 30.0)
        dest.putAll(mapOf(1L to 10.0, 2L to 20.0))
        assertEquals(3, dest.size)
        assertEquals(10.0, dest[1L])
        assertEquals(20.0, dest[2L])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Long2DoubleHashMap().apply { set(1L, 10.0); set(2L, 20.0) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10.0, map[1L])
        assertEquals(20.0, map[2L])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Long2DoubleHashMap(100).apply { set(1L, 10.0); set(2L, 20.0) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10.0, map[1L])
        assertEquals(20.0, map[2L])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Long2DoubleHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toDouble()
        assertEquals(500, map.size)
        for (i in 1L..500L) assertEquals((i + 1000).toDouble(), map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertEquals((i + 1000).toDouble(), map.remove(i))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toDouble(), map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2L..500L step 2) assertEquals((i + 1000).toDouble(), map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Long2DoubleHashMap()
        for (i in 1L..500L) map[i] = (i + 1000).toDouble()

        // mismatched value must not remove the entry
        for (i in 1L..500L step 2) assertFalse(map.remove(i, i.toDouble()))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertTrue(map.remove(i, (i + 1000).toDouble()))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals((i + 1000).toDouble(), map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Long2DoubleHashMap()
        for (i in 1L..200L) map[i] = (i + 1000).toDouble()
        map.trimToSize()

        val visited = mutableListOf<Long>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals((entry.key + 1000).toDouble(), entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1L..200L).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Long2DoubleHashMap()
        for (i in 1L..50L) map[i] = (i + 1000).toDouble()
        map[0L] = 9999.0

        val fromIterator = mutableListOf<Pair<Long, Long>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value.toBits())
        }

        val fromForeach = mutableListOf<Pair<Long, Long>>()
        map.foreach { k, v -> fromForeach.add(k to v.toBits()) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Long2DoubleMap
        // implementation, not just the same concrete class.
        val map: Any = mutableLong2DoubleMapOf(1L to 1.0, 2L to 2.0)
        assertEquals(long2DoubleMapOf(1L to 1.0, 2L to 2.0), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see Long2DoubleHashMap.Entry's equals()) requires equality against ANY
        // Long2DoubleMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableLong2DoubleMapOf(1L to 1.0).iterator().next()
        assertEquals(mutableLong2DoubleMapOf(1L to 1.0).iterator().next(), entry)
    }
}

// ============================= Long2Any =============================

class Long2AnyMapTest {
    @Test
    fun mapOf_vararg() {
        val map = long2AnyMapOf(1L to "a", 2L to "b", 3L to "c")
        assertEquals(3, map.size)
        assertEquals("a", map[1L])
        assertEquals("b", map[2L])
        assertEquals("c", map[3L])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableLong2AnyMapOf(1L to "a", 2L to "b")
        assertEquals(2, map.size)
        assertEquals("a", map[1L])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildLong2AnyMap<String> {
            set(1L, "a")
            set(2L, "b")
        }
        assertEquals(2, map.size)
        assertEquals("a", map[1L])
        assertEquals("b", map[2L])
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertNull(long2AnyMapOf(1L to "a")[99L])
        assertNull(mutableLong2AnyMapOf(1L to "a")[99L])
    }

    @Test
    fun containsValue_presentAndAbsent() {
        assertTrue(long2AnyMapOf(1L to "a").containsValue("a"))
        assertFalse(long2AnyMapOf(1L to "a").containsValue("z"))
        assertTrue(mutableLong2AnyMapOf(1L to "a").containsValue("a"))
        assertFalse(mutableLong2AnyMapOf(1L to "a").containsValue("z"))
    }

    @Test
    fun absentZeroKey_behavesLikeAnyAbsentKey() {
        // 0 is the hash map's initial empty-slot marker, but must still behave as an ordinary key
        assertFalse(mutableLong2AnyMapOf<String>().containsKey(0L))

        val map = mutableLong2AnyMapOf(1L to "a")
        assertNull(map[0L])
        assertFalse(map.containsKey(0L))
        assertNull(map.remove(0L))
        assertEquals(1, map.size)

        map[0L] = "z"
        assertTrue(map.containsKey(0L))
        assertEquals("z", map[0L])
        assertEquals(2, map.size)
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2AnyMapOf<String>().isDefaultValue(null))
    }

    @Test
    fun isDefaultValue_false_forNonDefaultValues() {
        val map = mutableLong2AnyMapOf(1L to "a")
        assertFalse(map.isDefaultValue("a"))
        assertFalse(map.isDefaultValue(""))
    }

    @Test
    fun storedDefaultValue_keyStillReportedPresent() {
        val map = mutableLong2AnyMapOf<String?>(1L to null)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is the default value")
        assertNull(map[1L])
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals("a", long2AnyMapOf(1L to "a").getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2AnyMapOf(1L to "a").getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2AnyMapOf(1L to "a").getOrElse(99L) { invoked = true; "fallback" }
        assertTrue(invoked)
        assertEquals("fallback", result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2AnyMapOf(1L to "a").getOrElse(1L) { invoked = true; "fallback" }
        assertFalse(invoked)
        assertEquals("a", result)
    }

    @Test
    fun getOrElse_storedDefaultValue_doesNotInvokeLambda() {
        val map = mutableLong2AnyMapOf<String?>(1L to null)
        var invoked = false
        val result = map.getOrElse(1L) { invoked = true; "fallback" }
        assertFalse(invoked, "lambda must not be invoked when the default value is stored under the key")
        assertNull(result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals("fallback", long2AnyMapOf(1L to "a").getOrDefault(99L, "fallback"))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals("a", long2AnyMapOf(1L to "a").getOrDefault(1L, "fallback"))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableLong2AnyMapOf(1L to "a")
        assertEquals("x", map.merge(99L, "x") { old, new -> old + new })
        assertEquals("x", map[99L])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableLong2AnyMapOf(1L to "a")
        assertEquals("ab", map.merge(1L, "b") { old, new -> old + new })
        assertEquals("ab", map[1L])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableLong2AnyMapOf(1L to "a")
        assertEquals("x", map.getOrPut(99L) { "x" })
        assertEquals("x", map[99L])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableLong2AnyMapOf(1L to "a")
        val result = map.getOrPut(1L) { invoked = true; "x" }
        assertFalse(invoked)
        assertEquals("a", result)
    }

    @Test
    fun replace_presentKey_updatesAndReturnsOldValue() {
        val map = mutableLong2AnyMapOf(1L to "a")
        assertEquals("a", map.replace(1L, "z"))
        assertEquals("z", map[1L])
        assertEquals(1, map.size)
    }

    @Test
    fun replace_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2AnyMapOf(1L to "a").replace(99L, "z") }
    }

    @Test
    fun removeKey_presentKey_removesAndReturnsValue() {
        val map = mutableLong2AnyMapOf(1L to "a", 2L to "b")
        assertEquals("a", map.removeKey(1L))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun removeKey_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { mutableLong2AnyMapOf(1L to "a").removeKey(99L) }
    }

    @Test
    fun remove_presentKeyMatchingValue_removesAndReturnsTrue() {
        val map = mutableLong2AnyMapOf(1L to "a", 2L to "b")
        assertTrue(map.remove(1L, "a"))
        assertEquals(1, map.size)
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun remove_presentKeyMismatchedValue_returnsFalseAndKeepsEntry() {
        val map = mutableLong2AnyMapOf(1L to "a", 2L to "b")
        assertFalse(map.remove(1L, "b"))
        assertEquals(2, map.size)
        assertEquals("a", map[1L])
    }

    @Test
    fun remove_absentKey_returnsFalse() {
        val map = mutableLong2AnyMapOf(1L to "a")
        assertFalse(map.remove(99L, "a"))
        assertEquals(1, map.size)
    }

    @Test
    fun remove_keyValue_nullValue_matchesStoredNull() {
        val map = mutableLong2AnyMapOf<String?>(1L to null)
        assertTrue(map.remove(1L, null))
        assertFalse(map.containsKey(1L))
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = long2AnyMapOf(1L to "a", 2L to "b")
        val dest = mutableLong2AnyMapOf(3L to "c")
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals("a", dest[1L])
        assertEquals("b", dest[2L])
        assertEquals("c", dest[3L])
    }

    @Test
    fun putAll_fromLargerOverlappingMap_prefersFromValues() {
        // when `from` is more than double this map's size, putAll takes a "reset to from" fast path;
        // for keys present in both maps, from's value must win, while keys unique to this map are preserved.
        val dest = mutableLong2AnyMapOf(1L to "old", 6L to "f")
        val source = long2AnyMapOf(1L to "new", 2L to "b", 3L to "c", 4L to "d", 5L to "e", 7L to "g")
        dest.putAll(source)
        assertEquals(7, dest.size)
        assertEquals("new", dest[1L])
        assertEquals("b", dest[2L])
        assertEquals("c", dest[3L])
        assertEquals("d", dest[4L])
        assertEquals("e", dest[5L])
        assertEquals("f", dest[6L])
        assertEquals("g", dest[7L])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableLong2AnyMapOf(3L to "c")
        dest.putAll(mapOf(1L to "a", 2L to "b"))
        assertEquals(3, dest.size)
        assertEquals("a", dest[1L])
        assertEquals("b", dest[2L])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Long2AnyHashMap<String>().apply { set(1L, "a"); set(2L, "b") }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals("a", map[1L])
        assertEquals("b", map[2L])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Long2AnyHashMap<String>(100).apply { set(1L, "a"); set(2L, "b") }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals("a", map[1L])
        assertEquals("b", map[2L])
    }

    @Test
    fun manyEntries_survivePutRemoveAndRehash() {
        // enough entries to force several rehashes and dense collision clusters
        val map = Long2AnyHashMap<String>()
        for (i in 1L..500L) map[i] = "v$i"
        assertEquals(500, map.size)
        for (i in 1L..500L) assertEquals("v$i", map[i])

        // removing entries exercises the backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertEquals("v$i", map.remove(i))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals("v$i", map[i]) else assertFalse(map.containsKey(i))
        }

        map.trimToSize()
        for (i in 2L..500L step 2) assertEquals("v$i", map[i])
    }

    @Test
    fun remove_keyValue_manyEntriesWithMismatchesSurviveRehash() {
        val map = Long2AnyHashMap<String>()
        for (i in 1L..500L) map[i] = "v$i"

        // mismatched value must not remove the entry
        for (i in 1L..500L step 2) assertFalse(map.remove(i, "wrong$i"))
        assertEquals(500, map.size)

        // matching value removes it, exercising backward-shift chains through collision clusters
        for (i in 1L..500L step 2) assertTrue(map.remove(i, "v$i"))
        assertEquals(250, map.size)
        for (i in 1L..500L) {
            if (i % 2 == 0L) assertEquals("v$i", map[i]) else assertFalse(map.containsKey(i))
        }
    }

    @Test
    fun iteratorRemove_removesAllEntries() {
        // a trimmed, densely loaded table makes the iterator's removal adjustment paths likely
        val map = Long2AnyHashMap<String>()
        for (i in 1L..200L) map[i] = "v$i"
        map.trimToSize()

        val visited = mutableListOf<Long>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            assertEquals("v${entry.key}", entry.value)
            visited.add(entry.key)
            it.remove()
        }
        assertTrue(map.isEmpty())
        assertEquals((1L..200L).toList(), visited.sorted())
    }

    @Test
    fun foreach_matchesIterator() {
        val map = Long2AnyHashMap<String>()
        for (i in 1L..50L) map[i] = "v$i"
        map[0L] = "v0"

        val fromIterator = mutableListOf<Pair<Long, String>>()
        val it = map.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            fromIterator.add(entry.key to entry.value)
        }

        val fromForeach = mutableListOf<Pair<Long, String>>()
        map.foreach { k, v -> fromForeach.add(k to v) }

        assertEquals(fromIterator.toMap(), fromForeach.toMap())
    }

    @Test
    fun equals_matchesAnyMapImplementation() {
        // Map contract (Abstract*Map.equals()) requires equality against ANY Long2AnyMap implementation,
        // not just the same concrete class.
        val map: Any = mutableLong2AnyMapOf(1L to "a", 2L to "b")
        assertEquals(long2AnyMapOf(1L to "a", 2L to "b"), map)
    }

    @Test
    fun entryEquals_matchesAnyMapEntryImplementation() {
        // Map.Entry contract (see HashMap.kte's FastEntryIterator.equals()) requires equality against
        // ANY Long2AnyMap.Entry implementation, not just the same concrete class.
        val entry: Any = mutableLong2AnyMapOf(1L to "a").iterator().next()
        assertEquals(mutableLong2AnyMapOf(1L to "a").iterator().next(), entry)
    }
}
