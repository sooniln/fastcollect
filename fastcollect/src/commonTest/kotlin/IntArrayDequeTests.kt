package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Deep coverage for ArrayDeque.kte. Int stands in for the whole expansion: every other primitive is generated
 * from this same template and is covered by ListConformanceTests plus the type-semantics suites.
 *
 * These are the deterministic edge cases. Long random operation sequences diffed against a reference list -
 * which is what actually drives the deque through its awkward internal states - live in RandomizedWorkloadTests.
 */
class IntArrayDequeTests {

    // ---------- construction ----------

    @Test
    fun constructors_produceExpectedContents() {
        IntArrayDeque().assertContents()
        IntArrayDeque(16).assertContents()
        IntArrayDeque(intArrayOf(1, 2, 3)).assertContents(1, 2, 3)
        IntArrayDeque(intArrayOf(1, 2, 3, 4, 5), 1, 4).assertContents(2, 3, 4)
        IntArrayDeque(intListOf(7, 8)).assertContents(7, 8)
        IntArrayDeque(listOf(7, 8)).assertContents(7, 8)
    }

    @Test
    fun constructor_negativeCapacity_throws() {
        assertFailsWith<IllegalArgumentException> { IntArrayDeque(-1) }
    }

    @Test
    fun arrayListTypealias_isTheSameClass() {
        val list: IntArrayDeque = IntArrayList(intArrayOf(1, 2))
        list.assertContents(1, 2)
    }

    // ---------- ends ----------

    @Test
    fun addFirst_prependsElement() {
        val list = mutableIntListOf(2, 3)
        list.addFirst(1)
        list.assertContents(1, 2, 3)
    }

    @Test
    fun addLast_appendsElement() {
        val list = mutableIntListOf(1, 2)
        list.addLast(3)
        list.assertContents(1, 2, 3)
    }

    @Test
    fun removeFirst_returnsAndRemoves() {
        val list = mutableIntListOf(10, 20, 30)
        assertEquals(10, list.removeFirst())
        list.assertContents(20, 30)
    }

    @Test
    fun removeLast_returnsAndRemoves() {
        val list = mutableIntListOf(10, 20, 30)
        assertEquals(30, list.removeLast())
        list.assertContents(10, 20)
    }

