package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.Boxing.assertBoxing
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
            if (entry.value == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the extension get(key) method instead.",
        replaceWith = ReplaceWith("get(key)", "io.github.sooniln.fastcollect.ints.get"),
        level = DeprecationLevel.HIDDEN)
    override fun get(key: Long): Int? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Long): Int

    override val keys: LongSet

    override val values: IntCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Long, Int>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Long, Int> {
        override val key: Long
        override val value: Int
    }
}

public operator fun Long2IntMap.get(key: Long): Int = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Long2IntMap.iterator(): Iterator<Long2IntMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Long2IntMap.getOrElse(key: Long, defaultValue: () -> Int): Int {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableLong2IntMap : Long2IntMap, MutableMap<Long, Int> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun put(key: Long, value: Int): Int? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Long, value: Int): Int

    public operator fun set(key: Long, value: Int) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Long): Int? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Int

    override val keys: MutableLongSet
    override val values: MutableIntCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Int>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Int>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Long2IntMap.Entry, MutableMap.MutableEntry<Long, Int> {
        override val key: Long
        override val value: Int
    }
}

public fun MutableLong2IntMap.put(key: Long, value: Int): Int = putValue(key, value)
public fun MutableLong2IntMap.remove(key: Long): Int = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2IntMap.getOrPut(key: Long, defaultValue: () -> Int): Int {
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
public inline fun MutableLong2IntMap.merge(key: Long, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
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
