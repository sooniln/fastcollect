/**
 * Methods for dealing with Long2LongMaps.
 */
@file:JvmName("Long2LongMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2LongMap(): Long2LongMap = EmptyLong2LongMap as Long2LongMap

@Suppress("UNCHECKED_CAST")
public fun  long2LongMapOf(): Long2LongMap = EmptyLong2LongMap as Long2LongMap
public fun  long2LongMapOf(entry: Pair<Long, Long>): Long2LongMap = SingletonLong2LongMap(entry.first, entry.second)
public fun  long2LongMapOf(vararg entries: Pair<Long, Long>): Long2LongMap = Long2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2LongMapOf(): MutableLong2LongMap = Long2LongHashMap()
public fun  mutableLong2LongMapOf(entry: Pair<Long, Long>): MutableLong2LongMap = Long2LongHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableLong2LongMapOf(vararg entries: Pair<Long, Long>): MutableLong2LongMap = Long2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class)
public inline fun  buildLong2LongMap(expectedSize: Int = 0, builderAction: MutableLong2LongMap.() -> Unit): Long2LongMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2LongHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Longs.
 *
 * A Long2LongMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Long2LongMap : Long2LongTraversable {

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
    public fun isDefaultValue(value: @UnsafeVariance Long): Boolean

    public operator fun get(key: Long): Long

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Long): Long = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Long, defaultValue: @UnsafeVariance Long): Long = getOrElse(key) { defaultValue }

    public fun containsKey(key: Long): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Long): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: LongSet
    public val values: LongCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): LongCollection = values

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Long
        public val value: Long

        public operator fun component1(): Long = key
        public operator fun component2(): Long = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsBoxed key && other.value equalsBoxed value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Long2LongMap.asMap(): Map<Long, Long> = Long2LongMapWrapper(this)

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
public interface MutableLong2LongMap : Long2LongMap, MutableLong2LongTraversable {

    public fun put(key: Long, value: Long): Long

    public operator fun set(key: Long, value: Long) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Long, value: Long): Long = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Long): Long

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Long): Long = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Long, value: Long): Boolean

    public fun clear()

    override val keys: LongSet
    override val values: LongCollection

    public fun putAll(from: Long2LongMap) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Long, Long>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Long2LongMap.Entry {
        override var value: Long
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Long2LongMap.AbstractEntry(), MutableEntry
}

public fun  MutableLong2LongMap.asMutableMap(): MutableMap<Long, Long> = MutableLong2LongMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2LongMap.merge(key: Long, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Long, value)
    if (absent || !(newValue equalsBoxed oldValue)) {
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

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2LongMap.replaceOrSet(key: Long, value: Long, oldValue: () -> Long): Long {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Long
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2LongMap.removeOrElse(key: Long, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Long
    } else {
        defaultValue()
    }
}

public abstract class AbstractLong2LongMap : Long2LongMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Long2LongMap) {
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

    public class SimpleEntry(override val key: Long, override val value: Long) : Long2LongMap.Entry
}

public abstract class AbstractMutableLong2LongMap : AbstractLong2LongMap(), MutableLong2LongMap


private object EmptyLong2LongMap : Long2LongMap {


    override fun isDefaultValue(value: Long): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Long): Boolean = false
    override fun get(key: Long): Long = Long.MIN_VALUE


    override val keys: LongSet get() = emptyLongSet()

    override val values: LongCollection get() = emptyLongList()
    override fun iterator(): Iterator<Long2LongMap.Entry> = emptyList<Long2LongMap.Entry>().iterator()



    override fun traverser(): Long2LongTraverser = emptyLong2LongTraverser()

}

private class SingletonLong2LongMap(private val key: Long, private val value: Long) : Long2LongMap {
    override fun isDefaultValue(value: Long): Boolean = value equalsBoxed Long.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Long): Boolean = value equalsBoxed this.value
    override fun get(key: Long): Long = if (key equalsBoxed this.key) value else Long.MIN_VALUE

    override val keys: LongSet by lazy { longSetOf(key) }

    override val values: LongCollection by lazy { longListOf(value) }

    override fun iterator() = object : Iterator<Long2LongMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2LongMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractLong2LongMap.SimpleEntry(key, value)
        }
    }

    override fun traverser(): Long2LongTraverser = object : Long2LongTraverser {
        private var consumed = false
        override val key: Long get() = this@SingletonLong2LongMap.key
        override val value: Long get() = this@SingletonLong2LongMap.value
        override fun forward(): Boolean {
            if (consumed) return false
            consumed = true
            return true
        }
    }
}

private class Long2LongMapWrapper(private val map: Long2LongMap) : AbstractMap<Long, Long>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Long? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Long): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Long>> = object : AbstractSet<Map.Entry<Long, Long>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Long>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsBoxed element.value
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
        val value = map[key]
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
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsBoxed element.value
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
                if (!(map.put(key, newValue) equalsBoxed oldValue)) throw ConcurrentModificationException()
                value = newValue
                return oldValue
            }

            override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
            override fun toString(): String = "$key=$value"
        }
    }

    override fun putAll(from: Map<out Long, Long>): Unit = map.putAll(from)
}


public fun interface LongLongConsumer {

    public fun accept(key: Long, value: Long)
}