    @Test
    fun removeFirst_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableIntListOf().removeFirst() }
    }

    @Test
    fun removeLast_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableIntListOf().removeLast() }
    }

    // ---------- indexed access ----------

    @Test
    fun get_outOfBounds_throws() {
        val list = mutableIntListOf(1, 2, 3)
        assertFailsWith<IndexOutOfBoundsException> { list[3] }
        assertFailsWith<IndexOutOfBoundsException> { list[-1] }
    }

    @Test
    fun set_outOfBounds_throws() {
        val list = mutableIntListOf(1, 2, 3)
        assertFailsWith<IndexOutOfBoundsException> { list[3] = 0 }
    }

    @Test
    fun replace_returnsOldValue() {
        val list = mutableIntListOf(1, 2, 3)
        assertEquals(2, list.replace(1, 99))
        list.assertContents(1, 99, 3)
    }

    @Test
    fun addAtIndex_insertsNearEitherEnd() {
        val front = mutableIntListOf(1, 2, 3, 4, 5, 6)
        front.add(1, 99)
        front.assertContents(1, 99, 2, 3, 4, 5, 6)

        val back = mutableIntListOf(1, 2, 3, 4, 5, 6)
        back.add(5, 99)
        back.assertContents(1, 2, 3, 4, 5, 99, 6)
    }

    @Test
    fun addAtIndex_atBothBoundaries() {
        val list = mutableIntListOf(2, 3)
        list.add(0, 1)
        list.add(3, 4)
        list.assertContents(1, 2, 3, 4)
        assertFailsWith<IndexOutOfBoundsException> { list.add(5, 0) }
    }

    @Test
    fun removeAt_returnsRemovedElement() {
        val list = mutableIntListOf(1, 2, 3, 4, 5)
        assertEquals(2, list.removeAt(1))
        assertEquals(5, list.removeAt(3))
        list.assertContents(1, 3, 4)
        assertFailsWith<IndexOutOfBoundsException> { list.removeAt(3) }
    }

    @Test
    fun indexOf_andLastIndexOf_findFirstAndLastOccurrence() {
        val list = mutableIntListOf(1, 2, 3, 2, 1)
        assertEquals(1, list.indexOf(2))
        assertEquals(3, list.lastIndexOf(2))
        assertEquals(-1, list.indexOf(9))
        assertEquals(-1, list.lastIndexOf(9))
        assertTrue(list.contains(3))
        assertFalse(list.contains(9))
    }

    // ---------- bulk operations ----------

    @Test
    fun removeRange_removesSlice() {
        val list = mutableIntListOf(1, 2, 3, 4, 5)
        list.removeRange(1, 4)
        list.assertContents(1, 5)
    }

    @Test
    fun removeRange_invertedRange_throws() {
        assertFailsWith<IllegalArgumentException> { mutableIntListOf(1, 2, 3).removeRange(2, 1) }
    }

    @Test
    fun addAll_atIndex_withPrimitiveCollection() {
        val list = mutableIntListOf(1, 4)
        list.addAll(1, intListOf(2, 3))
        list.assertContents(1, 2, 3, 4)
    }

    @Test
    fun addAll_atIndex_withStandardCollection() {
        val list = mutableIntListOf(1, 4)
        list.addAll(1, listOf(2, 3))
        list.assertContents(1, 2, 3, 4)
    }

    @Test
    fun addAll_fromAnotherArrayDeque_appendsEveryElement() {
        val source = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5))
        val target = IntArrayDeque(intArrayOf(0))
        assertTrue(target.addAll(source))
        target.assertContents(0, 1, 2, 3, 4, 5)
        assertFalse(target.addAll(IntArrayDeque()))
    }

    @Test
    fun removeAll_andRetainAll_withPredicate() {
        val removed = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5, 6))
        assertTrue(removed.removeAll { it % 2 == 0 })
        removed.assertContents(1, 3, 5)
        assertFalse(removed.removeAll { it > 100 })

        val retained = IntArrayDeque(intArrayOf(1, 2, 3, 4, 5, 6))
        assertTrue(retained.retainAll { it % 2 == 0 })
        retained.assertContents(2, 4, 6)
        assertFalse(retained.retainAll { true })
    }

    @Test
    fun clear_emptiesAndAllowsReuse() {
        val list = mutableIntListOf(1, 2, 3)
        list.clear()
        list.assertContents()
        assertTrue(list.isEmpty())
        list.add(9)
        list.assertContents(9)
    }

    // ---------- reordering ----------

    @Test
    fun sort_ascendingOrder() {
        val list = mutableIntListOf(-3, 1, 4, -1, 5)
        list.sort()
        list.assertContents(-3, -1, 1, 4, 5)
    }

    @Test
    fun sortDescending_descendingOrder() {
        val list = mutableIntListOf(-3, 1, 4, -1, 5)
        list.sortDescending()
        list.assertContents(5, 4, 1, -1, -3)
    }

    @Test
    fun sortAndReverse_emptyAndSingleton_areNoOps() {
        mutableIntListOf().apply { sort(); reverse(); sortDescending() }.assertContents()
        mutableIntListOf(42).apply { sort(); reverse(); sortDescending() }.assertContents(42)
    }

    @Test
    fun fill_setsAllElements() {
        val list = mutableIntListOf(1, 2, 3, 4)
        list.fill(7)
        list.assertContents(7, 7, 7, 7)
    }

    @Test
    fun reverse_reversesOrder() {
        mutableIntListOf(1, 2, 3, 4).apply { reverse() }.assertContents(4, 3, 2, 1)
        mutableIntListOf(1, 2, 3).apply { reverse() }.assertContents(3, 2, 1)
    }

    // ---------- capacity ----------

    @Test
    fun growth_preservesOrderAcrossManyAppends() {
        val list = IntArrayDeque(1)
        for (i in 0..<1000) list.add(i)
        assertEquals(1000, list.size)
        for (i in 0..<1000) assertEquals(i, list[i])
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val list = IntArrayDeque(intArrayOf(1, 2, 3))
        list.ensureCapacity(1000)
        list.assertContents(1, 2, 3)
        list.add(4)
        list.assertContents(1, 2, 3, 4)
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val list = IntArrayDeque(1000)
        for (i in 1..3) list.add(i)
        list.trimToSize()
        list.assertContents(1, 2, 3)
        list.add(4)
        list.assertContents(1, 2, 3, 4)
    }

    // ---------- iteration ----------

    @Test
    fun foreach_matchesIteratorInOrder() {
        val list = mutableIntListOf()
        for (i in 1..50) list.add(i)

        val fromForeach = mutableListOf<Int>()
        list.foreach { v -> fromForeach.add(v) }

        assertEquals((1..50).toList(), fromForeach)
    }

    @Test
    fun iteratorRemove_removesTheLastReturnedElement() {
        val list = mutableIntListOf(1, 2, 3, 4, 5, 6)
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            if (iterator.nextInt() % 2 == 0) iterator.remove()
        }
        list.assertContents(1, 3, 5)
    }

    @Test
    fun iteratorRemove_beforeNext_throws() {
        assertFailsWith<IllegalStateException> { mutableIntListOf(1).iterator().remove() }
    }

    @Test
    fun iteratorNext_pastEnd_throws() {
        val iterator = mutableIntListOf(1).iterator()
        iterator.nextInt()
        assertFailsWith<NoSuchElementException> { iterator.nextInt() }
    }

      // ---------- equality ----------

    @Test
    fun equals_matchesAnyIntListImplementation() {
        // IntList contract (Abstract*List.equals()) requires equality against ANY IntList
        // implementation, not just the same concrete class.
        val deque: IntList = mutableIntListOf(1, 2, 3)
        for (other in listOf<IntList>(intArrayOf(1, 2, 3).asIntList(), SequentialIntList(1, 2, 3))) {
            assertEquals(other, deque)
            assertEquals(deque, other)
            assertEquals(other.hashCode(), deque.hashCode())
        }

        assertEquals(emptyIntList(), IntArrayDeque() as IntList)
        assertEquals(intListOf(42), mutableIntListOf(42) as IntList)
        assertNotEquals(intListOf(1, 2), deque)
        assertNotEquals(intListOf(1, 2, 4), deque)
    }

    @Test
    fun toString_matchesTheStandardListRendering() {
        assertEquals("[1, 2, 3]", mutableIntListOf(1, 2, 3).toString())
        assertEquals("[]", mutableIntListOf().toString())
    }
}
