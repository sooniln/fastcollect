package io.github.sooniln.fastcollect

import kotlin.test.*

// ============================= Int =============================

class IntPriorityQueueTest {
    @Test
    fun add_removeFirst_returnsAscendingOrder() {
        val queue = IntPriorityQueue()
        for (v in listOf(5, 3, 8, 1, 9, 2)) queue.add(v)

        assertEquals(6, queue.size)
        for (expected in listOf(1, 2, 3, 5, 8, 9)) {
            assertEquals(expected, queue.removeFirst())
        }
        assertTrue(queue.isEmpty())
    }

    @Test
    fun descending_removeFirst_returnsDescendingOrder() {
        val queue = IntPriorityQueue(descending = true)
        for (v in listOf(5, 3, 8, 1, 9, 2)) queue.add(v)

        for (expected in listOf(9, 8, 5, 3, 2, 1)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun first_doesNotRemove() {
        val queue = intPriorityQueueOf(4, 2, 6)
        assertEquals(2, queue.first())
        assertEquals(3, queue.size)
        assertEquals(2, queue.first())
    }

    @Test
    fun first_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { IntPriorityQueue().first() }
    }

    @Test
    fun removeFirst_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { IntPriorityQueue().removeFirst() }
    }

    @Test
    fun priorityQueueOf_vararg() {
        val queue = intPriorityQueueOf(7, 1, 4)
        assertEquals(3, queue.size)
        assertEquals(1, queue.removeFirst())
        assertEquals(4, queue.removeFirst())
        assertEquals(7, queue.removeFirst())
    }

    @Test
    fun buildPriorityQueue_dsl() {
        val queue = buildIntPriorityQueue {
            add(10)
            add(5)
            add(20)
        }
        assertEquals(5, queue.removeFirst())
        assertEquals(10, queue.removeFirst())
        assertEquals(20, queue.removeFirst())
    }

