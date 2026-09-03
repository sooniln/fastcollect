@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package io.github.sooniln.fastcollect

import kotlin.test.*

// Three tests per generated list type. Deep behavioural coverage lives in IntArrayDequeTests and
// IntListDefaultsTests, and randomized differential coverage in RandomizedWorkloadTests; these classes exist to
// prove each template expansion is wired up and its logic holds for that element type. Most of that proof is at
// compile time - naming every factory for the expansion will not compile if one is missing or mistyped - so the
// runtime assertions stay thin. Anything genuinely type-specific belongs in FloatDoubleSemanticsTests or
// ByteSemanticsTests. Keep the classes identical to one another apart from the element type.

class ByteListConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyList(), byteListOf().asList())
        assertEquals(listOf(1.toByte()), byteListOf(1.toByte()).asList())
        assertEquals(listOf(1.toByte(), 2.toByte(), 3.toByte()), byteListOf(1.toByte(), 2.toByte(), 3.toByte()).asList())
        assertEquals(emptyList(), emptyByteList().asList())

        assertEquals(emptyList(), mutableByteListOf().asList())
        assertEquals(listOf(1.toByte()), mutableByteListOf(1.toByte()).asList())
        assertEquals(listOf(1.toByte(), 2.toByte()), mutableByteListOf(1.toByte(), 2.toByte()).asList())

        assertEquals(listOf(1.toByte(), 2.toByte()), buildByteList { add(1.toByte()); add(2.toByte()) }.asList())
        assertEquals(listOf(0.toByte(), 1.toByte(), 2.toByte()), ByteList(3) { it.toByte() }.asList())
        assertEquals(listOf(0.toByte(), 1.toByte(), 2.toByte()), MutableByteList(3) { it.toByte() }.asList())
        assertEquals(listOf(1.toByte(), 2.toByte(), 3.toByte()), byteArrayOf(1.toByte(), 2.toByte(), 3.toByte()).asByteList().asList())
    }

    @Test
    fun mutationRoundTrip() {
        val list = mutableByteListOf(2.toByte())

        list.addFirst(1.toByte())
        list.addLast(4.toByte())
        list.add(2, 3.toByte())
        assertEquals(listOf(1.toByte(), 2.toByte(), 3.toByte(), 4.toByte()), list.asList())

        assertEquals(2.toByte(), list.replace(1, 0.toByte()))
        list[1] = 2.toByte()
        assertEquals(1.toByte(), list.removeFirst())
        assertEquals(4.toByte(), list.removeLast())
        assertEquals(3.toByte(), list.removeAt(1))
        assertEquals(listOf(2.toByte()), list.asList())

        list.addAll(0, byteListOf(0.toByte(), 1.toByte()))
        assertTrue(list.addAll(listOf(3.toByte())))
        assertEquals(listOf(0.toByte(), 1.toByte(), 2.toByte(), 3.toByte()), list.asList())

        assertEquals(1, list.indexOf(1.toByte()))
        assertTrue(list.contains(3.toByte()))
        assertFalse(list.contains(4.toByte()))

        list.sortDescending()
        assertEquals(listOf(3.toByte(), 2.toByte(), 1.toByte(), 0.toByte()), list.asList())
        list.reverse()
        assertEquals(listOf(0.toByte(), 1.toByte(), 2.toByte(), 3.toByte()), list.asList())

        assertTrue(list.removeAll(byteListOf(0.toByte(), 3.toByte())))
        assertTrue(list.retainAll(listOf(1.toByte())))
        assertEquals(listOf(1.toByte()), list.asList())

        assertEquals(listOf(1.toByte()), ByteArray(1).also { list.copyInto(it) }.toList())

        list.clear()
        assertTrue(list.isEmpty())
    }

    @Test
    fun iterationAndEquality() {
        val list = mutableByteListOf(1.toByte(), 2.toByte(), 3.toByte())
        val expected = listOf(1.toByte(), 2.toByte(), 3.toByte())

        val fromIterator = mutableListOf<Byte>()
        val iterator = list.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextByte())
        assertEquals(expected, fromIterator)

        val fromForeach = mutableListOf<Byte>()
        list.traverse { fromForeach.add(it) }
        assertEquals(expected, fromForeach)

        val fromTraverser = mutableListOf<Byte>()
        val traverser = list.traverser(0)
        while (traverser.forward()) fromTraverser.add(traverser.value)
        assertEquals(expected, fromTraverser)
        while (traverser.backward()) { }
        traverser.forward()
        traverser.set(0.toByte())
        assertEquals(listOf(0.toByte(), 2.toByte(), 3.toByte()), list.asList())

        val fromReverse = mutableListOf<Byte>()
        list.traverseReverse { fromReverse.add(it) }
        assertEquals(list.asList().reversed(), fromReverse)

        // equality holds against a different ByteList implementation, not just another deque
        val arrayBacked = byteArrayOf(0.toByte(), 2.toByte(), 3.toByte()).asByteList()
        assertEquals(arrayBacked, list)
        assertEquals(list, arrayBacked)
        assertEquals(arrayBacked.hashCode(), list.hashCode())
        assertNotEquals(byteListOf(0.toByte(), 2.toByte()), list)
    }
}

class IntListConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyList(), intListOf().asList())
        assertEquals(listOf(1), intListOf(1).asList())
        assertEquals(listOf(1, 2, 3), intListOf(1, 2, 3).asList())
        assertEquals(emptyList(), emptyIntList().asList())

        assertEquals(emptyList(), mutableIntListOf().asList())
        assertEquals(listOf(1), mutableIntListOf(1).asList())
        assertEquals(listOf(1, 2), mutableIntListOf(1, 2).asList())

        assertEquals(listOf(1, 2), buildIntList { add(1); add(2) }.asList())
        assertEquals(listOf(0, 1, 2), IntList(3) { it.toInt() }.asList())
        assertEquals(listOf(0, 1, 2), MutableIntList(3) { it.toInt() }.asList())
        assertEquals(listOf(1, 2, 3), intArrayOf(1, 2, 3).asIntList().asList())
    }

    @Test
    fun mutationRoundTrip() {
        val list = mutableIntListOf(2)

        list.addFirst(1)
        list.addLast(4)
        list.add(2, 3)
        assertEquals(listOf(1, 2, 3, 4), list.asList())

        assertEquals(2, list.replace(1, 0))
        list[1] = 2
        assertEquals(1, list.removeFirst())
        assertEquals(4, list.removeLast())
        assertEquals(3, list.removeAt(1))
        assertEquals(listOf(2), list.asList())

        list.addAll(0, intListOf(0, 1))
        assertTrue(list.addAll(listOf(3)))
        assertEquals(listOf(0, 1, 2, 3), list.asList())

        assertEquals(1, list.indexOf(1))
        assertTrue(list.contains(3))
        assertFalse(list.contains(4))

        list.sortDescending()
        assertEquals(listOf(3, 2, 1, 0), list.asList())
        list.reverse()
        assertEquals(listOf(0, 1, 2, 3), list.asList())

        assertTrue(list.removeAll(intListOf(0, 3)))
        assertTrue(list.retainAll(listOf(1)))
        assertEquals(listOf(1), list.asList())

        assertEquals(listOf(1), IntArray(1).also { list.copyInto(it) }.toList())

        list.clear()
        assertTrue(list.isEmpty())
    }

    @Test
    fun iterationAndEquality() {
        val list = mutableIntListOf(1, 2, 3)
        val expected = listOf(1, 2, 3)

        val fromIterator = mutableListOf<Int>()
        val iterator = list.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextInt())
        assertEquals(expected, fromIterator)

        val fromForeach = mutableListOf<Int>()
        list.traverse { fromForeach.add(it) }
        assertEquals(expected, fromForeach)

        val fromTraverser = mutableListOf<Int>()
        val traverser = list.traverser(0)
        while (traverser.forward()) fromTraverser.add(traverser.value)
        assertEquals(expected, fromTraverser)
        while (traverser.backward()) { }
        traverser.forward()
        traverser.set(0)
        assertEquals(listOf(0, 2, 3), list.asList())

        val fromReverse = mutableListOf<Int>()
        list.traverseReverse { fromReverse.add(it) }
        assertEquals(list.asList().reversed(), fromReverse)

        // equality holds against a different IntList implementation, not just another deque
        val arrayBacked = intArrayOf(0, 2, 3).asIntList()
        assertEquals(arrayBacked, list)
        assertEquals(list, arrayBacked)
        assertEquals(arrayBacked.hashCode(), list.hashCode())
        assertNotEquals(intListOf(0, 2), list)
    }
}

class LongListConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyList(), longListOf().asList())
        assertEquals(listOf(1L), longListOf(1L).asList())
        assertEquals(listOf(1L, 2L, 3L), longListOf(1L, 2L, 3L).asList())
        assertEquals(emptyList(), emptyLongList().asList())

        assertEquals(emptyList(), mutableLongListOf().asList())
        assertEquals(listOf(1L), mutableLongListOf(1L).asList())
        assertEquals(listOf(1L, 2L), mutableLongListOf(1L, 2L).asList())

        assertEquals(listOf(1L, 2L), buildLongList { add(1L); add(2L) }.asList())
        assertEquals(listOf(0L, 1L, 2L), LongList(3) { it.toLong() }.asList())
        assertEquals(listOf(0L, 1L, 2L), MutableLongList(3) { it.toLong() }.asList())
        assertEquals(listOf(1L, 2L, 3L), longArrayOf(1L, 2L, 3L).asLongList().asList())
    }

    @Test
    fun mutationRoundTrip() {
        val list = mutableLongListOf(2L)

        list.addFirst(1L)
        list.addLast(4L)
        list.add(2, 3L)
        assertEquals(listOf(1L, 2L, 3L, 4L), list.asList())

        assertEquals(2L, list.replace(1, 0L))
        list[1] = 2L
        assertEquals(1L, list.removeFirst())
        assertEquals(4L, list.removeLast())
        assertEquals(3L, list.removeAt(1))
        assertEquals(listOf(2L), list.asList())

        list.addAll(0, longListOf(0L, 1L))
        assertTrue(list.addAll(listOf(3L)))
        assertEquals(listOf(0L, 1L, 2L, 3L), list.asList())

        assertEquals(1, list.indexOf(1L))
        assertTrue(list.contains(3L))
        assertFalse(list.contains(4L))

        list.sortDescending()
        assertEquals(listOf(3L, 2L, 1L, 0L), list.asList())
        list.reverse()
        assertEquals(listOf(0L, 1L, 2L, 3L), list.asList())

        assertTrue(list.removeAll(longListOf(0L, 3L)))
        assertTrue(list.retainAll(listOf(1L)))
        assertEquals(listOf(1L), list.asList())

        assertEquals(listOf(1L), LongArray(1).also { list.copyInto(it) }.toList())

        list.clear()
        assertTrue(list.isEmpty())
    }

    @Test
    fun iterationAndEquality() {
        val list = mutableLongListOf(1L, 2L, 3L)
        val expected = listOf(1L, 2L, 3L)

        val fromIterator = mutableListOf<Long>()
        val iterator = list.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextLong())
        assertEquals(expected, fromIterator)

        val fromForeach = mutableListOf<Long>()
        list.traverse { fromForeach.add(it) }
        assertEquals(expected, fromForeach)

        val fromTraverser = mutableListOf<Long>()
        val traverser = list.traverser(0)
        while (traverser.forward()) fromTraverser.add(traverser.value)
        assertEquals(expected, fromTraverser)
        while (traverser.backward()) { }
        traverser.forward()
        traverser.set(0L)
        assertEquals(listOf(0L, 2L, 3L), list.asList())

        val fromReverse = mutableListOf<Long>()
        list.traverseReverse { fromReverse.add(it) }
        assertEquals(list.asList().reversed(), fromReverse)

        // equality holds against a different LongList implementation, not just another deque
        val arrayBacked = longArrayOf(0L, 2L, 3L).asLongList()
        assertEquals(arrayBacked, list)
        assertEquals(list, arrayBacked)
        assertEquals(arrayBacked.hashCode(), list.hashCode())
        assertNotEquals(longListOf(0L, 2L), list)
    }
}

class FloatListConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyList(), floatListOf().asList())
        assertEquals(listOf(1f), floatListOf(1f).asList())
        assertEquals(listOf(1f, 2f, 3f), floatListOf(1f, 2f, 3f).asList())
        assertEquals(emptyList(), emptyFloatList().asList())

        assertEquals(emptyList(), mutableFloatListOf().asList())
        assertEquals(listOf(1f), mutableFloatListOf(1f).asList())
        assertEquals(listOf(1f, 2f), mutableFloatListOf(1f, 2f).asList())

        assertEquals(listOf(1f, 2f), buildFloatList { add(1f); add(2f) }.asList())
        assertEquals(listOf(0f, 1f, 2f), FloatList(3) { it.toFloat() }.asList())
        assertEquals(listOf(0f, 1f, 2f), MutableFloatList(3) { it.toFloat() }.asList())
        assertEquals(listOf(1f, 2f, 3f), floatArrayOf(1f, 2f, 3f).asFloatList().asList())
    }

    @Test
    fun mutationRoundTrip() {
        val list = mutableFloatListOf(2f)

        list.addFirst(1f)
        list.addLast(4f)
        list.add(2, 3f)
        assertEquals(listOf(1f, 2f, 3f, 4f), list.asList())

        assertEquals(2f, list.replace(1, 0f))
        list[1] = 2f
        assertEquals(1f, list.removeFirst())
        assertEquals(4f, list.removeLast())
        assertEquals(3f, list.removeAt(1))
        assertEquals(listOf(2f), list.asList())

        list.addAll(0, floatListOf(0f, 1f))
        assertTrue(list.addAll(listOf(3f)))
        assertEquals(listOf(0f, 1f, 2f, 3f), list.asList())

        assertEquals(1, list.indexOf(1f))
        assertTrue(list.contains(3f))
        assertFalse(list.contains(4f))

        list.sortDescending()
        assertEquals(listOf(3f, 2f, 1f, 0f), list.asList())
        list.reverse()
        assertEquals(listOf(0f, 1f, 2f, 3f), list.asList())

        assertTrue(list.removeAll(floatListOf(0f, 3f)))
        assertTrue(list.retainAll(listOf(1f)))
        assertEquals(listOf(1f), list.asList())

        assertEquals(listOf(1f), FloatArray(1).also { list.copyInto(it) }.toList())

        list.clear()
        assertTrue(list.isEmpty())
    }

    @Test
    fun iterationAndEquality() {
        val list = mutableFloatListOf(1f, 2f, 3f)
        val expected = listOf(1f, 2f, 3f)

        val fromIterator = mutableListOf<Float>()
        val iterator = list.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextFloat())
        assertEquals(expected, fromIterator)

        val fromForeach = mutableListOf<Float>()
        list.traverse { fromForeach.add(it) }
        assertEquals(expected, fromForeach)

        val fromTraverser = mutableListOf<Float>()
        val traverser = list.traverser(0)
        while (traverser.forward()) fromTraverser.add(traverser.value)
        assertEquals(expected, fromTraverser)
        while (traverser.backward()) { }
        traverser.forward()
        traverser.set(0f)
        assertEquals(listOf(0f, 2f, 3f), list.asList())

        val fromReverse = mutableListOf<Float>()
        list.traverseReverse { fromReverse.add(it) }
        assertEquals(list.asList().reversed(), fromReverse)

        // equality holds against a different FloatList implementation, not just another deque
        val arrayBacked = floatArrayOf(0f, 2f, 3f).asFloatList()
        assertEquals(arrayBacked, list)
        assertEquals(list, arrayBacked)
        assertEquals(arrayBacked.hashCode(), list.hashCode())
        assertNotEquals(floatListOf(0f, 2f), list)
    }
}

class DoubleListConformanceTest {

