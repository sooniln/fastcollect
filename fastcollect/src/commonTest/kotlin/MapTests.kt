package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.*
import io.github.sooniln.fastcollect.longs.*
import kotlin.test.*

// ============================= Int2Int =============================

class Int2IntMapTest {
    @Test
    fun mapOf_vararg() {
        val map = int2IntMapOf(1 to 10, 2 to 20, 3 to 30)
        assertEquals(3, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
        assertEquals(30, map[3])
    }

    @Test
    fun mutableMapOf_vararg() {
        val map = mutableInt2IntMapOf(1 to 10, 2 to 20)
        assertEquals(2, map.size)
        assertEquals(10, map[1])
    }

    @Test
    fun buildMap_dsl() {
        val map = buildInt2IntMap {
            set(1, 100)
            set(2, 200)
        }
        assertEquals(2, map.size)
        assertEquals(100, map[1])
        assertEquals(200, map[2])
    }

    @Test
    fun defaultValue_isMinValue() {
        assertEquals(Int.MIN_VALUE, mutableInt2IntMapOf().defaultValue)
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        val map = mutableInt2IntMapOf(1 to 10)
        assertEquals(Int.MIN_VALUE, map[99])
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        val map = mutableInt2IntMapOf()
        assertTrue(map.isDefaultValue(Int.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        val map = mutableInt2IntMapOf(1 to 42)
        assertFalse(map.isDefaultValue(42))
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        val map = int2IntMapOf(1 to 10)
        assertEquals(10, map.getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        val map = int2IntMapOf(1 to 10)
        assertFailsWith<NoSuchElementException> { map.getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2IntMapOf(1 to 10).getOrElse(99) { invoked = true; -1 }
        assertTrue(invoked)
        assertEquals(-1, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2IntMapOf(1 to 10).getOrElse(1) { invoked = true; -1 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun getOrDefault_absentKey_returnsDefault() {
        assertEquals(-99, int2IntMapOf(1 to 10).getOrDefault(99, -99))
    }

    @Test
    fun getOrDefault_presentKey_returnsStoredValue() {
        assertEquals(10, int2IntMapOf(1 to 10).getOrDefault(1, -99))
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2IntMapOf(1 to 10)
        val result = map.merge(99, 42) { old, new -> old + new }
        assertEquals(42, result)
        assertEquals(42, map[99])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2IntMapOf(1 to 10)
        val result = map.merge(1, 5) { old, new -> old + new }
        assertEquals(15, result)
        assertEquals(15, map[1])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2IntMapOf(1 to 10)
        val result = map.getOrPut(99) { 42 }
        assertEquals(42, result)
        assertEquals(42, map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2IntMapOf(1 to 10)
        val result = map.getOrPut(1) { invoked = true; 99 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = int2IntMapOf(1 to 10, 2 to 20)
        val dest = mutableInt2IntMapOf(3 to 30)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10, dest[1])
        assertEquals(20, dest[2])
        assertEquals(30, dest[3])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableInt2IntMapOf(3 to 30)
        dest.putAll(mapOf(1 to 10, 2 to 20))
        assertEquals(3, dest.size)
        assertEquals(10, dest[1])
        assertEquals(20, dest[2])
    }

    @Test
    fun ensureCapacity_doesNotLoseEntries() {
        val map = Int2IntHashMap().apply { set(1, 10); set(2, 20) }
        map.ensureCapacity(100)
        assertEquals(2, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
    }

    @Test
    fun trimToSize_doesNotLoseEntries() {
        val map = Int2IntHashMap(100).apply { set(1, 10); set(2, 20) }
        map.trimToSize()
        assertEquals(2, map.size)
        assertEquals(10, map[1])
        assertEquals(20, map[2])
    }
}

// ============================= Int2Long =============================

class Int2LongMapTest {
    @Test
    fun defaultValue_isMinValue() {
        assertEquals(Long.MIN_VALUE, mutableInt2LongMapOf().defaultValue)
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Long.MIN_VALUE, int2LongMapOf(1 to 10L)[99])
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableInt2LongMapOf().isDefaultValue(Long.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        assertFalse(int2LongMapOf(1 to 42L).isDefaultValue(42L))
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10L, int2LongMapOf(1 to 10L).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2LongMapOf(1 to 10L).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2LongMapOf(1 to 10L).getOrElse(99) { invoked = true; -1L }
        assertTrue(invoked)
        assertEquals(-1L, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2LongMapOf(1 to 10L).getOrElse(1) { invoked = true; -1L }
        assertFalse(invoked)
        assertEquals(10L, result)
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(42L, map.merge(99, 42L) { old, new -> old + new })
        assertEquals(42L, map[99])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(15L, map.merge(1, 5L) { old, new -> old + new })
        assertEquals(15L, map[1])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableInt2LongMapOf(1 to 10L)
        assertEquals(42L, map.getOrPut(99) { 42L })
        assertEquals(42L, map[99])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableInt2LongMapOf(1 to 10L)
        val result = map.getOrPut(1) { invoked = true; 99L }
        assertFalse(invoked)
        assertEquals(10L, result)
    }
}

// ============================= Int2Float =============================

class Int2FloatMapTest {
    @Test
    fun defaultValue_isNaN() {
        assertTrue(mutableInt2FloatMapOf().defaultValue.isNaN())
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(int2FloatMapOf(1 to 1f)[99].isNaN())
    }

    @Test
    fun isDefaultValue_true_forNaN() {
        assertTrue(mutableInt2FloatMapOf().isDefaultValue(Float.NaN))
    }

    @Test
    fun isDefaultValue_false_forZero() {
        assertFalse(mutableInt2FloatMapOf().isDefaultValue(0f))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        assertFalse(int2FloatMapOf(1 to 42f).isDefaultValue(42f))
    }

    @Test
    fun storedNaN_keyStillReportedPresent() {
        val map = mutableInt2FloatMapOf(1 to Float.NaN)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is NaN")
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(1.5f, int2FloatMapOf(1 to 1.5f).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2FloatMapOf(1 to 1f).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2FloatMapOf(1 to 1f).getOrElse(99) { invoked = true; -1f }
        assertTrue(invoked)
        assertEquals(-1f, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2FloatMapOf(1 to 1f).getOrElse(1) { invoked = true; -1f }
        assertFalse(invoked)
        assertEquals(1f, result)
    }
}

// ============================= Int2Double =============================

class Int2DoubleMapTest {
    @Test
    fun defaultValue_isNaN() {
        assertTrue(mutableInt2DoubleMapOf().defaultValue.isNaN())
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(int2DoubleMapOf(1 to 1.0)[99].isNaN())
    }

    @Test
    fun isDefaultValue_true_forNaN() {
        assertTrue(mutableInt2DoubleMapOf().isDefaultValue(Double.NaN))
    }

    @Test
    fun isDefaultValue_false_forZero() {
        assertFalse(mutableInt2DoubleMapOf().isDefaultValue(0.0))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        assertFalse(int2DoubleMapOf(1 to 42.0).isDefaultValue(42.0))
    }

    @Test
    fun storedNaN_keyStillReportedPresent() {
        val map = mutableInt2DoubleMapOf(1 to Double.NaN)
        assertTrue(map.containsKey(1), "key must still be present even when stored value is NaN")
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(1.5, int2DoubleMapOf(1 to 1.5).getValue(1))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { int2DoubleMapOf(1 to 1.0).getValue(99) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = int2DoubleMapOf(1 to 1.0).getOrElse(99) { invoked = true; -1.0 }
        assertTrue(invoked)
        assertEquals(-1.0, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = int2DoubleMapOf(1 to 1.0).getOrElse(1) { invoked = true; -1.0 }
        assertFalse(invoked)
        assertEquals(1.0, result)
    }
}

// ============================= Int2Any =============================

class Int2AnyMapTest {
    @Test
    fun put_nullValue_containsKey() {
        val map = mutableInt2AnyMapOf<String?>()
        map[1] = null
        assertTrue(map.containsKey(1), "key with null value must still report as present")
    }

    @Test
    fun get_nullValue_returnsNull() {
        val map = mutableInt2AnyMapOf<String?>()
        map[1] = null
        assertNull(map[1])
    }

    @Test
    fun getOrElse_nullStoredValue_returnsNull_notDefault() {
        val map = mutableInt2AnyMapOf<String?>()
        map[1] = null
        var invoked = false
        val result = map.getOrElse(1) { invoked = true; "fallback" }
        assertFalse(invoked, "lambda must not be invoked when null is stored under the key")
        assertNull(result)
    }
}

// ============================= Long2Int =============================

class Long2IntMapTest {
    @Test
    fun defaultValue_isMinValue() {
        assertEquals(Int.MIN_VALUE, mutableLong2IntMapOf().defaultValue)
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Int.MIN_VALUE, long2IntMapOf(1L to 10)[99L])
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2IntMapOf().isDefaultValue(Int.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        assertFalse(long2IntMapOf(1L to 42).isDefaultValue(42))
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10, long2IntMapOf(1L to 10).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2IntMapOf(1L to 10).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2IntMapOf(1L to 10).getOrElse(99L) { invoked = true; -1 }
        assertTrue(invoked)
        assertEquals(-1, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2IntMapOf(1L to 10).getOrElse(1L) { invoked = true; -1 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun merge_absentKey_insertsValue() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(42, map.merge(99L, 42) { old, new -> old + new })
        assertEquals(42, map[99L])
    }

    @Test
    fun merge_presentKey_appliesMergeFunction() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(15, map.merge(1L, 5) { old, new -> old + new })
        assertEquals(15, map[1L])
    }

    @Test
    fun getOrPut_absentKey_insertsAndReturns() {
        val map = mutableLong2IntMapOf(1L to 10)
        assertEquals(42, map.getOrPut(99L) { 42 })
        assertEquals(42, map[99L])
    }

    @Test
    fun getOrPut_presentKey_returnsExisting_doesNotCallLambda() {
        var invoked = false
        val map = mutableLong2IntMapOf(1L to 10)
        val result = map.getOrPut(1L) { invoked = true; 99 }
        assertFalse(invoked)
        assertEquals(10, result)
    }

    @Test
    fun putAll_fromPrimitiveMap_copiesAllEntries() {
        val source = long2IntMapOf(1L to 10, 2L to 20)
        val dest = mutableLong2IntMapOf(3L to 30)
        dest.putAll(source)
        assertEquals(3, dest.size)
        assertEquals(10, dest[1L])
        assertEquals(20, dest[2L])
        assertEquals(30, dest[3L])
    }

    @Test
    fun putAll_fromStandardMap_copiesAllEntries() {
        val dest = mutableLong2IntMapOf(3L to 30)
        dest.putAll(mapOf(1L to 10, 2L to 20))
        assertEquals(3, dest.size)
        assertEquals(10, dest[1L])
        assertEquals(20, dest[2L])
    }
}

// ============================= Long2Long =============================

class Long2LongMapTest {
    @Test
    fun defaultValue_isMinValue() {
        assertEquals(Long.MIN_VALUE, mutableLong2LongMapOf().defaultValue)
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertEquals(Long.MIN_VALUE, long2LongMapOf(1L to 10L)[99L])
    }

    @Test
    fun isDefaultValue_true_forDefaultValue() {
        assertTrue(mutableLong2LongMapOf().isDefaultValue(Long.MIN_VALUE))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        assertFalse(long2LongMapOf(1L to 42L).isDefaultValue(42L))
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(10L, long2LongMapOf(1L to 10L).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2LongMapOf(1L to 10L).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2LongMapOf(1L to 10L).getOrElse(99L) { invoked = true; -1L }
        assertTrue(invoked)
        assertEquals(-1L, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2LongMapOf(1L to 10L).getOrElse(1L) { invoked = true; -1L }
        assertFalse(invoked)
        assertEquals(10L, result)
    }
}

// ============================= Long2Float =============================

class Long2FloatMapTest {
    @Test
    fun defaultValue_isNaN() {
        assertTrue(mutableLong2FloatMapOf().defaultValue.isNaN())
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(long2FloatMapOf(1L to 1f)[99L].isNaN())
    }

    @Test
    fun isDefaultValue_true_forNaN() {
        assertTrue(mutableLong2FloatMapOf().isDefaultValue(Float.NaN))
    }

    @Test
    fun isDefaultValue_false_forZero() {
        assertFalse(mutableLong2FloatMapOf().isDefaultValue(0f))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        assertFalse(long2FloatMapOf(1L to 42f).isDefaultValue(42f))
    }

    @Test
    fun storedNaN_keyStillReportedPresent() {
        val map = mutableLong2FloatMapOf(1L to Float.NaN)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is NaN")
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(1.5f, long2FloatMapOf(1L to 1.5f).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2FloatMapOf(1L to 1f).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2FloatMapOf(1L to 1f).getOrElse(99L) { invoked = true; -1f }
        assertTrue(invoked)
        assertEquals(-1f, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2FloatMapOf(1L to 1f).getOrElse(1L) { invoked = true; -1f }
        assertFalse(invoked)
        assertEquals(1f, result)
    }
}

// ============================= Long2Double =============================

class Long2DoubleMapTest {
    @Test
    fun defaultValue_isNaN() {
        assertTrue(mutableLong2DoubleMapOf().defaultValue.isNaN())
    }

    @Test
    fun get_absentKey_returnsDefaultValue() {
        assertTrue(long2DoubleMapOf(1L to 1.0)[99L].isNaN())
    }

    @Test
    fun isDefaultValue_true_forNaN() {
        assertTrue(mutableLong2DoubleMapOf().isDefaultValue(Double.NaN))
    }

    @Test
    fun isDefaultValue_false_forZero() {
        assertFalse(mutableLong2DoubleMapOf().isDefaultValue(0.0))
    }

    @Test
    fun isDefaultValue_false_forStoredValue() {
        assertFalse(long2DoubleMapOf(1L to 42.0).isDefaultValue(42.0))
    }

    @Test
    fun storedNaN_keyStillReportedPresent() {
        val map = mutableLong2DoubleMapOf(1L to Double.NaN)
        assertTrue(map.containsKey(1L), "key must still be present even when stored value is NaN")
    }

    @Test
    fun getValue_presentKey_returnsValue() {
        assertEquals(1.5, long2DoubleMapOf(1L to 1.5).getValue(1L))
    }

    @Test
    fun getValue_absentKey_throws() {
        assertFailsWith<NoSuchElementException> { long2DoubleMapOf(1L to 1.0).getValue(99L) }
    }

    @Test
    fun getOrElse_absentKey_invokesLambda() {
        var invoked = false
        val result = long2DoubleMapOf(1L to 1.0).getOrElse(99L) { invoked = true; -1.0 }
        assertTrue(invoked)
        assertEquals(-1.0, result)
    }

    @Test
    fun getOrElse_presentKey_doesNotInvokeLambda() {
        var invoked = false
        val result = long2DoubleMapOf(1L to 1.0).getOrElse(1L) { invoked = true; -1.0 }
        assertFalse(invoked)
        assertEquals(1.0, result)
    }
}

// ============================= Long2Any =============================

class Long2AnyMapTest {
    @Test
    fun put_nullValue_containsKey() {
        val map = mutableLong2AnyMapOf<String?>()
        map[1L] = null
        assertTrue(map.containsKey(1L), "key with null value must still report as present")
    }

    @Test
    fun get_nullValue_returnsNull() {
        val map = mutableLong2AnyMapOf<String?>()
        map[1L] = null
        assertNull(map[1L])
    }

    @Test
    fun getOrElse_nullStoredValue_returnsNull_notDefault() {
        val map = mutableLong2AnyMapOf<String?>()
        map[1L] = null
        var invoked = false
        val result = map.getOrElse(1L) { invoked = true; "fallback" }
        assertFalse(invoked, "lambda must not be invoked when null is stored under the key")
        assertNull(result)
    }
}
