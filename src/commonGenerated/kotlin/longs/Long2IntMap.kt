package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.ints.IntCollection
import io.github.sooniln.fastcollect.ints.MutableIntCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Long2IntMap : Map<Long, Int> {
    public val defaultValue: Int

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: Int): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the lookup(key) method instead.",
        replaceWith = ReplaceWith("lookup(key)"),
        level = DeprecationLevel.WARNING)
    override fun get(key: Long): Int? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun lookup(key: Long): Int

    override val keys: LongSet

    override val values: IntCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Long, Int>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Long, Int> {
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
        override val value: Int get() {
            assertBoxing()
            return value()
        }

        public fun key(): Long
        public fun value(): Int
    }

    public operator fun iterator(): Iterator<Long2IntMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Long2IntMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Long2IntMap.isDefaultValue(value: Int): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Long2IntMap.getValue(key: Long): Int = getOrElse(key) { throw NoSuchElementException() }

@Suppress("NOTHING_TO_INLINE")
public inline fun Long2IntMap.getOrDefault(key: Long, defaultValue: Int): Int = getOrElse(key) { defaultValue }

@OptIn(ExperimentalContracts::class)
public inline fun Long2IntMap.getOrElse(key: Long, defaultValue: () -> Int): Int {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Long2IntMap.filterTo(destination: MutableLong2IntMap, predicate: (key: Long, value: Int) -> Boolean): MutableLong2IntMap {
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
public inline fun Long2IntMap.filter(predicate: (key: Long, value: Int) -> Boolean): Long2IntMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Long2IntHashMap(), predicate)
}

public interface MutableLong2IntMap : Long2IntMap, MutableMap<Long, Int> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Long, value: Int): Int? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Long, value: Int): Int

    public operator fun set(key: Long, value: Int) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Long): Int? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Int

    override val keys: MutableLongSet
    override val values: MutableIntCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Int>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Int>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Long2IntMap.Entry, MutableMap.MutableEntry<Long, Int>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2IntMap.getOrPut(key: Long, defaultValue: () -> Int): Int {
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
public inline fun MutableLong2IntMap.merge(key: Long, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
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
