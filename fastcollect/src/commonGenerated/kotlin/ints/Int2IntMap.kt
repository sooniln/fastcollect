@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator

import io.github.sooniln.fastcollect.ints.intListOf
import io.github.sooniln.fastcollect.ints.IntCollection
import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.emptyIntList
import io.github.sooniln.fastcollect.toBits

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2IntMap(): Int2IntMap = EmptyInt2IntMap as Int2IntMap

@Suppress("UNCHECKED_CAST")
public fun  int2IntMapOf(): Int2IntMap = EmptyInt2IntMap as Int2IntMap
public fun  int2IntMapOf(entry: Pair<Int, Int>): Int2IntMap = SingletonInt2IntMap(entry.first, entry.second)
public fun  int2IntMapOf(vararg entries: Pair<Int, Int>): Int2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2IntMapOf(): MutableInt2IntMap = Int2IntHashMap()
public fun  mutableInt2IntMapOf(entry: Pair<Int, Int>): MutableInt2IntMap = Int2IntHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2IntMapOf(vararg entries: Pair<Int, Int>): MutableInt2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2IntMap(expectedSize: Int = 0, builderAction: MutableInt2IntMap.() -> Unit): Int2IntMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2IntHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Ints.
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Int2IntMap has a [defaultValue] which is
 * returned to indicate no such key is present. In order to obtain the best performance, implementations and clients are
 * encouraged to ensure that the [defaultValue] is the value which is least likely to ever appear in the possible set of
 * values stored in this map. This is purely a performance and not a correctness concern however - the map will still
 * operate correctly and all methods will perform as expected even if the map contains values equal to [defaultValue].
 * [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if applicable.

 */
public interface Int2IntMap {

    public val defaultValue: Int


    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Int): Int

    public fun containsKey(key: Int): Boolean {
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

    public val keys: IntSet
    public val values: IntCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): IntCollection = values

    public interface Entry {
        public val key: Int
        public val value: Int

        public operator fun component1(): Int = key
        public operator fun component2(): Int = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2IntMap.isDefaultValue(value: Int): Boolean = value.toBits() == defaultValue.toBits()


public fun  Int2IntMap.asMap(): Map<Int, Int> = Int2IntMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2IntMap.getOrDefault(key: Int, defaultValue: Int): Int = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2IntMap.getValue(key: Int): Int = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntMap.getOrElse(key: Int, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Int
}

/**
 * A mutable map of Ints to Ints.
 */
public interface MutableInt2IntMap : Int2IntMap {

    public fun put(key: Int, value: Int): Int

    public operator fun set(key: Int, value: Int) {
        put(key, value)
    }

    public fun remove(key: Int): Int

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableIntCollection

    public fun putAll(from: Int2IntMap) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Int>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Int2IntMap.Entry {
        override var value: Int
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableInt2IntMap.asMutableMap(): MutableMap<Int, Int> = MutableInt2IntMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2IntMap.merge(key: Int, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
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
public inline fun  MutableInt2IntMap.getOrPut(key: Int, defaultValue: () -> Int): Int {
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

public abstract class AbstractInt2IntMap : Int2IntMap {

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

    public class SimpleEntry(override val key: Int, override val value: Int) : Int2IntMap.Entry
}

public abstract class AbstractMutableInt2IntMap : AbstractInt2IntMap(), MutableInt2IntMap


private object EmptyInt2IntMap : Int2IntMap {



    override val defaultValue: Int get() = Int.MIN_VALUE


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Int): Boolean = false
    override fun get(key: Int): Int = Int.MIN_VALUE



    override val keys: IntSet get() = emptyIntSet()

    override val values: IntCollection get() = emptyIntList()
    override fun iterator() = emptyFastIterator<Int2IntMap.Entry>()

}

private class SingletonInt2IntMap(private val key: Int, private val value: Int) : Int2IntMap {

    override val defaultValue: Int get() = Int.MIN_VALUE


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: Int): Boolean = value == this.value
    override fun get(key: Int): Int = if (key == this.key) value else Int.MIN_VALUE

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: IntCollection by lazy { intListOf(value) }


    override fun iterator() = object : FastIterator<Int2IntMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2IntMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2IntMap.SimpleEntry(key, value)
        }
    }
}

private class Int2IntMapWrapper(private val map: Int2IntMap) : AbstractMap<Int, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Int? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Int>> = object : AbstractSet<Map.Entry<Int, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Int>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Int>> = object : Iterator<Map.Entry<Int, Int>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Int> {
                val entry = it.next()
                return object : Map.Entry<Int, Int> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableInt2IntMapWrapper(private val map: MutableInt2IntMap) : AbstractMutableMap<Int, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Int? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override fun remove(key: Int): Int? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Int): Int? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Int>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Int>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Int>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Int>> = object : MutableIterator<MutableMap.MutableEntry<Int, Int>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Int> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Int, value: Int) : MutableMap.MutableEntry<Int, Int> {
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
