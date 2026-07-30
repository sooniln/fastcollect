package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.bytes.*
import io.github.sooniln.fastcollect.doubles.*
import io.github.sooniln.fastcollect.floats.*
import io.github.sooniln.fastcollect.ints.*
import io.github.sooniln.fastcollect.longs.*
import io.github.sooniln.fastcollect.shorts.*
import kotlin.test.*

// Per-type helpers for element-wise comparison without relying on primitive list equals.
private fun ByteList.assertContents(vararg expected: Int) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v.toByte(), get(i), "element[$i]") }
}

private fun ShortList.assertContents(vararg expected: Int) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v.toShort(), get(i), "element[$i]") }
}

private fun IntList.assertContents(vararg expected: Int) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v, get(i), "element[$i]") }
}

private fun LongList.assertContents(vararg expected: Long) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v, get(i), "element[$i]") }
}

private fun FloatList.assertContents(vararg expected: Float) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v, get(i), "element[$i]") }
}

private fun DoubleList.assertContents(vararg expected: Double) {
    assertEquals(expected.size, size, "size")
    expected.forEachIndexed { i, v -> assertEquals(v, get(i), "element[$i]") }
}

// ============================= Byte =============================

class ByteListTest {
    @Test
    fun listOf_vararg() {
        byteListOf(1, 2, 3).assertContents(1, 2, 3)
    }

    @Test
    fun mutableListOf_vararg() {
        mutableByteListOf(4, 5, 6).assertContents(4, 5, 6)
    }

    @Test
    fun buildList_dsl() {
        buildByteList { add(10); add(20) }.assertContents(10, 20)
    }

    @Test
    fun listConstructor_initFn() {
        ByteList(4) { (it * 3).toByte() }.assertContents(0, 3, 6, 9)
    }

    @Test
    fun mutableListConstructor_initFn() {
        MutableByteList(3) { (it + 10).toByte() }.assertContents(10, 11, 12)
    }

    @Test
    fun asXxxList_fromArray() {
        byteArrayOf(1, 2, 3).asByteList().assertContents(1, 2, 3)
    }

    @Test
    fun addFirst_prependsElement() {
        val list = mutableByteListOf(2, 3)
        list.addFirst(1)
        list.assertContents(1, 2, 3)
    }

    @Test
    fun addLast_appendsElement() {
        val list = mutableByteListOf(1, 2)
        list.addLast(3)
        list.assertContents(1, 2, 3)
    }

    @Test
    fun removeFirst_returnsAndRemoves() {
        val list = mutableByteListOf(10, 20, 30)
        assertEquals(10.toByte(), list.removeFirst())
        list.assertContents(20, 30)
    }

    @Test
    fun removeLast_returnsAndRemoves() {
        val list = mutableByteListOf(10, 20, 30)
        assertEquals(30.toByte(), list.removeLast())
        list.assertContents(10, 20)
    }

    @Test
    fun removeFirst_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableByteListOf().removeFirst() }
    }

    @Test
    fun removeLast_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableByteListOf().removeLast() }
    }

    @Test
    fun replace_returnsOldValue() {
        val list = mutableByteListOf(1, 2, 3)
        assertEquals(2.toByte(), list.replace(1, 99))
        list.assertContents(1, 99, 3)
    }

    @Test
    fun removeRange_removesSlice() {
        val list = mutableByteListOf(1, 2, 3, 4, 5)
        list.removeRange(1, 4)
        list.assertContents(1, 5)
    }

    @Test
    fun subList_removeRange_updatesSubList() {
        val list = mutableByteListOf(1, 2, 3, 4, 5)
        val sub = list.subList(1, 4)
        sub.removeRange(0, 2)
        sub.assertContents(4)
        list.assertContents(1, 4, 5)
    }

    @Test
    fun removeAll_atExactCapacity_removesMatching() {
        // a deque built from vararg elements exactly fills its backing ring buffer
        val list = mutableByteListOf(1, 2, 3, 4)
        assertTrue(list.removeAll(listOf(2.toByte(), 4.toByte())))
        list.assertContents(1, 3)
    }

    @Test
    fun addAll_atIndex_withPrimitiveCollection() {
        val list = mutableByteListOf(1, 4)
        list.addAll(1, byteListOf(2, 3))
        list.assertContents(1, 2, 3, 4)
    }

    @Test
    fun addAll_atIndex_withStandardCollection() {
        val list = mutableByteListOf(1, 4)
        list.addAll(1, listOf(2.toByte(), 3.toByte()))
        list.assertContents(1, 2, 3, 4)
    }

    @Test
    fun sort_ascendingOrder() {
        val list = mutableByteListOf(-3, 1, 4, -1, 5)
        list.sort()
        list.assertContents(-3, -1, 1, 4, 5)
    }

    @Test
    fun sortDescending_descendingOrder() {
        val list = mutableByteListOf(-3, 1, 4, -1, 5)
        list.sortDescending()
        list.assertContents(5, 4, 1, -1, -3)
    }

    @Test
    fun sort_emptyList() {
        val list = mutableByteListOf()
        list.sort()
        assertTrue(list.isEmpty())
    }

    @Test
    fun sort_singleElement() {
        val list = mutableByteListOf(42)
        list.sort()
        list.assertContents(42)
    }

    @Test
    fun fill_setsAllElements() {
        val list = mutableByteListOf(1, 2, 3, 4)
        list.fill(7)
        list.assertContents(7, 7, 7, 7)
    }

    @Test
    fun reverse_reversesOrder() {
        val list = mutableByteListOf(1, 2, 3, 4)
        list.reverse()
        list.assertContents(4, 3, 2, 1)
    }

    @Test
    fun reverse_emptyList() {
        val list = mutableByteListOf()
        list.reverse()
        assertTrue(list.isEmpty())
    }

    @Test
    fun shuffle_preservesElementsAndSize() {
        val list = mutableByteListOf(1, 2, 3, 4, 5)
        val before = list.toList().sorted()
        list.shuffle()
        assertEquals(5, list.size)
        assertEquals(before, list.toList().sorted())
    }

    @Test
    fun foldRight_correctResult() {
        val result = byteListOf(1, 2, 3).foldRight("") { element, acc -> "$element$acc" }
        assertEquals("123", result)
    }

    @Test
    fun reduceRight_correctResult() {
        // accumulated starts at 4, op(4,2)=6, op(6,1)=7
        val result = byteListOf(1, 2, 4).reduceRight { acc, element -> (acc + element).toByte() }
        assertEquals(7.toByte(), result)
    }

    @Test
    fun lastIndex_nonEmpty() {
        assertEquals(2, byteListOf(1, 2, 3).lastIndex)
    }

    @Test
    fun lastIndex_empty_isMinusOne() {
        assertEquals(-1, byteListOf().lastIndex)
    }

    @Test
    fun foreach_matchesIteratorInOrder() {
        val list = mutableByteListOf()
        for (i in 1..50) list.add(i.toByte())

        val fromIterator = mutableListOf<Byte>()
        val it = list.iterator()
        while (it.hasNext()) fromIterator.add(it.nextByte())

        val fromForeach = mutableListOf<Byte>()
        list.foreach { v -> fromForeach.add(v) }

        assertEquals(fromIterator, fromForeach)
    }

    @Test
    fun equals_matchesAnyByteListImplementation() {
        // ByteList contract (Abstract*List.equals()) requires equality against ANY ByteList
        // implementation, not just the same concrete class.
        val deque: Any = mutableByteListOf(1, 2, 3)
        val arrayBacked = byteArrayOf(1, 2, 3).asByteList()
        assertEquals(arrayBacked, deque)
    }
}

