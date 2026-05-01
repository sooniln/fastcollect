@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.emptyEntrySet
import io.github.sooniln.fastcollect.entrySetOf

import io.github.sooniln.fastcollect.doubles.doubleListOf
import io.github.sooniln.fastcollect.doubles.DoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import io.github.sooniln.fastcollect.doubles.emptyDoubleList

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2DoubleMap(): Int2DoubleMap = EmptyInt2DoubleMap as Int2DoubleMap

@Suppress("UNCHECKED_CAST")
public fun  int2DoubleMapOf(): Int2DoubleMap = EmptyInt2DoubleMap as Int2DoubleMap
public fun  int2DoubleMapOf(entry: Pair<Int, Double>): Int2DoubleMap = SingletonInt2DoubleMap(entry.first, entry.second)
public fun  int2DoubleMapOf(vararg entries: Pair<Int, Double>): Int2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2DoubleMapOf(): MutableInt2DoubleMap = Int2DoubleHashMap()
public fun  mutableInt2DoubleMapOf(entry: Pair<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2DoubleMapOf(vararg entries: Pair<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2DoubleMap(expectedSize: Int = 0, builderAction: MutableInt2DoubleMap.() -> Unit): Int2DoubleMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val map = Int2DoubleHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Doubles which inherits from [Map].
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Int2DoubleMap has a
 * [defaultValue] which is returned instead to indicate no such key is present. Thus in order to obtain the best
 * performance implementations and clients are encouraged to ensure that the [defaultValue] is the value which is least
 * likely to ever appear in the possible set of values stored in this map. This is purely a performance and not a
 * correctness concern however - the map will still operate correctly and all methods will perform as expected even if
 * the map contains values equal to [defaultValue]. [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if
 * applicable.

 */
public interface Int2DoubleMap : Map<Int, Double> {

    public val defaultValue: Double


    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: Double): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }


    @Deprecated(
        message = "Use the lookup(key) method instead.",
        replaceWith = ReplaceWith("lookup(key)"),
        level = DeprecationLevel.WARNING)
    override fun get(key: Int): Double? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }


    public fun getOrDefault(key: Int, defaultValue: Double): Double = getOrElse(key) { defaultValue }


    /**
     * Returns the value associated with the given key, or [defaultValue] if the given key is not present in the map.
     */

    public fun lookup(key: Int): Double

    override val keys: IntSet

    override val values: DoubleCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, Double>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Int, Double> {
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
        override val value: Double get() {
            assertBoxing()
            return value()
        }

        public fun key(): Int
        public fun value(): Double
    }

    public operator fun iterator(): Iterator<Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2DoubleMap.isDefaultValue(value: Double): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2DoubleMap.getValue(key: Int): Double = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleMap.getOrElse(key: Int, defaultValue: () -> Double): Double {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleMap.filterTo(destination: MutableInt2DoubleMap, predicate: (key: Int, value: Double) -> Boolean): MutableInt2DoubleMap {
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
public inline fun  Int2DoubleMap.filter(predicate: (key: Int, value: Double) -> Boolean): Int2DoubleMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Int2DoubleHashMap(), predicate)
}


/**
 * A mutable map of Ints to Doubles which inherits from [MutableMap].
 */
public interface MutableInt2DoubleMap : Int2DoubleMap, MutableMap<Int, Double> {


    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Int, value: Double): Double? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }



    /**
     * Updates the value associated with the given key and returns the previous value, or [defaultValue] if the given
     * key was not present previously.
     */

    public fun putValue(key: Int, value: Double): Double

    public operator fun set(key: Int, value: Double) {
        putValue(key, value)
    }


    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Int): Double? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }


    public fun removeKey(key: Int): Double


    public fun merge(key: Int, value: Double, merge: (oldValue: Double, value: Double) -> Double): Double {
        val oldValue = lookup(key)
        val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
        if (newValue != oldValue) {
            putValue(key, newValue)
        }
        return newValue
    }


    override val keys: MutableIntSet
    override val values: MutableDoubleCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Double>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Double>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Int2DoubleMap.Entry, MutableMap.MutableEntry<Int, Double>
}


@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.getOrPut(key: Int, defaultValue: () -> Double): Double {
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


public abstract class AbstractInt2DoubleMap : Int2DoubleMap {

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

    public class SimpleEntry(private val _key: Int, private val _value: Double) : Int2DoubleMap.Entry {
        override fun key(): Int = _key
        override fun value(): Double = _value
    }
}

public abstract class AbstractMutableInt2DoubleMap : AbstractInt2DoubleMap(), MutableInt2DoubleMap {

    public class SimpleMutableEntry(private val _key: Int, private var _value: Double) : MutableInt2DoubleMap.MutableEntry {
        override fun key(): Int = _key
        override fun value(): Double = _value
        override fun setValue(newValue: Double): Double {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}


private object EmptyInt2DoubleMap : Int2DoubleMap {



    override val defaultValue: Double get() = Double.NaN


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Double): Boolean = false
    override fun lookup(key: Int): Double = Double.NaN



    override val keys: IntSet get() = emptyIntSet()

    override val values: DoubleCollection get() = emptyDoubleList()
    override val primitiveEntries: EntrySet<Int2DoubleMap.Entry> = emptyEntrySet()

}

private class SingletonInt2DoubleMap(private val key: Int, private val value: Double) : Int2DoubleMap {

    override val defaultValue: Double get() = Double.NaN


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: Double): Boolean = value == this.value
    override fun lookup(key: Int): Double = if (key == this.key) value else Double.NaN

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: DoubleCollection by lazy { doubleListOf(value) }


    override val primitiveEntries: EntrySet<Int2DoubleMap.Entry> by lazy { entrySetOf(AbstractInt2DoubleMap.SimpleEntry(key, value)) }
}
