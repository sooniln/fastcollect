/**
 * Methods for dealing with Int2FloatMaps.
 */
@file:JvmName("Int2FloatMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2FloatMap(): Int2FloatMap = EmptyInt2FloatMap as Int2FloatMap

@Suppress("UNCHECKED_CAST")
public fun  int2FloatMapOf(): Int2FloatMap = EmptyInt2FloatMap as Int2FloatMap
public fun  int2FloatMapOf(key: Int, value: Float): Int2FloatMap = SingletonInt2FloatMap(key, value)
@JvmSynthetic
public fun  int2FloatMapOf(entry: Pair<Int, Float>): Int2FloatMap = SingletonInt2FloatMap(entry.first, entry.second)
@JvmSynthetic
public fun  int2FloatMapOf(vararg entries: Pair<Int, Float>): Int2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2FloatMapOf(): MutableInt2FloatMap = Int2FloatHashMap()
@JvmSynthetic
public fun  mutableInt2FloatMapOf(entry: Pair<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(1).apply { set(entry.first, entry.second) }
@JvmSynthetic
public fun  mutableInt2FloatMapOf(vararg entries: Pair<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  buildInt2FloatMap(expectedSize: Int = 0, builderAction: MutableInt2FloatMap.() -> Unit): Int2FloatMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2FloatHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Floats.
 *
 * A Int2FloatMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface Int2FloatMap : Int2FloatTraversable {

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
    public fun isDefaultValue(value: @UnsafeVariance Float): Boolean

    public operator fun get(key: Int): Float

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): Float = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance Float): Float = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        traverseKeys { k ->
            if (k equalsRaw key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Float): Boolean {
        traverse { _, v ->
            if (v equalsRaw value) return true
        }
        return false
    }

    @get:JvmName("keys")
    public val keys: IntSet

    @get:JvmName("values")
    public val values: FloatCollection

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Int
        public val value: Float

        public operator fun component1(): Int = key
        public operator fun component2(): Float = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Int2FloatMap.isNotEmpty(): Boolean = size != 0

public fun  Int2FloatMap.asMap(): Map<Int, Float> = Int2FloatMapWrapper(this)

public fun  Int2FloatMap.Entry.asEntry(): Map.Entry<Int, Float> = Int2FloatMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatMap.getOrElse(key: Int, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Float
}

/**
 * A mutable map of Ints to Floats.
 */
public interface MutableInt2FloatMap : Int2FloatMap, MutableInt2FloatTraversable {

    public fun put(key: Int, value: Float): Float

    public fun putIfAbsent(key: Int, value: Float): Float {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

    public operator fun set(key: Int, value: Float) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: Float): Float = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Int): Float

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): Float = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Int, value: Float): Boolean

    public fun clear()

    public fun putAll(from: Int2FloatMap) {
        from.traverse { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Int, Float>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Int2FloatMap.Entry {
        override var value: Float
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Int2FloatMap.AbstractEntry(), MutableEntry
}

public fun  MutableInt2FloatMap.asMap(): MutableMap<Int, Float> = MutableInt2FloatMapWrapper(this)

public fun  MutableInt2FloatMap.MutableEntry.asEntry(): MutableMap.MutableEntry<Int, Float> = MutableInt2FloatMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2FloatMap.merge(key: Int, value: Float, merge: (oldValue: Float, value: Float) -> Float): Float {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Float, value)
    if (absent || !(newValue equalsRaw oldValue)) {
        set(key, newValue)
    }
    return newValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2FloatMap.getOrPut(key: Int, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Float
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2FloatMap.replaceOrSet(key: Int, value: Float, oldValue: () -> Float): Float {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Float
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2FloatMap.removeOrElse(key: Int, defaultValue: () -> Float): Float {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Float
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2FloatMap : Int2FloatMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2FloatMap) {
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

    public class SimpleEntry(override val key: Int, override val value: Float) : Int2FloatMap.AbstractEntry()
}

public abstract class AbstractMutableInt2FloatMap : AbstractInt2FloatMap(), MutableInt2FloatMap


private object EmptyInt2FloatMap : AbstractInt2FloatMap() {


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false
    override val keys: IntSet get() = emptyIntSet()


    override fun isDefaultValue(value: Float): Boolean = true
    override fun containsValue(value: Float): Boolean = false
    override fun get(key: Int): Float = Float.NaN
    override val values: FloatCollection get() = emptyFloatList()
    override fun iterator(): Iterator<Int2FloatMap.Entry> = emptyList<Int2FloatMap.Entry>().iterator()
    override fun traverser(): Int2FloatTraverser = emptyInt2FloatTraverser()

}

private class SingletonInt2FloatMap(
    private val key: Int,
    private val value: Float
) : AbstractInt2FloatMap() {
    override fun isDefaultValue(value: Float): Boolean = value equalsRaw Float.NaN

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsRaw this.key
    override fun containsValue(value: Float): Boolean = value equalsRaw this.value
    override fun get(key: Int): Float = if (key equalsRaw this.key) value else Float.NaN

    override val keys: IntSet get() = intSetOf(key)

    override val values: FloatCollection get() = floatListOf(value)

    override fun iterator() = object : Iterator<Int2FloatMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2FloatMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Int2FloatTraverser = object : Int2FloatTraverser {
        private var complete = false
        override val key: Int get() {
            check(complete)
            return this@SingletonInt2FloatMap.key
        }
        override val value: Float get() {
            check(complete)
            return this@SingletonInt2FloatMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Int2FloatMapWrapper(private val map: Int2FloatMap) : AbstractMap<Int, Float>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Float? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Float): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Float>> = object : AbstractSet<Map.Entry<Int, Float>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Float>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Float>> = object : Iterator<Map.Entry<Int, Float>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Float> = it.next().asEntry()
        }
    }
}

private class Int2FloatMapEntryWrapper(
    private val entry: Int2FloatMap.Entry
) : Map.Entry<Int, Float> {
    override val key: Int get() = entry.key
    override val value: Float get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableInt2FloatMapWrapper(private val map: MutableInt2FloatMap) : AbstractMutableMap<Int, Float>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Float? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Float): Boolean = map.containsValue(value)

    override fun remove(key: Int): Float? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Float): Float? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Float>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Float>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Float>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Float>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Float>> = object : MutableIterator<MutableMap.MutableEntry<Int, Float>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Float> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Int, Float>): Unit = map.putAll(from)
}

private class MutableInt2FloatMapEntryWrapper(
    private val entry: MutableInt2FloatMap.MutableEntry
) : MutableMap.MutableEntry<Int, Float> {
    override val key: Int get() = entry.key
    override val value: Float get() = entry.value
    override fun setValue(newValue: Float): Float {
        val oldValue = entry.value
        entry.value = newValue
        return oldValue
    }
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}