// ============================= Short =============================

class ShortListTest {
    @Test
    fun listOf_vararg() {
        shortListOf(1, 2, 3).assertContents(1, 2, 3)
    }

    @Test
    fun mutableListOf_vararg() {
        mutableShortListOf(4, 5, 6).assertContents(4, 5, 6)
    }

    @Test
    fun buildList_dsl() {
        buildShortList { add(10); add(20) }.assertContents(10, 20)
    }

    @Test
    fun listConstructor_initFn() {
        ShortList(4) { (it * 3).toShort() }.assertContents(0, 3, 6, 9)
    }

    @Test
    fun mutableListConstructor_initFn() {
        MutableShortList(3) { (it + 10).toShort() }.assertContents(10, 11, 12)
    }

    @Test
    fun asXxxList_fromArray() {
        shortArrayOf(1, 2, 3).asShortList().assertContents(1, 2, 3)
    }

    @Test
    fun addFirst_prependsElement() {
        val list = mutableShortListOf(2, 3)
        list.addFirst(1)
        list.assertContents(1, 2, 3)
    }

    @Test
    fun addLast_appendsElement() {
        val list = mutableShortListOf(1, 2)
        list.addLast(3)
        list.assertContents(1, 2, 3)
    }

    @Test
    fun removeFirst_returnsAndRemoves() {
        val list = mutableShortListOf(10, 20, 30)
        assertEquals(10.toShort(), list.removeFirst())
        list.assertContents(20, 30)
    }

    @Test
    fun removeLast_returnsAndRemoves() {
        val list = mutableShortListOf(10, 20, 30)
        assertEquals(30.toShort(), list.removeLast())
        list.assertContents(10, 20)
    }

