package io.github.sooniln.fastcollect.ints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Int2DoubleHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Int2DoubleHashMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Int2DoubleHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsNaN() {
        assertTrue(Int2DoubleHashMap().defaultValue.isNaN())
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Int2DoubleHashMap(defaultValue = -1.0)
        assertEquals(-1.0, map.defaultValue)
        assertEquals(-1.0, map.lookup(999))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Int2DoubleHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Int2DoubleHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorOneThrows() {
        assertFailsWith<IllegalArgumentException> { Int2DoubleHashMap(4, 1f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2DoubleHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = Int2DoubleHashMap()
        assertTrue(map.putValue(1, 100.0).isNaN())
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 100.0)
        assertEquals(100.0, map.putValue(1, 200.0))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0)
        map.putValue(2, 20.0)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0)
        map.putValue(1, 20.0)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Int2DoubleHashMap()
        map[5] = 50.0
        assertEquals(50.0, map.lookup(5))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertEquals(42.0, map.lookup(1))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = Int2DoubleHashMap()
        assertTrue(map.lookup(99).isNaN())
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0)
        map.putValue(1, 20.0)
        assertEquals(20.0, map.lookup(1))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Int2DoubleHashMap()
        map.putValue(5, 50.0)
        assertTrue(map.containsKey(5))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Int2DoubleHashMap().containsKey(5))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Int2DoubleHashMap()
        map.putValue(5, 50.0)
        map.removeKey(5)
        assertFalse(map.containsKey(5))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertTrue(map.containsValue(42.0))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertFalse(map.containsValue(99.0))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        map.removeKey(1)
        assertFalse(map.containsValue(42.0))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Int2DoubleHashMap(defaultValue = -1.0)
        map.putValue(1, -1.0)
        assertTrue(map.containsValue(-1.0))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = Int2DoubleHashMap()
        assertTrue(map.putValue(0, 100.0).isNaN())
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 100.0)
        assertEquals(100.0, map.putValue(0, 200.0))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 99.0)
        assertEquals(99.0, map.lookup(0))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2DoubleHashMap()
        assertTrue(map.lookup(0).isNaN())
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 1.0)
        assertTrue(map.containsKey(0))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Int2DoubleHashMap().containsKey(0))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 1.0)
        map.putValue(1, 2.0)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 77.0)
        assertEquals(77.0, map.removeKey(0))
        assertFalse(map.containsKey(0))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2DoubleHashMap()
        assertTrue(map.removeKey(0).isNaN())
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 10.0)
        map.putValue(1, 20.0)
        val result = mutableMapOf<Int, Double>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0 to 10.0, 1 to 20.0), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 55.0)
        assertTrue(map.containsValue(55.0))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Int2DoubleHashMap()
        map.putValue(3, 30.0)
        assertEquals(30.0, map.removeKey(3))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = Int2DoubleHashMap()
        assertTrue(map.removeKey(99).isNaN())
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0)
        map.removeKey(1)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Int2DoubleHashMap()
        map.putValue(7, 70.0)
        map.removeKey(7)
        assertFalse(map.containsKey(7))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertTrue(Int2DoubleHashMap().removeKey(1).isNaN())
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 1.0); map.putValue(1, 2.0)
        map.clear()
        assertFalse(map.containsKey(0))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0)
        map.clear()
        map.putValue(3, 30.0)
        assertEquals(1, map.size)
        assertEquals(30.0, map.lookup(3))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0); map.putValue(3, 30.0)
        val result = mutableMapOf<Int, Double>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1 to 10.0, 2 to 20.0, 3 to 30.0), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Int2DoubleHashMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Int2DoubleHashMap()
        for (i in 1..20) map.putValue(i, i * 10.0)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0)
        val result = mutableMapOf<Int, Double>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1 to 10.0, 2 to 20.0), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0); map.putValue(3, 30.0)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Int2DoubleHashMap()
        map.putValue(7, 70.0)
        assertTrue(map.keys.contains(7))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Int2DoubleHashMap().keys.contains(7))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Int2DoubleHashMap()
        map.putValue(0, 1.0)
        assertTrue(map.keys.contains(0))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0); map.putValue(3, 30.0)
        assertEquals(setOf(1, 2, 3), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertTrue(map.values.contains(42.0))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Int2DoubleHashMap().values.contains(99.0))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertEquals(42.0, map.getOrDefault(1, -1.0))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1.0, Int2DoubleHashMap().getOrDefault(99, -1.0))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertEquals(42.0, map.getOrElse(1) { -1.0 })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1.0, Int2DoubleHashMap().getOrElse(99) { -1.0 })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Int2DoubleHashMap(defaultValue = 0.0)
        map.putValue(1, 0.0)
        assertEquals(0.0, map.getOrElse(1) { 99.0 })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertEquals(42.0, map.getValue(1))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { Int2DoubleHashMap().getValue(99) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 42.0)
        assertEquals(42.0, map.getOrPut(1) { 99.0 })
        assertEquals(42.0, map.lookup(1))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = Int2DoubleHashMap()
        assertEquals(99.0, map.getOrPut(1) { 99.0 })
        assertEquals(99.0, map.lookup(1))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = Int2DoubleHashMap()
        val result = map.merge(1, 10.0) { old, new -> old + new }
        assertEquals(10.0, result)
        assertEquals(10.0, map.lookup(1))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0)
        val result = map.merge(1, 5.0) { old, new -> old + new }
        assertEquals(15.0, result)
        assertEquals(15.0, map.lookup(1))
    }

    @Test
    fun filterReturnsEntriesMatchingPredicate() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0); map.putValue(3, 30.0)
        val filtered = map.filter { _, value -> value > 15.0 }
        assertFalse(filtered.containsKey(1))
        assertTrue(filtered.containsKey(2))
        assertTrue(filtered.containsKey(3))
        assertEquals(20.0, filtered.lookup(2))
        assertEquals(30.0, filtered.lookup(3))
    }

    @Test
    fun filterDoesNotMutateOriginalMap() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0)
        map.filter { key, _ -> key == 1 }
        assertEquals(2, map.size)
    }

    @Test
    fun filterToAddsToDestination() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0); map.putValue(2, 20.0); map.putValue(3, 30.0)
        val dest = Int2DoubleHashMap()
        dest.putValue(9, 90.0)
        map.filterTo(dest) { _, value -> value >= 20.0 }
        assertEquals(3, dest.size)
        assertTrue(dest.containsKey(2))
        assertTrue(dest.containsKey(3))
        assertTrue(dest.containsKey(9))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Int2DoubleHashMap().apply { putValue(1, 10.0); putValue(2, 20.0) }
        val b = Int2DoubleHashMap().apply { putValue(1, 10.0); putValue(2, 20.0) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Int2DoubleHashMap().apply { putValue(1, 10.0) }
        val b = Int2DoubleHashMap().apply { putValue(1, 20.0) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Int2DoubleHashMap().apply { putValue(1, 10.0); putValue(2, 20.0) }
        val b = Int2DoubleHashMap().apply { putValue(1, 10.0) }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Int2DoubleHashMap().apply { putValue(1, 10.0); putValue(2, 20.0) }
        assertEquals(mapOf(1 to 10.0, 2 to 20.0), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Int2DoubleHashMap().apply { putValue(1, 10.0); putValue(2, 20.0) }
        val b = Int2DoubleHashMap().apply { putValue(1, 10.0); putValue(2, 20.0) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Int2DoubleHashMap().apply { putValue(0, 100.0) }
        val b = Int2DoubleHashMap().apply { putValue(0, 100.0) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Int2DoubleHashMap().apply { putValue(0, 100.0) }
        val b = Int2DoubleHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Int2DoubleHashMap().apply { putValue(0, 100.0) }
        val b = Int2DoubleHashMap().apply { putValue(0, 200.0) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Int2DoubleHashMap()
        map.putAll(mapOf(1 to 10.0, 2 to 20.0, 3 to 30.0))
        assertEquals(3, map.size)
        assertEquals(10.0, map.lookup(1))
        assertEquals(20.0, map.lookup(2))
        assertEquals(30.0, map.lookup(3))
    }

    @Test
    fun putAllFromInt2DoubleMapAddsAllEntries() {
        val map = Int2DoubleHashMap()
        val src = Int2DoubleHashMap().apply { putValue(1, 10.0); putValue(2, 20.0); putValue(3, 30.0) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10.0, map.lookup(1))
        assertEquals(20.0, map.lookup(2))
        assertEquals(30.0, map.lookup(3))
    }

    @Test
    fun putAllFromInt2DoubleMapWithZeroKey() {
        val map = Int2DoubleHashMap()
        val src = Int2DoubleHashMap().apply { putValue(0, 99.0); putValue(1, 10.0) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99.0, map.lookup(0))
        assertEquals(10.0, map.lookup(1))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Int2DoubleHashMap()
        map.putValue(1, 10.0)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Int2DoubleHashMap()
        for (i in 1..20) map.putValue(i, i * 10.0)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10.0, map.lookup(i))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2DoubleHashMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Int2DoubleHashMap()
        for (i in 1..100) map.putValue(i, i * 3.0)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3.0, map.lookup(i))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Int2DoubleHashMap()
        for (i in 1..100) map.putValue(i, i.toDouble())
        val found = mutableMapOf<Int, Double>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toDouble(), found[i])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Int2DoubleHashMap()
        for (i in 1..50) map.putValue(i, i * 2.0)
        for (i in 1..25) map.removeKey(i)
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i))
        for (i in 26..50) assertEquals(i * 2.0, map.lookup(i))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Int2DoubleHashMap()
        map.putValue(0, -1.0)
        for (i in 1..50) map.putValue(i, i.toDouble())
        assertEquals(51, map.size)
        val found = mutableMapOf<Int, Double>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1.0, found[0])
    }

    // --- negative keys/values and boundary values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Int2DoubleHashMap()
        map.putValue(-1, -100.0)
        map.putValue(-50, -500.0)
        assertEquals(-100.0, map.lookup(-1))
        assertEquals(-500.0, map.lookup(-50))
    }

    @Test
    fun intMaxValueAsKey() {
        val map = Int2DoubleHashMap()
        map.putValue(Int.MAX_VALUE, 1.0)
        assertTrue(map.containsKey(Int.MAX_VALUE))
        assertEquals(1.0, map.lookup(Int.MAX_VALUE))
    }

    @Test
    fun doubleMaxValueAsValue() {
        val map = Int2DoubleHashMap()
        map.putValue(1, Double.MAX_VALUE)
        assertEquals(Double.MAX_VALUE, map.lookup(1))
    }

    @Test
    fun nanValueStorableWithCustomDefault() {
        val map = Int2DoubleHashMap(defaultValue = 0.0)
        map.putValue(1, Double.NaN)
        assertTrue(map.containsKey(1))
        assertTrue(map.lookup(1).isNaN())
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Int2DoubleHashMap(defaultValue = 42.0)
        map.putValue(1, 42.0)
        assertTrue(map.containsKey(1))
        assertEquals(42.0, map.lookup(1))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Int2DoubleHashMap(defaultValue = 0.0)
        assertFalse(map.containsKey(1))
        map.putValue(1, 0.0)
        assertTrue(map.containsKey(1))
    }
}
