package io.github.sooniln.fastcollect.ints

import kotlin.collections.removeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class IntArrayDequeTest {

    // --- construction & size ---

    @Test
    fun emptyDequeHasSizeZero() {
        val deque = IntArrayDeque()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun constructFromIntArray() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        assertEquals(3, deque.size)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    @Test
    fun constructFromIntCollection() {
        val source = mutableIntListOf(4, 5, 6)
        val deque = IntArrayDeque(source)
        assertEquals(listOf(4, 5, 6), deque.toList())
    }

    // --- addFirst / addLast ---

    @Test
    fun addLastAppendsToEnd() {
        val deque = IntArrayDeque()
        deque.addLast(1)
        deque.addLast(2)
        deque.addLast(3)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    @Test
    fun addFirstPrependsToFront() {
        val deque = IntArrayDeque()
        deque.addFirst(3)
        deque.addFirst(2)
        deque.addFirst(1)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    @Test
    fun addFirstAndLastInterleaved() {
        val deque = IntArrayDeque()
        deque.addLast(2)
        deque.addFirst(1)
        deque.addLast(3)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    // --- removeFirst / removeLast ---

    @Test
    fun removeFirstReturnsFrontElement() {
        val deque = IntArrayDeque()
        deque.addLast(10)
        deque.addLast(20)
        assertEquals(10, deque.removeFirst())
        assertEquals(listOf(20), deque.toList())
    }

    @Test
    fun removeLastReturnsBackElement() {
        val deque = IntArrayDeque()
        deque.addLast(10)
        deque.addLast(20)
        assertEquals(20, deque.removeLast())
        assertEquals(listOf(10), deque.toList())
    }

    @Test
    fun removeFirstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { IntArrayDeque().removeFirst() }
    }

    @Test
    fun removeLastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { IntArrayDeque().removeLast() }
    }

    @Test
    fun addFirstThenRemoveFirstIsLifo() {
        val deque = IntArrayDeque()
        for (i in 1..5) deque.addFirst(i)
        val result = mutableListOf<Int>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(5, 4, 3, 2, 1), result)
    }

    @Test
    fun addLastThenRemoveFirstIsFifo() {
        val deque = IntArrayDeque()
        for (i in 1..5) deque.addLast(i)
        val result = mutableListOf<Int>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    // --- first / last ---

    @Test
    fun firstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { IntArrayDeque().first() }
    }

    @Test
    fun lastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { IntArrayDeque().last() }
    }

    @Test
    fun firstAndLastReturnCorrectElements() {
        val deque = IntArrayDeque(intArrayOf(10, 20, 30))
        assertEquals(10, deque.first())
        assertEquals(30, deque.last())
    }

    // --- get / set ---

    @Test
    fun getReturnsCorrectElement() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        assertEquals(2, deque.getAt(1))
    }

    @Test
    fun setReplacesElement() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.setAt(1, 99)
        assertEquals(listOf(1, 99, 3), deque.toList())
    }

    @Test
    fun getOutOfBoundsThrows() {
        val deque = IntArrayDeque(intArrayOf(1))
        assertFailsWith<IndexOutOfBoundsException> { deque.getAt(5) }
    }

    @Test
    fun setOutOfBoundsThrows() {
        val deque = IntArrayDeque(intArrayOf(1))
        assertFailsWith<IndexOutOfBoundsException> { deque.setAt(5, 99) }
    }

    // --- add(index) / removeAt ---

    @Test
    fun addAtMiddle() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 4, 5))
        deque.add(2, 3)
        assertEquals(listOf(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun addAtLeft() {
        val deque = IntArrayDeque(intArrayOf(1, 3, 4, 5))
        deque.add(1, 2)
        assertEquals(listOf(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun addAtRight() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3, 5))
        deque.add(3, 4)
        assertEquals(listOf(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun addAtFront() {
        val deque = IntArrayDeque(intArrayOf(2, 3))
        deque.add(0, 1)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    @Test
    fun addAtEnd() {
        val deque = IntArrayDeque(intArrayOf(1, 2))
        deque.add(2, 3)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    @Test
    fun removeAtMiddle() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5))
        val removed = deque.removeAt(2)
        assertEquals(3, removed)
        assertEquals(listOf(1, 2, 4, 5), deque.toList())
    }

    @Test
    fun removeAtLeft() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5))
        val removed = deque.removeAt(1)
        assertEquals(2, removed)
        assertEquals(listOf(1, 3, 4, 5), deque.toList())
    }

    @Test
    fun removeAtRight() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5))
        val removed = deque.removeAt(3)
        assertEquals(4, removed)
        assertEquals(listOf(1, 2, 3, 5), deque.toList())
    }

    @Test
    fun removeAtFront() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.removeAt(0)
        assertEquals(listOf(2, 3), deque.toList())
    }

    @Test
    fun removeAtBack() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.removeAt(2)
        assertEquals(listOf(1, 2), deque.toList())
    }

    @Test
    fun removeAtOutOfBoundsThrows() {
        assertFailsWith<IndexOutOfBoundsException> { IntArrayDeque().removeAt(0) }
    }

    // --- removeRange ---

    @Test
    fun removeRangeMiddle() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5))
        deque.removeRange(1, 3)
        assertEquals(listOf(1, 4, 5), deque.toList())
    }

    @Test
    fun removeRangeToEnd() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5))
        deque.removeRange(2, 5)
        assertEquals(listOf(1, 2), deque.toList())
    }

    @Test
    fun removeRangeFull() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.removeRange(0, 3)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun removeRangeEmptyIsNoOp() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.removeRange(2, 2)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    @Test
    fun removeRangeAtEndIsNoOp() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.removeRange(3, 3)
        assertEquals(listOf(1, 2, 3), deque.toList())
    }

    @Test
    fun removeRangeInvalidThrows() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        assertFailsWith<IllegalArgumentException> { deque.removeRange(2, 1) }
    }

    // --- clear ---

    @Test
    fun clearEmptiesDeque() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.clear()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    // --- indexOf / lastIndexOf ---

    @Test
    fun indexOfReturnsFirstOccurrence() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 1))
        assertEquals(0, deque.indexOf(1))
    }

    @Test
    fun indexOfReturnsMinus1WhenAbsent() {
        assertEquals(-1, IntArrayDeque(intArrayOf(1, 2)).indexOf(99))
    }

    @Test
    fun lastIndexOfReturnsLastOccurrence() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 1))
        assertEquals(2, deque.lastIndexOf(1))
    }

    @Test
    fun indexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = IntArrayDeque()
        deque.addLast(1); deque.addLast(2); deque.addLast(3)
        deque.removeFirst()                  // head shifts: logical = [2, 3]
        assertEquals(1, deque.indexOf(3))    // logical 1, not physical 2
    }

    @Test
    fun lastIndexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = IntArrayDeque()
        deque.addLast(1); deque.addLast(2); deque.addLast(1)
        deque.removeFirst()                       // head shifts: logical = [2, 1]
        assertEquals(1, deque.lastIndexOf(1))     // logical 1, not physical 2
    }

    // --- contains ---

    @Test
    fun containsFindsExistingElement() {
        val deque = IntArrayDeque(intArrayOf(7))
        assertTrue(deque.contains(7))
        assertFalse(deque.contains(8))
    }

    // --- removeAll / retainAll ---

    @Test
    fun removeAllPredicateCorrectWhenWrapped() {
        // Force a physical wrap: fill 8, drain 6 (head=6), add 4 more so tail wraps to index 2.
        // Logical = [7, 8, 9, 10, 11, 12] with head at physical 6 and tail at physical 2.
        val deque = IntArrayDeque()
        for (i in 1..8) deque.addLast(i)
        repeat(6) { deque.removeFirst() }
        deque.addLast(9); deque.addLast(10); deque.addLast(11); deque.addLast(12)
        deque.removeAll { it % 2 == 0 }
        assertEquals(listOf(7, 9, 11), deque.toList())
    }

    // --- addAll ---

    @Test
    fun addAllDequeAppendsToEmpty() {
        val dest = IntArrayDeque()
        dest.addAll(IntArrayDeque(intArrayOf(1, 2, 3)))
        assertEquals(listOf(1, 2, 3), dest.toList())
    }

    @Test
    fun addAllDequeAppendsToNonEmpty() {
        val dest = IntArrayDeque(intArrayOf(1, 2))
        dest.addAll(IntArrayDeque(intArrayOf(3, 4)))
        assertEquals(listOf(1, 2, 3, 4), dest.toList())
    }

    @Test
    fun addAllDequeEmptySourceIsNoOp() {
        val dest = IntArrayDeque(intArrayOf(1, 2))
        val modified = dest.addAll(IntArrayDeque())
        assertFalse(modified)
        assertEquals(listOf(1, 2), dest.toList())
    }

    @Test
    fun addAllDequeSourceWrapped() {
        // Source deque is physically wrapped: fill 8, drain 6 (head=6), add 4 → logical [7,8,9,10], wrapped.
        val src = IntArrayDeque()
        for (i in 1..8) src.addLast(i)
        repeat(6) { src.removeFirst() }
        src.addLast(9); src.addLast(10)
        val dest = IntArrayDeque(intArrayOf(1, 2, 3))
        dest.addAll(src)
        assertEquals(listOf(1, 2, 3, 7, 8, 9, 10), dest.toList())
    }

    @Test
    fun addAllDequeDestWrapped() {
        // Dest deque has head > 0: fill 4, drain 2 (head=2), logical [3,4].
        val dest = IntArrayDeque()
        for (i in 1..4) dest.addLast(i)
        repeat(2) { dest.removeFirst() }
        dest.addAll(IntArrayDeque(intArrayOf(5, 6, 7)))
        assertEquals(listOf(3, 4, 5, 6, 7), dest.toList())
    }

    @Test
    fun addAllDequeBothWrapped() {
        // Source wrapped: fill 8, drain 6, add 4 → logical [7,8,9,10].
        val src = IntArrayDeque()
        for (i in 1..8) src.addLast(i)
        repeat(6) { src.removeFirst() }
        src.addLast(9); src.addLast(10)
        // Dest wrapped: fill 4, drain 2 → logical [3,4], head=2.
        val dest = IntArrayDeque()
        for (i in 1..4) dest.addLast(i)
        repeat(2) { dest.removeFirst() }
        dest.addAll(src)
        assertEquals(listOf(3, 4, 7, 8, 9, 10), dest.toList())
    }

    @Test
    fun addAllDequeSelfDoublesContent() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        deque.addAll(deque)
        assertEquals(listOf(1, 2, 3, 1, 2, 3), deque.toList())
    }

    // --- ensureCapacity / trimToSize ---

    @Test
    fun ensureCapacityDoesNotLoseData() {
        val deque = IntArrayDeque()
        for (i in 0 until 100) deque.addLast(i)
        deque.ensureCapacity(200)
        assertEquals(100, deque.size)
        for (i in 0 until 100) assertEquals(i, deque.getAt(i))
    }

    @Test
    fun trimToSizePreservesElements() {
        val deque = IntArrayDeque()
        for (i in 0 until 50) deque.addLast(i)
        deque.ensureCapacity(200)
        deque.trimToSize()
        assertEquals(50, deque.size)
        for (i in 0 until 50) assertEquals(i, deque.getAt(i))
    }

    @Test
    fun trimToSizeOnEmptyDequeDoesNotThrow() {
        IntArrayDeque().trimToSize()
    }

    // --- toIntArray ---

    @Test
    fun toIntArrayReturnsCorrectElements() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3))
        assertEquals(intArrayOf(1, 2, 3).toList(), deque.toIntArray().toList())
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = IntArrayDeque(intArrayOf(1, 2, 3))
        val b = IntArrayDeque(intArrayOf(1, 2, 3))
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentContent() {
        assertNotEquals(IntArrayDeque(intArrayOf(1)), IntArrayDeque(intArrayOf(2)))
    }

    @Test
    fun equalsWithStandardList() {
        assertEquals(IntArrayDeque(intArrayOf(1, 2, 3)), listOf(1, 2, 3))
    }

    @Test
    fun hashCodeConsistentWithEqualDeques() {
        val a = IntArrayDeque(intArrayOf(10))
        val b = IntArrayDeque(intArrayOf(10))
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun hashCodeConsistentAfterHeadShifts() {
        // head=0 deque and head>0 deque with identical logical content must agree.
        val fresh = IntArrayDeque(intArrayOf(2, 3))   // head=0
        val shifted = IntArrayDeque()
        shifted.addLast(1); shifted.addLast(2); shifted.addLast(3)
        shifted.removeFirst()                          // head=1, logical=[2,3]
        assertEquals(fresh.hashCode(), shifted.hashCode())
    }

    // --- iterator ---

    @Test
    fun iteratorTraversesAllElements() {
        val deque = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5))
        assertEquals(listOf(1, 2, 3, 4, 5), deque.toList())
    }

    // --- ring-buffer correctness after wrap ---

    @Test
    fun correctAfterHeadWrapsAround() {
        val deque = IntArrayDeque()
        for (i in 1..5) deque.addLast(i)
        repeat(3) { deque.removeFirst() }       // head now at 3 (wrapped)
        deque.addLast(6)
        deque.addLast(7)
        deque.addFirst(0)
        assertEquals(listOf(0, 4, 5, 6, 7), deque.toList())
    }

    @Test
    fun toIntArrayCorrectAfterWrap() {
        val deque = IntArrayDeque()
        for (i in 1..5) deque.addLast(i)
        repeat(3) { deque.removeFirst() }
        deque.addLast(6); deque.addLast(7)
        assertEquals(listOf(4, 5, 6, 7), deque.toIntArray().toList())
    }

    @Test
    fun addAtIndexCorrectAfterWrap() {
        val deque = IntArrayDeque()
        for (i in 1..4) deque.addLast(i)
        repeat(2) { deque.removeFirst() }       // logical: [3, 4], head wrapped
        deque.add(1, 99)
        assertEquals(listOf(3, 99, 4), deque.toList())
    }

    @Test
    fun removeAtCorrectAfterWrap() {
        val deque = IntArrayDeque()
        for (i in 1..4) deque.addLast(i)
        repeat(2) { deque.removeFirst() }       // logical: [3, 4]
        deque.addLast(5); deque.addLast(6)      // logical: [3, 4, 5, 6], wraps
        deque.removeAt(1)
        assertEquals(listOf(3, 5, 6), deque.toList())
    }

    @Test
    fun removeRangeCorrectAfterWrap() {
        val deque = IntArrayDeque()
        for (i in 1..6) deque.addLast(i)
        repeat(3) { deque.removeFirst() }       // logical: [4, 5, 6]
        deque.addLast(7); deque.addLast(8)      // logical: [4, 5, 6, 7, 8], wraps
        deque.removeRange(1, 3)
        assertEquals(listOf(4, 7, 8), deque.toList())
    }

    // --- sort / sortDescending ---

    @Test
    fun sortNonWrapped() {
        val deque = IntArrayDeque(intArrayOf(5, 3, 1, 4, 2))
        deque.sort()
        assertEquals(listOf(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun sortDescendingNonWrapped() {
        val deque = IntArrayDeque(intArrayOf(3, 1, 4, 2, 5))
        deque.sortDescending()
        assertEquals(listOf(5, 4, 3, 2, 1), deque.toList())
    }

    @Test
    fun sortWrappedHeadSegmentLarger() {
        // ring.size=8, head=5, size=5, tail=10 → end=2, ring.size-head=3 > end (case 1)
        val deque = IntArrayDeque()
        for (i in 1..8) deque.addLast(i)
        repeat(5) { deque.removeFirst() }   // head=5, logical=[6,7,8]
        deque.addLast(4); deque.addLast(2)  // logical=[6,7,8,4,2], tail wraps
        deque.sort()
        assertEquals(listOf(2, 4, 6, 7, 8), deque.toList())
    }

    @Test
    fun sortWrappedTailSegmentLarger() {
        // ring.size=8, head=6, size=5, tail=11 → end=3, ring.size-head=2 ≤ end (case 2)
        val deque = IntArrayDeque()
        for (i in 1..8) deque.addLast(i)
        repeat(6) { deque.removeFirst() }        // head=6, logical=[7,8]
        deque.addLast(3); deque.addLast(1); deque.addLast(5)  // logical=[7,8,3,1,5], tail wraps
        deque.sort()
        assertEquals(listOf(1, 3, 5, 7, 8), deque.toList())
    }

    @Test
    fun sortDescendingWrapped() {
        // Same wrapped state as sortWrappedTailSegmentLarger, using sortDescending
        val deque = IntArrayDeque()
        for (i in 1..8) deque.addLast(i)
        repeat(6) { deque.removeFirst() }
        deque.addLast(3); deque.addLast(1); deque.addLast(5)  // logical=[7,8,3,1,5]
        deque.sortDescending()
        assertEquals(listOf(8, 7, 5, 3, 1), deque.toList())
    }
}
