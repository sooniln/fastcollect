@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package io.github.sooniln.fastcollect

import kotlin.test.*

// Three tests per generated priority queue type; see the note in ListConformanceTests for why they are this
// thin. Deep coverage lives in IntPriorityQueueTests and RandomizedWorkloadTests, and Float/Double NaN ordering
// in FloatDoubleSemanticsTests.

class BytePriorityQueueConformanceTest {

    @Test
    fun factories_produceTheSameHeapContents() {
        val expected = listOf(1.toByte(), 2.toByte(), 3.toByte())

        assertEquals(expected, BytePriorityQueue(byteArrayOf(3.toByte(), 1.toByte(), 2.toByte())).drain())
        assertEquals(expected, BytePriorityQueue(byteArrayOf(0.toByte(), 3.toByte(), 1.toByte(), 2.toByte()), 1, 4).drain())
        assertEquals(expected, BytePriorityQueue(byteListOf(3.toByte(), 1.toByte(), 2.toByte())).drain())
        assertEquals(expected, BytePriorityQueue(listOf(3.toByte(), 1.toByte(), 2.toByte())).drain())
        assertEquals(expected, bytePriorityQueueOf(3.toByte(), 1.toByte(), 2.toByte()).drain())
        assertEquals(expected.reversed(), byteDescendingPriorityQueueOf(3.toByte(), 1.toByte(), 2.toByte()).drain())
        assertEquals(expected, buildBytePriorityQueue { addAll(byteListOf(3.toByte(), 1.toByte(), 2.toByte())) }.drain())
        assertTrue(BytePriorityQueue().isEmpty())
    }

    @Test
    fun mutationRoundTrip() {
        val queue = BytePriorityQueue()

        for (v in listOf(3.toByte(), 1.toByte(), 4.toByte(), 2.toByte())) queue.add(v)
        queue.addAll(byteArrayOf(0.toByte(), 5.toByte()), 1, 2)
        queue.addAll(byteListOf(0.toByte()))
        queue.addAll(listOf(6.toByte()))

        assertEquals(0.toByte(), queue.first())
        assertEquals(0.toByte(), queue.removeFirst())

        assertTrue(queue.contains(4.toByte()))
        assertFalse(queue.contains(9.toByte()))
        assertTrue(queue.remove(4.toByte()))
        assertFalse(queue.remove(4.toByte()))

        assertTrue(queue.removeAll(byteListOf(6.toByte())))
        assertTrue(queue.retainAll { it < 3.toByte() })
        assertEquals(listOf(1.toByte(), 2.toByte()), queue.drain())

        val descending = BytePriorityQueue(descending = true)
        for (v in listOf(3.toByte(), 1.toByte(), 2.toByte())) descending.add(v)
        assertEquals(listOf(3.toByte(), 2.toByte(), 1.toByte()), descending.drain())

        val cleared = bytePriorityQueueOf(1.toByte(), 2.toByte())
        cleared.clear()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun iterationVisitsEveryElement() {
        val queue = bytePriorityQueueOf(3.toByte(), 1.toByte(), 4.toByte(), 2.toByte())
        val expected = listOf(1.toByte(), 2.toByte(), 3.toByte(), 4.toByte())

        // heap order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Byte>()
        val iterator = queue.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextByte())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Byte>()
        queue.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        assertEquals(expected, ByteArray(4).also { queue.copyInto(it) }.sorted())
    }
}

class IntPriorityQueueConformanceTest {

    @Test
    fun factories_produceTheSameHeapContents() {
        val expected = listOf(1, 2, 3)

        assertEquals(expected, IntPriorityQueue(intArrayOf(3, 1, 2)).drain())
        assertEquals(expected, IntPriorityQueue(intArrayOf(0, 3, 1, 2), 1, 4).drain())
        assertEquals(expected, IntPriorityQueue(intListOf(3, 1, 2)).drain())
        assertEquals(expected, IntPriorityQueue(listOf(3, 1, 2)).drain())
        assertEquals(expected, intPriorityQueueOf(3, 1, 2).drain())
        assertEquals(expected.reversed(), intDescendingPriorityQueueOf(3, 1, 2).drain())
        assertEquals(expected, buildIntPriorityQueue { addAll(intListOf(3, 1, 2)) }.drain())
        assertTrue(IntPriorityQueue().isEmpty())
    }

