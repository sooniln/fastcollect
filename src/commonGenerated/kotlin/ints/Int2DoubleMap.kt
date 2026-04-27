package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
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
            if (entry.value() == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the lookup(key) method instead.",
        replaceWith = ReplaceWith("lookup(key)"),
        level = DeprecationLevel.WARNING)
    override fun get(key: Int): Double? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun lookup(key: Int): Double

    override val keys: IntSet

    override val values: DoubleCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, Double>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Int, Double> {
        @Deprecated(
            message = "Use key() instead.",
            replaceWith = ReplaceWith("key()"),
            level = DeprecationLevel.WARNING)
        override val key: Int get() {
            assertBoxing()
            return key()
        }

        @Deprecated(
            message = "Use value() instead.",
            replaceWith = ReplaceWith("key()"),
            level = DeprecationLevel.WARNING)
        override val value: Double get() {
            assertBoxing()
            return value()
        }

        public fun key(): Int
        public fun value(): Double
    }

    public operator fun iterator(): Iterator<Int2DoubleMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Int2DoubleMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Int2DoubleMap.isDefaultValue(value: Double): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2DoubleMap.getValue(key: Int): Double = getOrElse(key) { throw NoSuchElementException() }

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2DoubleMap.getOrDefault(key: Int, defaultValue: Double): Double = getOrElse(key) { defaultValue }

@OptIn(ExperimentalContracts::class)
public inline fun Int2DoubleMap.getOrElse(key: Int, defaultValue: () -> Double): Double {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Int2DoubleMap.filterTo(destination: MutableInt2DoubleMap, predicate: (key: Int, value: Double) -> Boolean): MutableInt2DoubleMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    for (entry in primitiveEntries.fastIterator()) {
        if (predicate(entry.key(), entry.value())) {
            destination.putValue(entry.key(), entry.value())
        }
    }
    return destination
}

@OptIn(ExperimentalContracts::class)
public inline fun Int2DoubleMap.filter(predicate: (key: Int, value: Double) -> Boolean): Int2DoubleMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Int2DoubleHashMap(), predicate)
}

public interface MutableInt2DoubleMap : Int2DoubleMap, MutableMap<Int, Double> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Int, value: Double): Double? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Double): Double

    public operator fun set(key: Int, value: Double) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Int): Double? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Double

    override val keys: MutableIntSet
    override val values: MutableDoubleCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Double>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Double>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Int2DoubleMap.Entry, MutableMap.MutableEntry<Int, Double>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2DoubleMap.getOrPut(key: Int, defaultValue: () -> Double): Double {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    var value = lookup(key)
    if (isDefaultValue(value) && !containsKey(key)) {
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

    val oldValue = lookup(key)
    val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
    if (newValue != oldValue) {
        putValue(key, newValue)
    }
    return newValue
}

// TODO: abstract class w/toString
