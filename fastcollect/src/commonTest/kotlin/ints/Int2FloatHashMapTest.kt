package io.github.sooniln.fastcollect.ints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Int2FloatHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Int2FloatHashMap()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Int2FloatHashMap(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsNaN() {
        assertTrue(Int2FloatHashMap().defaultValue.isNaN())
    }

    @Test
    fun customDefaultValueReflectedInLookup() {
        val map = Int2FloatHashMap(defaultValue = -1f)
        assertEquals(-1f, map.defaultValue)
        assertEquals(-1f, map.lookup(999))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Int2FloatHashMap(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Int2FloatHashMap(4, 0f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2FloatHashMap(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsDefaultValue() {
        val map = Int2FloatHashMap()
        assertTrue(map.putValue(1, 100f).isNaN())
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, 100f)
        assertEquals(100f, map.putValue(1, 200f))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f)
        map.putValue(2, 20f)
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f)
        map.putValue(1, 20f)
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Int2FloatHashMap()
        map[5] = 50f
        assertEquals(50f, map.lookup(5))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertEquals(42f, map.lookup(1))
    }

    @Test
    fun lookupAbsentKeyReturnsDefaultValue() {
        val map = Int2FloatHashMap()
        assertTrue(map.lookup(99).isNaN())
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f)
        map.putValue(1, 20f)
        assertEquals(20f, map.lookup(1))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Int2FloatHashMap()
        map.putValue(5, 50f)
        assertTrue(map.containsKey(5))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Int2FloatHashMap().containsKey(5))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Int2FloatHashMap()
        map.putValue(5, 50f)
        map.removeKey(5)
        assertFalse(map.containsKey(5))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertTrue(map.containsValue(42f))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertFalse(map.containsValue(99f))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        map.removeKey(1)
        assertFalse(map.containsValue(42f))
    }

    @Test
    fun containsValueWhenValueEqualsCustomDefault() {
        val map = Int2FloatHashMap(defaultValue = -1f)
        map.putValue(1, -1f)
        assertTrue(map.containsValue(-1f))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsDefaultValue() {
        val map = Int2FloatHashMap()
        assertTrue(map.putValue(0, 100f).isNaN())
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Int2FloatHashMap()
        map.putValue(0, 100f)
        assertEquals(100f, map.putValue(0, 200f))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Int2FloatHashMap()
        map.putValue(0, 99f)
        assertEquals(99f, map.lookup(0))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2FloatHashMap()
        assertTrue(map.lookup(0).isNaN())
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Int2FloatHashMap()
        map.putValue(0, 1f)
        assertTrue(map.containsKey(0))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Int2FloatHashMap().containsKey(0))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Int2FloatHashMap()
        map.putValue(0, 1f)
        map.putValue(1, 2f)
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Int2FloatHashMap()
        map.putValue(0, 77f)
        assertEquals(77f, map.removeKey(0))
        assertFalse(map.containsKey(0))
    }

    @Test
    fun removeZeroKeyAbsentReturnsDefaultValue() {
        val map = Int2FloatHashMap()
        assertTrue(map.removeKey(0).isNaN())
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Int2FloatHashMap()
        map.putValue(0, 10f)
        map.putValue(1, 20f)
        val result = mutableMapOf<Int, Float>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0 to 10f, 1 to 20f), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Int2FloatHashMap()
        map.putValue(0, 55f)
        assertTrue(map.containsValue(55f))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Int2FloatHashMap()
        map.putValue(3, 30f)
        assertEquals(30f, map.removeKey(3))
    }

    @Test
    fun removeKeyAbsentReturnsDefaultValue() {
        val map = Int2FloatHashMap()
        assertTrue(map.removeKey(99).isNaN())
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f)
        map.removeKey(1)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Int2FloatHashMap()
        map.putValue(7, 70f)
        map.removeKey(7)
        assertFalse(map.containsKey(7))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsDefaultValue() {
        assertTrue(Int2FloatHashMap().removeKey(1).isNaN())
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f)
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Int2FloatHashMap()
        map.putValue(0, 1f); map.putValue(1, 2f)
        map.clear()
        assertFalse(map.containsKey(0))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f)
        map.clear()
        map.putValue(3, 30f)
        assertEquals(1, map.size)
        assertEquals(30f, map.lookup(3))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f); map.putValue(3, 30f)
        val result = mutableMapOf<Int, Float>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1 to 10f, 2 to 20f, 3 to 30f), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Int2FloatHashMap().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Int2FloatHashMap()
        for (i in 1..20) map.putValue(i, i * 10f)
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f)
        val result = mutableMapOf<Int, Float>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1 to 10f, 2 to 20f), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f); map.putValue(3, 30f)
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Int2FloatHashMap()
        map.putValue(7, 70f)
        assertTrue(map.keys.contains(7))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Int2FloatHashMap().keys.contains(7))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f)
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Int2FloatHashMap()
        map.putValue(0, 1f)
        assertTrue(map.keys.contains(0))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f); map.putValue(3, 30f)
        assertEquals(setOf(1, 2, 3), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertTrue(map.values.contains(42f))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Int2FloatHashMap().values.contains(99f))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f); map.putValue(2, 20f)
        assertEquals(2, map.values.size)
    }

    // --- extension functions ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertEquals(42f, map.getOrDefault(1, -1f))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals(-1f, Int2FloatHashMap().getOrDefault(99, -1f))
    }

    @Test
    fun getOrElseReturnsValueForPresentKey() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertEquals(42f, map.getOrElse(1) { -1f })
    }

    @Test
    fun getOrElseReturnsLambdaResultForAbsentKey() {
        assertEquals(-1f, Int2FloatHashMap().getOrElse(99) { -1f })
    }

    @Test
    fun getOrElseWhenValueMatchesCustomDefaultButKeyPresent() {
        val map = Int2FloatHashMap(defaultValue = 0f)
        map.putValue(1, 0f)
        assertEquals(0f, map.getOrElse(1) { 99f })
    }

    @Test
    fun getValueReturnsValueForPresentKey() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertEquals(42f, map.getValue(1))
    }

    @Test
    fun getValueThrowsNoSuchElementForAbsentKey() {
        assertFailsWith<NoSuchElementException> { Int2FloatHashMap().getValue(99) }
    }

    @Test
    fun getOrPutReturnsExistingValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, 42f)
        assertEquals(42f, map.getOrPut(1) { 99f })
        assertEquals(42f, map.lookup(1))
    }

    @Test
    fun getOrPutInsertsAndReturnsNewValueForAbsentKey() {
        val map = Int2FloatHashMap()
        assertEquals(99f, map.getOrPut(1) { 99f })
        assertEquals(99f, map.lookup(1))
        assertEquals(1, map.size)
    }

    @Test
    fun mergeInsertsValueWhenKeyAbsent() {
        val map = Int2FloatHashMap()
        val result = map.merge(1, 10f) { old, new -> old + new }
        assertEquals(10f, result)
        assertEquals(10f, map.lookup(1))
    }

    @Test
    fun mergeCallsMergeFunctionWhenKeyPresent() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f)
        val result = map.merge(1, 5f) { old, new -> old + new }
        assertEquals(15f, result)
        assertEquals(15f, map.lookup(1))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Int2FloatHashMap().apply { putValue(1, 10f); putValue(2, 20f) }
        val b = Int2FloatHashMap().apply { putValue(1, 10f); putValue(2, 20f) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Int2FloatHashMap().apply { putValue(1, 10f) }
        val b = Int2FloatHashMap().apply { putValue(1, 20f) }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Int2FloatHashMap().apply { putValue(1, 10f); putValue(2, 20f) }
        val b = Int2FloatHashMap().apply { putValue(1, 10f) }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Int2FloatHashMap().apply { putValue(1, 10f); putValue(2, 20f) }
        assertEquals(mapOf(1 to 10f, 2 to 20f), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Int2FloatHashMap().apply { putValue(1, 10f); putValue(2, 20f) }
        val b = Int2FloatHashMap().apply { putValue(1, 10f); putValue(2, 20f) }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Int2FloatHashMap().apply { putValue(0, 100f) }
        val b = Int2FloatHashMap().apply { putValue(0, 100f) }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Int2FloatHashMap().apply { putValue(0, 100f) }
        val b = Int2FloatHashMap()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Int2FloatHashMap().apply { putValue(0, 100f) }
        val b = Int2FloatHashMap().apply { putValue(0, 200f) }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Int2FloatHashMap()
        map.putAll(mapOf(1 to 10f, 2 to 20f, 3 to 30f))
        assertEquals(3, map.size)
        assertEquals(10f, map.lookup(1))
        assertEquals(20f, map.lookup(2))
        assertEquals(30f, map.lookup(3))
    }

    @Test
    fun putAllFromInt2FloatMapAddsAllEntries() {
        val map = Int2FloatHashMap()
        val src = Int2FloatHashMap().apply { putValue(1, 10f); putValue(2, 20f); putValue(3, 30f) }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals(10f, map.lookup(1))
        assertEquals(20f, map.lookup(2))
        assertEquals(30f, map.lookup(3))
    }

    @Test
    fun putAllFromInt2FloatMapWithZeroKey() {
        val map = Int2FloatHashMap()
        val src = Int2FloatHashMap().apply { putValue(0, 99f); putValue(1, 10f) }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals(99f, map.lookup(0))
        assertEquals(10f, map.lookup(1))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Int2FloatHashMap()
        map.putValue(1, 10f)
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Int2FloatHashMap()
        for (i in 1..20) map.putValue(i, i * 10f)
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i * 10f, map.lookup(i))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2FloatHashMap().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Int2FloatHashMap()
        for (i in 1..100) map.putValue(i, i * 3f)
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals(i * 3f, map.lookup(i))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Int2FloatHashMap()
        for (i in 1..100) map.putValue(i, i.toFloat())
        val found = mutableMapOf<Int, Float>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toFloat(), found[i])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Int2FloatHashMap()
        for (i in 1..50) map.putValue(i, i * 2f)
        for (i in 1..25) map.removeKey(i)
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i))
        for (i in 26..50) assertEquals(i * 2f, map.lookup(i))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Int2FloatHashMap()
        map.putValue(0, -1f)
        for (i in 1..50) map.putValue(i, i.toFloat())
        assertEquals(51, map.size)
        val found = mutableMapOf<Int, Float>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals(-1f, found[0])
    }

    // --- negative keys/values and boundary values ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Int2FloatHashMap()
        map.putValue(-1, -100f)
        map.putValue(-50, -500f)
        assertEquals(-100f, map.lookup(-1))
        assertEquals(-500f, map.lookup(-50))
    }

    @Test
    fun intMaxValueAsKey() {
        val map = Int2FloatHashMap()
        map.putValue(Int.MAX_VALUE, 1f)
        assertTrue(map.containsKey(Int.MAX_VALUE))
        assertEquals(1f, map.lookup(Int.MAX_VALUE))
    }

    @Test
    fun floatMaxValueAsValue() {
        val map = Int2FloatHashMap()
        map.putValue(1, Float.MAX_VALUE)
        assertEquals(Float.MAX_VALUE, map.lookup(1), absoluteTolerance = 0.0001f)
    }

    @Test
    fun nanValueStorableWithCustomDefault() {
        val map = Int2FloatHashMap(defaultValue = 0f)
        map.putValue(1, Float.NaN)
        assertTrue(map.containsKey(1))
        assertTrue(map.lookup(1).isNaN())
    }

    // --- value equals defaultValue but key is present ---

    @Test
    fun lookupWhenValueEqualsCustomDefaultAndKeyPresent() {
        val map = Int2FloatHashMap(defaultValue = 42f)
        map.putValue(1, 42f)
        assertTrue(map.containsKey(1))
        assertEquals(42f, map.lookup(1))
    }

    @Test
    fun containsKeyDistinguishesAbsentFromValueMatchingDefault() {
        val map = Int2FloatHashMap(defaultValue = 0f)
        assertFalse(map.containsKey(1))
        map.putValue(1, 0f)
        assertTrue(map.containsKey(1))
    }
}