    @Test
    fun constructor_fromCollection_heapifies() {
        val source = mutableIntListOf(6, 1, 4, 2, 9)
        val queue = IntPriorityQueue(source)
        assertEquals(5, queue.size)
        for (expected in listOf(1, 2, 4, 6, 9)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun contains_findsPresentElements() {
        val queue = intPriorityQueueOf(3, 7, 1, 9)
        assertTrue(queue.contains(7))
        assertFalse(queue.contains(42))
    }

    @Test
    fun remove_nonRootElement_preservesHeapOrder() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        assertTrue(queue.remove(8))
        assertFalse(queue.contains(8))
        assertEquals(6, queue.size)

        val drained = mutableListOf<Int>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1, 2, 3, 5, 7, 9), drained)
    }

    @Test
    fun remove_missingElement_returnsFalse() {
        val queue = intPriorityQueueOf(1, 2, 3)
        assertFalse(queue.remove(42))
        assertEquals(3, queue.size)
    }

    @Test
    fun removeAll_removesMatchingElements_preservesHeapOrder() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        val modified = queue.removeAll(intSetOf(3, 9))

        assertTrue(modified)
        assertEquals(5, queue.size)
        val drained = mutableListOf<Int>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1, 2, 5, 7, 8), drained)
    }

    @Test
    fun removeAll_withBoxedCollection() {
        val queue = intPriorityQueueOf(5, 3, 8, 1)
        val modified = queue.removeAll(listOf(3, 1))

        assertTrue(modified)
        assertEquals(listOf(5, 8), listOf(queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun removeAll_withPredicate_removesMatchingElements() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        val modified = queue.removeAll { it > 6 }

        assertTrue(modified)
        val drained = mutableListOf<Int>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1, 2, 3, 5), drained)
    }

    @Test
    fun retainAll_keepsOnlyMatchingElements_preservesHeapOrder() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        val modified = queue.retainAll(intSetOf(3, 9, 1))

        assertTrue(modified)
        assertEquals(3, queue.size)
        val drained = mutableListOf<Int>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1, 3, 9), drained)
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val queue = intPriorityQueueOf(1, 2, 3)
        queue.ensureCapacity(100)
        assertEquals(3, queue.size)
        assertEquals(listOf(1, 2, 3), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val queue = IntPriorityQueue(100).apply { add(3); add(1); add(2) }
        queue.trimToSize()
        assertEquals(3, queue.size)
        assertEquals(listOf(1, 2, 3), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun iterator_visitsAllElements() {
        val queue = intPriorityQueueOf(5, 3, 8, 1)
        val visited = mutableListOf<Int>()
        val it = queue.iterator()
        while (it.hasNext()) visited.add(it.nextInt())

        assertEquals(listOf(5, 3, 8, 1).sorted(), visited.sorted())
    }

    @Test
    fun clear_emptiesQueue() {
        val queue = intPriorityQueueOf(1, 2, 3)
        queue.clear()
        assertTrue(queue.isEmpty())
        assertEquals(0, queue.size)
    }
}

// ================= Indirect priority queue (onIndexChanged / higherPriority / updatePriority) =================

private class IndirectIntPriorityQueue : AbstractIntPriorityQueue() {
    val priorities: MutableInt2IntMap = mutableInt2IntMapOf()
    val indexOf: MutableInt2IntMap = mutableInt2IntMapOf()

    override fun isHigherPriority(element1: Int, element2: Int): Boolean {
        return priorities.getValue(element1) < priorities.getValue(element2)
    }

    override fun onIndexChanged(element: Int, index: Int) {
        indexOf[element] = index
    }

    fun addWithPriority(handle: Int, priority: Int) {
        priorities[handle] = priority
        add(handle)
    }

    fun decreasePriority(handle: Int, newPriority: Int) {
        priorities[handle] = newPriority
        updatePriority(indexOf.getValue(handle))
    }
}

class IndirectIntPriorityQueueTest {
    @Test
    fun updatePriority_reordersHeap_byExternalPriority() {
        val queue = IndirectIntPriorityQueue()
        queue.addWithPriority(1, 50)
        queue.addWithPriority(2, 30)
        queue.addWithPriority(3, 70)
        queue.addWithPriority(4, 10)
        assertEquals(4, queue.first())

        queue.decreasePriority(3, 1)
        assertEquals(3, queue.first())

        queue.decreasePriority(4, 5)
        assertEquals(3, queue.first())

        val drained = mutableListOf<Int>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(3, 4, 2, 1), drained)
    }

    @Test
    fun onIndexChanged_tracksActualHeapPositions() {
        val queue = IndirectIntPriorityQueue()
        for ((handle, priority) in listOf(1 to 50, 2 to 30, 3 to 70, 4 to 10, 5 to 25)) {
            queue.addWithPriority(handle, priority)
        }
        queue.decreasePriority(3, 1)

        val heapContents = mutableListOf<Int>()
        val it = queue.iterator()
        while (it.hasNext()) heapContents.add(it.nextInt())

        for (handle in 1..5) {
            assertEquals(handle, heapContents[queue.indexOf.getValue(handle)])
        }
    }
}

// ============================= Long =============================

