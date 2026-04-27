package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.longs.LongCollection
import io.github.sooniln.fastcollect.longs.MutableLongCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Long2LongMap : Map<Long, Long> {
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
    override fun get(key: Long): Long? {
        assertBoxing()
        val value = lookup(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun lookup(key: Long): Long

    override val keys: LongSet

    override val values: LongCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Long, Long>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Long, Long> {
        @Deprecated(
            message = "Use key() instead.",
            replaceWith = ReplaceWith("key()"),
            level = DeprecationLevel.WARNING)
        override val key: Long get() {
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

        public fun key(): Long
        public fun value(): Long
    }

    public operator fun iterator(): Iterator<Long2LongMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Long2LongMap.Entry> = primitiveEntries.fastIterator()
}

@Suppress("NOTHING_TO_INLINE")
public inline fun Long2LongMap.getValue(key: Long): Long = getOrElse(key) { throw NoSuchElementException() }

@Suppress("NOTHING_TO_INLINE")
public inline fun Long2LongMap.getOrDefault(key: Long, defaultValue: Long): Long = getOrElse(key) { defaultValue }

@OptIn(ExperimentalContracts::class)
public inline fun Long2LongMap.getOrElse(key: Long, defaultValue: () -> Long): Long {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Long2LongMap.filterTo(destination: MutableLong2LongMap, predicate: (key: Long, value: Long) -> Boolean): MutableLong2LongMap {
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
public inline fun Long2LongMap.filter(predicate: (key: Long, value: Long) -> Boolean): Long2LongMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Long2LongHashMap(), predicate)
}

public interface MutableLong2LongMap : Long2LongMap, MutableMap<Long, Long> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Long, value: Long): Long? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Long, value: Long): Long

    public operator fun set(key: Long, value: Long) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Long): Long? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Long

    override val keys: MutableLongSet
    override val values: MutableLongCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Long>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Long>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Long2LongMap.Entry, MutableMap.MutableEntry<Long, Long>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2LongMap.getOrPut(key: Long, defaultValue: () -> Long): Long {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    var value = lookup(key)
    if (value == this.defaultValue && !containsKey(key)) {
        value = defaultValue()
        putValue(key, value)
    }
    return value
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2LongMap.merge(key: Long, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
    contract {
        callsInPlace(merge, InvocationKind.AT_MOST_ONCE)
    }

    val oldValue = lookup(key)
    val newValue = if (oldValue == defaultValue && !containsKey(key)) value else merge(oldValue, value)
    if (newValue != oldValue) {
        putValue(key, newValue)
    }
    return newValue
}

// TODO: abstract class w/toString
