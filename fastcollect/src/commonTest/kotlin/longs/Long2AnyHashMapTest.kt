package io.github.sooniln.fastcollect.longs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class Long2AnyHashMapTest {

    // --- construction & size ---

    @Test
    fun emptyMapHasSizeZero() {
        val map = Long2AnyHashMap<String>()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val map = Long2AnyHashMap<String>(100)
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun defaultValueIsNull() {
        assertNull(Long2AnyHashMap<String>().lookup(999L))
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { Long2AnyHashMap<String>(-1) }
    }

    // --- putValue / set ---

    @Test
    fun putValueNewKeyReturnsNull() {
        val map = Long2AnyHashMap<String>()
        assertNull(map.putValue(1L, "a"))
    }

    @Test
    fun putValueExistingKeyReturnsOldValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a")
        assertEquals("a", map.putValue(1L, "b"))
    }

    @Test
    fun putValueIncreasesSize() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a")
        map.putValue(2L, "b")
        assertEquals(2, map.size)
    }

    @Test
    fun putValueUpdateDoesNotChangeSize() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a")
        map.putValue(1L, "b")
        assertEquals(1, map.size)
    }

    @Test
    fun setOperatorPutsValue() {
        val map = Long2AnyHashMap<String>()
        map[5L] = "hello"
        assertEquals("hello", map.lookup(5L))
    }

    // --- lookup ---

    @Test
    fun lookupPresentKeyReturnsValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "hello")
        assertEquals("hello", map.lookup(1L))
    }

    @Test
    fun lookupAbsentKeyReturnsNull() {
        assertNull(Long2AnyHashMap<String>().lookup(99L))
    }

    @Test
    fun lookupUpdatedKeyReturnsNewValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a")
        map.putValue(1L, "b")
        assertEquals("b", map.lookup(1L))
    }

    // --- containsKey ---

    @Test
    fun containsKeyReturnsTrueForPresentKey() {
        val map = Long2AnyHashMap<String>()
        map.putValue(5L, "v")
        assertTrue(map.containsKey(5L))
    }

    @Test
    fun containsKeyReturnsFalseForAbsentKey() {
        assertFalse(Long2AnyHashMap<String>().containsKey(5L))
    }

    @Test
    fun containsKeyFalseAfterRemove() {
        val map = Long2AnyHashMap<String>()
        map.putValue(5L, "v")
        map.removeKey(5L)
        assertFalse(map.containsKey(5L))
    }

    // --- containsValue ---

    @Test
    fun containsValueReturnsTrueForPresentValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "hello")
        assertTrue(map.containsValue("hello"))
    }

    @Test
    fun containsValueReturnsFalseForAbsentValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "hello")
        assertFalse(map.containsValue("world"))
    }

    @Test
    fun containsValueReturnsFalseAfterRemoval() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "hello")
        map.removeKey(1L)
        assertFalse(map.containsValue("hello"))
    }

    // --- zero key handling ---

    @Test
    fun putValueZeroKeyFirstTimeReturnsNull() {
        val map = Long2AnyHashMap<String>()
        assertNull(map.putValue(0L, "zero"))
    }

    @Test
    fun putValueZeroKeyUpdateReturnsOldValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "zero")
        assertEquals("zero", map.putValue(0L, "updated"))
    }

    @Test
    fun lookupZeroKeyPresentReturnsValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "zero")
        assertEquals("zero", map.lookup(0L))
    }

    @Test
    fun lookupZeroKeyAbsentReturnsNull() {
        assertNull(Long2AnyHashMap<String>().lookup(0L))
    }

    @Test
    fun containsKeyZeroTrueWhenPresent() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "v")
        assertTrue(map.containsKey(0L))
    }

    @Test
    fun containsKeyZeroFalseWhenAbsent() {
        assertFalse(Long2AnyHashMap<String>().containsKey(0L))
    }

    @Test
    fun zeroKeyCountedInSize() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "a")
        map.putValue(1L, "b")
        assertEquals(2, map.size)
    }

    @Test
    fun removeZeroKeyReturnsOldValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "zero")
        assertEquals("zero", map.removeKey(0L))
        assertFalse(map.containsKey(0L))
    }

    @Test
    fun removeZeroKeyAbsentReturnsNull() {
        assertNull(Long2AnyHashMap<String>().removeKey(0L))
    }

    @Test
    fun zeroKeyIncludedInIteration() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "a")
        map.putValue(1L, "b")
        val result = mutableMapOf<Long, String>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(0L to "a", 1L to "b"), result)
    }

    @Test
    fun containsValueChecksZeroKeyValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "zero")
        assertTrue(map.containsValue("zero"))
    }

    // --- removeKey ---

    @Test
    fun removeKeyPresentReturnsOldValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(3L, "three")
        assertEquals("three", map.removeKey(3L))
    }

    @Test
    fun removeKeyAbsentReturnsNull() {
        assertNull(Long2AnyHashMap<String>().removeKey(99L))
    }

    @Test
    fun removeKeyDecreasesSize() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b")
        map.removeKey(1L)
        assertEquals(1, map.size)
    }

    @Test
    fun removedKeyNoLongerContained() {
        val map = Long2AnyHashMap<String>()
        map.putValue(7L, "seven")
        map.removeKey(7L)
        assertFalse(map.containsKey(7L))
    }

    @Test
    fun removeKeyFromEmptyMapReturnsNull() {
        assertNull(Long2AnyHashMap<String>().removeKey(1L))
    }

    // --- clear ---

    @Test
    fun clearEmptiesMap() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b")
        map.clear()
        assertEquals(0, map.size)
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearRemovesZeroKey() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "zero"); map.putValue(1L, "one")
        map.clear()
        assertFalse(map.containsKey(0L))
        assertEquals(0, map.size)
    }

    @Test
    fun addAfterClearWorks() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b")
        map.clear()
        map.putValue(3L, "c")
        assertEquals(1, map.size)
        assertEquals("c", map.lookup(3L))
    }

    // --- iteration (primitiveEntries) ---

    @Test
    fun iterationTraversesAllEntries() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b"); map.putValue(3L, "c")
        val result = mutableMapOf<Long, String>()
        for (e in map.primitiveEntries) result[e.key()] = e.value()
        assertEquals(mapOf(1L to "a", 2L to "b", 3L to "c"), result)
    }

    @Test
    fun iterationOnEmptyMapProducesNoEntries() {
        var count = 0
        for (e in Long2AnyHashMap<String>().primitiveEntries) count++
        assertEquals(0, count)
    }

    @Test
    fun iterationCountMatchesSize() {
        val map = Long2AnyHashMap<String>()
        for (i in 1..20) map.putValue(i.toLong(), i.toString())
        var count = 0
        for (e in map.primitiveEntries) count++
        assertEquals(20, count)
    }

    @Test
    fun fastIteratorTraversesAllEntries() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b")
        val result = mutableMapOf<Long, String>()
        val iter = map.fastIterator()
        while (iter.hasNext()) {
            val e = iter.next()
            result[e.key()] = e.value()
        }
        assertEquals(mapOf(1L to "a", 2L to "b"), result)
    }

    @Test
    fun iteratorRemoveDecreasesSize() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b"); map.putValue(3L, "c")
        val iter = map.primitiveEntries.iterator()
        val removedKey = iter.next().key()
        iter.remove()
        assertFalse(map.containsKey(removedKey))
        assertEquals(2, map.size)
    }

    // --- keys view ---

    @Test
    fun keysContainsPresentKey() {
        val map = Long2AnyHashMap<String>()
        map.putValue(7L, "v")
        assertTrue(map.keys.contains(7L))
    }

    @Test
    fun keysDoesNotContainAbsentKey() {
        assertFalse(Long2AnyHashMap<String>().keys.contains(7L))
    }

    @Test
    fun keysSizeMatchesMapSize() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b")
        assertEquals(2, map.keys.size)
    }

    @Test
    fun keysContainsZeroKey() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "zero")
        assertTrue(map.keys.contains(0L))
    }

    @Test
    fun keysIterationMatchesMapKeys() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b"); map.putValue(3L, "c")
        assertEquals(setOf(1L, 2L, 3L), map.keys.toSet())
    }

    // --- values view ---

    @Test
    fun valuesContainsPresentValue() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "hello")
        assertTrue(map.values.contains("hello"))
    }

    @Test
    fun valuesDoesNotContainAbsentValue() {
        assertFalse(Long2AnyHashMap<String>().values.contains("hello"))
    }

    @Test
    fun valuesSizeMatchesMapSize() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a"); map.putValue(2L, "b")
        assertEquals(2, map.values.size)
    }

    // --- getOrDefault ---

    @Test
    fun getOrDefaultReturnsValueForPresentKey() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "hello")
        assertEquals("hello", map.getOrDefault(1L, "default"))
    }

    @Test
    fun getOrDefaultReturnsSuppliedDefaultForAbsentKey() {
        assertEquals("default", Long2AnyHashMap<String>().getOrDefault(99L, "default"))
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = Long2AnyHashMap<String>().apply { putValue(1L, "a"); putValue(2L, "b") }
        val b = Long2AnyHashMap<String>().apply { putValue(1L, "a"); putValue(2L, "b") }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentValues() {
        val a = Long2AnyHashMap<String>().apply { putValue(1L, "a") }
        val b = Long2AnyHashMap<String>().apply { putValue(1L, "b") }
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentSize() {
        val a = Long2AnyHashMap<String>().apply { putValue(1L, "a"); putValue(2L, "b") }
        val b = Long2AnyHashMap<String>().apply { putValue(1L, "a") }
        assertNotEquals(a, b)
    }

    @Test
    fun equalsWithStandardMap() {
        val map = Long2AnyHashMap<String>().apply { putValue(1L, "a"); putValue(2L, "b") }
        assertEquals(mapOf(1L to "a", 2L to "b"), map)
    }

    @Test
    fun hashCodeConsistentWithEqualMaps() {
        val a = Long2AnyHashMap<String>().apply { putValue(1L, "a"); putValue(2L, "b") }
        val b = Long2AnyHashMap<String>().apply { putValue(1L, "a"); putValue(2L, "b") }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun equalsIncludesZeroKey() {
        val a = Long2AnyHashMap<String>().apply { putValue(0L, "zero") }
        val b = Long2AnyHashMap<String>().apply { putValue(0L, "zero") }
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWhenOneMapHasZeroKeyAndOtherDoesNot() {
        val a = Long2AnyHashMap<String>().apply { putValue(0L, "zero") }
        val b = Long2AnyHashMap<String>()
        assertNotEquals(a, b)
    }

    @Test
    fun notEqualsWhenZeroKeyValueDiffers() {
        val a = Long2AnyHashMap<String>().apply { putValue(0L, "a") }
        val b = Long2AnyHashMap<String>().apply { putValue(0L, "b") }
        assertNotEquals(a, b)
    }

    // --- putAll ---

    @Test
    fun putAllFromRegularMapAddsAllEntries() {
        val map = Long2AnyHashMap<String>()
        map.putAll(mapOf(1L to "a", 2L to "b", 3L to "c"))
        assertEquals(3, map.size)
        assertEquals("a", map.lookup(1L))
        assertEquals("b", map.lookup(2L))
        assertEquals("c", map.lookup(3L))
    }

    @Test
    fun putAllFromLong2AnyMapAddsAllEntries() {
        val map = Long2AnyHashMap<String>()
        val src = Long2AnyHashMap<String>().apply { putValue(1L, "a"); putValue(2L, "b"); putValue(3L, "c") }
        map.putAll(src)
        assertEquals(3, map.size)
        assertEquals("a", map.lookup(1L))
        assertEquals("b", map.lookup(2L))
        assertEquals("c", map.lookup(3L))
    }

    @Test
    fun putAllFromLong2AnyMapWithZeroKey() {
        val map = Long2AnyHashMap<String>()
        val src = Long2AnyHashMap<String>().apply { putValue(0L, "zero"); putValue(1L, "one") }
        map.putAll(src)
        assertEquals(2, map.size)
        assertEquals("zero", map.lookup(0L))
        assertEquals("one", map.lookup(1L))
    }

    @Test
    fun putAllFromEmptyMapIsNoOp() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a")
        map.putAll(mapOf())
        assertEquals(1, map.size)
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityPreservesData() {
        val map = Long2AnyHashMap<String>()
        for (i in 1..20) map.putValue(i.toLong(), i.toString())
        map.ensureCapacity(200)
        assertEquals(20, map.size)
        for (i in 1..20) assertEquals(i.toString(), map.lookup(i.toLong()))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { Long2AnyHashMap<String>().ensureCapacity(-1) }
    }

    // --- large map (forces hash mode: >32 entries) ---

    @Test
    fun largeMapStoresAndRetrievesAllEntries() {
        val map = Long2AnyHashMap<String>()
        for (i in 1..100) map.putValue(i.toLong(), "v$i")
        assertEquals(100, map.size)
        for (i in 1..100) assertEquals("v$i", map.lookup(i.toLong()))
    }

    @Test
    fun largeMapIterationIsComplete() {
        val map = Long2AnyHashMap<String>()
        for (i in 1..100) map.putValue(i.toLong(), i.toString())
        val found = mutableMapOf<Long, String>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(100, found.size)
        for (i in 1..100) assertEquals(i.toString(), found[i.toLong()])
    }

    @Test
    fun largeMapRemoveAndLookup() {
        val map = Long2AnyHashMap<String>()
        for (i in 1..50) map.putValue(i.toLong(), "v$i")
        for (i in 1..25) map.removeKey(i.toLong())
        assertEquals(25, map.size)
        for (i in 1..25) assertFalse(map.containsKey(i.toLong()))
        for (i in 26..50) assertEquals("v$i", map.lookup(i.toLong()))
    }

    @Test
    fun largeMapWithZeroKeyIteratesAll() {
        val map = Long2AnyHashMap<String>()
        map.putValue(0L, "zero")
        for (i in 1..50) map.putValue(i.toLong(), i.toString())
        assertEquals(51, map.size)
        val found = mutableMapOf<Long, String>()
        for (e in map.primitiveEntries) found[e.key()] = e.value()
        assertEquals(51, found.size)
        assertEquals("zero", found[0L])
    }

    // --- negative keys and boundary keys ---

    @Test
    fun negativeKeysStoredCorrectly() {
        val map = Long2AnyHashMap<String>()
        map.putValue(-1L, "neg1")
        map.putValue(-50L, "neg50")
        assertEquals("neg1", map.lookup(-1L))
        assertEquals("neg50", map.lookup(-50L))
    }

    @Test
    fun longMaxValueAsKey() {
        val map = Long2AnyHashMap<String>()
        map.putValue(Long.MAX_VALUE, "max")
        assertTrue(map.containsKey(Long.MAX_VALUE))
        assertEquals("max", map.lookup(Long.MAX_VALUE))
    }

    // --- containsKey distinguishes absent key from null lookup result ---

    @Test
    fun containsKeyReturnsFalseForAbsentKeyEvenThoughLookupReturnsNull() {
        val map = Long2AnyHashMap<String>()
        assertFalse(map.containsKey(1L))
        assertNull(map.lookup(1L))
    }

    @Test
    fun containsKeyReturnsTrueAfterPutWhileLookupOfOtherKeyReturnsNull() {
        val map = Long2AnyHashMap<String>()
        map.putValue(1L, "a")
        assertTrue(map.containsKey(1L))
        assertNull(map.lookup(99L))
        assertFalse(map.containsKey(99L))
    }
}
