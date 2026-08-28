/**
 * Methods for dealing with Int2LongMaps.
 */
@file:JvmName("Int2LongMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2LongMap(): Int2LongMap = EmptyInt2LongMap as Int2LongMap

public fun  int2LongMapOf(): Int2LongMap = EmptyInt2LongMap as Int2LongMap
public fun  int2LongMapOf(entry: Pair<Int, Long>): Int2LongMap = SingletonInt2LongMap(entry.first, entry.second)
public fun  int2LongMapOf(vararg entries: Pair<Int, Long>): Int2LongMap = Int2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2LongMapOf(): MutableInt2LongMap = Int2LongHashMap()
public fun  mutableInt2LongMapOf(entry: Pair<Int, Long>): MutableInt2LongMap = Int2LongHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2LongMapOf(vararg entries: Pair<Int, Long>): MutableInt2LongMap = Int2LongHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  buildInt2LongMap(expectedSize: Int = 0, builderAction: MutableInt2LongMap.() -> Unit): Int2LongMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2LongHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Longs.
 *
 * A Int2LongMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
public interface Int2LongMap : Int2LongTraversable {

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
    public fun isDefaultValue(value: @UnsafeVariance Long): Boolean

    public operator fun get(key: Int): Long

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): Long = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance Long): Long = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        foreachKey { k ->
            if (k equalsRaw key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Long): Boolean {
        foreach { _, v ->
            if (v equalsRaw value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: LongCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): LongCollection = values

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Int
        public val value: Long

        public operator fun component1(): Int = key
        public operator fun component2(): Long = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Int2LongMap.asMap(): Map<Int, Long> = Int2LongMapWrapper(this)

public fun  Int2LongMap.Entry.asEntry(): Map.Entry<Int, Long> = Int2LongMapEntryWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongMap.getOrElse(key: Int, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Long
}

/**
 * A mutable map of Ints to Longs.
 */
public interface MutableInt2LongMap : Int2LongMap, MutableInt2LongTraversable {

    public fun put(key: Int, value: Long): Long

    public fun putIfAbsent(key: Int, value: Long): Long {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

    public operator fun set(key: Int, value: Long) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: Long): Long = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Int): Long

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): Long = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Int, value: Long): Boolean

    public fun clear()

    public fun putAll(from: Int2LongMap) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Int, Long>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Int2LongMap.Entry {
        override var value: Long
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Int2LongMap.AbstractEntry(), MutableEntry
}

public fun  MutableInt2LongMap.asMap(): MutableMap<Int, Long> = MutableInt2LongMapWrapper(this)

public fun  MutableInt2LongMap.MutableEntry.asEntry(): MutableMap.MutableEntry<Int, Long> = MutableInt2LongMapEntryWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2LongMap.merge(key: Int, value: Long, merge: (oldValue: Long, value: Long) -> Long): Long {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Long, value)
    if (absent || !(newValue equalsRaw oldValue)) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2LongMap.getOrPut(key: Int, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Long
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2LongMap.replaceOrSet(key: Int, value: Long, oldValue: () -> Long): Long {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Long
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2LongMap.removeOrElse(key: Int, defaultValue: () -> Long): Long {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Long
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2LongMap : Int2LongMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2LongMap) {
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

    public class SimpleEntry(override val key: Int, override val value: Long) : Int2LongMap.AbstractEntry()
}

public abstract class AbstractMutableInt2LongMap : AbstractInt2LongMap(), MutableInt2LongMap


private object EmptyInt2LongMap : AbstractInt2LongMap() {


    override fun isDefaultValue(value: Long): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Long): Boolean = false
    override fun get(key: Int): Long = Long.MIN_VALUE


    override val keys: IntSet get() = emptyIntSet()

    override val values: LongCollection get() = emptyLongList()
    override fun iterator(): Iterator<Int2LongMap.Entry> = emptyList<Int2LongMap.Entry>().iterator()



    override fun traverser(): Int2LongTraverser = emptyInt2LongTraverser()

}

private class SingletonInt2LongMap(
    private val key: Int,
    private val value: Long
) : AbstractInt2LongMap() {
    override fun isDefaultValue(value: Long): Boolean = value equalsRaw Long.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsRaw this.key
    override fun containsValue(value: Long): Boolean = value equalsRaw this.value
    override fun get(key: Int): Long = if (key equalsRaw this.key) value else Long.MIN_VALUE

    override val keys: IntSet get() = intSetOf(key)

    override val values: LongCollection get() = longListOf(value)

    override fun iterator() = object : Iterator<Int2LongMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2LongMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Int2LongTraverser = object : Int2LongTraverser {
        private var complete = false
        override val key: Int get() {
            check(complete)
            return this@SingletonInt2LongMap.key
        }
        override val value: Long get() {
            check(complete)
            return this@SingletonInt2LongMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Int2LongMapWrapper(private val map: Int2LongMap) : AbstractMap<Int, Long>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Long? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Long): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Long>> = object : AbstractSet<Map.Entry<Int, Long>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Long>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Long>> = object : Iterator<Map.Entry<Int, Long>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Long> = it.next().asEntry()
        }
    }
}

private class Int2LongMapEntryWrapper(
    private val entry: Int2LongMap.Entry
) : Map.Entry<Int, Long> {
    override val key: Int get() = entry.key
    override val value: Long get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableInt2LongMapWrapper(private val map: MutableInt2LongMap) : AbstractMutableMap<Int, Long>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Long? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Long): Boolean = map.containsValue(value)

    override fun remove(key: Int): Long? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Long): Long? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Long>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Long>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Long>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Long>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Long>> = object : MutableIterator<MutableMap.MutableEntry<Int, Long>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Long> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Int, Long>): Unit = map.putAll(from)
}

private class MutableInt2LongMapEntryWrapper(
    private val entry: MutableInt2LongMap.MutableEntry
) : MutableMap.MutableEntry<Int, Long> {
    override val key: Int get() = entry.key
    override val value: Long get() = entry.value
    override fun setValue(newValue: Long): Long {
        val oldValue = entry.value
        entry.value = newValue
        return oldValue
    }
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}


public fun interface IntLongConsumer {

    public fun accept(key: Int, value: Long)
}
