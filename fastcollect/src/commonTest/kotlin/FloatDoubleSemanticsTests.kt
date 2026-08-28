package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * The one place where Float and Double genuinely differ from the other primitives: element equality inside every
 * collection is `equalsRaw`, i.e. a raw-bit comparison, so NaN equals NaN and -0.0 does not equal 0.0. Priority
 * queues are the exception - they order by compareTo, not equalsRaw - and for Float/Double maps the default value
 * that signals "absent" is NaN itself.
 *
 * Everything not covered here is shared template logic and is tested once against Int in the deep suites.
 */
class FloatDoubleSemanticsTests {

    // ---------- the underlying comparison ----------

    @Test
    fun equalsRaw_treatsNaNAsEqualToItselfAndSeparatesSignedZero() {
        assertTrue(Float.NaN equalsRaw Float.NaN)
        assertTrue(Double.NaN equalsRaw Double.NaN)
        assertFalse(Float.NaN notEqualsRaw Float.NaN)

        assertFalse(-0.0f equalsRaw 0.0f)
        assertFalse(-0.0 equalsRaw 0.0)
        assertTrue(-0.0f notEqualsRaw 0.0f)

        assertTrue(-0.0f equalsRaw -0.0f)
        assertTrue(1.5f equalsRaw 1.5f)
        assertFalse(1.5f equalsRaw 2.5f)
    }

    // ---------- lists ----------

