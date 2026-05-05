@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.emptyEntrySet
import io.github.sooniln.fastcollect.entrySetOf

import io.github.sooniln.fastcollect.floats.floatListOf
import io.github.sooniln.fastcollect.floats.FloatCollection
import io.github.sooniln.fastcollect.floats.MutableFloatCollection
import io.github.sooniln.fastcollect.floats.emptyFloatList

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2FloatMap(): Int2FloatMap = EmptyInt2FloatMap as Int2FloatMap

@Suppress("UNCHECKED_CAST")
public fun  int2FloatMapOf(): Int2FloatMap = EmptyInt2FloatMap as Int2FloatMap
public fun  int2FloatMapOf(entry: Pair<Int, Float>): Int2FloatMap = SingletonInt2FloatMap(entry.first, entry.second)
public fun  int2FloatMapOf(vararg entries: Pair<Int, Float>): Int2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2FloatMapOf(): MutableInt2FloatMap = Int2FloatHashMap()
public fun  mutableInt2FloatMapOf(entry: Pair<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2FloatMapOf(vararg entries: Pair<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2FloatMap(expectedSize: Int = 0, builderAction: MutableInt2FloatMap.() -> Unit): Int2FloatMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2FloatHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Floats which inherits from [Map].
 *

 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Int2FloatMap has a
 * [defaultValue] which is returned instead to indicate no such key is present. Thus in order to obtain the best
 * performance implementations and clients are encouraged to ensure that the [defaultValue] is the value which is least
 * likely to ever appear in the possible set of values stored in this map. This is purely a performance and not a
 * correctness concern however - the map will still operate correctly and all methods will perform as expected even if
 * the map contains values equal to [defaultValue]. [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if
 * applicable.

 */
public interface Int2FloatMap : Map<Int, Float> {

    public val defaultValue: Float


    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun containsValue(value: Float): Boolean {
        for (entry in primitiveEntries) {
            if (entry.value() == value) return true
        }
        return false
    }


    @Deprecated(
        message = "Use the lookup(key) method instead.",
        replaceWith = ReplaceWith("lookup(key)"),
        level = DeprecationLevel.WARNING)
    override fun get(key: Int): Float? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }


    public fun getOrDefault(key: Int, defaultValue: Float): Float = getOrElse(key) { defaultValue }


    /**
     * Returns the value associated with the given key, or [defaultValue] if the given key is not present in the map.
     */

    public fun lookup(key: Int): Float

    override val keys: IntSet

    override val values: FloatCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Int, Float>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Int, Float> {
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
        override val value: Float get() {
            assertBoxing()
            return value()
        }

        public fun key(): Int
        public fun value(): Float
    }

    public operator fun iterator(): Iterator<Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2FloatMap.isDefaultValue(value: Float): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun  Int2FloatMap.getValue(key: Int): Float = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatMap.getOrElse(key: Int, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}


/**
 * A mutable map of Ints to Floats which inherits from [MutableMap].
 */
public interface MutableInt2FloatMap : Int2FloatMap, MutableMap<Int, Float> {


    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Int, value: Float): Float? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }



    /**
     * Updates the value associated with the given key and returns the previous value, or [defaultValue] if the given
     * key was not present previously.
     */

    public fun putValue(key: Int, value: Float): Float

    public operator fun set(key: Int, value: Float) {
        putValue(key, value)
    }


    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Int): Float? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }


    public fun removeKey(key: Int): Float


    public fun merge(key: Int, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
        val oldValue = lookup(key)
        val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
        if (newValue != oldValue) {
            putValue(key, newValue)
        }
        return newValue
    }


    override val keys: MutableIntSet
    override val values: MutableFloatCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, Float>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Int, Float>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Int2FloatMap.Entry, MutableMap.MutableEntry<Int, Float>

    override operator fun iterator(): MutableIterator<MutableEntry> = primitiveEntries.iterator()

    override fun fastIterator(): MutableFastIterator<MutableEntry> = primitiveEntries.fastIterator()
}


@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2FloatMap.getOrPut(key: Int, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = lookup(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        putValue(key, value)
    }
    return value
}


public abstract class AbstractInt2FloatMap : Int2FloatMap {

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

    public class SimpleEntry(private val _key: Int, private val _value: Float) : Int2FloatMap.Entry {
        override fun key(): Int = _key
        override fun value(): Float = _value
    }
}

public abstract class AbstractMutableInt2FloatMap : AbstractInt2FloatMap(), MutableInt2FloatMap {

    public class SimpleMutableEntry(private val _key: Int, private var _value: Float) : MutableInt2FloatMap.MutableEntry {
        override fun key(): Int = _key
        override fun value(): Float = _value
        override fun setValue(newValue: Float): Float {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}


private object EmptyInt2FloatMap : Int2FloatMap {



    override val defaultValue: Float get() = Float.NaN


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Float): Boolean = false
    override fun lookup(key: Int): Float = Float.NaN



    override val keys: IntSet get() = emptyIntSet()

    override val values: FloatCollection get() = emptyFloatList()
    override val primitiveEntries: EntrySet<Int2FloatMap.Entry> = emptyEntrySet()

}

private class SingletonInt2FloatMap(private val key: Int, private val value: Float) : Int2FloatMap {

    override val defaultValue: Float get() = Float.NaN


    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key == this.key
    override fun containsValue(value: Float): Boolean = value == this.value
    override fun lookup(key: Int): Float = if (key == this.key) value else Float.NaN

    override val keys: IntSet by lazy { intSetOf(key) }


    override val values: FloatCollection by lazy { floatListOf(value) }


    override val primitiveEntries: EntrySet<Int2FloatMap.Entry> by lazy { entrySetOf(AbstractInt2FloatMap.SimpleEntry(key, value)) }
}
