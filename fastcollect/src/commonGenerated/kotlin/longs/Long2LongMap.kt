@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.emptyEntrySet
import io.github.sooniln.fastcollect.entrySetOf

import io.github.sooniln.fastcollect.longs.longListOf
import io.github.sooniln.fastcollect.longs.LongCollection
import io.github.sooniln.fastcollect.longs.MutableLongCollection
import io.github.sooniln.fastcollect.longs.emptyLongList

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2LongMap(): Long2LongMap = EmptyLong2LongMap as Long2LongMap

@Suppress("UNCHECKED_CAST")
public fun  long2LongMapOf(): Long2LongMap = EmptyLong2LongMap as Long2LongMap
public fun  long2LongMapOf(entry: Pair<Long, Long>): Long2LongMap = SingletonLong2LongMap(entry.first, entry.second)
public fun  long2LongMapOf(vararg entries: Pair<Long, Long>): Long2LongMap = Long2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2LongMapOf(): MutableLong2LongMap = Long2LongHashMap()
public fun  mutableLong2LongMapOf(entry: Pair<Long, Long>): MutableLong2LongMap = Long2LongHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableLong2LongMapOf(vararg entries: Pair<Long, Long>): MutableLong2LongMap = Long2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildLong2LongMap(expectedSize: Int = 0, builderAction: MutableLong2LongMap.() -> Unit): Long2LongMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2LongHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Longs which inherits from [Map].
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Long2LongMap has a
 * [defaultValue] which is returned instead to indicate no such key is present. Thus in order to obtain the best
 * performance implementations and clients are encouraged to ensure that the [defaultValue] is the value which is least
 * likely to ever appear in the possible set of values stored in this map. This is purely a performance and not a
 * correctness concern however - the map will still operate correctly and all methods will perform as expected even if
 * the map contains values equal to [defaultValue]. [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if
 * applicable.

 */
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
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }


    public fun getOrDefault(key: Long, defaultValue: Long): Long = getOrElse(key) { defaultValue }


    /**
     * Returns the value associated with the given key, or [defaultValue] if the given key is not present in the map.
     */

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

    public operator fun iterator(): Iterator<Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2LongMap.isDefaultValue(value: Long): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2LongMap.getValue(key: Long): Long = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongMap.getOrElse(key: Long, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}


/**
 * A mutable map of Longs to Longs which inherits from [MutableMap].
 */
public interface MutableLong2LongMap : Long2LongMap, MutableMap<Long, Long> {


    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Long, value: Long): Long? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }



    /**
     * Updates the value associated with the given key and returns the previous value, or [defaultValue] if the given
     * key was not present previously.
     */

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


    public fun merge(key: Long, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
        val oldValue = lookup(key)
        val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
        if (newValue != oldValue) {
            putValue(key, newValue)
        }
        return newValue
    }


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
public inline fun  MutableLong2LongMap.getOrPut(key: Long, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = lookup(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        putValue(key, value)
    }
    return value
}


public abstract class AbstractLong2LongMap : Long2LongMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Map<*, *>) {
            if (other.size != size) return false

            for (entry in fastIterator()) {
                if (other[entry.key()] != entry.value()) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        for (entry in fastIterator()) {
            result += entry.key().hashCode() xor entry.value().hashCode()
        }
        return result
    }

    override fun toString(): String {
        return Iterable { fastIterator() }.joinToString(", ", "{", "}") { "${it.key()}=${it.value()}" }
    }

    public class SimpleEntry(private val _key: Long, private val _value: Long) : Long2LongMap.Entry {
        override fun key(): Long = _key
        override fun value(): Long = _value
    }
}

public abstract class AbstractMutableLong2LongMap : AbstractLong2LongMap(), MutableLong2LongMap {

    public class SimpleMutableEntry(private val _key: Long, private var _value: Long) : MutableLong2LongMap.MutableEntry {
        override fun key(): Long = _key
        override fun value(): Long = _value
        override fun setValue(newValue: Long): Long {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}


private object EmptyLong2LongMap : Long2LongMap {



    override val defaultValue: Long get() = Long.MIN_VALUE


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Long): Boolean = false
    override fun lookup(key: Long): Long = Long.MIN_VALUE



    override val keys: LongSet get() = emptyLongSet()

    override val values: LongCollection get() = emptyLongList()
    override val primitiveEntries: EntrySet<Long2LongMap.Entry> = emptyEntrySet()

}

private class SingletonLong2LongMap(private val key: Long, private val value: Long) : Long2LongMap {

    override val defaultValue: Long get() = Long.MIN_VALUE


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: Long): Boolean = value == this.value
    override fun lookup(key: Long): Long = if (key == this.key) value else Long.MIN_VALUE

    override val keys: LongSet by lazy { longSetOf(key) }


    override val values: LongCollection by lazy { longListOf(value) }


    override val primitiveEntries: EntrySet<Long2LongMap.Entry> by lazy { entrySetOf(AbstractLong2LongMap.SimpleEntry(key, value)) }
}
