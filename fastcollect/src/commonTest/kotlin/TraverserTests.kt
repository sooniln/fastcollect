package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Coverage for the traverser hierarchy, and in particular for MutableIntListTraverser - the bidirectional cursor
 * (position/forward/backward/set/insert) that is public API on every generated list but had no tests at all.
 *
 * The cursor sits *between* elements at a position in 0..size. forward() moves over the element to its right and
 * backward() over the element to its left; either way `value` is the element just moved over, so a forward()
 * immediately followed by a backward() returns the same element twice.
 *
 * Each list-traverser test runs against every list implementation that supplies a cursor, since a caller can
 * hold a traverser over any of them: a deque, a sublist view, a read-only array wrapper, and a list that is not
 * RandomAccess.
 */
class TraverserTests {

    private fun mutableFixtures(vararg elements: Int): List<Pair<String, MutableIntList>> = listOf(
        "IntArrayDeque" to IntArrayDeque(elements),
        "SequentialIntList" to SequentialIntList(*elements),
        "subList" to IntArrayDeque(intArrayOf(-1, *elements.toTypedArray().toIntArray(), -2)).subList(1, elements.size + 1),
    )

    private fun readOnlyFixtures(vararg elements: Int): List<Pair<String, IntList>> = listOf(
        "array wrapper" to elements.asIntList(),
        "read-only subList" to (intArrayOf(-1, *elements.toTypedArray().toIntArray(), -2)).asIntList().subList(1, elements.size + 1),
    ) + mutableFixtures(*elements)

    // ---------- positioning ----------

