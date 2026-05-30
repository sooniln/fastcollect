@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator

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
 * A map of Longs to Longs.
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Long2LongMap has a [defaultValue] which is
 * returned to indicate no such key is present. In order to obtain the best performance, implementations and clients are
 * encouraged to ensure that the [defaultValue] is the value which is least likely to ever appear in the possible set of
 * values stored in this map. This is purely a performance and not a correctness concern however - the map will still
 * operate correctly and all methods will perform as expected even if the map contains values equal to [defaultValue].
 * [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if applicable.

 */
public interface Long2LongMap {

    public val defaultValue: Long


    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Long): Long

    public fun containsKey(key: Long): Boolean {
        for (k in keys) {
            if (k == key) return true
        }
        return false
    }

    public fun containsValue(value: Long): Boolean {
        for (v in values) {
            if (v == value) return true
        }
        return false
    }

    public val keys: LongSet
    public val values: LongCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): LongCollection = values

    public interface Entry {
        public val key: Long
        public val value: Long
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2LongMap.isDefaultValue(value: Long): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)


public fun  Long2LongMap.asMap(): Map<Long, Long> = Long2LongMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2LongMap.getOrDefault(key: Long, defaultValue: Long): Long = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun  Long2LongMap.getValue(key: Long): Long = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongMap.getOrElse(key: Long, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Long
}

/**
 * A mutable map of Longs to Longs.
 */
public interface MutableLong2LongMap : Long2LongMap {

    public fun put(key: Long, value: Long): Long

    public operator fun set(key: Long, value: Long) {
        put(key, value)
    }

    public fun remove(key: Long): Long

    public fun clear()

    override val keys: MutableLongSet
    override val values: MutableLongCollection

    public fun putAll(from: Long2LongMap) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Long, Long>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Long2LongMap.Entry {
        override var value: Long
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableLong2LongMap.asMutableMap(): MutableMap<Long, Long> = MutableLong2LongMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2LongMap.merge(key: Long, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue as Long, value)
    if (newValue != oldValue) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2LongMap.getOrPut(key: Long, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Long
    }
}

public abstract class AbstractLong2LongMap : Long2LongMap {

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

    public class SimpleEntry(override val key: Long, override val value: Long) : Long2LongMap.Entry
}

public abstract class AbstractMutableLong2LongMap : AbstractLong2LongMap(), MutableLong2LongMap


private object EmptyLong2LongMap : Long2LongMap {



    override val defaultValue: Long get() = Long.MIN_VALUE


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Long): Boolean = false
    override fun get(key: Long): Long = Long.MIN_VALUE



    override val keys: LongSet get() = emptyLongSet()

    override val values: LongCollection get() = emptyLongList()
    override fun iterator() = emptyFastIterator<Long2LongMap.Entry>()

}

private class SingletonLong2LongMap(private val key: Long, private val value: Long) : Long2LongMap {

    override val defaultValue: Long get() = Long.MIN_VALUE


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: Long): Boolean = value == this.value
    override fun get(key: Long): Long = if (key == this.key) value else Long.MIN_VALUE

    override val keys: LongSet by lazy { longSetOf(key) }


    override val values: LongCollection by lazy { longListOf(value) }


    override fun iterator() = object : FastIterator<Long2LongMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2LongMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractLong2LongMap.SimpleEntry(key, value)
        }
    }
}

private class Long2LongMapWrapper(private val map: Long2LongMap) : AbstractMap<Long, Long>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Long? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Long): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Long>> = object : AbstractSet<Map.Entry<Long, Long>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Long>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, Long>> = object : Iterator<Map.Entry<Long, Long>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, Long> {
                val entry = it.next()
                return object : Map.Entry<Long, Long> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableLong2LongMapWrapper(private val map: MutableLong2LongMap) : AbstractMutableMap<Long, Long>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Long? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Long): Boolean = map.containsValue(value)

    override fun remove(key: Long): Long? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: Long): Long? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Long>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, Long>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, Long>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, Long>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, Long>> = object : MutableIterator<MutableMap.MutableEntry<Long, Long>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, Long> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Long, value: Long) : MutableMap.MutableEntry<Long, Long> {
            override var value = value
                private set

            override fun setValue(newValue: Long): Long {
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
