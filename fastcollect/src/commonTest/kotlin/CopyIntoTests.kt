package io.github.sooniln.fastcollect

import kotlin.test.*

class IntCopyIntoTest {

    @Test
    fun copyInto_exactFitDestination() {
        val destination = IntArray(3)
        mutableIntListOf(1, 2, 3).copyInto(destination)

        assertEquals(listOf(1, 2, 3), destination.toList())
    }

    @Test
    fun copyInto_destinationOffset_leavesSurroundingSlotsUntouched() {
        val destination = IntArray(7) { -1 }
        mutableIntListOf(1, 2, 3).copyInto(destination, 2)

        assertEquals(listOf(-1, -1, 1, 2, 3, -1, -1), destination.toList())
    }

    @Test
    fun copyInto_emptyCollection() {
        val destination = IntArray(2) { -1 }
        mutableIntListOf().copyInto(destination, 1)
        assertEquals(listOf(-1, -1), destination.toList())
    }

    @Test
    fun copyInto_range() {
        val destination = IntArray(3)
        mutableIntListOf(1, 2, 3, 4, 5).copyInto(destination, 0, 2, 5)

        assertEquals(listOf(3, 4, 5), destination.toList())
    }

    @Test
    fun copyInto_emptyRange() {
        val destination = IntArray(2) { -1 }
        mutableIntListOf(1, 2, 3).copyInto(destination, 0, 2, 2)

        assertEquals(listOf(-1, -1), destination.toList())
    }

    @Test
    fun copyInto_rangeAndDestinationOffset() {
        val destination = IntArray(5) { -1 }
        mutableIntListOf(1, 2, 3, 4, 5).copyInto(destination, 1, 1, 4)

        assertEquals(listOf(-1, 2, 3, 4, -1), destination.toList())
    }

    @Test
    fun copyInto_arrayBackedList() {
        val destination = IntArray(2)
        intArrayOf(1, 2, 3, 4).asIntList().copyInto(destination, 0, 1, 3)

        assertEquals(listOf(2, 3), destination.toList())
    }

    @Test
    fun copyInto_subListView() {
        val destination = IntArray(2)
        mutableIntListOf(1, 2, 3, 4, 5).subList(1, 4).copyInto(destination, 0, 1, 3)

        assertEquals(listOf(3, 4), destination.toList())
    }

    @Test
    fun copyInto_set() {
        val destination = IntArray(3)
        mutableIntSetOf(1, 2, 3).copyInto(destination)

        assertEquals(listOf(1, 2, 3), destination.toList().sorted())
    }

    @Test
    fun copyInto_priorityQueue_copiesEveryElement() {
        val queue = IntPriorityQueue()
        for (v in listOf(5, 3, 8, 1, 9, 2)) queue.add(v)

        val destination = IntArray(8) { -1 }
        queue.copyInto(destination, 1)

        // heap order is unspecified, but every element must land in [1, 7)
        assertEquals(listOf(1, 2, 3, 5, 8, 9), destination.copyOfRange(1, 7).sorted())
        assertEquals(-1, destination[0])
        assertEquals(-1, destination[7])
    }

    @Test
    fun copyInto_negativeDestinationOffset_throws() {
        val destination = IntArray(3) { -1 }
        assertFailsWith<IndexOutOfBoundsException> { mutableIntListOf(1).copyInto(destination, -1) }
        assertEquals(listOf(-1, -1, -1), destination.toList())
    }

    @Test
    fun copyInto_destinationTooSmall_throwsWithoutPartiallyWriting() {
        val destination = IntArray(2) { -1 }
        assertFailsWith<IndexOutOfBoundsException> { mutableIntListOf(1, 2, 3).copyInto(destination) }
        assertEquals(listOf(-1, -1), destination.toList())
    }

    @Test
    fun copyInto_destinationTooSmallForOffset_throwsWithoutPartiallyWriting() {
        val destination = IntArray(3) { -1 }
        assertFailsWith<IndexOutOfBoundsException> { mutableIntListOf(1, 2).copyInto(destination, 2) }
        assertEquals(listOf(-1, -1, -1), destination.toList())
    }

