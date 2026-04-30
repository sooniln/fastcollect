package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.assertBoxing
import io.github.sooniln.fastcollect.EntrySet
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
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

public fun emptyLong2FloatMap(): Long2FloatMap = EmptyLong2FloatMap

public fun long2FloatMapOf(): Long2FloatMap = EmptyLong2FloatMap
public fun long2FloatMapOf(entry: Pair<Long, Float>): Long2FloatMap = SingletonLong2FloatMap(entry.first, entry.second)
public fun long2FloatMapOf(vararg entries: Pair<Long, Float>): Long2FloatMap = Long2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun mutableLong2FloatMapOf(): MutableLong2FloatMap = Long2FloatHashMap()
public fun mutableLong2FloatMapOf(entry: Pair<Long, Float>): MutableLong2FloatMap = Long2FloatHashMap(1).apply { set(entry.first, entry.second) }
public fun mutableLong2FloatMapOf(vararg entries: Pair<Long, Float>): MutableLong2FloatMap = Long2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildLong2FloatSet(expectedSize: Int = 0, builderAction: MutableLong2FloatMap.() -> Unit): Long2FloatMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val map = Long2FloatHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Floats which inherits from [Map].
 *
 * Because this interface is designed to store primitives, methods which lookup keys and return non-nullable primitive
 * values may not return null to indicate no such key is present. Instead, a Long2FloatMap has a
 * [defaultValue] which is returned instead to indicate no such key is present. Thus in order to obtain the best
 * performance implementations and clients are encouraged to ensure that the [defaultValue] is the value which is least
 * likely to ever appear in the possible set of values stored in this map. This is purely a performance and not a
 * correctness concern however - the map will still operate correctly and all methods will perform as expected even if
 * the map contains values equal to [defaultValue]. [Float.NaN] or [Double.NaN] are acceptable for [defaultValue] if
 * applicable.
 */
public interface Long2FloatMap : Map<Long, Float> {
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
    override fun get(key: Long): Float? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun getOrDefault(key: Long, defaultValue: Float): Float = getOrElse(key) { defaultValue }

    /**
     * Returns the value associated with the given key, or [defaultValue] if the given key is not present in the map.
     */
    public fun lookup(key: Long): Float

    override val keys: LongSet

    override val values: FloatCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Long, Float>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Long, Float> {
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
        override val value: Float get() {
            assertBoxing()
            return value()
        }

        public fun key(): Long
        public fun value(): Float
    }

    public operator fun iterator(): Iterator<Long2FloatMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Long2FloatMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Long2FloatMap.isDefaultValue(value: Float): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Long2FloatMap.getValue(key: Long): Float = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun Long2FloatMap.getOrElse(key: Long, defaultValue: () -> Float): Float {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Long2FloatMap.filterTo(destination: MutableLong2FloatMap, predicate: (key: Long, value: Float) -> Boolean): MutableLong2FloatMap {
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
public inline fun Long2FloatMap.filter(predicate: (key: Long, value: Float) -> Boolean): Long2FloatMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Long2FloatHashMap(), predicate)
}

/**
 * A mutable map of Longs to Floats which inherits from [MutableMap].
 */
public interface MutableLong2FloatMap : Long2FloatMap, MutableMap<Long, Float> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Long, value: Float): Float? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Long, value: Float): Float

    public operator fun set(key: Long, value: Float) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Long): Float? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Float

    public fun merge(key: Long, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
        val oldValue = lookup(key)
        val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
        if (newValue != oldValue) {
            putValue(key, newValue)
        }
        return newValue
    }

    override val keys: MutableLongSet
    override val values: MutableFloatCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Float>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Float>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Long2FloatMap.Entry, MutableMap.MutableEntry<Long, Float>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2FloatMap.getOrPut(key: Long, defaultValue: () -> Float): Float {
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

public abstract class AbstractLong2FloatMap : Long2FloatMap {

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

    public class SimpleEntry(private val _key: Long, private val _value: Float) : Long2FloatMap.Entry {
        override fun key(): Long = _key
        override fun value(): Float = _value
    }
}

public abstract class AbstractMutableLong2FloatMap : AbstractLong2FloatMap(), MutableLong2FloatMap {

    public class SimpleMutableEntry(private val _key: Long, private var _value: Float) : MutableLong2FloatMap.MutableEntry {
        override fun key(): Long = _key
        override fun value(): Float = _value
        override fun setValue(newValue: Float): Float {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}

private object EmptyLong2FloatMap : Long2FloatMap {
    override val defaultValue: Float get() = Float.NaN

    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false
    override fun containsValue(value: Float): Boolean = false
    override fun lookup(key: Long): Float = Float.NaN

    override val keys: LongSet get() = emptyLongSet()
    override val values: FloatCollection get() = emptyFloatList()
    override val primitiveEntries: EntrySet<Long2FloatMap.Entry> = emptyEntrySet()
}

private class SingletonLong2FloatMap(private val key: Long, private val value: Float) : Long2FloatMap {
    override val defaultValue: Float get() = Float.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: Float): Boolean = value == this.value
    override fun lookup(key: Long): Float = if (key == this.key) value else Float.NaN

    override val keys: LongSet by lazy { longSetOf(key) }
    override val values: FloatCollection by lazy { floatListOf(value) }
    override val primitiveEntries: EntrySet<Long2FloatMap.Entry> by lazy { entrySetOf(AbstractLong2FloatMap.SimpleEntry(key, value)) }
}