    @Test
    fun removeFirst_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableShortListOf().removeFirst() }
    }

    @Test
    fun removeLast_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableShortListOf().removeLast() }
    }

    @Test
    fun replace_returnsOldValue() {
        val list = mutableShortListOf(1, 2, 3)
        assertEquals(2.toShort(), list.replace(1, 99))
        list.assertContents(1, 99, 3)
    }

    @Test
    fun removeRange_removesSlice() {
        val list = mutableShortListOf(1, 2, 3, 4, 5)
        list.removeRange(1, 4)
        list.assertContents(1, 5)
    }

    @Test
    fun subList_removeRange_updatesSubList() {
        val list = mutableShortListOf(1, 2, 3, 4, 5)
        val sub = list.subList(1, 4)
        sub.removeRange(0, 2)
        sub.assertContents(4)
        list.assertContents(1, 4, 5)
    }

    @Test
    fun removeAll_atExactCapacity_removesMatching() {
        // a deque built from vararg elements exactly fills its backing ring buffer
        val list = mutableShortListOf(1, 2, 3, 4)
        assertTrue(list.removeAll(listOf(2.toShort(), 4.toShort())))
        list.assertContents(1, 3)
    }

    @Test
    fun addAll_atIndex_withPrimitiveCollection() {
        val list = mutableShortListOf(1, 4)
        list.addAll(1, shortListOf(2, 3))
        list.assertContents(1, 2, 3, 4)
    }

    @Test
    fun addAll_atIndex_withStandardCollection() {
        val list = mutableShortListOf(1, 4)
        list.addAll(1, listOf(2.toShort(), 3.toShort()))
        list.assertContents(1, 2, 3, 4)
    }

    @Test
    fun sort_ascendingOrder() {
        val list = mutableShortListOf(-3, 1, 4, -1, 5)
        list.sort()
        list.assertContents(-3, -1, 1, 4, 5)
    }

    @Test
    fun sortDescending_descendingOrder() {
        val list = mutableShortListOf(-3, 1, 4, -1, 5)
        list.sortDescending()
        list.assertContents(5, 4, 1, -1, -3)
    }

    @Test
    fun sort_emptyList() {
        val list = mutableShortListOf()
        list.sort()
        assertTrue(list.isEmpty())
    }

    @Test
    fun sort_singleElement() {
        val list = mutableShortListOf(42)
        list.sort()
        list.assertContents(42)
    }

    @Test
    fun fill_setsAllElements() {
        val list = mutableShortListOf(1, 2, 3, 4)
        list.fill(7)
        list.assertContents(7, 7, 7, 7)
    }

    @Test
    fun reverse_reversesOrder() {
        val list = mutableShortListOf(1, 2, 3, 4)
        list.reverse()
        list.assertContents(4, 3, 2, 1)
    }

    @Test
    fun reverse_emptyList() {
        val list = mutableShortListOf()
        list.reverse()
        assertTrue(list.isEmpty())
    }

    @Test
    fun shuffle_preservesElementsAndSize() {
        val list = mutableShortListOf(1, 2, 3, 4, 5)
        val before = list.toList().sorted()
        list.shuffle()
        assertEquals(5, list.size)
        assertEquals(before, list.toList().sorted())
    }

    @Test
    fun foldRight_correctResult() {
        val result = shortListOf(1, 2, 3).foldRight("") { element, acc -> "$element$acc" }
        assertEquals("123", result)
    }

    @Test
    fun reduceRight_correctResult() {
        // accumulated starts at 4, op(4,2)=6, op(6,1)=7
        val result = shortListOf(1, 2, 4).reduceRight { acc, element -> (acc + element).toShort() }
        assertEquals(7.toShort(), result)
    }

    @Test
    fun lastIndex_nonEmpty() {
        assertEquals(2, shortListOf(1, 2, 3).lastIndex)
    }

    @Test
    fun lastIndex_empty_isMinusOne() {
        assertEquals(-1, shortListOf().lastIndex)
    }

    @Test
    fun foreach_matchesIteratorInOrder() {
        val list = mutableShortListOf()
        for (i in 1..50) list.add(i.toShort())

        val fromIterator = mutableListOf<Short>()
        val it = list.iterator()
        while (it.hasNext()) fromIterator.add(it.nextShort())

        val fromForeach = mutableListOf<Short>()
        list.foreach { v -> fromForeach.add(v) }

        assertEquals(fromIterator, fromForeach)
    }

    @Test
    fun equals_matchesAnyShortListImplementation() {
        // ShortList contract (Abstract*List.equals()) requires equality against ANY ShortList
        // implementation, not just the same concrete class.
        val deque: Any = mutableShortListOf(1, 2, 3)
        val arrayBacked = shortArrayOf(1, 2, 3).asShortList()
        assertEquals(arrayBacked, deque)
    }
}

// ============================= Int =============================

class IntListTest {
    @Test
    fun listOf_vararg() {
        intListOf(1, 2, 3).assertContents(1, 2, 3)
    }

    @Test
    fun mutableListOf_vararg() {
        mutableIntListOf(4, 5, 6).assertContents(4, 5, 6)
    }

    @Test
    fun buildList_dsl() {
        buildIntList { add(10); add(20) }.assertContents(10, 20)
    }

    @Test
    fun listConstructor_initFn() {
        IntList(4) { it * 3 }.assertContents(0, 3, 6, 9)
    }

    @Test
    fun mutableListConstructor_initFn() {
        MutableIntList(3) { it + 10 }.assertContents(10, 11, 12)
    }

    @Test
    fun asXxxList_fromArray() {
        intArrayOf(1, 2, 3).asIntList().assertContents(1, 2, 3)
    }

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

    @Test
    fun replace_returnsOldValue() {
        val list = mutableIntListOf(1, 2, 3)
        assertEquals(2, list.replace(1, 99))
        list.assertContents(1, 99, 3)
    }

