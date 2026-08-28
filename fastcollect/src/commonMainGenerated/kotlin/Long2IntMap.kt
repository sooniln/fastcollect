/**
 * Methods for dealing with Long2IntMaps.
 */
@file:JvmName("Long2IntMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2IntMap(): Long2IntMap = EmptyLong2IntMap as Long2IntMap

@Suppress("UNCHECKED_CAST")
public fun  long2IntMapOf(): Long2IntMap = EmptyLong2IntMap as Long2IntMap
public fun  long2IntMapOf(entry: Pair<Long, Int>): Long2IntMap = SingletonLong2IntMap(entry.first, entry.second)
public fun  long2IntMapOf(vararg entries: Pair<Long, Int>): Long2IntMap = Long2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2IntMapOf(): MutableLong2IntMap = Long2IntHashMap()
public fun  mutableLong2IntMapOf(entry: Pair<Long, Int>): MutableLong2IntMap = Long2IntHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableLong2IntMapOf(vararg entries: Pair<Long, Int>): MutableLong2IntMap = Long2IntHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  buildLong2IntMap(expectedSize: Int = 0, builderAction: MutableLong2IntMap.() -> Unit): Long2IntMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2IntHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Ints.
 *
 * A Long2IntMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Long2IntMap : Long2IntTraversable {

    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

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

    public operator fun get(key: Long): Int

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Long): Int = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Long, defaultValue: @UnsafeVariance Int): Int = getOrElse(key) { defaultValue }

    public fun containsKey(key: Long): Boolean {
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

    public val keys: LongSet
    public val values: IntCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): IntCollection = values

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Long
        public val value: Int

        public operator fun component1(): Long = key
        public operator fun component2(): Int = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Long2IntMap.asMap(): Map<Long, Int> = Long2IntMapWrapper(this)

public fun  Long2IntMap.Entry.asEntry(): Map.Entry<Long, Int> = Long2IntMapEntryWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntMap.getOrElse(key: Long, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Int
}

/**
 * A mutable map of Longs to Ints.
 */
public interface MutableLong2IntMap : Long2IntMap, MutableLong2IntTraversable {

    public fun put(key: Long, value: Int): Int

    public fun putIfAbsent(key: Long, value: Int): Int {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

    public operator fun set(key: Long, value: Int) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Long, value: Int): Int = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Long): Int

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Long): Int = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Long, value: Int): Boolean

    public fun clear()

    public fun putAll(from: Long2IntMap) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Long, Int>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Long2IntMap.Entry {
        override var value: Int
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Long2IntMap.AbstractEntry(), MutableEntry
}

public fun  MutableLong2IntMap.asMap(): MutableMap<Long, Int> = MutableLong2IntMapWrapper(this)

public fun  MutableLong2IntMap.MutableEntry.asEntry(): MutableMap.MutableEntry<Long, Int> = MutableLong2IntMapEntryWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2IntMap.merge(key: Long, value: Int, merge: (oldValue: Int, value: Int) -> Int): Int {
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

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2IntMap.getOrPut(key: Long, defaultValue: () -> Int): Int {
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
public inline fun  MutableLong2IntMap.replaceOrSet(key: Long, value: Int, oldValue: () -> Int): Int {
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
public inline fun  MutableLong2IntMap.removeOrElse(key: Long, defaultValue: () -> Int): Int {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Int
    } else {
        defaultValue()
    }
}

public abstract class AbstractLong2IntMap : Long2IntMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Long2IntMap) {
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

    override fun toString(): String = Iterable { iterator() }.joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Long, override val value: Int) : Long2IntMap.AbstractEntry()
}

public abstract class AbstractMutableLong2IntMap : AbstractLong2IntMap(), MutableLong2IntMap


private object EmptyLong2IntMap : AbstractLong2IntMap() {


    override fun isDefaultValue(value: Int): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Int): Boolean = false
    override fun get(key: Long): Int = Int.MIN_VALUE


    override val keys: LongSet get() = emptyLongSet()

    override val values: IntCollection get() = emptyIntList()
    override fun iterator(): Iterator<Long2IntMap.Entry> = emptyList<Long2IntMap.Entry>().iterator()



    override fun traverser(): Long2IntTraverser = emptyLong2IntTraverser()

}

private class SingletonLong2IntMap(
    private val key: Long,
    private val value: Int
) : AbstractLong2IntMap() {
    override fun isDefaultValue(value: Int): Boolean = value equalsRaw Int.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key equalsRaw this.key
    override fun containsValue(value: Int): Boolean = value equalsRaw this.value
    override fun get(key: Long): Int = if (key equalsRaw this.key) value else Int.MIN_VALUE

    override val keys: LongSet get() = longSetOf(key)

    override val values: IntCollection get() = intListOf(value)

    override fun iterator() = object : Iterator<Long2IntMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2IntMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Long2IntTraverser = object : Long2IntTraverser {
        private var complete = false
        override val key: Long get() {
            check(complete)
            return this@SingletonLong2IntMap.key
        }
        override val value: Int get() {
            check(complete)
            return this@SingletonLong2IntMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Long2IntMapWrapper(private val map: Long2IntMap) : AbstractMap<Long, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Int? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Int>> = object : AbstractSet<Map.Entry<Long, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Int>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, Int>> = object : Iterator<Map.Entry<Long, Int>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, Int> = it.next().asEntry()
        }
    }
}

private class Long2IntMapEntryWrapper(
    private val entry: Long2IntMap.Entry
) : Map.Entry<Long, Int> {
    override val key: Long get() = entry.key
    override val value: Int get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableLong2IntMapWrapper(private val map: MutableLong2IntMap) : AbstractMutableMap<Long, Int>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Int? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Int): Boolean = map.containsValue(value)

    override fun remove(key: Long): Int? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: Int): Int? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Int>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, Int>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, Int>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, Int>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, Int>> = object : MutableIterator<MutableMap.MutableEntry<Long, Int>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, Int> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Long, Int>): Unit = map.putAll(from)
}

private class MutableLong2IntMapEntryWrapper(
    private val entry: MutableLong2IntMap.MutableEntry
) : MutableMap.MutableEntry<Long, Int> {
    override val key: Long get() = entry.key
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


public fun interface LongIntConsumer {

    public fun accept(key: Long, value: Int)
}
