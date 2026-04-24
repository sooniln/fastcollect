package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.Boxing.assertBoxing
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
            if (entry.value == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the extension get(key) method instead.",
        replaceWith = ReplaceWith("get(key)", "io.github.sooniln.fastcollect.ints.get"),
        level = DeprecationLevel.HIDDEN)
    override fun get(key: Long): Long? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Long): Long

    override val keys: LongSet

    override val values: LongCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Long, Long>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Long, Long> {
        override val key: Long
        override val value: Long
    }
}

public operator fun Long2LongMap.get(key: Long): Long = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Long2LongMap.iterator(): Iterator<Long2LongMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Long2LongMap.getOrElse(key: Long, defaultValue: () -> Long): Long {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableLong2LongMap : Long2LongMap, MutableMap<Long, Long> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
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
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Long): Long? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Long

    override val keys: MutableLongSet
    override val values: MutableLongCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Long>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Long>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Long2LongMap.Entry, MutableMap.MutableEntry<Long, Long> {
        override val key: Long
        override val value: Long
    }
}

public fun MutableLong2LongMap.put(key: Long, value: Long): Long = putValue(key, value)
public fun MutableLong2LongMap.remove(key: Long): Long = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2LongMap.getOrPut(key: Long, defaultValue: () -> Long): Long {
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
public inline fun MutableLong2LongMap.merge(key: Long, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
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
