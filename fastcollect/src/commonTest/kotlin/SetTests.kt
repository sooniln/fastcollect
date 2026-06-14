package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.doubles.*
import io.github.sooniln.fastcollect.floats.*
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

    @Test
    fun iteratorRemove_wrapsElementAroundToEndOfArray() {
        // Keys 1, 3, 5 all hash to slots 2 or 3 in a 4-slot array (mask=3, from trimToSize).
        // One key ends up at slot 0 via probe wrap-around (distance > 0). Removing the
        // highest-slot element chains a backward shift from slot 0 to slot 3, so the iterator
        // must use (slot-1) and mask rather than --slot to avoid an ArrayIndexOutOfBoundsException.
        val set = IntHashSet(100).apply { add(1); add(3); add(5) }
        set.trimToSize()
        val visited = mutableListOf<Int>()
        val it = set.iterator()
        while (it.hasNext()) {
            visited.add(it.nextInt())
            it.remove()
        }
        assertTrue(set.isEmpty())
        assertEquals(listOf(1, 3, 5), visited.sorted())
    }

    @Test
    fun absentZeroElement_notReportedPresent() {
        // 0 is the hash set's initial empty-slot marker, but must still behave as an ordinary element
        assertFalse(mutableIntSetOf().contains(0))

        val set = mutableIntSetOf(1, 2)
        assertFalse(set.contains(0))
        assertFalse(set.remove(0))
        assertEquals(2, set.size)

        assertTrue(set.add(0))
        assertTrue(set.contains(0))
        assertEquals(3, set.size)
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

    @Test
    fun iteratorRemove_wrapsElementAroundToEndOfArray() {
        // Same scenario as IntSetTest: keys 1, 3, 5 hash to the same slots for Long (small
        // values produce the same hashCode), exercising the backward-shift wrap-around path.
        val set = LongHashSet(100).apply { add(1L); add(3L); add(5L) }
        set.trimToSize()
        val visited = mutableListOf<Long>()
        val it = set.iterator()
        while (it.hasNext()) {
            visited.add(it.nextLong())
            it.remove()
        }
        assertTrue(set.isEmpty())
        assertEquals(listOf(1L, 3L, 5L), visited.sorted())
    }

    @Test
    fun absentZeroElement_notReportedPresent() {
        // 0 is the hash set's initial empty-slot marker, but must still behave as an ordinary element
        assertFalse(mutableLongSetOf().contains(0L))

        val set = mutableLongSetOf(1L, 2L)
        assertFalse(set.contains(0L))
        assertFalse(set.remove(0L))
        assertEquals(2, set.size)

        assertTrue(set.add(0L))
        assertTrue(set.contains(0L))
        assertEquals(3, set.size)
    }
}

// ============================= Float =============================

class FloatSetTest {
    @Test
    fun setOf_vararg() {
        val set = floatSetOf(1f, 2f, 3f)
        assertEquals(3, set.size)
        assertTrue(set.contains(1f))
        assertTrue(set.contains(2f))
        assertTrue(set.contains(3f))
    }

    @Test
    fun mutableSetOf_vararg() {
        val set = mutableFloatSetOf(4f, 5f, 6f)
        assertEquals(3, set.size)
        assertTrue(set.contains(4f))
        assertTrue(set.contains(5f))
        assertTrue(set.contains(6f))
    }

    @Test
    fun buildSet_dsl() {
        val set = buildFloatSet {
            add(10f)
            add(20f)
            add(30f)
        }
        assertEquals(3, set.size)
        assertTrue(set.contains(10f))
        assertTrue(set.contains(20f))
        assertTrue(set.contains(30f))
    }

    @Test
    fun union_combinesBothSets() {
        val a = floatSetOf(1f, 2f, 3f)
        val b = floatSetOf(3f, 4f, 5f)
        val result = a union b
        assertEquals(5, result.size)
        for (v in listOf(1f, 2f, 3f, 4f, 5f)) assertTrue(result.contains(v), "$v missing from union")
    }

