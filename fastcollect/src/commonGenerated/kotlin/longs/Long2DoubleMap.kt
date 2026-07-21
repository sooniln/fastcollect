@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator
import io.github.sooniln.fastcollect.equalsBoxed

import io.github.sooniln.fastcollect.doubles.doubleListOf
import io.github.sooniln.fastcollect.doubles.DoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import io.github.sooniln.fastcollect.doubles.emptyDoubleList

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2DoubleMap(): Long2DoubleMap = EmptyLong2DoubleMap as Long2DoubleMap

@Suppress("UNCHECKED_CAST")
public fun  long2DoubleMapOf(): Long2DoubleMap = EmptyLong2DoubleMap as Long2DoubleMap
public fun  long2DoubleMapOf(entry: Pair<Long, Double>): Long2DoubleMap = SingletonLong2DoubleMap(entry.first, entry.second)
public fun  long2DoubleMapOf(vararg entries: Pair<Long, Double>): Long2DoubleMap = Long2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2DoubleMapOf(): MutableLong2DoubleMap = Long2DoubleHashMap()
public fun  mutableLong2DoubleMapOf(entry: Pair<Long, Double>): MutableLong2DoubleMap = Long2DoubleHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableLong2DoubleMapOf(vararg entries: Pair<Long, Double>): MutableLong2DoubleMap = Long2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildLong2DoubleMap(expectedSize: Int = 0, builderAction: MutableLong2DoubleMap.() -> Unit): Long2DoubleMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2DoubleHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Doubles.
 *
 * A Long2DoubleMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Long2DoubleMap {

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
    public fun isDefaultValue(value: Double): Boolean

    public operator fun get(key: Long): Double

    public fun getValue(key: Long): Double = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Long, defaultValue: Double): Double = getOrElse(key) { defaultValue }

    public fun containsKey(key: Long): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: Double): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: LongSet
    public val values: DoubleCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): DoubleCollection = values

    public interface Entry {
        public val key: Long
        public val value: Double

        public operator fun component1(): Long = key
        public operator fun component2(): Double = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>
}

public fun  Long2DoubleMap.asMap(): Map<Long, Double> = Long2DoubleMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Long2DoubleMap.getOrElse(key: Long, defaultValue: () -> Double): Double {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Double
}

/**
 * A mutable map of Longs to Doubles.
 */
public interface MutableLong2DoubleMap : Long2DoubleMap {

    public fun put(key: Long, value: Double): Double

    public operator fun set(key: Long, value: Double) {
        put(key, value)
    }

    public fun remove(key: Long): Double

    public fun clear()

    override val keys: MutableLongSet
    override val values: MutableDoubleCollection

    public fun putAll(from: Long2DoubleMap) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Long, Double>) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Long2DoubleMap.Entry {
        override var value: Double
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableLong2DoubleMap.asMutableMap(): MutableMap<Long, Double> = MutableLong2DoubleMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2DoubleMap.merge(key: Long, value: Double, merge: (oldValue: Double, value: Double) -> Double): Double {
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
public inline fun  MutableLong2DoubleMap.getOrPut(key: Long, defaultValue: () -> Double): Double {
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

public abstract class AbstractLong2DoubleMap : Long2DoubleMap {

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

    public class SimpleEntry(override val key: Long, override val value: Double) : Long2DoubleMap.Entry
}

public abstract class AbstractMutableLong2DoubleMap : AbstractLong2DoubleMap(), MutableLong2DoubleMap


private object EmptyLong2DoubleMap : Long2DoubleMap {


    override fun isDefaultValue(value: Double): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Double): Boolean = false
    override fun get(key: Long): Double = Double.NaN


    override val keys: LongSet get() = emptyLongSet()

    override val values: DoubleCollection get() = emptyDoubleList()
    override fun iterator() = emptyFastIterator<Long2DoubleMap.Entry>()

}

private class SingletonLong2DoubleMap(private val key: Long, private val value: Double) : Long2DoubleMap {
    override fun isDefaultValue(value: Double): Boolean = value equalsBoxed Double.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Double): Boolean = value equalsBoxed this.value
    override fun get(key: Long): Double = if (key equalsBoxed this.key) value else Double.NaN

    override val keys: LongSet by lazy { longSetOf(key) }

    override val values: DoubleCollection by lazy { doubleListOf(value) }

    override fun iterator() = object : FastIterator<Long2DoubleMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2DoubleMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractLong2DoubleMap.SimpleEntry(key, value)
        }
    }
}

private class Long2DoubleMapWrapper(private val map: Long2DoubleMap) : AbstractMap<Long, Double>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Double? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Double): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Double>> = object : AbstractSet<Map.Entry<Long, Double>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Double>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, Double>> = object : Iterator<Map.Entry<Long, Double>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, Double> {
                val entry = it.next()
                return object : Map.Entry<Long, Double> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableLong2DoubleMapWrapper(private val map: MutableLong2DoubleMap) : AbstractMutableMap<Long, Double>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Double? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Double): Boolean = map.containsValue(value)

    override fun remove(key: Long): Double? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: Double): Double? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Double>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, Double>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, Double>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, Double>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, Double>> = object : MutableIterator<MutableMap.MutableEntry<Long, Double>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, Double> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Long, value: Double) : MutableMap.MutableEntry<Long, Double> {
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

    override fun putAll(from: Map<out Long, Double>): Unit = map.putAll(from)
}
