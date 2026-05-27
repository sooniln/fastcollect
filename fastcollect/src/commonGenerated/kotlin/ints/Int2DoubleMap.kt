@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator

import io.github.sooniln.fastcollect.doubles.doubleListOf
import io.github.sooniln.fastcollect.doubles.DoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import io.github.sooniln.fastcollect.doubles.emptyDoubleList

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2DoubleMap(): Int2DoubleMap = EmptyInt2DoubleMap as Int2DoubleMap

@Suppress("UNCHECKED_CAST")
public fun  int2DoubleMapOf(): Int2DoubleMap = EmptyInt2DoubleMap as Int2DoubleMap
public fun  int2DoubleMapOf(entry: Pair<Int, Double>): Int2DoubleMap = SingletonInt2DoubleMap(entry.first, entry.second)
public fun  int2DoubleMapOf(vararg entries: Pair<Int, Double>): Int2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2DoubleMapOf(): MutableInt2DoubleMap = Int2DoubleHashMap()
public fun  mutableInt2DoubleMapOf(entry: Pair<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2DoubleMapOf(vararg entries: Pair<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2DoubleMap(expectedSize: Int = 0, builderAction: MutableInt2DoubleMap.() -> Unit): Int2DoubleMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2DoubleHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Doubles.
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Int2DoubleMap has a [defaultValue] which is
 * returned to indicate no such key is present. In order to obtain the best performance, implementations and clients are
 * encouraged to ensure that the [defaultValue] is the value which is least likely to ever appear in the possible set of
 * values stored in this map. This is purely a performance and not a correctness concern however - the map will still
 * operate correctly and all methods will perform as expected even if the map contains values equal to [defaultValue].
 * [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if applicable.

 */
public interface Int2DoubleMap {

    public val defaultValue: Double


    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Int): Double

    public fun containsKey(key: Int): Boolean {
        for (k in keys) {
            if (k == key) return true
        }
        return false
    }

    public fun containsValue(value: Double): Boolean {
        for (v in values) {
            if (v == value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: DoubleCollection

    public interface Entry {
        public val key: Int
        public val value: Double
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2DoubleMap.isDefaultValue(value: Double): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)


public fun  Int2DoubleMap.asMap(): Map<Int, Double> = Int2DoubleMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2DoubleMap.getOrDefault(key: Int, defaultValue: Double): Double = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2DoubleMap.getValue(key: Int): Double {

    return getOrElse<Double>(key) { throw NoSuchElementException() }

}

@OptIn(ExperimentalContracts::class)

public inline fun <T : Double?> Int2DoubleMap.getOrElse(key: Int, defaultValue: () -> T): T {

    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as T
}

/**
 * A mutable map of Ints to Doubles.
 */
public interface MutableInt2DoubleMap : Int2DoubleMap {

    public fun put(key: Int, value: Double): Double

    public operator fun set(key: Int, value: Double) {
        put(key, value)
    }

    public fun remove(key: Int): Double

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableDoubleCollection

    public fun putAll(from: Int2DoubleMap) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Double>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Int2DoubleMap.Entry {
        override var value: Double
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableInt2DoubleMap.asMutableMap(): MutableMap<Int, Double> = MutableInt2DoubleMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.merge(key: Int, value: Double, merge: (oldValue: Double, value: Double) -> Double): Double {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue as Double, value)
    if (newValue != oldValue) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.getOrPut(key: Int, defaultValue: () -> Double): Double {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Double
    }
}

public abstract class AbstractInt2DoubleMap : Int2DoubleMap {

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

    public class SimpleEntry(override val key: Int, override val value: Double) : Int2DoubleMap.Entry
}

public abstract class AbstractMutableInt2DoubleMap : AbstractInt2DoubleMap(), MutableInt2DoubleMap


private object EmptyInt2DoubleMap : Int2DoubleMap {



    override val defaultValue: Double get() = Double.NaN


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Double): Boolean = false
    override fun get(key: Int): Double = Double.NaN



    override val keys: IntSet get() = emptyIntSet()

    override val values: DoubleCollection get() = emptyDoubleList()
    override fun iterator() = emptyFastIterator<Int2DoubleMap.Entry>()

}

private class SingletonInt2DoubleMap(private val key: Int, private val value: Double) : Int2DoubleMap {

    override val defaultValue: Double get() = Double.NaN


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: Double): Boolean = value == this.value
    override fun get(key: Int): Double = if (key == this.key) value else Double.NaN

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: DoubleCollection by lazy { doubleListOf(value) }


    override fun iterator() = object : FastIterator<Int2DoubleMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2DoubleMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2DoubleMap.SimpleEntry(key, value)
        }
    }
}

private class Int2DoubleMapWrapper(private val map: Int2DoubleMap) : AbstractMap<Int, Double>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Double? = map.getOrElse(key) { null }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Double): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Double>> = object : AbstractSet<Map.Entry<Int, Double>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Double>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Double>> = object : Iterator<Map.Entry<Int, Double>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Double> {
                val entry = it.next()
                return object : Map.Entry<Int, Double> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableInt2DoubleMapWrapper(private val map: MutableInt2DoubleMap) : AbstractMutableMap<Int, Double>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Double? = map.getOrElse(key) { null }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Double): Boolean = map.containsValue(value)

    override fun remove(key: Int): Double? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Double): Double? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Double>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Double>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Double>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Double>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Double>> = object : MutableIterator<MutableMap.MutableEntry<Int, Double>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Double> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Int, value: Double) : MutableMap.MutableEntry<Int, Double> {
            override var value = value
                private set

            override fun setValue(newValue: Double): Double {
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
