package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.floats.FloatCollection
import io.github.sooniln.fastcollect.floats.MutableFloatCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Int2FloatMap : Map<Int, Float> {
    public val defaultValue: Float

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: Float): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the lookup(key) method instead.",
        replaceWith = ReplaceWith("lookup(key)"),
        level = DeprecationLevel.WARNING)
    override fun get(key: Int): Float? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun lookup(key: Int): Float

    override val keys: IntSet

    override val values: FloatCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, Float>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Int, Float> {
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
        override val value: Float get() {
            assertBoxing()
            return value()
        }

        public fun key(): Int
        public fun value(): Float
    }

    public operator fun iterator(): Iterator<Int2FloatMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Int2FloatMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Int2FloatMap.isDefaultValue(value: Float): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2FloatMap.getValue(key: Int): Float = getOrElse(key) { throw NoSuchElementException() }

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2FloatMap.getOrDefault(key: Int, defaultValue: Float): Float = getOrElse(key) { defaultValue }

@OptIn(ExperimentalContracts::class)
public inline fun Int2FloatMap.getOrElse(key: Int, defaultValue: () -> Float): Float {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Int2FloatMap.filterTo(destination: MutableInt2FloatMap, predicate: (key: Int, value: Float) -> Boolean): MutableInt2FloatMap {
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
public inline fun Int2FloatMap.filter(predicate: (key: Int, value: Float) -> Boolean): Int2FloatMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Int2FloatHashMap(), predicate)
}

public interface MutableInt2FloatMap : Int2FloatMap, MutableMap<Int, Float> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Int, value: Float): Float? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Float): Float

    public operator fun set(key: Int, value: Float) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Int): Float? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Float

    override val keys: MutableIntSet
    override val values: MutableFloatCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Float>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Float>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Int2FloatMap.Entry, MutableMap.MutableEntry<Int, Float>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2FloatMap.getOrPut(key: Int, defaultValue: () -> Float): Float {
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
public inline fun MutableInt2FloatMap.merge(key: Int, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
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
