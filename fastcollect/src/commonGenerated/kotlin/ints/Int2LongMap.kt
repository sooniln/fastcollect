@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

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
public fun  emptyInt2LongMap(): Int2LongMap = EmptyInt2LongMap as Int2LongMap

@Suppress("UNCHECKED_CAST")
public fun  int2LongMapOf(): Int2LongMap = EmptyInt2LongMap as Int2LongMap
public fun  int2LongMapOf(entry: Pair<Int, Long>): Int2LongMap = SingletonInt2LongMap(entry.first, entry.second)
public fun  int2LongMapOf(vararg entries: Pair<Int, Long>): Int2LongMap = Int2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2LongMapOf(): MutableInt2LongMap = Int2LongHashMap()
public fun  mutableInt2LongMapOf(entry: Pair<Int, Long>): MutableInt2LongMap = Int2LongHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2LongMapOf(vararg entries: Pair<Int, Long>): MutableInt2LongMap = Int2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2LongMap(expectedSize: Int = 0, builderAction: MutableInt2LongMap.() -> Unit): Int2LongMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2LongHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Longs.
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Int2LongMap has a [defaultValue] which is
 * returned to indicate no such key is present. In order to obtain the best performance, implementations and clients are
 * encouraged to ensure that the [defaultValue] is the value which is least likely to ever appear in the possible set of
 * values stored in this map. This is purely a performance and not a correctness concern however - the map will still
 * operate correctly and all methods will perform as expected even if the map contains values equal to [defaultValue].
 * [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if applicable.

 */
public interface Int2LongMap {

    public val defaultValue: Long


    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Int): Long

    public fun containsKey(key: Int): Boolean {
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

    public val keys: IntSet
    public val values: LongCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): LongCollection = values

    public interface Entry {
        public val key: Int
        public val value: Long
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2LongMap.isDefaultValue(value: Long): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)


public fun  Int2LongMap.asMap(): Map<Int, Long> = Int2LongMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2LongMap.getOrDefault(key: Int, defaultValue: Long): Long = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2LongMap.getValue(key: Int): Long = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongMap.getOrElse(key: Int, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Long
}

/**
 * A mutable map of Ints to Longs.
 */
public interface MutableInt2LongMap : Int2LongMap {

    public fun put(key: Int, value: Long): Long

    public operator fun set(key: Int, value: Long) {
        put(key, value)
    }

    public fun remove(key: Int): Long

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableLongCollection

    public fun putAll(from: Int2LongMap) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Long>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Int2LongMap.Entry {
        override var value: Long
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableInt2LongMap.asMutableMap(): MutableMap<Int, Long> = MutableInt2LongMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2LongMap.merge(key: Int, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
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
public inline fun  MutableInt2LongMap.getOrPut(key: Int, defaultValue: () -> Long): Long {
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

public abstract class AbstractInt2LongMap : Int2LongMap {

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

    public class SimpleEntry(override val key: Int, override val value: Long) : Int2LongMap.Entry
}

public abstract class AbstractMutableInt2LongMap : AbstractInt2LongMap(), MutableInt2LongMap


private object EmptyInt2LongMap : Int2LongMap {



    override val defaultValue: Long get() = Long.MIN_VALUE


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Long): Boolean = false
    override fun get(key: Int): Long = Long.MIN_VALUE



    override val keys: IntSet get() = emptyIntSet()

    override val values: LongCollection get() = emptyLongList()
    override fun iterator() = emptyFastIterator<Int2LongMap.Entry>()

}

private class SingletonInt2LongMap(private val key: Int, private val value: Long) : Int2LongMap {

    override val defaultValue: Long get() = Long.MIN_VALUE


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: Long): Boolean = value == this.value
    override fun get(key: Int): Long = if (key == this.key) value else Long.MIN_VALUE

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: LongCollection by lazy { longListOf(value) }


    override fun iterator() = object : FastIterator<Int2LongMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2LongMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2LongMap.SimpleEntry(key, value)
        }
    }
}

private class Int2LongMapWrapper(private val map: Int2LongMap) : AbstractMap<Int, Long>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Long? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Long): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Long>> = object : AbstractSet<Map.Entry<Int, Long>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Long>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Long>> = object : Iterator<Map.Entry<Int, Long>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Long> {
                val entry = it.next()
                return object : Map.Entry<Int, Long> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableInt2LongMapWrapper(private val map: MutableInt2LongMap) : AbstractMutableMap<Int, Long>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Long? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Long): Boolean = map.containsValue(value)

    override fun remove(key: Int): Long? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Long): Long? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Long>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Long>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Long>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Long>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Long>> = object : MutableIterator<MutableMap.MutableEntry<Int, Long>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Long> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Int, value: Long) : MutableMap.MutableEntry<Int, Long> {
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
