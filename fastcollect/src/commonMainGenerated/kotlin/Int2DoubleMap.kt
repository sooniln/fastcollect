/**
 * Methods for dealing with Int2DoubleMaps.
 */
@file:JvmName("Int2DoubleMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

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
 * A Int2DoubleMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */

public interface Int2DoubleMap {


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
    public fun isDefaultValue(value: @UnsafeVariance Double): Boolean

    public operator fun get(key: Int): Double

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): Double = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance Double): Double = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Double): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: DoubleCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): DoubleCollection = values


    public interface Entry {

        public val key: Int
        public val value: Double

        public operator fun component1(): Int = key
        public operator fun component2(): Double = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
 
    public fun foreach(action: IntDoubleConsumer) {
 
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

public fun  Int2DoubleMap.asMap(): Map<Int, Double> = Int2DoubleMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleMap.getOrElse(key: Int, defaultValue: () -> Double): Double {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Double
}

/**
 * A mutable map of Ints to Doubles.
 */
public interface MutableInt2DoubleMap : Int2DoubleMap {

    public fun put(key: Int, value: Double): Double

    public operator fun set(key: Int, value: Double) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: Double): Double = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Int): Double

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): Double = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Int, value: Double): Boolean

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableDoubleCollection

    public fun putAll(from: Int2DoubleMap) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Double>) {
        for (entry in from) {
            set(entry.key, entry.value)
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
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Double, value)
    if (absent || !(newValue equalsBoxed oldValue)) {
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

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.replaceOrSet(key: Int, value: Double, oldValue: () -> Double): Double {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Double
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.removeOrElse(key: Int, defaultValue: () -> Double): Double {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Double
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2DoubleMap : Int2DoubleMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2DoubleMap) {
            if (other.size != size) return false

            for (entry in this) {
                if (!(other[entry.key] equalsBoxed entry.value)) return false
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

    public class SimpleEntry(override val key: Int, override val value: Double) : Int2DoubleMap.Entry
}

public abstract class AbstractMutableInt2DoubleMap : AbstractInt2DoubleMap(), MutableInt2DoubleMap


private object EmptyInt2DoubleMap : Int2DoubleMap {


    override fun isDefaultValue(value: Double): Boolean = true


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
    override fun isDefaultValue(value: Double): Boolean = value equalsBoxed Double.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Double): Boolean = value equalsBoxed this.value
    override fun get(key: Int): Double = if (key equalsBoxed this.key) value else Double.NaN

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

    override fun get(key: Int): Double? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Double): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Double>> = object : AbstractSet<Map.Entry<Int, Double>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Double>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
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

    override fun get(key: Int): Double? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

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
            return value equalsBoxed element.value
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
                if (!(map.put(key, newValue) equalsBoxed oldValue)) throw ConcurrentModificationException()
                value = newValue
                return oldValue
            }

            override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
            override fun toString(): String = "$key=$value"
        }
    }

    override fun putAll(from: Map<out Int, Double>): Unit = map.putAll(from)
}


public fun interface IntDoubleConsumer {

    public fun accept(key: Int, value: Double)
}