    @Test
    fun floatList_indexOfAndContains_useRawEquality() {
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
    fun doubleList_indexOfAndContains_useRawEquality() {
        assertTrue(doubleListOf(Double.NaN).contains(Double.NaN))
        assertEquals(0, doubleListOf(Double.NaN).indexOf(Double.NaN))
        assertEquals(0, doubleListOf(Double.NaN).lastIndexOf(Double.NaN))

        assertFalse(doubleListOf(-0.0).contains(0.0))
        assertEquals(-1, doubleArrayOf(-0.0).asDoubleList().indexOf(0.0))
        assertTrue(doubleArrayOf(-0.0).asDoubleList().contains(-0.0))
    }

    @Test
    fun floatAndDoubleList_equals_useRawEquality() {
        assertEquals<Any>(floatArrayOf(1f, Float.NaN).asFloatList(), floatArrayOf(1f, Float.NaN).asFloatList())
        assertNotEquals<Any>(floatArrayOf(-0.0f).asFloatList(), floatArrayOf(0.0f).asFloatList())

        assertEquals<Any>(doubleArrayOf(1.0, Double.NaN).asDoubleList(), doubleArrayOf(1.0, Double.NaN).asDoubleList())
        assertNotEquals<Any>(doubleArrayOf(-0.0).asDoubleList(), doubleArrayOf(0.0).asDoubleList())
    }

    @Test
    fun floatAndDoubleList_removeAndReplace_useRawEquality() {
        val floats = mutableFloatListOf(1f, Float.NaN, -0.0f)
        assertTrue(floats.remove(Float.NaN))
        assertFalse(floats.remove(0.0f), "0.0f must not match the stored -0.0f")
        assertTrue(floats.remove(-0.0f))
        assertEquals(1, floats.size)

        val doubles = mutableDoubleListOf(Double.NaN, -0.0)
        assertTrue(doubles.remove(Double.NaN))
        assertFalse(doubles.remove(0.0))
    }

    // ---------- sets ----------

    @Test
    fun floatSet_contains_andDeduplication_useRawEquality() {
        val set = FloatHashSet()
        assertTrue(set.add(Float.NaN))
        assertFalse(set.add(Float.NaN), "NaN is its own duplicate under equalsRaw")
        assertTrue(set.contains(Float.NaN))

        assertTrue(set.add(0.0f))
        assertTrue(set.add(-0.0f), "-0.0f is a distinct element from 0.0f")
        assertEquals(3, set.size)
        assertTrue(set.contains(-0.0f))

        assertTrue(set.remove(Float.NaN))
        assertFalse(set.contains(Float.NaN))
        assertEquals(2, set.size)
    }

    @Test
    fun doubleSet_contains_andDeduplication_useRawEquality() {
        val set = DoubleHashSet()
        assertTrue(set.add(Double.NaN))
        assertFalse(set.add(Double.NaN))
        assertTrue(set.contains(Double.NaN))

        assertTrue(set.add(0.0))
        assertTrue(set.add(-0.0))
        assertEquals(3, set.size)

        assertFalse(doubleSetOf(-0.0).contains(0.0))
        assertTrue(doubleSetOf(Double.NaN).contains(Double.NaN))
    }

    // ---------- maps: NaN is the default value ----------

    @Test
    fun floatValuedMap_defaultValueIsNaN() {
        val map = mutableInt2FloatMapOf()
        assertTrue(map[1].isNaN())
        assertTrue(map.isDefaultValue(Float.NaN))
        assertFalse(map.isDefaultValue(0.0f))

        val doubleMap = mutableInt2DoubleMapOf()
        assertTrue(doubleMap[1].isNaN())
        assertTrue(doubleMap.isDefaultValue(Double.NaN))
    }

    @Test
    fun floatValuedMap_storedNaN_keyStillReportedPresent() {
        // the "absent" signal and a legitimately stored NaN are the same bits, so containsKey has to disambiguate
        val map = mutableInt2FloatMapOf()
        map[1] = Float.NaN

        assertTrue(map.containsKey(1))
        assertEquals(1, map.size)
        assertTrue(map[1].isNaN())
        assertTrue(map.containsValue(Float.NaN))
        assertTrue(map.getValue(1).isNaN())

        var calls = 0
        assertTrue(map.getOrElse(1) { calls++; 99f }.isNaN())
        assertEquals(0, calls)
        assertTrue(map.getOrPut(1) { calls++; 99f }.isNaN())
        assertEquals(0, calls)

        assertTrue(map.removeKey(1).isNaN())
        assertTrue(map.isEmpty())
    }

    @Test
    fun floatValuedMap_containsValue_andRemove_useRawEquality() {
        val map = mutableInt2FloatMapOf(1 to Float.NaN, 2 to -0.0f, 3 to 0.0f)

        assertTrue(map.containsValue(Float.NaN))
        assertTrue(map.containsValue(-0.0f))
        assertTrue(map.containsValue(0.0f))
        assertFalse(map.containsValue(1.5f))

        assertFalse(map.remove(2, 0.0f), "0.0f must not match the stored -0.0f")
        assertTrue(map.remove(2, -0.0f))
        assertTrue(map.remove(1, Float.NaN))
        assertEquals(1, map.size)
    }

    @Test
    fun doubleValuedMap_containsValue_andRemove_useRawEquality() {
        val map = mutableLong2DoubleMapOf(1L to Double.NaN, 2L to -0.0)

        assertTrue(map.containsValue(Double.NaN))
        assertTrue(map.containsValue(-0.0))
        assertFalse(map.containsValue(0.0))

        assertFalse(map.remove(2L, 0.0))
        assertTrue(map.remove(2L, -0.0))
        assertTrue(map.remove(1L, Double.NaN))
        assertTrue(map.isEmpty())
    }

    @Test
    fun floatValuedMap_merge_treatsNaNAsAChangedValue() {
        // merge only writes when the new value differs under equalsRaw, so NaN -> NaN must be a no-op that still
        // reports NaN, while NaN -> 1f must be written through
        val map = mutableInt2FloatMapOf()
        map[1] = Float.NaN

        assertTrue(map.merge(1, 0f) { old, _ -> old }.isNaN())
        assertTrue(map[1].isNaN())

        assertEquals(1f, map.merge(1, 1f) { _, value -> value })
        assertEquals(1f, map[1])
    }

    // ---------- priority queues order by compareTo, not equalsRaw ----------

    @Test
    fun floatPriorityQueue_ordersNaNLastAndNegativeZeroBeforeZero() {
        // isHigherPriority() delegates to Float.compareTo, which is a total order: -0.0 < 0.0 < ... < NaN
        val queue = floatPriorityQueueOf(Float.NaN, 1f, -0.0f, 0.0f, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
        val drained = queue.drain()

        assertEquals(Float.NEGATIVE_INFINITY, drained[0])
        assertEquals(-0.0f, drained[1].also { assertTrue(it equalsRaw -0.0f) })
        assertTrue(drained[2] equalsRaw 0.0f)
        assertEquals(1f, drained[3])
        assertEquals(Float.POSITIVE_INFINITY, drained[4])
        assertTrue(drained[5].isNaN(), "NaN sorts last under compareTo")
    }

    @Test
    fun doublePriorityQueue_ordersNaNLastAndNegativeZeroBeforeZero() {
        val drained = doublePriorityQueueOf(Double.NaN, 1.0, -0.0, 0.0).drain()
        assertTrue(drained[0] equalsRaw -0.0)
        assertTrue(drained[1] equalsRaw 0.0)
        assertEquals(1.0, drained[2])
        assertTrue(drained[3].isNaN())
    }

    @Test
    fun descendingFloatPriorityQueue_ordersNaNFirst() {
        val drained = floatDescendingPriorityQueueOf(Float.NaN, 1f, -0.0f, 0.0f).drain()
        assertTrue(drained[0].isNaN())
        assertEquals(1f, drained[1])
        assertTrue(drained[2] equalsRaw 0.0f)
        assertTrue(drained[3] equalsRaw -0.0f)
    }

    @Test
    fun floatPriorityQueue_containsAndRemove_useRawEqualityNotCompareTo() {
        // ordering says NaN compares equal to NaN and -0.0 sorts below 0.0; membership is decided by equalsRaw
        val queue = floatPriorityQueueOf(Float.NaN, -0.0f, 1f)

        assertTrue(queue.contains(Float.NaN))
        assertTrue(queue.contains(-0.0f))
        assertFalse(queue.contains(0.0f), "0.0f must not match the stored -0.0f")

        assertFalse(queue.remove(0.0f))
        assertTrue(queue.remove(-0.0f))
        assertTrue(queue.remove(Float.NaN))
        assertEquals(listOf(1f), queue.drain())
    }
}
