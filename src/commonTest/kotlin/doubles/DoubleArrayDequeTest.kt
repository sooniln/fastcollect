package io.github.sooniln.fastcollect.doubles

import kotlin.collections.removeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class DoubleArrayDequeTest {

    // --- construction & size ---

    @Test
    fun emptyDequeHasSizeZero() {
        val deque = DoubleArrayDeque()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun constructFromDoubleArray() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        assertEquals(3, deque.size)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    @Test
    fun constructFromDoubleCollection() {
        val source = mutableDoubleListOf(4.0, 5.0, 6.0)
        val deque = DoubleArrayDeque(source)
        assertEquals(listOf(4.0, 5.0, 6.0), deque.toList())
    }

    // --- addFirst / addLast ---

    @Test
    fun addLastAppendsToEnd() {
        val deque = DoubleArrayDeque()
        deque.addLast(1.0)
        deque.addLast(2.0)
        deque.addLast(3.0)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    @Test
    fun addFirstPrependsToFront() {
        val deque = DoubleArrayDeque()
        deque.addFirst(3.0)
        deque.addFirst(2.0)
        deque.addFirst(1.0)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    @Test
    fun addFirstAndLastInterleaved() {
        val deque = DoubleArrayDeque()
        deque.addLast(2.0)
        deque.addFirst(1.0)
        deque.addLast(3.0)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    // --- removeFirst / removeLast ---

    @Test
    fun removeFirstReturnsFrontElement() {
        val deque = DoubleArrayDeque()
        deque.addLast(10.0)
        deque.addLast(20.0)
        assertEquals(10.0, deque.removeFirst())
        assertEquals(listOf(20.0), deque.toList())
    }

    @Test
    fun removeLastReturnsBackElement() {
        val deque = DoubleArrayDeque()
        deque.addLast(10.0)
        deque.addLast(20.0)
        assertEquals(20.0, deque.removeLast())
        assertEquals(listOf(10.0), deque.toList())
    }

    @Test
    fun removeFirstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { DoubleArrayDeque().removeFirst() }
    }

    @Test
    fun removeLastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { DoubleArrayDeque().removeLast() }
    }

    @Test
    fun addFirstThenRemoveFirstIsLifo() {
        val deque = DoubleArrayDeque()
        for (i in 1..5) deque.addFirst(i.toDouble())
        val result = mutableListOf<Double>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(5.0, 4.0, 3.0, 2.0, 1.0), result)
    }

    @Test
    fun addLastThenRemoveFirstIsFifo() {
        val deque = DoubleArrayDeque()
        for (i in 1..5) deque.addLast(i.toDouble())
        val result = mutableListOf<Double>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0, 5.0), result)
    }

    // --- first / last ---

    @Test
    fun firstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { DoubleArrayDeque().first() }
    }

    @Test
    fun lastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { DoubleArrayDeque().last() }
    }

    @Test
    fun firstAndLastReturnCorrectElements() {
        val deque = DoubleArrayDeque(doubleArrayOf(10.0, 20.0, 30.0))
        assertEquals(10.0, deque.first())
        assertEquals(30.0, deque.last())
    }

    // --- get / set ---

    @Test
    fun getReturnsCorrectElement() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        assertEquals(2.0, deque[1])
    }

    @Test
    fun setReplacesElement() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.setAt(1, 99.0)
        assertEquals(listOf(1.0, 99.0, 3.0), deque.toList())
    }

    @Test
    fun getOutOfBoundsThrows() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0))
        assertFailsWith<IndexOutOfBoundsException> { deque[5] }
    }

    @Test
    fun setOutOfBoundsThrows() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0))
        assertFailsWith<IndexOutOfBoundsException> { deque.setAt(5, 99.0) }
    }

    // --- add(index) / removeAt ---

    @Test
    fun addAtMiddle() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 4.0, 5.0))
        deque.add(2, 3.0)
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0, 5.0), deque.toList())
    }

    @Test
    fun addAtLeft() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 3.0, 4.0, 5.0))
        deque.add(1, 2.0)
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0, 5.0), deque.toList())
    }

    @Test
    fun addAtRight() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0, 5.0))
        deque.add(3, 4.0)
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0, 5.0), deque.toList())
    }

    @Test
    fun addAtFront() {
        val deque = DoubleArrayDeque(doubleArrayOf(2.0, 3.0))
        deque.add(0, 1.0)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    @Test
    fun addAtEnd() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0))
        deque.add(2, 3.0)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    @Test
    fun removeAtMiddle() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
        val removed = deque.removeAt(2)
        assertEquals(3.0, removed)
        assertEquals(listOf(1.0, 2.0, 4.0, 5.0), deque.toList())
    }

    @Test
    fun removeAtLeft() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
        val removed = deque.removeAt(1)
        assertEquals(2.0, removed)
        assertEquals(listOf(1.0, 3.0, 4.0, 5.0), deque.toList())
    }

    @Test
    fun removeAtRight() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
        val removed = deque.removeAt(3)
        assertEquals(4.0, removed)
        assertEquals(listOf(1.0, 2.0, 3.0, 5.0), deque.toList())
    }

    @Test
    fun removeAtFront() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.removeAt(0)
        assertEquals(listOf(2.0, 3.0), deque.toList())
    }

    @Test
    fun removeAtBack() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.removeAt(2)
        assertEquals(listOf(1.0, 2.0), deque.toList())
    }

    @Test
    fun removeAtOutOfBoundsThrows() {
        assertFailsWith<IndexOutOfBoundsException> { DoubleArrayDeque().removeAt(0) }
    }

    // --- removeRange ---

    @Test
    fun removeRangeMiddle() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
        deque.removeRange(1, 3)
        assertEquals(listOf(1.0, 4.0, 5.0), deque.toList())
    }

    @Test
    fun removeRangeToEnd() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
        deque.removeRange(2, 5)
        assertEquals(listOf(1.0, 2.0), deque.toList())
    }

    @Test
    fun removeRangeFull() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.removeRange(0, 3)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun removeRangeEmptyIsNoOp() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.removeRange(2, 2)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    @Test
    fun removeRangeAtEndIsNoOp() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.removeRange(3, 3)
        assertEquals(listOf(1.0, 2.0, 3.0), deque.toList())
    }

    @Test
    fun removeRangeInvalidThrows() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        assertFailsWith<IllegalArgumentException> { deque.removeRange(2, 1) }
    }

    // --- clear ---

    @Test
    fun clearEmptiesDeque() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.clear()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    // --- indexOf / lastIndexOf ---

    @Test
    fun indexOfReturnsFirstOccurrence() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 1.0))
        assertEquals(0, deque.indexOf(1.0))
    }

    @Test
    fun indexOfReturnsMinus1WhenAbsent() {
        assertEquals(-1, DoubleArrayDeque(doubleArrayOf(1.0, 2.0)).indexOf(99.0))
    }

    @Test
    fun lastIndexOfReturnsLastOccurrence() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 1.0))
        assertEquals(2, deque.lastIndexOf(1.0))
    }

    @Test
    fun indexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = DoubleArrayDeque()
        deque.addLast(1.0); deque.addLast(2.0); deque.addLast(3.0)
        deque.removeFirst()                   // head shifts: logical = [2, 3]
        assertEquals(1, deque.indexOf(3.0))   // logical 1, not physical 2
    }

    @Test
    fun lastIndexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = DoubleArrayDeque()
        deque.addLast(1.0); deque.addLast(2.0); deque.addLast(1.0)
        deque.removeFirst()                        // head shifts: logical = [2, 1]
        assertEquals(1, deque.lastIndexOf(1.0))    // logical 1, not physical 2
    }

    // --- contains ---

    @Test
    fun containsFindsExistingElement() {
        val deque = DoubleArrayDeque(doubleArrayOf(7.0))
        assertTrue(deque.contains(7.0))
        assertFalse(deque.contains(8.0))
    }

    // --- removeAll / retainAll ---

    @Test
    fun removeAllPredicateCorrectWhenWrapped() {
        // Force a physical wrap: fill 8, drain 6 (head=6), add 4 more so tail wraps to index 2.
        // Logical = [7, 8, 9, 10, 11, 12] with head at physical 6 and tail at physical 2.
        val deque = DoubleArrayDeque()
        for (i in 1..8) deque.addLast(i.toDouble())
        repeat(6) { deque.removeFirst() }
        deque.addLast(9.0); deque.addLast(10.0); deque.addLast(11.0); deque.addLast(12.0)
        deque.removeAll { it % 2.0 == 0.0 }
        assertEquals(listOf(7.0, 9.0, 11.0), deque.toList())
    }

    // --- addAll ---

    @Test
    fun addAllDequeAppendsToEmpty() {
        val dest = DoubleArrayDeque()
        dest.addAll(DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0)))
        assertEquals(listOf(1.0, 2.0, 3.0), dest.toList())
    }

    @Test
    fun addAllDequeAppendsToNonEmpty() {
        val dest = DoubleArrayDeque(doubleArrayOf(1.0, 2.0))
        dest.addAll(DoubleArrayDeque(doubleArrayOf(3.0, 4.0)))
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0), dest.toList())
    }

    @Test
    fun addAllDequeEmptySourceIsNoOp() {
        val dest = DoubleArrayDeque(doubleArrayOf(1.0, 2.0))
        val modified = dest.addAll(DoubleArrayDeque())
        assertFalse(modified)
        assertEquals(listOf(1.0, 2.0), dest.toList())
    }

    @Test
    fun addAllDequeSourceWrapped() {
        // Source deque is physically wrapped: fill 8, drain 6 (head=6), add 4 → logical [7,8,9,10], wrapped.
        val src = DoubleArrayDeque()
        for (i in 1..8) src.addLast(i.toDouble())
        repeat(6) { src.removeFirst() }
        src.addLast(9.0); src.addLast(10.0)
        val dest = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        dest.addAll(src)
        assertEquals(listOf(1.0, 2.0, 3.0, 7.0, 8.0, 9.0, 10.0), dest.toList())
    }

    @Test
    fun addAllDequeDestWrapped() {
        // Dest deque has head > 0: fill 4, drain 2 (head=2), logical [3,4].
        val dest = DoubleArrayDeque()
        for (i in 1..4) dest.addLast(i.toDouble())
        repeat(2) { dest.removeFirst() }
        dest.addAll(DoubleArrayDeque(doubleArrayOf(5.0, 6.0, 7.0)))
        assertEquals(listOf(3.0, 4.0, 5.0, 6.0, 7.0), dest.toList())
    }

    @Test
    fun addAllDequeBothWrapped() {
        // Source wrapped: fill 8, drain 6, add 4 → logical [7,8,9,10].
        val src = DoubleArrayDeque()
        for (i in 1..8) src.addLast(i.toDouble())
        repeat(6) { src.removeFirst() }
        src.addLast(9.0); src.addLast(10.0)
        // Dest wrapped: fill 4, drain 2 → logical [3,4], head=2.
        val dest = DoubleArrayDeque()
        for (i in 1..4) dest.addLast(i.toDouble())
        repeat(2) { dest.removeFirst() }
        dest.addAll(src)
        assertEquals(listOf(3.0, 4.0, 7.0, 8.0, 9.0, 10.0), dest.toList())
    }

    @Test
    fun addAllDequeSelfDoublesContent() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        deque.addAll(deque)
        assertEquals(listOf(1.0, 2.0, 3.0, 1.0, 2.0, 3.0), deque.toList())
    }

    // --- ensureCapacity / trimToSize ---

    @Test
    fun ensureCapacityDoesNotLoseData() {
        val deque = DoubleArrayDeque()
        for (i in 0 until 100) deque.addLast(i.toDouble())
        deque.ensureCapacity(200)
        assertEquals(100, deque.size)
        for (i in 0 until 100) assertEquals(i.toDouble(), deque[i])
    }

    @Test
    fun trimToSizePreservesElements() {
        val deque = DoubleArrayDeque()
        for (i in 0 until 50) deque.addLast(i.toDouble())
        deque.ensureCapacity(200)
        deque.trimToSize()
        assertEquals(50, deque.size)
        for (i in 0 until 50) assertEquals(i.toDouble(), deque[i])
    }

    @Test
    fun trimToSizeOnEmptyDequeDoesNotThrow() {
        DoubleArrayDeque().trimToSize()
    }

    // --- toDoubleArray ---

    @Test
    fun toDoubleArrayReturnsCorrectElements() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        assertEquals(doubleArrayOf(1.0, 2.0, 3.0).toList(), deque.toDoubleArray().toList())
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        val b = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0))
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentContent() {
        assertNotEquals(DoubleArrayDeque(doubleArrayOf(1.0)), DoubleArrayDeque(doubleArrayOf(2.0)))
    }

    @Test
    fun equalsWithStandardList() {
        assertEquals(DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0)), listOf(1.0, 2.0, 3.0))
    }

    @Test
    fun hashCodeConsistentWithEqualDeques() {
        val a = DoubleArrayDeque(doubleArrayOf(10.0))
        val b = DoubleArrayDeque(doubleArrayOf(10.0))
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun hashCodeConsistentAfterHeadShifts() {
        // head=0 deque and head>0 deque with identical logical content must agree.
        val fresh = DoubleArrayDeque(doubleArrayOf(2.0, 3.0))   // head=0
        val shifted = DoubleArrayDeque()
        shifted.addLast(1.0); shifted.addLast(2.0); shifted.addLast(3.0)
        shifted.removeFirst()                                    // head=1, logical=[2,3]
        assertEquals(fresh.hashCode(), shifted.hashCode())
    }

    // --- iterator ---

    @Test
    fun iteratorTraversesAllElements() {
        val deque = DoubleArrayDeque(doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0))
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0, 5.0), deque.toList())
    }

    // --- ring-buffer correctness after wrap ---

    @Test
    fun correctAfterHeadWrapsAround() {
        val deque = DoubleArrayDeque()
        for (i in 1..5) deque.addLast(i.toDouble())
        repeat(3) { deque.removeFirst() }       // head now at 3 (wrapped)
        deque.addLast(6.0)
        deque.addLast(7.0)
        deque.addFirst(0.0)
        assertEquals(listOf(0.0, 4.0, 5.0, 6.0, 7.0), deque.toList())
    }

    @Test
    fun toDoubleArrayCorrectAfterWrap() {
        val deque = DoubleArrayDeque()
        for (i in 1..5) deque.addLast(i.toDouble())
        repeat(3) { deque.removeFirst() }
        deque.addLast(6.0); deque.addLast(7.0)
        assertEquals(listOf(4.0, 5.0, 6.0, 7.0), deque.toDoubleArray().toList())
    }

    @Test
    fun addAtIndexCorrectAfterWrap() {
        val deque = DoubleArrayDeque()
        for (i in 1..4) deque.addLast(i.toDouble())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4], head wrapped
        deque.add(1, 99.0)
        assertEquals(listOf(3.0, 99.0, 4.0), deque.toList())
    }

    @Test
    fun removeAtCorrectAfterWrap() {
        val deque = DoubleArrayDeque()
        for (i in 1..4) deque.addLast(i.toDouble())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4]
        deque.addLast(5.0); deque.addLast(6.0)  // logical: [3, 4, 5, 6], wraps
        deque.removeAt(1)
        assertEquals(listOf(3.0, 5.0, 6.0), deque.toList())
    }

    @Test
    fun removeRangeCorrectAfterWrap() {
        val deque = DoubleArrayDeque()
        for (i in 1..6) deque.addLast(i.toDouble())
        repeat(3) { deque.removeFirst() }       // logical: [4, 5, 6]
        deque.addLast(7.0); deque.addLast(8.0)  // logical: [4, 5, 6, 7, 8], wraps
        deque.removeRange(1, 3)
        assertEquals(listOf(4.0, 7.0, 8.0), deque.toList())
    }
}
