package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Coverage for the default implementations and non-deque list flavours in List.kte: the EmptyIntList and
 * SingletonIntList singletons, the IntArray wrapper, sublist views, and a list that is deliberately NOT
 * RandomAccess so the traverser-based fallbacks are taken instead of the indexed fast paths.
 */
class IntListDefaultsTests {

    // ---------- factories ----------

    @Test
    fun factories_produceExpectedContents() {
        intListOf().assertContents()
        intListOf(42).assertContents(42)
        intListOf(1, 2, 3).assertContents(1, 2, 3)
        emptyIntList().assertContents()

        mutableIntListOf().assertContents()
        mutableIntListOf(42).assertContents(42)
        mutableIntListOf(1, 2, 3).assertContents(1, 2, 3)

        intArrayOf(1, 2, 3).asIntList().assertContents(1, 2, 3)
        buildIntList { add(10); add(20) }.assertContents(10, 20)
        buildIntList(8) { add(10) }.assertContents(10)
        IntList(4) { it * 3 }.assertContents(0, 3, 6, 9)
        MutableIntList(3) { it + 10 }.assertContents(10, 11, 12)
        IntList(0) { it }.assertContents()
    }

    // ---------- EmptyIntList ----------

    @Test
    fun emptyList_rejectsIndexedAccessAndReportsAbsence() {
        val empty = emptyIntList()
        assertEquals(0, empty.size)
        assertTrue(empty.isEmpty())
        assertFalse(empty.contains(0))
        assertEquals(-1, empty.indexOf(0))
        assertEquals(-1, empty.lastIndexOf(0))
        assertEquals(-1, empty.lastIndex)
        assertEquals(IntRange.EMPTY, empty.indices)
        assertFailsWith<IndexOutOfBoundsException> { empty[0] }
        assertFailsWith<NoSuchElementException> { empty.first() }
        assertFailsWith<NoSuchElementException> { empty.last() }
        assertFalse(empty.iterator().hasNext())
        assertFalse(empty.traverser().forward())
        assertEquals(emptyIntList(), empty.subList(0, 0))
    }

    @Test
    fun emptyList_containsAllOnlyAcceptsEmptyArguments() {
        val empty = emptyIntList()
        assertTrue(empty.containsAll(intListOf()))
        assertTrue(empty.containsAll(emptyList<Int>()))
        assertFalse(empty.containsAll(intListOf(1)))
        assertFalse(empty.containsAll(listOf(1)))
    }

    // ---------- SingletonIntList ----------

    @Test
    fun singletonList_behavesAsAOneElementList() {
        val single = intListOf(42)
        assertEquals(1, single.size)
        assertFalse(single.isEmpty())
        assertEquals(42, single[0])
        assertEquals(42, single.first())
        assertEquals(42, single.last())
        assertTrue(single.contains(42))
        assertFalse(single.contains(0))
        assertEquals(0, single.indexOf(42))
        assertEquals(0, single.lastIndexOf(42))
        assertEquals(-1, single.indexOf(0))
        assertFailsWith<IndexOutOfBoundsException> { single[1] }
    }

    @Test
    fun singletonList_subListNarrowsToEmpty() {
        val single = intListOf(42)
        single.subList(0, 1).assertContents(42)
        single.subList(0, 0).assertContents()
        single.subList(1, 1).assertContents()
        assertFailsWith<IndexOutOfBoundsException> { single.subList(0, 2) }
    }

    // ---------- IntArray wrapper ----------

    @Test
    fun arrayWrapper_isALiveViewOfTheArray() {
        val array = intArrayOf(1, 2, 3)
        val view = array.asIntList()
        view.assertContents(1, 2, 3)

        array[1] = 99
        view.assertContents(1, 99, 3)
        assertEquals(1, view.indexOf(99))
    }

    @Test
    fun arrayWrapper_iteratorAndTraverserWalkTheWholeArray() {
        val view = intArrayOf(1, 2, 3).asIntList()

        assertEquals(listOf(1, 2, 3), view.toBoxedList())

        val collected = mutableListOf<Int>()
        view.foreach { collected.add(it) }
        assertEquals(listOf(1, 2, 3), collected)

        val traverser = view.traverser()
        assertFailsWith<IllegalStateException> { traverser.value }
        assertTrue(traverser.forward())
        assertEquals(1, traverser.value)

        val iterator = view.iterator()
        repeat(3) { iterator.nextInt() }
        assertFailsWith<NoSuchElementException> { iterator.nextInt() }
    }

    @Test
    fun arrayWrapper_emptyArray() {
        val view = intArrayOf().asIntList()
        view.assertContents()
        assertFalse(view.traverser().forward())
        assertFalse(view.iterator().hasNext())
    }