    @Test
    fun removeRange_removesSlice() {
        val list = mutableIntListOf(1, 2, 3, 4, 5)
        list.removeRange(1, 4)
        list.assertContents(1, 5)
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
    fun removeAll_atExactCapacity_removesMatching() {
        // a deque built from vararg elements exactly fills its backing ring buffer
        val list = mutableIntListOf(1, 2, 3, 4)
        assertTrue(list.removeAll(listOf(2, 4)))
        list.assertContents(1, 3)
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
    fun sort_emptyList() {
        val list = mutableIntListOf()
        list.sort()
        assertTrue(list.isEmpty())
    }

    @Test
    fun sort_singleElement() {
        val list = mutableIntListOf(42)
        list.sort()
        list.assertContents(42)
    }

    @Test
    fun fill_setsAllElements() {
        val list = mutableIntListOf(1, 2, 3, 4)
        list.fill(7)
        list.assertContents(7, 7, 7, 7)
    }

    @Test
    fun reverse_reversesOrder() {
        val list = mutableIntListOf(1, 2, 3, 4)
        list.reverse()
        list.assertContents(4, 3, 2, 1)
    }

    @Test
    fun reverse_emptyList() {
        val list = mutableIntListOf()
        list.reverse()
        assertTrue(list.isEmpty())
    }

    @Test
    fun shuffle_preservesElementsAndSize() {
        val list = mutableIntListOf(1, 2, 3, 4, 5)
        val before = list.toList().sorted()
        list.shuffle()
        assertEquals(5, list.size)
        assertEquals(before, list.toList().sorted())
    }

    @Test
    fun foldRight_correctResult() {
        val result = intListOf(1, 2, 3).foldRight("") { element, acc -> "$element$acc" }
        assertEquals("123", result)
    }

    @Test
    fun reduceRight_correctResult() {
        // accumulated starts at 4, op(4,2)=6, op(6,1)=7
        val result = intListOf(1, 2, 4).reduceRight { acc, element -> acc + element }
        assertEquals(7, result)
    }

    @Test
    fun lastIndex_nonEmpty() {
        assertEquals(2, intListOf(1, 2, 3).lastIndex)
    }

    @Test
    fun lastIndex_empty_isMinusOne() {
        assertEquals(-1, intListOf().lastIndex)
    }

    @Test
    fun foreach_matchesIteratorInOrder() {
        val list = mutableIntListOf()
        for (i in 1..50) list.add(i)

        val fromIterator = mutableListOf<Int>()
        val it = list.iterator()
        while (it.hasNext()) fromIterator.add(it.nextInt())

        val fromForeach = mutableListOf<Int>()
        list.foreach { v -> fromForeach.add(v) }

        assertEquals(fromIterator, fromForeach)
    }

    @Test
    fun equals_matchesAnyIntListImplementation() {
        // IntList contract (Abstract*List.equals()) requires equality against ANY IntList
        // implementation, not just the same concrete class.
        val deque: Any = mutableIntListOf(1, 2, 3)
        val arrayBacked = intArrayOf(1, 2, 3).asIntList()
        assertEquals(arrayBacked, deque)
    }
}

// ============================= Long =============================

class LongListTest {
    @Test
    fun listOf_vararg() {
        longListOf(1L, 2L, 3L).assertContents(1L, 2L, 3L)
    }

    @Test
    fun mutableListOf_vararg() {
        mutableLongListOf(4L, 5L, 6L).assertContents(4L, 5L, 6L)
    }

    @Test
    fun buildList_dsl() {
        buildLongList { add(10L); add(20L) }.assertContents(10L, 20L)
    }

    @Test
    fun listConstructor_initFn() {
        LongList(4) { it.toLong() * 3 }.assertContents(0L, 3L, 6L, 9L)
    }

    @Test
    fun mutableListConstructor_initFn() {
        MutableLongList(3) { it.toLong() + 10 }.assertContents(10L, 11L, 12L)
    }

    @Test
    fun asXxxList_fromArray() {
        longArrayOf(1L, 2L, 3L).asLongList().assertContents(1L, 2L, 3L)
    }

    @Test
    fun addFirst_prependsElement() {
        val list = mutableLongListOf(2L, 3L)
        list.addFirst(1L)
        list.assertContents(1L, 2L, 3L)
    }

    @Test
    fun addLast_appendsElement() {
        val list = mutableLongListOf(1L, 2L)
        list.addLast(3L)
        list.assertContents(1L, 2L, 3L)
    }

    @Test
    fun removeFirst_returnsAndRemoves() {
        val list = mutableLongListOf(10L, 20L, 30L)
        assertEquals(10L, list.removeFirst())
        list.assertContents(20L, 30L)
    }

    @Test
    fun removeLast_returnsAndRemoves() {
        val list = mutableLongListOf(10L, 20L, 30L)
        assertEquals(30L, list.removeLast())
        list.assertContents(10L, 20L)
    }

    @Test
    fun removeFirst_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableLongListOf().removeFirst() }
    }

