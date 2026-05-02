package io.github.sooniln.fastcollect.floats

import kotlin.collections.removeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith

class FloatArrayDequeTest {

    // --- construction & size ---

    @Test
    fun emptyDequeHasSizeZero() {
        val deque = FloatArrayDeque()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun constructFromFloatArray() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        assertEquals(3, deque.size)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    @Test
    fun constructFromFloatCollection() {
        val source = mutableFloatListOf(4f, 5f, 6f)
        val deque = FloatArrayDeque(source)
        assertEquals(listOf(4f, 5f, 6f), deque.toList())
    }

    // --- addFirst / addLast ---

    @Test
    fun addLastAppendsToEnd() {
        val deque = FloatArrayDeque()
        deque.addLast(1f)
        deque.addLast(2f)
        deque.addLast(3f)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    @Test
    fun addFirstPrependsToFront() {
        val deque = FloatArrayDeque()
        deque.addFirst(3f)
        deque.addFirst(2f)
        deque.addFirst(1f)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    @Test
    fun addFirstAndLastInterleaved() {
        val deque = FloatArrayDeque()
        deque.addLast(2f)
        deque.addFirst(1f)
        deque.addLast(3f)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    // --- removeFirst / removeLast ---

    @Test
    fun removeFirstReturnsFrontElement() {
        val deque = FloatArrayDeque()
        deque.addLast(10f)
        deque.addLast(20f)
        assertEquals(10f, deque.removeFirst())
        assertEquals(listOf(20f), deque.toList())
    }

    @Test
    fun removeLastReturnsBackElement() {
        val deque = FloatArrayDeque()
        deque.addLast(10f)
        deque.addLast(20f)
        assertEquals(20f, deque.removeLast())
        assertEquals(listOf(10f), deque.toList())
    }

    @Test
    fun removeFirstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { FloatArrayDeque().removeFirst() }
    }

    @Test
    fun removeLastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { FloatArrayDeque().removeLast() }
    }

    @Test
    fun addFirstThenRemoveFirstIsLifo() {
        val deque = FloatArrayDeque()
        for (i in 1..5) deque.addFirst(i.toFloat())
        val result = mutableListOf<Float>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(5f, 4f, 3f, 2f, 1f), result)
    }

    @Test
    fun addLastThenRemoveFirstIsFifo() {
        val deque = FloatArrayDeque()
        for (i in 1..5) deque.addLast(i.toFloat())
        val result = mutableListOf<Float>()
        while (!deque.isEmpty()) result.add(deque.removeFirst())
        assertEquals(listOf(1f, 2f, 3f, 4f, 5f), result)
    }

    // --- first / last ---

    @Test
    fun firstThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { FloatArrayDeque().first() }
    }

    @Test
    fun lastThrowsOnEmptyDeque() {
        assertFailsWith<NoSuchElementException> { FloatArrayDeque().last() }
    }

    @Test
    fun firstAndLastReturnCorrectElements() {
        val deque = FloatArrayDeque(floatArrayOf(10f, 20f, 30f))
        assertEquals(10f, deque.first())
        assertEquals(30f, deque.last())
    }

    // --- get / set ---

