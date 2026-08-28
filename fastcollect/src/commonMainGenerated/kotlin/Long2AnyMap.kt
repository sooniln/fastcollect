/**
 * Methods for dealing with Long2AnyMaps.
 */
@file:JvmName("Long2AnyMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun <V> emptyLong2AnyMap(): Long2AnyMap<V> = EmptyLong2AnyMap as Long2AnyMap<V>

public fun <V> long2AnyMapOf(): Long2AnyMap<V> = EmptyLong2AnyMap as Long2AnyMap<V>
public fun <V> long2AnyMapOf(entry: Pair<Long, V>): Long2AnyMap<V> = SingletonLong2AnyMap(entry.first, entry.second)
public fun <V> long2AnyMapOf(vararg entries: Pair<Long, V>): Long2AnyMap<V> = Long2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun <V> mutableLong2AnyMapOf(): MutableLong2AnyMap<V> = Long2AnyHashMap()
public fun <V> mutableLong2AnyMapOf(entry: Pair<Long, V>): MutableLong2AnyMap<V> = Long2AnyHashMap<V>(1).apply { set(entry.first, entry.second) }
public fun <V> mutableLong2AnyMapOf(vararg entries: Pair<Long, V>): MutableLong2AnyMap<V> = Long2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <V> buildLong2AnyMap(expectedSize: Int = 0, builderAction: MutableLong2AnyMap<V>.() -> Unit): Long2AnyMap<V> {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2AnyHashMap<V>(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Vs.
 *
 * A Long2AnyMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Long2AnyMap<out V> : Long2AnyTraversable<V> {

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

    public operator fun get(key: Long): V?

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Long): V = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Long, defaultValue: @UnsafeVariance V): V = getOrElse(key) { defaultValue }

    public fun containsKey(key: Long): Boolean {
        foreachKey { k ->
            if (k equalsRaw key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance V): Boolean {
        foreach { _, v ->
            if (v equalsRaw value) return true
        }
        return false
    }

    public val keys: LongSet
    public val values: Collection<V>

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): Collection<V> = values

    public operator fun iterator(): Iterator<Entry<V>>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry<out V> {
        public val key: Long
        public val value: V

        public operator fun component1(): Long = key
        public operator fun component2(): V = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry<out V> : Entry<V> {
        final override fun equals(other: Any?): Boolean = other is Entry<*> && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun <V> Long2AnyMap<V>.asMap(): Map<Long, V> = Long2AnyMapWrapper(this)

public fun <V> Long2AnyMap.Entry<V>.asEntry(): Map.Entry<Long, V> = Long2AnyMapEntryWrapper(this)

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
public interface MutableLong2AnyMap<V> : Long2AnyMap<V>, MutableLong2AnyTraversable<V> {

    public fun put(key: Long, value: V): V?

    public fun putIfAbsent(key: Long, value: V): V? {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

    public operator fun set(key: Long, value: V) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Long, value: V): V = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Long): V?

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Long): V = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Long, value: V): Boolean

    public fun clear()

    public fun putAll(from: Long2AnyMap<V>) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Long, V>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry<V>>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry<V> : Long2AnyMap.Entry<V> {
        override var value: V
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry<V> : Long2AnyMap.AbstractEntry<V>(), MutableEntry<V>
}

public fun <V> MutableLong2AnyMap<V>.asMap(): MutableMap<Long, V> = MutableLong2AnyMapWrapper(this)

public fun <V> MutableLong2AnyMap.MutableEntry<V>.asEntry(): MutableMap.MutableEntry<Long, V> = MutableLong2AnyMapEntryWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableLong2AnyMap<V>.merge(key: Long, value: V, merge: (oldValue: V, value: V) -> V): V {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as V, value)
    if (absent || !(newValue equalsRaw oldValue)) {
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

@OptIn(ExperimentalContracts::class)
public inline fun <V> MutableLong2AnyMap<V>.replaceOrSet(key: Long, value: V, oldValue: () -> V): V {
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
public inline fun <V> MutableLong2AnyMap<V>.removeOrElse(key: Long, defaultValue: () -> V): V {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as V
    } else {
        defaultValue()
    }
}

public abstract class AbstractLong2AnyMap<V> : Long2AnyMap<V> {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Long2AnyMap<*>) {
            if (other.size != size) return false

            foreach { key, value ->
                if (!(other.getOrElse(key) { return false } equalsRaw value)) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        foreach { key, value ->
            result += key.hashCode() xor value.hashCode()
        }
        return result
    }

    override fun toString(): String = Iterable { iterator() }.joinToString(", ", "{", "}")

    public class SimpleEntry<V>(override val key: Long, override val value: V) : Long2AnyMap.AbstractEntry<V>()
}

public abstract class AbstractMutableLong2AnyMap<V> : AbstractLong2AnyMap<V>(), MutableLong2AnyMap<V>


private object EmptyLong2AnyMap : AbstractLong2AnyMap<Nothing>() {


    override fun isDefaultValue(value: Nothing?): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false
    override fun get(key: Long): Nothing? = null


    override val keys: LongSet get() = emptyLongSet()

    override val values: Collection<Nothing> get() = emptyList()
    override fun iterator(): Iterator<Long2AnyMap.Entry<Nothing>> = emptyList<Long2AnyMap.Entry<Nothing>>().iterator()



    override fun traverser(): Long2AnyTraverser<Nothing>  = emptyLong2AnyTraverser()

}

private class SingletonLong2AnyMap<V>(
    private val key: Long,
    private val value: V
) : AbstractLong2AnyMap<V>() {
    override fun isDefaultValue(value: V?): Boolean = value equalsRaw null

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key equalsRaw this.key
    override fun containsValue(value: V): Boolean = value equalsRaw this.value
    override fun get(key: Long): V? = if (key equalsRaw this.key) value else null

    override val keys: LongSet get() = longSetOf(key)

    override val values: Collection<V> get() = listOf(value)

    override fun iterator() = object : Iterator<Long2AnyMap.Entry<V>> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2AnyMap.Entry<V> {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Long2AnyTraverser<V> = object : Long2AnyTraverser<V> {
        private var complete = false
        override val key: Long get() {
            check(complete)
            return this@SingletonLong2AnyMap.key
        }
        override val value: V get() {
            check(complete)
            return this@SingletonLong2AnyMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Long2AnyMapWrapper<V>(private val map: Long2AnyMap<V>) : AbstractMap<Long, V>() {
    override val size: Int get() = map.size

    override fun get(key: Long): V? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: V): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, V>> = object : AbstractSet<Map.Entry<Long, V>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, V>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, V>> = object : Iterator<Map.Entry<Long, V>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, V> = it.next().asEntry()
        }
    }
}

private class Long2AnyMapEntryWrapper<V>(
    private val entry: Long2AnyMap.Entry<V>
) : Map.Entry<Long, V> {
    override val key: Long get() = entry.key
    override val value: V get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableLong2AnyMapWrapper<V>(private val map: MutableLong2AnyMap<V>) : AbstractMutableMap<Long, V>() {
    override val size: Int get() = map.size

    override fun get(key: Long): V? {
        val value = map[key]
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
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, V>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, V>> = object : MutableIterator<MutableMap.MutableEntry<Long, V>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, V> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Long, V>): Unit = map.putAll(from)
}

private class MutableLong2AnyMapEntryWrapper<V>(
    private val entry: MutableLong2AnyMap.MutableEntry<V>
) : MutableMap.MutableEntry<Long, V> {
    override val key: Long get() = entry.key
    override val value: V get() = entry.value
    override fun setValue(newValue: V): V {
        val oldValue = entry.value
        entry.value = newValue
        return oldValue
    }
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}


public fun interface LongAnyConsumer<in V> {

    public fun accept(key: Long, value: V)
}