    @Test
    fun traverser_startsAtTheRequestedPosition() {
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            assertEquals(0, list.traverser(0).position, name)
            assertEquals(1, list.traverser(1).position, name)
            assertEquals(3, list.traverser(3).position, name)
        }
    }

    @Test
    fun traverser_positionOutOfRange_throws() {
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            assertFailsWith<IndexOutOfBoundsException>(name) { list.traverser(4) }
            assertFailsWith<IndexOutOfBoundsException>(name) { list.traverser(-1) }
        }
    }

    @Test
    fun forward_walksToTheEndThenReportsFalse() {
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            val traverser = list.traverser(0)
            val seen = mutableListOf<Int>()
            val positions = mutableListOf<Int>()
            while (traverser.forward()) {
                seen.add(traverser.value)
                positions.add(traverser.position)
            }
            assertEquals(listOf(10, 20, 30), seen, name)
            assertEquals(listOf(1, 2, 3), positions, name)
            assertFalse(traverser.forward(), name)
            assertEquals(3, traverser.position, name)
        }
    }

    @Test
    fun backward_fromTheEndWalksToTheStartThenReportsFalse() {
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            val traverser = list.traverser(list.size)
            val seen = mutableListOf<Int>()
            val positions = mutableListOf<Int>()
            while (traverser.backward()) {
                seen.add(traverser.value)
                positions.add(traverser.position)
            }
            assertEquals(listOf(30, 20, 10), seen, name)
            assertEquals(listOf(2, 1, 0), positions, name)
            assertFalse(traverser.backward(), name)
            assertEquals(0, traverser.position, name)
        }
    }

    @Test
    fun forwardThenBackward_returnsTheSameElement() {
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            val traverser = list.traverser(0)

            assertTrue(traverser.forward(), name)
            assertEquals(10, traverser.value, name)
            assertEquals(1, traverser.position, name)

            assertTrue(traverser.backward(), name)
            assertEquals(10, traverser.value, name)
            assertEquals(0, traverser.position, name)

            assertFalse(traverser.backward(), name)
        }
    }

    @Test
    fun alternatingDirections_trackTheCursorBetweenElements() {
        for ((name, list) in readOnlyFixtures(10, 20, 30, 40)) {
            val traverser = list.traverser(2)

            assertTrue(traverser.backward(), name)
            assertEquals(20, traverser.value, name)
            assertEquals(1, traverser.position, name)

            assertTrue(traverser.forward(), name)
            assertEquals(20, traverser.value, name)
            assertEquals(2, traverser.position, name)

            assertTrue(traverser.forward(), name)
            assertEquals(30, traverser.value, name)
            assertEquals(3, traverser.position, name)
        }
    }

    @Test
    fun traverser_atTheEnds_reportsFalseWithoutMoving() {
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            val atStart = list.traverser(0)
            assertFalse(atStart.backward(), name)
            assertEquals(0, atStart.position, name)

            val atEnd = list.traverser(3)
            assertFalse(atEnd.forward(), name)
            assertEquals(3, atEnd.position, name)
        }
    }

    @Test
    fun value_atPositionZeroBeforeAnyMove_throws() {
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            assertFailsWith<IllegalStateException>(name) { list.traverser(0).value }
        }
    }

    @Test
    fun value_atANonZeroStartPosition_isTheElementToTheLeftOfTheCursor() {
        // every implementation initialises the cursor as though it had just moved forward onto the requested
        // position, which is what makes traverser(size) + backward() (i.e. foreachReverse) work
        for ((name, list) in readOnlyFixtures(10, 20, 30)) {
            assertEquals(10, list.traverser(1).value, name)
            assertEquals(20, list.traverser(2).value, name)
            assertEquals(30, list.traverser(3).value, name)
        }
    }

    @Test
    fun traverser_onEmptyAndSingletonLists() {
        for ((name, list) in readOnlyFixtures()) {
            val traverser = list.traverser(0)
            assertEquals(0, list.size, name)
            assertFalse(traverser.forward(), name)
            assertFalse(traverser.backward(), name)
            assertFailsWith<IllegalStateException>(name) { traverser.value }
        }

        for ((name, list) in readOnlyFixtures(42)) {
            val traverser = list.traverser(0)
            assertTrue(traverser.forward(), name)
            assertEquals(42, traverser.value, name)
            assertFalse(traverser.forward(), name)
            assertTrue(traverser.backward(), name)
            assertEquals(42, traverser.value, name)
            assertFalse(traverser.backward(), name)
        }
    }

    @Test
    fun emptyAndSingletonListSingletons_alsoSupplyAListTraverser() {
        val empty = emptyIntList().traverser(0)
        assertEquals(0, empty.position)
        assertFalse(empty.forward())
        assertFalse(empty.backward())
        assertFailsWith<IllegalStateException> { empty.value }
        assertFailsWith<IndexOutOfBoundsException> { emptyIntList().traverser(1) }

        val single = intListOf(42).traverser(0)
        assertTrue(single.forward())
        assertEquals(42, single.value)
        assertEquals(1, single.position)
        assertTrue(single.backward())
        assertEquals(42, single.value)
    }

    // ---------- set ----------

    @Test
    fun set_writesAtTheElementJustMovedOver() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            val traverser = list.traverser(0)
            assertTrue(traverser.forward(), name)
            traverser.set(99)
            list.assertContents(99, 20, 30)
            assertEquals(99, traverser.value, name)
        }
    }

    @Test
    fun set_afterBackward_writesTheElementToTheRightOfTheCursor() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            val traverser = list.traverser(3)
            assertTrue(traverser.backward(), name)
            assertEquals(30, traverser.value, name)
            traverser.set(99)
            list.assertContents(10, 20, 99)
            assertEquals(2, traverser.position, name)
        }
    }

    @Test
    fun set_beforeAnyMove_throws() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            assertFailsWith<IllegalStateException>(name) { list.traverser(0).set(99) }
        }
    }

    @Test
    fun set_overTheWholeList_rewritesEveryElement() {
        for ((name, list) in mutableFixtures(1, 2, 3, 4, 5)) {
            val traverser = list.traverser(0)
            while (traverser.forward()) traverser.set(traverser.value * 10)
            list.assertContents(10, 20, 30, 40, 50)
            assertEquals(5, traverser.position, name)
        }
    }

    // ---------- insert ----------

    @Test
    fun insert_addsAtTheCursorAndAdvancesPastIt() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            val traverser = list.traverser(1)
            traverser.insert(15)

            list.assertContents(10, 15, 20, 30)
            assertEquals(2, traverser.position, name)

            assertTrue(traverser.forward(), name)
            assertEquals(20, traverser.value, name)
        }
    }

    @Test
    fun insert_atBothEnds() {
        for ((name, list) in mutableFixtures(10, 20)) {
            val front = list.traverser(0)
            front.insert(5)
            list.assertContents(5, 10, 20)
            assertEquals(1, front.position, name)

            val back = list.traverser(list.size)
            back.insert(30)
            list.assertContents(5, 10, 20, 30)
            assertEquals(4, back.position, name)
        }
    }

    @Test
    fun insert_invalidatesTheCurrentElement() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            val traverser = list.traverser(0)
            assertTrue(traverser.forward(), name)
            assertEquals(10, traverser.value, name)

            traverser.insert(15)
            assertFailsWith<IllegalStateException>(name) { traverser.value }
            assertFailsWith<IllegalStateException>(name) { traverser.set(0) }

            assertTrue(traverser.forward(), name)
            assertEquals(20, traverser.value, name)
            list.assertContents(10, 15, 20, 30)
        }
    }

    @Test
    fun insert_repeatedly_buildsTheListInOrder() {
        for ((name, list) in mutableFixtures()) {
            val traverser = list.traverser(0)
            for (v in 1..5) traverser.insert(v)
            list.assertContents(1, 2, 3, 4, 5)
            assertEquals(5, traverser.position, name)
            assertFalse(traverser.forward(), name)
        }
    }

    // ---------- remove ----------

    @Test
    fun remove_afterForward_dropsTheElementAndPullsTheCursorBack() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            val traverser = list.traverser(0)
            assertTrue(traverser.forward(), name)
            traverser.remove()

            list.assertContents(20, 30)
            assertEquals(0, traverser.position, name)

            assertTrue(traverser.forward(), name)
            assertEquals(20, traverser.value, name)
        }
    }

    @Test
    fun remove_afterBackward_dropsTheElementAndLeavesTheCursor() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            val traverser = list.traverser(3)
            assertTrue(traverser.backward(), name)
            assertEquals(30, traverser.value, name)
            traverser.remove()

            list.assertContents(10, 20)
            assertEquals(2, traverser.position, name)
            assertFalse(traverser.forward(), name)
        }
    }

    @Test
    fun remove_invalidatesTheCurrentElement() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            val traverser = list.traverser(0)
            assertTrue(traverser.forward(), name)
            traverser.remove()

            assertFailsWith<IllegalStateException>(name) { traverser.value }
            assertFailsWith<IllegalStateException>(name) { traverser.set(0) }
            assertFailsWith<IllegalStateException>(name) { traverser.remove() }
        }
    }

    @Test
    fun remove_beforeAnyMove_throws() {
        for ((name, list) in mutableFixtures(10, 20, 30)) {
            assertFailsWith<IllegalStateException>(name) { list.traverser(0).remove() }
        }
    }

    @Test
    fun forwardRemove_overTheWholeList_visitsEveryElementExactlyOnce() {
        for ((name, list) in mutableFixtures(1, 2, 3, 4, 5, 6)) {
            val seen = mutableListOf<Int>()
            val traverser = list.traverser(0)
            while (traverser.forward()) {
                val value = traverser.value
                seen.add(value)
                if (value % 2 == 0) traverser.remove()
            }
            assertEquals(listOf(1, 2, 3, 4, 5, 6), seen, name)
            list.assertContents(1, 3, 5)
        }
    }

    @Test
    fun backwardRemove_overTheWholeList_visitsEveryElementExactlyOnce() {
        for ((name, list) in mutableFixtures(1, 2, 3, 4, 5, 6)) {
            val seen = mutableListOf<Int>()
            val traverser = list.traverser(list.size)
            while (traverser.backward()) {
                val value = traverser.value
                seen.add(value)
                if (value % 2 == 0) traverser.remove()
            }
            assertEquals(listOf(6, 5, 4, 3, 2, 1), seen, name)
            list.assertContents(1, 3, 5)
        }
    }

    @Test
    fun interleavedInsertAndRemove_keepTheCursorConsistent() {
        for ((name, list) in mutableFixtures(1, 2, 3)) {
            val traverser = list.traverser(0)

            assertTrue(traverser.forward(), name)      // over 1, position 1
            traverser.insert(99)                       // [1, 99, 2, 3], position 2
            assertTrue(traverser.forward(), name)      // over 2, position 3
            assertEquals(2, traverser.value, name)
            traverser.remove()                         // [1, 99, 3], position 2
            assertEquals(2, traverser.position, name)
            assertTrue(traverser.forward(), name)      // over 3
            assertEquals(3, traverser.value, name)

            list.assertContents(1, 99, 3)
        }
    }

    // ---------- value traversers ----------

    @Test
    fun valueTraverser_onASet_visitsEveryElementOnce() {
        val set = IntHashSet(intListOf(1, 2, 3))
        val traverser = set.traverser()

        assertFailsWith<IllegalStateException> { traverser.value }
        val seen = mutableListOf<Int>()
        while (traverser.forward()) seen.add(traverser.value)
        assertEquals(listOf(1, 2, 3), seen.sorted())
        assertFalse(traverser.forward())
    }

    @Test
    fun emptyValueTraverser_reportsNothingAndRefusesEveryAccess() {
        val traverser = emptyIntTraverser()
        assertFalse(traverser.forward())
        assertFailsWith<IllegalStateException> { traverser.value }
        assertFailsWith<IllegalStateException> { traverser.remove() }
    }

    // ---------- entry traversers ----------

    @Test
    fun entryTraverser_exposesKeyAndMutableValue() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L)
        val traverser = map.traverser()

        val seen = mutableMapOf<Int, Long>()
        while (traverser.forward()) {
            seen[traverser.key] = traverser.value
            traverser.value = traverser.value + 1
        }

        assertEquals(mapOf(1 to 10L, 2 to 20L), seen)
        assertEquals(11L, map[1])
        assertEquals(21L, map[2])
    }

    @Test
    fun asKeyTraverser_andAsValueTraverser_projectTheEntryStream() {
        val map = mutableInt2LongMapOf(1 to 10L, 2 to 20L, 3 to 30L)

        val keys = mutableListOf<Int>()
        val keyTraverser = map.traverser().asKeyTraverser()
        while (keyTraverser.forward()) keys.add(keyTraverser.value)
        assertEquals(listOf(1, 2, 3), keys.sorted())

        val values = mutableListOf<Long>()
        val valueTraverser = map.traverser().asValueTraverser()
        while (valueTraverser.forward()) values.add(valueTraverser.value)
        assertEquals(listOf(10L, 20L, 30L), values.sorted())
    }

    @Test
    fun asKeyTraverser_andAsValueTraverser_beforeFirstForward_throw() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertFailsWith<IllegalStateException> { map.traverser().asKeyTraverser().value }
        assertFailsWith<IllegalStateException> { map.traverser().asValueTraverser().value }
    }

    @Test
    fun emptyEntryTraverser_reportsNothingAndRefusesEveryAccess() {
        val traverser = emptyInt2LongTraverser()
        assertFalse(traverser.forward())
        assertFailsWith<IllegalStateException> { traverser.key }
        assertFailsWith<IllegalStateException> { traverser.value }
        assertFailsWith<IllegalStateException> { traverser.remove() }
    }

    // ---------- the Iterator methods Traverser deliberately does not implement ----------

    @Test
    fun traverserIsNotUsableAsAnIterator() {
        // Traverser extends Iterator purely so the JIT can inline through it (JDK-8223504); the inherited
        // methods are not meant to be called and must fail loudly rather than half-work.
        val traverser: Iterator<*> = mutableIntListOf(1, 2, 3).traverser()
        assertFails { traverser.hasNext() }
        assertFails { traverser.next() }
    }
}
