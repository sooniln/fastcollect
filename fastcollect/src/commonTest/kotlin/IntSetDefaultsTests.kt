package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Coverage for Set.kte: the factories, the EmptyIntSet/SingletonIntSet implementations, the boxed view, and the
 * union/intersect/subtract operators.
 */
class IntSetDefaultsTests {

    // ---------- factories ----------

    @Test
    fun factories_produceExpectedContents() {
        assertTrue(intSetOf().isEmpty())
        assertEquals(listOf(42), intSetOf(42).toBoxedList())
        assertEquals(listOf(1, 2, 3), intSetOf(1, 2, 3).toBoxedList().sorted())
        assertEquals(listOf(1, 2), intSetOf(1, 2, 1).toBoxedList().sorted())

        assertTrue(mutableIntSetOf().isEmpty())
        assertEquals(listOf(42), mutableIntSetOf(42).toBoxedList())
        assertEquals(listOf(1, 2, 3), mutableIntSetOf(1, 2, 3).toBoxedList().sorted())

        assertEquals(listOf(1, 2), buildIntSet { add(1); add(2); add(1) }.toBoxedList().sorted())
        assertEquals(listOf(1), buildIntSet(8) { add(1) }.toBoxedList())
    }

    // ---------- EmptyIntSet ----------

    @Test
    fun emptySet_reportsEverythingAbsent() {
        val empty = emptyIntSet()
        assertEquals(0, empty.size)
        assertTrue(empty.isEmpty())
        assertFalse(empty.contains(0))
        assertFalse(empty.contains(1))
        assertFalse(empty.iterator().hasNext())
        assertFalse(empty.traverser().forward())
        assertTrue(empty.containsAll(intSetOf()))
        assertFalse(empty.containsAll(intSetOf(1)))
        assertEquals("[]", empty.toString())
    }

    // ---------- SingletonIntSet ----------

    @Test
    fun singletonSet_holdsExactlyOneElement() {
        val single = intSetOf(42)
        assertEquals(1, single.size)
        assertFalse(single.isEmpty())
        assertTrue(single.contains(42))
        assertFalse(single.contains(0))
        assertEquals(listOf(42), single.toBoxedList())
        assertEquals("[42]", single.toString())

        val traverser = single.traverser()
        assertFailsWith<IllegalStateException> { traverser.value }
        assertTrue(traverser.forward())
        assertEquals(42, traverser.value)
        assertFalse(traverser.forward())
    }

    @Test
    fun singletonSet_iteratorYieldsOneElementThenThrows() {
        val iterator = intSetOf(42).iterator()
        assertTrue(iterator.hasNext())
        assertEquals(42, iterator.nextInt())
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.nextInt() }
    }

    // ---------- set operators ----------

    @Test
    fun union_combinesBothSets() {
        assertEquals(listOf(1, 2, 3, 4, 5), (intSetOf(1, 2, 3) union intSetOf(3, 4, 5)).toBoxedList().sorted())
        assertEquals(listOf(1, 2, 3), (intSetOf(1, 2, 3) union intSetOf()).toBoxedList().sorted())
        assertEquals(listOf(1, 2, 3), (intSetOf() union intSetOf(1, 2, 3)).toBoxedList().sorted())
        assertTrue((intSetOf() union intSetOf()).isEmpty())
    }

    @Test
    fun union_doesNotMutateEitherOperand() {
        val a = intSetOf(1, 2)
        val b = intSetOf(3)
        a union b
        assertEquals(listOf(1, 2), a.toBoxedList().sorted())
        assertEquals(listOf(3), b.toBoxedList())
    }

    @Test
    fun intersect_returnsCommonElements() {
        assertEquals(listOf(3, 4), (intSetOf(1, 2, 3, 4) intersect intSetOf(3, 4, 5, 6)).toBoxedList().sorted())
        // intersect iterates the smaller operand, so both argument orders must agree
        assertEquals(listOf(3, 4), (intSetOf(3, 4, 5, 6) intersect intSetOf(1, 2, 3, 4)).toBoxedList().sorted())
        assertTrue((intSetOf(1, 2) intersect intSetOf(3, 4)).isEmpty())
        assertTrue((intSetOf(1, 2) intersect intSetOf()).isEmpty())
        assertTrue((intSetOf() intersect intSetOf(1, 2)).isEmpty())
    }

    @Test
    fun subtract_removesRhsFromLhs() {
        assertEquals(listOf(1, 2), (intSetOf(1, 2, 3, 4) subtract intSetOf(3, 4)).toBoxedList().sorted())
        assertTrue((intSetOf(1, 2) subtract intSetOf(1, 2)).isEmpty())
        assertEquals(listOf(1, 2, 3), (intSetOf(1, 2, 3) subtract intSetOf()).toBoxedList().sorted())
        assertTrue((intSetOf() subtract intSetOf(1)).isEmpty())
    }

    @Test
    fun subtract_doesNotMutateEitherOperand() {
        val a = intSetOf(1, 2, 3)
        val b = intSetOf(3)
        a subtract b
        assertEquals(listOf(1, 2, 3), a.toBoxedList().sorted())
        assertEquals(listOf(3), b.toBoxedList())
    }

    // ---------- boxed views ----------

    @Test
    fun asSet_readOnlyView_reflectsTheBackingSet() {
        val backing = mutableIntSetOf(1, 2, 3)
        val view: Set<Int> = (backing as IntSet).asSet()
        assertEquals(setOf(1, 2, 3), view)
        assertTrue(view.contains(1))
        assertFalse(view.contains(9))

        backing.add(4)
        assertEquals(setOf(1, 2, 3, 4), view)
    }

    @Test
    fun asSet_mutableView_writesThrough() {
        val backing = mutableIntSetOf(1, 2, 3)
        val view = backing.asSet()

        assertTrue(view.add(4))
        assertFalse(view.add(4))
        assertEquals(listOf(1, 2, 3, 4), backing.toBoxedList().sorted())

        assertTrue(view.remove(1))
        assertEquals(listOf(2, 3, 4), backing.toBoxedList().sorted())

        view.clear()
        assertTrue(backing.isEmpty())
    }
}
