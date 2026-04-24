package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
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
            if (entry.value == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the extension get(key) method instead.",
        replaceWith = ReplaceWith("get(key)", "io.github.sooniln.fastcollect.ints.get"),
        level = DeprecationLevel.HIDDEN)
    override fun get(key: Int): Long? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Int): Long

    override val keys: IntSet

    override val values: LongCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Int, Long>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Int, Long> {
        override val key: Int
        override val value: Long
    }
}

public operator fun Int2LongMap.get(key: Int): Long = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Int2LongMap.iterator(): Iterator<Int2LongMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Int2LongMap.getOrElse(key: Int, defaultValue: () -> Long): Long {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableInt2LongMap : Int2LongMap, MutableMap<Int, Long> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun put(key: Int, value: Long): Long? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Long): Long

    public operator fun set(key: Int, value: Long) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Int): Long? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Long

    override val keys: MutableIntSet
    override val values: MutableLongCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Long>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Long>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Int2LongMap.Entry, MutableMap.MutableEntry<Int, Long> {
        override val key: Int
        override val value: Long
    }
}

public fun MutableInt2LongMap.put(key: Int, value: Long): Long = putValue(key, value)
public fun MutableInt2LongMap.remove(key: Int): Long = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2LongMap.getOrPut(key: Int, defaultValue: () -> Long): Long {
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
public inline fun MutableInt2LongMap.merge(key: Int, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
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
