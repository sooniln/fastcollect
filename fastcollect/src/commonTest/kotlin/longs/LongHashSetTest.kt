package io.github.sooniln.fastcollect.longs

import kotlin.collections.removeAll
import kotlin.collections.retainAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class LongHashSetTest {

    // --- construction & size ---

    @Test
    fun emptySetHasSizeZero() {
        val set = LongHashSet()
        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val set = LongHashSet(100)
        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }

    @Test
    fun constructFromCollection() {
        val set = LongHashSet(listOf(1L, 2L, 3L))
        assertEquals(3, set.size)
        assertTrue(set.contains(1L))
        assertTrue(set.contains(2L))
        assertTrue(set.contains(3L))
    }

    @Test
    fun constructFromCollectionDeduplicates() {
        val set = LongHashSet(listOf(1L, 1L, 2L, 2L))
        assertEquals(2, set.size)
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { LongHashSet(-1) }
    }

    // --- add ---

    @Test
    fun addReturnsTrueForNewElement() {
        val set = LongHashSet()
        assertTrue(set.add(42L))
    }

    @Test
    fun addReturnsFalseForDuplicate() {
        val set = LongHashSet()
        set.add(42L)
        assertFalse(set.add(42L))
    }

    @Test
    fun addIncreasesSize() {
        val set = LongHashSet()
        set.add(1L)
        set.add(2L)
        set.add(3L)
        assertEquals(3, set.size)
    }

    @Test
    fun addDuplicateDoesNotIncreaseSize() {
        val set = LongHashSet()
        set.add(7L)
        set.add(7L)
        assertEquals(1, set.size)
    }

    // --- zero handling ---

    @Test
    fun addZeroReturnsTrueOnFirstAdd() {
        val set = LongHashSet()
        assertTrue(set.add(0L))
    }

    @Test
    fun addZeroReturnsFalseOnSecondAdd() {
        val set = LongHashSet()
        set.add(0L)
        assertFalse(set.add(0L))
    }

    @Test
    fun containsZeroAfterAdd() {
        val set = LongHashSet()
        set.add(0L)
        assertTrue(set.contains(0L))
    }

    @Test
    fun removeZeroReturnsTrueWhenPresent() {
        val set = LongHashSet()
        set.add(0L)
        assertTrue(set.remove(0L))
        assertFalse(set.contains(0L))
    }

    @Test
    fun removeZeroReturnsFalseWhenAbsent() {
        val set = LongHashSet()
        assertFalse(set.remove(0L))
    }

    @Test
    fun zeroCountedInSize() {
        val set = LongHashSet()
        set.add(0L)
        set.add(1L)
        assertEquals(2, set.size)
    }

    @Test
    fun zeroIteratedCorrectly() {
        val set = LongHashSet()
        set.add(0L)
        set.add(1L)
        set.add(2L)
        val contents = set.toSet()
        assertEquals(setOf(0L, 1L, 2L), contents)
    }

    // --- remove ---

    @Test
    fun removeReturnsTrueForPresentElement() {
        val set = LongHashSet()
        set.add(5L)
        assertTrue(set.remove(5L))
    }

    @Test
    fun removeReturnsFalseForAbsentElement() {
        val set = LongHashSet()
        assertFalse(set.remove(99L))
    }

    @Test
    fun removeDecreasesSize() {
        val set = LongHashSet()
        set.add(1L)
        set.add(2L)
        set.remove(1L)
        assertEquals(1, set.size)
    }

    @Test
    fun removedElementNoLongerContained() {
        val set = LongHashSet()
        set.add(10L)
        set.remove(10L)
        assertFalse(set.contains(10L))
    }

    @Test
    fun removeFromEmptySetReturnsFalse() {
        assertFalse(LongHashSet().remove(1L))
    }

    @Test
    fun removePreservesAllOtherElements() {
        val set = LongHashSet()
        for (i in 1..100) set.add(i.toLong())
        for (i in 1..100) {
            set.remove(i.toLong())
            for (j in i + 1..100) {
                assertTrue(set.contains(j.toLong()), "After removing $i, set no longer contains $j")
            }
        }
    }

    // --- contains ---

    @Test
    fun containsReturnsTrueForPresentElement() {
        val set = LongHashSet()
        set.add(7L)
        assertTrue(set.contains(7L))
    }

    @Test
    fun containsReturnsFalseForAbsentElement() {
        val set = LongHashSet()
        assertFalse(set.contains(7L))
    }

    @Test
    fun containsAfterRemoveReturnsFalse() {
        val set = LongHashSet()
        set.add(3L)
        set.remove(3L)
        assertFalse(set.contains(3L))
    }

    // --- clear ---

    @Test
    fun clearEmptiesSet() {
        val set = LongHashSet()
        set.add(1L); set.add(2L); set.add(3L)
        set.clear()
        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }

    @Test
    fun clearRemovesZero() {
        val set = LongHashSet()
        set.add(0L); set.add(1L)
        set.clear()
        assertFalse(set.contains(0L))
        assertEquals(0, set.size)
    }

    @Test
    fun addAfterClearWorks() {
        val set = LongHashSet()
        set.add(1L); set.add(2L)
        set.clear()
        set.add(3L)
        assertEquals(1, set.size)
        assertTrue(set.contains(3L))
    }

    // --- iterator ---

    @Test
    fun iteratorTraversesAllElements() {
        val set = LongHashSet()
        set.add(1L); set.add(2L); set.add(3L)
        assertEquals(setOf(1L, 2L, 3L), set.toSet())
    }

    @Test
    fun iteratorOnEmptySetHasNoNext() {
        assertFalse(LongHashSet().iterator().hasNext())
    }

    @Test
    fun iteratorNextOnEmptySetThrows() {
        assertFailsWith<NoSuchElementException> { LongHashSet().iterator().nextLong() }
    }

    @Test
    fun iteratorRemoveWorks() {
        val set = LongHashSet()
        set.add(1L); set.add(2L); set.add(3L)
        val iter = set.iterator()
        val removed = iter.nextLong()
        iter.remove()
        assertFalse(set.contains(removed))
        assertEquals(2, set.size)
    }

    @Test
    fun iteratorCountMatchesSize() {
        val set = LongHashSet()
        for (i in 1..10) set.add(i.toLong())
        var count = 0
        val iter = set.iterator()
        while (iter.hasNext()) { iter.nextLong(); count++ }
        assertEquals(set.size, count)
    }

    // --- addAll ---

    @Test
    fun addAllFromCollectionAddsNewElements() {
        val set = LongHashSet()
        val modified = set.addAll(listOf(1L, 2L, 3L))
        assertTrue(modified)
        assertEquals(setOf(1L, 2L, 3L), set.toSet())
    }

    @Test
    fun addAllFromCollectionReturnsFalseWhenNoDuplicates() {
        val set = LongHashSet()
        set.add(1L); set.add(2L)
        val modified = set.addAll(listOf(1L, 2L))
        assertFalse(modified)
    }

    @Test
    fun addAllFromLongCollectionAddsNewElements() {
        val set = LongHashSet()
        val src = mutableLongSetOf(4L, 5L, 6L)
        val modified = set.addAll(src)
        assertTrue(modified)
        assertEquals(setOf(4L, 5L, 6L), set.toSet())
    }

    @Test
    fun addAllEmptySourceIsNoOp() {
        val set = LongHashSet()
        set.add(1L)
        val modified = set.addAll(emptyList())
        assertFalse(modified)
        assertEquals(1, set.size)
    }

    // --- removeAll ---

    @Test
    fun removeAllRemovesMatchingElements() {
        val set = LongHashSet()
        for (i in 1..5) set.add(i.toLong())
        val modified = set.removeAll(listOf(2L, 4L))
        assertTrue(modified)
        assertEquals(setOf(1L, 3L, 5L), set.toSet())
    }

    @Test
    fun removeAllReturnsFalseWhenNothingRemoved() {
        val set = LongHashSet()
        set.add(1L); set.add(2L)
        val modified = set.removeAll(listOf(99L, 100L))
        assertFalse(modified)
        assertEquals(2, set.size)
    }

    @Test
    fun removeAllByPredicateWorks() {
        val set = LongHashSet()
        for (i in 1..6) set.add(i.toLong())
        set.removeAll { it % 2L == 0L }
        assertEquals(setOf(1L, 3L, 5L), set.toSet())
    }

    @Test
    fun removeAllByPredicateRemovesZero() {
        val set = LongHashSet()
        set.add(0L); set.add(1L); set.add(2L)
        set.removeAll { it % 2L == 0L }
        assertEquals(setOf(1L), set.toSet())
    }

    // --- retainAll ---

    @Test
    fun retainAllKeepsOnlyMatchingElements() {
        val set = LongHashSet()
        for (i in 1..5) set.add(i.toLong())
        val modified = set.retainAll(listOf(2L, 4L))
        assertTrue(modified)
        assertEquals(setOf(2L, 4L), set.toSet())
    }

    @Test
    fun retainAllReturnsFalseWhenNothingRemoved() {
        val set = LongHashSet()
        set.add(1L); set.add(2L)
        val modified = set.retainAll(listOf(1L, 2L, 3L))
        assertFalse(modified)
        assertEquals(2, set.size)
    }

    @Test
    fun retainAllByPredicateWorks() {
        val set = LongHashSet()
        for (i in 1..6) set.add(i.toLong())
        set.retainAll { it % 2L == 0L }
        assertEquals(setOf(2L, 4L, 6L), set.toSet())
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = LongHashSet(listOf(1L, 2L, 3L))
        val b = LongHashSet(listOf(1L, 2L, 3L))
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentContent() {
        assertNotEquals(LongHashSet(listOf(1L, 2L)), LongHashSet(listOf(1L, 3L)))
    }

    @Test
    fun notEqualsWithDifferentSize() {
        assertNotEquals(LongHashSet(listOf(1L, 2L)), LongHashSet(listOf(1L)))
    }

    @Test
    fun equalsWithStandardSet() {
        val set = LongHashSet(listOf(1L, 2L, 3L))
        assertEquals(setOf(1L, 2L, 3L), set)
    }

    @Test
    fun hashCodeConsistentWithEqualSets() {
        val a = LongHashSet(listOf(10L, 20L, 30L))
        val b = LongHashSet(listOf(10L, 20L, 30L))
        assertEquals(a.hashCode(), b.hashCode())
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityDoesNotLoseData() {
        val set = LongHashSet()
        for (i in 1..20) set.add(i.toLong())
        set.ensureCapacity(200)
        assertEquals(20, set.size)
        for (i in 1..20) assertTrue(set.contains(i.toLong()))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { LongHashSet().ensureCapacity(-1) }
    }

    // --- negative numbers ---

    @Test
    fun negativeNumbersStoredCorrectly() {
        val set = LongHashSet()
        set.add(-1L); set.add(-100L); set.add(-Long.MAX_VALUE)
        assertEquals(3, set.size)
        assertTrue(set.contains(-1L))
        assertTrue(set.contains(-100L))
        assertTrue(set.contains(-Long.MAX_VALUE))
    }

    @Test
    fun removeNegativeNumber() {
        val set = LongHashSet()
        set.add(-5L)
        assertTrue(set.remove(-5L))
        assertFalse(set.contains(-5L))
    }

    // --- mutableLongSetOf factory ---

    @Test
    fun mutableLongSetOfVarargWorks() {
        val set = mutableLongSetOf(1L, 2L, 3L)
        assertEquals(setOf(1L, 2L, 3L), set.toSet())
    }

    @Test
    fun mutableLongSetOfEmptyWorks() {
        val set = mutableLongSetOf()
        assertTrue(set.isEmpty())
    }
}