    @Test
    fun mutationRoundTrip() {
        val queue = IntPriorityQueue()

        for (v in listOf(3, 1, 4, 2)) queue.add(v)
        queue.addAll(intArrayOf(0, 5), 1, 2)
        queue.addAll(intListOf(0))
        queue.addAll(listOf(6))

        assertEquals(0, queue.first())
        assertEquals(0, queue.removeFirst())

        assertTrue(queue.contains(4))
        assertFalse(queue.contains(9))
        assertTrue(queue.remove(4))
        assertFalse(queue.remove(4))

        assertTrue(queue.removeAll(intListOf(6)))
        assertTrue(queue.retainAll { it < 3 })
        assertEquals(listOf(1, 2), queue.drain())

        val descending = IntPriorityQueue(descending = true)
        for (v in listOf(3, 1, 2)) descending.add(v)
        assertEquals(listOf(3, 2, 1), descending.drain())

        val cleared = intPriorityQueueOf(1, 2)
        cleared.clear()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun iterationVisitsEveryElement() {
        val queue = intPriorityQueueOf(3, 1, 4, 2)
        val expected = listOf(1, 2, 3, 4)

        // heap order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Int>()
        val iterator = queue.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextInt())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Int>()
        queue.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        assertEquals(expected, IntArray(4).also { queue.copyInto(it) }.sorted())
    }
}

class LongPriorityQueueConformanceTest {

    @Test
    fun factories_produceTheSameHeapContents() {
        val expected = listOf(1L, 2L, 3L)

        assertEquals(expected, LongPriorityQueue(longArrayOf(3L, 1L, 2L)).drain())
        assertEquals(expected, LongPriorityQueue(longArrayOf(0L, 3L, 1L, 2L), 1, 4).drain())
        assertEquals(expected, LongPriorityQueue(longListOf(3L, 1L, 2L)).drain())
        assertEquals(expected, LongPriorityQueue(listOf(3L, 1L, 2L)).drain())
        assertEquals(expected, longPriorityQueueOf(3L, 1L, 2L).drain())
        assertEquals(expected.reversed(), longDescendingPriorityQueueOf(3L, 1L, 2L).drain())
        assertEquals(expected, buildLongPriorityQueue { addAll(longListOf(3L, 1L, 2L)) }.drain())
        assertTrue(LongPriorityQueue().isEmpty())
    }

    @Test
    fun mutationRoundTrip() {
        val queue = LongPriorityQueue()

        for (v in listOf(3L, 1L, 4L, 2L)) queue.add(v)
        queue.addAll(longArrayOf(0L, 5L), 1, 2)
        queue.addAll(longListOf(0L))
        queue.addAll(listOf(6L))

        assertEquals(0L, queue.first())
        assertEquals(0L, queue.removeFirst())

        assertTrue(queue.contains(4L))
        assertFalse(queue.contains(9L))
        assertTrue(queue.remove(4L))
        assertFalse(queue.remove(4L))

        assertTrue(queue.removeAll(longListOf(6L)))
        assertTrue(queue.retainAll { it < 3L })
        assertEquals(listOf(1L, 2L), queue.drain())

        val descending = LongPriorityQueue(descending = true)
        for (v in listOf(3L, 1L, 2L)) descending.add(v)
        assertEquals(listOf(3L, 2L, 1L), descending.drain())

        val cleared = longPriorityQueueOf(1L, 2L)
        cleared.clear()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun iterationVisitsEveryElement() {
        val queue = longPriorityQueueOf(3L, 1L, 4L, 2L)
        val expected = listOf(1L, 2L, 3L, 4L)

        // heap order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Long>()
        val iterator = queue.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextLong())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Long>()
        queue.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        assertEquals(expected, LongArray(4).also { queue.copyInto(it) }.sorted())
    }
}

class FloatPriorityQueueConformanceTest {

    @Test
    fun factories_produceTheSameHeapContents() {
        val expected = listOf(1f, 2f, 3f)

        assertEquals(expected, FloatPriorityQueue(floatArrayOf(3f, 1f, 2f)).drain())
        assertEquals(expected, FloatPriorityQueue(floatArrayOf(0f, 3f, 1f, 2f), 1, 4).drain())
        assertEquals(expected, FloatPriorityQueue(floatListOf(3f, 1f, 2f)).drain())
        assertEquals(expected, FloatPriorityQueue(listOf(3f, 1f, 2f)).drain())
        assertEquals(expected, floatPriorityQueueOf(3f, 1f, 2f).drain())
        assertEquals(expected.reversed(), floatDescendingPriorityQueueOf(3f, 1f, 2f).drain())
        assertEquals(expected, buildFloatPriorityQueue { addAll(floatListOf(3f, 1f, 2f)) }.drain())
        assertTrue(FloatPriorityQueue().isEmpty())
    }