class LongPriorityQueueTest {
    @Test
    fun add_removeFirst_returnsAscendingOrder() {
        val queue = LongPriorityQueue()
        for (v in listOf(5L, 3L, 8L, 1L, 9L, 2L)) queue.add(v)

        for (expected in listOf(1L, 2L, 3L, 5L, 8L, 9L)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun descending_removeFirst_returnsDescendingOrder() {
        val queue = LongPriorityQueue(descending = true)
        for (v in listOf(5L, 3L, 8L, 1L, 9L, 2L)) queue.add(v)

        for (expected in listOf(9L, 8L, 5L, 3L, 2L, 1L)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun first_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { LongPriorityQueue().first() }
    }

    @Test
    fun removeFirst_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { LongPriorityQueue().removeFirst() }
    }

    @Test
    fun priorityQueueOf_vararg() {
        val queue = longPriorityQueueOf(7L, 1L, 4L)
        assertEquals(1L, queue.removeFirst())
        assertEquals(4L, queue.removeFirst())
        assertEquals(7L, queue.removeFirst())
    }

    @Test
    fun buildPriorityQueue_dsl() {
        val queue = buildLongPriorityQueue {
            add(10L)
            add(5L)
            add(20L)
        }
        assertEquals(5L, queue.removeFirst())
        assertEquals(10L, queue.removeFirst())
        assertEquals(20L, queue.removeFirst())
    }

    @Test
    fun constructor_fromCollection_heapifies() {
        val source = mutableLongListOf(6L, 1L, 4L, 2L, 9L)
        val queue = LongPriorityQueue(source)
        for (expected in listOf(1L, 2L, 4L, 6L, 9L)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun remove_nonRootElement_preservesHeapOrder() {
        val queue = longPriorityQueueOf(5L, 3L, 8L, 1L, 9L, 2L, 7L)
        assertTrue(queue.remove(8L))
        assertFalse(queue.contains(8L))

        val drained = mutableListOf<Long>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1L, 2L, 3L, 5L, 7L, 9L), drained)
    }

    @Test
    fun removeAll_removesMatchingElements_preservesHeapOrder() {
        val queue = longPriorityQueueOf(5L, 3L, 8L, 1L, 9L, 2L, 7L)
        assertTrue(queue.removeAll(longSetOf(3L, 9L)))

        val drained = mutableListOf<Long>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1L, 2L, 5L, 7L, 8L), drained)
    }

