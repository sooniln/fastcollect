package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
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
            if (entry.value == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the extension get(key) method instead.",
        replaceWith = ReplaceWith("get(key)", "io.github.sooniln.fastcollect.ints.get"),
        level = DeprecationLevel.HIDDEN)
    override fun get(key: Int): Int? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Int): Int

    override val keys: IntSet

    override val values: IntCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Int, Int>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Int, Int> {
        override val key: Int
        override val value: Int
    }
}

public operator fun Int2IntMap.get(key: Int): Int = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Int2IntMap.iterator(): Iterator<Int2IntMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Int2IntMap.getOrElse(key: Int, defaultValue: () -> Int): Int {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableInt2IntMap : Int2IntMap, MutableMap<Int, Int> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun put(key: Int, value: Int): Int? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Int): Int

    public operator fun set(key: Int, value: Int) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Int): Int? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Int

    override val keys: MutableIntSet
    override val values: MutableIntCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Int>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Int>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Int2IntMap.Entry, MutableMap.MutableEntry<Int, Int> {
        override val key: Int
        override val value: Int
    }
}

public fun MutableInt2IntMap.put(key: Int, value: Int): Int = putValue(key, value)
public fun MutableInt2IntMap.remove(key: Int): Int = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2IntMap.getOrPut(key: Int, defaultValue: () -> Int): Int {
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
public inline fun MutableInt2IntMap.merge(key: Int, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
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
