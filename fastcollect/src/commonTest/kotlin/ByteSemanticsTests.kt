package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * What is actually specific to Byte: it is signed and only eight bits wide, its map default value is
 * Byte.MIN_VALUE rather than a floating-point sentinel, sum() widens to Int, and - unlike every other primitive -
 * there is no ByteSet/ByteHashSet expansion at all. Everything else Byte does is shared template logic covered
 * once against Int in the deep suites, plus ListConformanceTests/PriorityQueueConformanceTests.
 */
class ByteSemanticsTests {

    @Test
    fun byteList_sortsBySignedValue() {
        // an unsigned comparison would put -128 last rather than first
        val list = mutableByteListOf(1, Byte.MAX_VALUE, -1, Byte.MIN_VALUE, 0)
        list.sort()
        list.assertContents(-128, -1, 0, 1, 127)

        list.sortDescending()
        list.assertContents(127, 1, 0, -1, -128)
    }

    @Test
    fun bytePriorityQueue_ordersBySignedValue() {
        val queue = bytePriorityQueueOf(1, Byte.MAX_VALUE, -1, Byte.MIN_VALUE, 0)
        assertEquals(listOf<Byte>(-128, -1, 0, 1, 127), queue.drain())

        val descending = byteDescendingPriorityQueueOf(1, Byte.MAX_VALUE, -1, Byte.MIN_VALUE, 0)
        assertEquals(listOf<Byte>(127, 1, 0, -1, -128), descending.drain())
    }

    @Test
    fun byteSum_widensToInt() {
        // the running total must not be truncated to a Byte on the way
        val list = mutableByteListOf(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE)
        val total: Int = list.sum()
        assertEquals(381, total)

        assertEquals(-384, mutableByteListOf(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE).sum())
        assertEquals(0, mutableByteListOf().sum())
    }

    @Test
    fun byteValuedMap_defaultValueIsByteMinValue() {
        val map = mutableInt2ByteMapOf()
        assertEquals(Byte.MIN_VALUE, map[1])
        assertTrue(map.isDefaultValue(Byte.MIN_VALUE))
        assertFalse(map.isDefaultValue(0))
        assertFalse(map.isDefaultValue(Byte.MAX_VALUE))
    }

    @Test
    fun byteValuedMap_storedMinValue_keyStillReportedPresent() {
        val map = mutableInt2ByteMapOf()
        map[1] = Byte.MIN_VALUE

        assertTrue(map.containsKey(1))
        assertEquals(1, map.size)
        assertEquals(Byte.MIN_VALUE, map[1])
        assertTrue(map.containsValue(Byte.MIN_VALUE))

        var calls = 0
        assertEquals(Byte.MIN_VALUE, map.getOrElse(1) { calls++; 9 })
        assertEquals(0, calls)
        assertEquals(Byte.MIN_VALUE, map.removeKey(1))
        assertTrue(map.isEmpty())
    }

    @Test
    fun byteValuedMap_storesTheFullSignedRange() {
        val map = mutableLong2ByteMapOf()
        for (value in Byte.MIN_VALUE..Byte.MAX_VALUE) map[value.toLong()] = value.toByte()

        assertEquals(256, map.size)
        for (value in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            assertEquals(value.toByte(), map[value.toLong()], "lost entry $value")
        }
    }
}