    @Test
    fun union_withEmptySet() {
        val a = floatSetOf(1f, 2f, 3f)
        val result = a union floatSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1f, 2f, 3f)) assertTrue(result.contains(v))
    }

    @Test
    fun intersect_returnsCommonElements() {
        val a = floatSetOf(1f, 2f, 3f, 4f)
        val b = floatSetOf(3f, 4f, 5f, 6f)
        val result = a intersect b
        assertEquals(2, result.size)
        assertTrue(result.contains(3f))
        assertTrue(result.contains(4f))
    }

    @Test
    fun intersect_noCommonElements_isEmpty() {
        val a = floatSetOf(1f, 2f)
        val b = floatSetOf(3f, 4f)
        assertTrue((a intersect b).isEmpty())
    }

    @Test
    fun subtract_removesRhsFromLhs() {
        val a = floatSetOf(1f, 2f, 3f, 4f)
        val b = floatSetOf(3f, 4f)
        val result = a subtract b
        assertEquals(2, result.size)
        assertTrue(result.contains(1f))
        assertTrue(result.contains(2f))
        assertFalse(result.contains(3f))
        assertFalse(result.contains(4f))
    }

    @Test
    fun subtract_removesAll_isEmpty() {
        val a = floatSetOf(1f, 2f)
        val result = a subtract floatSetOf(1f, 2f)
        assertTrue(result.isEmpty())
    }

    @Test
    fun subtract_emptyRhs_unchanged() {
        val a = floatSetOf(1f, 2f, 3f)
        val result = a subtract floatSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1f, 2f, 3f)) assertTrue(result.contains(v))
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val set = FloatHashSet().apply { add(1f); add(2f); add(3f) }
        set.ensureCapacity(100)
        assertEquals(3, set.size)
        assertTrue(set.contains(1f))
        assertTrue(set.contains(2f))
        assertTrue(set.contains(3f))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val set = FloatHashSet(100).apply { add(1f); add(2f); add(3f) }
        set.trimToSize()
        assertEquals(3, set.size)
        assertTrue(set.contains(1f))
        assertTrue(set.contains(2f))
        assertTrue(set.contains(3f))
    }

    @Test
    fun iteratorRemove_wrapsElementAroundToEndOfArray() {
        // Same scenario as IntSetTest: these elements convert to raw bits 1, 3, 5, which hash to
        // the same slots in the backing hash set, exercising the backward-shift wrap-around path.
        val set = FloatHashSet(100).apply { add(Float.fromBits(1)); add(Float.fromBits(3)); add(Float.fromBits(5)) }
        set.trimToSize()
        val visited = mutableListOf<Float>()
        val it = set.iterator()
        while (it.hasNext()) {
            visited.add(it.nextFloat())
            it.remove()
        }
        assertTrue(set.isEmpty())
        assertEquals(listOf(Float.fromBits(1), Float.fromBits(3), Float.fromBits(5)), visited.sorted())
    }

    @Test
    fun contains_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must be found regardless of implementation, and -0.0f must not match 0.0f
        assertTrue(floatSetOf(Float.NaN).contains(Float.NaN))
        assertTrue(mutableFloatSetOf(Float.NaN).contains(Float.NaN))
        assertFalse(floatSetOf(-0.0f).contains(0.0f))
        assertFalse(mutableFloatSetOf(-0.0f).contains(0.0f))
        assertTrue(floatSetOf(-0.0f).contains(-0.0f))
    }

    @Test
    fun absentZeroElement_notReportedPresent() {
        // 0.0f converts to raw bits 0, the backing hash set's initial empty-slot marker, but must
        // still behave as an ordinary element
        assertFalse(mutableFloatSetOf().contains(0f))

        val set = mutableFloatSetOf(1f, 2f)
        assertFalse(set.contains(0f))
        assertFalse(set.remove(0f))
        assertEquals(2, set.size)

        assertTrue(set.add(0f))
        assertTrue(set.contains(0f))
        assertEquals(3, set.size)
    }
}

// ============================= Double =============================

class DoubleSetTest {
    @Test
    fun setOf_vararg() {
        val set = doubleSetOf(1.0, 2.0, 3.0)
        assertEquals(3, set.size)
        assertTrue(set.contains(1.0))
        assertTrue(set.contains(2.0))
        assertTrue(set.contains(3.0))
    }

    @Test
    fun mutableSetOf_vararg() {
        val set = mutableDoubleSetOf(4.0, 5.0, 6.0)
        assertEquals(3, set.size)
        assertTrue(set.contains(4.0))
        assertTrue(set.contains(5.0))
        assertTrue(set.contains(6.0))
    }

    @Test
    fun buildSet_dsl() {
        val set = buildDoubleSet {
            add(10.0)
            add(20.0)
            add(30.0)
        }
        assertEquals(3, set.size)
        assertTrue(set.contains(10.0))
        assertTrue(set.contains(20.0))
        assertTrue(set.contains(30.0))
    }

    @Test
    fun union_combinesBothSets() {
        val a = doubleSetOf(1.0, 2.0, 3.0)
        val b = doubleSetOf(3.0, 4.0, 5.0)
        val result = a union b
        assertEquals(5, result.size)
        for (v in listOf(1.0, 2.0, 3.0, 4.0, 5.0)) assertTrue(result.contains(v), "$v missing from union")
    }

