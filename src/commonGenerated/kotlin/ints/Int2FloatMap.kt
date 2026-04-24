package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.Boxing.assertBoxing
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
            if (entry.value == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the extension get(key) method instead.",
        replaceWith = ReplaceWith("get(key)", "io.github.sooniln.fastcollect.ints.get"),
        level = DeprecationLevel.HIDDEN)
    override fun get(key: Int): Float? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Int): Float

    override val keys: IntSet

    override val values: FloatCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Int, Float>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Int, Float> {
        override val key: Int
        override val value: Float
    }
}

public operator fun Int2FloatMap.get(key: Int): Float = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Int2FloatMap.iterator(): Iterator<Int2FloatMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Int2FloatMap.getOrElse(key: Int, defaultValue: () -> Float): Float {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableInt2FloatMap : Int2FloatMap, MutableMap<Int, Float> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun put(key: Int, value: Float): Float? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Float): Float

    public operator fun set(key: Int, value: Float) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Int): Float? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Float

    override val keys: MutableIntSet
    override val values: MutableFloatCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Float>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Float>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Int2FloatMap.Entry, MutableMap.MutableEntry<Int, Float> {
        override val key: Int
        override val value: Float
    }
}

public fun MutableInt2FloatMap.put(key: Int, value: Float): Float = putValue(key, value)
public fun MutableInt2FloatMap.remove(key: Int): Float = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2FloatMap.getOrPut(key: Int, defaultValue: () -> Float): Float {
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
public inline fun MutableInt2FloatMap.merge(key: Int, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
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
