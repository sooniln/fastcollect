package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.Boxing.assertBoxing
import io.github.sooniln.fastcollect.floats.FloatCollection
import io.github.sooniln.fastcollect.floats.MutableFloatCollection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface Long2FloatMap : Map<Long, Float> {
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
    override fun get(key: Long): Float? {
        assertBoxing()
        val value = getValue(key)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun getValue(key: Long): Float

    override val keys: LongSet

    override val values: FloatCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.HIDDEN)
    override val entries: Set<Map.Entry<Long, Float>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: Set<Entry>

    public interface Entry : Map.Entry<Long, Float> {
        override val key: Long
        override val value: Float
    }
}

public operator fun Long2FloatMap.get(key: Long): Float = getValue(key)

@Suppress("NOTHING_TO_INLINE")
public inline operator fun Long2FloatMap.iterator(): Iterator<Long2FloatMap.Entry> = primitiveEntries.iterator()

@OptIn(ExperimentalContracts::class)
public inline fun Long2FloatMap.getOrElse(key: Long, defaultValue: () -> Float): Float {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = getValue(key)
    return if (value == this.defaultValue && !containsKey(key)) defaultValue() else value
}

public interface MutableLong2FloatMap : Long2FloatMap, MutableMap<Long, Float> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun put(key: Long, value: Float): Float? {
        assertBoxing()
        val value = putValue(key, value)
        return if (value == defaultValue && !containsKey(key)) null else value
    }

    public fun putValue(key: Long, value: Float): Float

    public operator fun set(key: Long, value: Float) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.HIDDEN)
    override fun remove(key: Long): Float? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Float

    override val keys: MutableLongSet
    override val values: MutableFloatCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(message = "Use primitiveEntries instead.", replaceWith = ReplaceWith("primitiveEntries"), level = DeprecationLevel.HIDDEN)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Float>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Float>>
        }

    override val primitiveEntries: MutableSet<MutableEntry>

    public interface MutableEntry : Long2FloatMap.Entry, MutableMap.MutableEntry<Long, Float> {
        override val key: Long
        override val value: Float
    }
}

public fun MutableLong2FloatMap.put(key: Long, value: Float): Float = putValue(key, value)
public fun MutableLong2FloatMap.remove(key: Long): Float = removeKey(key)

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2FloatMap.getOrPut(key: Long, defaultValue: () -> Float): Float {
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
public inline fun MutableLong2FloatMap.merge(key: Long, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
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
