package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Deep coverage for PriorityQueue.kte. Int stands in for the whole expansion; the other primitives are covered by
 * PriorityQueueConformanceTests, and the Float/Double NaN ordering rules live in FloatDoubleSemanticsTests.
 *
 * These are the deterministic edge cases. Long random operation sequences diffed against a sorted reference list
 * live in RandomizedWorkloadTests.
 */
class IntPriorityQueueTests {

    // ---------- construction ----------

    @Test
    fun constructors_produceTheSameHeapContents() {
        assertTrue(IntPriorityQueue().isEmpty())
        assertTrue(IntPriorityQueue(16).isEmpty())

        val expected = listOf(1, 2, 4, 6, 9)
        assertEquals(expected, IntPriorityQueue(intArrayOf(6, 1, 4, 2, 9)).drain())
        assertEquals(expected, IntPriorityQueue(intArrayOf(0, 6, 1, 4, 2, 9, 0), 1, 6).drain())
        assertEquals(expected, IntPriorityQueue(intListOf(6, 1, 4, 2, 9)).drain())
        assertEquals(expected, IntPriorityQueue(listOf(6, 1, 4, 2, 9)).drain())
        assertEquals(expected, intPriorityQueueOf(6, 1, 4, 2, 9).drain())
        assertEquals(expected.reversed(), intDescendingPriorityQueueOf(6, 1, 4, 2, 9).drain())
        assertEquals(expected, buildIntPriorityQueue { addAll(intListOf(6, 1, 4, 2, 9)) }.drain())
        assertEquals(expected.reversed(), buildIntPriorityQueue(descending = true) { addAll(intListOf(6, 1, 4, 2, 9)) }.drain())
    }

    @Test
    fun constructor_fromCollection_ordersEveryElement() {
        // building from an existing collection is a different code path from N successive adds
        val source = mutableIntListOf(6, 1, 4, 2, 9, 3, 8, 7, 5)
        assertEquals(source.asList().sorted(), IntPriorityQueue(source).drain())
    }

    // ---------- ordering ----------

    @Test
    fun add_removeFirst_returnsAscendingOrder() {
        val queue = IntPriorityQueue()
        for (v in listOf(5, 3, 8, 1, 9, 2)) queue.add(v)
        assertEquals(listOf(1, 2, 3, 5, 8, 9), queue.drain())
    }

    @Test
    fun descending_removeFirst_returnsDescendingOrder() {
        val queue = IntPriorityQueue(descending = true)
        for (v in listOf(5, 3, 8, 1, 9, 2)) queue.add(v)
        assertEquals(listOf(9, 8, 5, 3, 2, 1), queue.drain())
    }

    @Test
    fun duplicateElements_areAllRetained() {
        val queue = intPriorityQueueOf(3, 1, 3, 1, 2)
        assertEquals(listOf(1, 1, 2, 3, 3), queue.drain())
    }

    // ---------- reads ----------

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
    fun contains_findsPresentElementsOnly() {
        val queue = intPriorityQueueOf(3, 7, 1, 9)
        assertTrue(queue.contains(7))
        assertTrue(queue.contains(1))
        assertFalse(queue.contains(4))
        assertFalse(IntPriorityQueue().contains(0))
    }

    @Test
    fun containsAll_checksEveryElement() {
        val queue = intPriorityQueueOf(3, 7, 1, 9)
        assertTrue(queue.containsAll(intListOf(1, 9)))
        assertTrue(queue.containsAll(listOf(1, 9)))
        assertFalse(queue.containsAll(intListOf(1, 4)))
    }

    // ---------- mutation ----------

    @Test
    fun remove_anyElement_leavesTheRestInOrder() {
        // the smallest, the largest and an interior element all have to leave the queue sorted
        for (removed in listOf(1, 8, 9)) {
            val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
            assertTrue(queue.remove(removed), "remove($removed)")
            assertFalse(queue.contains(removed), "remove($removed)")
            assertEquals(listOf(1, 2, 3, 5, 7, 8, 9) - removed, queue.drain(), "remove($removed)")
        }
    }