    @Test
    fun removeLast_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableLongListOf().removeLast() }
    }

    @Test
    fun replace_returnsOldValue() {
        val list = mutableLongListOf(1L, 2L, 3L)
        assertEquals(2L, list.replace(1, 99L))
        list.assertContents(1L, 99L, 3L)
    }

    @Test
    fun removeRange_removesSlice() {
        val list = mutableLongListOf(1L, 2L, 3L, 4L, 5L)
        list.removeRange(1, 4)
        list.assertContents(1L, 5L)
    }

    @Test
    fun subList_removeRange_updatesSubList() {
        val list = mutableLongListOf(1L, 2L, 3L, 4L, 5L)
        val sub = list.subList(1, 4)
        sub.removeRange(0, 2)
        sub.assertContents(4L)
        list.assertContents(1L, 4L, 5L)
    }

    @Test
    fun removeAll_atExactCapacity_removesMatching() {
        // a deque built from vararg elements exactly fills its backing ring buffer
        val list = mutableLongListOf(1L, 2L, 3L, 4L)
        assertTrue(list.removeAll(listOf(2L, 4L)))
        list.assertContents(1L, 3L)
    }

    @Test
    fun addAll_atIndex_withPrimitiveCollection() {
        val list = mutableLongListOf(1L, 4L)
        list.addAll(1, longListOf(2L, 3L))
        list.assertContents(1L, 2L, 3L, 4L)
    }

    @Test
    fun addAll_atIndex_withStandardCollection() {
        val list = mutableLongListOf(1L, 4L)
        list.addAll(1, listOf(2L, 3L))
        list.assertContents(1L, 2L, 3L, 4L)
    }

    @Test
    fun sort_ascendingOrder() {
        val list = mutableLongListOf(-3L, 1L, 4L, -1L, 5L)
        list.sort()
        list.assertContents(-3L, -1L, 1L, 4L, 5L)
    }

    @Test
    fun sortDescending_descendingOrder() {
        val list = mutableLongListOf(-3L, 1L, 4L, -1L, 5L)
        list.sortDescending()
        list.assertContents(5L, 4L, 1L, -1L, -3L)
    }

    @Test
    fun sort_emptyList() {
        val list = mutableLongListOf()
        list.sort()
        assertTrue(list.isEmpty())
    }

    @Test
    fun sort_singleElement() {
        val list = mutableLongListOf(42L)
        list.sort()
        list.assertContents(42L)
    }

    @Test
    fun fill_setsAllElements() {
        val list = mutableLongListOf(1L, 2L, 3L, 4L)
        list.fill(7L)
        list.assertContents(7L, 7L, 7L, 7L)
    }

    @Test
    fun reverse_reversesOrder() {
        val list = mutableLongListOf(1L, 2L, 3L, 4L)
        list.reverse()
        list.assertContents(4L, 3L, 2L, 1L)
    }

    @Test
    fun reverse_emptyList() {
        val list = mutableLongListOf()
        list.reverse()
        assertTrue(list.isEmpty())
    }

    @Test
    fun shuffle_preservesElementsAndSize() {
        val list = mutableLongListOf(1L, 2L, 3L, 4L, 5L)
        val before = list.toList().sorted()
        list.shuffle()
        assertEquals(5, list.size)
        assertEquals(before, list.toList().sorted())
    }

    @Test
    fun foldRight_correctResult() {
        val result = longListOf(1L, 2L, 3L).foldRight("") { element, acc -> "$element$acc" }
        assertEquals("123", result)
    }

    @Test
    fun reduceRight_correctResult() {
        // accumulated starts at 4, op(4,2)=6, op(6,1)=7
        val result = longListOf(1L, 2L, 4L).reduceRight { acc, element -> acc + element }
        assertEquals(7L, result)
    }

    @Test
    fun lastIndex_nonEmpty() {
        assertEquals(2, longListOf(1L, 2L, 3L).lastIndex)
    }

    @Test
    fun lastIndex_empty_isMinusOne() {
        assertEquals(-1, longListOf().lastIndex)
    }

    @Test
    fun foreach_matchesIteratorInOrder() {
        val list = mutableLongListOf()
        for (i in 1L..50L) list.add(i)

        val fromIterator = mutableListOf<Long>()
        val it = list.iterator()
        while (it.hasNext()) fromIterator.add(it.nextLong())

        val fromForeach = mutableListOf<Long>()
        list.foreach { v -> fromForeach.add(v) }

        assertEquals(fromIterator, fromForeach)
    }

    @Test
    fun equals_matchesAnyLongListImplementation() {
        // LongList contract (Abstract*List.equals()) requires equality against ANY LongList
        // implementation, not just the same concrete class.
        val deque: Any = mutableLongListOf(1L, 2L, 3L)
        val arrayBacked = longArrayOf(1L, 2L, 3L).asLongList()
        assertEquals(arrayBacked, deque)
    }
}

// ============================= Float =============================

class FloatListTest {
    @Test
    fun listOf_vararg() {
        floatListOf(1f, 2f, 3f).assertContents(1f, 2f, 3f)
    }

    @Test
    fun mutableListOf_vararg() {
        mutableFloatListOf(4f, 5f, 6f).assertContents(4f, 5f, 6f)
    }

    @Test
    fun buildList_dsl() {
        buildFloatList { add(10f); add(20f) }.assertContents(10f, 20f)
    }

    @Test
    fun listConstructor_initFn() {
        FloatList(4) { it.toFloat() * 3 }.assertContents(0f, 3f, 6f, 9f)
    }

    @Test
    fun mutableListConstructor_initFn() {
        MutableFloatList(3) { it.toFloat() + 10 }.assertContents(10f, 11f, 12f)
    }

    @Test
    fun asXxxList_fromArray() {
        floatArrayOf(1f, 2f, 3f).asFloatList().assertContents(1f, 2f, 3f)
    }

    @Test
    fun addFirst_prependsElement() {
        val list = mutableFloatListOf(2f, 3f)
        list.addFirst(1f)
        list.assertContents(1f, 2f, 3f)
    }

    @Test
    fun addLast_appendsElement() {
        val list = mutableFloatListOf(1f, 2f)
        list.addLast(3f)
        list.assertContents(1f, 2f, 3f)
    }

    @Test
    fun removeFirst_returnsAndRemoves() {
        val list = mutableFloatListOf(10f, 20f, 30f)
        assertEquals(10f, list.removeFirst())
        list.assertContents(20f, 30f)
    }

