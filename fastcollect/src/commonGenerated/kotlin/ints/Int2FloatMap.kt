@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator

import io.github.sooniln.fastcollect.floats.floatListOf
import io.github.sooniln.fastcollect.floats.FloatCollection
import io.github.sooniln.fastcollect.floats.MutableFloatCollection
import io.github.sooniln.fastcollect.floats.emptyFloatList

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2FloatMap(): Int2FloatMap = EmptyInt2FloatMap as Int2FloatMap

@Suppress("UNCHECKED_CAST")
public fun  int2FloatMapOf(): Int2FloatMap = EmptyInt2FloatMap as Int2FloatMap
public fun  int2FloatMapOf(entry: Pair<Int, Float>): Int2FloatMap = SingletonInt2FloatMap(entry.first, entry.second)
public fun  int2FloatMapOf(vararg entries: Pair<Int, Float>): Int2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2FloatMapOf(): MutableInt2FloatMap = Int2FloatHashMap()
public fun  mutableInt2FloatMapOf(entry: Pair<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2FloatMapOf(vararg entries: Pair<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2FloatMap(expectedSize: Int = 0, builderAction: MutableInt2FloatMap.() -> Unit): Int2FloatMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2FloatHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Floats.
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Int2FloatMap has a [defaultValue] which is
 * returned to indicate no such key is present. In order to obtain the best performance, implementations and clients are
 * encouraged to ensure that the [defaultValue] is the value which is least likely to ever appear in the possible set of
 * values stored in this map. This is purely a performance and not a correctness concern however - the map will still
 * operate correctly and all methods will perform as expected even if the map contains values equal to [defaultValue].
 * [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if applicable.

 */
public interface Int2FloatMap {

    public val defaultValue: Float


    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Int): Float

    public fun containsKey(key: Int): Boolean {
        for (k in keys) {
            if (k == key) return true
        }
        return false
    }

    public fun containsValue(value: Float): Boolean {
        for (v in values) {
            if (v == value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: FloatCollection

    public interface Entry {
        public val key: Int
        public val value: Float
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2FloatMap.isDefaultValue(value: Float): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)


public fun  Int2FloatMap.asMap(): Map<Int, Float> = Int2FloatMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2FloatMap.getOrDefault(key: Int, defaultValue: Float): Float = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2FloatMap.getValue(key: Int): Float {

    return getOrElse<Float>(key) { throw NoSuchElementException() }

}

@OptIn(ExperimentalContracts::class)

public inline fun <T : Float?> Int2FloatMap.getOrElse(key: Int, defaultValue: () -> T): T {

    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as T
}

/**
 * A mutable map of Ints to Floats.
 */
public interface MutableInt2FloatMap : Int2FloatMap {

    public fun put(key: Int, value: Float): Float

    public operator fun set(key: Int, value: Float) {
        put(key, value)
    }

    public fun remove(key: Int): Float

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableFloatCollection

    public fun putAll(from: Int2FloatMap) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Float>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Int2FloatMap.Entry {
        override var value: Float
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableInt2FloatMap.asMutableMap(): MutableMap<Int, Float> = MutableInt2FloatMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2FloatMap.merge(key: Int, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue as Float, value)
    if (newValue != oldValue) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2FloatMap.getOrPut(key: Int, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Float
    }
}

public abstract class AbstractInt2FloatMap : Int2FloatMap {

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

    public class SimpleEntry(override val key: Int, override val value: Float) : Int2FloatMap.Entry
}

public abstract class AbstractMutableInt2FloatMap : AbstractInt2FloatMap(), MutableInt2FloatMap


private object EmptyInt2FloatMap : Int2FloatMap {



    override val defaultValue: Float get() = Float.NaN


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Float): Boolean = false
    override fun get(key: Int): Float = Float.NaN



    override val keys: IntSet get() = emptyIntSet()

    override val values: FloatCollection get() = emptyFloatList()
    override fun iterator() = emptyFastIterator<Int2FloatMap.Entry>()

}

private class SingletonInt2FloatMap(private val key: Int, private val value: Float) : Int2FloatMap {

    override val defaultValue: Float get() = Float.NaN


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: Float): Boolean = value == this.value
    override fun get(key: Int): Float = if (key == this.key) value else Float.NaN

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: FloatCollection by lazy { floatListOf(value) }


    override fun iterator() = object : FastIterator<Int2FloatMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2FloatMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2FloatMap.SimpleEntry(key, value)
        }
    }
}

private class Int2FloatMapWrapper(private val map: Int2FloatMap) : AbstractMap<Int, Float>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Float? = map.getOrElse(key) { null }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Float): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Float>> = object : AbstractSet<Map.Entry<Int, Float>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Float>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Float>> = object : Iterator<Map.Entry<Int, Float>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Float> {
                val entry = it.next()
                return object : Map.Entry<Int, Float> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableInt2FloatMapWrapper(private val map: MutableInt2FloatMap) : AbstractMutableMap<Int, Float>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Float? = map.getOrElse(key) { null }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Float): Boolean = map.containsValue(value)

    override fun remove(key: Int): Float? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Float): Float? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Float>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Float>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Float>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Float>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Float>> = object : MutableIterator<MutableMap.MutableEntry<Int, Float>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Float> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Int, value: Float) : MutableMap.MutableEntry<Int, Float> {
            override var value = value
                private set

            override fun setValue(newValue: Float): Float {
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
