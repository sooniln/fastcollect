package io.github.sooniln.fastcollect.longs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Long2IntHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Long2IntHashMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Long2IntHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsMinValue() {
        assertEquals(Int.MIN_VALUE, Long2IntHashMap().defaultValue)
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Long2IntHashMap(defaultValue = -1)
        assertEquals(-1, map.defaultValue)
        assertEquals(-1, map.lookup(999L))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Long2IntHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Long2IntHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorOneThrows() {
        assertFailsWith<IllegalArgumentException> { Long2IntHashMap(4, 1f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2IntHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = Long2IntHashMap()
        assertEquals(map.defaultValue, map.putValue(1L, 100))
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, 100)
        assertEquals(100, map.putValue(1L, 200))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10)
        map.putValue(2L, 20)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10)
        map.putValue(1L, 20)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Long2IntHashMap()
        map[5L] = 50
        assertEquals(50, map.lookup(5L))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertEquals(42, map.lookup(1L))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = Long2IntHashMap()
        assertEquals(map.defaultValue, map.lookup(99L))
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10)
        map.putValue(1L, 20)
        assertEquals(20, map.lookup(1L))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Long2IntHashMap()
        map.putValue(5L, 50)
        assertTrue(map.containsKey(5L))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Long2IntHashMap().containsKey(5L))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Long2IntHashMap()
        map.putValue(5L, 50)
        map.removeKey(5L)
        assertFalse(map.containsKey(5L))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertTrue(map.containsValue(42))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertFalse(map.containsValue(99))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        map.removeKey(1L)
        assertFalse(map.containsValue(42))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Long2IntHashMap(defaultValue = -1)
        map.putValue(1L, -1)
        assertTrue(map.containsValue(-1))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = Long2IntHashMap()
        assertEquals(map.defaultValue, map.putValue(0L, 100))
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Long2IntHashMap()
        map.putValue(0L, 100)
        assertEquals(100, map.putValue(0L, 200))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Long2IntHashMap()
        map.putValue(0L, 99)
        assertEquals(99, map.lookup(0L))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = Long2IntHashMap()
        assertEquals(map.defaultValue, map.lookup(0L))
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Long2IntHashMap()
        map.putValue(0L, 1)
        assertTrue(map.containsKey(0L))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Long2IntHashMap().containsKey(0L))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Long2IntHashMap()
        map.putValue(0L, 1)
        map.putValue(1L, 2)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Long2IntHashMap()
        map.putValue(0L, 77)
        assertEquals(77, map.removeKey(0L))
        assertFalse(map.containsKey(0L))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = Long2IntHashMap()
        assertEquals(map.defaultValue, map.removeKey(0L))
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Long2IntHashMap()
        map.putValue(0L, 10)
        map.putValue(1L, 20)
        val result = mutableMapOf<Long, Int>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0L to 10, 1L to 20), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Long2IntHashMap()
        map.putValue(0L, 55)
        assertTrue(map.containsValue(55))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Long2IntHashMap()
        map.putValue(3L, 30)
        assertEquals(30, map.removeKey(3L))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = Long2IntHashMap()
        assertEquals(map.defaultValue, map.removeKey(99L))
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20)
        map.removeKey(1L)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Long2IntHashMap()
        map.putValue(7L, 70)
        map.removeKey(7L)
        assertFalse(map.containsKey(7L))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertEquals(Long2IntHashMap().defaultValue, Long2IntHashMap().removeKey(1L))
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Long2IntHashMap()
        map.putValue(0L, 1); map.putValue(1L, 2)
        map.clear()
        assertFalse(map.containsKey(0L))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20)
        map.clear()
        map.putValue(3L, 30)
        assertEquals(1, map.size)
        assertEquals(30, map.lookup(3L))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20); map.putValue(3L, 30)
        val result = mutableMapOf<Long, Int>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1L to 10, 2L to 20, 3L to 30), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Long2IntHashMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Long2IntHashMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20)
        val result = mutableMapOf<Long, Int>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1L to 10, 2L to 20), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20); map.putValue(3L, 30)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Long2IntHashMap()
        map.putValue(7L, 70)
        assertTrue(map.keys.contains(7L))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Long2IntHashMap().keys.contains(7L))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Long2IntHashMap()
        map.putValue(0L, 1)
        assertTrue(map.keys.contains(0L))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20); map.putValue(3L, 30)
        assertEquals(setOf(1L, 2L, 3L), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertTrue(map.values.contains(42))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Long2IntHashMap().values.contains(99))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10); map.putValue(2L, 20)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertEquals(42, map.getOrDefault(1L, -1))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1, Long2IntHashMap().getOrDefault(99L, -1))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertEquals(42, map.getOrElse(1L) { -1 })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1, Long2IntHashMap().getOrElse(99L) { -1 })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Long2IntHashMap(defaultValue = 0)
        map.putValue(1L, 0)
        assertEquals(0, map.getOrElse(1L) { 99 })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertEquals(42, map.getValue(1L))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { Long2IntHashMap().getValue(99L) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, 42)
        assertEquals(42, map.getOrPut(1L) { 99 })
        assertEquals(42, map.lookup(1L))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = Long2IntHashMap()
        assertEquals(99, map.getOrPut(1L) { 99 })
        assertEquals(99, map.lookup(1L))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = Long2IntHashMap()
        val result = map.merge(1L, 10) { old, new -> old + new }
        assertEquals(10, result)
        assertEquals(10, map.lookup(1L))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10)
        val result = map.merge(1L, 5) { old, new -> old + new }
        assertEquals(15, result)
        assertEquals(15, map.lookup(1L))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Long2IntHashMap().apply { putValue(1L, 10); putValue(2L, 20) }
        val b = Long2IntHashMap().apply { putValue(1L, 10); putValue(2L, 20) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Long2IntHashMap().apply { putValue(1L, 10) }
        val b = Long2IntHashMap().apply { putValue(1L, 20) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Long2IntHashMap().apply { putValue(1L, 10); putValue(2L, 20) }
        val b = Long2IntHashMap().apply { putValue(1L, 10) }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Long2IntHashMap().apply { putValue(1L, 10); putValue(2L, 20) }
        assertEquals(mapOf(1L to 10, 2L to 20), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Long2IntHashMap().apply { putValue(1L, 10); putValue(2L, 20) }
        val b = Long2IntHashMap().apply { putValue(1L, 10); putValue(2L, 20) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Long2IntHashMap().apply { putValue(0L, 100) }
        val b = Long2IntHashMap().apply { putValue(0L, 100) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Long2IntHashMap().apply { putValue(0L, 100) }
        val b = Long2IntHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Long2IntHashMap().apply { putValue(0L, 100) }
        val b = Long2IntHashMap().apply { putValue(0L, 200) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Long2IntHashMap()
        map.putAll(mapOf(1L to 10, 2L to 20, 3L to 30))
        assertEquals(3, map.size)
        assertEquals(10, map.lookup(1L))
        assertEquals(20, map.lookup(2L))
        assertEquals(30, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2IntMapAddsAllEntries() {
        val map = Long2IntHashMap()
        val src = Long2IntHashMap().apply { putValue(1L, 10); putValue(2L, 20); putValue(3L, 30) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10, map.lookup(1L))
        assertEquals(20, map.lookup(2L))
        assertEquals(30, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2IntMapWithZeroKey() {
        val map = Long2IntHashMap()
        val src = Long2IntHashMap().apply { putValue(0L, 99); putValue(1L, 10) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99, map.lookup(0L))
        assertEquals(10, map.lookup(1L))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Long2IntHashMap()
        map.putValue(1L, 10)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Long2IntHashMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10, map.lookup(i.toLong()))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2IntHashMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Long2IntHashMap()
        for (i in 1..100) map.putValue(i.toLong(), i * 3)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Long2IntHashMap()
        for (i in 1..100) map.putValue(i.toLong(), i)
        val found = mutableMapOf<Long, Int>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i, found[i.toLong()])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Long2IntHashMap()
        for (i in 1..50) map.putValue(i.toLong(), i * 2)
        for (i in 1..25) map.removeKey(i.toLong())
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i.toLong()))
        for (i in 26..50) assertEquals(i * 2, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Long2IntHashMap()
        map.putValue(0L, -1)
        for (i in 1..50) map.putValue(i.toLong(), i)
        assertEquals(51, map.size)
        val found = mutableMapOf<Long, Int>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1, found[0L])
    }

    // --- negative keys/values and boundary values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Long2IntHashMap()
        map.putValue(-1L, -100)
        map.putValue(-50L, -500)
        assertEquals(-100, map.lookup(-1L))
        assertEquals(-500, map.lookup(-50L))
    }

    @Test
    fun longMaxValueAsKey() {
        val map = Long2IntHashMap()
        map.putValue(Long.MAX_VALUE, 1)
        assertTrue(map.containsKey(Long.MAX_VALUE))
        assertEquals(1, map.lookup(Long.MAX_VALUE))
    }

    @Test
    fun intMaxValueAsValue() {
        val map = Long2IntHashMap()
        map.putValue(1L, Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, map.lookup(1L))
    }

    @Test
    fun intMinValueAsValueRequiresCustomDefault() {
        val map = Long2IntHashMap(defaultValue = -1)
        map.putValue(1L, Int.MIN_VALUE)
        assertTrue(map.containsKey(1L))
        assertEquals(Int.MIN_VALUE, map.lookup(1L))
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Long2IntHashMap(defaultValue = 42)
        map.putValue(1L, 42)
        assertTrue(map.containsKey(1L))
        assertEquals(42, map.lookup(1L))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Long2IntHashMap(defaultValue = 0)
        assertFalse(map.containsKey(1L))
        map.putValue(1L, 0)
        assertTrue(map.containsKey(1L))
    }
}
