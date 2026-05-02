package io.github.sooniln.fastcollect.shorts

import kotlin.collections.removeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class ShortArrayDequeTest {

    // --- construction & size ---

    @Test
    fun emptyDequeHasSizeZero() {
        val deque = ShortArrayDeque()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun constructFromShortArray() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        assertEquals(3, deque.size)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    @Test
    fun constructFromShortCollection() {
        val source = mutableShortListOf(4, 5, 6)
        val deque = ShortArrayDeque(source)
        assertEquals(listOf<Short>(4, 5, 6), deque.toList())
    }

    // --- addFirst / addLast ---

    @Test
    fun addLastAppendsToEnd() {
        val deque = ShortArrayDeque()
        deque.addLast(1)
        deque.addLast(2)
        deque.addLast(3)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    @Test
    fun addFirstPrependsToFront() {
        val deque = ShortArrayDeque()
        deque.addFirst(3)
        deque.addFirst(2)
        deque.addFirst(1)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    @Test
    fun addFirstAndLastInterleaved() {
        val deque = ShortArrayDeque()
        deque.addLast(2)
        deque.addFirst(1)
        deque.addLast(3)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    // --- removeFirst / removeLast ---

    @Test
    fun removeFirstReturnsFrontElement() {
        val deque = ShortArrayDeque()
        deque.addLast(10)
        deque.addLast(20)
        assertEquals(10.toShort(), deque.removeFirst())
        assertEquals(listOf<Short>(20), deque.toList())
    }

    @Test
    fun removeLastReturnsBackElement() {
        val deque = ShortArrayDeque()
        deque.addLast(10)
        deque.addLast(20)
        assertEquals(20.toShort(), deque.removeLast())
        assertEquals(listOf<Short>(10), deque.toList())
    }

    @Test
    fun removeFirstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { ShortArrayDeque().removeFirst() }
    }

    @Test
    fun removeLastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { ShortArrayDeque().removeLast() }
    }

    @Test
    fun addFirstThenRemoveFirstIsLifo() {
        val deque = ShortArrayDeque()
        for (i in 1..5) deque.addFirst(i.toShort())
        val result = mutableListOf<Short>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf<Short>(5, 4, 3, 2, 1), result)
    }

    @Test
    fun addLastThenRemoveFirstIsFifo() {
        val deque = ShortArrayDeque()
        for (i in 1..5) deque.addLast(i.toShort())
        val result = mutableListOf<Short>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf<Short>(1, 2, 3, 4, 5), result)
    }

    // --- first / last ---

    @Test
    fun firstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { ShortArrayDeque().first() }
    }

    @Test
    fun lastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { ShortArrayDeque().last() }
    }

    @Test
    fun firstAndLastReturnCorrectElements() {
        val deque = ShortArrayDeque(shortArrayOf(10, 20, 30))
        assertEquals(10.toShort(), deque.first())
        assertEquals(30.toShort(), deque.last())
    }

    // --- get / set ---

    @Test
    fun getReturnsCorrectElement() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        assertEquals(2.toShort(), deque.getAt(1))
    }

    @Test
    fun setReplacesElement() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.setAt(1, 99)
        assertEquals(listOf<Short>(1, 99, 3), deque.toList())
    }

    @Test
    fun getOutOfBoundsThrows() {
        val deque = ShortArrayDeque(shortArrayOf(1))
        assertFailsWith<IndexOutOfBoundsException> { deque.getAt(5) }
    }

    @Test
    fun setOutOfBoundsThrows() {
        val deque = ShortArrayDeque(shortArrayOf(1))
        assertFailsWith<IndexOutOfBoundsException> { deque.setAt(5, 99) }
    }

    // --- add(index) / removeAt ---

    @Test
    fun addAtMiddle() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 4, 5))
        deque.add(2, 3)
        assertEquals(listOf<Short>(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun addAtLeft() {
        val deque = ShortArrayDeque(shortArrayOf(1, 3, 4, 5))
        deque.add(1, 2)
        assertEquals(listOf<Short>(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun addAtRight() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3, 5))
        deque.add(3, 4)
        assertEquals(listOf<Short>(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun addAtFront() {
        val deque = ShortArrayDeque(shortArrayOf(2, 3))
        deque.add(0, 1)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    @Test
    fun addAtEnd() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2))
        deque.add(2, 3)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    @Test
    fun removeAtMiddle() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3, 4, 5))
        val removed = deque.removeAt(2)
        assertEquals(3.toShort(), removed)
        assertEquals(listOf<Short>(1, 2, 4, 5), deque.toList())
    }

    @Test
    fun removeAtLeft() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3, 4, 5))
        val removed = deque.removeAt(1)
        assertEquals(2.toShort(), removed)
        assertEquals(listOf<Short>(1, 3, 4, 5), deque.toList())
    }

    @Test
    fun removeAtRight() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3, 4, 5))
        val removed = deque.removeAt(3)
        assertEquals(4.toShort(), removed)
        assertEquals(listOf<Short>(1, 2, 3, 5), deque.toList())
    }

    @Test
    fun removeAtFront() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.removeAt(0)
        assertEquals(listOf<Short>(2, 3), deque.toList())
    }

    @Test
    fun removeAtBack() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.removeAt(2)
        assertEquals(listOf<Short>(1, 2), deque.toList())
    }

    @Test
    fun removeAtOutOfBoundsThrows() {
        assertFailsWith<IndexOutOfBoundsException> { ShortArrayDeque().removeAt(0) }
    }

    // --- removeRange ---

    @Test
    fun removeRangeMiddle() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3, 4, 5))
        deque.removeRange(1, 3)
        assertEquals(listOf<Short>(1, 4, 5), deque.toList())
    }

    @Test
    fun removeRangeToEnd() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3, 4, 5))
        deque.removeRange(2, 5)
        assertEquals(listOf<Short>(1, 2), deque.toList())
    }

    @Test
    fun removeRangeFull() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.removeRange(0, 3)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun removeRangeEmptyIsNoOp() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.removeRange(2, 2)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    @Test
    fun removeRangeAtEndIsNoOp() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.removeRange(3, 3)
        assertEquals(listOf<Short>(1, 2, 3), deque.toList())
    }

    @Test
    fun removeRangeInvalidThrows() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        assertFailsWith<IllegalArgumentException> { deque.removeRange(2, 1) }
    }

    // --- clear ---

    @Test
    fun clearEmptiesDeque() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.clear()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    // --- indexOf / lastIndexOf ---

    @Test
    fun indexOfReturnsFirstOccurrence() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 1))
        assertEquals(0, deque.indexOf(1.toShort()))
    }

    @Test
    fun indexOfReturnsMinus1WhenAbsent() {
        assertEquals(-1, ShortArrayDeque(shortArrayOf(1, 2)).indexOf(99.toShort()))
    }

    @Test
    fun lastIndexOfReturnsLastOccurrence() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 1))
        assertEquals(2, deque.lastIndexOf(1.toShort()))
    }

    @Test
    fun indexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = ShortArrayDeque()
        deque.addLast(1); deque.addLast(2); deque.addLast(3)
        deque.removeFirst()                  // head shifts: logical = [2, 3]
        assertEquals(1, deque.indexOf(3.toShort()))    // logical 1, not physical 2
    }

    @Test
    fun lastIndexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = ShortArrayDeque()
        deque.addLast(1); deque.addLast(2); deque.addLast(1)
        deque.removeFirst()                       // head shifts: logical = [2, 1]
        assertEquals(1, deque.lastIndexOf(1.toShort()))     // logical 1, not physical 2
    }

    // --- contains ---

    @Test
    fun containsFindsExistingElement() {
        val deque = ShortArrayDeque(shortArrayOf(7))
        assertTrue(deque.contains(7.toShort()))
        assertFalse(deque.contains(8.toShort()))
    }

    // --- removeAll / retainAll ---

    @Test
    fun removeAllPredicateCorrectWhenWrapped() {
        // Force a physical wrap: fill 8, drain 6 (head=6), add 4 more so tail wraps to index 2.
        // Logical = [7, 8, 9, 10, 11, 12] with head at physical 6 and tail at physical 2.
        val deque = ShortArrayDeque()
        for (i in 1..8) deque.addLast(i.toShort())
        repeat(6) { deque.removeFirst() }
        deque.addLast(9); deque.addLast(10); deque.addLast(11); deque.addLast(12)
        deque.removeAll { it % 2 == 0 }
        assertEquals(listOf<Short>(7, 9, 11), deque.toList())
    }

    // --- addAll ---

    @Test
    fun addAllDequeAppendsToEmpty() {
        val dest = ShortArrayDeque()
        dest.addAll(ShortArrayDeque(shortArrayOf(1, 2, 3)))
        assertEquals(listOf<Short>(1, 2, 3), dest.toList())
    }

    @Test
    fun addAllDequeAppendsToNonEmpty() {
        val dest = ShortArrayDeque(shortArrayOf(1, 2))
        dest.addAll(ShortArrayDeque(shortArrayOf(3, 4)))
        assertEquals(listOf<Short>(1, 2, 3, 4), dest.toList())
    }

    @Test
    fun addAllDequeEmptySourceIsNoOp() {
        val dest = ShortArrayDeque(shortArrayOf(1, 2))
        val modified = dest.addAll(ShortArrayDeque())
        assertFalse(modified)
        assertEquals(listOf<Short>(1, 2), dest.toList())
    }

    @Test
    fun addAllDequeSourceWrapped() {
        // Source deque is physically wrapped: fill 8, drain 6 (head=6), add 4 → logical [7,8,9,10], wrapped.
        val src = ShortArrayDeque()
        for (i in 1..8) src.addLast(i.toShort())
        repeat(6) { src.removeFirst() }
        src.addLast(9); src.addLast(10)
        val dest = ShortArrayDeque(shortArrayOf(1, 2, 3))
        dest.addAll(src)
        assertEquals(listOf<Short>(1, 2, 3, 7, 8, 9, 10), dest.toList())
    }

    @Test
    fun addAllDequeDestWrapped() {
        // Dest deque has head > 0: fill 4, drain 2 (head=2), logical [3,4].
        val dest = ShortArrayDeque()
        for (i in 1..4) dest.addLast(i.toShort())
        repeat(2) { dest.removeFirst() }
        dest.addAll(ShortArrayDeque(shortArrayOf(5, 6, 7)))
        assertEquals(listOf<Short>(3, 4, 5, 6, 7), dest.toList())
    }

    @Test
    fun addAllDequeBothWrapped() {
        // Source wrapped: fill 8, drain 6, add 4 → logical [7,8,9,10].
        val src = ShortArrayDeque()
        for (i in 1..8) src.addLast(i.toShort())
        repeat(6) { src.removeFirst() }
        src.addLast(9); src.addLast(10)
        // Dest wrapped: fill 4, drain 2 → logical [3,4], head=2.
        val dest = ShortArrayDeque()
        for (i in 1..4) dest.addLast(i.toShort())
        repeat(2) { dest.removeFirst() }
        dest.addAll(src)
        assertEquals(listOf<Short>(3, 4, 7, 8, 9, 10), dest.toList())
    }

    @Test
    fun addAllDequeSelfDoublesContent() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        deque.addAll(deque)
        assertEquals(listOf<Short>(1, 2, 3, 1, 2, 3), deque.toList())
    }

    // --- ensureCapacity / trimToSize ---

    @Test
    fun ensureCapacityDoesNotLoseData() {
        val deque = ShortArrayDeque()
        for (i in 0 until 100) deque.addLast(i.toShort())
        deque.ensureCapacity(200)
        assertEquals(100, deque.size)
        for (i in 0 until 100) assertEquals(i.toShort(), deque.getAt(i))
    }

    @Test
    fun trimToSizePreservesElements() {
        val deque = ShortArrayDeque()
        for (i in 0 until 50) deque.addLast(i.toShort())
        deque.ensureCapacity(200)
        deque.trimToSize()
        assertEquals(50, deque.size)
        for (i in 0 until 50) assertEquals(i.toShort(), deque.getAt(i))
    }

    @Test
    fun trimToSizeOnEmptyDequeDoesNotThrow() {
        ShortArrayDeque().trimToSize()
    }

    // --- toShortArray ---

    @Test
    fun toShortArrayReturnsCorrectElements() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3))
        assertEquals(shortArrayOf(1, 2, 3).toList(), deque.toShortArray().toList())
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = ShortArrayDeque(shortArrayOf(1, 2, 3))
        val b = ShortArrayDeque(shortArrayOf(1, 2, 3))
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentContent() {
        assertNotEquals(ShortArrayDeque(shortArrayOf(1)), ShortArrayDeque(shortArrayOf(2)))
    }

    @Test
    fun equalsWithStandardList() {
        assertEquals(ShortArrayDeque(shortArrayOf(1, 2, 3)), listOf<Short>(1, 2, 3))
    }

    @Test
    fun hashCodeConsistentWithEqualDeques() {
        val a = ShortArrayDeque(shortArrayOf(10))
        val b = ShortArrayDeque(shortArrayOf(10))
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun hashCodeConsistentAfterHeadShifts() {
        // head=0 deque and head>0 deque with identical logical content must agree.
        val fresh = ShortArrayDeque(shortArrayOf(2, 3))   // head=0
        val shifted = ShortArrayDeque()
        shifted.addLast(1); shifted.addLast(2); shifted.addLast(3)
        shifted.removeFirst()                          // head=1, logical=[2,3]
        assertEquals(fresh.hashCode(), shifted.hashCode())
    }

    // --- iterator ---

    @Test
    fun iteratorTraversesAllElements() {
        val deque = ShortArrayDeque(shortArrayOf(1, 2, 3, 4, 5))
        assertEquals(listOf<Short>(1, 2, 3, 4, 5), deque.toList())
    }

    // --- ring-buffer correctness after wrap ---

    @Test
    fun correctAfterHeadWrapsAround() {
        val deque = ShortArrayDeque()
        for (i in 1..5) deque.addLast(i.toShort())
        repeat(3) { deque.removeFirst() }       // head now at 3 (wrapped)
        deque.addLast(6)
        deque.addLast(7)
        deque.addFirst(0)
        assertEquals(listOf<Short>(0, 4, 5, 6, 7), deque.toList())
    }

    @Test
    fun toShortArrayCorrectAfterWrap() {
        val deque = ShortArrayDeque()
        for (i in 1..5) deque.addLast(i.toShort())
        repeat(3) { deque.removeFirst() }
        deque.addLast(6); deque.addLast(7)
        assertEquals(listOf<Short>(4, 5, 6, 7), deque.toShortArray().toList())
    }

    @Test
    fun addAtIndexCorrectAfterWrap() {
        val deque = ShortArrayDeque()
        for (i in 1..4) deque.addLast(i.toShort())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4], head wrapped
        deque.add(1, 99)
        assertEquals(listOf<Short>(3, 99, 4), deque.toList())
    }

    @Test
    fun removeAtCorrectAfterWrap() {
        val deque = ShortArrayDeque()
        for (i in 1..4) deque.addLast(i.toShort())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4]
        deque.addLast(5); deque.addLast(6)      // logical: [3, 4, 5, 6], wraps
        deque.removeAt(1)
        assertEquals(listOf<Short>(3, 5, 6), deque.toList())
    }

    @Test
    fun removeRangeCorrectAfterWrap() {
        val deque = ShortArrayDeque()
        for (i in 1..6) deque.addLast(i.toShort())
        repeat(3) { deque.removeFirst() }       // logical: [4, 5, 6]
        deque.addLast(7); deque.addLast(8)      // logical: [4, 5, 6, 7, 8], wraps
        deque.removeRange(1, 3)
        assertEquals(listOf<Short>(4, 7, 8), deque.toList())
    }

    // --- sort / sortDescending ---

    @Test
    fun sortNonWrapped() {
        val deque = ShortArrayDeque(shortArrayOf(5, 3, 1, 4, 2))
        deque.sort()
        assertEquals(listOf<Short>(1, 2, 3, 4, 5), deque.toList())
    }

    @Test
    fun sortDescendingNonWrapped() {
        val deque = ShortArrayDeque(shortArrayOf(3, 1, 4, 2, 5))
        deque.sortDescending()
        assertEquals(listOf<Short>(5, 4, 3, 2, 1), deque.toList())
    }

    @Test
    fun sortWrappedHeadSegmentLarger() {
        // ring.size=8, head=5, size=5, tail=10 → end=2, ring.size-head=3 > end (case 1)
        val deque = ShortArrayDeque()
        for (i in 1..8) deque.addLast(i.toShort())
        repeat(5) { deque.removeFirst() }       // head=5, logical=[6,7,8]
        deque.addLast(4); deque.addLast(2)      // logical=[6,7,8,4,2], tail wraps
        deque.sort()
        assertEquals(listOf<Short>(2, 4, 6, 7, 8), deque.toList())
    }

    @Test
    fun sortWrappedTailSegmentLarger() {
        // ring.size=8, head=6, size=5, tail=11 → end=3, ring.size-head=2 ≤ end (case 2)
        val deque = ShortArrayDeque()
        for (i in 1..8) deque.addLast(i.toShort())
        repeat(6) { deque.removeFirst() }            // head=6, logical=[7,8]
        deque.addLast(3); deque.addLast(1); deque.addLast(5)  // logical=[7,8,3,1,5], tail wraps
        deque.sort()
        assertEquals(listOf<Short>(1, 3, 5, 7, 8), deque.toList())
    }

    @Test
    fun sortDescendingWrapped() {
        // Same wrapped state as sortWrappedTailSegmentLarger, using sortDescending
        val deque = ShortArrayDeque()
        for (i in 1..8) deque.addLast(i.toShort())
        repeat(6) { deque.removeFirst() }
        deque.addLast(3); deque.addLast(1); deque.addLast(5)  // logical=[7,8,3,1,5]
        deque.sortDescending()
        assertEquals(listOf<Short>(8, 7, 5, 3, 1), deque.toList())
    }
}