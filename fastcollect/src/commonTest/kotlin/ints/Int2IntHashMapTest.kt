package io.github.sooniln.fastcollect.ints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Int2IntHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Int2IntHashMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Int2IntHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsMinValue() {
        assertEquals(Int.MIN_VALUE, Int2IntHashMap().defaultValue)
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Int2IntHashMap(defaultValue = -1)
        assertEquals(-1, map.defaultValue)
        assertEquals(-1, map.lookup(999))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Int2IntHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Int2IntHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2IntHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = Int2IntHashMap()
        assertEquals(map.defaultValue, map.putValue(1, 100))
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Int2IntHashMap()
        map.putValue(1, 100)
        assertEquals(100, map.putValue(1, 200))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Int2IntHashMap()
        map.putValue(1, 10)
        map.putValue(2, 20)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Int2IntHashMap()
        map.putValue(1, 10)
        map.putValue(1, 20)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Int2IntHashMap()
        map[5] = 50
        assertEquals(50, map.lookup(5))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertEquals(42, map.lookup(1))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = Int2IntHashMap()
        assertEquals(map.defaultValue, map.lookup(99))
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Int2IntHashMap()
        map.putValue(1, 10)
        map.putValue(1, 20)
        assertEquals(20, map.lookup(1))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Int2IntHashMap()
        map.putValue(5, 50)
        assertTrue(map.containsKey(5))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Int2IntHashMap().containsKey(5))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Int2IntHashMap()
        map.putValue(5, 50)
        map.removeKey(5)
        assertFalse(map.containsKey(5))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertTrue(map.containsValue(42))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertFalse(map.containsValue(99))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        map.removeKey(1)
        assertFalse(map.containsValue(42))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Int2IntHashMap(defaultValue = -1)
        map.putValue(1, -1)
        assertTrue(map.containsValue(-1))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = Int2IntHashMap()
        assertEquals(map.defaultValue, map.putValue(0, 100))
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Int2IntHashMap()
        map.putValue(0, 100)
        assertEquals(100, map.putValue(0, 200))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Int2IntHashMap()
        map.putValue(0, 99)
        assertEquals(99, map.lookup(0))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2IntHashMap()
        assertEquals(map.defaultValue, map.lookup(0))
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Int2IntHashMap()
        map.putValue(0, 1)
        assertTrue(map.containsKey(0))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Int2IntHashMap().containsKey(0))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Int2IntHashMap()
        map.putValue(0, 1)
        map.putValue(1, 2)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Int2IntHashMap()
        map.putValue(0, 77)
        assertEquals(77, map.removeKey(0))
        assertFalse(map.containsKey(0))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2IntHashMap()
        assertEquals(map.defaultValue, map.removeKey(0))
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Int2IntHashMap()
        map.putValue(0, 10)
        map.putValue(1, 20)
        val result = mutableMapOf<Int, Int>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0 to 10, 1 to 20), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Int2IntHashMap()
        map.putValue(0, 55)
        assertTrue(map.containsValue(55))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Int2IntHashMap()
        map.putValue(3, 30)
        assertEquals(30, map.removeKey(3))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = Int2IntHashMap()
        assertEquals(map.defaultValue, map.removeKey(99))
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20)
        map.removeKey(1)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Int2IntHashMap()
        map.putValue(7, 70)
        map.removeKey(7)
        assertFalse(map.containsKey(7))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertEquals(Int2IntHashMap().defaultValue, Int2IntHashMap().removeKey(1))
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Int2IntHashMap()
        map.putValue(0, 1); map.putValue(1, 2)
        map.clear()
        assertFalse(map.containsKey(0))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20)
        map.clear()
        map.putValue(3, 30)
        assertEquals(1, map.size)
        assertEquals(30, map.lookup(3))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20); map.putValue(3, 30)
        val result = mutableMapOf<Int, Int>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1 to 10, 2 to 20, 3 to 30), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Int2IntHashMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Int2IntHashMap()
        for (i in 1..20) map.putValue(i, i * 10)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20)
        val result = mutableMapOf<Int, Int>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1 to 10, 2 to 20), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20); map.putValue(3, 30)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Int2IntHashMap()
        map.putValue(7, 70)
        assertTrue(map.keys.contains(7))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Int2IntHashMap().keys.contains(7))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Int2IntHashMap()
        map.putValue(0, 1)
        assertTrue(map.keys.contains(0))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20); map.putValue(3, 30)
        assertEquals(setOf(1, 2, 3), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertTrue(map.values.contains(42))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Int2IntHashMap().values.contains(99))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Int2IntHashMap()
        map.putValue(1, 10); map.putValue(2, 20)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertEquals(42, map.getOrDefault(1, -1))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1, Int2IntHashMap().getOrDefault(99, -1))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertEquals(42, map.getOrElse(1) { -1 })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1, Int2IntHashMap().getOrElse(99) { -1 })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Int2IntHashMap(defaultValue = 0)
        map.putValue(1, 0)
        assertEquals(0, map.getOrElse(1) { 99 })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertEquals(42, map.getValue(1))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { Int2IntHashMap().getValue(99) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = Int2IntHashMap()
        map.putValue(1, 42)
        assertEquals(42, map.getOrPut(1) { 99 })
        assertEquals(42, map.lookup(1))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = Int2IntHashMap()
        assertEquals(99, map.getOrPut(1) { 99 })
        assertEquals(99, map.lookup(1))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = Int2IntHashMap()
        val result = map.merge(1, 10) { old, new -> old + new }
        assertEquals(10, result)
        assertEquals(10, map.lookup(1))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = Int2IntHashMap()
        map.putValue(1, 10)
        val result = map.merge(1, 5) { old, new -> old + new }
        assertEquals(15, result)
        assertEquals(15, map.lookup(1))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Int2IntHashMap().apply { putValue(1, 10); putValue(2, 20) }
        val b = Int2IntHashMap().apply { putValue(1, 10); putValue(2, 20) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Int2IntHashMap().apply { putValue(1, 10) }
        val b = Int2IntHashMap().apply { putValue(1, 20) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Int2IntHashMap().apply { putValue(1, 10); putValue(2, 20) }
        val b = Int2IntHashMap().apply { putValue(1, 10) }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Int2IntHashMap().apply { putValue(1, 10); putValue(2, 20) }
        assertEquals(mapOf(1 to 10, 2 to 20), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Int2IntHashMap().apply { putValue(1, 10); putValue(2, 20) }
        val b = Int2IntHashMap().apply { putValue(1, 10); putValue(2, 20) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Int2IntHashMap().apply { putValue(0, 100) }
        val b = Int2IntHashMap().apply { putValue(0, 100) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Int2IntHashMap().apply { putValue(0, 100) }
        val b = Int2IntHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Int2IntHashMap().apply { putValue(0, 100) }
        val b = Int2IntHashMap().apply { putValue(0, 200) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Int2IntHashMap()
        map.putAll(mapOf(1 to 10, 2 to 20, 3 to 30))
        assertEquals(3, map.size)
        assertEquals(10, map.lookup(1))
        assertEquals(20, map.lookup(2))
        assertEquals(30, map.lookup(3))
    }

    @Test
    fun putAllFromInt2IntMapAddsAllEntries() {
        val map = Int2IntHashMap()
        val src = Int2IntHashMap().apply { putValue(1, 10); putValue(2, 20); putValue(3, 30) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10, map.lookup(1))
        assertEquals(20, map.lookup(2))
        assertEquals(30, map.lookup(3))
    }

    @Test
    fun putAllFromInt2IntMapWithZeroKey() {
        val map = Int2IntHashMap()
        val src = Int2IntHashMap().apply { putValue(0, 99); putValue(1, 10) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99, map.lookup(0))
        assertEquals(10, map.lookup(1))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Int2IntHashMap()
        map.putValue(1, 10)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Int2IntHashMap()
        for (i in 1..20) map.putValue(i, i * 10)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10, map.lookup(i))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2IntHashMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Int2IntHashMap()
        for (i in 1..100) map.putValue(i, i * 3)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3, map.lookup(i))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Int2IntHashMap()
        for (i in 1..100) map.putValue(i, i)
        val found = mutableMapOf<Int, Int>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i, found[i])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Int2IntHashMap()
        for (i in 1..50) map.putValue(i, i * 2)
        for (i in 1..25) map.removeKey(i)
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i))
        for (i in 26..50) assertEquals(i * 2, map.lookup(i))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Int2IntHashMap()
        map.putValue(0, -1)
        for (i in 1..50) map.putValue(i, i)
        assertEquals(51, map.size)
        val found = mutableMapOf<Int, Int>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1, found[0])
    }

    // --- negative keys/values and boundary values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Int2IntHashMap()
        map.putValue(-1, -100)
        map.putValue(-50, -500)
        assertEquals(-100, map.lookup(-1))
        assertEquals(-500, map.lookup(-50))
    }

    @Test
    fun intMaxValueAsKey() {
        val map = Int2IntHashMap()
        map.putValue(Int.MAX_VALUE, 1)
        assertTrue(map.containsKey(Int.MAX_VALUE))
        assertEquals(1, map.lookup(Int.MAX_VALUE))
    }

    @Test
    fun intMaxValueAsValue() {
        val map = Int2IntHashMap()
        map.putValue(1, Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, map.lookup(1))
    }

    @Test
    fun intMinValueAsValueRequiresCustomDefault() {
        val map = Int2IntHashMap(defaultValue = -1)
        map.putValue(1, Int.MIN_VALUE)
        assertTrue(map.containsKey(1))
        assertEquals(Int.MIN_VALUE, map.lookup(1))
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Int2IntHashMap(defaultValue = 42)
        map.putValue(1, 42)
        assertTrue(map.containsKey(1))
        assertEquals(42, map.lookup(1))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Int2IntHashMap(defaultValue = 0)
        assertFalse(map.containsKey(1))
        map.putValue(1, 0)
        assertTrue(map.containsKey(1))
    }
}
