@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyEntrySet
import io.github.sooniln.fastcollect.entrySetOf

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
 * A map of Ints to Vs which inherits from [Map].
 *

 */
public interface Int2AnyMap<V> : Map<Int, V> {


    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: V): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }


    override fun get(key: Int): V? = lookup(key)


    public fun getOrDefault(key: Int, defaultValue: V): V = getOrElse(key) { defaultValue }


    /**
     * Returns the value associated with the given key, or null if the given key is not present in the map.
     */

    public fun lookup(key: Int): V?

    override val keys: IntSet

    override val values: Collection<V>

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, V>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry<V>>

    public interface Entry<V> : Map.Entry<Int, V> {
        @Deprecated(
            message = "Use key() instead.",
            replaceWith = ReplaceWith("key()"),
            level = DeprecationLevel.WARNING)
        override val key: Int get() {
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

        public fun key(): Int
        public fun value(): V
    }

    public operator fun iterator(): Iterator<Entry<V>> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Entry<V>> = primitiveEntries.fastIterator()
}



/**
 * A mutable map of Ints to Vs which inherits from [MutableMap].
 */
public interface MutableInt2AnyMap<V> : Int2AnyMap<V>, MutableMap<Int, V> {


    override fun put(key: Int, value: V): V? = putValue(key, value)



    /**
     * Updates the value associated with the given key and returns the previous value, or null if the given key was not
     * present previously.
     */

    public fun putValue(key: Int, value: V): V?

    public operator fun set(key: Int, value: V) {
        putValue(key, value)
    }


    override fun remove(key: Int): V? = removeKey(key)


    public fun removeKey(key: Int): V?



    override val keys: MutableIntSet
    override val values: MutableCollection<V>

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, V>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, V>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry<V>>

    public interface MutableEntry<V> : Int2AnyMap.Entry<V>, MutableMap.MutableEntry<Int, V>

    override operator fun iterator(): MutableIterator<MutableEntry<V>> = primitiveEntries.iterator()

    override fun fastIterator(): MutableFastIterator<MutableEntry<V>> = primitiveEntries.fastIterator()
}



public abstract class AbstractInt2AnyMap<V> : Int2AnyMap<V> {

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

    public class SimpleEntry<V>(private val _key: Int, private val _value: V) : Int2AnyMap.Entry<V> {
        override fun key(): Int = _key
        override fun value(): V = _value
    }
}

public abstract class AbstractMutableInt2AnyMap<V> : AbstractInt2AnyMap<V>(), MutableInt2AnyMap<V> {

    public class SimpleMutableEntry<V>(private val _key: Int, private var _value: V) : MutableInt2AnyMap.MutableEntry<V> {
        override fun key(): Int = _key
        override fun value(): V = _value
        override fun setValue(newValue: V): V {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}


private object EmptyInt2AnyMap : Int2AnyMap<Nothing> {




    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Nothing): Boolean = false
    override fun lookup(key: Int): Nothing? = null



    override val keys: IntSet get() = emptyIntSet()

    override val values: Collection<Nothing> get() = emptyList()
    override val primitiveEntries: EntrySet<Int2AnyMap.Entry<Nothing>> = emptyEntrySet()

}

private class SingletonInt2AnyMap<V>(private val key: Int, private val value: V) : Int2AnyMap<V> {


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: V): Boolean = value == this.value
    override fun lookup(key: Int): V? = if (key == this.key) value else null

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: Collection<V> by lazy { listOf(value) }


    override val primitiveEntries: EntrySet<Int2AnyMap.Entry<V>> by lazy { entrySetOf(AbstractInt2AnyMap.SimpleEntry(key, value)) }
}