    @Test
    fun copyInto_setDestinationTooSmall_throwsWithoutPartiallyWriting() {
        val destination = IntArray(2) { -1 }
        assertFailsWith<IndexOutOfBoundsException> { mutableIntSetOf(1, 2, 3).copyInto(destination) }
        assertEquals(listOf(-1, -1), destination.toList())
    }

    @Test
    fun copyInto_priorityQueueDestinationTooSmall_throwsWithoutPartiallyWriting() {
        val queue = IntPriorityQueue()
        for (v in listOf(1, 2, 3)) queue.add(v)

        val destination = IntArray(2) { -1 }
        assertFailsWith<IndexOutOfBoundsException> { queue.copyInto(destination) }
        assertEquals(listOf(-1, -1), destination.toList())
    }

    @Test
    fun copyInto_toIndexPastSize_throws() {
        assertFailsWith<IndexOutOfBoundsException> { mutableIntListOf(1, 2, 3).copyInto(IntArray(5), 0, 0, 4) }
    }

    @Test
    fun copyInto_negativeFromIndex_throws() {
        assertFailsWith<IndexOutOfBoundsException> { mutableIntListOf(1, 2, 3).copyInto(IntArray(5), 0, -1, 2) }
    }

    @Test
    fun copyInto_fromIndexGreaterThanToIndex_throws() {
        assertFailsWith<IllegalArgumentException> { mutableIntListOf(1, 2, 3).copyInto(IntArray(5), 0, 2, 1) }
    }

}

// ================== the un-overridden IntList.copyInto ==================

// IntCopyIntoTest above only reaches list types that override copyInto(dest, off, from, to): IntArrayDeque, the
// IntArray wrapper, and the sublist views. These cover the default implementation on IntList itself.
class DefaultListCopyIntoTest {

    @Test
    fun emptyList_zeroLengthDestination() {
        val destination = IntArray(0)
        emptyIntList().copyInto(destination)
    }

    @Test
    fun emptyList_leavesDestinationUntouched() {
        val destination = IntArray(2) { -1 }
        emptyIntList().copyInto(destination, 1)
        assertEquals(listOf(-1, -1), destination.toList())
    }

    @Test
    fun singletonList_copiesExactlyOneElement() {
        val destination = IntArray(3) { -1 }
        intListOf(7).copyInto(destination, 1)
        assertEquals(listOf(-1, 7, -1), destination.toList())
    }

    @Test
    fun singletonList_emptyRange() {
        val destination = IntArray(2) { -1 }
        intListOf(7).copyInto(destination, 0, 1, 1)
        assertEquals(listOf(-1, -1), destination.toList())
    }

    @Test
    fun singletonList_destinationTooSmall_throws() {
        assertFailsWith<IndexOutOfBoundsException> { intListOf(7).copyInto(IntArray(0)) }
    }

    // IntArrayDeque(IntCollection) copies through elements.copyInto(), so it goes through the default
    // implementation whenever it is handed a list that does not override it
    @Test
    fun collectionConstructor_acceptsListsUsingTheDefault() {
        assertEquals(listOf(7), IntArrayDeque(intListOf(7)).asList())
        assertEquals(emptyList<Int>(), IntArrayDeque(emptyIntList()).asList())
    }

    // the default is generated once per primitive, so each expansion gets a smoke check here; the deep
    // behavioural cases above only need Int
    @Test
    fun otherPrimitives() {
        assertEquals(listOf<Byte>(-1, 7), byteListOf(7).copyInto(ByteArray(2) { -1 }, 1).toList())
        assertEquals(listOf<Long>(-1, 7), longListOf(7L).copyInto(LongArray(2) { -1 }, 1).toList())
        assertEquals(listOf(-1f, 7f), floatListOf(7f).copyInto(FloatArray(2) { -1f }, 1).toList())
        assertEquals(listOf(-1.0, 7.0), doubleListOf(7.0).copyInto(DoubleArray(2) { -1.0 }, 1).toList())
    }
}
