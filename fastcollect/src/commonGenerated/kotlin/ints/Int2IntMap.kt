package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.emptyEntrySet
import io.github.sooniln.fastcollect.entrySetOf
import io.github.sooniln.fastcollect.ints.intListOf
import io.github.sooniln.fastcollect.ints.IntCollection
import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.emptyIntList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

public fun emptyInt2IntMap(): Int2IntMap = EmptyInt2IntMap

public fun int2IntMapOf(): Int2IntMap = EmptyInt2IntMap
public fun int2IntMapOf(entry: Pair<Int, Int>): Int2IntMap = SingletonInt2IntMap(entry.first, entry.second)
public fun int2IntMapOf(vararg entries: Pair<Int, Int>): Int2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun mutableInt2IntMapOf(): MutableInt2IntMap = Int2IntHashMap()
public fun mutableInt2IntMapOf(entry: Pair<Int, Int>): MutableInt2IntMap = Int2IntHashMap(1).apply { set(entry.first, entry.second) }
public fun mutableInt2IntMapOf(vararg entries: Pair<Int, Int>): MutableInt2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildInt2IntSet(expectedSize: Int = 0, builderAction: MutableInt2IntMap.() -> Unit): Int2IntMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val map = Int2IntHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Ints which inherits from [Map].
 *
 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Int2IntMap has a
 * [defaultValue] which is returned instead to indicate no such key is present. Thus in order to obtain the best
 * performance implementations and clients are encouraged to ensure that the [defaultValue] is the value which is least
 * likely to ever appear in the possible set of values stored in this map. This is purely a performance and not a
 * correctness concern however - the map will still operate correctly and all methods will perform as expected even if
 * the map contains values equal to [defaultValue]. [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if
 * applicable.
 */
public interface Int2IntMap : Map<Int, Int> {
    public val defaultValue: Int

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: Int): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }

    @Deprecated(
        message = "Use the lookup(key) method instead.",
        replaceWith = ReplaceWith("lookup(key)"),
        level = DeprecationLevel.WARNING)
    override fun get(key: Int): Int? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun getOrDefault(key: Int, defaultValue: Int): Int = getOrElse(key) { defaultValue }

    /**
     * Returns the value associated with the given key, or [defaultValue] if the given key is not present in the map.
     */
    public fun lookup(key: Int): Int

    override val keys: IntSet

    override val values: IntCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, Int>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Int, Int> {
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
        override val value: Int get() {
            assertBoxing()
            return value()
        }

        public fun key(): Int
        public fun value(): Int
    }

    public operator fun iterator(): Iterator<Int2IntMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Int2IntMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Int2IntMap.isDefaultValue(value: Int): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Int2IntMap.getValue(key: Int): Int = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun Int2IntMap.getOrElse(key: Int, defaultValue: () -> Int): Int {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Int2IntMap.filterTo(destination: MutableInt2IntMap, predicate: (key: Int, value: Int) -> Boolean): MutableInt2IntMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    for (entry in primitiveEntries.fastIterator()) {
        if (predicate(entry.key(), entry.value())) {
            destination.putValue(entry.key(), entry.value())
        }
    }
    return destination
}

@OptIn(ExperimentalContracts::class)
public inline fun Int2IntMap.filter(predicate: (key: Int, value: Int) -> Boolean): Int2IntMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Int2IntHashMap(), predicate)
}

/**
 * A mutable map of Ints to Ints which inherits from [MutableMap].
 */
public interface MutableInt2IntMap : Int2IntMap, MutableMap<Int, Int> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Int, value: Int): Int? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Int, value: Int): Int

    public operator fun set(key: Int, value: Int) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Int): Int? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Int): Int

    public fun merge(key: Int, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
        val oldValue = lookup(key)
        val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
        if (newValue != oldValue) {
            putValue(key, newValue)
        }
        return newValue
    }

    override val keys: MutableIntSet
    override val values: MutableIntCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Int>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Int>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Int2IntMap.Entry, MutableMap.MutableEntry<Int, Int>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableInt2IntMap.getOrPut(key: Int, defaultValue: () -> Int): Int {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    var value = lookup(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        putValue(key, value)
    }
    return value
}

public abstract class AbstractInt2IntMap : Int2IntMap {

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

    public class SimpleEntry(private val _key: Int, private val _value: Int) : Int2IntMap.Entry {
        override fun key(): Int = _key
        override fun value(): Int = _value
    }
}

public abstract class AbstractMutableInt2IntMap : AbstractInt2IntMap(), MutableInt2IntMap {

    public class SimpleMutableEntry(private val _key: Int, private var _value: Int) : MutableInt2IntMap.MutableEntry {
        override fun key(): Int = _key
        override fun value(): Int = _value
        override fun setValue(newValue: Int): Int {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}

private object EmptyInt2IntMap : Int2IntMap {
    override val defaultValue: Int get() = Int.MIN_VALUE

    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false
    override fun containsValue(value: Int): Boolean = false
    override fun lookup(key: Int): Int = Int.MIN_VALUE

    override val keys: IntSet get() = emptyIntSet()
    override val values: IntCollection get() = emptyIntList()
    override val primitiveEntries: EntrySet<Int2IntMap.Entry> = emptyEntrySet()
}

private class SingletonInt2IntMap(private val key: Int, private val value: Int) : Int2IntMap {
    override val defaultValue: Int get() = Int.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: Int): Boolean = value == this.value
    override fun lookup(key: Int): Int = if (key == this.key) value else Int.MIN_VALUE

    override val keys: IntSet by lazy { intSetOf(key) }
    override val values: IntCollection by lazy { intListOf(value) }
    override val primitiveEntries: EntrySet<Int2IntMap.Entry> by lazy { entrySetOf(AbstractInt2IntMap.SimpleEntry(key, value)) }
}
