package io.github.sooniln.fastcollect

import kotlin.test.assertEquals

// Shared fixtures and assertions. Element-wise comparison deliberately avoids the primitive list equals()
// so that a broken equals() cannot mask a broken get().

internal fun ByteList.assertContents(vararg expected: Int) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v.toByte(), get(i), "element[$i]") }
}

internal fun IntList.assertContents(vararg expected: Int) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v, get(i), "element[$i]") }
}

// A MutableIntList that is NOT RandomAccess, so the traverser-based fallbacks in AbstractMutableIntList
// (sort/sortDescending/fill/reverse/subList/removeRange) are taken instead of the indexed fast paths.
internal class SequentialIntList(vararg elements: Int) : AbstractMutableIntList() {
    private val backing = IntArrayDeque(elements)

    override val size: Int get() = backing.size
    override fun get(index: Int): Int = backing[index]
    override fun set(index: Int, element: Int): Unit = backing.set(index, element)
    override fun add(index: Int, element: Int): Unit = backing.add(index, element)
    override fun removeAt(index: Int): Int = backing.removeAt(index)
}

internal fun AbstractBytePriorityQueue.drain(): List<Byte> {
    val drained = mutableListOf<Byte>()
    while (size > 0) drained.add(removeFirst())
    return drained
}

internal fun AbstractIntPriorityQueue.drain(): List<Int> {
    val drained = mutableListOf<Int>()
    while (size > 0) drained.add(removeFirst())
    return drained
}

internal fun AbstractLongPriorityQueue.drain(): List<Long> {
    val drained = mutableListOf<Long>()
    while (size > 0) drained.add(removeFirst())
    return drained
}

internal fun AbstractFloatPriorityQueue.drain(): List<Float> {
    val drained = mutableListOf<Float>()
    while (size > 0) drained.add(removeFirst())
    return drained
}

internal fun AbstractDoublePriorityQueue.drain(): List<Double> {
    val drained = mutableListOf<Double>()
    while (size > 0) drained.add(removeFirst())
    return drained
}

internal fun IntCollection.toBoxedList(): List<Int> {
    val iter = iterator()
    return buildList { while (iter.hasNext()) add(iter.nextInt()) }
}

internal fun LongCollection.toBoxedList(): List<Long> {
    val iter = iterator()
    return buildList { while (iter.hasNext()) add(iter.nextLong()) }
}
