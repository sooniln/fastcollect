@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator

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
 * A map of Longs to Ints.
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Long2IntMap has a [defaultValue] which is
 * returned to indicate no such key is present. In order to obtain the best performance, implementations and clients are
 * encouraged to ensure that the [defaultValue] is the value which is least likely to ever appear in the possible set of
 * values stored in this map. This is purely a performance and not a correctness concern however - the map will still
 * operate correctly and all methods will perform as expected even if the map contains values equal to [defaultValue].
 * [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if applicable.

 */
public interface Long2IntMap {

    public val defaultValue: Int


    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Long): Int

    public fun containsKey(key: Long): Boolean {
        for (k in keys) {
            if (k == key) return true
        }
        return false
    }

    public fun containsValue(value: Int): Boolean {
        for (v in values) {
            if (v == value) return true
        }
        return false
    }

    public val keys: LongSet
    public val values: IntCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): IntCollection = values

    public interface Entry {
        public val key: Long
        public val value: Int
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2IntMap.isDefaultValue(value: Int): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)


public fun  Long2IntMap.asMap(): Map<Long, Int> = Long2IntMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2IntMap.getOrDefault(key: Long, defaultValue: Int): Int = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2IntMap.getValue(key: Long): Int = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntMap.getOrElse(key: Long, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Int
}

/**
 * A mutable map of Longs to Ints.
 */
public interface MutableLong2IntMap : Long2IntMap {

    public fun put(key: Long, value: Int): Int

    public operator fun set(key: Long, value: Int) {
        put(key, value)
    }

    public fun remove(key: Long): Int

    public fun clear()

    override val keys: MutableLongSet
    override val values: MutableIntCollection

    public fun putAll(from: Long2IntMap) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Long, Int>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Long2IntMap.Entry {
        override var value: Int
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableLong2IntMap.asMutableMap(): MutableMap<Long, Int> = MutableLong2IntMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2IntMap.merge(key: Long, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue as Int, value)
    if (newValue != oldValue) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2IntMap.getOrPut(key: Long, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Int
    }
}

public abstract class AbstractLong2IntMap : Long2IntMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Map<*, *>) {
            if (other.size != size) return false

            for (entry in this) {
                if (other[entry.key] != entry.value) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        for (entry in this) {
            result += entry.key.hashCode() xor entry.value.hashCode()
        }
        return result
    }

    override fun toString(): String {
        return Iterable { iterator() }.joinToString(", ", "{", "}") { "${it.key}=${it.value}" }
    }

    public class SimpleEntry(override val key: Long, override val value: Int) : Long2IntMap.Entry
}

public abstract class AbstractMutableLong2IntMap : AbstractLong2IntMap(), MutableLong2IntMap


private object EmptyLong2IntMap : Long2IntMap {



    override val defaultValue: Int get() = Int.MIN_VALUE


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Int): Boolean = false
    override fun get(key: Long): Int = Int.MIN_VALUE



    override val keys: LongSet get() = emptyLongSet()

    override val values: IntCollection get() = emptyIntList()
    override fun iterator() = emptyFastIterator<Long2IntMap.Entry>()

}

private class SingletonLong2IntMap(private val key: Long, private val value: Int) : Long2IntMap {

    override val defaultValue: Int get() = Int.MIN_VALUE


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: Int): Boolean = value == this.value
    override fun get(key: Long): Int = if (key == this.key) value else Int.MIN_VALUE

    override val keys: LongSet by lazy { longSetOf(key) }


    override val values: IntCollection by lazy { intListOf(value) }


    override fun iterator() = object : FastIterator<Long2IntMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2IntMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractLong2IntMap.SimpleEntry(key, value)
        }
    }
}

private class Long2IntMapWrapper(private val map: Long2IntMap) : AbstractMap<Long, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Int? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Int>> = object : AbstractSet<Map.Entry<Long, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Int>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, Int>> = object : Iterator<Map.Entry<Long, Int>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, Int> {
                val entry = it.next()
                return object : Map.Entry<Long, Int> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableLong2IntMapWrapper(private val map: MutableLong2IntMap) : AbstractMutableMap<Long, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Int? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override fun remove(key: Long): Int? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: Int): Int? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Int>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, Int>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, Int>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, Int>> = object : MutableIterator<MutableMap.MutableEntry<Long, Int>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, Int> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Long, value: Int) : MutableMap.MutableEntry<Long, Int> {
            override var value = value
                private set

            override fun setValue(newValue: Int): Int {
                val oldValue = value
                if (map.put(key, newValue) != oldValue) throw ConcurrentModificationException()
                value = newValue
                return oldValue
            }

            override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
            override fun toString(): String = "$key=$value"
        }
    }
}
