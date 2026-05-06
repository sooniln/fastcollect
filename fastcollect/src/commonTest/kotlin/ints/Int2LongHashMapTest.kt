package io.github.sooniln.fastcollect.ints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Int2LongHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Int2LongHashMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Int2LongHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsLongMinValue() {
        assertEquals(Long.MIN_VALUE, Int2LongHashMap().defaultValue)
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Int2LongHashMap(defaultValue = -1L)
        assertEquals(-1L, map.defaultValue)
        assertEquals(-1L, map.lookup(999))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Int2LongHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Int2LongHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2LongHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = Int2LongHashMap()
        assertEquals(map.defaultValue, map.putValue(1, 100L))
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Int2LongHashMap()
        map.putValue(1, 100L)
        assertEquals(100L, map.putValue(1, 200L))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L)
        map.putValue(2, 20L)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L)
        map.putValue(1, 20L)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Int2LongHashMap()
        map[5] = 50L
        assertEquals(50L, map.lookup(5))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertEquals(42L, map.lookup(1))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = Int2LongHashMap()
        assertEquals(map.defaultValue, map.lookup(99))
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L)
        map.putValue(1, 20L)
        assertEquals(20L, map.lookup(1))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Int2LongHashMap()
        map.putValue(5, 50L)
        assertTrue(map.containsKey(5))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Int2LongHashMap().containsKey(5))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Int2LongHashMap()
        map.putValue(5, 50L)
        map.removeKey(5)
        assertFalse(map.containsKey(5))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertTrue(map.containsValue(42L))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertFalse(map.containsValue(99L))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        map.removeKey(1)
        assertFalse(map.containsValue(42L))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Int2LongHashMap(defaultValue = -1L)
        map.putValue(1, -1L)
        assertTrue(map.containsValue(-1L))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = Int2LongHashMap()
        assertEquals(map.defaultValue, map.putValue(0, 100L))
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Int2LongHashMap()
        map.putValue(0, 100L)
        assertEquals(100L, map.putValue(0, 200L))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Int2LongHashMap()
        map.putValue(0, 99L)
        assertEquals(99L, map.lookup(0))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2LongHashMap()
        assertEquals(map.defaultValue, map.lookup(0))
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Int2LongHashMap()
        map.putValue(0, 1L)
        assertTrue(map.containsKey(0))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Int2LongHashMap().containsKey(0))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Int2LongHashMap()
        map.putValue(0, 1L)
        map.putValue(1, 2L)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Int2LongHashMap()
        map.putValue(0, 77L)
        assertEquals(77L, map.removeKey(0))
        assertFalse(map.containsKey(0))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2LongHashMap()
        assertEquals(map.defaultValue, map.removeKey(0))
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Int2LongHashMap()
        map.putValue(0, 10L)
        map.putValue(1, 20L)
        val result = mutableMapOf<Int, Long>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0 to 10L, 1 to 20L), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Int2LongHashMap()
        map.putValue(0, 55L)
        assertTrue(map.containsValue(55L))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Int2LongHashMap()
        map.putValue(3, 30L)
        assertEquals(30L, map.removeKey(3))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = Int2LongHashMap()
        assertEquals(map.defaultValue, map.removeKey(99))
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L)
        map.removeKey(1)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Int2LongHashMap()
        map.putValue(7, 70L)
        map.removeKey(7)
        assertFalse(map.containsKey(7))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertEquals(Int2LongHashMap().defaultValue, Int2LongHashMap().removeKey(1))
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Int2LongHashMap()
        map.putValue(0, 1L); map.putValue(1, 2L)
        map.clear()
        assertFalse(map.containsKey(0))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L)
        map.clear()
        map.putValue(3, 30L)
        assertEquals(1, map.size)
        assertEquals(30L, map.lookup(3))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L); map.putValue(3, 30L)
        val result = mutableMapOf<Int, Long>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1 to 10L, 2 to 20L, 3 to 30L), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Int2LongHashMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Int2LongHashMap()
        for (i in 1..20) map.putValue(i, i * 10L)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L)
        val result = mutableMapOf<Int, Long>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1 to 10L, 2 to 20L), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L); map.putValue(3, 30L)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Int2LongHashMap()
        map.putValue(7, 70L)
        assertTrue(map.keys.contains(7))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Int2LongHashMap().keys.contains(7))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Int2LongHashMap()
        map.putValue(0, 1L)
        assertTrue(map.keys.contains(0))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L); map.putValue(3, 30L)
        assertEquals(setOf(1, 2, 3), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertTrue(map.values.contains(42L))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Int2LongHashMap().values.contains(99L))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L); map.putValue(2, 20L)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertEquals(42L, map.getOrDefault(1, -1L))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1L, Int2LongHashMap().getOrDefault(99, -1L))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertEquals(42L, map.getOrElse(1) { -1L })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1L, Int2LongHashMap().getOrElse(99) { -1L })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Int2LongHashMap(defaultValue = 0L)
        map.putValue(1, 0L)
        assertEquals(0L, map.getOrElse(1) { 99L })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertEquals(42L, map.getValue(1))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { Int2LongHashMap().getValue(99) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = Int2LongHashMap()
        map.putValue(1, 42L)
        assertEquals(42L, map.getOrPut(1) { 99L })
        assertEquals(42L, map.lookup(1))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = Int2LongHashMap()
        assertEquals(99L, map.getOrPut(1) { 99L })
        assertEquals(99L, map.lookup(1))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = Int2LongHashMap()
        val result = map.merge(1, 10L) { old, new -> old + new }
        assertEquals(10L, result)
        assertEquals(10L, map.lookup(1))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L)
        val result = map.merge(1, 5L) { old, new -> old + new }
        assertEquals(15L, result)
        assertEquals(15L, map.lookup(1))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Int2LongHashMap().apply { putValue(1, 10L); putValue(2, 20L) }
        val b = Int2LongHashMap().apply { putValue(1, 10L); putValue(2, 20L) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Int2LongHashMap().apply { putValue(1, 10L) }
        val b = Int2LongHashMap().apply { putValue(1, 20L) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Int2LongHashMap().apply { putValue(1, 10L); putValue(2, 20L) }
        val b = Int2LongHashMap().apply { putValue(1, 10L) }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Int2LongHashMap().apply { putValue(1, 10L); putValue(2, 20L) }
        assertEquals(mapOf(1 to 10L, 2 to 20L), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Int2LongHashMap().apply { putValue(1, 10L); putValue(2, 20L) }
        val b = Int2LongHashMap().apply { putValue(1, 10L); putValue(2, 20L) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Int2LongHashMap().apply { putValue(0, 100L) }
        val b = Int2LongHashMap().apply { putValue(0, 100L) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Int2LongHashMap().apply { putValue(0, 100L) }
        val b = Int2LongHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Int2LongHashMap().apply { putValue(0, 100L) }
        val b = Int2LongHashMap().apply { putValue(0, 200L) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Int2LongHashMap()
        map.putAll(mapOf(1 to 10L, 2 to 20L, 3 to 30L))
        assertEquals(3, map.size)
        assertEquals(10L, map.lookup(1))
        assertEquals(20L, map.lookup(2))
        assertEquals(30L, map.lookup(3))
    }

    @Test
    fun putAllFromInt2LongMapAddsAllEntries() {
        val map = Int2LongHashMap()
        val src = Int2LongHashMap().apply { putValue(1, 10L); putValue(2, 20L); putValue(3, 30L) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10L, map.lookup(1))
        assertEquals(20L, map.lookup(2))
        assertEquals(30L, map.lookup(3))
    }

    @Test
    fun putAllFromInt2LongMapWithZeroKey() {
        val map = Int2LongHashMap()
        val src = Int2LongHashMap().apply { putValue(0, 99L); putValue(1, 10L) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99L, map.lookup(0))
        assertEquals(10L, map.lookup(1))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Int2LongHashMap()
        map.putValue(1, 10L)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Int2LongHashMap()
        for (i in 1..20) map.putValue(i, i * 10L)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10L, map.lookup(i))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2LongHashMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Int2LongHashMap()
        for (i in 1..100) map.putValue(i, i * 3L)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3L, map.lookup(i))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Int2LongHashMap()
        for (i in 1..100) map.putValue(i, i.toLong())
        val found = mutableMapOf<Int, Long>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toLong(), found[i])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Int2LongHashMap()
        for (i in 1..50) map.putValue(i, i * 2L)
        for (i in 1..25) map.removeKey(i)
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i))
        for (i in 26..50) assertEquals(i * 2L, map.lookup(i))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Int2LongHashMap()
        map.putValue(0, -1L)
        for (i in 1..50) map.putValue(i, i.toLong())
        assertEquals(51, map.size)
        val found = mutableMapOf<Int, Long>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1L, found[0])
    }

    // --- negative keys/values and boundary values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Int2LongHashMap()
        map.putValue(-1, -100L)
        map.putValue(-50, -500L)
        assertEquals(-100L, map.lookup(-1))
        assertEquals(-500L, map.lookup(-50))
    }

    @Test
    fun intMaxValueAsKey() {
        val map = Int2LongHashMap()
        map.putValue(Int.MAX_VALUE, 1L)
        assertTrue(map.containsKey(Int.MAX_VALUE))
        assertEquals(1L, map.lookup(Int.MAX_VALUE))
    }

    @Test
    fun longMaxValueAsValue() {
        val map = Int2LongHashMap()
        map.putValue(1, Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, map.lookup(1))
    }

    @Test
    fun longMinValueAsValueRequiresCustomDefault() {
        val map = Int2LongHashMap(defaultValue = -1L)
        map.putValue(1, Long.MIN_VALUE)
        assertTrue(map.containsKey(1))
        assertEquals(Long.MIN_VALUE, map.lookup(1))
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Int2LongHashMap(defaultValue = 42L)
        map.putValue(1, 42L)
        assertTrue(map.containsKey(1))
        assertEquals(42L, map.lookup(1))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Int2LongHashMap(defaultValue = 0L)
        assertFalse(map.containsKey(1))
        map.putValue(1, 0L)
        assertTrue(map.containsKey(1))
    }
}