    @Test
    fun mutationRoundTrip() {
        val queue = FloatPriorityQueue()

        for (v in listOf(3f, 1f, 4f, 2f)) queue.add(v)
        queue.addAll(floatArrayOf(0f, 5f), 1, 2)
        queue.addAll(floatListOf(0f))
        queue.addAll(listOf(6f))

        assertEquals(0f, queue.first())
        assertEquals(0f, queue.removeFirst())

        assertTrue(queue.contains(4f))
        assertFalse(queue.contains(9f))
        assertTrue(queue.remove(4f))
        assertFalse(queue.remove(4f))

        assertTrue(queue.removeAll(floatListOf(6f)))
        assertTrue(queue.retainAll { it < 3f })
        assertEquals(listOf(1f, 2f), queue.drain())

        val descending = FloatPriorityQueue(descending = true)
        for (v in listOf(3f, 1f, 2f)) descending.add(v)
        assertEquals(listOf(3f, 2f, 1f), descending.drain())

        val cleared = floatPriorityQueueOf(1f, 2f)
        cleared.clear()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun iterationVisitsEveryElement() {
        val queue = floatPriorityQueueOf(3f, 1f, 4f, 2f)
        val expected = listOf(1f, 2f, 3f, 4f)

        // heap order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Float>()
        val iterator = queue.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextFloat())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Float>()
        queue.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        assertEquals(expected, FloatArray(4).also { queue.copyInto(it) }.sorted())
    }
}

class DoublePriorityQueueConformanceTest {

    @Test
    fun factories_produceTheSameHeapContents() {
        val expected = listOf(1.0, 2.0, 3.0)

        assertEquals(expected, DoublePriorityQueue(doubleArrayOf(3.0, 1.0, 2.0)).drain())
        assertEquals(expected, DoublePriorityQueue(doubleArrayOf(0.0, 3.0, 1.0, 2.0), 1, 4).drain())
        assertEquals(expected, DoublePriorityQueue(doubleListOf(3.0, 1.0, 2.0)).drain())
        assertEquals(expected, DoublePriorityQueue(listOf(3.0, 1.0, 2.0)).drain())
        assertEquals(expected, doublePriorityQueueOf(3.0, 1.0, 2.0).drain())
        assertEquals(expected.reversed(), doubleDescendingPriorityQueueOf(3.0, 1.0, 2.0).drain())
        assertEquals(expected, buildDoublePriorityQueue { addAll(doubleListOf(3.0, 1.0, 2.0)) }.drain())
        assertTrue(DoublePriorityQueue().isEmpty())
    }

    @Test
    fun mutationRoundTrip() {
        val queue = DoublePriorityQueue()

        for (v in listOf(3.0, 1.0, 4.0, 2.0)) queue.add(v)
        queue.addAll(doubleArrayOf(0.0, 5.0), 1, 2)
        queue.addAll(doubleListOf(0.0))
        queue.addAll(listOf(6.0))

        assertEquals(0.0, queue.first())
        assertEquals(0.0, queue.removeFirst())

        assertTrue(queue.contains(4.0))
        assertFalse(queue.contains(9.0))
        assertTrue(queue.remove(4.0))
        assertFalse(queue.remove(4.0))

        assertTrue(queue.removeAll(doubleListOf(6.0)))
        assertTrue(queue.retainAll { it < 3.0 })
        assertEquals(listOf(1.0, 2.0), queue.drain())

        val descending = DoublePriorityQueue(descending = true)
        for (v in listOf(3.0, 1.0, 2.0)) descending.add(v)
        assertEquals(listOf(3.0, 2.0, 1.0), descending.drain())

        val cleared = doublePriorityQueueOf(1.0, 2.0)
        cleared.clear()
        assertTrue(cleared.isEmpty())
    }

    @Test
    fun iterationVisitsEveryElement() {
        val queue = doublePriorityQueueOf(3.0, 1.0, 4.0, 2.0)
        val expected = listOf(1.0, 2.0, 3.0, 4.0)

        // heap order is unspecified, so only the multiset is guaranteed
        val fromIterator = mutableListOf<Double>()
        val iterator = queue.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextDouble())
        assertEquals(expected, fromIterator.sorted())

        val fromForeach = mutableListOf<Double>()
        queue.foreach { fromForeach.add(it) }
        assertEquals(expected, fromForeach.sorted())

        assertEquals(expected, DoubleArray(4).also { queue.copyInto(it) }.sorted())
    }
}
