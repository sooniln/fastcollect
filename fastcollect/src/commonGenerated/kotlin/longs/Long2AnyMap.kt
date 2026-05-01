@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.emptyEntrySet
import io.github.sooniln.fastcollect.entrySetOf

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
 * A map of Longs to Vs which inherits from [Map].
 *

 */
public interface Long2AnyMap<V> : Map<Long, V> {


    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: V): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }


    override fun get(key: Long): V? = lookup(key)


    public fun getOrDefault(key: Long, defaultValue: V): V = getOrElse(key) { defaultValue }


    /**
     * Returns the value associated with the given key, or null if the given key is not present in the map.
     */

    public fun lookup(key: Long): V?

    override val keys: LongSet

    override val values: Collection<V>

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Long, V>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry<V>>

    public interface Entry<V> : Map.Entry<Long, V> {
        @Deprecated(
            message = "Use key() instead.",
            replaceWith = ReplaceWith("key()"),
            level = DeprecationLevel.WARNING)
        override val key: Long get() {
            assertBoxing()
            return key()
        }

        @Deprecated(
            message = "Use value() instead.",
            replaceWith = ReplaceWith("key()"),
            level = DeprecationLevel.WARNING)
        override val value: V get() {
            assertBoxing()
            return value()
        }

        public fun key(): Long
        public fun value(): V
    }

    public operator fun iterator(): Iterator<Entry<V>> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Entry<V>> = primitiveEntries.fastIterator()
}



/**
 * A mutable map of Longs to Vs which inherits from [MutableMap].
 */
public interface MutableLong2AnyMap<V> : Long2AnyMap<V>, MutableMap<Long, V> {


    override fun put(key: Long, value: V): V? = putValue(key, value)



    /**
     * Updates the value associated with the given key and returns the previous value, or null if the given key was not
     * present previously.
     */

    public fun putValue(key: Long, value: V): V?

    public operator fun set(key: Long, value: V) {
        putValue(key, value)
    }


    override fun remove(key: Long): V? = removeKey(key)


    public fun removeKey(key: Long): V?



    override val keys: MutableLongSet
    override val values: MutableCollection<V>

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, V>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, V>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry<V>>

    public interface MutableEntry<V> : Long2AnyMap.Entry<V>, MutableMap.MutableEntry<Long, V>
}



public abstract class AbstractLong2AnyMap<V> : Long2AnyMap<V> {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Map<*, *>) {
            if (other.size != size) return false

            for (entry in fastIterator()) {
                if (other[entry.key()] != entry.value()) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        for (entry in fastIterator()) {
            result += entry.key().hashCode() xor entry.value().hashCode()
        }
        return result
    }

    override fun toString(): String {
        return Iterable { fastIterator() }.joinToString(", ", "{", "}") { "${it.key()}=${it.value()}" }
    }

    public class SimpleEntry<V>(private val _key: Long, private val _value: V) : Long2AnyMap.Entry<V> {
        override fun key(): Long = _key
        override fun value(): V = _value
    }
}

public abstract class AbstractMutableLong2AnyMap<V> : AbstractLong2AnyMap<V>(), MutableLong2AnyMap<V> {

    public class SimpleMutableEntry<V>(private val _key: Long, private var _value: V) : MutableLong2AnyMap.MutableEntry<V> {
        override fun key(): Long = _key
        override fun value(): V = _value
        override fun setValue(newValue: V): V {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}


private object EmptyLong2AnyMap : Long2AnyMap<Nothing> {




    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false
    override fun lookup(key: Long): Nothing? = null



    override val keys: LongSet get() = emptyLongSet()

    override val values: Collection<Nothing> get() = emptyList()
    override val primitiveEntries: EntrySet<Long2AnyMap.Entry<Nothing>> = emptyEntrySet()

}

private class SingletonLong2AnyMap<V>(private val key: Long, private val value: V) : Long2AnyMap<V> {


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: V): Boolean = value == this.value
    override fun lookup(key: Long): V? = if (key == this.key) value else null

    override val keys: LongSet by lazy { longSetOf(key) }


    override val values: Collection<V> by lazy { listOf(value) }


    override val primitiveEntries: EntrySet<Long2AnyMap.Entry<V>> by lazy { entrySetOf(AbstractLong2AnyMap.SimpleEntry(key, value)) }
}