    @Test
    fun removeAll_withPredicate_removesMatchingElements() {
        val queue = longPriorityQueueOf(5L, 3L, 8L, 1L, 9L, 2L, 7L)
        val modified = queue.removeAll { it > 6L }

        assertTrue(modified)
        val drained = mutableListOf<Long>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1L, 2L, 3L, 5L), drained)
    }

    @Test
    fun retainAll_keepsOnlyMatchingElements_preservesHeapOrder() {
        val queue = longPriorityQueueOf(5L, 3L, 8L, 1L, 9L, 2L, 7L)
        assertTrue(queue.retainAll(longSetOf(3L, 9L, 1L)))

        val drained = mutableListOf<Long>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1L, 3L, 9L), drained)
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val queue = longPriorityQueueOf(1L, 2L, 3L)
        queue.ensureCapacity(100)
        assertEquals(listOf(1L, 2L, 3L), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val queue = LongPriorityQueue(100).apply { add(3L); add(1L); add(2L) }
        queue.trimToSize()
        assertEquals(listOf(1L, 2L, 3L), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun iterator_visitsAllElements() {
        val queue = longPriorityQueueOf(5L, 3L, 8L, 1L)
        val visited = mutableListOf<Long>()
        val it = queue.iterator()
        while (it.hasNext()) visited.add(it.nextLong())

        assertEquals(listOf(5L, 3L, 8L, 1L).sorted(), visited.sorted())
    }
}

// ============================= Float =============================

class FloatPriorityQueueTest {
    @Test
    fun add_removeFirst_returnsAscendingOrder() {
        val queue = FloatPriorityQueue()
        for (v in listOf(5f, 3f, 8f, 1f, 9f, 2f)) queue.add(v)

        for (expected in listOf(1f, 2f, 3f, 5f, 8f, 9f)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun descending_removeFirst_returnsDescendingOrder() {
        val queue = FloatPriorityQueue(descending = true)
        for (v in listOf(5f, 3f, 8f, 1f, 9f, 2f)) queue.add(v)

        for (expected in listOf(9f, 8f, 5f, 3f, 2f, 1f)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun first_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { FloatPriorityQueue().first() }
    }

    @Test
    fun removeFirst_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { FloatPriorityQueue().removeFirst() }
    }

    @Test
    fun priorityQueueOf_vararg() {
        val queue = floatPriorityQueueOf(7f, 1f, 4f)
        assertEquals(1f, queue.removeFirst())
        assertEquals(4f, queue.removeFirst())
        assertEquals(7f, queue.removeFirst())
    }

    @Test
    fun buildPriorityQueue_dsl() {
        val queue = buildFloatPriorityQueue {
            add(10f)
            add(5f)
            add(20f)
        }
        assertEquals(5f, queue.removeFirst())
        assertEquals(10f, queue.removeFirst())
        assertEquals(20f, queue.removeFirst())
    }

    @Test
    fun constructor_fromCollection_heapifies() {
        val source = mutableFloatListOf(6f, 1f, 4f, 2f, 9f)
        val queue = FloatPriorityQueue(source)
        for (expected in listOf(1f, 2f, 4f, 6f, 9f)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun remove_nonRootElement_preservesHeapOrder() {
        val queue = floatPriorityQueueOf(5f, 3f, 8f, 1f, 9f, 2f, 7f)
        assertTrue(queue.remove(8f))
        assertFalse(queue.contains(8f))

        val drained = mutableListOf<Float>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1f, 2f, 3f, 5f, 7f, 9f), drained)
    }

    @Test
    fun removeAll_removesMatchingElements_preservesHeapOrder() {
        val queue = floatPriorityQueueOf(5f, 3f, 8f, 1f, 9f, 2f, 7f)
        assertTrue(queue.removeAll(floatSetOf(3f, 9f)))

        val drained = mutableListOf<Float>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1f, 2f, 5f, 7f, 8f), drained)
    }

    @Test
    fun removeAll_withPredicate_removesMatchingElements() {
        val queue = floatPriorityQueueOf(5f, 3f, 8f, 1f, 9f, 2f, 7f)
        val modified = queue.removeAll { it > 6f }

        assertTrue(modified)
        val drained = mutableListOf<Float>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1f, 2f, 3f, 5f), drained)
    }

    @Test
    fun retainAll_keepsOnlyMatchingElements_preservesHeapOrder() {
        val queue = floatPriorityQueueOf(5f, 3f, 8f, 1f, 9f, 2f, 7f)
        assertTrue(queue.retainAll(floatSetOf(3f, 9f, 1f)))

        val drained = mutableListOf<Float>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1f, 3f, 9f), drained)
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val queue = floatPriorityQueueOf(1f, 2f, 3f)
        queue.ensureCapacity(100)
        assertEquals(listOf(1f, 2f, 3f), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val queue = FloatPriorityQueue(100).apply { add(3f); add(1f); add(2f) }
        queue.trimToSize()
        assertEquals(listOf(1f, 2f, 3f), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun iterator_visitsAllElements() {
        val queue = floatPriorityQueueOf(5f, 3f, 8f, 1f)
        val visited = mutableListOf<Float>()
        val it = queue.iterator()
        while (it.hasNext()) visited.add(it.nextFloat())

        assertEquals(listOf(5f, 3f, 8f, 1f).sorted(), visited.sorted())
    }
}

// ============================= Double =============================

class DoublePriorityQueueTest {
    @Test
    fun add_removeFirst_returnsAscendingOrder() {
        val queue = DoublePriorityQueue()
        for (v in listOf(5.0, 3.0, 8.0, 1.0, 9.0, 2.0)) queue.add(v)

        for (expected in listOf(1.0, 2.0, 3.0, 5.0, 8.0, 9.0)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun descending_removeFirst_returnsDescendingOrder() {
        val queue = DoublePriorityQueue(descending = true)
        for (v in listOf(5.0, 3.0, 8.0, 1.0, 9.0, 2.0)) queue.add(v)

        for (expected in listOf(9.0, 8.0, 5.0, 3.0, 2.0, 1.0)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun first_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { DoublePriorityQueue().first() }
    }

    @Test
    fun removeFirst_emptyQueue_throws() {
        assertFailsWith<NoSuchElementException> { DoublePriorityQueue().removeFirst() }
    }

    @Test
    fun priorityQueueOf_vararg() {
        val queue = doublePriorityQueueOf(7.0, 1.0, 4.0)
        assertEquals(1.0, queue.removeFirst())
        assertEquals(4.0, queue.removeFirst())
        assertEquals(7.0, queue.removeFirst())
    }

    @Test
    fun buildPriorityQueue_dsl() {
        val queue = buildDoublePriorityQueue {
            add(10.0)
            add(5.0)
            add(20.0)
        }
        assertEquals(5.0, queue.removeFirst())
        assertEquals(10.0, queue.removeFirst())
        assertEquals(20.0, queue.removeFirst())
    }

    @Test
    fun constructor_fromCollection_heapifies() {
        val source = mutableDoubleListOf(6.0, 1.0, 4.0, 2.0, 9.0)
        val queue = DoublePriorityQueue(source)
        for (expected in listOf(1.0, 2.0, 4.0, 6.0, 9.0)) {
            assertEquals(expected, queue.removeFirst())
        }
    }

    @Test
    fun remove_nonRootElement_preservesHeapOrder() {
        val queue = doublePriorityQueueOf(5.0, 3.0, 8.0, 1.0, 9.0, 2.0, 7.0)
        assertTrue(queue.remove(8.0))
        assertFalse(queue.contains(8.0))

        val drained = mutableListOf<Double>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1.0, 2.0, 3.0, 5.0, 7.0, 9.0), drained)
    }

    @Test
    fun removeAll_removesMatchingElements_preservesHeapOrder() {
        val queue = doublePriorityQueueOf(5.0, 3.0, 8.0, 1.0, 9.0, 2.0, 7.0)
        assertTrue(queue.removeAll(doubleSetOf(3.0, 9.0)))

        val drained = mutableListOf<Double>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1.0, 2.0, 5.0, 7.0, 8.0), drained)
    }

    @Test
    fun removeAll_withPredicate_removesMatchingElements() {
        val queue = doublePriorityQueueOf(5.0, 3.0, 8.0, 1.0, 9.0, 2.0, 7.0)
        val modified = queue.removeAll { it > 6.0 }

        assertTrue(modified)
        val drained = mutableListOf<Double>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1.0, 2.0, 3.0, 5.0), drained)
    }

    @Test
    fun retainAll_keepsOnlyMatchingElements_preservesHeapOrder() {
        val queue = doublePriorityQueueOf(5.0, 3.0, 8.0, 1.0, 9.0, 2.0, 7.0)
        assertTrue(queue.retainAll(doubleSetOf(3.0, 9.0, 1.0)))

        val drained = mutableListOf<Double>()
        while (!queue.isEmpty()) drained.add(queue.removeFirst())
        assertEquals(listOf(1.0, 3.0, 9.0), drained)
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val queue = doublePriorityQueueOf(1.0, 2.0, 3.0)
        queue.ensureCapacity(100)
        assertEquals(listOf(1.0, 2.0, 3.0), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val queue = DoublePriorityQueue(100).apply { add(3.0); add(1.0); add(2.0) }
        queue.trimToSize()
        assertEquals(listOf(1.0, 2.0, 3.0), listOf(queue.removeFirst(), queue.removeFirst(), queue.removeFirst()))
    }

    @Test
    fun iterator_visitsAllElements() {
        val queue = doublePriorityQueueOf(5.0, 3.0, 8.0, 1.0)
        val visited = mutableListOf<Double>()
        val it = queue.iterator()
        while (it.hasNext()) visited.add(it.nextDouble())

        assertEquals(listOf(5.0, 3.0, 8.0, 1.0).sorted(), visited.sorted())
    }
}
