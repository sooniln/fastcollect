@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator
import io.github.sooniln.fastcollect.equalsBoxed

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
 * A Int2FloatMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Int2FloatMap {

    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

    public fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * Returns true if the given value is current the default value of the map (i.e., the value returned from retrieval
     * operations when a key is not present). Note that maps are not required to have an unchanging default value
     * (though this is the most common implementation). A map may change its default value during the invocation of any
     * mutable public API method. A map may not change its default value outside of the invocation of any mutable public
     * API method. For this reason clients should not store or make other assumptions about the default value.
     */
    public fun isDefaultValue(value: Float): Boolean

    public operator fun get(key: Int): Float

    public fun getValue(key: Int): Float = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: Float): Float = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: Float): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: FloatCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): FloatCollection = values

    public interface Entry {
        public val key: Int
        public val value: Float

        public operator fun component1(): Int = key
        public operator fun component2(): Float = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
 
    public fun foreach(action: IntFloatConsumer) {
 
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key, entry.value)
        }
    }

    /**
     * A method for iteration over keys guaranteed to be as fast or faster than [iterator].
     */
    public fun foreachKey(action: IntConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key)
        }
    }
}

public fun  Int2FloatMap.asMap(): Map<Int, Float> = Int2FloatMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatMap.getOrElse(key: Int, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Float
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
            set(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Float>) {
        for (entry in from) {
            set(entry.key, entry.value)
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
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Float, value)
    if (absent || !(newValue equalsBoxed oldValue)) {
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

    override fun toString(): String = Iterable { iterator() }.joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Int, override val value: Float) : Int2FloatMap.Entry
}

public abstract class AbstractMutableInt2FloatMap : AbstractInt2FloatMap(), MutableInt2FloatMap


private object EmptyInt2FloatMap : Int2FloatMap {


    override fun isDefaultValue(value: Float): Boolean = true


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
    override fun isDefaultValue(value: Float): Boolean = value equalsBoxed Float.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Float): Boolean = value equalsBoxed this.value
    override fun get(key: Int): Float = if (key equalsBoxed this.key) value else Float.NaN

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

    override fun get(key: Int): Float? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Float): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Float>> = object : AbstractSet<Map.Entry<Int, Float>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Float>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
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

    override fun get(key: Int): Float? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

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
            return value equalsBoxed element.value
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
                if (!(map.put(key, newValue) equalsBoxed oldValue)) throw ConcurrentModificationException()
                value = newValue
                return oldValue
            }

            override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
            override fun toString(): String = "$key=$value"
        }
    }

    override fun putAll(from: Map<out Int, Float>): Unit = map.putAll(from)
}


public fun interface IntFloatConsumer {

    public fun accept(key: Int, value: Float)
}
