@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package io.github.sooniln.fastcollect

import kotlin.test.*

// Three tests per generated set type; see the note in ListConformanceTests for why they are this thin. Deep
// coverage lives in IntHashSetTests, IntSetDefaultsTests and RandomizedWorkloadTests. Byte has no set expansion
// (see build.gradle.kts).

class IntSetConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptySet<Int>(), intSetOf().asSet())
        assertEquals(setOf(1), intSetOf(1).asSet())
        assertEquals(setOf(1, 2, 3), intSetOf(1, 2, 3).asSet())
        assertEquals(setOf(1, 2), intSetOf(1, 2, 1).asSet())
        assertEquals(emptySet<Int>(), emptyIntSet().asSet())

        assertEquals(emptySet<Int>(), mutableIntSetOf().asSet())
        assertEquals(setOf(1), mutableIntSetOf(1).asSet())
        assertEquals(setOf(1, 2), mutableIntSetOf(1, 2).asSet())

        assertEquals(setOf(1, 2), buildIntSet { add(1); add(2); add(1) }.asSet())
        assertEquals(setOf(1, 2), IntHashSet(intListOf(1, 2)).asSet())
        assertEquals(setOf(1, 2), IntHashSet(listOf(1, 2)).asSet())
    }

    @Test
    fun mutationRoundTrip() {
        val set = IntHashSet()

        assertTrue(set.add(1))
        assertFalse(set.add(1))
        assertTrue(set.addAll(intListOf(2, 3)))
        assertFalse(set.addAll(listOf(2)))
        assertEquals(setOf(1, 2, 3), set.asSet())

        assertTrue(set.contains(2))
        assertFalse(set.contains(4))
        assertTrue(set.containsAll(intSetOf(1, 3)))
        assertFalse(set.containsAll(intSetOf(1, 4)))

        assertTrue(set.remove(1))
        assertFalse(set.remove(1))
        assertTrue(set.retainAll(listOf(2)))
        assertEquals(setOf(2), set.asSet())

        // enough elements to force several rehashes, then remove half of them again
        set.addAll(IntList(300) { it.toInt() })
        for (i in 0..<300 step 2) assertTrue(set.remove(i.toInt()), "lost element $i")
        for (i in 1..<300 step 2) assertTrue(set.contains(i.toInt()), "lost element $i")

        set.clear()
        assertTrue(set.isEmpty())
    }

    @Test
    fun iterationEqualityAndSetOperators() {
        val set = IntHashSet(intListOf(1, 2, 3))
        val expected = listOf(1, 2, 3)

        // hash iteration order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Int>()
        val iterator = set.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextInt())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Int>()
        set.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        val fromTraverser = mutableListOf<Int>()
        val traverser = set.traverser()
        while (traverser.forward()) {
            fromTraverser.add(traverser.value)
            if (traverser.value == 2) traverser.remove()
        }
        assertEquals(expected, fromTraverser.sorted())
        assertEquals(setOf(1, 3), set.asSet())

        val a = intSetOf(1, 2, 3)
        val b = intSetOf(3, 4)
        assertEquals(setOf(1, 2, 3, 4), (a union b).asSet())
        assertEquals(setOf(3), (a intersect b).asSet())
        assertEquals(setOf(1, 2), (a subtract b).asSet())

        // equality holds against a different IntSet implementation
        assertEquals<IntSet>(intSetOf(1), IntHashSet(intListOf(1)))
        assertEquals(intSetOf(1).hashCode(), IntHashSet(intListOf(1)).hashCode())
        assertEquals<IntSet>(emptyIntSet(), IntHashSet())
        assertNotEquals<IntSet>(intSetOf(2), IntHashSet(intListOf(1)))
    }
}

class LongSetConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptySet<Long>(), longSetOf().asSet())
        assertEquals(setOf(1L), longSetOf(1L).asSet())
        assertEquals(setOf(1L, 2L, 3L), longSetOf(1L, 2L, 3L).asSet())
        assertEquals(setOf(1L, 2L), longSetOf(1L, 2L, 1L).asSet())
        assertEquals(emptySet<Long>(), emptyLongSet().asSet())

        assertEquals(emptySet<Long>(), mutableLongSetOf().asSet())
        assertEquals(setOf(1L), mutableLongSetOf(1L).asSet())
        assertEquals(setOf(1L, 2L), mutableLongSetOf(1L, 2L).asSet())

        assertEquals(setOf(1L, 2L), buildLongSet { add(1L); add(2L); add(1L) }.asSet())
        assertEquals(setOf(1L, 2L), LongHashSet(longListOf(1L, 2L)).asSet())
        assertEquals(setOf(1L, 2L), LongHashSet(listOf(1L, 2L)).asSet())
    }

    @Test
    fun mutationRoundTrip() {
        val set = LongHashSet()

        assertTrue(set.add(1L))
        assertFalse(set.add(1L))
        assertTrue(set.addAll(longListOf(2L, 3L)))
        assertFalse(set.addAll(listOf(2L)))
        assertEquals(setOf(1L, 2L, 3L), set.asSet())

        assertTrue(set.contains(2L))
        assertFalse(set.contains(4L))
        assertTrue(set.containsAll(longSetOf(1L, 3L)))
        assertFalse(set.containsAll(longSetOf(1L, 4L)))

        assertTrue(set.remove(1L))
        assertFalse(set.remove(1L))
        assertTrue(set.retainAll(listOf(2L)))
        assertEquals(setOf(2L), set.asSet())

        // enough elements to force several rehashes, then remove half of them again
        set.addAll(LongList(300) { it.toLong() })
        for (i in 0..<300 step 2) assertTrue(set.remove(i.toLong()), "lost element $i")
        for (i in 1..<300 step 2) assertTrue(set.contains(i.toLong()), "lost element $i")

        set.clear()
        assertTrue(set.isEmpty())
    }

    @Test
    fun iterationEqualityAndSetOperators() {
        val set = LongHashSet(longListOf(1L, 2L, 3L))
        val expected = listOf(1L, 2L, 3L)

        // hash iteration order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Long>()
        val iterator = set.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextLong())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Long>()
        set.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        val fromTraverser = mutableListOf<Long>()
        val traverser = set.traverser()
        while (traverser.forward()) {
            fromTraverser.add(traverser.value)
            if (traverser.value == 2L) traverser.remove()
        }
        assertEquals(expected, fromTraverser.sorted())
        assertEquals(setOf(1L, 3L), set.asSet())

        val a = longSetOf(1L, 2L, 3L)
        val b = longSetOf(3L, 4L)
        assertEquals(setOf(1L, 2L, 3L, 4L), (a union b).asSet())
        assertEquals(setOf(3L), (a intersect b).asSet())
        assertEquals(setOf(1L, 2L), (a subtract b).asSet())

        // equality holds against a different LongSet implementation
        assertEquals<LongSet>(longSetOf(1L), LongHashSet(longListOf(1L)))
        assertEquals(longSetOf(1L).hashCode(), LongHashSet(longListOf(1L)).hashCode())
        assertEquals<LongSet>(emptyLongSet(), LongHashSet())
        assertNotEquals<LongSet>(longSetOf(2L), LongHashSet(longListOf(1L)))
    }
}

class FloatSetConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptySet<Float>(), floatSetOf().asSet())
        assertEquals(setOf(1f), floatSetOf(1f).asSet())
        assertEquals(setOf(1f, 2f, 3f), floatSetOf(1f, 2f, 3f).asSet())
        assertEquals(setOf(1f, 2f), floatSetOf(1f, 2f, 1f).asSet())
        assertEquals(emptySet<Float>(), emptyFloatSet().asSet())

        assertEquals(emptySet<Float>(), mutableFloatSetOf().asSet())
        assertEquals(setOf(1f), mutableFloatSetOf(1f).asSet())
        assertEquals(setOf(1f, 2f), mutableFloatSetOf(1f, 2f).asSet())

        assertEquals(setOf(1f, 2f), buildFloatSet { add(1f); add(2f); add(1f) }.asSet())
        assertEquals(setOf(1f, 2f), FloatHashSet(floatListOf(1f, 2f)).asSet())
        assertEquals(setOf(1f, 2f), FloatHashSet(listOf(1f, 2f)).asSet())
    }

    @Test
    fun mutationRoundTrip() {
        val set = FloatHashSet()

        assertTrue(set.add(1f))
        assertFalse(set.add(1f))
        assertTrue(set.addAll(floatListOf(2f, 3f)))
        assertFalse(set.addAll(listOf(2f)))
        assertEquals(setOf(1f, 2f, 3f), set.asSet())

        assertTrue(set.contains(2f))
        assertFalse(set.contains(4f))
        assertTrue(set.containsAll(floatSetOf(1f, 3f)))
        assertFalse(set.containsAll(floatSetOf(1f, 4f)))

        assertTrue(set.remove(1f))
        assertFalse(set.remove(1f))
        assertTrue(set.retainAll(listOf(2f)))
        assertEquals(setOf(2f), set.asSet())

        // enough elements to force several rehashes, then remove half of them again
        set.addAll(FloatList(300) { it.toFloat() })
        for (i in 0..<300 step 2) assertTrue(set.remove(i.toFloat()), "lost element $i")
        for (i in 1..<300 step 2) assertTrue(set.contains(i.toFloat()), "lost element $i")

        set.clear()
        assertTrue(set.isEmpty())
    }

    @Test
    fun iterationEqualityAndSetOperators() {
        val set = FloatHashSet(floatListOf(1f, 2f, 3f))
        val expected = listOf(1f, 2f, 3f)

        // hash iteration order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Float>()
        val iterator = set.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextFloat())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Float>()
        set.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        val fromTraverser = mutableListOf<Float>()
        val traverser = set.traverser()
        while (traverser.forward()) {
            fromTraverser.add(traverser.value)
            if (traverser.value == 2f) traverser.remove()
        }
        assertEquals(expected, fromTraverser.sorted())
        assertEquals(setOf(1f, 3f), set.asSet())

        val a = floatSetOf(1f, 2f, 3f)
        val b = floatSetOf(3f, 4f)
        assertEquals(setOf(1f, 2f, 3f, 4f), (a union b).asSet())
        assertEquals(setOf(3f), (a intersect b).asSet())
        assertEquals(setOf(1f, 2f), (a subtract b).asSet())

        // equality holds against a different FloatSet implementation
        assertEquals<FloatSet>(floatSetOf(1f), FloatHashSet(floatListOf(1f)))
        assertEquals(floatSetOf(1f).hashCode(), FloatHashSet(floatListOf(1f)).hashCode())
        assertEquals<FloatSet>(emptyFloatSet(), FloatHashSet())
        assertNotEquals<FloatSet>(floatSetOf(2f), FloatHashSet(floatListOf(1f)))
    }
}

class DoubleSetConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptySet<Double>(), doubleSetOf().asSet())
        assertEquals(setOf(1.0), doubleSetOf(1.0).asSet())
        assertEquals(setOf(1.0, 2.0, 3.0), doubleSetOf(1.0, 2.0, 3.0).asSet())
        assertEquals(setOf(1.0, 2.0), doubleSetOf(1.0, 2.0, 1.0).asSet())
        assertEquals(emptySet<Double>(), emptyDoubleSet().asSet())

        assertEquals(emptySet<Double>(), mutableDoubleSetOf().asSet())
        assertEquals(setOf(1.0), mutableDoubleSetOf(1.0).asSet())
        assertEquals(setOf(1.0, 2.0), mutableDoubleSetOf(1.0, 2.0).asSet())

        assertEquals(setOf(1.0, 2.0), buildDoubleSet { add(1.0); add(2.0); add(1.0) }.asSet())
        assertEquals(setOf(1.0, 2.0), DoubleHashSet(doubleListOf(1.0, 2.0)).asSet())
        assertEquals(setOf(1.0, 2.0), DoubleHashSet(listOf(1.0, 2.0)).asSet())
    }

    @Test
    fun mutationRoundTrip() {
        val set = DoubleHashSet()

        assertTrue(set.add(1.0))
        assertFalse(set.add(1.0))
        assertTrue(set.addAll(doubleListOf(2.0, 3.0)))
        assertFalse(set.addAll(listOf(2.0)))
        assertEquals(setOf(1.0, 2.0, 3.0), set.asSet())

        assertTrue(set.contains(2.0))
        assertFalse(set.contains(4.0))
        assertTrue(set.containsAll(doubleSetOf(1.0, 3.0)))
        assertFalse(set.containsAll(doubleSetOf(1.0, 4.0)))

        assertTrue(set.remove(1.0))
        assertFalse(set.remove(1.0))
        assertTrue(set.retainAll(listOf(2.0)))
        assertEquals(setOf(2.0), set.asSet())

        // enough elements to force several rehashes, then remove half of them again
        set.addAll(DoubleList(300) { it.toDouble() })
        for (i in 0..<300 step 2) assertTrue(set.remove(i.toDouble()), "lost element $i")
        for (i in 1..<300 step 2) assertTrue(set.contains(i.toDouble()), "lost element $i")

        set.clear()
        assertTrue(set.isEmpty())
    }

    @Test
    fun iterationEqualityAndSetOperators() {
        val set = DoubleHashSet(doubleListOf(1.0, 2.0, 3.0))
        val expected = listOf(1.0, 2.0, 3.0)

        // hash iteration order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Double>()
        val iterator = set.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextDouble())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Double>()
        set.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        val fromTraverser = mutableListOf<Double>()
        val traverser = set.traverser()
        while (traverser.forward()) {
            fromTraverser.add(traverser.value)
            if (traverser.value == 2.0) traverser.remove()
        }
        assertEquals(expected, fromTraverser.sorted())
        assertEquals(setOf(1.0, 3.0), set.asSet())

        val a = doubleSetOf(1.0, 2.0, 3.0)
        val b = doubleSetOf(3.0, 4.0)
        assertEquals(setOf(1.0, 2.0, 3.0, 4.0), (a union b).asSet())
        assertEquals(setOf(3.0), (a intersect b).asSet())
        assertEquals(setOf(1.0, 2.0), (a subtract b).asSet())

        // equality holds against a different DoubleSet implementation
        assertEquals<DoubleSet>(doubleSetOf(1.0), DoubleHashSet(doubleListOf(1.0)))
        assertEquals(doubleSetOf(1.0).hashCode(), DoubleHashSet(doubleListOf(1.0)).hashCode())
        assertEquals<DoubleSet>(emptyDoubleSet(), DoubleHashSet())
        assertNotEquals<DoubleSet>(doubleSetOf(2.0), DoubleHashSet(doubleListOf(1.0)))
    }
}
