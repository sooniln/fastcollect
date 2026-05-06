package io.github.sooniln.fastcollect.longs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Long2LongHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Long2LongHashMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Long2LongHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsLongMinValue() {
        assertEquals(Long.MIN_VALUE, Long2LongHashMap().defaultValue)
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Long2LongHashMap(defaultValue = -1L)
        assertEquals(-1L, map.defaultValue)
        assertEquals(-1L, map.lookup(999L))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Long2LongHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Long2LongHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2LongHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = Long2LongHashMap()
        assertEquals(map.defaultValue, map.putValue(1L, 100L))
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, 100L)
        assertEquals(100L, map.putValue(1L, 200L))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L)
        map.putValue(2L, 20L)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L)
        map.putValue(1L, 20L)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Long2LongHashMap()
        map[5L] = 50L
        assertEquals(50L, map.lookup(5L))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertEquals(42L, map.lookup(1L))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = Long2LongHashMap()
        assertEquals(map.defaultValue, map.lookup(99L))
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L)
        map.putValue(1L, 20L)
        assertEquals(20L, map.lookup(1L))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Long2LongHashMap()
        map.putValue(5L, 50L)
        assertTrue(map.containsKey(5L))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Long2LongHashMap().containsKey(5L))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Long2LongHashMap()
        map.putValue(5L, 50L)
        map.removeKey(5L)
        assertFalse(map.containsKey(5L))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertTrue(map.containsValue(42L))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertFalse(map.containsValue(99L))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        map.removeKey(1L)
        assertFalse(map.containsValue(42L))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Long2LongHashMap(defaultValue = -1L)
        map.putValue(1L, -1L)
        assertTrue(map.containsValue(-1L))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = Long2LongHashMap()
        assertEquals(map.defaultValue, map.putValue(0L, 100L))
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Long2LongHashMap()
        map.putValue(0L, 100L)
        assertEquals(100L, map.putValue(0L, 200L))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Long2LongHashMap()
        map.putValue(0L, 99L)
        assertEquals(99L, map.lookup(0L))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = Long2LongHashMap()
        assertEquals(map.defaultValue, map.lookup(0L))
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Long2LongHashMap()
        map.putValue(0L, 1L)
        assertTrue(map.containsKey(0L))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Long2LongHashMap().containsKey(0L))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Long2LongHashMap()
        map.putValue(0L, 1L)
        map.putValue(1L, 2L)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Long2LongHashMap()
        map.putValue(0L, 77L)
        assertEquals(77L, map.removeKey(0L))
        assertFalse(map.containsKey(0L))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = Long2LongHashMap()
        assertEquals(map.defaultValue, map.removeKey(0L))
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Long2LongHashMap()
        map.putValue(0L, 10L)
        map.putValue(1L, 20L)
        val result = mutableMapOf<Long, Long>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0L to 10L, 1L to 20L), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Long2LongHashMap()
        map.putValue(0L, 55L)
        assertTrue(map.containsValue(55L))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Long2LongHashMap()
        map.putValue(3L, 30L)
        assertEquals(30L, map.removeKey(3L))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = Long2LongHashMap()
        assertEquals(map.defaultValue, map.removeKey(99L))
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L)
        map.removeKey(1L)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Long2LongHashMap()
        map.putValue(7L, 70L)
        map.removeKey(7L)
        assertFalse(map.containsKey(7L))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertEquals(Long2LongHashMap().defaultValue, Long2LongHashMap().removeKey(1L))
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Long2LongHashMap()
        map.putValue(0L, 1L); map.putValue(1L, 2L)
        map.clear()
        assertFalse(map.containsKey(0L))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L)
        map.clear()
        map.putValue(3L, 30L)
        assertEquals(1, map.size)
        assertEquals(30L, map.lookup(3L))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L); map.putValue(3L, 30L)
        val result = mutableMapOf<Long, Long>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1L to 10L, 2L to 20L, 3L to 30L), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Long2LongHashMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Long2LongHashMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10L)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L)
        val result = mutableMapOf<Long, Long>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1L to 10L, 2L to 20L), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L); map.putValue(3L, 30L)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Long2LongHashMap()
        map.putValue(7L, 70L)
        assertTrue(map.keys.contains(7L))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Long2LongHashMap().keys.contains(7L))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Long2LongHashMap()
        map.putValue(0L, 1L)
        assertTrue(map.keys.contains(0L))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L); map.putValue(3L, 30L)
        assertEquals(setOf(1L, 2L, 3L), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertTrue(map.values.contains(42L))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Long2LongHashMap().values.contains(99L))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L); map.putValue(2L, 20L)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertEquals(42L, map.getOrDefault(1L, -1L))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1L, Long2LongHashMap().getOrDefault(99L, -1L))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertEquals(42L, map.getOrElse(1L) { -1L })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1L, Long2LongHashMap().getOrElse(99L) { -1L })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Long2LongHashMap(defaultValue = 0L)
        map.putValue(1L, 0L)
        assertEquals(0L, map.getOrElse(1L) { 99L })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertEquals(42L, map.getValue(1L))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { Long2LongHashMap().getValue(99L) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, 42L)
        assertEquals(42L, map.getOrPut(1L) { 99L })
        assertEquals(42L, map.lookup(1L))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = Long2LongHashMap()
        assertEquals(99L, map.getOrPut(1L) { 99L })
        assertEquals(99L, map.lookup(1L))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = Long2LongHashMap()
        val result = map.merge(1L, 10L) { old, new -> old + new }
        assertEquals(10L, result)
        assertEquals(10L, map.lookup(1L))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L)
        val result = map.merge(1L, 5L) { old, new -> old + new }
        assertEquals(15L, result)
        assertEquals(15L, map.lookup(1L))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Long2LongHashMap().apply { putValue(1L, 10L); putValue(2L, 20L) }
        val b = Long2LongHashMap().apply { putValue(1L, 10L); putValue(2L, 20L) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Long2LongHashMap().apply { putValue(1L, 10L) }
        val b = Long2LongHashMap().apply { putValue(1L, 20L) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Long2LongHashMap().apply { putValue(1L, 10L); putValue(2L, 20L) }
        val b = Long2LongHashMap().apply { putValue(1L, 10L) }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Long2LongHashMap().apply { putValue(1L, 10L); putValue(2L, 20L) }
        assertEquals(mapOf(1L to 10L, 2L to 20L), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Long2LongHashMap().apply { putValue(1L, 10L); putValue(2L, 20L) }
        val b = Long2LongHashMap().apply { putValue(1L, 10L); putValue(2L, 20L) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Long2LongHashMap().apply { putValue(0L, 100L) }
        val b = Long2LongHashMap().apply { putValue(0L, 100L) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Long2LongHashMap().apply { putValue(0L, 100L) }
        val b = Long2LongHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Long2LongHashMap().apply { putValue(0L, 100L) }
        val b = Long2LongHashMap().apply { putValue(0L, 200L) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Long2LongHashMap()
        map.putAll(mapOf(1L to 10L, 2L to 20L, 3L to 30L))
        assertEquals(3, map.size)
        assertEquals(10L, map.lookup(1L))
        assertEquals(20L, map.lookup(2L))
        assertEquals(30L, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2LongMapAddsAllEntries() {
        val map = Long2LongHashMap()
        val src = Long2LongHashMap().apply { putValue(1L, 10L); putValue(2L, 20L); putValue(3L, 30L) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10L, map.lookup(1L))
        assertEquals(20L, map.lookup(2L))
        assertEquals(30L, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2LongMapWithZeroKey() {
        val map = Long2LongHashMap()
        val src = Long2LongHashMap().apply { putValue(0L, 99L); putValue(1L, 10L) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99L, map.lookup(0L))
        assertEquals(10L, map.lookup(1L))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Long2LongHashMap()
        map.putValue(1L, 10L)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Long2LongHashMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10L)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10L, map.lookup(i.toLong()))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2LongHashMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Long2LongHashMap()
        for (i in 1..100) map.putValue(i.toLong(), i * 3L)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3L, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Long2LongHashMap()
        for (i in 1..100) map.putValue(i.toLong(), i.toLong())
        val found = mutableMapOf<Long, Long>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toLong(), found[i.toLong()])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Long2LongHashMap()
        for (i in 1..50) map.putValue(i.toLong(), i * 2L)
        for (i in 1..25) map.removeKey(i.toLong())
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i.toLong()))
        for (i in 26..50) assertEquals(i * 2L, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Long2LongHashMap()
        map.putValue(0L, -1L)
        for (i in 1..50) map.putValue(i.toLong(), i.toLong())
        assertEquals(51, map.size)
        val found = mutableMapOf<Long, Long>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1L, found[0L])
    }

    // --- negative keys/values and boundary values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Long2LongHashMap()
        map.putValue(-1L, -100L)
        map.putValue(-50L, -500L)
        assertEquals(-100L, map.lookup(-1L))
        assertEquals(-500L, map.lookup(-50L))
    }

    @Test
    fun longMaxValueAsKey() {
        val map = Long2LongHashMap()
        map.putValue(Long.MAX_VALUE, 1L)
        assertTrue(map.containsKey(Long.MAX_VALUE))
        assertEquals(1L, map.lookup(Long.MAX_VALUE))
    }

    @Test
    fun longMaxValueAsValue() {
        val map = Long2LongHashMap()
        map.putValue(1L, Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, map.lookup(1L))
    }

    @Test
    fun longMinValueAsValueRequiresCustomDefault() {
        val map = Long2LongHashMap(defaultValue = -1L)
        map.putValue(1L, Long.MIN_VALUE)
        assertTrue(map.containsKey(1L))
        assertEquals(Long.MIN_VALUE, map.lookup(1L))
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Long2LongHashMap(defaultValue = 42L)
        map.putValue(1L, 42L)
        assertTrue(map.containsKey(1L))
        assertEquals(42L, map.lookup(1L))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Long2LongHashMap(defaultValue = 0L)
        assertFalse(map.containsKey(1L))
        map.putValue(1L, 0L)
        assertTrue(map.containsKey(1L))
    }
}
