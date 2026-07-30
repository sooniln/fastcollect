@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

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
public fun  emptyLong2FloatMap(): Long2FloatMap = EmptyLong2FloatMap as Long2FloatMap

@Suppress("UNCHECKED_CAST")
public fun  long2FloatMapOf(): Long2FloatMap = EmptyLong2FloatMap as Long2FloatMap
public fun  long2FloatMapOf(entry: Pair<Long, Float>): Long2FloatMap = SingletonLong2FloatMap(entry.first, entry.second)
public fun  long2FloatMapOf(vararg entries: Pair<Long, Float>): Long2FloatMap = Long2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2FloatMapOf(): MutableLong2FloatMap = Long2FloatHashMap()
public fun  mutableLong2FloatMapOf(entry: Pair<Long, Float>): MutableLong2FloatMap = Long2FloatHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableLong2FloatMapOf(vararg entries: Pair<Long, Float>): MutableLong2FloatMap = Long2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildLong2FloatMap(expectedSize: Int = 0, builderAction: MutableLong2FloatMap.() -> Unit): Long2FloatMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2FloatHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Floats.
 *
 * A Long2FloatMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Long2FloatMap {

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

    public operator fun get(key: Long): Float

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Long): Float = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Long, defaultValue: Float): Float = getOrElse(key) { defaultValue }

    public fun containsKey(key: Long): Boolean {
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

    public val keys: LongSet
    public val values: FloatCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): FloatCollection = values

    public interface Entry {
        public val key: Long
        public val value: Float

        public operator fun component1(): Long = key
        public operator fun component2(): Float = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
 
    public fun foreach(action: LongFloatConsumer) {
 
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key, entry.value)
        }
    }

    /**
     * A method for iteration over keys guaranteed to be as fast or faster than [iterator].
     */
    public fun foreachKey(action: LongConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key)
        }
    }
}

public fun  Long2FloatMap.asMap(): Map<Long, Float> = Long2FloatMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Long2FloatMap.getOrElse(key: Long, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Float
}

/**
 * A mutable map of Longs to Floats.
 */
public interface MutableLong2FloatMap : Long2FloatMap {

    public fun put(key: Long, value: Float): Float

    public operator fun set(key: Long, value: Float) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Long, value: Float): Float = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns it's value, or the default value if the key is not present. */
    public fun remove(key: Long): Float

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Long): Float = removeOrElse(key) { throw NoSuchElementException() }

    public fun clear()

    override val keys: MutableLongSet
    override val values: MutableFloatCollection

    public fun putAll(from: Long2FloatMap) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Long, Float>) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Long2FloatMap.Entry {
        override var value: Float
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableLong2FloatMap.asMutableMap(): MutableMap<Long, Float> = MutableLong2FloatMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2FloatMap.merge(key: Long, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
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
public inline fun  MutableLong2FloatMap.getOrPut(key: Long, defaultValue: () -> Float): Float {
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

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2FloatMap.replaceOrSet(key: Long, value: Float, oldValue: () -> Float): Float {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Float
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2FloatMap.removeOrElse(key: Long, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Float
    } else {
        defaultValue()
    }
}

public abstract class AbstractLong2FloatMap : Long2FloatMap {

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

    public class SimpleEntry(override val key: Long, override val value: Float) : Long2FloatMap.Entry
}

public abstract class AbstractMutableLong2FloatMap : AbstractLong2FloatMap(), MutableLong2FloatMap


private object EmptyLong2FloatMap : Long2FloatMap {


    override fun isDefaultValue(value: Float): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Float): Boolean = false
    override fun get(key: Long): Float = Float.NaN


    override val keys: LongSet get() = emptyLongSet()

    override val values: FloatCollection get() = emptyFloatList()
    override fun iterator() = emptyFastIterator<Long2FloatMap.Entry>()

}

private class SingletonLong2FloatMap(private val key: Long, private val value: Float) : Long2FloatMap {
    override fun isDefaultValue(value: Float): Boolean = value equalsBoxed Float.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Float): Boolean = value equalsBoxed this.value
    override fun get(key: Long): Float = if (key equalsBoxed this.key) value else Float.NaN

    override val keys: LongSet by lazy { longSetOf(key) }

    override val values: FloatCollection by lazy { floatListOf(value) }

    override fun iterator() = object : FastIterator<Long2FloatMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2FloatMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractLong2FloatMap.SimpleEntry(key, value)
        }
    }
}

private class Long2FloatMapWrapper(private val map: Long2FloatMap) : AbstractMap<Long, Float>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Float? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Float): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Float>> = object : AbstractSet<Map.Entry<Long, Float>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Float>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, Float>> = object : Iterator<Map.Entry<Long, Float>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, Float> {
                val entry = it.next()
                return object : Map.Entry<Long, Float> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableLong2FloatMapWrapper(private val map: MutableLong2FloatMap) : AbstractMutableMap<Long, Float>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Float? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Float): Boolean = map.containsValue(value)

    override fun remove(key: Long): Float? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: Float): Float? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Float>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, Float>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, Float>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, Float>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, Float>> = object : MutableIterator<MutableMap.MutableEntry<Long, Float>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, Float> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Long, value: Float) : MutableMap.MutableEntry<Long, Float> {
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

    override fun putAll(from: Map<out Long, Float>): Unit = map.putAll(from)
}


public fun interface LongFloatConsumer {

    public fun accept(key: Long, value: Float)
}
