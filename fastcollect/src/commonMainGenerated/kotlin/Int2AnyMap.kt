/**
 * Methods for dealing with Int2AnyMaps.
 */
@file:JvmName("Int2AnyMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

@Suppress("UNCHECKED_CAST")
public fun <V> emptyInt2AnyMap(): Int2AnyMap<V> = EmptyInt2AnyMap as Int2AnyMap<V>

@Suppress("UNCHECKED_CAST")
public fun <V> int2AnyMapOf(): Int2AnyMap<V> = EmptyInt2AnyMap as Int2AnyMap<V>
public fun <V> int2AnyMapOf(entry: Pair<Int, V>): Int2AnyMap<V> = SingletonInt2AnyMap<V>(entry.first, entry.second)
public fun <V> int2AnyMapOf(vararg entries: Pair<Int, V>): Int2AnyMap<V> = Int2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun <V> mutableInt2AnyMapOf(): MutableInt2AnyMap<V> = Int2AnyHashMap()
public fun <V> mutableInt2AnyMapOf(entry: Pair<Int, V>): MutableInt2AnyMap<V> = Int2AnyHashMap<V>(1).apply { set(entry.first, entry.second) }
public fun <V> mutableInt2AnyMapOf(vararg entries: Pair<Int, V>): MutableInt2AnyMap<V> = Int2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class)
public inline fun <V> buildInt2AnyMap(expectedSize: Int = 0, builderAction: MutableInt2AnyMap<V>.() -> Unit): Int2AnyMap<V> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2AnyHashMap<V>(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Vs.
 *
 * A Int2AnyMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Int2AnyMap<out V> : Int2AnyTraversable<V> {

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
    public fun isDefaultValue(value: @UnsafeVariance V?): Boolean

    public operator fun get(key: Int): V?

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): V = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance V): V = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance V): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: Collection<V>

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): Collection<V> = values

    public operator fun iterator(): Iterator<Entry<V>>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry<out V> {
        public val key: Int
        public val value: V

        public operator fun component1(): Int = key
        public operator fun component2(): V = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry<out V> : Entry<V> {
        final override fun equals(other: Any?): Boolean = other is Entry<*> && other.key equalsBoxed key && other.value equalsBoxed value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun <V> Int2AnyMap<V>.asMap(): Map<Int, V> = Int2AnyMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyMap<V>.getOrElse(key: Int, defaultValue: () -> V): V {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as V
}

/**
 * A mutable map of Ints to Vs.
 */
public interface MutableInt2AnyMap<V> : Int2AnyMap<V>, MutableInt2AnyTraversable<V> {

    public fun put(key: Int, value: V): V?

    public operator fun set(key: Int, value: V) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: V): V = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Int): V?

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): V = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Int, value: V): Boolean

    public fun clear()

    override val keys: IntSet
    override val values: Collection<V>

    public fun putAll(from: Int2AnyMap<V>) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Int, V>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry<V>>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry<V> : Int2AnyMap.Entry<V> {
        override var value: V
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry<V> : Int2AnyMap.AbstractEntry<V>(), MutableEntry<V>
}

public fun <V> MutableInt2AnyMap<V>.asMutableMap(): MutableMap<Int, V> = MutableInt2AnyMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableInt2AnyMap<V>.merge(key: Int, value: V, merge: (oldValue: V, value: V) -> V): V {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as V, value)
    if (absent || !(newValue equalsBoxed oldValue)) {
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

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableInt2AnyMap<V>.replaceOrSet(key: Int, value: V, oldValue: () -> V): V {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as V
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableInt2AnyMap<V>.removeOrElse(key: Int, defaultValue: () -> V): V {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as V
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2AnyMap<V> : Int2AnyMap<V> {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2AnyMap<*>) {
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

    public class SimpleEntry<V>(override val key: Int, override val value: V) : Int2AnyMap.Entry<V>
}

public abstract class AbstractMutableInt2AnyMap<V> : AbstractInt2AnyMap<V>(), MutableInt2AnyMap<V>


private object EmptyInt2AnyMap : Int2AnyMap<Nothing> {


    override fun isDefaultValue(value: Nothing?): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false
    override fun get(key: Int): Nothing? = null


    override val keys: IntSet get() = emptyIntSet()

    override val values: Collection<Nothing> get() = emptyList()
    override fun iterator(): Iterator<Int2AnyMap.Entry<Nothing>> = emptyList<Int2AnyMap.Entry<Nothing>>().iterator()



    override fun traverser(): Int2AnyTraverser<Nothing>  = emptyInt2AnyTraverser()

}

private class SingletonInt2AnyMap<V>(private val key: Int, private val value: V) : Int2AnyMap<V> {
    override fun isDefaultValue(value: V?): Boolean = value equalsBoxed null

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsBoxed this.key
    override fun containsValue(value: V): Boolean = value equalsBoxed this.value
    override fun get(key: Int): V? = if (key equalsBoxed this.key) value else null

    override val keys: IntSet by lazy { intSetOf(key) }

    override val values: Collection<V> by lazy { listOf(value) }

    override fun iterator() = object : Iterator<Int2AnyMap.Entry<V>> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2AnyMap.Entry<V> {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2AnyMap.SimpleEntry(key, value)
        }
    }

    override fun traverser(): Int2AnyTraverser<V> = object : Int2AnyTraverser<V> {
        private var consumed = false
        override val key: Int get() = this@SingletonInt2AnyMap.key
        override val value: V get() = this@SingletonInt2AnyMap.value
        override fun forward(): Boolean {
            if (consumed) return false
            consumed = true
            return true
        }
    }
}

private class Int2AnyMapWrapper<V>(private val map: Int2AnyMap<V>) : AbstractMap<Int, V>() {
    override val size: Int get() = map.size

    override fun get(key: Int): V? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, V>> = object : AbstractSet<Map.Entry<Int, V>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, V>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsBoxed element.value
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

    override fun get(key: Int): V? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

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
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsBoxed element.value
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
                if (!(map.put(key, newValue) equalsBoxed oldValue)) throw ConcurrentModificationException()
                value = newValue
                return oldValue
            }

            override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
            override fun toString(): String = "$key=$value"
        }
    }

    override fun putAll(from: Map<out Int, V>): Unit = map.putAll(from)
}


public fun interface IntAnyConsumer<in V> {

    public fun accept(key: Int, value: V)
}
