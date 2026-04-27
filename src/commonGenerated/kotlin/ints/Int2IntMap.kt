package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.ints.IntCollection
import io.github.sooniln.fastcollect.ints.MutableIntCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Int2IntMap : Map<Int, Int> {
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
    override fun get(key: Int): Int? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun lookup(key: Int): Int

    override val keys: IntSet

    override val values: IntCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, Int>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Int, Int> {
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
        override val value: Int get() {
            assertBoxing()
            return value()
        }

        public fun key(): Int
        public fun value(): Int
    }

    public operator fun iterator(): Iterator<Int2IntMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Int2IntMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Int2IntMap.isDefaultValue(value: Int): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2IntMap.getValue(key: Int): Int = getOrElse(key) { throw NoSuchElementException() }

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2IntMap.getOrDefault(key: Int, defaultValue: Int): Int = getOrElse(key) { defaultValue }

@OptIn(ExperimentalContracts::class)
public inline fun Int2IntMap.getOrElse(key: Int, defaultValue: () -> Int): Int {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Int2IntMap.filterTo(destination: MutableInt2IntMap, predicate: (key: Int, value: Int) -> Boolean): MutableInt2IntMap {
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
public inline fun Int2IntMap.filter(predicate: (key: Int, value: Int) -> Boolean): Int2IntMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Int2IntHashMap(), predicate)
}

public interface MutableInt2IntMap : Int2IntMap, MutableMap<Int, Int> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Int, value: Int): Int? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Int): Int

    public operator fun set(key: Int, value: Int) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Int): Int? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Int

    override val keys: MutableIntSet
    override val values: MutableIntCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Int>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Int>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Int2IntMap.Entry, MutableMap.MutableEntry<Int, Int>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2IntMap.getOrPut(key: Int, defaultValue: () -> Int): Int {
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
public inline fun MutableInt2IntMap.merge(key: Int, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
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
