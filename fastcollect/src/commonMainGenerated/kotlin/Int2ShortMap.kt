/**
 * Methods for dealing with Int2ShortMaps.
 */
@file:JvmName("Int2ShortMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2ShortMap(): Int2ShortMap = EmptyInt2ShortMap as Int2ShortMap

@Suppress("UNCHECKED_CAST")
public fun  int2ShortMapOf(): Int2ShortMap = EmptyInt2ShortMap as Int2ShortMap
public fun  int2ShortMapOf(entry: Pair<Int, Short>): Int2ShortMap = SingletonInt2ShortMap(entry.first, entry.second)
public fun  int2ShortMapOf(vararg entries: Pair<Int, Short>): Int2ShortMap = Int2ShortHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2ShortMapOf(): MutableInt2ShortMap = Int2ShortHashMap()
public fun  mutableInt2ShortMapOf(entry: Pair<Int, Short>): MutableInt2ShortMap = Int2ShortHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2ShortMapOf(vararg entries: Pair<Int, Short>): MutableInt2ShortMap = Int2ShortHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2ShortMap(expectedSize: Int = 0, builderAction: MutableInt2ShortMap.() -> Unit): Int2ShortMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2ShortHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Shorts.
 *
 * A Int2ShortMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */

public interface Int2ShortMap {


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
    public fun isDefaultValue(value: @UnsafeVariance Short): Boolean

    public operator fun get(key: Int): Short

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): Short = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance Short): Short = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Short): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: ShortCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): ShortCollection = values


    public interface Entry {

        public val key: Int
        public val value: Short

        public operator fun component1(): Int = key
        public operator fun component2(): Short = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
 
    public fun foreach(action: IntShortConsumer) {
 
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

public fun  Int2ShortMap.asMap(): Map<Int, Short> = Int2ShortMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Int2ShortMap.getOrElse(key: Int, defaultValue: () -> Short): Short {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Short
}

/**
 * A mutable map of Ints to Shorts.
 */
public interface MutableInt2ShortMap : Int2ShortMap {

    public fun put(key: Int, value: Short): Short

    public operator fun set(key: Int, value: Short) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: Short): Short = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns it's value, or the default value if the key is not present. */
    public fun remove(key: Int): Short

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): Short = removeOrElse(key) { throw NoSuchElementException() }

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableShortCollection

    public fun putAll(from: Int2ShortMap) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Short>) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Int2ShortMap.Entry {
        override var value: Short
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableInt2ShortMap.asMutableMap(): MutableMap<Int, Short> = MutableInt2ShortMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ShortMap.merge(key: Int, value: Short, merge: (oldValue: Short, value: Short) -> Short): Short {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Short, value)
    if (absent || !(newValue equalsBoxed oldValue)) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ShortMap.getOrPut(key: Int, defaultValue: () -> Short): Short {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Short
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ShortMap.replaceOrSet(key: Int, value: Short, oldValue: () -> Short): Short {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Short
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ShortMap.removeOrElse(key: Int, defaultValue: () -> Short): Short {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Short
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2ShortMap : Int2ShortMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2ShortMap) {
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

    public class SimpleEntry(override val key: Int, override val value: Short) : Int2ShortMap.Entry
}

public abstract class AbstractMutableInt2ShortMap : AbstractInt2ShortMap(), MutableInt2ShortMap


private object EmptyInt2ShortMap : Int2ShortMap {


    override fun isDefaultValue(value: Short): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Short): Boolean = false
    override fun get(key: Int): Short = Short.MIN_VALUE


    override val keys: IntSet get() = emptyIntSet()

    override val values: ShortCollection get() = emptyShortList()
    override fun iterator() = emptyFastIterator<Int2ShortMap.Entry>()

}

private class SingletonInt2ShortMap(private val key: Int, private val value: Short) : Int2ShortMap {
    override fun isDefaultValue(value: Short): Boolean = value equalsBoxed Short.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Short): Boolean = value equalsBoxed this.value
    override fun get(key: Int): Short = if (key equalsBoxed this.key) value else Short.MIN_VALUE

    override val keys: IntSet by lazy { intSetOf(key) }

    override val values: ShortCollection by lazy { shortListOf(value) }

    override fun iterator() = object : FastIterator<Int2ShortMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2ShortMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2ShortMap.SimpleEntry(key, value)
        }
    }
}

private class Int2ShortMapWrapper(private val map: Int2ShortMap) : AbstractMap<Int, Short>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Short? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Short): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Short>> = object : AbstractSet<Map.Entry<Int, Short>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Short>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Short>> = object : Iterator<Map.Entry<Int, Short>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Short> {
                val entry = it.next()
                return object : Map.Entry<Int, Short> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableInt2ShortMapWrapper(private val map: MutableInt2ShortMap) : AbstractMutableMap<Int, Short>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Short? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Short): Boolean = map.containsValue(value)

    override fun remove(key: Int): Short? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Short): Short? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Short>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Short>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Short>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Short>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Short>> = object : MutableIterator<MutableMap.MutableEntry<Int, Short>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Short> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Int, value: Short) : MutableMap.MutableEntry<Int, Short> {
            override var value = value
                private set

            override fun setValue(newValue: Short): Short {
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

    override fun putAll(from: Map<out Int, Short>): Unit = map.putAll(from)
}


public fun interface IntShortConsumer {

    public fun accept(key: Int, value: Short)
}
