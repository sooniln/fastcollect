package io.github.sooniln.fastcollect.longs

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

public fun emptyLong2DoubleMap(): Long2DoubleMap = EmptyLong2DoubleMap

public fun long2DoubleMapOf(): Long2DoubleMap = EmptyLong2DoubleMap
public fun long2DoubleMapOf(entry: Pair<Long, Double>): Long2DoubleMap = SingletonLong2DoubleMap(entry.first, entry.second)
public fun long2DoubleMapOf(vararg entries: Pair<Long, Double>): Long2DoubleMap = Long2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun mutableLong2DoubleMapOf(): MutableLong2DoubleMap = Long2DoubleHashMap()
public fun mutableLong2DoubleMapOf(entry: Pair<Long, Double>): MutableLong2DoubleMap = Long2DoubleHashMap(1).apply { set(entry.first, entry.second) }
public fun mutableLong2DoubleMapOf(vararg entries: Pair<Long, Double>): MutableLong2DoubleMap = Long2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildLong2DoubleSet(expectedSize: Int = 0, builderAction: MutableLong2DoubleMap.() -> Unit): Long2DoubleMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val map = Long2DoubleHashMap(expectedSize)
    map.builderAction()
    return map
}

public interface Long2DoubleMap : Map<Long, Double> {
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
    override fun get(key: Long): Double? {
        assertBoxing()
        val value = lookup(key)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun getOrDefault(key: Long, defaultValue: Double): Double = getOrElse(key) { defaultValue }

    public fun lookup(key: Long): Double

    override val keys: LongSet

    override val values: DoubleCollection

    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: Set<Map.Entry<Long, Double>>
        get() {
            assertBoxing()
            return primitiveEntries
        }

    public val primitiveEntries: EntrySet<Entry>

    public interface Entry : Map.Entry<Long, Double> {
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
        override val value: Double get() {
            assertBoxing()
            return value()
        }

        public fun key(): Long
        public fun value(): Double
    }

    public operator fun iterator(): Iterator<Long2DoubleMap.Entry> = primitiveEntries.iterator()

    public fun fastIterator(): FastIterator<Long2DoubleMap.Entry> = primitiveEntries.fastIterator()
}


// handles presence of NaN correctly
@Suppress("NOTHING_TO_INLINE")
public inline fun Long2DoubleMap.isDefaultValue(value: Double): Boolean = value == defaultValue || (defaultValue != defaultValue && value != value)

@Suppress("NOTHING_TO_INLINE")
public inline fun Long2DoubleMap.getValue(key: Long): Double = getOrElse(key) { throw NoSuchElementException() }

@OptIn(ExperimentalContracts::class)
public inline fun Long2DoubleMap.getOrElse(key: Long, defaultValue: () -> Double): Double {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }

    val value = lookup(key)
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value
}

@OptIn(ExperimentalContracts::class)
public inline fun Long2DoubleMap.filterTo(destination: MutableLong2DoubleMap, predicate: (key: Long, value: Double) -> Boolean): MutableLong2DoubleMap {
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
public inline fun Long2DoubleMap.filter(predicate: (key: Long, value: Double) -> Boolean): Long2DoubleMap {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
    }

    return filterTo(Long2DoubleHashMap(), predicate)
}

public interface MutableLong2DoubleMap : Long2DoubleMap, MutableMap<Long, Double> {

    @Deprecated(
        message = "Use putValue(key, value) instead.",
        replaceWith = ReplaceWith("putValue(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun put(key: Long, value: Double): Double? {
        assertBoxing()
        val value = putValue(key, value)
        return if (isDefaultValue(value) && !containsKey(key)) null else value
    }

    public fun putValue(key: Long, value: Double): Double

    public operator fun set(key: Long, value: Double) {
        putValue(key, value)
    }

    @Deprecated(
        message = "Use removeKey(key) instead.",
        replaceWith = ReplaceWith("removeKey(key, value)"),
        level = DeprecationLevel.WARNING)
    override fun remove(key: Long): Double? {
        assertBoxing()
        return if (containsKey(key)) removeKey(key) else null
    }

    public fun removeKey(key: Long): Double

    public fun merge(key: Long, value: Double, merge: (oldValue: Double, value: Double) -> Double): Double {
        val oldValue = lookup(key)
        val newValue = if (isDefaultValue(oldValue) && !containsKey(key)) value else merge(oldValue, value)
        if (newValue != oldValue) {
            putValue(key, newValue)
        }
        return newValue
    }

    override val keys: MutableLongSet
    override val values: MutableDoubleCollection

    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        message = "Use primitiveEntries instead.",
        replaceWith = ReplaceWith("primitiveEntries"),
        level = DeprecationLevel.WARNING)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Double>>
        get() {
            assertBoxing()
            return primitiveEntries as MutableSet<MutableMap.MutableEntry<Long, Double>>
        }

    override val primitiveEntries: MutableEntrySet<MutableEntry>

    public interface MutableEntry : Long2DoubleMap.Entry, MutableMap.MutableEntry<Long, Double>
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableLong2DoubleMap.getOrPut(key: Long, defaultValue: () -> Double): Double {
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

public abstract class AbstractLong2DoubleMap : Long2DoubleMap {

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

    public class SimpleEntry(private val _key: Long, private val _value: Double) : Long2DoubleMap.Entry {
        override fun key(): Long = _key
        override fun value(): Double = _value
    }
}

public abstract class AbstractMutableLong2DoubleMap : AbstractLong2DoubleMap(), MutableLong2DoubleMap {

    public class SimpleMutableEntry(private val _key: Long, private var _value: Double) : MutableLong2DoubleMap.MutableEntry {
        override fun key(): Long = _key
        override fun value(): Double = _value
        override fun setValue(newValue: Double): Double {
            val oldValue = _value
            _value = newValue
            return oldValue
        }
    }
}

private object EmptyLong2DoubleMap : Long2DoubleMap {
    override val defaultValue: Double get() = Double.NaN

    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false
    override fun containsValue(value: Double): Boolean = false
    override fun lookup(key: Long): Double = Double.NaN

    override val keys: LongSet get() = emptyLongSet()
    override val values: DoubleCollection get() = emptyDoubleList()
    override val primitiveEntries: EntrySet<Long2DoubleMap.Entry> = emptyEntrySet()
}

private class SingletonLong2DoubleMap(private val key: Long, private val value: Double) : Long2DoubleMap {
    override val defaultValue: Double get() = Double.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key == this.key
    override fun containsValue(value: Double): Boolean = value == this.value
    override fun lookup(key: Long): Double = if (key == this.key) value else Double.NaN

    override val keys: LongSet by lazy { longSetOf(key) }
    override val values: DoubleCollection by lazy { doubleListOf(value) }
    override val primitiveEntries: EntrySet<Long2DoubleMap.Entry> by lazy { entrySetOf(AbstractLong2DoubleMap.SimpleEntry(key, value)) }
}
