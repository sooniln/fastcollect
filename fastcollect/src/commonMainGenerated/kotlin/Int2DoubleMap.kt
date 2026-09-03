/**
 * Methods for dealing with Int2DoubleMaps.
 */
@file:JvmName("Int2DoubleMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2DoubleMap(): Int2DoubleMap = EmptyInt2DoubleMap as Int2DoubleMap

@Suppress("UNCHECKED_CAST")
public fun  int2DoubleMapOf(): Int2DoubleMap = EmptyInt2DoubleMap as Int2DoubleMap
public fun  int2DoubleMapOf(key: Int, value: Double): Int2DoubleMap = SingletonInt2DoubleMap(key, value)
@JvmSynthetic
public fun  int2DoubleMapOf(entry: Pair<Int, Double>): Int2DoubleMap = SingletonInt2DoubleMap(entry.first, entry.second)
@JvmSynthetic
public fun  int2DoubleMapOf(vararg entries: Pair<Int, Double>): Int2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2DoubleMapOf(): MutableInt2DoubleMap = Int2DoubleHashMap()
@JvmSynthetic
public fun  mutableInt2DoubleMapOf(entry: Pair<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(1).apply { set(entry.first, entry.second) }
@JvmSynthetic
public fun  mutableInt2DoubleMapOf(vararg entries: Pair<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  buildInt2DoubleMap(expectedSize: Int = 0, builderAction: MutableInt2DoubleMap.() -> Unit): Int2DoubleMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2DoubleHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Doubles.
 *
 * A Int2DoubleMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface Int2DoubleMap : Int2DoubleTraversable {

    @get:JvmName("size")
    public val size: Int

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
    public fun isDefaultValue(value: @UnsafeVariance Double): Boolean

    public operator fun get(key: Int): Double

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): Double = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance Double): Double = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        traverseKeys { k ->
            if (k equalsRaw key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Double): Boolean {
        traverse { _, v ->
            if (v equalsRaw value) return true
        }
        return false
    }

    @get:JvmName("keys")
    public val keys: IntSet

    @get:JvmName("values")
    public val values: DoubleCollection

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Int
        public val value: Double

        public operator fun component1(): Int = key
        public operator fun component2(): Double = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Int2DoubleMap.isNotEmpty(): Boolean = size != 0

public fun  Int2DoubleMap.asMap(): Map<Int, Double> = Int2DoubleMapWrapper(this)

public fun  Int2DoubleMap.Entry.asEntry(): Map.Entry<Int, Double> = Int2DoubleMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleMap.getOrElse(key: Int, defaultValue: () -> Double): Double {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Double
}

/**
 * A mutable map of Ints to Doubles.
 */
public interface MutableInt2DoubleMap : Int2DoubleMap, MutableInt2DoubleTraversable {

    public fun put(key: Int, value: Double): Double

    public fun putIfAbsent(key: Int, value: Double): Double {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

    public operator fun set(key: Int, value: Double) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: Double): Double = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Int): Double

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): Double = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Int, value: Double): Boolean

    public fun clear()

    public fun putAll(from: Int2DoubleMap) {
        from.traverse { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Int, Double>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Int2DoubleMap.Entry {
        override var value: Double
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Int2DoubleMap.AbstractEntry(), MutableEntry
}

public fun  MutableInt2DoubleMap.asMap(): MutableMap<Int, Double> = MutableInt2DoubleMapWrapper(this)

public fun  MutableInt2DoubleMap.MutableEntry.asEntry(): MutableMap.MutableEntry<Int, Double> = MutableInt2DoubleMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.merge(key: Int, value: Double, merge: (oldValue: Double, value: Double) -> Double): Double {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Double, value)
    if (absent || !(newValue equalsRaw oldValue)) {
        set(key, newValue)
    }
    return newValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.getOrPut(key: Int, defaultValue: () -> Double): Double {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Double
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.replaceOrSet(key: Int, value: Double, oldValue: () -> Double): Double {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Double
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2DoubleMap.removeOrElse(key: Int, defaultValue: () -> Double): Double {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Double
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2DoubleMap : Int2DoubleMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2DoubleMap) {
            if (other.size != size) return false

            traverse { key, value ->
                if (!(other.getOrElse(key) { return false } equalsRaw value)) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        traverse { key, value ->
            result += key.hashCode() xor value.hashCode()
        }
        return result
    }

    override fun toString(): String = joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Int, override val value: Double) : Int2DoubleMap.AbstractEntry()
}

public abstract class AbstractMutableInt2DoubleMap : AbstractInt2DoubleMap(), MutableInt2DoubleMap


private object EmptyInt2DoubleMap : AbstractInt2DoubleMap() {


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false
    override val keys: IntSet get() = emptyIntSet()


    override fun isDefaultValue(value: Double): Boolean = true
    override fun containsValue(value: Double): Boolean = false
    override fun get(key: Int): Double = Double.NaN
    override val values: DoubleCollection get() = emptyDoubleList()
    override fun iterator(): Iterator<Int2DoubleMap.Entry> = emptyList<Int2DoubleMap.Entry>().iterator()
    override fun traverser(): Int2DoubleTraverser = emptyInt2DoubleTraverser()

}

private class SingletonInt2DoubleMap(
    private val key: Int,
    private val value: Double
) : AbstractInt2DoubleMap() {
    override fun isDefaultValue(value: Double): Boolean = value equalsRaw Double.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsRaw this.key
    override fun containsValue(value: Double): Boolean = value equalsRaw this.value
    override fun get(key: Int): Double = if (key equalsRaw this.key) value else Double.NaN

    override val keys: IntSet get() = intSetOf(key)

    override val values: DoubleCollection get() = doubleListOf(value)

    override fun iterator() = object : Iterator<Int2DoubleMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2DoubleMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Int2DoubleTraverser = object : Int2DoubleTraverser {
        private var complete = false
        override val key: Int get() {
            check(complete)
            return this@SingletonInt2DoubleMap.key
        }
        override val value: Double get() {
            check(complete)
            return this@SingletonInt2DoubleMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Int2DoubleMapWrapper(private val map: Int2DoubleMap) : AbstractMap<Int, Double>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Double? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Double): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Double>> = object : AbstractSet<Map.Entry<Int, Double>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Double>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Double>> = object : Iterator<Map.Entry<Int, Double>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Double> = it.next().asEntry()
        }
    }
}

private class Int2DoubleMapEntryWrapper(
    private val entry: Int2DoubleMap.Entry
) : Map.Entry<Int, Double> {
    override val key: Int get() = entry.key
    override val value: Double get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableInt2DoubleMapWrapper(private val map: MutableInt2DoubleMap) : AbstractMutableMap<Int, Double>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Double? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Double): Boolean = map.containsValue(value)

    override fun remove(key: Int): Double? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Double): Double? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Double>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Double>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Double>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Double>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Double>> = object : MutableIterator<MutableMap.MutableEntry<Int, Double>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Double> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Int, Double>): Unit = map.putAll(from)
}

private class MutableInt2DoubleMapEntryWrapper(
    private val entry: MutableInt2DoubleMap.MutableEntry
) : MutableMap.MutableEntry<Int, Double> {
    override val key: Int get() = entry.key
    override val value: Double get() = entry.value
    override fun setValue(newValue: Double): Double {
        val oldValue = entry.value
        entry.value = newValue
        return oldValue
    }
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}
