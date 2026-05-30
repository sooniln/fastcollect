@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun <V> emptyLong2AnyMap(): Long2AnyMap<V> = EmptyLong2AnyMap as Long2AnyMap<V>

@Suppress("UNCHECKED_CAST")
public fun <V> long2AnyMapOf(): Long2AnyMap<V> = EmptyLong2AnyMap as Long2AnyMap<V>
public fun <V> long2AnyMapOf(entry: Pair<Long, V>): Long2AnyMap<V> = SingletonLong2AnyMap<V>(entry.first, entry.second)
public fun <V> long2AnyMapOf(vararg entries: Pair<Long, V>): Long2AnyMap<V> = Long2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun <V> mutableLong2AnyMapOf(): MutableLong2AnyMap<V> = Long2AnyHashMap()
public fun <V> mutableLong2AnyMapOf(entry: Pair<Long, V>): MutableLong2AnyMap<V> = Long2AnyHashMap<V>(1).apply { set(entry.first, entry.second) }
public fun <V> mutableLong2AnyMapOf(vararg entries: Pair<Long, V>): MutableLong2AnyMap<V> = Long2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <V> buildLong2AnyMap(expectedSize: Int = 0, builderAction: MutableLong2AnyMap<V>.() -> Unit): Long2AnyMap<V> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2AnyHashMap<V>(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Vs.
 *

 */
public interface Long2AnyMap<V> {


    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Long): V?

    public fun containsKey(key: Long): Boolean {
        for (k in keys) {
            if (k == key) return true
        }
        return false
    }

    public fun containsValue(value: V): Boolean {
        for (v in values) {
            if (v == value) return true
        }
        return false
    }

    public val keys: LongSet
    public val values: Collection<V>

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): Collection<V> = values

    public interface Entry<V> {
        public val key: Long
        public val value: V
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry<V>>
}


@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
@PublishedApi
internal inline fun <V> Long2AnyMap<V>.isDefaultValue(value: V?): Boolean = value == null


public fun <V> Long2AnyMap<V>.asMap(): Map<Long, V> = Long2AnyMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun <V> Long2AnyMap<V>.getOrDefault(key: Long, defaultValue: V): V = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun <V> Long2AnyMap<V>.getValue(key: Long): V = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun <V> Long2AnyMap<V>.getOrElse(key: Long, defaultValue: () -> V): V {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as V
}

/**
 * A mutable map of Longs to Vs.
 */
public interface MutableLong2AnyMap<V> : Long2AnyMap<V> {

    public fun put(key: Long, value: V): V?

    public operator fun set(key: Long, value: V) {
        put(key, value)
    }

    public fun remove(key: Long): V?

    public fun clear()

    override val keys: MutableLongSet
    override val values: MutableCollection<V>

    public fun putAll(from: Long2AnyMap<V>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Long, V>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry<V> : Long2AnyMap.Entry<V> {
        override var value: V
    }

    override fun iterator(): MutableFastIterator<MutableEntry<V>>
}

public fun <V> MutableLong2AnyMap<V>.asMutableMap(): MutableMap<Long, V> = MutableLong2AnyMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableLong2AnyMap<V>.merge(key: Long, value: V, merge: (oldValue: V, value: V) -> V): V {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue as V, value)
    if (newValue != oldValue) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableLong2AnyMap<V>.getOrPut(key: Long, defaultValue: () -> V): V {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as V
    }
}

public abstract class AbstractLong2AnyMap<V> : Long2AnyMap<V> {

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

    public class SimpleEntry<V>(override val key: Long, override val value: V) : Long2AnyMap.Entry<V>
}

public abstract class AbstractMutableLong2AnyMap<V> : AbstractLong2AnyMap<V>(), MutableLong2AnyMap<V>


private object EmptyLong2AnyMap : Long2AnyMap<Nothing> {




    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false
    override fun get(key: Long): Nothing? = null



    override val keys: LongSet get() = emptyLongSet()

    override val values: Collection<Nothing> get() = emptyList()
    override fun iterator() = emptyFastIterator<Long2AnyMap.Entry<Nothing>>()

}

private class SingletonLong2AnyMap<V>(private val key: Long, private val value: V) : Long2AnyMap<V> {


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: V): Boolean = value == this.value
    override fun get(key: Long): V? = if (key == this.key) value else null

    override val keys: LongSet by lazy { longSetOf(key) }


    override val values: Collection<V> by lazy { listOf(value) }


    override fun iterator() = object : FastIterator<Long2AnyMap.Entry<V>> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2AnyMap.Entry<V> {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractLong2AnyMap.SimpleEntry(key, value)
        }
    }
}

private class Long2AnyMapWrapper<V>(private val map: Long2AnyMap<V>) : AbstractMap<Long, V>() {
    override val size: Int get() = map.size

    override fun get(key: Long): V? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, V>> = object : AbstractSet<Map.Entry<Long, V>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, V>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, V>> = object : Iterator<Map.Entry<Long, V>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, V> {
                val entry = it.next()
                return object : Map.Entry<Long, V> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableLong2AnyMapWrapper<V>(private val map: MutableLong2AnyMap<V>) : AbstractMutableMap<Long, V>() {
    override val size: Int get() = map.size

    override fun get(key: Long): V? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override fun remove(key: Long): V? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: V): V? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, V>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, V>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, V>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, V>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, V>> = object : MutableIterator<MutableMap.MutableEntry<Long, V>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, V> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Long, value: V) : MutableMap.MutableEntry<Long, V> {
            override var value = value
                private set

            override fun setValue(newValue: V): V {
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