    @Test
    fun removeLast_returnsAndRemoves() {
        val list = mutableFloatListOf(10f, 20f, 30f)
        assertEquals(30f, list.removeLast())
        list.assertContents(10f, 20f)
    }

    @Test
    fun removeFirst_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableFloatListOf().removeFirst() }
    }

    @Test
    fun removeLast_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableFloatListOf().removeLast() }
    }

    @Test
    fun replace_returnsOldValue() {
        val list = mutableFloatListOf(1f, 2f, 3f)
        assertEquals(2f, list.replace(1, 99f))
        list.assertContents(1f, 99f, 3f)
    }

    @Test
    fun removeRange_removesSlice() {
        val list = mutableFloatListOf(1f, 2f, 3f, 4f, 5f)
        list.removeRange(1, 4)
        list.assertContents(1f, 5f)
    }

    @Test
    fun subList_removeRange_updatesSubList() {
        val list = mutableFloatListOf(1f, 2f, 3f, 4f, 5f)
        val sub = list.subList(1, 4)
        sub.removeRange(0, 2)
        sub.assertContents(4f)
        list.assertContents(1f, 4f, 5f)
    }

    @Test
    fun removeAll_atExactCapacity_removesMatching() {
        // a deque built from vararg elements exactly fills its backing ring buffer
        val list = mutableFloatListOf(1f, 2f, 3f, 4f)
        assertTrue(list.removeAll(listOf(2f, 4f)))
        list.assertContents(1f, 3f)
    }

    @Test
    fun addAll_atIndex_withPrimitiveCollection() {
        val list = mutableFloatListOf(1f, 4f)
        list.addAll(1, floatListOf(2f, 3f))
        list.assertContents(1f, 2f, 3f, 4f)
    }

    @Test
    fun addAll_atIndex_withStandardCollection() {
        val list = mutableFloatListOf(1f, 4f)
        list.addAll(1, listOf(2f, 3f))
        list.assertContents(1f, 2f, 3f, 4f)
    }

    @Test
    fun sort_ascendingOrder() {
        // -0.0f sorts to the front of the raw-bits ordering and must be reversed into place with
        // the other negative values
        val list = mutableFloatListOf(-3f, 1f, 4f, -0.0f, -1f, 5f)
        list.sort()
        list.assertContents(-3f, -1f, -0.0f, 1f, 4f, 5f)
    }

    @Test
    fun sortDescending_descendingOrder() {
        val list = mutableFloatListOf(-3f, 1f, 4f, -0.0f, -1f, 5f)
        list.sortDescending()
        list.assertContents(5f, 4f, 1f, -0.0f, -1f, -3f)
    }

    @Test
    fun sort_emptyList() {
        val list = mutableFloatListOf()
        list.sort()
        assertTrue(list.isEmpty())
    }

    @Test
    fun sort_singleElement() {
        val list = mutableFloatListOf(42f)
        list.sort()
        list.assertContents(42f)
    }

    @Test
    fun fill_setsAllElements() {
        val list = mutableFloatListOf(1f, 2f, 3f, 4f)
        list.fill(7f)
        list.assertContents(7f, 7f, 7f, 7f)
    }

    @Test
    fun reverse_reversesOrder() {
        val list = mutableFloatListOf(1f, 2f, 3f, 4f)
        list.reverse()
        list.assertContents(4f, 3f, 2f, 1f)
    }

    @Test
    fun reverse_emptyList() {
        val list = mutableFloatListOf()
        list.reverse()
        assertTrue(list.isEmpty())
    }

    @Test
    fun shuffle_preservesElementsAndSize() {
        val list = mutableFloatListOf(1f, 2f, 3f, 4f, 5f)
        val before = list.toList().sortedBy { it }
        list.shuffle()
        assertEquals(5, list.size)
        assertEquals(before, list.toList().sortedBy { it })
    }

    @Test
    fun foldRight_correctResult() {
        val result = floatListOf(1f, 2f, 3f).foldRight(listOf<Float>()) { element, acc -> listOf(element) + acc }
        assertEquals(listOf(1f, 2f, 3f), result)
    }

    @Test
    fun reduceRight_correctResult() {
        // accumulated starts at 4f, op(4f,2f)=6f, op(6f,1f)=7f
        val result = floatListOf(1f, 2f, 4f).reduceRight { acc, element -> acc + element }
        assertEquals(7f, result)
    }

    @Test
    fun lastIndex_nonEmpty() {
        assertEquals(2, floatListOf(1f, 2f, 3f).lastIndex)
    }

    @Test
    fun lastIndex_empty_isMinusOne() {
        assertEquals(-1, floatListOf().lastIndex)
    }

    @Test
    fun indexOfAndContains_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must be found in all list implementations, and -0.0f must not match 0.0f
        assertTrue(floatListOf(Float.NaN).contains(Float.NaN))
        assertEquals(0, floatListOf(Float.NaN).indexOf(Float.NaN))
        assertEquals(0, floatListOf(Float.NaN).lastIndexOf(Float.NaN))

        val wrapped = floatArrayOf(1f, Float.NaN, Float.NaN).asFloatList()
        assertTrue(wrapped.contains(Float.NaN))
        assertEquals(1, wrapped.indexOf(Float.NaN))
        assertEquals(2, wrapped.lastIndexOf(Float.NaN))

        assertTrue(mutableFloatListOf(Float.NaN).contains(Float.NaN))

        assertFalse(floatListOf(-0.0f).contains(0.0f))
        assertEquals(-1, floatArrayOf(-0.0f).asFloatList().indexOf(0.0f))
        assertTrue(floatArrayOf(-0.0f).asFloatList().contains(-0.0f))
    }

    @Test
    fun equals_nanAndNegativeZero_matchBoxedSemantics() {
        assertEquals<Any>(floatArrayOf(1f, Float.NaN).asFloatList(), floatArrayOf(1f, Float.NaN).asFloatList())
        assertNotEquals<Any>(floatArrayOf(-0.0f).asFloatList(), floatArrayOf(0.0f).asFloatList())
    }

    @Test
    fun equals_matchesAnyFloatListImplementation() {
        // FloatList contract (Abstract*List.equals()) requires equality against ANY FloatList
        // implementation. InlineFloatArrayDeque is a @JvmInline value class and can't define a custom
        // equals(), so it falls back to the compiler-synthesized one, which only matches the same
        // wrapper class - this currently fails against a different FloatList implementation.
        val deque: Any = mutableFloatListOf(1f, 2f, 3f)
        val arrayBacked = floatArrayOf(1f, 2f, 3f).asFloatList()
        assertEquals(arrayBacked, deque)
    }

    @Test
    fun foreach_matchesIteratorInOrder() {
        val list = mutableFloatListOf()
        for (i in 1..50) list.add(i.toFloat())

        val fromIterator = mutableListOf<Int>()
        val it = list.iterator()
        while (it.hasNext()) fromIterator.add(it.nextFloat().toBits())

        val fromForeach = mutableListOf<Int>()
        list.foreach { v -> fromForeach.add(v.toBits()) }

        assertEquals(fromIterator, fromForeach)
    }
}

