/**
 * Methods for dealing with Long2ByteMaps.
 */
@file:JvmName("Long2ByteMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2ByteMap(): Long2ByteMap = EmptyLong2ByteMap as Long2ByteMap

@Suppress("UNCHECKED_CAST")
public fun  long2ByteMapOf(): Long2ByteMap = EmptyLong2ByteMap as Long2ByteMap
public fun  long2ByteMapOf(entry: Pair<Long, Byte>): Long2ByteMap = SingletonLong2ByteMap(entry.first, entry.second)
public fun  long2ByteMapOf(vararg entries: Pair<Long, Byte>): Long2ByteMap = Long2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2ByteMapOf(): MutableLong2ByteMap = Long2ByteHashMap()
public fun  mutableLong2ByteMapOf(entry: Pair<Long, Byte>): MutableLong2ByteMap = Long2ByteHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableLong2ByteMapOf(vararg entries: Pair<Long, Byte>): MutableLong2ByteMap = Long2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildLong2ByteMap(expectedSize: Int = 0, builderAction: MutableLong2ByteMap.() -> Unit): Long2ByteMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2ByteHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Bytes.
 *
 * A Long2ByteMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */

public interface Long2ByteMap {


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
    public fun isDefaultValue(value: @UnsafeVariance Byte): Boolean

    public operator fun get(key: Long): Byte

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Long): Byte = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Long, defaultValue: @UnsafeVariance Byte): Byte = getOrElse(key) { defaultValue }

    public fun containsKey(key: Long): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Byte): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: LongSet
    public val values: ByteCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): LongSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): ByteCollection = values


    public interface Entry {

        public val key: Long
        public val value: Byte

        public operator fun component1(): Long = key
        public operator fun component2(): Byte = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
 
    public fun foreach(action: LongByteConsumer) {
 
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key, entry.value)
        }
    }

    /**
     * A method for iteration over keys guaranteed to be as fast or faster than [iterator].
     */
    public fun foreachKey(action: LongConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key)
        }
    }
}

public fun  Long2ByteMap.asMap(): Map<Long, Byte> = Long2ByteMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteMap.getOrElse(key: Long, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Byte
}

/**
 * A mutable map of Longs to Bytes.
 */
public interface MutableLong2ByteMap : Long2ByteMap {

    public fun put(key: Long, value: Byte): Byte

    public operator fun set(key: Long, value: Byte) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Long, value: Byte): Byte = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns it's value, or the default value if the key is not present. */
    public fun remove(key: Long): Byte

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Long): Byte = removeOrElse(key) { throw NoSuchElementException() }

    public fun clear()

    override val keys: MutableLongSet
    override val values: MutableByteCollection

    public fun putAll(from: Long2ByteMap) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Long, Byte>) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Long2ByteMap.Entry {
        override var value: Byte
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableLong2ByteMap.asMutableMap(): MutableMap<Long, Byte> = MutableLong2ByteMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2ByteMap.merge(key: Long, value: Byte, merge: (oldValue: Byte, value: Byte) -> Byte): Byte {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Byte, value)
    if (absent || !(newValue equalsBoxed oldValue)) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2ByteMap.getOrPut(key: Long, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Byte
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2ByteMap.replaceOrSet(key: Long, value: Byte, oldValue: () -> Byte): Byte {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Byte
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2ByteMap.removeOrElse(key: Long, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Byte
    } else {
        defaultValue()
    }
}

public abstract class AbstractLong2ByteMap : Long2ByteMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Long2ByteMap) {
            if (other.size != size) return false

            for (entry in this) {
                if (!(other[entry.key] equalsBoxed entry.value)) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        for (entry in this) {
            result += entry.key.hashCode() xor entry.value.hashCode()
        }
        return result
    }

    override fun toString(): String = Iterable { iterator() }.joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Long, override val value: Byte) : Long2ByteMap.Entry
}

public abstract class AbstractMutableLong2ByteMap : AbstractLong2ByteMap(), MutableLong2ByteMap


private object EmptyLong2ByteMap : Long2ByteMap {


    override fun isDefaultValue(value: Byte): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false

    override fun containsValue(value: Byte): Boolean = false
    override fun get(key: Long): Byte = Byte.MIN_VALUE


    override val keys: LongSet get() = emptyLongSet()

    override val values: ByteCollection get() = emptyByteList()
    override fun iterator() = emptyFastIterator<Long2ByteMap.Entry>()

}

private class SingletonLong2ByteMap(private val key: Long, private val value: Byte) : Long2ByteMap {
    override fun isDefaultValue(value: Byte): Boolean = value equalsBoxed Byte.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Byte): Boolean = value equalsBoxed this.value
    override fun get(key: Long): Byte = if (key equalsBoxed this.key) value else Byte.MIN_VALUE

    override val keys: LongSet by lazy { longSetOf(key) }

    override val values: ByteCollection by lazy { byteListOf(value) }

    override fun iterator() = object : FastIterator<Long2ByteMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2ByteMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractLong2ByteMap.SimpleEntry(key, value)
        }
    }
}

private class Long2ByteMapWrapper(private val map: Long2ByteMap) : AbstractMap<Long, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Byte? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Byte): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Byte>> = object : AbstractSet<Map.Entry<Long, Byte>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Byte>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, Byte>> = object : Iterator<Map.Entry<Long, Byte>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, Byte> {
                val entry = it.next()
                return object : Map.Entry<Long, Byte> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableLong2ByteMapWrapper(private val map: MutableLong2ByteMap) : AbstractMutableMap<Long, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Byte? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Byte): Boolean = map.containsValue(value)

    override fun remove(key: Long): Byte? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: Byte): Byte? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Byte>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, Byte>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, Byte>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, Byte>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, Byte>> = object : MutableIterator<MutableMap.MutableEntry<Long, Byte>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, Byte> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Long, value: Byte) : MutableMap.MutableEntry<Long, Byte> {
            override var value = value
                private set

            override fun setValue(newValue: Byte): Byte {
                val oldValue = value
                if (!(map.put(key, newValue) equalsBoxed oldValue)) throw ConcurrentModificationException()
                value = newValue
                return oldValue
            }

            override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
            override fun toString(): String = "$key=$value"
        }
    }

    override fun putAll(from: Map<out Long, Byte>): Unit = map.putAll(from)
}


public fun interface LongByteConsumer {

    public fun accept(key: Long, value: Byte)
}
