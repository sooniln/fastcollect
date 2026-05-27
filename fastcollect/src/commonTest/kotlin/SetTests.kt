package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.*
import io.github.sooniln.fastcollect.longs.*
import kotlin.test.*

// ============================= Int =============================

class IntSetTest {
    @Test
    fun setOf_vararg() {
        val set = intSetOf(1, 2, 3)
        assertEquals(3, set.size)
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    fun mutableSetOf_vararg() {
        val set = mutableIntSetOf(4, 5, 6)
        assertEquals(3, set.size)
        assertTrue(set.contains(4))
        assertTrue(set.contains(5))
        assertTrue(set.contains(6))
    }

    @Test
    fun buildSet_dsl() {
        val set = buildIntSet {
            add(10)
            add(20)
            add(30)
        }
        assertEquals(3, set.size)
        assertTrue(set.contains(10))
        assertTrue(set.contains(20))
        assertTrue(set.contains(30))
    }

    @Test
    fun union_combinesBothSets() {
        val a = intSetOf(1, 2, 3)
        val b = intSetOf(3, 4, 5)
        val result = a union b
        assertEquals(5, result.size)
        for (v in listOf(1, 2, 3, 4, 5)) assertTrue(result.contains(v), "$v missing from union")
    }

    @Test
    fun union_withEmptySet() {
        val a = intSetOf(1, 2, 3)
        val result = a union intSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1, 2, 3)) assertTrue(result.contains(v))
    }

    @Test
    fun intersect_returnsCommonElements() {
        val a = intSetOf(1, 2, 3, 4)
        val b = intSetOf(3, 4, 5, 6)
        val result = a intersect b
        assertEquals(2, result.size)
        assertTrue(result.contains(3))
        assertTrue(result.contains(4))
    }

    @Test
    fun intersect_noCommonElements_isEmpty() {
        val a = intSetOf(1, 2)
        val b = intSetOf(3, 4)
        assertTrue((a intersect b).isEmpty())
    }

    @Test
    fun subtract_removesRhsFromLhs() {
        val a = intSetOf(1, 2, 3, 4)
        val b = intSetOf(3, 4)
        val result = a subtract b
        assertEquals(2, result.size)
        assertTrue(result.contains(1))
        assertTrue(result.contains(2))
        assertFalse(result.contains(3))
        assertFalse(result.contains(4))
    }

    @Test
    fun subtract_removesAll_isEmpty() {
        val a = intSetOf(1, 2)
        val result = a subtract intSetOf(1, 2)
        assertTrue(result.isEmpty())
    }

    @Test
    fun subtract_emptyRhs_unchanged() {
        val a = intSetOf(1, 2, 3)
        val result = a subtract intSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1, 2, 3)) assertTrue(result.contains(v))
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val set = IntHashSet().apply { add(1); add(2); add(3) }
        set.ensureCapacity(100)
        assertEquals(3, set.size)
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val set = IntHashSet(100).apply { add(1); add(2); add(3) }
        set.trimToSize()
        assertEquals(3, set.size)
        assertTrue(set.contains(1))
        assertTrue(set.contains(2))
        assertTrue(set.contains(3))
    }
}

// ============================= Long =============================

class LongSetTest {
    @Test
    fun setOf_vararg() {
        val set = longSetOf(1L, 2L, 3L)
        assertEquals(3, set.size)
        assertTrue(set.contains(1L))
        assertTrue(set.contains(2L))
        assertTrue(set.contains(3L))
    }

    @Test
    fun mutableSetOf_vararg() {
        val set = mutableLongSetOf(4L, 5L, 6L)
        assertEquals(3, set.size)
        assertTrue(set.contains(4L))
        assertTrue(set.contains(5L))
        assertTrue(set.contains(6L))
    }

    @Test
    fun buildSet_dsl() {
        val set = buildLongSet {
            add(10L)
            add(20L)
            add(30L)
        }
        assertEquals(3, set.size)
        assertTrue(set.contains(10L))
        assertTrue(set.contains(20L))
        assertTrue(set.contains(30L))
    }

    @Test
    fun union_combinesBothSets() {
        val a = longSetOf(1L, 2L, 3L)
        val b = longSetOf(3L, 4L, 5L)
        val result = a union b
        assertEquals(5, result.size)
        for (v in listOf(1L, 2L, 3L, 4L, 5L)) assertTrue(result.contains(v), "$v missing from union")
    }

    @Test
    fun union_withEmptySet() {
        val a = longSetOf(1L, 2L, 3L)
        val result = a union longSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1L, 2L, 3L)) assertTrue(result.contains(v))
    }

    @Test
    fun intersect_returnsCommonElements() {
        val a = longSetOf(1L, 2L, 3L, 4L)
        val b = longSetOf(3L, 4L, 5L, 6L)
        val result = a intersect b
        assertEquals(2, result.size)
        assertTrue(result.contains(3L))
        assertTrue(result.contains(4L))
    }

    @Test
    fun intersect_noCommonElements_isEmpty() {
        val a = longSetOf(1L, 2L)
        val b = longSetOf(3L, 4L)
        assertTrue((a intersect b).isEmpty())
    }

    @Test
    fun subtract_removesRhsFromLhs() {
        val a = longSetOf(1L, 2L, 3L, 4L)
        val b = longSetOf(3L, 4L)
        val result = a subtract b
        assertEquals(2, result.size)
        assertTrue(result.contains(1L))
        assertTrue(result.contains(2L))
        assertFalse(result.contains(3L))
        assertFalse(result.contains(4L))
    }

    @Test
    fun subtract_removesAll_isEmpty() {
        val a = longSetOf(1L, 2L)
        val result = a subtract longSetOf(1L, 2L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun subtract_emptyRhs_unchanged() {
        val a = longSetOf(1L, 2L, 3L)
        val result = a subtract longSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1L, 2L, 3L)) assertTrue(result.contains(v))
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val set = LongHashSet().apply { add(1L); add(2L); add(3L) }
        set.ensureCapacity(100)
        assertEquals(3, set.size)
        assertTrue(set.contains(1L))
        assertTrue(set.contains(2L))
        assertTrue(set.contains(3L))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val set = LongHashSet(100).apply { add(1L); add(2L); add(3L) }
        set.trimToSize()
        assertEquals(3, set.size)
        assertTrue(set.contains(1L))
        assertTrue(set.contains(2L))
        assertTrue(set.contains(3L))
    }
}