// ============================= Double =============================

class DoubleListTest {
    @Test
    fun listOf_vararg() {
        doubleListOf(1.0, 2.0, 3.0).assertContents(1.0, 2.0, 3.0)
    }

    @Test
    fun mutableListOf_vararg() {
        mutableDoubleListOf(4.0, 5.0, 6.0).assertContents(4.0, 5.0, 6.0)
    }

    @Test
    fun buildList_dsl() {
        buildDoubleList { add(10.0); add(20.0) }.assertContents(10.0, 20.0)
    }

    @Test
    fun listConstructor_initFn() {
        DoubleList(4) { it.toDouble() * 3 }.assertContents(0.0, 3.0, 6.0, 9.0)
    }

    @Test
    fun mutableListConstructor_initFn() {
        MutableDoubleList(3) { it.toDouble() + 10 }.assertContents(10.0, 11.0, 12.0)
    }

    @Test
    fun asXxxList_fromArray() {
        doubleArrayOf(1.0, 2.0, 3.0).asDoubleList().assertContents(1.0, 2.0, 3.0)
    }

    @Test
    fun addFirst_prependsElement() {
        val list = mutableDoubleListOf(2.0, 3.0)
        list.addFirst(1.0)
        list.assertContents(1.0, 2.0, 3.0)
    }

    @Test
    fun addLast_appendsElement() {
        val list = mutableDoubleListOf(1.0, 2.0)
        list.addLast(3.0)
        list.assertContents(1.0, 2.0, 3.0)
    }

    @Test
    fun removeFirst_returnsAndRemoves() {
        val list = mutableDoubleListOf(10.0, 20.0, 30.0)
        assertEquals(10.0, list.removeFirst())
        list.assertContents(20.0, 30.0)
    }

    @Test
    fun removeLast_returnsAndRemoves() {
        val list = mutableDoubleListOf(10.0, 20.0, 30.0)
        assertEquals(30.0, list.removeLast())
        list.assertContents(10.0, 20.0)
    }

