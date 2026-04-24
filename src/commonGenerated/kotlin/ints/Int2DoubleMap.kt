package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.doubles.DoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Int2DoubleMap : Map<Int, Double> {
    public val defaultValue: Double

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: Double): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the extension get(key) method instead.",
        replaceWith = ReplaceWith("get(key)", "io.github.sooniln.fastcollect.ints.get"),
        level = DeprecationLevel.HIDDEN)
    override fun get(key: Int): Double? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Int): Double

    override val keys: IntSet

    override val values: DoubleCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Int, Double>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Int, Double> {
        override val key: Int
        override val value: Double
    }
}

public operator fun Int2DoubleMap.get(key: Int): Double = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Int2DoubleMap.iterator(): Iterator<Int2DoubleMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Int2DoubleMap.getOrElse(key: Int, defaultValue: () -> Double): Double {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableInt2DoubleMap : Int2DoubleMap, MutableMap<Int, Double> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun put(key: Int, value: Double): Double? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Double): Double

    public operator fun set(key: Int, value: Double) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Int): Double? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Double

    override val keys: MutableIntSet
    override val values: MutableDoubleCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Double>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Double>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Int2DoubleMap.Entry, MutableMap.MutableEntry<Int, Double> {
        override val key: Int
        override val value: Double
    }
}

public fun MutableInt2DoubleMap.put(key: Int, value: Double): Double = putValue(key, value)
public fun MutableInt2DoubleMap.remove(key: Int): Double = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2DoubleMap.getOrPut(key: Int, defaultValue: () -> Double): Double {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    var value = getValue(key)
    if (value == this.defaultValue && !containsKey(key)) {
        value = defaultValue()
        putValue(key, value)
    }
    return value
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2DoubleMap.merge(key: Int, value: Double, merge: (oldValue: Double, value: Double) -> Double): Double {
    contract {
        callsInPlace(merge, InvocationKind.AT_MOST_ONCE)
    }

    val oldValue = getValue(key)
    val newValue = if (oldValue == defaultValue && !containsKey(key)) value else merge(oldValue, value)
    if (newValue != oldValue) {
        putValue(key, newValue)
    }
    return newValue
}

// TODO: abstract class w/toString