    @Test
    fun union_withEmptySet() {
        val a = doubleSetOf(1.0, 2.0, 3.0)
        val result = a union doubleSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1.0, 2.0, 3.0)) assertTrue(result.contains(v))
    }

    @Test
    fun intersect_returnsCommonElements() {
        val a = doubleSetOf(1.0, 2.0, 3.0, 4.0)
        val b = doubleSetOf(3.0, 4.0, 5.0, 6.0)
        val result = a intersect b
        assertEquals(2, result.size)
        assertTrue(result.contains(3.0))
        assertTrue(result.contains(4.0))
    }

    @Test
    fun intersect_noCommonElements_isEmpty() {
        val a = doubleSetOf(1.0, 2.0)
        val b = doubleSetOf(3.0, 4.0)
        assertTrue((a intersect b).isEmpty())
    }

    @Test
    fun subtract_removesRhsFromLhs() {
        val a = doubleSetOf(1.0, 2.0, 3.0, 4.0)
        val b = doubleSetOf(3.0, 4.0)
        val result = a subtract b
        assertEquals(2, result.size)
        assertTrue(result.contains(1.0))
        assertTrue(result.contains(2.0))
        assertFalse(result.contains(3.0))
        assertFalse(result.contains(4.0))
    }

    @Test
    fun subtract_removesAll_isEmpty() {
        val a = doubleSetOf(1.0, 2.0)
        val result = a subtract doubleSetOf(1.0, 2.0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun subtract_emptyRhs_unchanged() {
        val a = doubleSetOf(1.0, 2.0, 3.0)
        val result = a subtract doubleSetOf()
        assertEquals(3, result.size)
        for (v in listOf(1.0, 2.0, 3.0)) assertTrue(result.contains(v))
    }

    @Test
    fun ensureCapacity_doesNotLoseElements() {
        val set = DoubleHashSet().apply { add(1.0); add(2.0); add(3.0) }
        set.ensureCapacity(100)
        assertEquals(3, set.size)
        assertTrue(set.contains(1.0))
        assertTrue(set.contains(2.0))
        assertTrue(set.contains(3.0))
    }

    @Test
    fun trimToSize_doesNotLoseElements() {
        val set = DoubleHashSet(100).apply { add(1.0); add(2.0); add(3.0) }
        set.trimToSize()
        assertEquals(3, set.size)
        assertTrue(set.contains(1.0))
        assertTrue(set.contains(2.0))
        assertTrue(set.contains(3.0))
    }

    @Test
    fun iteratorRemove_wrapsElementAroundToEndOfArray() {
        // Same scenario as IntSetTest: these elements convert to raw bits 1, 3, 5, which hash to
        // the same slots in the backing hash set, exercising the backward-shift wrap-around path.
        val set = DoubleHashSet(100).apply { add(Double.fromBits(1L)); add(Double.fromBits(3L)); add(Double.fromBits(5L)) }
        set.trimToSize()
        val visited = mutableListOf<Double>()
        val it = set.iterator()
        while (it.hasNext()) {
            visited.add(it.nextDouble())
            it.remove()
        }
        assertTrue(set.isEmpty())
        assertEquals(listOf(Double.fromBits(1L), Double.fromBits(3L), Double.fromBits(5L)), visited.sorted())
    }

    @Test
    fun contains_nanAndNegativeZero_matchBoxedSemantics() {
        // NaN must be found regardless of implementation, and -0.0 must not match 0.0
        assertTrue(doubleSetOf(Double.NaN).contains(Double.NaN))
        assertTrue(mutableDoubleSetOf(Double.NaN).contains(Double.NaN))
        assertFalse(doubleSetOf(-0.0).contains(0.0))
        assertFalse(mutableDoubleSetOf(-0.0).contains(0.0))
        assertTrue(doubleSetOf(-0.0).contains(-0.0))
    }

    @Test
    fun absentZeroElement_notReportedPresent() {
        // 0.0 converts to raw bits 0, the backing hash set's initial empty-slot marker, but must
        // still behave as an ordinary element
        assertFalse(mutableDoubleSetOf().contains(0.0))

        val set = mutableDoubleSetOf(1.0, 2.0)
        assertFalse(set.contains(0.0))
        assertFalse(set.remove(0.0))
        assertEquals(2, set.size)

        assertTrue(set.add(0.0))
        assertTrue(set.contains(0.0))
        assertEquals(3, set.size)
    }
}
