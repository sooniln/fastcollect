@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyFastIterator

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun <V> emptyInt2AnyMap(): Int2AnyMap<V> = EmptyInt2AnyMap as Int2AnyMap<V>

@Suppress("UNCHECKED_CAST")
public fun <V> int2AnyMapOf(): Int2AnyMap<V> = EmptyInt2AnyMap as Int2AnyMap<V>
public fun <V> int2AnyMapOf(entry: Pair<Int, V>): Int2AnyMap<V> = SingletonInt2AnyMap<V>(entry.first, entry.second)
public fun <V> int2AnyMapOf(vararg entries: Pair<Int, V>): Int2AnyMap<V> = Int2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun <V> mutableInt2AnyMapOf(): MutableInt2AnyMap<V> = Int2AnyHashMap()
public fun <V> mutableInt2AnyMapOf(entry: Pair<Int, V>): MutableInt2AnyMap<V> = Int2AnyHashMap<V>(1).apply { set(entry.first, entry.second) }
public fun <V> mutableInt2AnyMapOf(vararg entries: Pair<Int, V>): MutableInt2AnyMap<V> = Int2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <V> buildInt2AnyMap(expectedSize: Int = 0, builderAction: MutableInt2AnyMap<V>.() -> Unit): Int2AnyMap<V> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2AnyHashMap<V>(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Vs.
 *

 */
public interface Int2AnyMap<V> {


    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun get(key: Int): V?

    public fun containsKey(key: Int): Boolean {
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

    public val keys: IntSet
    public val values: Collection<V>

    public interface Entry<V> {
        public val key: Int
        public val value: V
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry<V>>
}


@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
@PublishedApi
internal inline fun <V> Int2AnyMap<V>.isDefaultValue(value: V?): Boolean = value == null


public fun <V> Int2AnyMap<V>.asMap(): Map<Int, V> = Int2AnyMapWrapper(this)

@Suppress("NOTHING_TO_INLINE")
public inline fun <V> Int2AnyMap<V>.getOrDefault(key: Int, defaultValue: V): V = getOrElse(key) { defaultValue }

@Suppress("NOTHING_TO_INLINE")
public inline fun <V> Int2AnyMap<V>.getValue(key: Int): V {

    return getOrElse<V, V>(key) { throw NoSuchElementException() }

}

@OptIn(ExperimentalContracts::class)

public inline fun <V, T : V?> Int2AnyMap<V>.getOrElse(key: Int, defaultValue: () -> T): T {

    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as T
}

/**
 * A mutable map of Ints to Vs.
 */
public interface MutableInt2AnyMap<V> : Int2AnyMap<V> {

    public fun put(key: Int, value: V): V?

    public operator fun set(key: Int, value: V) {
        put(key, value)
    }

    public fun remove(key: Int): V?

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableCollection<V>

    public fun putAll(from: Int2AnyMap<V>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, V>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    public interface MutableEntry<V> : Int2AnyMap.Entry<V> {
        override var value: V
    }

    override fun iterator(): MutableFastIterator<MutableEntry<V>>
}

public fun <V> MutableInt2AnyMap<V>.asMutableMap(): MutableMap<Int, V> = MutableInt2AnyMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableInt2AnyMap<V>.merge(key: Int, value: V, merge: (oldValue: V, value: V) -> V): V {
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
public inline fun <V> MutableInt2AnyMap<V>.getOrPut(key: Int, defaultValue: () -> V): V {
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

public abstract class AbstractInt2AnyMap<V> : Int2AnyMap<V> {

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

    public class SimpleEntry<V>(override val key: Int, override val value: V) : Int2AnyMap.Entry<V>
}

public abstract class AbstractMutableInt2AnyMap<V> : AbstractInt2AnyMap<V>(), MutableInt2AnyMap<V>


private object EmptyInt2AnyMap : Int2AnyMap<Nothing> {




    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false
    override fun get(key: Int): Nothing? = null



    override val keys: IntSet get() = emptyIntSet()

    override val values: Collection<Nothing> get() = emptyList()
    override fun iterator() = emptyFastIterator<Int2AnyMap.Entry<Nothing>>()

}

private class SingletonInt2AnyMap<V>(private val key: Int, private val value: V) : Int2AnyMap<V> {


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: V): Boolean = value == this.value
    override fun get(key: Int): V? = if (key == this.key) value else null

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: Collection<V> by lazy { listOf(value) }


    override fun iterator() = object : FastIterator<Int2AnyMap.Entry<V>> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2AnyMap.Entry<V> {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2AnyMap.SimpleEntry(key, value)
        }
    }
}

private class Int2AnyMapWrapper<V>(private val map: Int2AnyMap<V>) : AbstractMap<Int, V>() {
    override val size: Int get() = map.size

    override fun get(key: Int): V? = map.getOrElse(key) { null }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, V>> = object : AbstractSet<Map.Entry<Int, V>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, V>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, V>> = object : Iterator<Map.Entry<Int, V>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, V> {
                val entry = it.next()
                return object : Map.Entry<Int, V> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableInt2AnyMapWrapper<V>(private val map: MutableInt2AnyMap<V>) : AbstractMutableMap<Int, V>() {
    override val size: Int get() = map.size

    override fun get(key: Int): V? = map.getOrElse(key) { null }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override fun remove(key: Int): V? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: V): V? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, V>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, V>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, V>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value == element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, V>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, V>> = object : MutableIterator<MutableMap.MutableEntry<Int, V>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, V> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Int, value: V) : MutableMap.MutableEntry<Int, V> {
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