    @Test
    fun getReturnsCorrectElement() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        assertEquals(2f, deque.getAt(1))
    }

    @Test
    fun setReplacesElement() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.setAt(1, 99f)
        assertEquals(listOf(1f, 99f, 3f), deque.toList())
    }

    @Test
    fun getOutOfBoundsThrows() {
        val deque = FloatArrayDeque(floatArrayOf(1f))
        assertFailsWith<IndexOutOfBoundsException> { deque.getAt(5) }
    }

    @Test
    fun setOutOfBoundsThrows() {
        val deque = FloatArrayDeque(floatArrayOf(1f))
        assertFailsWith<IndexOutOfBoundsException> { deque.setAt(5, 99f) }
    }

    // --- add(index) / removeAt ---

    @Test
    fun addAtMiddle() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 4f, 5f))
        deque.add(2, 3f)
        assertEquals(listOf(1f, 2f, 3f, 4f, 5f), deque.toList())
    }

    @Test
    fun addAtLeft() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 3f, 4f, 5f))
        deque.add(1, 2f)
        assertEquals(listOf(1f, 2f, 3f, 4f, 5f), deque.toList())
    }

    @Test
    fun addAtRight() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f, 5f))
        deque.add(3, 4f)
        assertEquals(listOf(1f, 2f, 3f, 4f, 5f), deque.toList())
    }

    @Test
    fun addAtFront() {
        val deque = FloatArrayDeque(floatArrayOf(2f, 3f))
        deque.add(0, 1f)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    @Test
    fun addAtEnd() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f))
        deque.add(2, 3f)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    @Test
    fun removeAtMiddle() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        val removed = deque.removeAt(2)
        assertEquals(3f, removed)
        assertEquals(listOf(1f, 2f, 4f, 5f), deque.toList())
    }

    @Test
    fun removeAtLeft() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        val removed = deque.removeAt(1)
        assertEquals(2f, removed)
        assertEquals(listOf(1f, 3f, 4f, 5f), deque.toList())
    }

    @Test
    fun removeAtRight() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        val removed = deque.removeAt(3)
        assertEquals(4f, removed)
        assertEquals(listOf(1f, 2f, 3f, 5f), deque.toList())
    }

    @Test
    fun removeAtFront() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.removeAt(0)
        assertEquals(listOf(2f, 3f), deque.toList())
    }

    @Test
    fun removeAtBack() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.removeAt(2)
        assertEquals(listOf(1f, 2f), deque.toList())
    }

    @Test
    fun removeAtOutOfBoundsThrows() {
        assertFailsWith<IndexOutOfBoundsException> { FloatArrayDeque().removeAt(0) }
    }

    // --- removeRange ---

    @Test
    fun removeRangeMiddle() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        deque.removeRange(1, 3)
        assertEquals(listOf(1f, 4f, 5f), deque.toList())
    }

    @Test
    fun removeRangeToEnd() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        deque.removeRange(2, 5)
        assertEquals(listOf(1f, 2f), deque.toList())
    }

    @Test
    fun removeRangeFull() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.removeRange(0, 3)
        assertTrue(deque.isEmpty())
    }

    @Test
    fun removeRangeEmptyIsNoOp() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.removeRange(2, 2)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    @Test
    fun removeRangeAtEndIsNoOp() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.removeRange(3, 3)
        assertEquals(listOf(1f, 2f, 3f), deque.toList())
    }

    @Test
    fun removeRangeInvalidThrows() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        assertFailsWith<IllegalArgumentException> { deque.removeRange(2, 1) }
    }

    // --- clear ---

    @Test
    fun clearEmptiesDeque() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.clear()
        assertEquals(0, deque.size)
        assertTrue(deque.isEmpty())
    }

    // --- indexOf / lastIndexOf ---

    @Test
    fun indexOfReturnsFirstOccurrence() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 1f))
        assertEquals(0, deque.indexOf(1f))
    }

    @Test
    fun indexOfReturnsMinus1WhenAbsent() {
        assertEquals(-1, FloatArrayDeque(floatArrayOf(1f, 2f)).indexOf(99f))
    }

    @Test
    fun lastIndexOfReturnsLastOccurrence() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 1f))
        assertEquals(2, deque.lastIndexOf(1f))
    }

    @Test
    fun indexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = FloatArrayDeque()
        deque.addLast(1f); deque.addLast(2f); deque.addLast(3f)
        deque.removeFirst()                  // head shifts: logical = [2, 3]
        assertEquals(1, deque.indexOf(3f))   // logical 1, not physical 2
    }

    @Test
    fun lastIndexOfReturnsLogicalIndexWhenHeadIsNonZero() {
        val deque = FloatArrayDeque()
        deque.addLast(1f); deque.addLast(2f); deque.addLast(1f)
        deque.removeFirst()                       // head shifts: logical = [2, 1]
        assertEquals(1, deque.lastIndexOf(1f))    // logical 1, not physical 2
    }

    // --- contains ---

    @Test
    fun containsFindsExistingElement() {
        val deque = FloatArrayDeque(floatArrayOf(7f))
        assertTrue(deque.contains(7f))
        assertFalse(deque.contains(8f))
    }

    // --- removeAll / retainAll ---

    @Test
    fun removeAllPredicateCorrectWhenWrapped() {
        // Force a physical wrap: fill 8, drain 6 (head=6), add 4 more so tail wraps to index 2.
        // Logical = [7, 8, 9, 10, 11, 12] with head at physical 6 and tail at physical 2.
        val deque = FloatArrayDeque()
        for (i in 1..8) deque.addLast(i.toFloat())
        repeat(6) { deque.removeFirst() }
        deque.addLast(9f); deque.addLast(10f); deque.addLast(11f); deque.addLast(12f)
        deque.removeAll { it % 2f == 0f }
        assertEquals(listOf(7f, 9f, 11f), deque.toList())
    }

    // --- addAll ---

    @Test
    fun addAllDequeAppendsToEmpty() {
        val dest = FloatArrayDeque()
        dest.addAll(FloatArrayDeque(floatArrayOf(1f, 2f, 3f)))
        assertEquals(listOf(1f, 2f, 3f), dest.toList())
    }

    @Test
    fun addAllDequeAppendsToNonEmpty() {
        val dest = FloatArrayDeque(floatArrayOf(1f, 2f))
        dest.addAll(FloatArrayDeque(floatArrayOf(3f, 4f)))
        assertEquals(listOf(1f, 2f, 3f, 4f), dest.toList())
    }

    @Test
    fun addAllDequeEmptySourceIsNoOp() {
        val dest = FloatArrayDeque(floatArrayOf(1f, 2f))
        val modified = dest.addAll(FloatArrayDeque())
        assertFalse(modified)
        assertEquals(listOf(1f, 2f), dest.toList())
    }

    @Test
    fun addAllDequeSourceWrapped() {
        // Source deque is physically wrapped: fill 8, drain 6 (head=6), add 4 → logical [7,8,9,10], wrapped.
        val src = FloatArrayDeque()
        for (i in 1..8) src.addLast(i.toFloat())
        repeat(6) { src.removeFirst() }
        src.addLast(9f); src.addLast(10f)
        val dest = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        dest.addAll(src)
        assertEquals(listOf(1f, 2f, 3f, 7f, 8f, 9f, 10f), dest.toList())
    }

    @Test
    fun addAllDequeDestWrapped() {
        // Dest deque has head > 0: fill 4, drain 2 (head=2), logical [3,4].
        val dest = FloatArrayDeque()
        for (i in 1..4) dest.addLast(i.toFloat())
        repeat(2) { dest.removeFirst() }
        dest.addAll(FloatArrayDeque(floatArrayOf(5f, 6f, 7f)))
        assertEquals(listOf(3f, 4f, 5f, 6f, 7f), dest.toList())
    }

    @Test
    fun addAllDequeBothWrapped() {
        // Source wrapped: fill 8, drain 6, add 4 → logical [7,8,9,10].
        val src = FloatArrayDeque()
        for (i in 1..8) src.addLast(i.toFloat())
        repeat(6) { src.removeFirst() }
        src.addLast(9f); src.addLast(10f)
        // Dest wrapped: fill 4, drain 2 → logical [3,4], head=2.
        val dest = FloatArrayDeque()
        for (i in 1..4) dest.addLast(i.toFloat())
        repeat(2) { dest.removeFirst() }
        dest.addAll(src)
        assertEquals(listOf(3f, 4f, 7f, 8f, 9f, 10f), dest.toList())
    }

    @Test
    fun addAllDequeSelfDoublesContent() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        deque.addAll(deque)
        assertEquals(listOf(1f, 2f, 3f, 1f, 2f, 3f), deque.toList())
    }

    // --- ensureCapacity / trimToSize ---

    @Test
    fun ensureCapacityDoesNotLoseData() {
        val deque = FloatArrayDeque()
        for (i in 0 until 100) deque.addLast(i.toFloat())
        deque.ensureCapacity(200)
        assertEquals(100, deque.size)
        for (i in 0 until 100) assertEquals(i.toFloat(), deque.getAt(i))
    }

    @Test
    fun trimToSizePreservesElements() {
        val deque = FloatArrayDeque()
        for (i in 0 until 50) deque.addLast(i.toFloat())
        deque.ensureCapacity(200)
        deque.trimToSize()
        assertEquals(50, deque.size)
        for (i in 0 until 50) assertEquals(i.toFloat(), deque.getAt(i))
    }

    @Test
    fun trimToSizeOnEmptyDequeDoesNotThrow() {
        FloatArrayDeque().trimToSize()
    }

    // --- toFloatArray ---

    @Test
    fun toFloatArrayReturnsCorrectElements() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        assertEquals(floatArrayOf(1f, 2f, 3f).toList(), deque.toFloatArray().toList())
    }

    // --- equals / hashCode ---

    @Test
    fun equalsWithSameContent() {
        val a = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        val b = FloatArrayDeque(floatArrayOf(1f, 2f, 3f))
        assertEquals(a, b)
    }

    @Test
    fun notEqualsWithDifferentContent() {
        assertNotEquals(FloatArrayDeque(floatArrayOf(1f)), FloatArrayDeque(floatArrayOf(2f)))
    }

    @Test
    fun equalsWithStandardList() {
        assertEquals(FloatArrayDeque(floatArrayOf(1f, 2f, 3f)), listOf(1f, 2f, 3f))
    }

    @Test
    fun hashCodeConsistentWithEqualDeques() {
        val a = FloatArrayDeque(floatArrayOf(10f))
        val b = FloatArrayDeque(floatArrayOf(10f))
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun hashCodeConsistentAfterHeadShifts() {
        // head=0 deque and head>0 deque with identical logical content must agree.
        val fresh = FloatArrayDeque(floatArrayOf(2f, 3f))   // head=0
        val shifted = FloatArrayDeque()
        shifted.addLast(1f); shifted.addLast(2f); shifted.addLast(3f)
        shifted.removeFirst()                                // head=1, logical=[2,3]
        assertEquals(fresh.hashCode(), shifted.hashCode())
    }

    // --- iterator ---

    @Test
    fun iteratorTraversesAllElements() {
        val deque = FloatArrayDeque(floatArrayOf(1f, 2f, 3f, 4f, 5f))
        assertEquals(listOf(1f, 2f, 3f, 4f, 5f), deque.toList())
    }

    // --- ring-buffer correctness after wrap ---

    @Test
    fun correctAfterHeadWrapsAround() {
        val deque = FloatArrayDeque()
        for (i in 1..5) deque.addLast(i.toFloat())
        repeat(3) { deque.removeFirst() }       // head now at 3 (wrapped)
        deque.addLast(6f)
        deque.addLast(7f)
        deque.addFirst(0f)
        assertEquals(listOf(0f, 4f, 5f, 6f, 7f), deque.toList())
    }

    @Test
    fun toFloatArrayCorrectAfterWrap() {
        val deque = FloatArrayDeque()
        for (i in 1..5) deque.addLast(i.toFloat())
        repeat(3) { deque.removeFirst() }
        deque.addLast(6f); deque.addLast(7f)
        assertEquals(listOf(4f, 5f, 6f, 7f), deque.toFloatArray().toList())
    }

    @Test
    fun addAtIndexCorrectAfterWrap() {
        val deque = FloatArrayDeque()
        for (i in 1..4) deque.addLast(i.toFloat())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4], head wrapped
        deque.add(1, 99f)
        assertEquals(listOf(3f, 99f, 4f), deque.toList())
    }

    @Test
    fun removeAtCorrectAfterWrap() {
        val deque = FloatArrayDeque()
        for (i in 1..4) deque.addLast(i.toFloat())
        repeat(2) { deque.removeFirst() }       // logical: [3, 4]
        deque.addLast(5f); deque.addLast(6f)    // logical: [3, 4, 5, 6], wraps
        deque.removeAt(1)
        assertEquals(listOf(3f, 5f, 6f), deque.toList())
    }

    @Test
    fun removeRangeCorrectAfterWrap() {
        val deque = FloatArrayDeque()
        for (i in 1..6) deque.addLast(i.toFloat())
        repeat(3) { deque.removeFirst() }       // logical: [4, 5, 6]
        deque.addLast(7f); deque.addLast(8f)    // logical: [4, 5, 6, 7, 8], wraps
        deque.removeRange(1, 3)
        assertEquals(listOf(4f, 7f, 8f), deque.toList())
    }

    // --- sort / sortDescending ---

    @Test
    fun sortNonWrapped() {
        val deque = FloatArrayDeque(floatArrayOf(5f, 3f, 1f, 4f, 2f))
        deque.sort()
        assertEquals(listOf(1f, 2f, 3f, 4f, 5f), deque.toList())
    }

    @Test
    fun sortDescendingNonWrapped() {
        val deque = FloatArrayDeque(floatArrayOf(3f, 1f, 4f, 2f, 5f))
        deque.sortDescending()
        assertEquals(listOf(5f, 4f, 3f, 2f, 1f), deque.toList())
    }

    @Test
    fun sortWrappedHeadSegmentLarger() {
        // ring.size=8, head=5, size=5, tail=10 → end=2, ring.size-head=3 > end (case 1)
        val deque = FloatArrayDeque()
        for (i in 1..8) deque.addLast(i.toFloat())
        repeat(5) { deque.removeFirst() }    // head=5, logical=[6,7,8]
        deque.addLast(4f); deque.addLast(2f)  // logical=[6,7,8,4,2], tail wraps
        deque.sort()
        assertEquals(listOf(2f, 4f, 6f, 7f, 8f), deque.toList())
    }

    @Test
    fun sortWrappedTailSegmentLarger() {
        // ring.size=8, head=6, size=5, tail=11 → end=3, ring.size-head=2 ≤ end (case 2)
        val deque = FloatArrayDeque()
        for (i in 1..8) deque.addLast(i.toFloat())
        repeat(6) { deque.removeFirst() }       // head=6, logical=[7,8]
        deque.addLast(3f); deque.addLast(1f); deque.addLast(5f)  // logical=[7,8,3,1,5], tail wraps
        deque.sort()
        assertEquals(listOf(1f, 3f, 5f, 7f, 8f), deque.toList())
    }

    @Test
    fun sortDescendingWrapped() {
        // Same wrapped state as sortWrappedTailSegmentLarger, using sortDescending
        val deque = FloatArrayDeque()
        for (i in 1..8) deque.addLast(i.toFloat())
        repeat(6) { deque.removeFirst() }
        deque.addLast(3f); deque.addLast(1f); deque.addLast(5f)  // logical=[7,8,3,1,5]
        deque.sortDescending()
        assertEquals(listOf(8f, 7f, 5f, 3f, 1f), deque.toList())
    }
}
