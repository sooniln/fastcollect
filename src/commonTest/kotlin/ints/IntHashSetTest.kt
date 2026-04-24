package io.github.sooniln.fastcollect.ints

import kotlin.collections.removeAll
import kotlin.collections.retainAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class IntHashSetTest {

    // --- construction & size ---

    @Test
    fun emptySetHasSizeZero() {
        val set = IntHashSet()
        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }

    @Test
    fun constructWithCapacity() {
        val set = IntHashSet(100)
        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }

    @Test
    fun constructFromCollection() {
        val set = IntHashSet(listOf(1, 2, 3))
        assertEquals(3, set.size)
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    fun constructFromCollectionDeduplicates() {
        val set = IntHashSet(listOf(1, 1, 2, 2))
        assertEquals(2, set.size)
    }

    @Test
    fun negativeCapacityThrows() {
        assertFailsWith<IllegalArgumentException> { IntHashSet(-1) }
    }

    @Test
    fun invalidLoadFactorZeroThrows() {
        assertFailsWith<IllegalArgumentException> { IntHashSet(4, 0f) }
    }

    @Test
    fun invalidLoadFactorOneThrows() {
        assertFailsWith<IllegalArgumentException> { IntHashSet(4, 1f) }
    }

    @Test
    fun invalidLoadFactorNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { IntHashSet(4, -0.5f) }
    }

    // --- add ---

    @Test
    fun addReturnsTrueForNewElement() {
        val set = IntHashSet()
        assertTrue(set.add(42))
    }

    @Test
    fun addReturnsFalseForDuplicate() {
        val set = IntHashSet()
        set.add(42)
        assertFalse(set.add(42))
    }

    @Test
    fun addIncreasesSize() {
        val set = IntHashSet()
        set.add(1)
        set.add(2)
        set.add(3)
        assertEquals(3, set.size)
    }

    @Test
    fun addDuplicateDoesNotIncreaseSize() {
        val set = IntHashSet()
        set.add(7)
        set.add(7)
        assertEquals(1, set.size)
    }

    // --- zero handling ---

    @Test
    fun addZeroReturnsTrueOnFirstAdd() {
        val set = IntHashSet()
        assertTrue(set.add(0))
    }

    @Test
    fun addZeroReturnsFalseOnSecondAdd() {
        val set = IntHashSet()
        set.add(0)
        assertFalse(set.add(0))
    }

    @Test
    fun containsZeroAfterAdd() {
        val set = IntHashSet()
        set.add(0)
        assertTrue(set.contains(0))
    }

    @Test
    fun removeZeroReturnsTrueWhenPresent() {
        val set = IntHashSet()
        set.add(0)
        assertTrue(set.remove(0))
        assertFalse(set.contains(0))
    }

    @Test
    fun removeZeroReturnsFalseWhenAbsent() {
        val set = IntHashSet()
        assertFalse(set.remove(0))
    }

    @Test
    fun zeroCountedInSize() {
        val set = IntHashSet()
        set.add(0)
        set.add(1)
        assertEquals(2, set.size)
    }

    @Test
    fun zeroIteratedCorrectly() {
        val set = IntHashSet()
        set.add(0)
        set.add(1)
        set.add(2)
        val contents = set.toSet()
        assertEquals(setOf(0, 1, 2), contents)
    }

    // --- remove ---

    @Test
    fun removeReturnsTrueForPresentElement() {
        val set = IntHashSet()
        set.add(5)
        assertTrue(set.remove(5))
    }

    @Test
    fun removeReturnsFalseForAbsentElement() {
        val set = IntHashSet()
        assertFalse(set.remove(99))
    }

    @Test
    fun removeDecreasesSize() {
        val set = IntHashSet()
        set.add(1)
        set.add(2)
        set.remove(1)
        assertEquals(1, set.size)
    }

    @Test
    fun removedElementNoLongerContained() {
        val set = IntHashSet()
        set.add(10)
        set.remove(10)
        assertFalse(set.contains(10))
    }

    @Test
    fun removeFromEmptySetReturnsFalse() {
        assertFalse(IntHashSet().remove(1))
    }

    // --- contains ---

    @Test
    fun containsReturnsTrueForPresentElement() {
        val set = IntHashSet()
        set.add(7)
        assertTrue(set.contains(7))
    }

    @Test
    fun containsReturnsFalseForAbsentElement() {
        val set = IntHashSet()
        assertFalse(set.contains(7))
    }

    @Test
    fun containsAfterRemoveReturnsFalse() {
        val set = IntHashSet()
        set.add(3)
        set.remove(3)
        assertFalse(set.contains(3))
    }

    // --- clear ---

    @Test
    fun clearEmptiesSet() {
        val set = IntHashSet()
        set.add(1); set.add(2); set.add(3)
        set.clear()
        assertEquals(0, set.size)
        assertTrue(set.isEmpty())
    }

    @Test
    fun clearRemovesZero() {
        val set = IntHashSet()
        set.add(0); set.add(1)
        set.clear()
        assertFalse(set.contains(0))
        assertEquals(0, set.size)
    }

    @Test
    fun addAfterClearWorks() {
        val set = IntHashSet()
        set.add(1); set.add(2)
        set.clear()
        set.add(3)
        assertEquals(1, set.size)
        assertTrue(set.contains(3))
    }

    // --- iterator ---

    @Test
    fun iteratorTraversesAllElements() {
        val set = IntHashSet()
        set.add(1); set.add(2); set.add(3)
        assertEquals(setOf(1, 2, 3), set.toSet())
    }

    @Test
    fun iteratorOnEmptySetHasNoNext() {
        assertFalse(IntHashSet().iterator().hasNext())
    }

    @Test
    fun iteratorNextOnEmptySetThrows() {
        assertFailsWith<NoSuchElementException> { IntHashSet().iterator().nextInt() }
    }

    @Test
    fun iteratorRemoveWorks() {
        val set = IntHashSet()
        set.add(1); set.add(2); set.add(3)
        val iter = set.iterator()
        val removed = iter.nextInt()
        iter.remove()
        assertFalse(set.contains(removed))
        assertEquals(2, set.size)
    }

    @Test
    fun iteratorCountMatchesSize() {
        val set = IntHashSet()
        for (i in 1..10) set.add(i)
        var count = 0
        val iter = set.iterator()
        while (iter.hasNext()) { iter.nextInt(); count++ }
        assertEquals(set.size, count)
    }

    // --- addAll ---

    @Test
    fun addAllFromCollectionAddsNewElements() {
        val set = IntHashSet()
        val modified = set.addAll(listOf(1, 2, 3))
        assertTrue(modified)
        assertEquals(setOf(1, 2, 3), set.toSet())
    }

    @Test
    fun addAllFromCollectionReturnsFalseWhenNoDuplicates() {
        val set = IntHashSet()
        set.add(1); set.add(2)
        val modified = set.addAll(listOf(1, 2))
        assertFalse(modified)
    }

    @Test
    fun addAllFromIntCollectionAddsNewElements() {
        val set = IntHashSet()
        val src = mutableIntSetOf(4, 5, 6)
        val modified = set.addAll(src)
        assertTrue(modified)
        assertEquals(setOf(4, 5, 6), set.toSet())
    }

    @Test
    fun addAllEmptySourceIsNoOp() {
        val set = IntHashSet()
        set.add(1)
        val modified = set.addAll(emptyList())
        assertFalse(modified)
        assertEquals(1, set.size)
    }

    // --- removeAll ---

    @Test
    fun removeAllRemovesMatchingElements() {
        val set = IntHashSet()
        for (i in 1..5) set.add(i)
        val modified = set.removeAll(listOf(2, 4))
        assertTrue(modified)
        assertEquals(setOf(1, 3, 5), set.toSet())
    }

    @Test
    fun removeAllReturnsFalseWhenNothingRemoved() {
        val set = IntHashSet()
        set.add(1); set.add(2)
        val modified = set.removeAll(listOf(99, 100))
        assertFalse(modified)
        assertEquals(2, set.size)
    }

    @Test
    fun removeAllByPredicateWorks() {
        val set = IntHashSet()
        for (i in 1..6) set.add(i)
        set.removeAll { it % 2 == 0 }
        assertEquals(setOf(1, 3, 5), set.toSet())
    }

    @Test
    fun removeAllByPredicateRemovesZero() {
        val set = IntHashSet()
        set.add(0); set.add(1); set.add(2)
        set.removeAll { it % 2 == 0 }
        assertEquals(setOf(1), set.toSet())
    }

    // --- retainAll ---

    @Test
    fun retainAllKeepsOnlyMatchingElements() {
        val set = IntHashSet()
        for (i in 1..5) set.add(i)
        val modified = set.retainAll(listOf(2, 4))
        assertTrue(modified)
        assertEquals(setOf(2, 4), set.toSet())
    }

    @Test
    fun retainAllReturnsFalseWhenNothingRemoved() {
        val set = IntHashSet()
        set.add(1); set.add(2)
        val modified = set.retainAll(listOf(1, 2, 3))
        assertFalse(modified)
        assertEquals(2, set.size)
    }

    @Test
    fun retainAllByPredicateWorks() {
        val set = IntHashSet()
        for (i in 1..6) set.add(i)
        set.retainAll { it % 2 == 0 }
        assertEquals(setOf(2, 4, 6), set.toSet())
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = IntHashSet(listOf(1, 2, 3))
        val b = IntHashSet(listOf(1, 2, 3))
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentContent() {
        assertNotEquals(IntHashSet(listOf(1, 2)), IntHashSet(listOf(1, 3)))
    }

    @Test
    fun notEqualsWithDifferentSize() {
        assertNotEquals(IntHashSet(listOf(1, 2)), IntHashSet(listOf(1)))
    }

    @Test
    fun equalsWithStandardSet() {
        val set = IntHashSet(listOf(1, 2, 3))
        assertEquals(setOf(1, 2, 3), set)
    }

    @Test
    fun hashCodeConsistentWithEqualSets() {
        val a = IntHashSet(listOf(10, 20, 30))
        val b = IntHashSet(listOf(10, 20, 30))
        assertEquals(a.hashCode(), b.hashCode())
    }

    // --- ensureCapacity ---

    @Test
    fun ensureCapacityDoesNotLoseData() {
        val set = IntHashSet()
        for (i in 1..20) set.add(i)
        set.ensureCapacity(200)
        assertEquals(20, set.size)
        for (i in 1..20) assertTrue(set.contains(i))
    }

    @Test
    fun ensureCapacityNegativeThrows() {
        assertFailsWith<IllegalArgumentException> { IntHashSet().ensureCapacity(-1) }
    }

    // --- negative numbers ---

    @Test
    fun negativeNumbersStoredCorrectly() {
        val set = IntHashSet()
        set.add(-1); set.add(-100); set.add(-Int.MAX_VALUE)
        assertEquals(3, set.size)
        assertTrue(set.contains(-1))
        assertTrue(set.contains(-100))
        assertTrue(set.contains(-Int.MAX_VALUE))
    }

    @Test
    fun removeNegativeNumber() {
        val set = IntHashSet()
        set.add(-5)
        assertTrue(set.remove(-5))
        assertFalse(set.contains(-5))
    }

    // --- mutableIntSetOf factory ---

    @Test
    fun mutableIntSetOfVarargWorks() {
        val set = mutableIntSetOf(1, 2, 3)
        assertEquals(setOf(1, 2, 3), set.toSet())
    }

    @Test
    fun mutableIntSetOfEmptyWorks() {
        val set = mutableIntSetOf()
        assertTrue(set.isEmpty())
    }
}