    // ---------- sublist views ----------

    @Test
    fun subList_readsThroughToTheBackingList() {
        val backing = mutableIntListOf(1, 2, 3, 4, 5)
        val sub = backing.subList(1, 4)
        sub.assertContents(2, 3, 4)
        assertEquals(3, sub.size)
        assertEquals(2, sub.first())
        assertEquals(4, sub.last())
        assertEquals(1, sub.indexOf(3))
        assertFailsWith<IndexOutOfBoundsException> { sub[3] }
    }

    @Test
    fun subList_writesThroughToTheBackingList() {
        val backing = mutableIntListOf(1, 2, 3, 4, 5)
        val sub = backing.subList(1, 4)

        sub[0] = 99
        backing.assertContents(1, 99, 3, 4, 5)

        sub.add(1, 88)
        sub.assertContents(99, 88, 3, 4)
        backing.assertContents(1, 99, 88, 3, 4, 5)

        assertEquals(88, sub.removeAt(1))
        backing.assertContents(1, 99, 3, 4, 5)
    }

    @Test
    fun subList_removeRange_updatesSubList() {
        val list = mutableIntListOf(1, 2, 3, 4, 5)
        val sub = list.subList(1, 4)
        sub.removeRange(0, 2)
        sub.assertContents(4)
        list.assertContents(1, 4, 5)
    }

    @Test
    fun subList_addAll_updatesBothSizes() {
        val backing = mutableIntListOf(1, 2, 5)
        val sub = backing.subList(1, 2)
        sub.addAll(1, intListOf(3, 4))
        sub.assertContents(2, 3, 4)
        backing.assertContents(1, 2, 3, 4, 5)

        sub.addAll(0, listOf(0))
        backing.assertContents(1, 0, 2, 3, 4, 5)
    }

    @Test
    fun subList_ofSubList_composesOffsets() {
        val backing = mutableIntListOf(0, 1, 2, 3, 4, 5, 6)
        val sub = backing.subList(1, 6).subList(1, 4)
        sub.assertContents(2, 3, 4)
        sub[0] = 99
        backing.assertContents(0, 1, 99, 3, 4, 5, 6)
    }

    @Test
    fun subList_invalidRange_throws() {
        val backing = mutableIntListOf(1, 2, 3)
        assertFailsWith<IndexOutOfBoundsException> { backing.subList(0, 4) }
        assertFailsWith<IndexOutOfBoundsException> { backing.subList(-1, 2) }
        assertFailsWith<IllegalArgumentException> { backing.subList(2, 1) }
    }

    @Test
    fun subList_copyInto_translatesTheRange() {
        val backing = mutableIntListOf(1, 2, 3, 4, 5)
        val sub = backing.subList(1, 4)
        assertEquals(listOf(2, 3, 4), sub.copyInto(IntArray(3)).toList())
        // the sublist's own indices, not the backing list's
        assertEquals(listOf(0, 3, 4, 0), sub.copyInto(IntArray(4), 1, 1, 3).toList())
    }

    @Test
    fun traverseRemove_subList_doesNotThrowSpuriousConcurrentModificationException() {
        // regression test: AbstractMutableList.TraverserImpl cached the list's lastIndex once at construction and
        // never updated it after remove(), which threw a spurious CME on the very next forward() call
        val backing = mutableIntListOf(1, 2, 3, 4, 5, 6)
        val sub = backing.subList(1, 5)

        val kept = mutableListOf<Int>()
        val traverser = sub.traverser()
        while (traverser.forward()) {
            val value = traverser.value
            if (value % 2 == 0) traverser.remove() else kept.add(value)
        }

        assertEquals(listOf(3, 5), kept)
        sub.assertContents(3, 5)
        backing.assertContents(1, 3, 5, 6)
    }

    // ---------- non-RandomAccess fallbacks ----------

    @Test
    fun sort_worksOnAListThatIsNotRandomAccess() {
        // a caller can supply their own MutableIntList; nothing requires it to be RandomAccess
        val list: MutableIntList = SequentialIntList(-3, 1, 4, -1, 5)
        list.sort()
        list.assertContents(-3, -1, 1, 4, 5)

        list.sortDescending()
        list.assertContents(5, 4, 1, -1, -3)
    }

    @Test
    fun fillAndReverse_workOnAListThatIsNotRandomAccess() {
        val reversed = SequentialIntList(1, 2, 3, 4, 5)
        reversed.reverse()
        reversed.assertContents(5, 4, 3, 2, 1)

        val filled = SequentialIntList(1, 2, 3)
        filled.fill(7)
        filled.assertContents(7, 7, 7)
    }

