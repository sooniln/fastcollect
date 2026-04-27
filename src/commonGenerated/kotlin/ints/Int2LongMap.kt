package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.longs.LongCollection
import io.github.sooniln.fastcollect.longs.MutableLongCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Int2LongMap : Map<Int, Long> {
    public val defaultValue: Long

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: Long): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the lookup(key) method instead.",
        replaceWith = ReplaceWith("lookup(key)"),
        level = DeprecationLevel.WARNING)
    override fun get(key: Int): Long? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun lookup(key: Int): Long

    override val keys: IntSet

    override val values: LongCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, Long>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Int, Long> {
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
        override val value: Long get() {
            assertBoxing()
            return value()
        }

        public fun key(): Int
        public fun value(): Long
    }

    public operator fun iterator(): Iterator<Int2LongMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Int2LongMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Int2LongMap.isDefaultValue(value: Long): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2LongMap.getValue(key: Int): Long = getOrElse(key) { throw NoSuchElementException() }

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2LongMap.getOrDefault(key: Int, defaultValue: Long): Long = getOrElse(key) { defaultValue }

@OptIn(ExperimentalContracts::class)
public inline fun Int2LongMap.getOrElse(key: Int, defaultValue: () -> Long): Long {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Int2LongMap.filterTo(destination: MutableInt2LongMap, predicate: (key: Int, value: Long) -> Boolean): MutableInt2LongMap {
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
public inline fun Int2LongMap.filter(predicate: (key: Int, value: Long) -> Boolean): Int2LongMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Int2LongHashMap(), predicate)
}

public interface MutableInt2LongMap : Int2LongMap, MutableMap<Int, Long> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Int, value: Long): Long? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Long): Long

    public operator fun set(key: Int, value: Long) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Int): Long? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Long

    override val keys: MutableIntSet
    override val values: MutableLongCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Long>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Long>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Int2LongMap.Entry, MutableMap.MutableEntry<Int, Long>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2LongMap.getOrPut(key: Int, defaultValue: () -> Long): Long {
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
public inline fun MutableInt2LongMap.merge(key: Int, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
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