    @Test
    fun removeFirst_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableDoubleListOf().removeFirst() }
    }

    @Test
    fun removeLast_emptyThrows() {
        assertFailsWith<NoSuchElementException> { mutableDoubleListOf().removeLast() }
    }

    @Test
    fun replace_returnsOldValue() {
        val list = mutableDoubleListOf(1.0, 2.0, 3.0)
        assertEquals(2.0, list.replace(1, 99.0))
        list.assertContents(1.0, 99.0, 3.0)
    }

    @Test
    fun removeRange_removesSlice() {
        val list = mutableDoubleListOf(1.0, 2.0, 3.0, 4.0, 5.0)
        list.removeRange(1, 4)
        list.assertContents(1.0, 5.0)
    }

    @Test
    fun subList_removeRange_updatesSubList() {
        val list = mutableDoubleListOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val sub = list.subList(1, 4)
        sub.removeRange(0, 2)
        sub.assertContents(4.0)
        list.assertContents(1.0, 4.0, 5.0)
    }

    @Test
    fun removeAll_atExactCapacity_removesMatching() {
        // a deque built from vararg elements exactly fills its backing ring buffer
        val list = mutableDoubleListOf(1.0, 2.0, 3.0, 4.0)
        assertTrue(list.removeAll(listOf(2.0, 4.0)))
        list.assertContents(1.0, 3.0)
    }

    @Test
    fun addAll_atIndex_withPrimitiveCollection() {
        val list = mutableDoubleListOf(1.0, 4.0)
        list.addAll(1, doubleListOf(2.0, 3.0))
        list.assertContents(1.0, 2.0, 3.0, 4.0)
    }

    @Test
    fun addAll_atIndex_withStandardCollection() {
        val list = mutableDoubleListOf(1.0, 4.0)
        list.addAll(1, listOf(2.0, 3.0))
        list.assertContents(1.0, 2.0, 3.0, 4.0)
    }

    @Test
    fun sort_ascendingOrder() {
        // -0.0 sorts to the front of the raw-bits ordering and must be reversed into place with
        // the other negative values
        val list = mutableDoubleListOf(-3.0, 1.0, 4.0, -0.0, -1.0, 5.0)
        list.sort()
        list.assertContents(-3.0, -1.0, -0.0, 1.0, 4.0, 5.0)
    }

    @Test
    fun sortDescending_descendingOrder() {
        val list = mutableDoubleListOf(-3.0, 1.0, 4.0, -0.0, -1.0, 5.0)
        list.sortDescending()
        list.assertContents(5.0, 4.0, 1.0, -0.0, -1.0, -3.0)
    }

    @Test
    fun sort_emptyList() {
        val list = mutableDoubleListOf()
        list.sort()
        assertTrue(list.isEmpty())
    }

    @Test
    fun sort_singleElement() {
        val list = mutableDoubleListOf(42.0)
        list.sort()
        list.assertContents(42.0)
    }

    @Test
    fun fill_setsAllElements() {
        val list = mutableDoubleListOf(1.0, 2.0, 3.0, 4.0)
        list.fill(7.0)
        list.assertContents(7.0, 7.0, 7.0, 7.0)
    }

    @Test
    fun reverse_reversesOrder() {
        val list = mutableDoubleListOf(1.0, 2.0, 3.0, 4.0)
        list.reverse()
        list.assertContents(4.0, 3.0, 2.0, 1.0)
    }

    @Test
    fun reverse_emptyList() {
        val list = mutableDoubleListOf()
        list.reverse()
        assertTrue(list.isEmpty())
    }

    @Test
    fun shuffle_preservesElementsAndSize() {
        val list = mutableDoubleListOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val before = list.toList().sortedBy { it }
        list.shuffle()
        assertEquals(5, list.size)
        assertEquals(before, list.toList().sortedBy { it })
    }

    @Test
    fun foldRight_correctResult() {
        val result = doubleListOf(1.0, 2.0, 3.0).foldRight(listOf<Double>()) { element, acc -> listOf(element) + acc }
        assertEquals(listOf(1.0, 2.0, 3.0), result)
    }

    @Test
    fun reduceRight_correctResult() {
        // accumulated starts at 4.0, op(4.0,2.0)=6.0, op(6.0,1.0)=7.0
        val result = doubleListOf(1.0, 2.0, 4.0).reduceRight { acc, element -> acc + element }
        assertEquals(7.0, result)
    }

    @Test
    fun lastIndex_nonEmpty() {
        assertEquals(2, doubleListOf(1.0, 2.0, 3.0).lastIndex)
    }

    @Test
    fun lastIndex_empty_isMinusOne() {
        assertEquals(-1, doubleListOf().lastIndex)
    }

    @Test
    fun indexOfAndContains_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must be found in all list implementations, and -0.0 must not match 0.0
        assertTrue(doubleListOf(Double.NaN).contains(Double.NaN))
        assertEquals(0, doubleListOf(Double.NaN).indexOf(Double.NaN))
        assertEquals(0, doubleListOf(Double.NaN).lastIndexOf(Double.NaN))

        val wrapped = doubleArrayOf(1.0, Double.NaN, Double.NaN).asDoubleList()
        assertTrue(wrapped.contains(Double.NaN))
        assertEquals(1, wrapped.indexOf(Double.NaN))
        assertEquals(2, wrapped.lastIndexOf(Double.NaN))

        assertTrue(mutableDoubleListOf(Double.NaN).contains(Double.NaN))

        assertFalse(doubleListOf(-0.0).contains(0.0))
        assertEquals(-1, doubleArrayOf(-0.0).asDoubleList().indexOf(0.0))
        assertTrue(doubleArrayOf(-0.0).asDoubleList().contains(-0.0))
    }

    @Test
    fun equals_nanAndNegativeZero_matchBoxedSemantics() {
        assertEquals<Any>(doubleArrayOf(1.0, Double.NaN).asDoubleList(), doubleArrayOf(1.0, Double.NaN).asDoubleList())
        assertNotEquals<Any>(doubleArrayOf(-0.0).asDoubleList(), doubleArrayOf(0.0).asDoubleList())
    }

    @Test
    fun equals_matchesAnyDoubleListImplementation() {
        // See FloatListTest.equals_matchesAnyFloatListImplementation - same value-class limitation.
        val deque: Any = mutableDoubleListOf(1.0, 2.0, 3.0)
        val arrayBacked = doubleArrayOf(1.0, 2.0, 3.0).asDoubleList()
        assertEquals(arrayBacked, deque)
    }

    @Test
    fun foreach_matchesIteratorInOrder() {
        val list = mutableDoubleListOf()
        for (i in 1..50) list.add(i.toDouble())

        val fromIterator = mutableListOf<Long>()
        val it = list.iterator()
        while (it.hasNext()) fromIterator.add(it.nextDouble().toBits())

        val fromForeach = mutableListOf<Long>()
        list.foreach { v -> fromForeach.add(v.toBits()) }

        assertEquals(fromIterator, fromForeach)
    }
}
