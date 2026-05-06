package io.github.sooniln.fastcollect.longs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Long2FloatHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Long2FloatHashMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Long2FloatHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsNaN() {
        assertTrue(Long2FloatHashMap().defaultValue.isNaN())
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Long2FloatHashMap(defaultValue = -1f)
        assertEquals(-1f, map.defaultValue)
        assertEquals(-1f, map.lookup(999L))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Long2FloatHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Long2FloatHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2FloatHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = Long2FloatHashMap()
        assertTrue(map.putValue(1L, 100f).isNaN())
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 100f)
        assertEquals(100f, map.putValue(1L, 200f))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f)
        map.putValue(2L, 20f)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f)
        map.putValue(1L, 20f)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Long2FloatHashMap()
        map[5L] = 50f
        assertEquals(50f, map.lookup(5L))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertEquals(42f, map.lookup(1L))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = Long2FloatHashMap()
        assertTrue(map.lookup(99L).isNaN())
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f)
        map.putValue(1L, 20f)
        assertEquals(20f, map.lookup(1L))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Long2FloatHashMap()
        map.putValue(5L, 50f)
        assertTrue(map.containsKey(5L))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Long2FloatHashMap().containsKey(5L))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Long2FloatHashMap()
        map.putValue(5L, 50f)
        map.removeKey(5L)
        assertFalse(map.containsKey(5L))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertTrue(map.containsValue(42f))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertFalse(map.containsValue(99f))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        map.removeKey(1L)
        assertFalse(map.containsValue(42f))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Long2FloatHashMap(defaultValue = -1f)
        map.putValue(1L, -1f)
        assertTrue(map.containsValue(-1f))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = Long2FloatHashMap()
        assertTrue(map.putValue(0L, 100f).isNaN())
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 100f)
        assertEquals(100f, map.putValue(0L, 200f))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 99f)
        assertEquals(99f, map.lookup(0L))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = Long2FloatHashMap()
        assertTrue(map.lookup(0L).isNaN())
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 1f)
        assertTrue(map.containsKey(0L))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Long2FloatHashMap().containsKey(0L))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 1f)
        map.putValue(1L, 2f)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 77f)
        assertEquals(77f, map.removeKey(0L))
        assertFalse(map.containsKey(0L))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = Long2FloatHashMap()
        assertTrue(map.removeKey(0L).isNaN())
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 10f)
        map.putValue(1L, 20f)
        val result = mutableMapOf<Long, Float>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0L to 10f, 1L to 20f), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 55f)
        assertTrue(map.containsValue(55f))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Long2FloatHashMap()
        map.putValue(3L, 30f)
        assertEquals(30f, map.removeKey(3L))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = Long2FloatHashMap()
        assertTrue(map.removeKey(99L).isNaN())
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f)
        map.removeKey(1L)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Long2FloatHashMap()
        map.putValue(7L, 70f)
        map.removeKey(7L)
        assertFalse(map.containsKey(7L))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertTrue(Long2FloatHashMap().removeKey(1L).isNaN())
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 1f); map.putValue(1L, 2f)
        map.clear()
        assertFalse(map.containsKey(0L))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f)
        map.clear()
        map.putValue(3L, 30f)
        assertEquals(1, map.size)
        assertEquals(30f, map.lookup(3L))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f); map.putValue(3L, 30f)
        val result = mutableMapOf<Long, Float>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1L to 10f, 2L to 20f, 3L to 30f), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Long2FloatHashMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Long2FloatHashMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10f)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f)
        val result = mutableMapOf<Long, Float>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1L to 10f, 2L to 20f), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f); map.putValue(3L, 30f)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Long2FloatHashMap()
        map.putValue(7L, 70f)
        assertTrue(map.keys.contains(7L))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Long2FloatHashMap().keys.contains(7L))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Long2FloatHashMap()
        map.putValue(0L, 1f)
        assertTrue(map.keys.contains(0L))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f); map.putValue(3L, 30f)
        assertEquals(setOf(1L, 2L, 3L), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertTrue(map.values.contains(42f))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Long2FloatHashMap().values.contains(99f))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f); map.putValue(2L, 20f)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertEquals(42f, map.getOrDefault(1L, -1f))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1f, Long2FloatHashMap().getOrDefault(99L, -1f))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertEquals(42f, map.getOrElse(1L) { -1f })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1f, Long2FloatHashMap().getOrElse(99L) { -1f })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Long2FloatHashMap(defaultValue = 0f)
        map.putValue(1L, 0f)
        assertEquals(0f, map.getOrElse(1L) { 99f })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertEquals(42f, map.getValue(1L))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { Long2FloatHashMap().getValue(99L) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 42f)
        assertEquals(42f, map.getOrPut(1L) { 99f })
        assertEquals(42f, map.lookup(1L))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = Long2FloatHashMap()
        assertEquals(99f, map.getOrPut(1L) { 99f })
        assertEquals(99f, map.lookup(1L))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = Long2FloatHashMap()
        val result = map.merge(1L, 10f) { old, new -> old + new }
        assertEquals(10f, result)
        assertEquals(10f, map.lookup(1L))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f)
        val result = map.merge(1L, 5f) { old, new -> old + new }
        assertEquals(15f, result)
        assertEquals(15f, map.lookup(1L))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Long2FloatHashMap().apply { putValue(1L, 10f); putValue(2L, 20f) }
        val b = Long2FloatHashMap().apply { putValue(1L, 10f); putValue(2L, 20f) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Long2FloatHashMap().apply { putValue(1L, 10f) }
        val b = Long2FloatHashMap().apply { putValue(1L, 20f) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Long2FloatHashMap().apply { putValue(1L, 10f); putValue(2L, 20f) }
        val b = Long2FloatHashMap().apply { putValue(1L, 10f) }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Long2FloatHashMap().apply { putValue(1L, 10f); putValue(2L, 20f) }
        assertEquals(mapOf(1L to 10f, 2L to 20f), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Long2FloatHashMap().apply { putValue(1L, 10f); putValue(2L, 20f) }
        val b = Long2FloatHashMap().apply { putValue(1L, 10f); putValue(2L, 20f) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Long2FloatHashMap().apply { putValue(0L, 100f) }
        val b = Long2FloatHashMap().apply { putValue(0L, 100f) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Long2FloatHashMap().apply { putValue(0L, 100f) }
        val b = Long2FloatHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Long2FloatHashMap().apply { putValue(0L, 100f) }
        val b = Long2FloatHashMap().apply { putValue(0L, 200f) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Long2FloatHashMap()
        map.putAll(mapOf(1L to 10f, 2L to 20f, 3L to 30f))
        assertEquals(3, map.size)
        assertEquals(10f, map.lookup(1L))
        assertEquals(20f, map.lookup(2L))
        assertEquals(30f, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2FloatMapAddsAllEntries() {
        val map = Long2FloatHashMap()
        val src = Long2FloatHashMap().apply { putValue(1L, 10f); putValue(2L, 20f); putValue(3L, 30f) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10f, map.lookup(1L))
        assertEquals(20f, map.lookup(2L))
        assertEquals(30f, map.lookup(3L))
    }

    @Test
    fun putAllFromLong2FloatMapWithZeroKey() {
        val map = Long2FloatHashMap()
        val src = Long2FloatHashMap().apply { putValue(0L, 99f); putValue(1L, 10f) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99f, map.lookup(0L))
        assertEquals(10f, map.lookup(1L))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Long2FloatHashMap()
        map.putValue(1L, 10f)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Long2FloatHashMap()
        for (i in 1..20) map.putValue(i.toLong(), i * 10f)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10f, map.lookup(i.toLong()))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2FloatHashMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Long2FloatHashMap()
        for (i in 1..100) map.putValue(i.toLong(), i * 3f)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3f, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Long2FloatHashMap()
        for (i in 1..100) map.putValue(i.toLong(), i.toFloat())
        val found = mutableMapOf<Long, Float>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toFloat(), found[i.toLong()])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Long2FloatHashMap()
        for (i in 1..50) map.putValue(i.toLong(), i * 2f)
        for (i in 1..25) map.removeKey(i.toLong())
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i.toLong()))
        for (i in 26..50) assertEquals(i * 2f, map.lookup(i.toLong()))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Long2FloatHashMap()
        map.putValue(0L, -1f)
        for (i in 1..50) map.putValue(i.toLong(), i.toFloat())
        assertEquals(51, map.size)
        val found = mutableMapOf<Long, Float>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1f, found[0L])
    }

    // --- negative keys/values and boundary values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Long2FloatHashMap()
        map.putValue(-1L, -100f)
        map.putValue(-50L, -500f)
        assertEquals(-100f, map.lookup(-1L))
        assertEquals(-500f, map.lookup(-50L))
    }

    @Test
    fun longMaxValueAsKey() {
        val map = Long2FloatHashMap()
        map.putValue(Long.MAX_VALUE, 1f)
        assertTrue(map.containsKey(Long.MAX_VALUE))
        assertEquals(1f, map.lookup(Long.MAX_VALUE))
    }

    @Test
    fun floatMaxValueAsValue() {
        val map = Long2FloatHashMap()
        map.putValue(1L, Float.MAX_VALUE)
        assertEquals(Float.MAX_VALUE, map.lookup(1L), absoluteTolerance = 0.0001f)
    }

    @Test
    fun nanValueStorableWithCustomDefault() {
        val map = Long2FloatHashMap(defaultValue = 0f)
        map.putValue(1L, Float.NaN)
        assertTrue(map.containsKey(1L))
        assertTrue(map.lookup(1L).isNaN())
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Long2FloatHashMap(defaultValue = 42f)
        map.putValue(1L, 42f)
        assertTrue(map.containsKey(1L))
        assertEquals(42f, map.lookup(1L))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Long2FloatHashMap(defaultValue = 0f)
        assertFalse(map.containsKey(1L))
        map.putValue(1L, 0f)
        assertTrue(map.containsKey(1L))
    }
}
