package io.github.sooniln.fastcollect.ints

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Int2AnyHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Int2AnyHashMap<String>()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Int2AnyHashMap<String>(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsNull() {
        assertNull(Int2AnyHashMap<String>().lookup(999))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Int2AnyHashMap<String>(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { Int2AnyHashMap<String>(4, 0f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2AnyHashMap<String>(4, -0.5f) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsNull() {
        val map = Int2AnyHashMap<String>()
        assertNull(map.putValue(1, "a"))
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a")
        assertEquals("a", map.putValue(1, "b"))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a")
        map.putValue(2, "b")
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a")
        map.putValue(1, "b")
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Int2AnyHashMap<String>()
        map[5] = "hello"
        assertEquals("hello", map.lookup(5))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "hello")
        assertEquals("hello", map.lookup(1))
    }

    @Test
    fun lookupAbsentKeyReturnsNull() {
        assertNull(Int2AnyHashMap<String>().lookup(99))
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a")
        map.putValue(1, "b")
        assertEquals("b", map.lookup(1))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Int2AnyHashMap<String>()
        map.putValue(5, "v")
        assertTrue(map.containsKey(5))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Int2AnyHashMap<String>().containsKey(5))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Int2AnyHashMap<String>()
        map.putValue(5, "v")
        map.removeKey(5)
        assertFalse(map.containsKey(5))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "hello")
        assertTrue(map.containsValue("hello"))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "hello")
        assertFalse(map.containsValue("world"))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "hello")
        map.removeKey(1)
        assertFalse(map.containsValue("hello"))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsNull() {
        val map = Int2AnyHashMap<String>()
        assertNull(map.putValue(0, "zero"))
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "zero")
        assertEquals("zero", map.putValue(0, "updated"))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "zero")
        assertEquals("zero", map.lookup(0))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsNull() {
        assertNull(Int2AnyHashMap<String>().lookup(0))
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "v")
        assertTrue(map.containsKey(0))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Int2AnyHashMap<String>().containsKey(0))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "a")
        map.putValue(1, "b")
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "zero")
        assertEquals("zero", map.removeKey(0))
        assertFalse(map.containsKey(0))
    }

    @Test
    fun removeZeroKeyAbsentReturnsNull() {
        assertNull(Int2AnyHashMap<String>().removeKey(0))
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "a")
        map.putValue(1, "b")
        val result = mutableMapOf<Int, String>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0 to "a", 1 to "b"), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "zero")
        assertTrue(map.containsValue("zero"))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(3, "three")
        assertEquals("three", map.removeKey(3))
    }

    @Test
    fun removeKeyAbsentReturnsNull() {
        assertNull(Int2AnyHashMap<String>().removeKey(99))
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b")
        map.removeKey(1)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Int2AnyHashMap<String>()
        map.putValue(7, "seven")
        map.removeKey(7)
        assertFalse(map.containsKey(7))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsNull() {
        assertNull(Int2AnyHashMap<String>().removeKey(1))
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b")
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "zero"); map.putValue(1, "one")
        map.clear()
        assertFalse(map.containsKey(0))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b")
        map.clear()
        map.putValue(3, "c")
        assertEquals(1, map.size)
        assertEquals("c", map.lookup(3))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b"); map.putValue(3, "c")
        val result = mutableMapOf<Int, String>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1 to "a", 2 to "b", 3 to "c"), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Int2AnyHashMap<String>().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Int2AnyHashMap<String>()
        for (i in 1..20) map.putValue(i, i.toString())
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b")
        val result = mutableMapOf<Int, String>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1 to "a", 2 to "b"), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b"); map.putValue(3, "c")
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Int2AnyHashMap<String>()
        map.putValue(7, "v")
        assertTrue(map.keys.contains(7))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Int2AnyHashMap<String>().keys.contains(7))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b")
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "zero")
        assertTrue(map.keys.contains(0))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b"); map.putValue(3, "c")
        assertEquals(setOf(1, 2, 3), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "hello")
        assertTrue(map.values.contains("hello"))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Int2AnyHashMap<String>().values.contains("hello"))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a"); map.putValue(2, "b")
        assertEquals(2, map.values.size)
    }

    // --- getOrDefault ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "hello")
        assertEquals("hello", map.getOrDefault(1, "default"))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals("default", Int2AnyHashMap<String>().getOrDefault(99, "default"))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Int2AnyHashMap<String>().apply { putValue(1, "a"); putValue(2, "b") }
        val b = Int2AnyHashMap<String>().apply { putValue(1, "a"); putValue(2, "b") }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Int2AnyHashMap<String>().apply { putValue(1, "a") }
        val b = Int2AnyHashMap<String>().apply { putValue(1, "b") }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Int2AnyHashMap<String>().apply { putValue(1, "a"); putValue(2, "b") }
        val b = Int2AnyHashMap<String>().apply { putValue(1, "a") }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Int2AnyHashMap<String>().apply { putValue(1, "a"); putValue(2, "b") }
        assertEquals(mapOf(1 to "a", 2 to "b"), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Int2AnyHashMap<String>().apply { putValue(1, "a"); putValue(2, "b") }
        val b = Int2AnyHashMap<String>().apply { putValue(1, "a"); putValue(2, "b") }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Int2AnyHashMap<String>().apply { putValue(0, "zero") }
        val b = Int2AnyHashMap<String>().apply { putValue(0, "zero") }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Int2AnyHashMap<String>().apply { putValue(0, "zero") }
        val b = Int2AnyHashMap<String>()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Int2AnyHashMap<String>().apply { putValue(0, "a") }
        val b = Int2AnyHashMap<String>().apply { putValue(0, "b") }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Int2AnyHashMap<String>()
        map.putAll(mapOf(1 to "a", 2 to "b", 3 to "c"))
        assertEquals(3, map.size)
        assertEquals("a", map.lookup(1))
        assertEquals("b", map.lookup(2))
        assertEquals("c", map.lookup(3))
    }

    @Test
    fun putAllFromInt2AnyMapAddsAllEntries() {
        val map = Int2AnyHashMap<String>()
        val src = Int2AnyHashMap<String>().apply { putValue(1, "a"); putValue(2, "b"); putValue(3, "c") }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals("a", map.lookup(1))
        assertEquals("b", map.lookup(2))
        assertEquals("c", map.lookup(3))
    }

    @Test
    fun putAllFromInt2AnyMapWithZeroKey() {
        val map = Int2AnyHashMap<String>()
        val src = Int2AnyHashMap<String>().apply { putValue(0, "zero"); putValue(1, "one") }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals("zero", map.lookup(0))
        assertEquals("one", map.lookup(1))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a")
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Int2AnyHashMap<String>()
        for (i in 1..20) map.putValue(i, i.toString())
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i.toString(), map.lookup(i))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Int2AnyHashMap<String>().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Int2AnyHashMap<String>()
        for (i in 1..100) map.putValue(i, "v$i")
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals("v$i", map.lookup(i))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Int2AnyHashMap<String>()
        for (i in 1..100) map.putValue(i, i.toString())
        val found = mutableMapOf<Int, String>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toString(), found[i])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Int2AnyHashMap<String>()
        for (i in 1..50) map.putValue(i, "v$i")
        for (i in 1..25) map.removeKey(i)
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i))
        for (i in 26..50) assertEquals("v$i", map.lookup(i))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Int2AnyHashMap<String>()
        map.putValue(0, "zero")
        for (i in 1..50) map.putValue(i, i.toString())
        assertEquals(51, map.size)
        val found = mutableMapOf<Int, String>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals("zero", found[0])
    }

    // --- negative keys and boundary keys ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Int2AnyHashMap<String>()
        map.putValue(-1, "neg1")
        map.putValue(-50, "neg50")
        assertEquals("neg1", map.lookup(-1))
        assertEquals("neg50", map.lookup(-50))
    }

    @Test
    fun intMaxValueAsKey() {
        val map = Int2AnyHashMap<String>()
        map.putValue(Int.MAX_VALUE, "max")
        assertTrue(map.containsKey(Int.MAX_VALUE))
        assertEquals("max", map.lookup(Int.MAX_VALUE))
    }

    // --- containsKey distinguishes absent key from null lookup result ---

    @Test
    fun containsKeyReturnsFalseForAbsentKeyEvenThoughLookupReturnsNull() {
        val map = Int2AnyHashMap<String>()
        assertFalse(map.containsKey(1))
        assertNull(map.lookup(1))
    }

    @Test
    fun containsKeyReturnsTrueAfterPutWhileLookupOfOtherKeyReturnsNull() {
        val map = Int2AnyHashMap<String>()
        map.putValue(1, "a")
        assertTrue(map.containsKey(1))
        assertNull(map.lookup(99))
        assertFalse(map.containsKey(99))
    }
}