    @Test
    fun remove_missingElement_returnsFalse() {
        val queue = intPriorityQueueOf(1, 2, 3)
        assertFalse(queue.remove(4))
        assertEquals(3, queue.size)
    }

    @Test
    fun remove_onlyRemovesOneOccurrence() {
        val queue = intPriorityQueueOf(1, 2, 2, 3)
        assertTrue(queue.remove(2))
        assertEquals(listOf(1, 2, 3), queue.drain())
    }

    @Test
    fun addAll_acceptsEveryOverload() {
        val queue = IntPriorityQueue()
        queue.addAll(intArrayOf(5, 3))
        queue.addAll(intArrayOf(0, 8, 1, 0), 1, 3)
        queue.addAll(intListOf(9))
        queue.addAll(listOf(2))
        assertEquals(listOf(1, 2, 3, 5, 8, 9), queue.drain())
    }

    @Test
    fun removeAll_removesMatchingElements_preservesHeapOrder() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        assertTrue(queue.removeAll(intSetOf(3, 9)))
        assertEquals(5, queue.size)
        assertEquals(listOf(1, 2, 5, 7, 8), queue.drain())
    }

    @Test
    fun removeAll_withBoxedCollection() {
        val queue = intPriorityQueueOf(5, 3, 8, 1)
        assertTrue(queue.removeAll(listOf(3, 1)))
        assertEquals(listOf(5, 8), queue.drain())
    }

    @Test
    fun removeAll_withPredicate_removesMatchingElements() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        assertTrue(queue.removeAll { it > 6 })
        assertEquals(listOf(1, 2, 3, 5), queue.drain())
    }

    @Test
    fun removeAll_matchingNothing_returnsFalse() {
        val queue = intPriorityQueueOf(1, 2, 3)
        assertFalse(queue.removeAll(intSetOf(9)))
        assertFalse(queue.removeAll { it > 100 })
        assertEquals(3, queue.size)
    }

    @Test
    fun retainAll_keepsOnlyMatchingElements_preservesHeapOrder() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        assertTrue(queue.retainAll(intSetOf(3, 9, 1)))
        assertEquals(listOf(1, 3, 9), queue.drain())
    }

    @Test
    fun retainAll_withPredicateAndBoxedCollection() {
        val predicate = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        assertTrue(predicate.retainAll { it % 2 == 0 })
        assertEquals(listOf(2, 8), predicate.drain())

        val boxed = intPriorityQueueOf(5, 3, 8, 1)
        assertTrue(boxed.retainAll(listOf(3, 1)))
        assertEquals(listOf(1, 3), boxed.drain())
    }

    @Test
    fun retainAll_keepingEverything_returnsFalse() {
        val queue = intPriorityQueueOf(1, 2, 3)
        assertFalse(queue.retainAll { true })
        assertEquals(3, queue.size)
    }

    @Test
    fun clear_emptiesQueue() {
        val queue = intPriorityQueueOf(1, 2, 3)
        queue.clear()
        assertTrue(queue.isEmpty())
        assertEquals(0, queue.size)
        queue.add(9)
        assertEquals(listOf(9), queue.drain())
    }

    // ---------- capacity ----------

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val queue = intPriorityQueueOf(1, 2, 3)
        queue.ensureCapacity(10_000)
        assertEquals(3, queue.size)
        assertEquals(listOf(1, 2, 3), queue.drain())
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val queue = IntPriorityQueue(1000)
        for (v in listOf(3, 1, 2)) queue.add(v)
        queue.trimToSize()
        assertEquals(listOf(1, 2, 3), queue.drain())
    }

    @Test
    fun growth_preservesOrderAcrossManyAdds() {
        val queue = IntPriorityQueue(1)
        for (i in 1000 downTo 1) queue.add(i)
        assertEquals((1..1000).toList(), queue.drain())
    }

    // ---------- iteration ----------

    @Test
    fun iterator_visitsAllElements() {
        val queue = intPriorityQueueOf(5, 3, 8, 1)
        // heap order is unspecified, so compare as a multiset
        assertEquals(listOf(1, 3, 5, 8), queue.toBoxedList().sorted())
    }

    @Test
    fun traverse_visitsAllElementsExactlyOnce() {
        val queue = intPriorityQueueOf(5, 3, 8, 1, 9, 2, 7)
        val visited = mutableListOf<Int>()
        queue.traverse { visited.add(it) }
        assertEquals(listOf(1, 2, 3, 5, 7, 8, 9), visited.sorted())
    }

    @Test
    fun traverse_emptyQueue_visitsNothing() {
        val visited = mutableListOf<Int>()
        IntPriorityQueue().traverse { visited.add(it) }
        assertEquals(emptyList(), visited)
        assertFalse(IntPriorityQueue().traverser().forward())
    }

    @Test
    fun copyInto_copiesEveryElement() {
        val queue = intPriorityQueueOf(5, 3, 8, 1)
        val destination = IntArray(6)
        queue.copyInto(destination, 1)
        assertEquals(listOf(1, 3, 5, 8), destination.slice(1..4).sorted())
        assertEquals(listOf(0, 0), listOf(destination[0], destination[5]))
    }

    @Test
    fun toString_rendersEveryElement() {
        assertEquals("[]", IntPriorityQueue().toString())
        assertEquals("[7]", intPriorityQueueOf(7).toString())
        val rendered = intPriorityQueueOf(3, 1, 2).toString()
        assertEquals(listOf("1", "2", "3"), rendered.removeSurrounding("[", "]").split(", ").sorted())
    }
}