    @Test
    fun factories_produceExpectedContents() {
        assertEquals(emptyList(), doubleListOf().asList())
        assertEquals(listOf(1.0), doubleListOf(1.0).asList())
        assertEquals(listOf(1.0, 2.0, 3.0), doubleListOf(1.0, 2.0, 3.0).asList())
        assertEquals(emptyList(), emptyDoubleList().asList())

        assertEquals(emptyList(), mutableDoubleListOf().asList())
        assertEquals(listOf(1.0), mutableDoubleListOf(1.0).asList())
        assertEquals(listOf(1.0, 2.0), mutableDoubleListOf(1.0, 2.0).asList())

        assertEquals(listOf(1.0, 2.0), buildDoubleList { add(1.0); add(2.0) }.asList())
        assertEquals(listOf(0.0, 1.0, 2.0), DoubleList(3) { it.toDouble() }.asList())
        assertEquals(listOf(0.0, 1.0, 2.0), MutableDoubleList(3) { it.toDouble() }.asList())
        assertEquals(listOf(1.0, 2.0, 3.0), doubleArrayOf(1.0, 2.0, 3.0).asDoubleList().asList())
    }

    @Test
    fun mutationRoundTrip() {
        val list = mutableDoubleListOf(2.0)

        list.addFirst(1.0)
        list.addLast(4.0)
        list.add(2, 3.0)
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0), list.asList())

        assertEquals(2.0, list.replace(1, 0.0))
        list[1] = 2.0
        assertEquals(1.0, list.removeFirst())
        assertEquals(4.0, list.removeLast())
        assertEquals(3.0, list.removeAt(1))
        assertEquals(listOf(2.0), list.asList())

        list.addAll(0, doubleListOf(0.0, 1.0))
        assertTrue(list.addAll(listOf(3.0)))
        assertEquals(listOf(0.0, 1.0, 2.0, 3.0), list.asList())

        assertEquals(1, list.indexOf(1.0))
        assertTrue(list.contains(3.0))
        assertFalse(list.contains(4.0))

        list.sortDescending()
        assertEquals(listOf(3.0, 2.0, 1.0, 0.0), list.asList())
        list.reverse()
        assertEquals(listOf(0.0, 1.0, 2.0, 3.0), list.asList())

        assertTrue(list.removeAll(doubleListOf(0.0, 3.0)))
        assertTrue(list.retainAll(listOf(1.0)))
        assertEquals(listOf(1.0), list.asList())

        assertEquals(listOf(1.0), DoubleArray(1).also { list.copyInto(it) }.toList())

        list.clear()
        assertTrue(list.isEmpty())
    }

    @Test
    fun iterationAndEquality() {
        val list = mutableDoubleListOf(1.0, 2.0, 3.0)
        val expected = listOf(1.0, 2.0, 3.0)

        val fromIterator = mutableListOf<Double>()
        val iterator = list.iterator()
        while (iterator.hasNext()) fromIterator.add(iterator.nextDouble())
        assertEquals(expected, fromIterator)

        val fromForeach = mutableListOf<Double>()
        list.traverse { fromForeach.add(it) }
        assertEquals(expected, fromForeach)

        val fromTraverser = mutableListOf<Double>()
        val traverser = list.traverser(0)
        while (traverser.forward()) fromTraverser.add(traverser.value)
        assertEquals(expected, fromTraverser)
        while (traverser.backward()) { }
        traverser.forward()
        traverser.set(0.0)
        assertEquals(listOf(0.0, 2.0, 3.0), list.asList())

        val fromReverse = mutableListOf<Double>()
        list.traverseReverse { fromReverse.add(it) }
        assertEquals(list.asList().reversed(), fromReverse)

        // equality holds against a different DoubleList implementation, not just another deque
        val arrayBacked = doubleArrayOf(0.0, 2.0, 3.0).asDoubleList()
        assertEquals(arrayBacked, list)
        assertEquals(list, arrayBacked)
        assertEquals(arrayBacked.hashCode(), list.hashCode())
        assertNotEquals(doubleListOf(0.0, 2.0), list)
    }
}
