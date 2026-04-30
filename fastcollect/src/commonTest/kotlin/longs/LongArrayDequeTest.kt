package io.github.sooniln.fastcollect.longs

import kotlin.collections.removeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class LongArrayDequeTest {

    // --- construction & size ---

    @Test
    fun emptyDequeHasSizeZero() {
        val deque = LongArrayDeque()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun constructFromLongArray() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        assertEquals(3, deque.size)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    @Test
    fun constructFromLongCollection() {
        val source = mutableLongListOf(4L, 5L, 6L)
        val deque = LongArrayDeque(source)
        assertEquals(listOf(4L, 5L, 6L), deque.toList())
    }

    // --- addFirst / addLast ---

    @Test
    fun addLastAppendsToEnd() {
        val deque = LongArrayDeque()
        deque.addLast(1L)
        deque.addLast(2L)
        deque.addLast(3L)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    @Test
    fun addFirstPrependsToFront() {
        val deque = LongArrayDeque()
        deque.addFirst(3L)
        deque.addFirst(2L)
        deque.addFirst(1L)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    @Test
    fun addFirstAndLastInterleaved() {
        val deque = LongArrayDeque()
        deque.addLast(2L)
        deque.addFirst(1L)
        deque.addLast(3L)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    // --- removeFirst / removeLast ---

    @Test
    fun removeFirstReturnsFrontElement() {
        val deque = LongArrayDeque()
        deque.addLast(10L)
        deque.addLast(20L)
        assertEquals(10L, deque.removeFirst())
        assertEquals(listOf(20L), deque.toList())
    }

    @Test
    fun removeLastReturnsBackElement() {
        val deque = LongArrayDeque()
        deque.addLast(10L)
        deque.addLast(20L)
        assertEquals(20L, deque.removeLast())
        assertEquals(listOf(10L), deque.toList())
    }

    @Test
    fun removeFirstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { LongArrayDeque().removeFirst() }
    }

    @Test
    fun removeLastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { LongArrayDeque().removeLast() }
    }

    @Test
    fun addFirstThenRemoveFirstIsLifo() {
        val deque = LongArrayDeque()
        for (i in 1..5) deque.addFirst(i.toLong())
        val result = mutableListOf<Long>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L), result)
    }

    @Test
    fun addLastThenRemoveFirstIsFifo() {
        val deque = LongArrayDeque()
        for (i in 1..5) deque.addLast(i.toLong())
        val result = mutableListOf<Long>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), result)
    }

    // --- first / last ---

    @Test
    fun firstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { LongArrayDeque().first() }
    }

    @Test
    fun lastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { LongArrayDeque().last() }
    }

    @Test
    fun firstAndLastReturnCorrectElements() {
        val deque = LongArrayDeque(longArrayOf(10L, 20L, 30L))
        assertEquals(10L, deque.first())
        assertEquals(30L, deque.last())
    }

    // --- get / set ---

    @Test
    fun getReturnsCorrectElement() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        assertEquals(2L, deque[1])
    }

    @Test
    fun setReplacesElement() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.setAt(1, 99L)
        assertEquals(listOf(1L, 99L, 3L), deque.toList())
    }

    @Test
    fun getOutOfBoundsThrows() {
        val deque = LongArrayDeque(longArrayOf(1L))
        assertFailsWith<IndexOutOfBoundsException> { deque[5] }
    }

    @Test
    fun setOutOfBoundsThrows() {
        val deque = LongArrayDeque(longArrayOf(1L))
        assertFailsWith<IndexOutOfBoundsException> { deque.setAt(5, 99L) }
    }

    // --- add(index) / removeAt ---

    @Test
    fun addAtMiddle() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 4L, 5L))
        deque.add(2, 3L)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), deque.toList())
    }

    @Test
    fun addAtLeft() {
        val deque = LongArrayDeque(longArrayOf(1L, 3L, 4L, 5L))
        deque.add(1, 2L)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), deque.toList())
    }

    @Test
    fun addAtRight() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L, 5L))
        deque.add(3, 4L)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), deque.toList())
    }

    @Test
    fun addAtFront() {
        val deque = LongArrayDeque(longArrayOf(2L, 3L))
        deque.add(0, 1L)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    @Test
    fun addAtEnd() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L))
        deque.add(2, 3L)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    @Test
    fun removeAtMiddle() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L, 4L, 5L))
        val removed = deque.removeAt(2)
        assertEquals(3L, removed)
        assertEquals(listOf(1L, 2L, 4L, 5L), deque.toList())
    }

    @Test
    fun removeAtLeft() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L, 4L, 5L))
        val removed = deque.removeAt(1)
        assertEquals(2L, removed)
        assertEquals(listOf(1L, 3L, 4L, 5L), deque.toList())
    }

    @Test
    fun removeAtRight() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L, 4L, 5L))
        val removed = deque.removeAt(3)
        assertEquals(4L, removed)
        assertEquals(listOf(1L, 2L, 3L, 5L), deque.toList())
    }

    @Test
    fun removeAtFront() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.removeAt(0)
        assertEquals(listOf(2L, 3L), deque.toList())
    }

    @Test
    fun removeAtBack() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.removeAt(2)
        assertEquals(listOf(1L, 2L), deque.toList())
    }

    @Test
    fun removeAtOutOfBoundsThrows() {
        assertFailsWith<IndexOutOfBoundsException> { LongArrayDeque().removeAt(0) }
    }

    // --- removeRange ---

    @Test
    fun removeRangeMiddle() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L, 4L, 5L))
        deque.removeRange(1, 3)
        assertEquals(listOf(1L, 4L, 5L), deque.toList())
    }

    @Test
    fun removeRangeToEnd() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L, 4L, 5L))
        deque.removeRange(2, 5)
        assertEquals(listOf(1L, 2L), deque.toList())
    }

    @Test
    fun removeRangeFull() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.removeRange(0, 3)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun removeRangeEmptyIsNoOp() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.removeRange(2, 2)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    @Test
    fun removeRangeAtEndIsNoOp() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.removeRange(3, 3)
        assertEquals(listOf(1L, 2L, 3L), deque.toList())
    }

    @Test
    fun removeRangeInvalidThrows() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        assertFailsWith<IllegalArgumentException> { deque.removeRange(2, 1) }
    }

    // --- clear ---

    @Test
    fun clearEmptiesDeque() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.clear()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    // --- indexOf / lastIndexOf ---

    @Test
    fun indexOfReturnsFirstOccurrence() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 1L))
        assertEquals(0, deque.indexOf(1L))
    }

    @Test
    fun indexOfReturnsMinus1WhenAbsent() {
        assertEquals(-1, LongArrayDeque(longArrayOf(1L, 2L)).indexOf(99L))
    }

    @Test
    fun lastIndexOfReturnsLastOccurrence() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 1L))
        assertEquals(2, deque.lastIndexOf(1L))
    }

    @Test
    fun indexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = LongArrayDeque()
        deque.addLast(1L); deque.addLast(2L); deque.addLast(3L)
        deque.removeFirst()                  // head shifts: logical = [2, 3]
        assertEquals(1, deque.indexOf(3L))   // logical 1, not physical 2
    }

    @Test
    fun lastIndexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = LongArrayDeque()
        deque.addLast(1L); deque.addLast(2L); deque.addLast(1L)
        deque.removeFirst()                       // head shifts: logical = [2, 1]
        assertEquals(1, deque.lastIndexOf(1L))    // logical 1, not physical 2
    }

    // --- contains ---

    @Test
    fun containsFindsExistingElement() {
        val deque = LongArrayDeque(longArrayOf(7L))
        assertTrue(deque.contains(7L))
        assertFalse(deque.contains(8L))
    }

    // --- removeAll / retainAll ---

    @Test
    fun removeAllPredicateCorrectWhenWrapped() {
        // Force a physical wrap: fill 8, drain 6 (head=6), add 4 more so tail wraps to index 2.
        // Logical = [7, 8, 9, 10, 11, 12] with head at physical 6 and tail at physical 2.
        val deque = LongArrayDeque()
        for (i in 1..8) deque.addLast(i.toLong())
        repeat(6) { deque.removeFirst() }
        deque.addLast(9L); deque.addLast(10L); deque.addLast(11L); deque.addLast(12L)
        deque.removeAll { it % 2L == 0L }
        assertEquals(listOf(7L, 9L, 11L), deque.toList())
    }

    // --- addAll ---

    @Test
    fun addAllDequeAppendsToEmpty() {
        val dest = LongArrayDeque()
        dest.addAll(LongArrayDeque(longArrayOf(1L, 2L, 3L)))
        assertEquals(listOf(1L, 2L, 3L), dest.toList())
    }

    @Test
    fun addAllDequeAppendsToNonEmpty() {
        val dest = LongArrayDeque(longArrayOf(1L, 2L))
        dest.addAll(LongArrayDeque(longArrayOf(3L, 4L)))
        assertEquals(listOf(1L, 2L, 3L, 4L), dest.toList())
    }

    @Test
    fun addAllDequeEmptySourceIsNoOp() {
        val dest = LongArrayDeque(longArrayOf(1L, 2L))
        val modified = dest.addAll(LongArrayDeque())
        assertFalse(modified)
        assertEquals(listOf(1L, 2L), dest.toList())
    }

    @Test
    fun addAllDequeSourceWrapped() {
        // Source deque is physically wrapped: fill 8, drain 6 (head=6), add 4 → logical [7,8,9,10], wrapped.
        val src = LongArrayDeque()
        for (i in 1..8) src.addLast(i.toLong())
        repeat(6) { src.removeFirst() }
        src.addLast(9L); src.addLast(10L)
        val dest = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        dest.addAll(src)
        assertEquals(listOf(1L, 2L, 3L, 7L, 8L, 9L, 10L), dest.toList())
    }

    @Test
    fun addAllDequeDestWrapped() {
        // Dest deque has head > 0: fill 4, drain 2 (head=2), logical [3,4].
        val dest = LongArrayDeque()
        for (i in 1..4) dest.addLast(i.toLong())
        repeat(2) { dest.removeFirst() }
        dest.addAll(LongArrayDeque(longArrayOf(5L, 6L, 7L)))
        assertEquals(listOf(3L, 4L, 5L, 6L, 7L), dest.toList())
    }

    @Test
    fun addAllDequeBothWrapped() {
        // Source wrapped: fill 8, drain 6, add 4 → logical [7,8,9,10].
        val src = LongArrayDeque()
        for (i in 1..8) src.addLast(i.toLong())
        repeat(6) { src.removeFirst() }
        src.addLast(9L); src.addLast(10L)
        // Dest wrapped: fill 4, drain 2 → logical [3,4], head=2.
        val dest = LongArrayDeque()
        for (i in 1..4) dest.addLast(i.toLong())
        repeat(2) { dest.removeFirst() }
        dest.addAll(src)
        assertEquals(listOf(3L, 4L, 7L, 8L, 9L, 10L), dest.toList())
    }

    @Test
    fun addAllDequeSelfDoublesContent() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        deque.addAll(deque)
        assertEquals(listOf(1L, 2L, 3L, 1L, 2L, 3L), deque.toList())
    }

    // --- ensureCapacity / trimToSize ---

    @Test
    fun ensureCapacityDoesNotLoseData() {
        val deque = LongArrayDeque()
        for (i in 0 until 100) deque.addLast(i.toLong())
        deque.ensureCapacity(200)
        assertEquals(100, deque.size)
        for (i in 0 until 100) assertEquals(i.toLong(), deque[i])
    }

    @Test
    fun trimToSizePreservesElements() {
        val deque = LongArrayDeque()
        for (i in 0 until 50) deque.addLast(i.toLong())
        deque.ensureCapacity(200)
        deque.trimToSize()
        assertEquals(50, deque.size)
        for (i in 0 until 50) assertEquals(i.toLong(), deque[i])
    }

    @Test
    fun trimToSizeOnEmptyDequeDoesNotThrow() {
        LongArrayDeque().trimToSize()
    }

    // --- toLongArray ---

    @Test
    fun toLongArrayReturnsCorrectElements() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        assertEquals(longArrayOf(1L, 2L, 3L).toList(), deque.toLongArray().toList())
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        val b = LongArrayDeque(longArrayOf(1L, 2L, 3L))
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentContent() {
        assertNotEquals(LongArrayDeque(longArrayOf(1L)), LongArrayDeque(longArrayOf(2L)))
    }

    @Test
    fun equalsWithStandardList() {
        assertEquals(LongArrayDeque(longArrayOf(1L, 2L, 3L)), listOf(1L, 2L, 3L))
    }

    @Test
    fun hashCodeConsistentWithEqualDeques() {
        val a = LongArrayDeque(longArrayOf(10L))
        val b = LongArrayDeque(longArrayOf(10L))
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun hashCodeConsistentAfterHeadShifts() {
        // head=0 deque and head>0 deque with identical logical content must agree.
        val fresh = LongArrayDeque(longArrayOf(2L, 3L))   // head=0
        val shifted = LongArrayDeque()
        shifted.addLast(1L); shifted.addLast(2L); shifted.addLast(3L)
        shifted.removeFirst()                              // head=1, logical=[2,3]
        assertEquals(fresh.hashCode(), shifted.hashCode())
    }

    // --- iterator ---

    @Test
    fun iteratorTraversesAllElements() {
        val deque = LongArrayDeque(longArrayOf(1L, 2L, 3L, 4L, 5L))
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), deque.toList())
    }

    // --- ring-buffer correctness after wrap ---

    @Test
    fun correctAfterHeadWrapsAround() {
        val deque = LongArrayDeque()
        for (i in 1..5) deque.addLast(i.toLong())
        repeat(3) { deque.removeFirst() }       // head now at 3 (wrapped)
        deque.addLast(6L)
        deque.addLast(7L)
        deque.addFirst(0L)
        assertEquals(listOf(0L, 4L, 5L, 6L, 7L), deque.toList())
    }

    @Test
    fun toLongArrayCorrectAfterWrap() {
        val deque = LongArrayDeque()
        for (i in 1..5) deque.addLast(i.toLong())
        repeat(3) { deque.removeFirst() }
        deque.addLast(6L); deque.addLast(7L)
        assertEquals(listOf(4L, 5L, 6L, 7L), deque.toLongArray().toList())
    }

    @Test
    fun addAtIndexCorrectAfterWrap() {
        val deque = LongArrayDeque()
        for (i in 1..4) deque.addLast(i.toLong())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4], head wrapped
        deque.add(1, 99L)
        assertEquals(listOf(3L, 99L, 4L), deque.toList())
    }

    @Test
    fun removeAtCorrectAfterWrap() {
        val deque = LongArrayDeque()
        for (i in 1..4) deque.addLast(i.toLong())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4]
        deque.addLast(5L); deque.addLast(6L)    // logical: [3, 4, 5, 6], wraps
        deque.removeAt(1)
        assertEquals(listOf(3L, 5L, 6L), deque.toList())
    }

    @Test
    fun removeRangeCorrectAfterWrap() {
        val deque = LongArrayDeque()
        for (i in 1..6) deque.addLast(i.toLong())
        repeat(3) { deque.removeFirst() }       // logical: [4, 5, 6]
        deque.addLast(7L); deque.addLast(8L)    // logical: [4, 5, 6, 7, 8], wraps
        deque.removeRange(1, 3)
        assertEquals(listOf(4L, 7L, 8L), deque.toList())
    }
}