// ================= Subclassing AbstractIntPriorityQueue with an external priority table =================

private class IndirectIntPriorityQueue : AbstractIntPriorityQueue() {
    val priorities: MutableInt2IntMap = mutableInt2IntMapOf()
    val indexOf: MutableInt2IntMap = mutableInt2IntMapOf()
    val removed: MutableIntList = mutableIntListOf()

    override fun isHigherPriority(element1: Int, element2: Int): Boolean {
        return priorities.getValue(element1) < priorities.getValue(element2)
    }

    override fun onIndexChanged(element: Int, index: Int) {
        indexOf[element] = index
    }

    override fun onRemoved(element: Int, index: Int) {
        removed.add(element)
        indexOf.remove(element)
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

class IndirectIntPriorityQueueTests {

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

        assertEquals(listOf(3, 4, 2, 1), queue.drain())
    }

    @Test
    fun updatePriority_alsoSiftsDown() {
        val queue = IndirectIntPriorityQueue()
        queue.addWithPriority(1, 10)
        queue.addWithPriority(2, 20)
        queue.addWithPriority(3, 30)
        assertEquals(1, queue.first())

        // demoting the current root must push it back down the heap
        queue.decreasePriority(1, 99)
        assertEquals(2, queue.first())
        assertEquals(listOf(2, 3, 1), queue.drain())
    }

    @Test
    fun onRemoved_firesOnceForEveryElementLeavingTheHeap() {
        val queue = IndirectIntPriorityQueue()
        for ((handle, priority) in listOf(1 to 50, 2 to 30, 3 to 70, 4 to 10)) {
            queue.addWithPriority(handle, priority)
        }

        assertEquals(4, queue.removeFirst())
        assertTrue(queue.remove(3))
        assertEquals(listOf(4, 3), queue.removed.asList())
        assertFalse(queue.indexOf.containsKey(4))
        assertFalse(queue.indexOf.containsKey(3))

        queue.clear()
        assertEquals(listOf(4, 3, 2, 1), queue.removed.asList().sortedBy { listOf(4, 3, 2, 1).indexOf(it) })
        assertEquals(4, queue.removed.size)
    }
}
