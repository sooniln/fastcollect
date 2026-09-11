package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Deep coverage for HashSet.kte. Int stands in for the Long/Float/Double expansions, which are generated from
 * this same template and covered by SetConformanceTests and the type-semantics suites.
 *
 * These are the deterministic edge cases. Long random operation sequences diffed against a reference HashSet -
 * which is what drives probing, deletion chains, growth and shrink - live in RandomizedWorkloadTests.
 */
class IntHashSetTests {

    @Test
    fun constructors_produceExpectedContents() {
        assertTrue(IntHashSet().isEmpty())
        assertTrue(IntHashSet(64).isEmpty())
        assertEquals(listOf(1, 2, 3), IntHashSet(intListOf(1, 2, 3)).toBoxedList().sorted())
        assertEquals(listOf(1, 2, 3), IntHashSet(listOf(1, 2, 3)).toBoxedList().sorted())
        // duplicates in the source collapse
        assertEquals(listOf(1, 2), IntHashSet(intListOf(1, 2, 1, 2)).toBoxedList().sorted())
    }

    @Test
    fun constructor_negativeCapacity_throws() {
        assertFailsWith<IllegalArgumentException> { IntHashSet(-1) }
    }

    @Test
    fun add_reportsWhetherTheElementWasNew() {
        val set = IntHashSet()
        assertTrue(set.add(1))
        assertFalse(set.add(1))
        assertTrue(set.add(2))
        assertEquals(2, set.size)
    }

    @Test
    fun remove_reportsWhetherTheElementWasPresent() {
        val set = IntHashSet(intListOf(1, 2, 3))
        assertTrue(set.remove(2))
        assertFalse(set.remove(2))
        assertFalse(set.remove(99))
        assertEquals(listOf(1, 3), set.toBoxedList().sorted())
    }

    @Test
    fun contains_findsPresentElementsOnly() {
        val set = IntHashSet(intListOf(1, 2, 3))
        assertTrue(set.contains(1))
        assertFalse(set.contains(4))
        assertFalse(IntHashSet().contains(0))
    }

    @Test
    fun clear_emptiesAndAllowsReuse() {
        val set = IntHashSet(intListOf(1, 2, 3))
        set.clear()
        assertTrue(set.isEmpty())
        assertFalse(set.contains(1))
        assertTrue(set.add(1))
        assertEquals(listOf(1), set.toBoxedList())
    }

    // ---------- zero ----------

    @Test
    fun zero_isAnOrdinaryElement() {
        // zero is the boundary value most likely to be confused with an empty slot
        val set = IntHashSet(intListOf(1, 2))
        assertFalse(set.contains(0))

        assertTrue(set.add(0))
        assertTrue(set.contains(0))
        assertEquals(listOf(0, 1, 2), set.toBoxedList().sorted())

        assertTrue(set.remove(0))
        assertFalse(set.contains(0))
        assertEquals(listOf(1, 2), set.toBoxedList().sorted())
    }

    // ---------- capacity ----------

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val set = IntHashSet(intListOf(1, 2, 3))
        set.ensureCapacity(10_000)
        assertEquals(listOf(1, 2, 3), set.toBoxedList().sorted())
        set.add(4)
        assertEquals(listOf(1, 2, 3, 4), set.toBoxedList().sorted())
    }

    @Test
    fun ensureCapacity_negative_throws() {
        assertFailsWith<IllegalArgumentException> { IntHashSet().ensureCapacity(-1) }
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val set = IntHashSet(1000)
        for (i in 1..3) set.add(i)
        set.trimToSize()
        assertEquals(listOf(1, 2, 3), set.toBoxedList().sorted())
        set.add(4)
        assertEquals(listOf(1, 2, 3, 4), set.toBoxedList().sorted())
    }

    // ---------- iteration ----------

    @Test
    fun iteration_visitsEveryElement() {
        val set = IntHashSet()
        for (i in 0..50) set.add(i)

        val fromForeach = mutableListOf<Int>()
        for (value in set) fromForeach.add(value)

        // hash iteration order is unspecified, so only the multiset is guaranteed
        assertEquals((0..50).toList(), fromForeach.sorted())
    }

    @Test
    fun iteration_emptyAndSingletonSet() {
        val fromEmpty = mutableListOf<Int>()
        for (value in IntHashSet()) fromEmpty.add(value)
        assertEquals(emptyList(), fromEmpty)

        val fromSingleton = mutableListOf<Int>()
        for (value in IntHashSet(intListOf(42))) fromSingleton.add(value)
        assertEquals(listOf(42), fromSingleton)
    }

    @Test
    fun iteratorRemove_drainsTheWholeSet() {
        val set = IntHashSet()
        for (i in 1..200) set.add(i)

        val iterator = set.iterator()
        val visited = mutableListOf<Int>()
        while (iterator.hasNext()) {
            visited.add(iterator.nextInt())
            iterator.remove()
        }

        assertEquals((1..200).toList(), visited.sorted())
        assertTrue(set.isEmpty())
    }

    @Test
    fun iteratorRemove_beforeNext_throws() {
        assertFailsWith<IllegalStateException> { IntHashSet(intListOf(1)).iterator().remove() }
    }

    @Test
    fun iteratorNext_pastEnd_throws() {
        val iterator = IntHashSet(intListOf(1)).iterator()
        iterator.nextInt()
        assertFailsWith<NoSuchElementException> { iterator.nextInt() }
    }

    @Test
    fun iteratorRemove_visitsEveryElementExactlyOnceAndRemovesMatching() {
        val set = IntHashSet()
        for (i in 1..50) set.add(i)

        val visited = mutableListOf<Int>()
        val iterator = set.iterator()
        while (iterator.hasNext()) {
            val value = iterator.nextInt()
            visited.add(value)
            if (value % 2 == 0) iterator.remove()
        }

        assertEquals((1..50).toList(), visited.sorted(), "every element must be visited exactly once")
        assertEquals((1..50).filter { it % 2 != 0 }, set.toBoxedList().sorted())
    }

    @Test
    fun iterator_advancesPastTheLastElement() {
        val iterator = IntHashSet(intListOf(1)).iterator()
        assertTrue(iterator.hasNext())
        assertEquals(1, iterator.nextInt())
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.nextInt() }
    }

    @Test
    fun iterator_removeTwiceInARowThrows() {
        val set = IntHashSet(intListOf(1, 2))
        val iterator = set.iterator()
        iterator.nextInt()
        iterator.remove()
        assertFailsWith<IllegalStateException> { iterator.remove() }
        assertTrue(iterator.hasNext())
        iterator.nextInt()
    }

    // ---------- equality ----------

    @Test
    fun equals_matchesAnyIntSetImplementation() {
        // AbstractIntSet.equals() must hold against ANY IntSet implementation, not just IntHashSet
        val hash: IntSet = IntHashSet(intListOf(42))
        val singleton = intSetOf(42)
        assertEquals(singleton, hash)
        assertEquals(hash, singleton)
        assertEquals(singleton.hashCode(), hash.hashCode())

        val empty: IntSet = IntHashSet()
        assertEquals(emptyIntSet(), empty)
        assertEquals(empty, emptyIntSet())
        assertEquals(emptyIntSet().hashCode(), empty.hashCode())

        assertNotEquals(intSetOf(1), hash)
        assertNotEquals(emptyIntSet(), hash)
    }

    @Test
    fun equals_isOrderIndependent() {
        val a = IntHashSet(intListOf(1, 2, 3))
        val b = IntHashSet(intListOf(3, 2, 1))
        assertEquals<IntSet>(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals<IntSet>(IntHashSet(intListOf(1, 2)), a)
    }
}
