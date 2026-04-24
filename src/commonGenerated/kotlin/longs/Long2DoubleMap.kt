package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.doubles.DoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Long2DoubleMap : Map<Long, Double> {
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
    override fun get(key: Long): Double? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Long): Double

    override val keys: LongSet

    override val values: DoubleCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Long, Double>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Long, Double> {
        override val key: Long
        override val value: Double
    }
}

public operator fun Long2DoubleMap.get(key: Long): Double = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Long2DoubleMap.iterator(): Iterator<Long2DoubleMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Long2DoubleMap.getOrElse(key: Long, defaultValue: () -> Double): Double {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableLong2DoubleMap : Long2DoubleMap, MutableMap<Long, Double> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun put(key: Long, value: Double): Double? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Long, value: Double): Double

    public operator fun set(key: Long, value: Double) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Long): Double? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Double

    override val keys: MutableLongSet
    override val values: MutableDoubleCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Double>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Double>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Long2DoubleMap.Entry, MutableMap.MutableEntry<Long, Double> {
        override val key: Long
        override val value: Double
    }
}

public fun MutableLong2DoubleMap.put(key: Long, value: Double): Double = putValue(key, value)
public fun MutableLong2DoubleMap.remove(key: Long): Double = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2DoubleMap.getOrPut(key: Long, defaultValue: () -> Double): Double {
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
public inline fun MutableLong2DoubleMap.merge(key: Long, value: Double, merge: (oldValue: Double, value: Double) -> Double): Double {
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