    @Test
    fun removeRange_worksOnAListThatIsNotRandomAccess() {
        val list = SequentialIntList(1, 2, 3, 4, 5)
        list.removeRange(1, 4)
        list.assertContents(1, 5)
    }

    // ---------- index checks ----------

    @Test
    fun indexCheck_acceptsValidIndicesAndRejectsTheRest() {
        val list = intListOf(1, 2, 3)
        assertEquals(0, list.indexCheck(0))
        assertEquals(2, list.indexCheck(2))
        assertFailsWith<IndexOutOfBoundsException> { list.indexCheck(3) }
        assertFailsWith<IndexOutOfBoundsException> { list.indexCheck(-1) }
    }

    @Test
    fun indexCheckInclusive_alsoAcceptsSize() {
        val list = intListOf(1, 2, 3)
        assertEquals(3, list.indexCheckInclusive(3))
        assertFailsWith<IndexOutOfBoundsException> { list.indexCheckInclusive(4) }
        assertFailsWith<IndexOutOfBoundsException> { list.indexCheckInclusive(-1) }
    }

    @Test
    fun rangeCheck_rejectsInvertedAndOutOfBoundsRanges() {
        val list = intListOf(1, 2, 3)
        list.rangeCheck(0, 3)
        list.rangeCheck(1, 1)
        assertFailsWith<IllegalArgumentException> { list.rangeCheck(2, 1) }
        assertFailsWith<IndexOutOfBoundsException> { list.rangeCheck(0, 4) }
        assertFailsWith<IndexOutOfBoundsException> { list.rangeCheck(-1, 2) }
    }

    // ---------- positional extensions ----------

    @Test
    fun firstAndLast_returnTheEnds() {
        assertEquals(1, intListOf(1, 2, 3).first())
        assertEquals(3, intListOf(1, 2, 3).last())
        assertEquals(42, intListOf(42).first())
        assertEquals(42, intListOf(42).last())
    }

    @Test
    fun foreachIndexed_visitsEveryIndexInOrder() {
        val seen = mutableListOf<Pair<Int, Int>>()
        intListOf(10, 20, 30).foreachIndexed { index, value -> seen.add(index to value) }
        assertEquals(listOf(0 to 10, 1 to 20, 2 to 30), seen)

        seen.clear()
        intListOf().foreachIndexed { index, value -> seen.add(index to value) }
        assertEquals(emptyList(), seen)
    }

    @Test
    fun foreachReverse_visitsEveryElementBackwards() {
        val seen = mutableListOf<Int>()
        intListOf(10, 20, 30).foreachReverse { seen.add(it) }
        assertEquals(listOf(30, 20, 10), seen)

        seen.clear()
        intListOf().foreachReverse { seen.add(it) }
        assertEquals(emptyList(), seen)
    }

    @Test
    fun foreachReverseIndexed_pairsIndicesWithElementsBackwards() {
        val seen = mutableListOf<Pair<Int, Int>>()
        intListOf(10, 20, 30).foreachReverseIndexed { index, value -> seen.add(index to value) }
        assertEquals(listOf(2 to 30, 1 to 20, 0 to 10), seen)
    }

    @Test
    fun foldRight_correctResult() {
        assertEquals("123", intListOf(1, 2, 3).foldRight("") { element, acc -> "$element$acc" })
        assertEquals("", intListOf().foldRight("") { element, acc -> "$element$acc" })
    }

    @Test
    fun reduceRight_correctResult() {
        // accumulated starts at 4, op(4,2)=6, op(6,1)=7
        assertEquals(7, intListOf(1, 2, 4).reduceRight { acc, element -> acc + element })
        assertEquals(42, intListOf(42).reduceRight { acc, element -> acc + element })
        assertFailsWith<NoSuchElementException> { intListOf().reduceRight { acc, element -> acc + element } }
    }

    // ---------- boxed views ----------

    @Test
    fun asList_readOnlyView_reflectsTheBackingList() {
        val backing = mutableIntListOf(1, 2, 3)
        val view: List<Int> = (backing as IntList).asList()
        assertEquals(listOf(1, 2, 3), view)

        backing.add(4)
        assertEquals(listOf(1, 2, 3, 4), view)
    }

    @Test
    fun asList_mutableView_writesThrough() {
        val backing = mutableIntListOf(1, 2, 3)
        val view = backing.asList()

        view.add(4)
        backing.assertContents(1, 2, 3, 4)

        view[0] = 99
        backing.assertContents(99, 2, 3, 4)

        assertEquals(99, view.removeAt(0))
        backing.assertContents(2, 3, 4)
    }
}
