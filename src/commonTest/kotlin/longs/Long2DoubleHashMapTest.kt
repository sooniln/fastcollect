package io.github.sooniln.fastcollect.longs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Long2DoubleHashMapTest {

    private fun newMap() = Long2DoubleHashMap()

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = newMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Long2DoubleHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsNaN() {
        assertTrue(newMap().defaultValue.isNaN())
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Long2DoubleHashMap(defaultValue = -1.0)
        assertEquals(-1.0, map.defaultValue)
        assertEquals(-1.0, map.lookup(999L))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Long2DoubleHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Long2DoubleHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorOneThrows() {
        assertFailsWith<IllegalArgumentException> { Long2DoubleHashMap(4, 1f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2DoubleHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = newMap()
        assertTrue(map.putValue(1L, 100.0).isNaN())
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = newMap()
        map.putValue(1L, 100.0)
        assertEquals(100.0, map.putValue(1L, 200.0))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = newMap()
        map.putValue(1L, 10.0)
        map.putValue(2L, 20.0)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = newMap()
        map.putValue(1L, 10.0)
        map.putValue(1L, 20.0)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = newMap()
        map[5L] = 50.0
        assertEquals(50.0, map.lookup(5L))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertEquals(42.0, map.lookup(1L))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = newMap()
        assertTrue(map.lookup(99L).isNaN())
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = newMap()
        map.putValue(1L, 10.0)
        map.putValue(1L, 20.0)
        assertEquals(20.0, map.lookup(1L))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = newMap()
        map.putValue(5L, 50.0)
        assertTrue(map.containsKey(5L))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(newMap().containsKey(5L))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = newMap()
        map.putValue(5L, 50.0)
        map.removeKey(5L)
        assertFalse(map.containsKey(5L))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertTrue(map.containsValue(42.0))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertFalse(map.containsValue(99.0))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = newMap()
        map.putValue(1L, 42.0)
        map.removeKey(1L)
        assertFalse(map.containsValue(42.0))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Long2DoubleHashMap(defaultValue = -1.0)
        map.putValue(1L, -1.0)
        assertTrue(map.containsValue(-1.0))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = newMap()
        assertTrue(map.putValue(0L, 100.0).isNaN())
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = newMap()
        map.putValue(0L, 100.0)
        assertEquals(100.0, map.putValue(0L, 200.0))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = newMap()
        map.putValue(0L, 99.0)
        assertEquals(99.0, map.lookup(0L))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = newMap()
        assertTrue(map.lookup(0L).isNaN())
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = newMap()
        map.putValue(0L, 1.0)
        assertTrue(map.containsKey(0L))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(newMap().containsKey(0L))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = newMap()
        map.putValue(0L, 1.0)
        map.putValue(1L, 2.0)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = newMap()
        map.putValue(0L, 77.0)
        assertEquals(77.0, map.removeKey(0L))
        assertFalse(map.containsKey(0L))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = newMap()
        assertTrue(map.removeKey(0L).isNaN())
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = newMap()
        map.putValue(0L, 10.0)
        map.putValue(1L, 20.0)
        val result = mutableMapOf<Long, Double>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0L to 10.0, 1L to 20.0), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = newMap()
        map.putValue(0L, 55.0)
        assertTrue(map.containsValue(55.0))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = newMap()
        map.putValue(3L, 30.0)
        assertEquals(30.0, map.removeKey(3L))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = newMap()
        assertTrue(map.removeKey(99L).isNaN())
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0)
        map.removeKey(1L)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = newMap()
        map.putValue(7L, 70.0)
        map.removeKey(7L)
        assertFalse(map.containsKey(7L))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertTrue(newMap().removeKey(1L).isNaN())
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = newMap()
        map.putValue(0L, 1.0); map.putValue(1L, 2.0)
        map.clear()
        assertFalse(map.containsKey(0L))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0)
        map.clear()
        map.putValue(3L, 30.0)
        assertEquals(1, map.size)
        assertEquals(30.0, map.lookup(3L))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0); map.putValue(3L, 30.0)
        val result = mutableMapOf<Long, Double>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1L to 10.0, 2L to 20.0, 3L to 30.0), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in newMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = newMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10.0)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0)
        val result = mutableMapOf<Long, Double>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1L to 10.0, 2L to 20.0), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0); map.putValue(3L, 30.0)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = newMap()
        map.putValue(7L, 70.0)
        assertTrue(map.keys.contains(7L))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(newMap().keys.contains(7L))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = newMap()
        map.putValue(0L, 1.0)
        assertTrue(map.keys.contains(0L))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0); map.putValue(3L, 30.0)
        assertEquals(setOf(1L, 2L, 3L), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertTrue(map.values.contains(42.0))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(newMap().values.contains(99.0))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertEquals(42.0, map.getOrDefault(1L, -1.0))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1.0, newMap().getOrDefault(99L, -1.0))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertEquals(42.0, map.getOrElse(1L) { -1.0 })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1.0, newMap().getOrElse(99L) { -1.0 })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Long2DoubleHashMap(defaultValue = 0.0)
        map.putValue(1L, 0.0)
        assertEquals(0.0, map.getOrElse(1L) { 99.0 })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertEquals(42.0, map.getValue(1L))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { newMap().getValue(99L) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = newMap()
        map.putValue(1L, 42.0)
        assertEquals(42.0, map.getOrPut(1L) { 99.0 })
        assertEquals(42.0, map.lookup(1L))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = newMap()
        assertEquals(99.0, map.getOrPut(1L) { 99.0 })
        assertEquals(99.0, map.lookup(1L))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = newMap()
        val result = map.merge(1L, 10.0) { old, new -> old + new }
        assertEquals(10.0, result)
        assertEquals(10.0, map.lookup(1L))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = newMap()
        map.putValue(1L, 10.0)
        val result = map.merge(1L, 5.0) { old, new -> old + new }
        assertEquals(15.0, result)
        assertEquals(15.0, map.lookup(1L))
    }

    @Test
    fun filterReturnsEntriesMatchingPredicate() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0); map.putValue(3L, 30.0)
        val filtered = map.filter { _, value -> value > 15.0 }
        assertFalse(filtered.containsKey(1L))
        assertTrue(filtered.containsKey(2L))
        assertTrue(filtered.containsKey(3L))
        assertEquals(20.0, filtered.lookup(2L))
        assertEquals(30.0, filtered.lookup(3L))
    }

    @Test
    fun filterDoesNotMutateOriginalMap() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0)
        map.filter { key, _ -> key == 1L }
        assertEquals(2, map.size)
    }

    @Test
    fun filterToAddsToDestination() {
        val map = newMap()
        map.putValue(1L, 10.0); map.putValue(2L, 20.0); map.putValue(3L, 30.0)
        val dest = Long2DoubleHashMap()
        dest.putValue(9L, 90.0)
        map.filterTo(dest) { _, value -> value >= 20.0 }
        assertEquals(3, dest.size)
        assertTrue(dest.containsKey(2L))
        assertTrue(dest.containsKey(3L))
        assertTrue(dest.containsKey(9L))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Long2DoubleHashMap().apply { putValue(1L, 10.0); putValue(2L, 20.0) }
        val b = Long2DoubleHashMap().apply { putValue(1L, 10.0); putValue(2L, 20.0) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Long2DoubleHashMap().apply { putValue(1L, 10.0) }
        val b = Long2DoubleHashMap().apply { putValue(1L, 20.0) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Long2DoubleHashMap().apply { putValue(1L, 10.0); putValue(2L, 20.0) }
        val b = Long2DoubleHashMap().apply { putValue(1L, 10.0) }
        assertNotEquals(a, b)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Long2DoubleHashMap().apply { putValue(1L, 10.0); putValue(2L, 20.0) }
        val b = Long2DoubleHashMap().apply { putValue(1L, 10.0); putValue(2L, 20.0) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Long2DoubleHashMap().apply { putValue(0L, 100.0) }
        val b = Long2DoubleHashMap().apply { putValue(0L, 100.0) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Long2DoubleHashMap().apply { putValue(0L, 100.0) }
        val b = Long2DoubleHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Long2DoubleHashMap().apply { putValue(0L, 100.0) }
        val b = Long2DoubleHashMap().apply { putValue(0L, 200.0) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = newMap()
        map.putAll(mapOf(1L to 10.0, 2L to 20.0, 3L to 30.0))
        assertEquals(3, map.size)
        assertEquals(10.0, map.lookup(1L))
        assertEquals(20.0, map.lookup(2L))
        assertEquals(30.0, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2DoubleMapAddsAllEntries() {
        val map = newMap()
        val src = Long2DoubleHashMap().apply { putValue(1L, 10.0); putValue(2L, 20.0); putValue(3L, 30.0) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10.0, map.lookup(1L))
        assertEquals(20.0, map.lookup(2L))
        assertEquals(30.0, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2DoubleMapWithZeroKey() {
        val map = newMap()
        val src = Long2DoubleHashMap().apply { putValue(0L, 99.0); putValue(1L, 10.0) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99.0, map.lookup(0L))
        assertEquals(10.0, map.lookup(1L))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = newMap()
        map.putValue(1L, 10.0)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = newMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10.0)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10.0, map.lookup(i.toLong()))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { newMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = newMap()
        for (i in 1..100) map.putValue(i.toLong(), i * 3.0)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3.0, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = newMap()
        for (i in 1..100) map.putValue(i.toLong(), i.toDouble())
        val found = mutableMapOf<Long, Double>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toDouble(), found[i.toLong()])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = newMap()
        for (i in 1..50) map.putValue(i.toLong(), i * 2.0)
        for (i in 1..25) map.removeKey(i.toLong())
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i.toLong()))
        for (i in 26..50) assertEquals(i * 2.0, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = newMap()
        map.putValue(0L, -1.0)
        for (i in 1..50) map.putValue(i.toLong(), i.toDouble())
        assertEquals(51, map.size)
        val found = mutableMapOf<Long, Double>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1.0, found[0L])
    }

    // --- negative and boundary keys/values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = newMap()
        map.putValue(-1L, -100.0)
        map.putValue(-50L, -500.0)
        assertEquals(-100.0, map.lookup(-1L))
        assertEquals(-500.0, map.lookup(-50L))
    }

    @Test
    fun longMaxValueAsKey() {
        val map = newMap()
        map.putValue(Long.MAX_VALUE, 1.0)
        assertTrue(map.containsKey(Long.MAX_VALUE))
        assertEquals(1.0, map.lookup(Long.MAX_VALUE))
    }

    @Test
    fun doubleMaxValueAsValue() {
        val map = newMap()
        map.putValue(1L, Double.MAX_VALUE)
        assertEquals(Double.MAX_VALUE, map.lookup(1L))
    }

    @Test
    fun nanValueStorableWithCustomDefault() {
        val map = Long2DoubleHashMap(defaultValue = 0.0)
        map.putValue(1L, Double.NaN)
        assertTrue(map.containsKey(1L))
        assertTrue(map.lookup(1L).isNaN())
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Long2DoubleHashMap(defaultValue = 42.0)
        map.putValue(1L, 42.0)
        assertTrue(map.containsKey(1L))
        assertEquals(42.0, map.lookup(1L))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Long2DoubleHashMap(defaultValue = 0.0)
        assertFalse(map.containsKey(1L))
        map.putValue(1L, 0.0)
        assertTrue(map.containsKey(1L))
    }
}
