@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.emptyEntrySet
import io.github.sooniln.fastcollect.entrySetOf

import io.github.sooniln.fastcollect.ints.intListOf
import io.github.sooniln.fastcollect.ints.IntCollection
import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.emptyIntList

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2IntMap(): Long2IntMap = EmptyLong2IntMap as Long2IntMap

@Suppress("UNCHECKED_CAST")
public fun  long2IntMapOf(): Long2IntMap = EmptyLong2IntMap as Long2IntMap
public fun  long2IntMapOf(entry: Pair<Long, Int>): Long2IntMap = SingletonLong2IntMap(entry.first, entry.second)
public fun  long2IntMapOf(vararg entries: Pair<Long, Int>): Long2IntMap = Long2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2IntMapOf(): MutableLong2IntMap = Long2IntHashMap()
public fun  mutableLong2IntMapOf(entry: Pair<Long, Int>): MutableLong2IntMap = Long2IntHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableLong2IntMapOf(vararg entries: Pair<Long, Int>): MutableLong2IntMap = Long2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildLong2IntMap(expectedSize: Int = 0, builderAction: MutableLong2IntMap.() -> Unit): Long2IntMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2IntHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Ints which inherits from [Map].
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Long2IntMap has a
 * [defaultValue] which is returned instead to indicate no such key is present. Thus in order to obtain the best
 * performance implementations and clients are encouraged to ensure that the [defaultValue] is the value which is least
 * likely to ever appear in the possible set of values stored in this map. This is purely a performance and not a
 * correctness concern however - the map will still operate correctly and all methods will perform as expected even if
 * the map contains values equal to [defaultValue]. [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if
 * applicable.

 */
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


    public fun getOrDefault(key: Long, defaultValue: Int): Int = getOrElse(key) { defaultValue }


    /**
     * Returns the value associated with the given key, or [defaultValue] if the given key is not present in the map.
     */

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

    public operator fun iterator(): Iterator<Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2IntMap.isDefaultValue(value: Int): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2IntMap.getValue(key: Long): Int = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntMap.getOrElse(key: Long, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}


/**
 * A mutable map of Longs to Ints which inherits from [MutableMap].
 */
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



    /**
     * Updates the value associated with the given key and returns the previous value, or [defaultValue] if the given
     * key was not present previously.
     */

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


    public fun merge(key: Long, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
        val oldValue = lookup(key)
        val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
        if (newValue != oldValue) {
            putValue(key, newValue)
        }
        return newValue
    }


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
public inline fun  MutableLong2IntMap.getOrPut(key: Long, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = lookup(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        putValue(key, value)
    }
    return value
}


public abstract class AbstractLong2IntMap : Long2IntMap {

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

    public class SimpleEntry(private val _key: Long, private val _value: Int) : Long2IntMap.Entry {
        override fun key(): Long = _key
        override fun value(): Int = _value
    }
}

public abstract class AbstractMutableLong2IntMap : AbstractLong2IntMap(), MutableLong2IntMap {

    public class SimpleMutableEntry(private val _key: Long, private var _value: Int) : MutableLong2IntMap.MutableEntry {
        override fun key(): Long = _key
        override fun value(): Int = _value
        override fun setValue(newValue: Int): Int {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}


private object EmptyLong2IntMap : Long2IntMap {



    override val defaultValue: Int get() = Int.MIN_VALUE


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Int): Boolean = false
    override fun lookup(key: Long): Int = Int.MIN_VALUE



    override val keys: LongSet get() = emptyLongSet()

    override val values: IntCollection get() = emptyIntList()
    override val primitiveEntries: EntrySet<Long2IntMap.Entry> = emptyEntrySet()

}

private class SingletonLong2IntMap(private val key: Long, private val value: Int) : Long2IntMap {

    override val defaultValue: Int get() = Int.MIN_VALUE


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: Int): Boolean = value == this.value
    override fun lookup(key: Long): Int = if (key == this.key) value else Int.MIN_VALUE

    override val keys: LongSet by lazy { longSetOf(key) }


    override val values: IntCollection by lazy { intListOf(value) }


    override val primitiveEntries: EntrySet<Long2IntMap.Entry> by lazy { entrySetOf(AbstractLong2IntMap.SimpleEntry(key, value)) }
}
