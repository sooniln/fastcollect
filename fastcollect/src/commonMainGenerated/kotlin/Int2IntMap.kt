/**
 * Methods for dealing with Int2IntMaps.
 */
@file:JvmName("Int2IntMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2IntMap(): Int2IntMap = EmptyInt2IntMap as Int2IntMap

@Suppress("UNCHECKED_CAST")
public fun  int2IntMapOf(): Int2IntMap = EmptyInt2IntMap as Int2IntMap
public fun  int2IntMapOf(key: Int, value: Int): Int2IntMap = SingletonInt2IntMap(key, value)
@JvmSynthetic
public fun  int2IntMapOf(entry: Pair<Int, Int>): Int2IntMap = SingletonInt2IntMap(entry.first, entry.second)
@JvmSynthetic
public fun  int2IntMapOf(vararg entries: Pair<Int, Int>): Int2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2IntMapOf(): MutableInt2IntMap = Int2IntHashMap()
@JvmSynthetic
public fun  mutableInt2IntMapOf(entry: Pair<Int, Int>): MutableInt2IntMap = Int2IntHashMap(1).apply { set(entry.first, entry.second) }
@JvmSynthetic
public fun  mutableInt2IntMapOf(vararg entries: Pair<Int, Int>): MutableInt2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  buildInt2IntMap(expectedSize: Int = 0, builderAction: MutableInt2IntMap.() -> Unit): Int2IntMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2IntHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Ints.
 *
 * A Int2IntMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface Int2IntMap : Int2IntTraversable {

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
    public fun isDefaultValue(value: @UnsafeVariance Int): Boolean

    public operator fun get(key: Int): Int

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): Int = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance Int): Int = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        foreachKey { k ->
            if (k equalsRaw key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Int): Boolean {
        foreach { _, v ->
            if (v equalsRaw value) return true
        }
        return false
    }

    @get:JvmName("keys")
    public val keys: IntSet

    @get:JvmName("values")
    public val values: IntCollection

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Int
        public val value: Int

        public operator fun component1(): Int = key
        public operator fun component2(): Int = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Int2IntMap.isNotEmpty(): Boolean = size != 0

public fun  Int2IntMap.asMap(): Map<Int, Int> = Int2IntMapWrapper(this)

public fun  Int2IntMap.Entry.asEntry(): Map.Entry<Int, Int> = Int2IntMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntMap.getOrElse(key: Int, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Int
}

/**
 * A mutable map of Ints to Ints.
 */
public interface MutableInt2IntMap : Int2IntMap, MutableInt2IntTraversable {

    public fun put(key: Int, value: Int): Int

    public fun putIfAbsent(key: Int, value: Int): Int {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

    public operator fun set(key: Int, value: Int) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: Int): Int = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Int): Int

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): Int = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Int, value: Int): Boolean

    public fun clear()

    public fun putAll(from: Int2IntMap) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Int, Int>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Int2IntMap.Entry {
        override var value: Int
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Int2IntMap.AbstractEntry(), MutableEntry
}

public fun  MutableInt2IntMap.asMap(): MutableMap<Int, Int> = MutableInt2IntMapWrapper(this)

public fun  MutableInt2IntMap.MutableEntry.asEntry(): MutableMap.MutableEntry<Int, Int> = MutableInt2IntMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2IntMap.merge(key: Int, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Int, value)
    if (absent || !(newValue equalsRaw oldValue)) {
        set(key, newValue)
    }
    return newValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2IntMap.getOrPut(key: Int, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Int
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2IntMap.replaceOrSet(key: Int, value: Int, oldValue: () -> Int): Int {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Int
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2IntMap.removeOrElse(key: Int, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Int
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2IntMap : Int2IntMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2IntMap) {
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

    override fun toString(): String = joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Int, override val value: Int) : Int2IntMap.AbstractEntry()
}

public abstract class AbstractMutableInt2IntMap : AbstractInt2IntMap(), MutableInt2IntMap


private object EmptyInt2IntMap : AbstractInt2IntMap() {


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false
    override val keys: IntSet get() = emptyIntSet()


    override fun isDefaultValue(value: Int): Boolean = true
    override fun containsValue(value: Int): Boolean = false
    override fun get(key: Int): Int = Int.MIN_VALUE
    override val values: IntCollection get() = emptyIntList()
    override fun iterator(): Iterator<Int2IntMap.Entry> = emptyList<Int2IntMap.Entry>().iterator()
    override fun traverser(): Int2IntTraverser = emptyInt2IntTraverser()

}

private class SingletonInt2IntMap(
    private val key: Int,
    private val value: Int
) : AbstractInt2IntMap() {
    override fun isDefaultValue(value: Int): Boolean = value equalsRaw Int.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsRaw this.key
    override fun containsValue(value: Int): Boolean = value equalsRaw this.value
    override fun get(key: Int): Int = if (key equalsRaw this.key) value else Int.MIN_VALUE

    override val keys: IntSet get() = intSetOf(key)

    override val values: IntCollection get() = intListOf(value)

    override fun iterator() = object : Iterator<Int2IntMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2IntMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Int2IntTraverser = object : Int2IntTraverser {
        private var complete = false
        override val key: Int get() {
            check(complete)
            return this@SingletonInt2IntMap.key
        }
        override val value: Int get() {
            check(complete)
            return this@SingletonInt2IntMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Int2IntMapWrapper(private val map: Int2IntMap) : AbstractMap<Int, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Int? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Int>> = object : AbstractSet<Map.Entry<Int, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Int>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Int>> = object : Iterator<Map.Entry<Int, Int>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Int> = it.next().asEntry()
        }
    }
}

private class Int2IntMapEntryWrapper(
    private val entry: Int2IntMap.Entry
) : Map.Entry<Int, Int> {
    override val key: Int get() = entry.key
    override val value: Int get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableInt2IntMapWrapper(private val map: MutableInt2IntMap) : AbstractMutableMap<Int, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Int? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override fun remove(key: Int): Int? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Int): Int? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Int>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Int>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Int>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Int>> = object : MutableIterator<MutableMap.MutableEntry<Int, Int>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Int> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Int, Int>): Unit = map.putAll(from)
}

private class MutableInt2IntMapEntryWrapper(
    private val entry: MutableInt2IntMap.MutableEntry
) : MutableMap.MutableEntry<Int, Int> {
    override val key: Int get() = entry.key
    override val value: Int get() = entry.value
    override fun setValue(newValue: Int): Int {
        val oldValue = entry.value
        entry.value = newValue
        return oldValue
    }
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}


public fun interface IntIntConsumer {

    public fun accept(key: Int, value: Int)
}
